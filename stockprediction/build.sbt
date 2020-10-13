/*
Library Used:-
1> Play FrameWork Version[2.8.2]
-To enable Web development in scala
2> ScalaTestPlusPlay Version[5.0.0]
- For Testing our api
3> Spark FrameWork
- For Building Spark Applications i.e. for Predicting stock close price using Pre-created Spark Model.
 */


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





