package Streaming

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{
  StreamExecutionEnvironment,
  createTypeInformation
}

import scala.collection.mutable.ListBuffer

object SplitLabels extends App {
  def splitOddEven(value: Integer): ListBuffer[String] = {
    var out: ListBuffer[String] = ListBuffer()
    if (value % 2 == 0) {
      out += "even"
    } else {
      out += "odd"
    }
    println(out.toList)
    out
  }

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val params = ParameterTool.fromArgs(args)
  env.getConfig.setGlobalJobParameters(params)
  val data = env.readTextFile("./InputFiles/oddeven.txt")
  val mapped = data
    .map(_.toInt)
    .split(splitOddEven(_))

  val evenData = mapped.select("even")
  evenData.print()
  val oddData = mapped.select("odd")
  oddData.print()
  env.execute("Split Labels")

}
