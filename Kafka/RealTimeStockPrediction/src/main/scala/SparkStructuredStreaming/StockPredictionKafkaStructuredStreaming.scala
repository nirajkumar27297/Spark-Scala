/**
  * The objective is to get the data from ka cluster ,push it through spark streaming in batches and predict
  * the close price for each of them and save it as a csv file.
  * Library Used -
  * 1> org.apache.spark.spark-sql
  *  Version - 3.0.0
  * 2> org.apache.spark.spark-core
  *   Version - 3.0.0
  * 3> org.apache.spark.spark-Streaming
  *  Version - 3.0.0
  * 4> org.apache.spark.spark-mllib
  *    Version - 3.0.0
  *
  *    @author:Niraj
  *    *
  */

package SparkStructuredStreaming
import Utility.UtilityClass
import org.apache.log4j.Logger
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{
  DoubleType,
  StringType,
  StructField,
  StructType
}

/**
  *Creating an object StockPredictionKafkaStructuredStreaming with following Function
  * 1> takingInput
  * 2> preProcessing
  * 3> creatingDataFrameFromJson
  * 4> castingDataColumns
  * 5> loadingLinearRegressionModelSpark
  * 6> loadingLinearRegressionModelPython
  * 7> predictingPrice
  * 8> writeToOutputStream
  */
object StockPredictionKafkaStructuredStreaming extends App {
  val sparkSessionObj =
    UtilityClass.createSessionObject("Real Time Stock Prediction")
  val structuredStreamingObj = new StockPredictionKafkaStructuredStreaming(
    sparkSessionObj
  )
  val streamedDataFrame = structuredStreamingObj.takingInput(args(0), args(1))
  val preprocessedDataFrame =
    structuredStreamingObj.preProcessing(streamedDataFrame)
  structuredStreamingObj.writeToOutputStream(preprocessedDataFrame, args(2))
}

