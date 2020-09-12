package SparkScalaQuestions

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object LookupSpark {
  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")

  def lookupKey(inputRDD:RDD[(Int,String)],key:Int) = {
    //Best Practice is always sort the RDD
    inputRDD.sortByKey()
    inputRDD.lookup(key)
  }

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val inputRDD = sc.parallelize(List((0,"Zero"),(1,"One"),(2,"Two"),(3,"Three"),(4,"Four"),(6,"Six"),(5,"Five")))
    println(lookupKey(inputRDD,5))
  }
}
