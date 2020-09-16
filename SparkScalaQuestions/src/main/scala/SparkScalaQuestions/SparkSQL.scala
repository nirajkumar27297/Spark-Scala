package SparkScalaQuestions

import org.apache.spark.sql.SparkSession

object SparkSQL extends App {
  val spark = SparkSession.builder().master("local[*]").appName("SQL Practice")getOrCreate()
  val inputDF = spark.read.option("header",true).option("inferSchema",true).csv("./src/test/resources/Admission_Prediction.csv")
  inputDF.show(5)
  inputDF.createOrReplaceTempView("admission")
  inputDF.printSchema()
  println(inputDF.columns)
  //Including Columns having space in their name
  val greScore = spark.sql("select * from admission where `gre score` > 330")
  greScore.show(5)
  val cgpaDF = spark.sql("select * from admission where cgpa > 9")
  cgpaDF.show(5)
  spark.stop()

}
