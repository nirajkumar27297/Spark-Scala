package SparkScalaQuestions

import org.apache.spark.{SparkConf, SparkContext}

object BroadCastVariables {

  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")
  val sc = new SparkContext(conf)
  var commonWords = Array("a","an","the","of","at","is","am")
  var commonWordsBC = sc.broadcast(commonWords)
  def toWords(line:String):Array[String] = {
    var words = line.split(" ")
    var output = Array[String]()
    for(word <- words) {
      if(! commonWordsBC.value.contains(word.toLowerCase.trim.replaceAll("[^a-zA-Z]]",""))) {
        output = output :+ word
      }
    }
    output
  }

  def main(args: Array[String]): Unit = {
    val inputRDD = sc.textFile(args(0))
    val uncommonWords = inputRDD.flatMap(toWords)
    uncommonWords.saveAsTextFile(args(1))
  }
}
