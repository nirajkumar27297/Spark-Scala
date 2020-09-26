package SparkScalaQuestions


import com.datastax.spark.connector.CassandraSparkExtensions
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

object CassandraSparkQuestions extends App {
  val spark = SparkSession.builder().master("local[*]").withExtensions(new CassandraSparkExtensions).getOrCreate()

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  spark.conf.set("spark.cassandra.connection.host","localhost")
  spark.conf.set("spark.sql.catalog.casscatalog","com.datastax.spark.connector.datasource.CassandraCatalog")
  val inputDF = spark.read.table("casscatalog.logs.logdata")

  val castedDF = inputDF.select(col("mouse"),
    col("boot_time").alias("BootTime"),
    col("date_time").alias("DateTime"),
    col("technology"),
    col("user_name").alias("username"),
    col("keyboard"))
  castedDF.show()
  castedDF.printSchema()
  castedDF.createOrReplaceTempView("logdata")
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
  val adjustedWorkingHoursfinal = spark.sql("select logdata.username,round((bigint(max(cast(boottime as timestamp))) - bigint(min(cast(boottime as timestamp))))/3600,2) - coalesce(value,0) as wrkHrs from logdata  left join workingHours " +
    "on logdata.username = workingHours.username group by logdata.username,to_date(boottime),value")
  adjustedWorkingHoursfinal.show()


}