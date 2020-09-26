package SparkScalaQuestions

import com.datastax.spark.connector.CassandraSparkExtensions
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.TimestampType

object CassandraSparkQuestions extends App {

  //configuration settings for spark session
  def configuration(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .withExtensions(new CassandraSparkExtensions)
      .getOrCreate()

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    spark.conf.set("spark.cassandra.connection.host", "localhost")
    spark.conf.set(
      "spark.sql.catalog.casscatalog",
      "com.datastax.spark.connector.datasource.CassandraCatalog"
    )
    spark
  }
  //taking input with input parameter keyspace name,table name and spark session object and return dataframe
  def takingInput(
      keyspace: String,
      tableName: String,
      spark: SparkSession
  ): DataFrame = {
    val table = "casscatalog." + keyspace + "." + tableName
    println(table)
    val inputDF = spark.read.table(table)

    val castedDF = inputDF.select(
      col("mouse"),
      col("boot_time").alias("BootTime").cast(TimestampType),
      col("date_time").alias("DateTime"),
      col("technology"),
      col("user_name").alias("username"),
      col("keyboard")
    )
    castedDF.show()
    castedDF.printSchema()
    castedDF.createOrReplaceTempView("logdata")
    castedDF
  }

  //Finding number of students per technology
  def countingStudentsByTech(
      inputDF: DataFrame,
      spark: SparkSession
  ): DataFrame = {
    inputDF.createOrReplaceTempView("logdata")
    val countStudentsBasedOnTech = spark.sql(
      "Select technology,count(1) as NumberOfStudents from logdata group by technology"
    )
    countStudentsBasedOnTech
  }

  //Finding students whose keyboard and mouse count is Zero
  def studentsKeyBoardMouseCountZero(
      inputDF: DataFrame,
      spark: SparkSession
  ): DataFrame = {
    inputDF.createOrReplaceTempView("logdata")
    val timesKeyboardMouseZero = spark.sql(
      "select count(keyboard) as keyboardCount,count(mouse) as MouseCount,username from logdata group by username having count(keyboard) =0 and count(mouse) = 0"
    )
    timesKeyboardMouseZero
  }
  //Finding students who are late i.e. time is after 8:30 am
  def lateStudents(inputDF: DataFrame, spark: SparkSession): DataFrame = {
    inputDF.createOrReplaceTempView("logdata")
    val lateHrs = spark.sql(
      "select boottime,username,hour(boottime),minute(boottime) from logdata where hour(boottime) > 8 and minute(boottime) > 30 order by boottime"
    )
    lateHrs
  }

  //If consecutive six times keyboard and mouse count is zero then adjusting working hrs i.e. decreasing by 0.5 hr
  def adjustedWorkingHrs(inputDF: DataFrame, spark: SparkSession): DataFrame = {
    inputDF.createOrReplaceTempView("logdata")
    val adjustedWorkingHours = spark.sql(
      "select username,mouse,keyboard,lag(mouse,1) over (partition by username order  by boottime,mouse) as firstValue," +
        " lag(mouse,2) over (partition by username order  by boottime,mouse) as secondValue,lag(mouse,3) over (partition by username order  by boottime,mouse) as thirdValue," +
        "lag(mouse,4) over (partition by username order  by boottime, mouse) as fourthValue,lag(mouse,5) over (partition by username order  by boottime,mouse) as fifthValue," +
        "lag(mouse,6) over (partition by username order  by boottime,mouse) as sixthValue from logData"
    )
    adjustedWorkingHours.createOrReplaceTempView("workingHours")
    val adjustedWorkingHoursNew = spark.sql(
      "select distinct username,0.5 as value from workingHours where firstValue = 0 and secondValue = 0 and thirdValue = 0" +
        " and fourthValue = 0 and fifthValue = 0 and sixthValue = 0"
    )
    adjustedWorkingHoursNew.show()
    adjustedWorkingHoursNew.createOrReplaceTempView("workingHours")
    val adjustedWorkingHoursfinal = spark.sql(
      "select logdata.username,round((bigint(max(boottime as timestamp)) - bigint(max(boottime as timestamp))) / 3600,2) - coalesce(value,0) as wrkHrs from logdata  left join workingHours " +
        "on logdata.username = workingHours.username group by logdata.username,to_date(boottime),value"
    )
    adjustedWorkingHoursfinal
  }

  val spark = configuration()
  val outputDF = takingInput("logs", "logdata", spark)
  val countStudentsBasedOnTech = countingStudentsByTech(outputDF, spark)
  countStudentsBasedOnTech.show(5)
  val timesKeyboardMouseZero = studentsKeyBoardMouseCountZero(outputDF, spark)
  timesKeyboardMouseZero.show(5)
  val lateHrs = lateStudents(outputDF, spark)
  lateHrs.show(10)
  val adjustedWorkingHoursfinal = adjustedWorkingHrs(outputDF, spark)
  adjustedWorkingHoursfinal.show()
}
