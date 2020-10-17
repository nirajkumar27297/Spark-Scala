package UnstrcuturedStreaming
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConfiguration extends App {
  def configureKafka(brokers: String, groupId: String): Map[String, Object] = {
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[
        StringDeserializer
      ],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[
        StringDeserializer
      ]
    )
    kafkaParams
  }
}
