package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{
  MinMaxScaler,
  StringIndexer,
  VectorAssembler
}
import org.apache.spark.sql.{DataFrame, SparkSession}

object DecisionTreeClassifierDemo extends App {
  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("DecisionTreeClassifierDemo")
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
  val spark = createSession()

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val inputDF = spark.read
    .option("header", true)
    .option("inferSchema", true)
    .csv("./src/test/resources/winequality_red.csv")
  inputDF.printSchema()
  inputDF.show()
  checkNull(inputDF)

  val assembler = new VectorAssembler()
    .setInputCols(inputDF.drop("quality").columns)
    .setOutputCol("features")

  val outputDF = assembler.transform(inputDF).select("features", "quality")
  outputDF.show()
  val scaler =
    new MinMaxScaler().setInputCol("features").setOutputCol("scaledFeatures")
  val scalerModel = scaler.fit(outputDF)
  val scaledDF =
    scalerModel.transform(outputDF).select("quality", "scaledFeatures")

  val stringIndexer =
    new StringIndexer().setInputCol("quality").setOutputCol("quality_index")
  val indexedDF = stringIndexer
    .fit(scaledDF)
    .transform(scaledDF)
    .select("scaledFeatures", "quality_index")
  indexedDF.show()

  val split = indexedDF.randomSplit(Array(0.75, 0.25))
  val train = split(0)
  val test = split(1)

  val decisionTreeModel = new DecisionTreeClassifier()
    .setFeaturesCol("scaledFeatures")
    .setLabelCol("quality_index")

  val predictions = decisionTreeModel.fit(train).transform(test)
  predictions.select("quality_index", "prediction", "scaledFeatures").show()
  val evaluator = new MulticlassClassificationEvaluator()
    .setLabelCol("quality_index")
    .setPredictionCol("prediction")
    .setMetricName("accuracy")
  val accuracy = evaluator.evaluate(predictions)
  println(s"The accuracy is ${accuracy * 100}%")
  spark.stop()
}
