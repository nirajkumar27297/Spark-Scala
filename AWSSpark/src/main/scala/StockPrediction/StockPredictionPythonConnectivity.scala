/* The objective is to take input from a Amazon S3 resource and call machine learning python file
and save the trained model to S3 using Spark Scala.
Library Used -
1> org.apache.spark.spark-core,org.apache.spark-sql,org.apache.spark-mllib [For Creating Spark Applications]
  Version - 3.0.0
2> org.apache.hadoop.hadoop-aws[Apache Hadoop’s hadoop-aws module provides support for AWS integration for using S3.]
  Version - 2.7.4
3> External Jars to be provided at Runtime [While using Spark-Submit]
  1> hadoop-aws
    Version - 3.2.0
  2> aws-java-sdk-bundle.jar
    Version - 1.11.375
@author : Niraj Kumar
 */

package StockPrediction

import Configurations.configurations
import org.apache.log4j.{Level, Logger}
import Utility.UtilityClass
import org.apache.spark.sql.DataFrame

object StockPredictionPythonConnectivity extends App {

  /* Function for taking input
   @param Path[String]
   @return DataFrame
   */
  def takeInput(path: String): DataFrame = {
    try {
      val inputStockPriceDF = sparkSessionObj.read
        .option("header", true)
        .option("inferSchema", true)
        .csv(path)
      inputStockPriceDF
    } catch {
      case ex: java.io.IOException =>
        rootLogger.error(ex)
        println("The File Does not Exist")
        sparkSessionObj.emptyDataFrame
      case ex: Exception =>
        rootLogger.error(ex)
        println("SomeThing Unexpected Occured\n"+ex)
        sparkSessionObj.emptyDataFrame
    }
  }
  //Creating function Objects
  val sparkSessionObj = UtilityClass.createSessionObject("Stock Prediction Application")
  val sparkContextObj = sparkSessionObj.sparkContext
  //Configuring Hadoop
  configurations.apply(sparkContextObj).configurationsForHadoop()
  //Setting Log level to ERROR
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  //Taking Bucket name and CSV file name as command line arguments
  try {
    val bucketName = args(0)
    val fileName = args(1)
    val path = "s3a://" + bucketName + "/" + fileName
    //Calling takeInput function
    val inputStockPriceDF = takeInput(path)
    //printing the schema for input Dataframe
    inputStockPriceDF.printSchema()
    inputStockPriceDF.show()
    val pipedCommand =
      "python3 ./src/test/resources/StockPrediction.py " + bucketName
    //Calling the python file through rdd pipe function
    val outputPipedRdd = inputStockPriceDF.rdd.pipe(pipedCommand)
    //Printing the contents of RDD
    outputPipedRdd.foreach(println(_))
  } catch {
    case ex: ArrayIndexOutOfBoundsException =>
      rootLogger.error(ex)
      println("Please Enter the required Arguments")
    case ex: org.apache.spark.SparkException =>
      rootLogger.error(ex)
      println(ex.printStackTrace())
    case ex: Exception =>
      rootLogger.error(ex)
      println("Something Unexpected Occured Please Try Again\n"+ex)
  } finally {
    sparkSessionObj.stop()
  }
}
