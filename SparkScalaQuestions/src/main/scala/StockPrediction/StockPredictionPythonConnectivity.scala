package StockPrediction

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object StockPredictionPythonConnectivity extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .appName("StockPredictionPythonConnectivity")
      .getOrCreate()
    spark
  }
  val spark = createSession()
  val sc = spark.sparkContext
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  val inputDF = spark.read
    .option("header", true)
    .option("inferSchema", true)
    .csv(args(0))
  inputDF.printSchema()
  inputDF.show()
  val command = "python3" + " " + args(1)

  val newRdd = inputDF.rdd.pipe(command)
  newRdd.foreach(println(_))
  spark.stop()
}
