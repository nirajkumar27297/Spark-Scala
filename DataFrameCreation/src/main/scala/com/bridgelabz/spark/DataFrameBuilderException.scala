package com.bridgelabz.spark

class DataFrameBuilderException(message:DataFrameBuilderExceptionEnum.Value) extends Exception(message.toString) {}

object DataFrameBuilderExceptionEnum extends Enumeration {

  type  DataFrameBuilderException = Value
  val sparkSqlException = Value("Spark SQL Exception")
  val avroFileException = Value("Avro File Exception")
  val sparkException = Value("Spark Exception")
  val emptyArray = Value("List is Empty")
}
