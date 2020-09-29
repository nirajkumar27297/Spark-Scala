package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.evaluation.{MulticlassMetrics}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
object LogisticRegressionDemo extends App {
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
    .csv("./src/test/resources/diabetes.csv")

  inputDF.printSchema()
  inputDF.show()
  inputDF.head(2)

  val df = inputDF.select(
    col("Outcome").as("label"),
    col("Pregnancies"),
    col("Glucose"),
    col("BloodPressure"),
    col("SkinThickness"),
    col("Insulin"),
    col("BMI"),
    col("DiabetesPedigreeFunction"),
    col("Age")
  )
  val columns = df.columns
  val indexOutput = columns.indexOf("label")
  val inputCols = columns.drop(indexOutput)
  print(inputCols.toList)

  val assembler = new VectorAssembler()
    .setInputCols(inputCols)
    .setOutputCol("features")

  val output = assembler.transform(df).select("label", "features")
  output.show()

  val splits = output.randomSplit(Array(0.7, 0.3), 111)
  val train = splits.apply(0)
  val test = splits(1)
  val logRegression = new LogisticRegression()
  val logRegressionModel = logRegression.fit(train)
  println(s"Coefficients: \n${logRegressionModel.coefficientMatrix}")
  println(s"Intercepts: \n${logRegressionModel.interceptVector}")

  val trainingSummary = logRegressionModel.binarySummary
  println("The area under ROC Curve " + trainingSummary.areaUnderROC)
  // Obtain the objective per iteration
  val objectiveHistory = trainingSummary.objectiveHistory
  println("objectiveHistory:")
  objectiveHistory.foreach(println)
  val accuracy = trainingSummary.accuracy
  val falsePositiveRate = trainingSummary.weightedFalsePositiveRate
  val truePositiveRate = trainingSummary.weightedTruePositiveRate
  val fMeasure = trainingSummary.weightedFMeasure
  val precision = trainingSummary.weightedPrecision
  val recall = trainingSummary.weightedRecall
  println(
    s"Accuracy: $accuracy\nFPR: $falsePositiveRate\nTPR: $truePositiveRate\n" +
      s"F-measure: $fMeasure\nPrecision: $precision\nRecall: $recall"
  )

  import spark.implicits._
  val testPrediction = logRegressionModel.transform(test)
  val predictionAndLabels = testPrediction
    .select("label", "prediction")
    .as[(Double, Double)]
    .rdd
  val metrics = new MulticlassMetrics(predictionAndLabels)
  println("Predicted Results")
  testPrediction.show()
  println(metrics.confusionMatrix)
  println(s"The accuracy is ${metrics.accuracy * 100} % ")
}
