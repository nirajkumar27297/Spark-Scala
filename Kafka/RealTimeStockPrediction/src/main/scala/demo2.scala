import UtilityPackage.Utility
import org.apache.spark.sql.DataFrame
object demo2 extends App {

  def takeValue(batchDF: DataFrame) = {
    batchDF.show(5)
  }
  val brokers = "localhost:9092"
  val topics = "kafkatutorial"
  val sparkSessionObj = Utility.createSessionObject("Stock Price Test")
  sparkSessionObj.sparkContext.setLogLevel("ERROR")
  val inputDataFrame = sparkSessionObj.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", brokers)
    .option("subscribe", topics)
    .option("startingOffsets", "earliest")
    .load()

  inputDataFrame.writeStream
    .format("console")
    .foreachBatch((batchDF: DataFrame, _: Long) => takeValue(batchDF))
    .option("checkpointLocation", "chk-point-dir-test")
    .start()
    .awaitTermination()

}
