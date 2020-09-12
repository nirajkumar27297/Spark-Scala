package SparkScalaQuestions

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object JoinSpark {
  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")

  def innerjoin(firstRDD:RDD[(String,Int)],secondRDD:RDD[(String,Int)]) =
    firstRDD.join(secondRDD).collect().toList

  def leftjoin(firstRDD:RDD[(String,Int)],secondRDD:RDD[(String,Int)]) =
    firstRDD.leftOuterJoin(secondRDD).collect().toList

  def rightjoin(firstRDD:RDD[(String,Int)],secondRDD:RDD[(String,Int)]) =
    firstRDD.rightOuterJoin(secondRDD).collect().toList

  def findCoGroup(firstRDD:RDD[(String,Int)],secondRDD:RDD[(String,Int)]) =
    firstRDD.cogroup(secondRDD).collect().toList

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val firstRDD = sc.parallelize(List(("a",1),("b",4),("c",5)))
    val secondRDD = sc.parallelize(List(("a",2),("a",3),("d",7)))
    println(innerjoin(firstRDD,secondRDD))
    println(leftjoin(firstRDD,secondRDD))
    println(rightjoin(firstRDD,secondRDD))
    println(findCoGroup(firstRDD,secondRDD))
  }
}
