name := """StockPrediction"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test


libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"





