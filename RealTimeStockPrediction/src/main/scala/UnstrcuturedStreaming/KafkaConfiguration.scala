/**
  * The objective of the prom to configuure kafka client parameters
  */
package UnstrcuturedStreaming
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConfiguration extends App {

  /**
    * The Functions takes brokers name and group Id and configure kafka params
    * and  return kafka params.
    * @param brokers
    * @param groupId
    * @return
    */
  def configureKafka(brokers: String, groupId: String): Map[String, Object] = {
    val kafkaParams = Map[String, Object](
      //Assigning bootstrap servers
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      //Assigning groupID
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      //Assigning searializer for key
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[
        StringDeserializer
      ],
      //Assigning searializer for Value
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[
        StringDeserializer
      ]
    )
    kafkaParams
  }
}
