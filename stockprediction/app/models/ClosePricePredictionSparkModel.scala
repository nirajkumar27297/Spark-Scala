/* The objective is to load the created Spark Linear Regression Model and pass the input Values that we
got from the control layer and predict the output and return the result back to control layer

Library Used:-
1> org.apache.spark
  Version - 3.0.0
@author:Niraj Kumar
 */

package models

import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

/*
Created ClosePricePredictionSparkModel with two functions-
1> calculateClosePrice - To load the model and return the predicted price
2> predictPrice - To predict the close price
 */

object ClosePricePredictionSparkModel {

  /*
  The objective of the function is load the saved Linear Regression Pipeline Model and predict and return the close price
  with dataframe as input.
  @params: inputDataFrame Type [DataFrame]
  @return : predictedClosePrice Type[Double]
   */

  private def calculateClosePrice(inputDataFrame: DataFrame): Double = {
    try {
      //Loading the Linear Regression Pipeline Model
      val linearRegressionModel =
        PipelineModel.load("./app/MachineLearningModel/model")
      //Applying the model to our Input DataFrame
      val predictedDataFrame = linearRegressionModel.transform(inputDataFrame)
      //Extracting the Predicted Close Price from the Output DataFrame
      val predictedClosePrice =
        predictedDataFrame.select("prediction").collect().apply(0).getDouble(0)
      //Scaling the Predicted Close Price to 2 decimal places and returning it
      BigDecimal(predictedClosePrice)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
        .toDouble
    } catch {
      case ex: Exception =>
        println(ex)
        ex.printStackTrace()
        -1
    }
  }

  /*
  The objective of the function is to create a dataframe with the given inputs and return the calculated Close Price
  @params: OpenPrice[Double],highPrice[Double],lowPrice[Double],Volume[Double]
  @return:PredictedPrice[Double]
   */
  def predictPrice(
      openPrice: Double,
      highPrice: Double,
      lowPrice: Double,
      Volume: Double,
      sparkSessionObj: SparkSession
  ): Double = {
    try {
      //Creating a rdd with the given input
      val stockPriceInputRDD = sparkSessionObj.sparkContext.parallelize(
        Seq(
          Row.fromSeq(
            List(openPrice, highPrice, lowPrice, Volume)
          )
        )
      )
      //Creating the schema for our input RDD to convert it into dataframe
      val schemaString = "Open High Low Volume"
      val schema = StructType(
        schemaString
          .split(" ")
          .map(fieldName ⇒ StructField(fieldName, DoubleType, true))
      )
      //Converting Our RDD to a DataFrame
      val stockPriceInputDataFrame =
        sparkSessionObj.createDataFrame(stockPriceInputRDD, schema)
      //calling calculateClosePrice to get Predicted Close Price
      calculateClosePrice(stockPriceInputDataFrame)
    } catch {
      case ex: Exception =>
        println(ex)
        ex.printStackTrace()
        -1
    }
  }
  //Creating a rootLogger
  val rootLogger = Utility.UtilityClass.getRootLogger()
}
