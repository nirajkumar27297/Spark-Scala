package Streaming

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object AverageProfit extends App {
  def reduceOperation(
      current: (String, String, String, Int, Int),
      pre_result: (String, String, String, Int, Int)
  ) =
    new Tuple5[String, String, String, Int, Int](
      current._1,
      current._2,
      current._3,
      current._4 + pre_result._4,
      current._5 + pre_result._5
    )
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val params = ParameterTool.fromArgs(args)
  env.getConfig.setGlobalJobParameters(params)
  val data = env.readTextFile("./InputFiles/avg.txt")
  val mapped = data
    .map(_.split(","))
    .map(columnValues =>
      (
        columnValues(1),
        columnValues(2),
        columnValues(3),
        columnValues(4).toInt,
        1
      )
    )
  val reduced = mapped
    .keyBy(0)
    .reduce((current, pre_result) => reduceOperation(current, pre_result))
  val profitPerMonth =
    reduced.map(column => (column._1, (column._4 * 1.0) / column._5))
  profitPerMonth.print()
  env.execute()
}
