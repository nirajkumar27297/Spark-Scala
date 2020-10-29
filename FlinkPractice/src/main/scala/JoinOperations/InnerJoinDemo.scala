package JoinOperations
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.{ExecutionEnvironment, createTypeInformation}

object InnerJoinDemo extends App {
  val env = ExecutionEnvironment.getExecutionEnvironment
  val params = ParameterTool.fromArgs(args)
  env.getConfig.setGlobalJobParameters(params)
  val personSet = env
    .readTextFile("./InputFiles/person.txt")
    .map(line => line.split(","))
    .map(words => (words(0).toInt, words(1)))
  val locationSet = env
    .readTextFile("./InputFiles/location.txt")
    .map(line => line.split(","))
    .map(words => (words(0).toInt, words(1)))

  val joinedSet = personSet.join(locationSet).where(0).equalTo(0) { (l, r) =>
    (l._1, l._2, r._2)
  }
  joinedSet.print()

}
