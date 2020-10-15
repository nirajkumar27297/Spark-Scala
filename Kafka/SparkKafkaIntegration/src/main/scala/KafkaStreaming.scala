//import org.apache.kafka.clients.consumer.ConsumerConfig
//import org.apache.kafka.common.serialization.StringDeserializer
//import org.apache.spark.SparkConf
//import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
//import org.apache.spark.streaming.{Seconds, StreamingContext}
//
//object KafkaStreaming {
//  def main(args: Array[String]): Unit = {
//    val brokers = "localhost:9092"
//    val groupId = "GRP1"
//    val topics = "kafkatutorial"
//
//    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("KAFKAStreaming")
//    val ssc = new StreamingContext(sparkConf,Seconds(3))
//    val sc = ssc.sparkContext
//    sc.setLogLevel("OFF")
//    val topicSet = topics.split(",").toSet
//    val kafkaParams = Map[String,Object](
//      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
//      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
//      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
//      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
//    )
//    val messages = KafkaUtils.createDirectStream[String,String](
//      ssc,LocationStrategies.PreferConsistent,ConsumerStrategies.Subscribe[String,String](topicSet,kafkaParams)
//    )
//    val line = messages.map(_.value())
//    line.print()
////    val words = line.flatMap(_.split(" "))
////    val wordCounts = words.map( x => (x,1)).reduceByKey(_ + _)
////    wordCounts.print()
//    ssc.start()
//    ssc.awaitTermination()
//
//  }
//
//
//}
