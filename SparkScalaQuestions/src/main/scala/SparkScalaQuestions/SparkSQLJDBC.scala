package SparkScalaQuestions

import org.apache.spark.sql.SparkSession

object SparkSQLJDBC extends App {
  val spark = SparkSession.builder().master("local[*]").appName("SQL JDBC") getOrCreate()
  val inputDF = spark.read.format("jdbc").option("url","jdbc:mysql://localhost/newDB").option("user","root").option("password","Niraj123@")
    .option("dbtable","employee").load()
  inputDF.show(5)
  inputDF.createOrReplaceTempView("employee")
  val resultDF = spark.sql("select * from employee where empid = 111")
  resultDF.show(5)
}