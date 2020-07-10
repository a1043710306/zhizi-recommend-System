package bdu
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext
import org.apache.spark.rdd._

object TestObjectFile {
  def main(args: Array[String]): Unit = {

    val clickFile = args(0)
    val infoFile = args(1)
    val conf = new SparkConf().setAppName("ufs from big data")
    val sc = new SparkContext(conf)

    val ff: RDD[(String, Map[String, Map[String, Double]])] = sc.objectFile[(String, Map[String, Map[String, Double]])](clickFile).persist
    println(ff.count)
    ff.take(10) foreach { println }
  }
}