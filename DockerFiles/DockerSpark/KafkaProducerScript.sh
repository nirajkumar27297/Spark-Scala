cd KafkaProducer && sbt package && spark-submit --driver-java-options "-Dlog4j.configuration=file:./log4j.properties" --class Producer.ProducerScript --jars "../jars/commons-pool2-2.8.0.jar","../jars/kafka-clients-2.6.0.jar","../jars/spray-json_2.12-1.3.5.jar", "./target/scala-2.12/kafkaproducer_2.12-0.1.jar" "kafka:9092" "kafkatutorial" "GOOG" "QKYWTQ4XOGA0FIGJ"
