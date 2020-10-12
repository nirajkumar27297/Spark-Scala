/* The objective is to load the created Spark Linear Regression Model and pass the input Values that we
got from the control layer and predict the output and return the result back to control layer
 */

package models

import org.apache.spark.sql.SparkSession

/*
Created StockPricePredictionModel with two functions-
1> calculateClosePrice - To load the model and return the predicted price
2> predictPrice - To predict the close price
@author:Niraj Kumar
 */
object StockPricePredictionPythonModel {

  /*
  The objective of the function is to create a rdd with the given inputs,call the python machine learning algorithm  and return the calculated Close Price
  @params: OpenPrice[Double],highPrice[Double],lowPrice[Double],Volume[Double]
  @return:PredictedPrice[Double]
   */
  def predictPrice(
      openPrice: Double,
      HighPrice: Double,
      lowPrice: Double,
      Volume: Double,
      sparkSessionObj: SparkSession
  ): Double = {
    val command = "python3 ./pythonFiles/StockPricePrediction.py"
    //creating rdd with the input files,reparatitining the rdd and passing the command using pipw
    val predictedPriceRDD = sparkSessionObj.sparkContext
      .makeRDD(List(openPrice, HighPrice, lowPrice, Volume))
      .repartition(1)
      .pipe(command)
    //Collecting the result from the output RDD.
    val predictedClosePrice = predictedPriceRDD.collect().apply(0)
    //Scaling the Predicted Close Price to 2 decimal places and returning it
    BigDecimal(predictedClosePrice)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble
  }
  //Creating a rootLogger
  val rootLogger = Utility.UtilityClass.getRootLogger()

}
