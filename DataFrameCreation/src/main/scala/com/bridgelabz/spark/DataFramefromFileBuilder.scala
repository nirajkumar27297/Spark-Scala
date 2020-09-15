package com.bridgelabz.spark

import org.apache.spark.sql.catalyst.ScalaReflection.Schema
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}


class DataFramefromFileBuilder {
  val spark = SparkSession.builder().master("local[*]").appName("Builder").getOrCreate()

  def showDataFrame(readDataFrame: DataFrame):Unit  = {
    readDataFrame.describe()
    readDataFrame.show(10)
  }

  def createDataFrame(choice:Int, filepath:String):DataFrame = {
    try {
        val readDataFrame = choice match {
        case 1 => spark.read.option("header", "true").option("inferSchema","true").csv(filepath)
        case 2 => spark.read.json(filepath)
        case 3 => spark.read.parquet(filepath)
        case 4 => spark.read.format("avro").load(filepath)
      }
      showDataFrame(readDataFrame)
      readDataFrame
    }
    catch {
      case _ : org.apache.spark.sql.AnalysisException =>
        throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.sparkSqlException)
      case _ : org.apache.avro.AvroRuntimeException =>
        throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.avroFileException)
      case _ : org.apache.spark.SparkException =>
        throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.sparkException)
    }
  }


  def createCSVDataFrame(filepath: String):DataFrame = {
    createDataFrame(1,filepath)
  }

  def createJSONDataFrame(filepath: String):DataFrame = {
    createDataFrame(2,filepath)
  }

  def createParaquetDataFrame(filepath: String):DataFrame = {
    createDataFrame(3,filepath)
  }

  def createAvroDataFrame(filepath:String):DataFrame = {
    createDataFrame(4,filepath)
  }

  def createListDataFrame(inputArray:List[Row],schema:StructType):DataFrame = {
    try {
      if(inputArray.length == 0) {
        throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.emptyArray)
      }

      val rdd = spark.sparkContext.parallelize(inputArray)
      val dataFrame = spark.createDataFrame(rdd,schema)
      showDataFrame(dataFrame)
      dataFrame
    }
    catch {
      case  _ : NullPointerException => throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.emptyArray)
      case _ : org.apache.spark.SparkException =>
        throw new DataFrameBuilderException(DataFrameBuilderExceptionEnum.sparkException)
    }
 }
}

