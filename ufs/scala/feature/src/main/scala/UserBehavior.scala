package ufs

import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{ read, write }
import com.redis._
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object UserBehavior {
  implicit val formats = DefaultFormats
  val logger = LoggerFactory.getLogger(getClass)

  object MsgType extends Enumeration {
    type MsgType = Value
    val Imp, Click, Emotion, None = Value
  }

  import MsgType._
  def detectType(msg: String): MsgType = {
    if (msg contains "article_impression_extra") {
      MsgType.Imp
    } else if (msg contains "article_click_extra") {
      MsgType.Click
    } else if (msg contains "ballot") {
      MsgType.Emotion
    } else {
      MsgType.None
    }
  }

  case class Extra(content_id: String, content_type: String)
  case class IEvent(uid: String, article_impression_extra: Extra)
  case class CEvent(uid: String, article_click_extra: Extra)

  case class UserEvent(uid: String, event: String)
  // {"ballot":{"Bored":1,"Like":0,"Angry":0,"Sad":0},"uid":"01011612151031375101000028012606","content_id":1029799908,"product_id":"mata","language":"Indonesian","timestamp":1481783175}

  //  case class Ballot(Bored:Int, Like:Int, Angry:Int, Sad:Int)
  case class EEvent(uid: String, ballot: Map[String, Int])

  //  type DictType = scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[String, Int]]

  def AccumulateImp(msg: String): List[UserEvent] = {
    try {
      val res = read[IEvent](msg)
      val uid = res.uid
      val event = "imp" + "_" + res.article_impression_extra.content_type.toLowerCase()

      List(new UserEvent(uid, event))
    } catch {
      case t: Throwable => {
        t.printStackTrace() // TODO: handle error
        List()
      }
    }
  }

  def AccumulateClick(msg: String): List[UserEvent] = {
    try {
      val res = read[CEvent](msg)
      val uid = res.uid
      val event = "click" + "_" + res.article_click_extra.content_type.toLowerCase()

      List(new UserEvent(uid, event))
    } catch {
      case t: Throwable => {
        t.printStackTrace() // TODO: handle error
        List()
      }
    }
  }

  def AccumulateEmotion(msg: String): List[UserEvent] = {
    try {
      val res = read[EEvent](msg)
      val uid = res.uid
      (res.ballot filter { case (k, v) => v > 0 } map { case (k, v) => new UserEvent(uid, "emotion_" + k.toLowerCase()) }).toList
    } catch {
      case t: Throwable => {
        t.printStackTrace() // TODO: handle error
        List()
      }
    }
  }

  def syncToSsdb(conns: Array[RedisClientPool], behavior: Map[String, Map[String, Int]]): Unit = {
    val behaviorHash = "zhizi.user.behavior"

    def batchRead(conn: RedisClientPool, behaviorHash: String, listOfUid: Iterable[String]): Iterable[(String, Map[String, Int])] = {
      // val conn = conns(idx)
      conn.withClient {
        client =>
          client.hmget[String, String](behaviorHash, listOfUid.toSeq: _*) match {
            case Some(a) => {
              a map {
                case (uid, json) => (uid, read[Map[String, Int]](json))
              }
            }
            case _ => {
              logger.error("hmget fail: " + listOfUid.size)
              List()
            }
          }
      }
    }

    def batchWrite(conn: RedisClientPool, behaviorHash: String, listOfKv: Iterable[(String, String)]): Int = {
      conn.withClient {
        client =>
          if (client.hmset(behaviorHash, listOfKv)) {
            listOfKv.size
          } else {
            0
          }
      }
    }

    val dbVals = behavior map {
      case (uid, _) => uid
    } groupBy {
      uid => Util.getIdx(uid, "", conns.size)
    } map {
      case (idx, listOfUid) => Util.task(batchRead(conns(idx), behaviorHash, listOfUid))
    } flatMap {
      _.join
    }

    val history = dbVals.toMap
    val written = (behavior map {
      case (uid, round) => (history contains uid) match {
        case true => (uid, round ++ history(uid).map { case (k, v) => k -> (v + round.getOrElse(k, 0)) })
        case _    => (uid, round)
      }
    } map {
      case (uid, mm) => {
        val ts = (System.currentTimeMillis()/1000).toInt
        (uid, compact(render((mm updated ("ts", ts)))))
        }
    } groupBy {
      case (uid, _) => Util.getIdx(uid, "", conns.size)
    } map {
      case (idx, listOfKv) => Util.task(batchWrite(conns(idx), behaviorHash, listOfKv))
    } map { _.join }).sum
    if (written < behavior.size) {
      logger.error("write fail: " + (behavior.size - written))
    }
    // 
  }

  // java -cp feature-assembly-1.0.jar ufs.UserBehavior "172.31.4.53:9092,172.31.4.54:9092,172.31.4.55:9092" "zhizi.behavior" impression-reformat click-reformat "ballot-comments" 172.31.36.230:8888
  def main(args: Array[String]): Unit = {
    val brokers = args(0)
    val group = args(1)
    val impTopic = args(2)
    val clickTopic = args(3)
    val emotionTopic = args(4)
    val addrs = args(5)
    val conns = Util.makePooledConns(addrs)

    val consumer = Util.getComsumer(brokers, group)
    consumer.subscribe(List(impTopic, clickTopic, emotionTopic))
    while (true) {
      val records = consumer.poll(1000)
      logger.error("got " + records.size + " msgs")
      val batchRes = records flatMap { record =>
        detectType(record.value) match {
          case MsgType.Imp     => AccumulateImp(record.value)
          case MsgType.Click   => AccumulateClick(record.value)
          case MsgType.Emotion => AccumulateEmotion(record.value)
          case _               => List()
        }
      } groupBy {
        case UserEvent(uid, _) => uid
      } map {
        case (uid, lst) => (uid, {
          lst.foldLeft(Map[String, Int]()) {
            case (acc, UserEvent(_, z)) => acc updated (z, {
              (acc contains z) match {
                case true => acc(z) + 1
                case _    => 1
              }
            })
          }
        })
      }
      syncToSsdb(conns, batchRes.toMap)
    }
  }
}
