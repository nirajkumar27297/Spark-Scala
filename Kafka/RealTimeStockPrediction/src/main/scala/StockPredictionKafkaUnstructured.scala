import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.ml.PipelineModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.streaming.kafka010.{
  ConsumerStrategies,
  KafkaUtils,
  LocationStrategies
}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StockPredictionKafkaUnstructured extends App {
  val brokers = "localhost:9092"
  val groupId = "GRP1"
  val topics = "kafkatutorial"

  val sparkConf =
    new SparkConf().setMaster("local[*]").setAppName("KAFKAStreaming")
  val ssc = new StreamingContext(sparkConf, Seconds(3))
  val sc = ssc.sparkContext
  sc.setLogLevel("OFF")
  val topicSet = topics.split(",").toSet
  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
    ConsumerConfig.GROUP_ID_CONFIG -> groupId,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[
      StringDeserializer
    ]
  )
  val messages = KafkaUtils.createDirectStream[String, String](
    ssc,
    LocationStrategies.PreferConsistent,
    ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
  )
  val line = messages.map(_.value())
  val spark = SparkSession.builder().master("local[*]").getOrCreate()
  line.foreachRDD(x => predictPrice(x))

  ssc.start()
  ssc.awaitTermination()

  private def predictPrice(inputRDD: RDD[String]) = {
    val jsonStrings = inputRDD.collect()
    jsonStrings.foreach { jsonString => getClosePricePrediction(jsonString) }
  }
  private def getClosePricePrediction(jsonString: String) = {
    import spark.implicits._
    val df = spark.read
      .json(Seq(jsonString).toDS())
      .withColumnRenamed("1. open", "Open")
      .withColumnRenamed("2. high", "High")
      .withColumnRenamed("3. low", "Low")
      .withColumnRenamed("4. close", "Close")
      .withColumnRenamed("5. volume", "Volume")
    val castedDF = df.select(
      col("Open").cast(DoubleType),
      col("High").cast(DoubleType),
      col("Low").cast(DoubleType),
      col("Volume").cast(DoubleType)
    )
    val command = "python3 ./pythonFiles/StockPricePrediction.py"
    //    //creating rdd with the input files,repartitioning the rdd and passing the command using pipe
    val predictedPriceRDD = castedDF.rdd
      .coalesce(1)
      .pipe(command)
    //    //Collecting the result from the output RDD.
    predictedPriceRDD.foreach(println(_))
  }
}
