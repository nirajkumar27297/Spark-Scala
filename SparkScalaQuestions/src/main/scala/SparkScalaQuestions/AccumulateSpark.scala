package SparkScalaQuestions

import org.apache.spark.{SparkConf, SparkContext}

object AccumulateSpark {

  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")
  val sc = new SparkContext(conf)
  var numBlankLines = sc.longAccumulator("Blank Line Counter")
  def toWords(line:String):Array[String] = {
    if(line.length == 0) {
      numBlankLines.add(1)
    }
    line.split(" ")
}

  def main(args: Array[String]): Unit = {
    sc.setLogLevel("ERROR")
    val inputRDD = sc.textFile(args(1))
    val words = inputRDD.flatMap(toWords)
    words.saveAsTextFile(args(1))
    println("Total Number of Blank Lines are "+numBlankLines.value)
  }
}
