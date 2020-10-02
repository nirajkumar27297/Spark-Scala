package StockPrediction

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{avg, col}

object StockPredictionPipeline extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("StockPrediction")
      .getOrCreate()

    spark
  }

  def metricsEvaluation(test: DataFrame) {
    val prediction = lrModel.transform(test)
    prediction.show()
    val evaluator = new RegressionEvaluator()
      .setLabelCol("Close")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmseValue = evaluator.evaluate(prediction)
    println(s"The RMSE value is %1.2f".format(rmseValue))
    val mean =
      prediction.select(avg(col("prediction"))).collect().apply(0).getDouble(0)
    println("The average value is %1.2f".format(mean))
    val errorValue = rmseValue / mean
    println(s"The error percentage is %1.2f%%".format(errorValue))
  }

  def preProcessingAndAssembling(): VectorAssembler = {
    val assembler = new VectorAssembler()
      .setInputCols(Array("Open", "High", "Low", "Volume"))
      .setOutputCol("features")
    assembler
  }

  def scalingData(): StandardScaler = {
    val scaler =
      new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures")
    scaler
  }

  def modelCreation(): LinearRegression = {
    val linearRegression =
      new LinearRegression()
        .setFeaturesCol("scaledFeatures")
        .setLabelCol("Close")

    linearRegression
  }

  def takeInput(filepath: String): DataFrame = {
    val inputDF = spark.read
      .option("header", true)
      .option("inferSchema", true)
      .csv(filepath)
    inputDF.printSchema()
    inputDF.show()
    inputDF
  }

  val spark = createSession()
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  val inputDF = takeInput("./src/test/resources/GOOG.csv")
  val assembler = preProcessingAndAssembling()
  val scaler = scalingData()
  val linearRegression = modelCreation()
  val pipeline =
    new Pipeline().setStages(Array(assembler, scaler, linearRegression))
  val split = inputDF.randomSplit(Array(0.80, 0.20), 111)
  val train = split.apply(0)
  val test = split.apply(1)
  val lrModel = pipeline.fit(train)
  lrModel.write.overwrite().save("./src/test/resources/model")
  metricsEvaluation(test)
  spark.stop()
}
