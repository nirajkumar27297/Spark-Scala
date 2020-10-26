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
import org.scalatest.{BeforeAndAfterEach, FunSuite}
class StockPredictionKafkaStructuredStreamingTest
    extends FunSuite
    with BeforeAndAfterEach {

  @transient var structuredStreamingObj
      : StockPredictionKafkaStructuredStreaming = _

  @transient var inputDataFrame: DataFrame = _

  val filePath = "./src/test/resources/GOOG.csv"

  val sparkSessionObj = Utility.createSessionObject("Stock Price Test")
  sparkSessionObj.sparkContext.setLogLevel("ERROR")

  var brokers = ""
  var topics = ""
  val wrongPythonFilePath = "./pythonFiles/StockPricePredisction.py"
  val pythonFilePath = "./pythonFiles/StockPricePrediction.py"
  val command = "python3 ./pythonFiles/StockPricePrediction.py"
  val frameTest = new FrameComparison()
  val invalidJsonString =
    """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500"}"""
  val jsonString =
    """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500","5. volume":"229"}"""
  import sparkSessionObj.implicits._
  val inputDataFrameFromJson = sparkSessionObj.sparkContext
    .makeRDD(Seq(jsonString))
    .toDF("value")

  override def beforeEach() {
    structuredStreamingObj = new StockPredictionKafkaStructuredStreaming(
      sparkSessionObj
    )

    inputDataFrame = sparkSessionObj.read
      .option("header", true)
      .option("inferSchema", true)
      .csv(filePath)
      .drop("Date", "Close", "Adj Close")
    brokers = "localhost:9092"
    topics = "test"
  }

  def generatingDataFrameFromString(): DataFrame = {

    val schema = new StructType()
      .add("1. open", StringType, true)
      .add("2. high", StringType, true)
      .add("3. low", StringType, true)
      .add("4. close", StringType, true)
      .add("5. volume", StringType, true)
    val columnsRenamedDataFrame = inputDataFrameFromJson
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
    castedDataFrame
  }

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
        predictedStockPriceDataFrame
      )
    )
  }
  test(
    "test_preProcessingTest_MatchTwoDataFrames_OneCalculatedOtherFromFunction_AssertsTrue"
  ) {

    val castedDataFrame = generatingDataFrameFromString()
    val castedReturnedDataFrame =
      structuredStreamingObj.preProcessing(inputDataFrameFromJson)

    val frameTest = new FrameComparison()
    assert(
      frameTest.frameComparison(
        castedDataFrame,
        castedReturnedDataFrame
      )
    )
  }
  test(
    "test_preProcessingFunction_InvalidJsonString_ThrowException"
  ) {

    val inputDataFrame =
      sparkSessionObj.read.json(Seq(invalidJsonString).toDS())
    val thrown = intercept[Exception] {
      structuredStreamingObj.preProcessing(inputDataFrame)
    }
    assert(
      thrown.getMessage == "Difficulty in creating dataframe from kafka topic message"
    )
  }
  test("test_PythonFilePathWrong_ReturnException") {
    val thrown = intercept[Exception] {
      structuredStreamingObj.loadingLinearRegressionModelPython(
        inputDataFrame,
        wrongPythonFilePath
      )
    }
    assert(
      thrown.getMessage == "Difficulty in Predicting Close Price Using Python Model"
    )
  }

  test(
    "test_JsonInput_MatchTwoDataFrames_OneThroughFunctionOtherThroughProducer_AssertsTrue"
  ) {

    val kafkaProducer = createKafkaProducer(brokers)

    def checkDataFrames(batchDF: DataFrame): Unit = {
      val preProcessedDF = structuredStreamingObj.preProcessing(batchDF)
      val inputDataFrame = generatingDataFrameFromString()
      assert(frameTest.frameComparison(inputDataFrame, preProcessedDF))
    }

    val record =
      new ProducerRecord[String, String](
        topics,
        "2020-10-22 16:53:00",
        jsonString
      )
    kafkaProducer.send(record)
    val functionDataFrame = structuredStreamingObj.takingInput(brokers, topics)

    functionDataFrame.writeStream
      .foreachBatch((batchDF: DataFrame, _: Long) => checkDataFrames(batchDF))
      .start()
      .awaitTermination(20000)

  }

}
