package StockPrediction

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{avg, col}

object StockPrediction extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("MinMaxScaler")
      .getOrCreate()

    spark
  }

  def metricsEvaluation(test: DataFrame) {
    val prediction = lrModel.transform(test)
    println("The r-squared value is %1.2f".format(lrModel.summary.r2))
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

  def preProcessingAndAssembling(inputDF: DataFrame): DataFrame = {
    val inputDFDroppedColumns = inputDF.drop("Adj Close", "Date")
    inputDFDroppedColumns.show()

    val assembler = new VectorAssembler()
      .setInputCols(inputDFDroppedColumns.drop("Close").columns)
      .setOutputCol("features")

    val assembledOutput =
      assembler
        .transform(inputDFDroppedColumns.drop())
        .select("Close", "features")
    assembledOutput.show()
    assembledOutput
  }

  def scalingData(inputDF: DataFrame): DataFrame = {
    val scaler =
      new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures")
    val scalerModel = scaler.fit(inputDF)
    val scaledDF =
      scalerModel.transform(inputDF).select("Close", "scaledFeatures")
    scaledDF.show()
    scaledDF
  }

  def modelCreation(inputDF: DataFrame): LinearRegressionModel = {
    val lr =
      new LinearRegression()
        .setFeaturesCol("scaledFeatures")
        .setLabelCol("Close")

    val lrModel = lr.fit(inputDF)
    lrModel
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
  val assembledDF = preProcessingAndAssembling(inputDF)
  val scaledDF = scalingData(assembledDF)
  val split = scaledDF.randomSplit(Array(0.80, 0.20), 111)
  val train = split.apply(0)
  val test = split.apply(1)
  val lrModel = modelCreation(train)
  metricsEvaluation(test)
  spark.stop()

}
