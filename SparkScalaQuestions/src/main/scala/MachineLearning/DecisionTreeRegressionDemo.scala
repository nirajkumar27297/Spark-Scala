package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{Imputer, MinMaxScaler, VectorAssembler}
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.sql.functions.{avg, col}
import org.apache.spark.sql.{DataFrame, SparkSession}

object DecisionTreeRegressionDemo extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("DecisionTreeRegressorDemo")
      .getOrCreate()
    spark
  }
  def checkNull(inputDF: DataFrame): Unit = {
    //Checking for null or nan type values in our columns
    inputDF.columns.foreach { colName =>
      print(colName + "---->")
      println(
        inputDF
          .filter(
            inputDF(colName).isNull || inputDF(colName) === "" || inputDF(
              colName
            ).isNaN
          )
          .count()
      )
    }
  }

  def imputingValues(inputDF: DataFrame): DataFrame = {
    val imputer = new Imputer()
      .setInputCols(Array("GRE Score", "TOEFL Score", "University Rating"))
      .setOutputCols(Array("GRE Score", "TOEFL Score", "University Rating"))
      .setStrategy("mean")
    val imputerModel = imputer.fit(inputDF)
    val imputedDF = imputerModel.transform(inputDF)
    println("After Imputation")
    //Checking for null or nan type values in our columns
    checkNull(imputedDF)
    imputedDF
  }
  val spark = createSession()
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  val inputDF = spark.read
    .option("header", true)
    .option("inferSchema", true)
    .csv("./src/test/resources/Admission_Prediction.csv")
  inputDF.printSchema()
  inputDF.show()
  checkNull(inputDF)
  val imputedDF = imputingValues(inputDF)

  val assembler = new VectorAssembler()
    .setInputCols(inputDF.drop("Chance of Admit").columns)
    .setOutputCol("features")
  val outputDF =
    assembler.transform(imputedDF).select("features", "Chance of Admit")
  outputDF.show()
  val scaler =
    new MinMaxScaler().setInputCol("features").setOutputCol("scaledFeatures")
  val scalerModel = scaler.fit(outputDF)
  val scaledDF =
    scalerModel.transform(outputDF).select("Chance of Admit", "scaledFeatures")

  scaledDF.show()

  val decisionTreeModel = new DecisionTreeRegressor()
    .setFeaturesCol("scaledFeatures")
    .setLabelCol("Chance of Admit")

  val splits = scaledDF.randomSplit(Array(0.75, 0.25))
  val train = splits(0)
  val test = splits(1)

  val predictions = decisionTreeModel.fit(train).transform(test)
  predictions.show()

  val evaluator = new RegressionEvaluator()
    .setLabelCol("Chance of Admit")
    .setPredictionCol("prediction")
    .setMetricName("rmse")

  val rmseValue = evaluator.evaluate(predictions)
  println("The rmse value is " + rmseValue)
  val mean =
    predictions.select(avg(col("prediction"))).collect().apply(0).getDouble(0)
  println("The average value is " + mean)
  val errorValue = rmseValue / mean
  println(s"The error percentage is %1.2f%%".format(errorValue))
  spark.stop()
}
