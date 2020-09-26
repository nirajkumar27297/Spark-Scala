name := "Practice-Spark"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"

// https://mvnrepository.com/artifact/mysql/mysql-connector-java
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.21"



libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
// https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.0"


// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.10.6"

// https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector-driver
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector-driver" % "3.0.0"

// https://mvnrepository.com/artifact/com.sksamuel.scapegoat/scalac-scapegoat-plugin
libraryDependencies += "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.3.8" % "provided"




