package bdu
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext

//import org.apache.spark.sql.SQLContext
//import org.apache.spark.sql.hive.HiveContext

import com.alibaba.fastjson.JSON
import collection.JavaConversions._

object Main {

  def main(args: Array[String]): Unit = {
    val clickFile = args(0)
    val infoFile = args(1)
    val outputFile = args(2)
    val conf = new SparkConf().setAppName("ufs from big data")
    val sc = new SparkContext(conf)

    val clickErrorJsonCount = sc.accumulator(0)
    val infoErrorRecordCount = sc.accumulator(0)
    val infoErrorJsonCount = sc.accumulator(0)

    val input = sc.textFile(clickFile)
    val clicks = (input.map {
      line =>
        try {
          val log = JSON.parseObject(line)
          if (log.getJSONObject("article_click_extra").getString("content_type") == "news") {
            Some((log.getJSONObject("article_click_extra").getString("content_id").trim(), log.getString("uid")))
          } else {
            None
          }
        } catch {
          case t: Throwable => {
            clickErrorJsonCount += 1
            None // TODO: handle error
          }
        }
    } filter {
      case Some(_) => true
      case _       => false
    } map {
      _.get
    }).distinct.persist

    val infos = sc.textFile(infoFile)
    val tags = infos.map {
      x =>
        {
          val fields = x.split("[\t]+")
          if (fields.length >= 12) {
            try {
              Some((fields(0).trim(), parseSignalTags(fields(11))))
            } catch {
              case t: Throwable => {
                infoErrorJsonCount += 1
                None
              }
            }
          } else {
            infoErrorRecordCount += 1
            None
          }
        }
    } filter {
      case Some(_) => true
      case _       => false
    } map {
      _.get
    }

    val cjt = (clicks.join(tags) map {
      case (k, v) => v
    } reduceByKey {
      case (m1, m2) => mergeTagMap(m1, m2)
    }).persist

    //    println("--------------------", clicks.count(), jj.count(), clickErrorJsonCount)

    println("**************************")
    println(clickErrorJsonCount, infoErrorRecordCount, infoErrorJsonCount)
    println("**************************")
    println(clicks.count, cjt.count)
    println("**************************")

//    cjt.saveAsTextFile(outputFile)
    cjt.saveAsObjectFile(outputFile)

    //    println(t.count())
    //    tags.saveAsTextFile(outputFile)

    //    val conf = new SparkConf().setAppName("ufs from big data")
    //    val sc = new SparkContext(conf)
    //    //    val sqlCtx = new SQLContext(sc)
    //    val sqlCtx = new HiveContext(sc)
    //
    //    import sqlCtx.implicits._
    //
    //    val rows = (sqlCtx.sql("select content_id,tags,update_time from db_nlp.s_t_signal_d limit 10") map {
    //      x => x(1)
    //    }).collect()
    //
    //    //    println(rows.count)
    //    rows foreach { x => println("--------" + x + "|||||||||||") }
  }

  //  (v + m1.get(ver).getOrElse(k, 0.0))

  def mergeTagMap(m1: Map[String, Map[String, Double]], m2: Map[String, Map[String, Double]]): Map[String, Map[String, Double]] = {
    m1 ++ (m2 map {
      case (ver, tags) => {
        if (!m1.contains(ver)) {
          ver -> tags
        } else {
          val vm1 = m1(ver)
          val vm2 = m2(ver)
          (ver -> (vm1 ++ (vm2 map {
            case (k: String, v: Double) => k -> (v + (vm1.getOrElse(k, 0.0)))
          })))
        }
      }
    })
  }

  def parseSignalTags(json: String): Map[String, Map[String, Double]] = {
    val obj = JSON.parseObject(json)
    (obj.keySet() map {
      ver =>
        {
          val vtags = obj.getJSONObject(ver)
          val tm = vtags.keySet() map {
            word: String =>
              {
                (word, vtags.getJSONObject(word).getDouble("weight").toDouble)
              }
          }
          (ver, tm.toMap)
        }
    }).toMap
  }
}