package WordCount
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.{ExecutionEnvironment, createTypeInformation}

object WordCount extends App {

  val env = ExecutionEnvironment.getExecutionEnvironment
//  env.execute("Word Count")
  val params = ParameterTool.fromArgs(args)
  env.getConfig.setGlobalJobParameters(params)

  val text = env.readTextFile("./InputFiles/wc.txt")
  val filtered = text.filter(line => line.startsWith("N"))
  val tokenized = filtered.map(x => (x, 1))
  val counts = tokenized.groupBy(0).sum(1)
  counts.print()
}
