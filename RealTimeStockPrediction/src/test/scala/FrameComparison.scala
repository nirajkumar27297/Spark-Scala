import org.apache.spark.sql.DataFrame

class FrameComparison {

  def frameComparison(
      firstDataFrame: DataFrame,
      secondDataFrame: DataFrame
  ): Boolean = {
    if (
      firstDataFrame.schema
        .toString()
        .equalsIgnoreCase(secondDataFrame.schema.toString())
      &&
      firstDataFrame
        .unionAll(secondDataFrame)
        .except(firstDataFrame.intersect(secondDataFrame))
        .count() == 0
    ) {
      return true
    }
    false
  }
}
