package Streaming

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{
  StreamExecutionEnvironment,
  createTypeInformation
}

object AggregationOperations extends App {
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
  val sumStreams = mapped.keyBy(0).sum(3)
  println("Sum By Months")
  sumStreams.print()
  val minStreams = mapped.keyBy(0).min(3)
  println("Min By Months")
  minStreams.print()
  val minByStreams = mapped.keyBy(0).minBy(3)
  println("Min By Months Using MinBy")
  minByStreams.print()
  val maxStreams = mapped.keyBy(0).max(3)
  maxStreams.print()
  val maxByStreams = mapped.keyBy(0).maxBy(3)
  println("Max By Months Using MaxBy")
  maxByStreams.print()
  env.execute("Aggregation Operations")

}
