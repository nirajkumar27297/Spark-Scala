import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

case class Emp(id:Int,name:String,sal:Double)

object SparkStreamingTextFile extends App {
  val spark = SparkSession.builder().master("local[*]").appName("SparkStreaming").getOrCreate()
  val sc = spark.sparkContext
  val logger = Logger.getRootLogger
  logger.setLevel(Level.ERROR)
  val ssc = new StreamingContext(sc,Seconds(5))
  val empDstream = ssc.textFileStream("file:///home/niraj/inputFiles/streaming")
  empDstream.foreachRDD(rdd => rdd.map(line => line.split(",")).map(col => Emp(col(0).toInt,col(1),col(2).toDouble)).foreach(println(_)))
  ssc.start()
  ssc.awaitTermination()
}
