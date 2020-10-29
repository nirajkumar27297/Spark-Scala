package JoinOperations
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.{ExecutionEnvironment, createTypeInformation}

object LeftJoinDemo extends App {
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

  def leftJoinOperation(
      person: Tuple2[Int, String],
      location: Tuple2[Int, String]
  ): Tuple3[Integer, String, String] = {
    if (location == null)
      return new Tuple3[Integer, String, String](
        person._1,
        person._2,
        "NULL"
      )
    new Tuple3[Integer, String, String](
      person._1,
      person._2,
      location._2
    )
  }

  val joinedSet = personSet
    .leftOuterJoin(locationSet)
    .where(0)
    .equalTo(0)
    .apply((l, r) => leftJoinOperation(l, r))
  joinedSet.print()

}
