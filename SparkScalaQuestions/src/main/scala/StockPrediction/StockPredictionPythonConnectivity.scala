package StockPrediction

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object StockPredictionPythonConnectivity extends App {

  def createSession(): SparkSession = {
    val conf = new SparkConf().setAppName("Simple Application")
      .setMaster("local")
    val sparkSessionObj = SparkSession
      .builder()
      .appName("StockPredictionPythonConnectivity").config(conf)
      .getOrCreate()
    sparkSessionObj
  }

  def configutaionsHadoop() = {
    System.setProperty("com.amazonaws.services.s3.enableV4", "true")
    sparkContextObj.hadoopConfiguration.set("fs.s3a.access.key",System.getenv("AWS_ACCESS_KEY_ID"))
    sparkContextObj.hadoopConfiguration.set("fs.s3a.secret.key",System.getenv("AWS_SECRET_ACCESS_KEY"))
    sparkContextObj.hadoopConfiguration.set("fs.s3a.endpoint","s3.ap-south-1.amazonaws.com")
    sparkContextObj.hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
  }

  val sparkSessionObj = createSession()
  val sparkContextObj = sparkSessionObj.sparkContext
  //Configuring Hadoop
  configutaionsHadoop()
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  val inputStockPriceDF = sparkSessionObj.read
    .option("header", true)
    .option("inferSchema", true)
    .csv("s3a://getstocksdata/GOOG.csv")
  inputStockPriceDF.printSchema()
  inputStockPriceDF.show()

  val ouputPipedRdd = inputStockPriceDF.rdd.pipe("python3 ./src/test/resources/StockPrediction.py")
  ouputPipedRdd.foreach(println(_))
  sparkSessionObj.stop()
}
