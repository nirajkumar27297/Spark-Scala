/* The objective is add specific hadoop configuration to our
spark context object.
@author : Niraj Kumar
 */

package Configurations
import org.apache.spark.SparkContext

//Class for configuring Hadoop parameters
case class configurations(sparkContext: SparkContext) {
  val sparkContextObj = sparkContext
  //Configuring Hadoop Parameters and providing access key and secret key using environment variables
  def configurationsForHadoop() = {

    System.setProperty("com.amazonaws.services.s3.enableV4", "true")
    sparkContextObj.hadoopConfiguration.set(
      "fs.s3a.access.key",
      System.getenv("AWS_ACCESS_KEY_ID")
    )
    sparkContextObj.hadoopConfiguration.set(
      "fs.s3a.secret.key",
      System.getenv("AWS_SECRET_ACCESS_KEY")
    )
    sparkContextObj.hadoopConfiguration.set(
      "fs.s3a.endpoint",
      "s3.ap-south-1.amazonaws.com"
    )
    sparkContextObj.hadoopConfiguration.set(
      "fs.s3a.impl",
      "org.apache.hadoop.fs.s3a.S3AFileSystem"
    )
  }
}
