package SparkScalaQuestions

import com.datastax.spark.connector.{CassandraSparkExtensions, toSparkContextFunctions}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.sql.cassandra.DataFrameReaderWrapper
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType, TimestampType}

object LogsQuestion extends App {
  val conf = new SparkConf().setAppName("CassandraQuestions").setMaster("local[*]").set("spark.cassandra.connection.host","localhost")
  val sc = new SparkContext(conf)
  val myrdd =sc.cassandraTable("logs", "logdata")
  val spark = SparkSession.builder().master("local[*]").appName("SQLCassandra").getOrCreate()
  import spark.implicits._
  val inputDf = myrdd.keyBy(row => (row.getStringOption("user_name"),row.getStringOption("technology"),row.getStringOption("keyboard"),
  row.getStringOption("mouse"),row.getStringOption("boot_time"),row.getStringOption("date_time"))).map(x => x._1).toDF("UserName","Technology","Keyboard","Mouse","BootTime","DateTime")
  val castedDf = inputDf.withColumn("Keyboard",col("Keyboard").cast(DoubleType))
    .withColumn("Mouse",col("Mouse").cast(DoubleType))
    .withColumn("BootTime",col("BootTime").cast(TimestampType))
    .withColumn("DateTime",col("DateTime").cast(TimestampType))
  castedDf.printSchema()
  castedDf.createOrReplaceTempView("logdata")
  val countStudentsBasedOnTech = spark.sql("Select technology,count(1) as NumberOfStudents from logdata group by technology")
  countStudentsBasedOnTech.show(5)
  val timesKeyboardMouseZero = spark.sql("select count(keyboard) as keyboardCount,count(mouse) as MouseCount,username from logdata group by username having count(keyboard) =0 and count(mouse) = 0")
  timesKeyboardMouseZero.show(5)
  val lateHrs = spark.sql("select boottime,username,hour(boottime),minute(boottime) from logdata where hour(boottime) > 8 and minute(boottime) > 30 order by boottime")
  lateHrs.show(10)
  val adjustedWorkingHours = spark.sql("select username,mouse,keyboard,lag(mouse,1) over (partition by username order  by boottime,mouse) as firstValue," +
  " lag(mouse,2) over (partition by username order  by boottime,mouse) as secondValue,lag(mouse,3) over (partition by username order  by boottime,mouse) as thirdValue,"+
  "lag(mouse,4) over (partition by username order  by boottime, mouse) as fourthValue,lag(mouse,5) over (partition by username order  by boottime,mouse) as fifthValue," +
  "lag(mouse,6) over (partition by username order  by boottime,mouse) as sixthValue from logData")
  adjustedWorkingHours.createOrReplaceTempView("workingHours")
  val adjustedWorkingHoursNew = spark.sql("select distinct username,0.5 as value from workingHours where firstValue = 0 and secondValue = 0 and thirdValue = 0" +
    " and fourthValue = 0 and fifthValue = 0 and sixthValue = 0")
  adjustedWorkingHoursNew.show()
  adjustedWorkingHoursNew.createOrReplaceTempView("workingHours")
  val adjustedWorkingHoursfinal = spark.sql("select logdata.username,round((bigint(max(boottime)) - bigint(min(boottime)))/3600,2) - coalesce(value,0) as WorkingHrs  from logdata " +
    "left join workingHours  on workingHours.username = logdata.username group by logdata.username,to_date(BootTime),value")
  adjustedWorkingHoursfinal.show()

}
