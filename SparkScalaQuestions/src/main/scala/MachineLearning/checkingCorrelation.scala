package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object checkingCorrelation extends App {
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

  def findCorrelation(inputDF: DataFrame): Unit = {
    val corr = Correlation.corr(inputDF, "features")
    corr.collect().foreach(x => println(x))
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
  findCorrelation(outputDF)
}
