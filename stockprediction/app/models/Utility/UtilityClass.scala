/*
The objective is to create a utility class which can be passed over
different classes to create the required session and context object if Required
@author:Niraj Kumar
 */

package models.Utility

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object UtilityClass {

  /* Function to Create Spark Session Object
  @return SparkSession
   */
  def createSessionObject(appName: String): SparkSession = {

    val sparkconfigurations = new SparkConf()
      .setAppName(appName)
      .setMaster("local")

    val sparkSessionObj = SparkSession
      .builder()
      .appName("StockPredictionPythonConnectivity")
      .config(sparkconfigurations)
      .getOrCreate()
    sparkSessionObj
  }
  /* Function to get Root Logger with Level as Error
  @return RootLogger
   */

  def getRootLogger(): Logger = {
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)
    rootLogger
  }

  //TODO Implement create Context Function if required

}
