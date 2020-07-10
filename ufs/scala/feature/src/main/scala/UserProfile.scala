package ufs

import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{ read, write }
import com.redis._

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.PreparedStatement

import java.text.SimpleDateFormat
import scala.collection.immutable.Iterable

import org.slf4j.Logger
import org.slf4j.LoggerFactory


object UserProfile {
  implicit val formats = DefaultFormats
  val logger = LoggerFactory.getLogger(getClass)

  //  {
  //    "aid": "ea83a8052389c9cb",
  //    "app": "noticias",
  //    "app_lan": "unknown",
  //    "app_ver": "1.1.1.0.0.3",
  //    "event_time": "2016-12-16 11:17:42",
  //    "gate_ip": "10.10.40.169",
  //    "imei": "860857037487248",
  //    "log_type": "profile",
  //    "mac": "48:3c:0c:d2:b9:2e",
  //    "model": "pm",
  //    "network": "",
  //    "osv": "",
  //    "product_id": "noticias",
  //    "promotion": "gp_null",
  //    "protocol": "https",
  //    "uid": "01011612161117425201000030529503"
  //}
  case class UserProfile(aid: String, app: String, app_lan: String,
                         app_ver: String, event_time: String, imei: String, mac: String,
                         product_id: String, promotion: String, uid: String, log_type: String)

//  def parseDateTime(dt: String): Int = {
//    try {
//      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//      val date = df.parse(dt)
//      (date.getTime() / 1000).toInt
//    } catch {
//      case t: Throwable => {
//        t.printStackTrace() // TODO: handle error
//        (System.currentTimeMillis() / 1000).toInt
//      }
//    }
//  }

  def batchAdd(connectStr: String, numOfTables: Int, users: List[UserProfile]): Int = {
    logger.error("thread: " + Thread.currentThread().getId)
    (users map {
      x => addNewUser(connectStr, numOfTables, x)
    }).sum
  }

  def addNewUser(connectStr: String, numOfTables: Int, user: UserProfile): Int = {
    val idx = Util.getIdx(user.uid, "", numOfTables)
    val tableName = "user_profile_" + idx.toString
    val insertSql = "INSERT INTO " + tableName + " " +
      """(aid, app, app_lan, app_ver, event_time, imei, mac, product_id, promotion, uid) 
      VALUES (?,?,?,?,?,?,?,?,?,?)"""

    val selectSql = "SELECT uid FROM " + tableName + " WHERE uid=?"

    var dbConn: Connection = null
    var prepareStmt: PreparedStatement = null
    var selectStmt: PreparedStatement = null

    try {
      dbConn = DriverManager.getConnection(connectStr)

      selectStmt = dbConn.prepareStatement(selectSql)
      selectStmt.setString(1, user.uid);
      val rs = selectStmt.executeQuery()
      if (rs.next()) { // already exist
        logger.error("exist: " + idx + ", " + user.uid)
        return 0
      }

      prepareStmt = dbConn.prepareStatement(insertSql)
      prepareStmt.setString(1, user.aid)
      prepareStmt.setString(2, user.app)
      prepareStmt.setString(3, user.app_lan)
      prepareStmt.setString(4, user.app_ver)
      prepareStmt.setInt(5, Util.parseDateTime(user.event_time, 8))
      prepareStmt.setString(6, user.imei)
      prepareStmt.setString(7, user.mac)
      prepareStmt.setString(8, user.product_id)
      prepareStmt.setString(9, user.promotion)
      prepareStmt.setString(10, user.uid)
      prepareStmt.executeUpdate();
    } catch {
      case e: SQLException => {
        logger.error("sql error, idx: " + idx + ", " + user.uid)
        -1
      }
      case t: Throwable => {
        -1
      }
    } finally {
      if (prepareStmt != null) {
        prepareStmt.close()
      }
      if (selectStmt != null) {
        selectStmt.close()
      }
      if (dbConn != null) {
        dbConn.close()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val brokers = args(0)
    val group = args(1)
    val profileTopic = args(2)
    //    val addrs = args(3)
    //    val conns = Util.makePooledConns(addrs)
    val dbUrl = args(3)

    val threadNum = args(4).toInt

    val connectStr = MysqlPool.setupDriver("UserProfile", dbUrl)
    val consumer = Util.getComsumer(brokers, group)
    consumer.subscribe(List(profileTopic))

    val numOfTables = 256
    while (true) {
      val records = consumer.poll(1000)
      logger.error("got " + records.size + " msgs")
      val batchRes = records map {
        msg =>
          {
            try {
              val up = read[UserProfile](msg.value)
              Some(up)
            } catch {
              case t: Throwable => {
                t.printStackTrace() // TODO: handle error
                None
              }
            }
          }
      } filter {
        case Some(up) => up.log_type == "profile"
        case _        => false
      } map {
        _.get
      }

      val batchNum = (batchRes.size / threadNum) + 1
      val tasks = batchRes.grouped(batchNum) map { x =>
        new Thread {
          override def run {
            batchAdd(connectStr, numOfTables, x.toList)
          }
        }
      }

      tasks foreach { _.start }
    }
  }
}

