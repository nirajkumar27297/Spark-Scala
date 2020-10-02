import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object StockPredictionPythonConnectivity extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
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
    .csv("./src/test/resources/GOOG.csv")
  inputDF.printSchema()
  inputDF.show()

  val newRdd = inputDF.rdd.pipe(
    "python3 /home/niraj/PycharmProjects/pythonProject/Spark-Connector/StockPrediction.py /home/niraj/IdeaProjects/Practice-Spark/src/test/resources/"
  )
  newRdd.foreach(println(_))
  spark.stop()

}
