package SparkScalaQuestions

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel

object PersistSpark {
  val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")

  def mysum(itr:Iterator[Int]):Iterator[Int] = {
    Array(itr.sum).toIterator
  }

  def incrByOne(x:Int):Int = x + 1

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(conf)
    val nums = sc.parallelize(1 to 100000,50)
    val partitions = nums.mapPartitions(mysum)
    val paritionsNew = partitions.map(incrByOne)
    paritionsNew.persist() // Default Storage Level
    println(paritionsNew.collect().take(20).toList)
    paritionsNew.unpersist()
    //userDefined Storage level
    val mysl = StorageLevel(true,true,false,true,1)
    paritionsNew.persist(mysl)
    println(paritionsNew.collect().take(20).toList)
    paritionsNew.unpersist()
  }
}
