/**
  * Thw objective of the program is to create a kafka Producer with provided bootstrap servers
  * and topic name and send the producer record to brokers
  * Library Used -
  * 1> org.apche.http - To get http response
  * 2> org.apache.kafka.clients - To create kafka producer and send record
  *
  */
package Producer
import java.util.Properties

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.utils.Utils.sleep
import spray.json.JsValue

/**
  * The ProducerScript is used to create a producer script and send data
  * 1> getRestContent
  * 2> setProducerProperties
  * 3> parsingData
  * 4> sendingDataToKafkaTopic
  */
object ProducerScript extends App {
  val brokers = args(0)
  val topics = args(1)

  val api_key = System.getenv("AlphaVantageApiKey")
  val companyName = "GOOG"
  val url =
    "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&interval=1min&symbol=" + companyName + "apikey=" + api_key
  val data = getRestContent(url)
  val stockData = parsingData(data)
  val kafkaProducer = setProducerProperties()
  sendingDataToKafkaTopic(stockData, kafkaProducer)

  /**
    * The function takes the url as input and return the content using HTTP client
    * @param url
    * @return content
    */
  def getRestContent(url: String): String = {
    val httpClient = new DefaultHttpClient()
    val httpResponse = httpClient.execute(new HttpGet(url))
    val entity = httpResponse.getEntity()
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
      content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
      inputStream.close
    }
    httpClient.getConnectionManager().shutdown()
    content
  }

  /**
    * The function sets the Kafka Producer Properties and return a kafka producer
    * @return producer KafkaProducer[String, String]
    */

  def setProducerProperties(): KafkaProducer[String, String] = {
    val properties = new Properties()
    // Adding bootstrap servers
    properties.put("bootstrap.servers", brokers)
    //Adding serializer for key
    properties.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    //Adding serializer for value
    properties.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    // Creating a producer with provided properties
    val producer = new KafkaProducer[String, String](properties)
    producer
  }

  /**
    * The objective of the function is to extract only the required data from rest api
    * @param data String
    * @return requiredStockDataMap Map[String, JsValue]
    */

  def parsingData(data: String): Map[String, JsValue] = {
    import spray.json._
    val jsonStockData = data.parseJson
    val requiredStockData =
      jsonStockData.asJsObject.fields("Time Series (1min)")
    val requiredStockDataMap = requiredStockData.asJsObject.fields
    requiredStockDataMap
  }

  /**
    * The objective of the function is to send the data over topics using Kafka Producer to out Broker.
    * @param inputData inputData [Map[String, JsValue]]
    * @param kafkaProducer  kafkaProducer[KafkaProducer[String, String]]
    */

  def sendingDataToKafkaTopic(
      inputData: Map[String, JsValue],
      kafkaProducer: KafkaProducer[String, String]
  ) = {
    inputData.keysIterator.foreach { key =>
      val record =
        new ProducerRecord[String, String](
          topics,
          key,
          inputData(key).toString
        )
      println(inputData(key))
      kafkaProducer.send(record)
      sleep(3000)
    }
    kafkaProducer.close()
  }

}
