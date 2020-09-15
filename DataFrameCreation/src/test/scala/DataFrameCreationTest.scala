import com.bridgelabz.spark.{DataFrameBuilderExceptionEnum, DataFramefromFileBuilder, FrameComparison}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.scalatest.FunSuite


class DataFrameCreationTest extends FunSuite{
  val spark = SparkSession.builder().master("local[*]").appName("Builder").getOrCreate()
  val churnCSV = "file:///home/niraj/inputFiles/Churn.csv"
  val wrongPathCSV = "file:///home/niraj/inputFiles/churn_re.csv"
  val irisJson = "file:///home/niraj/inputFiles/iris.json"
  val churnCSVNew = "file:///home/niraj/inputFiles/Churn_New.csv"
  val irisNew = "file:///home/niraj/inputFiles/iris_New.json"
  val userDataParaquet = "file:///home/niraj/inputFiles/userdata1.parquet"
  val userDataParaquetNew = "file:///home/niraj/inputFiles/userdata2.parquet"
  val epsiodeAvro = "file:///home/niraj/inputFiles/episodes.avro"
  val userdataAvro = "file:///home/niraj/inputFiles/userdata1.avro"


  test("test_InputCSVFile_CreatingDataFrames_ReturnEqual") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createCSVDataFrame(churnCSV)
    val secondDataFrame = spark.read.option("header", "true").option("inferSchema","true").csv(churnCSV)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == true)
  }

  test("test_InputWrongCSVFilePath_ThrowInterception") {
    val objCreation = new DataFramefromFileBuilder()
    val thrown = intercept[Exception] {
        objCreation.createCSVDataFrame(wrongPathCSV)
    }
    assert(thrown.getMessage == DataFrameBuilderExceptionEnum.sparkSqlException.toString)
  }

  test("test_InputCSVFile_CreatingDataFrames_ReturnUnequal") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createCSVDataFrame(churnCSV)
    val secondDataFrame = spark.read.option("header", "true").option("inferSchema","true").csv(churnCSVNew)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == false)
  }

  test("test_InputJsonFile_CreatingDataFrames_ReturnEqual") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createJSONDataFrame(irisJson)
    val secondDataFrame = spark.read.json(irisJson)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == true)
  }

  test("test_InputJsonFile_CreatingDataFrames_ReturnUnequal") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createJSONDataFrame(irisJson)
    val secondDataFrame = spark.read.json(irisNew)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == false)
  }

  test("test_InputParaquetFile_CreatingDataFrames_ReturnEqual") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createParaquetDataFrame(userDataParaquet)
    val secondDataFrame = spark.read.parquet(userDataParaquet)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == true)
  }

  test("test_InputParaquetFile_CreatingDataFrames_ReturnUnequal") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createParaquetDataFrame(userDataParaquet)
    val secondDataFrame = spark.read.parquet(userDataParaquetNew)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == false)
  }

  test("test_InputAvroFile_CreatingDataFrames_ReturnEqual") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createAvroDataFrame(epsiodeAvro)
    val secondDataFrame = spark.read.format("avro").load(epsiodeAvro)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == true)
  }

  test("test_InputAvroFile_CreatingDataFrames_ReturnUnequal") {
    val objCreation = new DataFramefromFileBuilder()
    val firstDataFrame = objCreation.createAvroDataFrame(epsiodeAvro)
    val secondDataFrame = spark.read.format("avro").load(userdataAvro)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(firstDataFrame,secondDataFrame) == false)
  }

  test("test_InputWrongFile_ThrowException") {
    val objCreation = new DataFramefromFileBuilder()
    val thrown = intercept[Exception] {
      objCreation.createAvroDataFrame(churnCSV)
    }
    assert(thrown.getMessage == DataFrameBuilderExceptionEnum.sparkException.toString)
  }

  test("test_InputArraySame_ReturnEqual") {
    val inputArray = List(Row("Category A", 100, "This is category A"),
      Row("Category B", 120, "This is category B"),
      Row("Category C", 150, "This is category C"))
    val rdd = spark.sparkContext.parallelize(inputArray)
    val schema = StructType(List(
      StructField("Category", StringType, true),
      StructField("Count", IntegerType, true),
      StructField("Description", StringType, true)
    ))
    val testdataFrame = spark.createDataFrame(rdd,schema)
    val objCreation = new DataFramefromFileBuilder()
    val outputDataFrame = objCreation.createListDataFrame(inputArray,schema)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(testdataFrame,outputDataFrame) == true)
  }
  test("test_InputArrayDifferent_ReturnFalse") {
    val inputArray = List(Row("Category A", 100, "This is category A"),
      Row("Category B", 120, "This is category B"),
      Row("Category C", 150, "This is category C"))
    val rdd = spark.sparkContext.parallelize(inputArray)
    val schema = StructType(List(
      StructField("Category", StringType, true),
      StructField("Count", IntegerType, true),
      StructField("Description", StringType, true)
    ))
    val functionInputArray = List(Row("Category T", 100, "This is category A"),
      Row("Category B", 120, "This is category B"),
      Row("Category C", 150, "This is category C"))
    val testdataFrame = spark.createDataFrame(rdd,schema)
    val objCreation = new DataFramefromFileBuilder()
    val outputDataFrame = objCreation.createListDataFrame(functionInputArray,schema)
    val frameCompareObj = new FrameComparison()
    assert(frameCompareObj.frameComparison(testdataFrame,outputDataFrame) == false)
  }

  test("test_InputArrayNull_ThrowException") {
    val thrown = intercept[Exception] {
      val objCreation = new DataFramefromFileBuilder()
      val _ = objCreation.createListDataFrame(null, null)
    }
    assert(thrown.getMessage == DataFrameBuilderExceptionEnum.emptyArray.toString)
  }


  test("test_InputArraySchemaIsWrong_ThrowException") {
    val thrown = intercept[Exception] {
      val functionInputArray = List(Row("Category T", 100, "This is category A"),
        Row("Category B", 120, "This is category B"),
        Row("Category C", 150, "This is category C"))
      val schema = StructType(List(
        StructField("Category", IntegerType, true),
        StructField("Count", IntegerType, true),
        StructField("Description", StringType, true)
      ))
      val objCreation = new DataFramefromFileBuilder()
      val _ = objCreation.createListDataFrame(functionInputArray, schema)
    }
    assert(thrown.getMessage == DataFrameBuilderExceptionEnum.sparkException.toString)
  }

  test("test_InputArrayEmpty_ThrowException") {
    val thrown = intercept[Exception] {
      val objCreation = new DataFramefromFileBuilder()
      val _ = objCreation.createListDataFrame(List(), null)
    }
    assert(thrown.getMessage == DataFrameBuilderExceptionEnum.emptyArray.toString)
  }
}
