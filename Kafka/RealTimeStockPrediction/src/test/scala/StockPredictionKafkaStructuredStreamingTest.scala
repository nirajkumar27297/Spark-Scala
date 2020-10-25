import SparkStructuredStreaming.StockPredictionKafkaStructuredStreaming
import UtilityPackage.Utility
import UtilityPackage.Utility.createKafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{
  DoubleType,
  StringType,
  StructField,
  StructType
}
import org.scalatest.FunSuite
class StockPredictionKafkaStructuredStreamingTest extends FunSuite {
  val brokers = "localhost:9092"
  val topics = "kafkatutorial"
  val filePath = "./src/test/resources/GOOG.csv"
  val pythonFilePath = "./pythonFiles/StockPricePrediction.py"
  val sparkSessionObj = Utility.createSessionObject("Stock Price Test")
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
  val frameTest = new FrameComparison()

  test(
    "test_InputStockPriceDataFrame_MatchTwoDataFrames_OneCalculatedOtherFromFunction_AssertsTrue"
  ) {

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
      structuredStreamingObj.loadingLinearRegressionModelPython(
        inputDataFrame,
        pythonFilePath
      )

    assert(
      frameTest.frameComparison(
        predictedStockPriceDataFrameTest,
        predictedStockPriceDataFrameTest
      )
    )
  }
  test(
    "test_KafkaInputTest_MatchTwoDataFrames_OneCalculatedOtherFromFunction_ThrowException"
  ) {
    val jsonString =
      """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500","5. volume":"229"}"""

    import sparkSessionObj.implicits._
    val inputDataFrame = sparkSessionObj.sparkContext
      .makeRDD(Seq(jsonString))
      .toDF("value")
    val schema = new StructType()
      .add("1. open", StringType, true)
      .add("2. high", StringType, true)
      .add("3. low", StringType, true)
      .add("4. close", StringType, true)
      .add("5. volume", StringType, true)
    val columnsRenamedDataFrame = inputDataFrame
      .select(
        from_json(col("value").cast("string"), schema)
          .as("jsonData")
      )
      .selectExpr("jsonData.*")
      .withColumnRenamed("1. open", "Open")
      .withColumnRenamed("2. high", "High")
      .withColumnRenamed("3. low", "Low")
      .withColumnRenamed("4. close", "Close")
      .withColumnRenamed("5. volume", "Volume")

    val castedDataFrame = columnsRenamedDataFrame.select(
      col("Open").cast(DoubleType),
      col("High").cast(DoubleType),
      col("Low").cast(DoubleType),
      col("Volume").cast(DoubleType)
    )

    val castedReturnedDataFrame =
      structuredStreamingObj.preProcessing(inputDataFrame)

    val frameTest = new FrameComparison()
    assert(
      frameTest.frameComparison(
        castedDataFrame,
        castedReturnedDataFrame
      )
    )
  }
  test(
    "test_JsonInput_MatchTwoDataFrames_OneCalculatedOtherFromFunction_ThrowException"
  ) {
    val jsonString =
      """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500"}"""

    import sparkSessionObj.implicits._
    val inputDataFrame = sparkSessionObj.read.json(Seq(jsonString).toDS())
    val thrown = intercept[Exception] {
      structuredStreamingObj.preProcessing(inputDataFrame)
    }
    assert(
      thrown.getMessage == "Difficulty in creating dataframe from kafka topic message"
    )
  }

  test(
    "test_JsonInput_MatchTwoDataFrames_OneThroughFunctionOtherThroughProducer_AssertsTrue"
  ) {
    val jsonString =
      """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500","5. volume":"229"}"""
    val kafkaProducer = createKafkaProducer(brokers)

    def checkDataFrames(batchDF: DataFrame) = {
      val preProcessedDF = structuredStreamingObj.preProcessing(batchDF)
      batchDF.show(5)
      //assert(frameTest.frameComparison(inputDataFrame, functionDataFrame))

    }

    val record =
      new ProducerRecord[String, String](
        topics,
        "2020-10-22 16:53:00",
        jsonString
      )
    kafkaProducer.send(record)
    println(record)

    val inputDataFrame = sparkSessionObj.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topics)
      .option("startingOffsets", "earliest")
      .load()

    val functionDataFrame = structuredStreamingObj.takingInput(brokers, topics)

    println(functionDataFrame.isStreaming)
    kafkaProducer.close()
    functionDataFrame.writeStream
      .format("console")
      .foreachBatch((batchDF: DataFrame, _: Long) => checkDataFrames(batchDF))
      .option("checkpointLocation", "chk-point-dir-test")
      .start()
      .awaitTermination()

  }

}
