package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

object LinearRegressionDemo extends App {

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("LinearRegression")
    .getOrCreate()

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val inputDF = spark.read
    .option("header", true)
    .option("inferSchema", true)
    .csv("./src/test/resources/Clean-USA-Housing.csv")

  inputDF.printSchema()
  inputDF.show()
  inputDF.head(2)
  val df = inputDF.select(
    col("Price").as("label"),
    col("Avg Area Income"),
    col("Avg Area House Age"),
    col("Avg Area Number of Rooms"),
    col("Avg Area Number of Bedrooms"),
    col("Area Population")
  )
  val assembler = new VectorAssembler()
    .setInputCols(
      Array(
        "Avg Area Income",
        "Avg Area House Age",
        "Avg Area Number of Rooms",
        "Avg Area Number of Bedrooms",
        "Area Population"
      )
    )
    .setOutputCol("features")

  val output = assembler.transform(df).select("label", "features")
  output.show()
  val lr = new LinearRegression()
  val lrModel = lr.fit(output)
  val trainingSummary = lrModel.summary
  trainingSummary.residuals.show()
  print(trainingSummary.rootMeanSquaredError)
  println(trainingSummary.predictions.show())

}
