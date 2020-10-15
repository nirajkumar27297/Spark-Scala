import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}

object StockPredictionKafkaStructuredStreaming extends App {
  val brokers = "localhost:9092"
  val groupId = "GRP1"
  val topics = "kafkatutorial"

 val spark = SparkSession.builder().master("local[*]").getOrCreate()
  val df = spark.readStream.format("kafka").option("kafka.bootstrap.servers",brokers).option("subscribe",topics).load()
  val schema = new StructType()
    .add("1. open", StringType, true)
    .add("2. high", StringType, true)
    .add("3. low", StringType, true)
    .add("4. close", StringType, true)
    .add("5. volume", StringType, true)
  val rawDF = df.selectExpr("CAST(value AS STRING)").as[String]
  val dfFromCSVJSON =  rawDF.select(from_json(col("value"),schema).as("jsonData"))
  .select("jsonData.*").withColumnRenamed("1. open","Open").withColumnRenamed("2. high","High")
    .withColumnRenamed("3. low","Low").withColumnRenamed("4. close","Close")
    .withColumnRenamed("5. volume","Volume")
  val castedDF = dfFromCSVJSON.select(col("Open").cast(DoubleType), col("High").cast(DoubleType), col("Low").cast(DoubleType),col("Volume").cast(DoubleType))
  castedDF.printSchema()
  val query = castedDF.writeStream.format("console").outputMode("update").start().awaitTermination()
}

