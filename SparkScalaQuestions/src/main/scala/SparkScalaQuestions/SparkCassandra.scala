package SparkScalaQuestions

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector._
import org.apache.spark.sql.cassandra.DataFrameReaderWrapper
object SparkCassandra extends App {

  val conf = new SparkConf().setAppName("CassandraDemo").setMaster("local[*]").set("spark.cassandra.connection.host","localhost")
  val sc = new SparkContext(conf)
  val myrdd =sc.cassandraTable("home_tracker", "activity")
  println(myrdd.first())
//  val spark = SparkSession.builder().master("local[*]").appName("SQLCassandra") getOrCreate()
//  val df = spark.read.cassandraFormat("activity_new2", "home_tracker", "").option("inferSchema",true)load()
//  println(df.show())
}
