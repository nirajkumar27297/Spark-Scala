package SparkScalaQuestions

import org.apache.spark.ml.feature.Imputer
import org.apache.spark.sql.SparkSession

object HandlingNulls extends App {
  val spark = SparkSession.builder().master("local[*]").appName("SQL Practice") getOrCreate()
  val inputDF = spark.read.option("header", true).option("inferSchema", true).csv("./src/test/resources/Admission_Prediction.csv")
  inputDF.show(5)
  inputDF.describe().show()
  inputDF.createOrReplaceTempView("admission")
  inputDF.printSchema()
  println(inputDF.columns)
  //Checking for null or nan type values in our columns
  inputDF.columns.foreach { colName => print(colName+"---->")
    println(inputDF.filter(inputDF(colName).isNull || inputDF(colName) === "" || inputDF(colName).isNaN).count())
  }
  val imputer = new Imputer().setInputCols(Array("GRE Score","TOEFL Score","University Rating"))
                      .setOutputCols(Array("GRE Score","TOEFL Score","University Rating"))
                      .setStrategy("mean")
  val imputerModel = imputer.fit(inputDF)
  val imputedDF = imputerModel.transform(inputDF)
  //Checking for null or nan type values in our columns
  imputedDF.columns.foreach { colName => print(colName+"---->")
    println(imputedDF.filter(imputedDF(colName).isNull || imputedDF(colName) === "" || imputedDF(colName).isNaN).count())
  }
}
