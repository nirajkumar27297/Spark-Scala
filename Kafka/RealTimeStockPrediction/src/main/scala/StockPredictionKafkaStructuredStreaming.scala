import Utility.UtilityClass
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame}
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
object StockPredictionKafkaStructuredStreaming extends App {
  val brokers = args(0)
  val groupId = "GRP1"
  val topics = args(1)

  val sparkSessionObj = UtilityClass.createSessionObject("StockPrediction")
  sparkSessionObj.sparkContext.setLogLevel("ERROR")
  import sparkSessionObj.implicits._
  val streamedDataFrame = takingInput()
  val preprocessedDataFrame = preProcessing(streamedDataFrame)
  val predictedClosePrice = predictingPrice(preprocessedDataFrame)
  writeToOutputStream(predictedClosePrice)

  def takingInput(): DataFrame = {
    val inputDataFrame = sparkSessionObj.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topics)
      .load()
    inputDataFrame
  }

  private def creatingDataFrameFromJson(
      inputDataFrame: DataFrame
  ): DataFrame = {
    val schema = new StructType()
      .add("1. open", StringType, true)
      .add("2. high", StringType, true)
      .add("3. low", StringType, true)
      .add("4. close", StringType, true)
      .add("5. volume", StringType, true)

    val jsonStringDataFrame =
      inputDataFrame.selectExpr("CAST(value AS STRING)").as[String]
    val columnsRenamedDataFrame = jsonStringDataFrame
      .select(from_json(col("value"), schema).as("jsonData"))
      .select("jsonData.*")
      .withColumnRenamed("1. open", "Open")
      .withColumnRenamed("2. high", "High")
      .withColumnRenamed("3. low", "Low")
      .withColumnRenamed("4. close", "Close")
      .withColumnRenamed("5. volume", "Volume")

    columnsRenamedDataFrame

  }
  private def castingDataType(inputDataFrame: DataFrame): DataFrame = {
    val castedDataFrame = inputDataFrame.select(
      col("Open").cast(DoubleType),
      col("High").cast(DoubleType),
      col("Low").cast(DoubleType),
      col("Volume").cast(DoubleType)
    )
    castedDataFrame
  }

  def preProcessing(inputDataFrame: DataFrame): DataFrame = {
    val columnsRenamedDataFrame = creatingDataFrameFromJson(inputDataFrame)
    val castedDataFrame = castingDataType(columnsRenamedDataFrame)
    castedDataFrame
  }

  private def loadingLinearRegressionModelSpark(
      inputDataFrame: DataFrame
  ): DataFrame = {
    val linearRegressionModel =
      PipelineModel.load("./model")
    //Applying the model to our Input DataFrame
    val predictedDataFrame = linearRegressionModel.transform(inputDataFrame)
    //Extracting the Predicted Close Price from the Output DataFrame
    predictedDataFrame

  }

  private def loadingLinearRegressionModelPython(
      inputDataFrame: DataFrame
  ): DataFrame = {
    val command = "python3 ./pythonFiles/StockPricePrediction.py"
//    //creating rdd with the input files,repartitioning the rdd and passing the command using pipe
    val predictedPriceRDD = inputDataFrame.rdd
      .pipe(command)
//    //Collecting the result from the output RDD.
    predictedPriceRDD.toDF().show()
    predictedPriceRDD.toDF()
  }

  def predictingPrice(inputDataFrame: DataFrame): DataFrame = {
    val predictedDataFrame = loadingLinearRegressionModelSpark(inputDataFrame)
    val predictedClosePrice = predictedDataFrame.select(
      col("prediction").alias("Predicted Close Price"),
      col("Open"),
      col("Low"),
      col("High"),
      col("Volume")
    )
    predictedClosePrice
  }

  def writeToOutputStream(inputDataFrame: DataFrame): Unit = {
    val outputQuery = inputDataFrame.writeStream
      .format("console")
      .outputMode("update")
      .start()
      .awaitTermination()
  }

}
