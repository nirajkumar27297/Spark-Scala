package UnstrcuturedStreaming
import UtilityPackage.Utility
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{DataFrame}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{
  ConsumerStrategies,
  KafkaUtils,
  LocationStrategies
}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StockPredictionKafkaUnstructured extends App {
  val brokers = args(0)
  val groupId = args(1)
  val topics = args(2)
  val spark = Utility.createSessionObject("Real Time Stock Prediction")
  val sparkContextObj = spark.sparkContext
  val ssc = new StreamingContext(sparkContextObj, Seconds(3))
  sparkContextObj.setLogLevel("OFF")

  val kafkaParams = KafkaConfiguration.configureKafka(brokers, groupId)
  val messages = creatingDirectStream(topics, kafkaParams)
  predictStockClosePrice()
  startStreaming()

  def startStreaming(): Unit = {
    ssc.start()
    ssc.awaitTermination()
  }

  def creatingDirectStream(
      topics: String,
      kafkaParams: Map[String, Object]
  ): InputDStream[ConsumerRecord[String, String]] = {
    val topicSet = topics.split(",").toSet
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
    )
    messages
  }

  def predictStockClosePrice() = {
    val stockInfoDstreams = messages.map(_.value())
    stockInfoDstreams.foreachRDD(jsonRDD =>
      predictClosePriceFromEachRDD(jsonRDD.coalesce(1))
    )
  }

  /**
    *
    * @param inputRDD
    */
  private def predictClosePriceFromEachRDD(inputRDD: RDD[String]) = {
    val jsonStrings = inputRDD.collect()
    jsonStrings.foreach { jsonString => getClosePrice(jsonString) }
  }

  /**
    *
    * @param inputDataFrame
    * @return
    */

  private def loadingLinearRegressionModelPython(
      inputDataFrame: DataFrame
  ): DataFrame = {
    val command = "python3 ./pythonFiles/StockPricePrediction.py"
    //    //creating rdd with the input files,repartitioning the rdd and passing the command using pipe
    val predictedPriceRDD =
      inputDataFrame.rdd
        .repartition(1)
        .pipe(command)
    //Collecting the result from the output RDD.
    val predictedPrice = predictedPriceRDD.collect().apply(0)
    val scaledPredictedPrice = BigDecimal(predictedPrice)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble
    val predictedColumnDataFrame =
      inputDataFrame.withColumn("PredictedPrice", lit(scaledPredictedPrice))
    predictedColumnDataFrame.printSchema()
    predictedColumnDataFrame.show(10)
    predictedColumnDataFrame
  }

  /**
    *
    * @param inputDataFrame
    * @return
    */

  private def castingDataTypeOfDataFrame(
      inputDataFrame: DataFrame
  ): DataFrame = {
    val castedDataFrame = inputDataFrame.select(
      col("Open").cast(DoubleType),
      col("High").cast(DoubleType),
      col("Low").cast(DoubleType),
      col("Volume").cast(DoubleType)
    )
    castedDataFrame
  }

  /**
    *
    * @param jsonString
    * @return
    */

  private def creatingDataFrameFromJsonString(jsonString: String): DataFrame = {
    import spark.implicits._
    val jsonDataFrame = spark.read
      .json(Seq(jsonString).toDS())
      .withColumnRenamed("1. open", "Open")
      .withColumnRenamed("2. high", "High")
      .withColumnRenamed("3. low", "Low")
      .withColumnRenamed("4. close", "Close")
      .withColumnRenamed("5. volume", "Volume")
    jsonDataFrame

  }

  /**
    *
    * @param jsonString
    */

  private def getClosePrice(jsonString: String) = {
    val jsonDataFrame = creatingDataFrameFromJsonString(jsonString)
    val castedDF = castingDataTypeOfDataFrame(jsonDataFrame)
    val predictedColumnDataFrame = loadingLinearRegressionModelPython(castedDF)
    predictedColumnDataFrame.write
      .mode("append")
      .option("header", true)
      .csv(
        args(3)
      )
  }
}
