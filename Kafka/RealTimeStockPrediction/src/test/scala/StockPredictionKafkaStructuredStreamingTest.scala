import SparkStructuredStreaming.StockPredictionKafkaStructuredStreaming
import Utility.UtilityClass
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.scalatest.FunSuite
class StockPredictionKafkaStructuredStreamingTest extends FunSuite {
  val filePath = "./src/test/resources/GOOG.csv"

  test(
    "test_InputStockPriceDataFrame_MatchTwoDataFrames_OneCalculatedOtherFromFunction_AssertsTrue"
  ) {
    val sparkSessionObj = UtilityClass.createSessionObject("Stock Price Test")
    sparkSessionObj.sparkContext.setLogLevel("ERROR")
    val structuredStreamingObj =
      new StockPredictionKafkaStructuredStreaming(sparkSessionObj)
    val command = "python3 ./pythonFiles/StockPricePrediction.py"
    // creating rdd with the input files,repartitioning the rdd and passing the command using pipe
    val inputDataFrame = sparkSessionObj.read
      .option("header", true)
      .option("inferSchema", true)
      .csv(filePath)
      .drop("Date", "Close", "Adj Close")

    val predictedPriceRDD =
      inputDataFrame.rdd
        .repartition(1)
        .pipe(command)
    //Collecting the result from the output RDD and converting it to Double
    val predictedPrice =
      predictedPriceRDD.collect().toList.map(elements => elements.toDouble)
    //Creating a new dataframe with new predicted value Column
    val predictedStockPriceDataFrame = sparkSessionObj.createDataFrame(
      // Adding New Column
      inputDataFrame.rdd.zipWithIndex.map {
        case (row, columnIndex) =>
          Row.fromSeq(row.toSeq :+ predictedPrice(columnIndex.toInt))
      },
      // Create schema
      StructType(
        inputDataFrame.schema.fields :+ StructField(
          "Predicted Close Price",
          DoubleType,
          false
        )
      )
    )
    val predictedStockPriceDataFrameTest =
      structuredStreamingObj.loadingLinearRegressionModelPython(inputDataFrame)
    val frameTest = new FrameComparison()
    assert(
      frameTest.frameComparison(
        predictedStockPriceDataFrameTest,
        predictedStockPriceDataFrameTest
      )
    )
  }
}
