package SparkScalaQuestions

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Sorting {

  val conf = new SparkConf().setMaster("local[*]").setAppName("findingMaximum")

  def sorting(inputRDD: RDD[(Char, Int)]) =
    inputRDD.sortByKey(true).collect().toList

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val inputList = List(('a', 1), ('b', 2), ('1', 2), ('d', 4), ('2', 5))
    val inputRDD = sc.parallelize(inputList)
    println(sorting(inputRDD))
    sc.stop()
  }
}
