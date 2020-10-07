name := "AWSSpark"

version := "0.1"

scalaVersion := "2.12.10"

//Library Used -
//  1> org.apache.spark.spark-core,org.apache.spark-sql,org.apache.spark-mllib [For Creating Spark Applications]
//Version - 3.0.0
//2> org.apache.hadoop.hadoop-aws[Apache Hadoop’s hadoop-aws module provides support for AWS integration for using S3.]
//Version - 2.7.4
//3> External Jars to be provided at Runtime [While using Spark-Submit]
//1> hadoop-aws
//Version - 3.2.0
//2> aws-java-sdk-bundle.jar
//Version - 1.11.375

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-aws
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.4"