class StockPredictionKafkaStructuredStreaming(
    sparkSessionObj: SparkSession
) {
  //Configuring log4j
  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  /**
    * The objective the function to take input from kafka source and return dataframe
    * @return inputDataFrame [DataFrame]
    */
  def takingInput(brokers: String, topics: String): DataFrame = {
    logger.info("Taking Input From Kafka Topic")
    try {

      val inputDataFrame = sparkSessionObj.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", topics)
        .option("startingOffsets", "earliest")
        .load()
      inputDataFrame
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Difficulty in taking Input")
        throw new Exception("Difficulty in taking Input")
    }
  }

  /**
    * The objective of the function is to take Dataframe having json String as value and creating dataframe
    * from it and returning it
    * @param inputDataFrame [DataFrame]
    * @return columnsRenamedDataFrame [DataFrame]
    */

  private def creatingDataFrameFromJson(
      inputDataFrame: DataFrame
  ): DataFrame = {
    try {

      logger.info("Creating Json DataFrame from Kafka Topic Message")
      // Defining Schema for dataframe
      val schema = new StructType()
        .add("1. open", StringType, true)
        .add("2. high", StringType, true)
        .add("3. low", StringType, true)
        .add("4. close", StringType, true)
        .add("5. volume", StringType, true)

      /* Taking only the value column which is a json string  from inputDataFrame and creating
         dataframe from the json String
       as well renaming the column
       */

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
      columnsRenamedDataFrame
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Difficulty in creating dataframe fron kafka topic message")
        throw new Exception(
          "Difficulty in creating dataframe fron kafka topic message"
        )
    }

  }

  /**
    * The function takes dataframe as a input and cast it to appropriate datatype
    * @param inputDataFrame [DataFrame]
    * @return castedDataFrame [DataFRame]
    */
  private def castingDataColumns(inputDataFrame: DataFrame): DataFrame = {
    try {
      logger.info("Casting DataFrame to Appropriate data types")
      //Casting the dataframe to appropriate data types
      val castedDataFrame = inputDataFrame.select(
        col("Open").cast(DoubleType),
        col("High").cast(DoubleType),
        col("Low").cast(DoubleType),
        col("Volume").cast(DoubleType)
      )
      castedDataFrame
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Difficulty in Casting DataFrame to Appropriate data types")
        throw new Exception(
          "Difficulty in Casting DataFrame to Appropriate data types"
        )
    }
  }

  /**
    * The objective of the function is to call functions creatingDataFrameFromJson and castingDataColumns
    * for preprocessing
    * @param inputDataFrame [DataFrame]
    * @return castedDataFrame [DataFrame]
    */
  def preProcessing(inputDataFrame: DataFrame): DataFrame = {
    logger.info("PreProcessing DataFrame")
    val columnsRenamedDataFrame = creatingDataFrameFromJson(inputDataFrame)
    val castedDataFrame = castingDataColumns(columnsRenamedDataFrame)
    castedDataFrame
  }

  /**
    * The functions loads the saved spark Pipeline Model and predict Stock Close Price
    * for the giev input
    * @param inputDataFrame [Dataframe]
    * @return predictedDataFrame [DataFrame]
    */

  private def loadingLinearRegressionModelSpark(
      inputDataFrame: DataFrame
  ): DataFrame = {
    try {
      logger.info("Predicting Close Price Using Spark Model")
      val linearRegressionModel =
        PipelineModel.load("./MachineLearningModel/model")
      //Applying the model to our Input DataFrame
      val predictedDataFrame = linearRegressionModel.transform(inputDataFrame)
      //Extracting the Predicted Close Price from the Output DataFrame
      predictedDataFrame

    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Difficulty in loading spark model")
        throw new Exception("Difficulty in loading spark model")
    }

  }

  /**
    * The objective of the function is to pipe the python machine learning algorithm for the
    * given input dataframe and predict the Close Price
    * @param inputDataFrame [DataFrame]
    * @return predictedStockPriceDataFrame [DataFrame]
    */

  def loadingLinearRegressionModelPython(
      inputDataFrame: DataFrame
  ): DataFrame = {
    try {
      logger.info("Predicting Close Price Using Python Model")

      val command = "python3 ./pythonFiles/StockPricePrediction.py"
      // creating rdd with the input files,repartitioning the rdd and passing the command using pipe

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
      predictedStockPriceDataFrame
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Difficulty in Predicting Close Price Using Python Model")
        throw new Exception(
          "Difficulty in Predicting Close Price Using Python Model"
        )
    }
  }

  /**
    * The function takes dataframe as input and call loadingLinearRegressionModelPython function to predict
    * the output and save the output as csv file in the provided path
    * @param inputDataFrame [DataFrame]
    */
  def predictingPrice(
      inputDataFrame: DataFrame,
      pathToSave: String
  ): Unit = {
    try {
      val predictedClosePriceDataFrame = loadingLinearRegressionModelPython(
        inputDataFrame
      )
      if (predictedClosePriceDataFrame.isEmpty == false) {
        predictedClosePriceDataFrame.printSchema()
        predictedClosePriceDataFrame.show()
        logger.info(
          "Saving the predicted Price in the following path" + pathToSave
        )
        //Saving the output dataframe as csv in the provided path
        predictedClosePriceDataFrame.write
          .mode("append")
          .option("header", true)
          .csv(
            pathToSave
          )
      }
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        println("Provide the path argument")
      case ex: Exception =>
        ex.printStackTrace()
        logger.info("Saving the predicted Price in the provided path")
        throw new Exception(
          "Saving the predicted Price in the following path"
        )
    }
  }

  /**
    * The objective of the function is to call our stream process and run predictingPrice function
    * for each batch after a trigger of 5 seconds.
    * The stream will wait for 5 minutes and terminate if no input is provided
    * @param inputDataFrame [DataFrame]
    */
  def writeToOutputStream(
      inputDataFrame: DataFrame,
      pathToSave: String
  ): Unit = {
    logger.info("Writing to Output Stream")
    val query = inputDataFrame.writeStream
      .foreachBatch { (batchDataFrame: DataFrame, batchID: Long) =>
        println("Running for tha batch " + batchID)
        predictingPrice(batchDataFrame, pathToSave)
      }
      .queryName("Real Time Stock Prediction Query")
      .option("checkpointLocation", "chk-point-dir")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .start()
    logger.info("Terminating the Streaming Services")
    query.awaitTermination(300000)
  }
}
