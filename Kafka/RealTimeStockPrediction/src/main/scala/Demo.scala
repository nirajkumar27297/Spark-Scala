import UtilityPackage.Utility.createKafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Utils.sleep
object Demo extends App {
  val jsonString =
    """{"1. open":"1616.7500","2. high":"1616.7500","3. low":"1616.7500","4. close":"1616.7500","5. volume":"229"}"""
  val brokers = "localhost:9092"
  val topics = "kafkatutorial"
  val kafkaProducer = createKafkaProducer(brokers)
  sleep(3000)

  val record =
    new ProducerRecord[String, String](
      topics,
      "2020-10-22 16:53:00",
      jsonString
    )
  kafkaProducer.send(record)

  println(record)
  val a = scala.io.StdIn.readLine()

}
