package WordCountQuestion

import WordCountQuestion.WordCountExceptionEnum.WordCountExceptionEnum


class WordCountException(message:WordCountExceptionEnum) extends Exception(message.toString) {}

object WordCountExceptionEnum extends Enumeration {

  type WordCountExceptionEnum = Value
  val mapRIOException = Value("Input is Invalid")
  val sparkSqlException = Value("Spark Sql Analysis Exception")

}
