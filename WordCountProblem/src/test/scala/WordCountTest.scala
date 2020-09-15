
import WordCountQuestion.{WordCount, WordCountExceptionEnum}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FunSuite

class WordCoutTest extends FunSuite {

  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")
  val inputFilePath = "./src/test/resources/lines.txt"
  val outputDirectory = "file:///home/niraj/IdeaProjects/WordCountProblem/output"
  val inputFilePathWrong = "./src/test/resources/line.txt"
  val secondInputFilePath = "./src/test/resources/linesWrong.txt"

  test("test_InputSameTextFiles_MatchCountOfWordsUsingRDD_ReturnTrue") {
    val sc = new SparkContext(conf)
    val rawData = sc.textFile(inputFilePath)
    val words = rawData.flatMap(line => line.split(" "))
    val wordsKv = words.map(word => (word, 1))
    val output = wordsKv.reduceByKey(_ + _)
    val testMap = output.collect().toMap
    val outputMap = new WordCount().countWordsRDD(sc,inputFilePath,outputDirectory)
    assert(testMap.equals(outputMap) == true)
    sc.stop()
  }

  test("test_InputDifferentTextFiles_MatchCountOfWords_ReturnFalse") {
    val sc = new SparkContext(conf)
    val rawData = sc.textFile(inputFilePath)
    val words = rawData.flatMap(line => line.split(" "))
    val wordsKv = words.map(word => (word, 1))
    val output = wordsKv.reduceByKey(_ + _)
    val testMap = output.collect().toMap
    val outputMap = new WordCount().countWordsRDD(sc,secondInputFilePath,outputDirectory)
    assert(testMap.equals(outputMap) == false)
    sc.stop()
  }

  test("test_InputWrongFilePathRDD_ThrowException") {
    val sc = new SparkContext(conf)
    val thrown = intercept[Exception] {
      val _ = new WordCount().countWordsRDD(sc,inputFilePathWrong,outputDirectory)
    }
    assert(thrown.getMessage == WordCountExceptionEnum.mapRIOException.toString)
    sc.stop()
  }

  test("test_InputSameTextFiles_MatchCountOfWordsUsingDataFrame_ReturnZero") {
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val textDf = spark.read.text(inputFilePath)
    val wordsDf = textDf.select(explode(split(textDf("value")," ")).alias("word"))
    val countDfTest = wordsDf.groupBy("word").count()
    val outputCountDF = new WordCount().countWordsDataFrame(spark,inputFilePath,outputDirectory)
    assert(outputCountDF.except(countDfTest).count() == 0)
  }

  test("test_InputSameTextFiles_MatchCountOfWordsUsingDataFrame_ReturnNonZero") {
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val textDf = spark.read.text(inputFilePath)
    val wordsDf = textDf.select(explode(split(textDf("value")," ")).alias("word"))
    val countDfTest = wordsDf.groupBy("word").count()
    val outputCountDF = new WordCount().countWordsDataFrame(spark,secondInputFilePath,outputDirectory)
    assert(outputCountDF.except(countDfTest).count() != 0)
  }

  test("test_InputWrongFilePathDataFrame_ThrowException") {
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val thrown = intercept[Exception] {
      val _ = new WordCount().countWordsDataFrame(spark,inputFilePathWrong,outputDirectory)
    }
    assert(thrown.getMessage == WordCountExceptionEnum.sparkSqlException.toString)
  }

  test("test_InputSameTextFiles_MatchCountOfWordsUsingDataSet_ReturnZero") {
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val textDataSet = spark.read.text(inputFilePath).as[String]
    val wordsDataset = textDataSet.flatMap(_.split(" ")).withColumnRenamed("value","words")
    val countDfTest = wordsDataset.groupBy("words").count()
    val outputCountDF = new WordCount().countWordsDataset(spark,inputFilePath,outputDirectory)
    assert(outputCountDF.except(countDfTest).count() == 0)
  }

  test("test_InputSameTextFiles_MatchCountOfWordsUsingDataSet_ReturnNonZero") {
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val textDataSet = spark.read.text(inputFilePath).as[String]
    val wordsDataset = textDataSet.flatMap(_.split(" ")).withColumnRenamed("value","words")
    val countDfTest = wordsDataset.groupBy("words").count()
    val outputCountDF = new WordCount().countWordsDataset(spark,secondInputFilePath,outputDirectory)
    assert(outputCountDF.except(countDfTest).count() != 0)
  }

  test("test_InputWrongFilePathForDataset_ThrowException") {
    val thrown = intercept[Exception] {
      val spark = SparkSession.builder().config(conf).getOrCreate()
      val _ = new WordCount().countWordsDataset(spark,inputFilePathWrong,outputDirectory)
    }
    assert(thrown.getMessage == WordCountExceptionEnum.sparkSqlException.toString )
  }
}
