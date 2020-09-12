package SparkScalaQuestions

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FindingStdDev {

  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")

  def findStdDev(inputRDD: RDD[Int]): Double = {
    val rddPair = inputRDD.map((_, 1))
    val (sum, count) = rddPair.reduce((x, y) => (x._1 + y._1, x._2 + y._2))
    val average = sum * 1.0 / count
    val sqDiff = inputRDD.map(_ - average).map(math.pow(_, 2))
    println(sqDiff.toDebugString) //Printing Lineage
    val sumSqDiff = sqDiff.reduce(_ + _)
    val standardDeviation = math.sqrt(sumSqDiff * 1.0 / count)
    standardDeviation

  }

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val inputRdd = sc.parallelize(Array(2, 3, 4, 5, 6, 7, 8, 9))
    println(findStdDev(inputRdd))
    sc.stop()
  }
}
