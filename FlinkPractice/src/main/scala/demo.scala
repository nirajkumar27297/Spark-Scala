//package p1
import java.util
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.collector.selector.OutputSelector
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.datastream.SplitStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object SplitDemo {
  @throws[Exception]
  def main(args: Array[String]): Unit = { // set up the stream execution environment
    val env =
      StreamExecutionEnvironment.getExecutionEnvironment
    // Checking input parameters
    val params = ParameterTool.fromArgs(args)
    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)
    val text = env.readTextFile("/home/jivesh/oddeven.txt")
    val evenOddStream = text
      .map(new MapFunction[String, Integer]() {
        override def map(value: String): Integer = value.toInt
      })
      .split(new OutputSelector[Integer]() {
        override def select(value: Integer): util.ArrayList[String] = {
          val out = new util.ArrayList[String]
          if (value % 2 == 0)
            out.add(
              "even"
            ) // label element  --> even 454   odd 565 etc
          else out.add("odd")
          out
        }
      })
    val evenData = evenOddStream.select("even")
    val oddData = evenOddStream.select("odd")
    evenData.writeAsText("/home/jivesh/even")
    oddData.writeAsText("/home/jivesh/odd")
    // execute program
    env.execute("ODD EVEN")
  }
}
