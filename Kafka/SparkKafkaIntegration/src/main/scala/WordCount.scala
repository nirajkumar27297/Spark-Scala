import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WordCount extends App {
  val spark = SparkSession.builder().master("local[*]").appName("SparkStreaming").getOrCreate()
  val sc = spark.sparkContext
  val logger = Logger.getRootLogger
  logger.setLevel(Level.ERROR)
  val ssc = new StreamingContext(sc,Seconds(5))
  val data = ssc.socketTextStream("localhost",7890)
  val wordCount = data.flatMap(_.split(" ")).map(word => (word,1)).reduceByKey(_ + _)
  wordCount.print()
  ssc.start()
  ssc.awaitTermination()

}

