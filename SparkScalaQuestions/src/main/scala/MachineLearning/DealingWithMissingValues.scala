package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.Imputer
import org.apache.spark.sql.{DataFrame, SparkSession}

object DealingWithMissingValues extends App {

  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("HandlingNulls")
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
    .csv("./src/test/resources/Admission_Prediction.csv")
  inputDF.show(5)
  inputDF.describe().show()
  inputDF.printSchema()
  println("Before Imptatuion")
  checkNull(inputDF)

  val imputer = new Imputer()
    .setInputCols(Array("GRE Score", "TOEFL Score", "University Rating"))
    .setOutputCols(Array("GRE Score", "TOEFL Score", "University Rating"))
    .setStrategy("mean")
  val imputerModel = imputer.fit(inputDF)
  val imputedDF = imputerModel.transform(inputDF)
  println("After Imputation")
  //Checking for null or nan type values in our columns
  checkNull(imputedDF)
}
