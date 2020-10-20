/*
Library Used
1> org.apache.spark.spark-core Version 3.0.0
--This is used for spark rdd operations
2> org.apache.spark.spark-sql Version 3.0.0
--This is used for dataframe operations
3> org.apache.spark.spark-streaming Version 3.0.0
--This is used for streaming operations
4> org.apache.spark.spark-mllib Version 3.0.0
--This is used for machine learning operations
5> org.apache.kafka.kafka-clients 2.6.0
--This is used for connecting kafka to spark and creating producer and consumer
6>org.apache.spark.spark-streaming-kafka-0.10 Version 2.4.0
--This is used for spark unstructured streaming
7> org.apache.spark.spark-sql-kafka-0.10 Version 3.0.0
--This is used for spark structured streaming
8> io.spray.spray-json Version 1.3.5
--This is used to parse Json Strings.

 */

name := "RealTimeStockPrediction"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"

// https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.6.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kafka-0-10
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql-kafka-0-10
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.0.0"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
