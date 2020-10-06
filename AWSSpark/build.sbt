name := "AWSSpark"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-aws
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.4"
