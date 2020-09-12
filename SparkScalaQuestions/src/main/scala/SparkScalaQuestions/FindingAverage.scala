package SparkScalaQuestions

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FindingAverage {
  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")

  def getAverage(inputrdd:RDD[Int]):Double = {
    val rddPair = inputrdd.map(x => (x, 1))
    val (sum, count) = rddPair.reduce((x, y) => (x._1 + y._1, x._2 + y._2))
    sum * 1.0 / count
  }

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val inputRdd = sc.parallelize(1 to 100)
    println(getAverage(inputRdd))
    sc.stop()
  }
}
