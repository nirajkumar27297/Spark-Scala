package MachineLearning

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.{MinMaxScaler, VectorAssembler}
import org.apache.spark.sql.SparkSession

object MinMaxScaler extends App {

  def createSession(): SparkSession = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("MinMaxScaler")
      .getOrCreate()

    spark
  }
  val spark = createSession()

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val inputDF = spark.read
    .option("header", true)
    .option("inferSchema", true)
    .csv("./src/test/resources/diabetes.csv")
  inputDF.printSchema()
  inputDF.show()

  val assembler = new VectorAssembler()
    .setInputCols(inputDF.drop("Outcome").columns)
    .setOutputCol("features")

  val output = assembler.transform(inputDF).select("Outcome", "features")
  output.show()

  val scaler =
    new MinMaxScaler().setInputCol("features").setOutputCol("scaledFeatures")
  val scalerModel = scaler.fit(output)
  val scaledDF =
    scalerModel.transform(output).select("Outcome", "scaledFeatures")

  println(s"Features are in the range ${scaler.getMin} and ${scaler.getMax}")
  scaledDF.show()

}
