import SparkStructuredStreaming.StockPredictionKafkaStructuredStreaming
import Utility.UtilityClass
import org.apache.spark.sql.functions.{col, from_json}
object demo extends App {
  val sparkSessionObj = UtilityClass.createSessionObject("Stock Price Test")
  val logger = UtilityClass.getRootLogger()
  val jsonString = {
    """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500","5. volume":"lop"}"""

  }
  import sparkSessionObj.implicits._
  val inputDataFrame =
    sparkSessionObj.sparkContext.makeRDD(Seq(jsonString)).toDF("value")
  inputDataFrame.show(1)
  val structuredStreamingObj =
    new StockPredictionKafkaStructuredStreaming(sparkSessionObj)
  val casted = structuredStreamingObj.preProcessing(inputDataFrame)
  casted.show(5)
  casted.printSchema()
}
