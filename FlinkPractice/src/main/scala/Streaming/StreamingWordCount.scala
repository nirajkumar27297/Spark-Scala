package Streaming
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{
  StreamExecutionEnvironment,
  createTypeInformation
}

object StreamingWordCount extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val params = ParameterTool.fromArgs(args)
  env.getConfig.setGlobalJobParameters(params)
  val text = env.socketTextStream("localhost", 9999)
  val counts =
    text.filter(_.startsWith("N")).map(word => (word, 1)).keyBy(0).sum(1)
  counts.print()
  env.execute()

}
