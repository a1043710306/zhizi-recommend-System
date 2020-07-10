package ufs

import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{ read, write }

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.PreparedStatement

import java.text.SimpleDateFormat
import scala.collection.immutable.Iterable
import com.redis._
import java.lang.Boolean

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object FreshUserUpdater {
  implicit val formats = DefaultFormats

  val logger = LoggerFactory.getLogger(getClass)

  def handleOneTable(connectStr: String, idx: Int, conns: Array[RedisClientPool]): Int = {

    def get7dayUids(connectStr: String, idx: Int): List[String] = {
      val tableName = "user_profile_" + idx.toString
      //      val sql = "SELECT uid FROM " + tableName + " WHERE event_time > '" + (System.currentTimeMillis() / 1000 - 7 * 24 * 60 * 60) + "'"

      val sql = "SELECT uid FROM " + tableName + " WHERE event_time > '" + (System.currentTimeMillis() / (1000 * 24 * 60 * 60) - 7) * (24 * 60 * 60) + "'"
      var dbConn: Connection = null
      var selectStmt: PreparedStatement = null
      try {
        dbConn = DriverManager.getConnection(connectStr)

        selectStmt = dbConn.prepareStatement(sql)
        val rs = selectStmt.executeQuery()
        var res: List[String] = List()
        while (rs.next()) { // already exist
          res ::= rs.getString("uid")
        }
        res
      } catch {
        case e: SQLException => {
          List()
        }
        case t: Throwable => {
          List()
        }
      } finally {
        if (selectStmt != null) {
          selectStmt.close()
        }
        if (dbConn != null) {
          dbConn.close()
        }
      }
    }

    def checkFreshness(uid: String, conns: Array[RedisClientPool]): Boolean = {
      // todo check against zhizi.behavior
      val behaviorHash = "zhizi.user.behavior"
      val idx = Util.getIdx(uid, "", conns.size);
      conns(idx).withClient {
        client =>
          {
            client.hget(behaviorHash, uid) match {
              case Some(json) => {
                val counter = read[Map[String, Int]](json)
                val impCount = (counter filter {
                  case (k, v) => (k.length() >= 4 && k.substring(0, 4) == "imp_")
                } map {
                  case (k, v) => v
                }).sum

                val clickCount = (counter filter {
                  case (k, v) => (k.length() >= 6 && k.substring(0, 6) == "click_")
                } map {
                  case (k, v) => v
                }).sum

                impCount <= 120 || clickCount <= 12
              }
              case _ => true
            }
          }
      }
    }

    def checkExist(connectStr: String, uid: String): Boolean = {

      val sql = s"SELECT uid FROM daily_fresh_user WHERE uid='$uid'"

      var dbConn: Connection = null
      var updateStmt: PreparedStatement = null
      try {
        dbConn = DriverManager.getConnection(connectStr)

        updateStmt = dbConn.prepareStatement(sql)
        val rs = updateStmt.executeQuery()
        rs.next()
      } catch {
        case e: SQLException => {
          logger.error("update fail: " + sql)
          false
        }
        case t: Throwable => {
          logger.error("update fail: " + sql)
          false
        }
      } finally {
        if (updateStmt != null) {
          updateStmt.close()
        }
        if (dbConn != null) {
          dbConn.close()
        }
      }
    }

    def updateMysql(connectStr: String, uids: List[String]): Int = {
      if (uids.size == 0) {
        return 0
      }
      assert(uids.size <= 100)

      val ed = (System.currentTimeMillis() / 1000) / (24 * 60 * 60)
      val us = uids.map { uid => "'" + uid + "'" }.mkString(",")
      val sql = s"UPDATE daily_fresh_user SET epoch_day = '$ed' WHERE uid IN ($us)"
      //      val sql = sqlPrefix + (uids map { uid => "('" + uid + "', '" + (System.currentTimeMillis() / 1000) / (24 * 60 * 60) + "')" }).mkString(",")

      var dbConn: Connection = null
      var updateStmt: PreparedStatement = null
      try {
        dbConn = DriverManager.getConnection(connectStr)

        updateStmt = dbConn.prepareStatement(sql)
        updateStmt.executeUpdate()
      } catch {
        case e: SQLException => {
          logger.error("update fail: " + sql)
          -1
        }
        case t: Throwable => {
          logger.error("update fail: " + sql)
          -1
        }
      } finally {
        if (updateStmt != null) {
          updateStmt.close()
        }
        if (dbConn != null) {
          dbConn.close()
        }
      }
    }

    def tryUpdateFreshness(connectStr: String, uids: List[String]): Int = {
      val gg = uids groupBy {
        uid: String => checkExist(connectStr, uid)
      }
      val wc = if (gg.contains(false)) { writeToMysql(connectStr, gg(false)) } else { 0 }
      val uc = if (gg.contains(true)) { updateMysql(connectStr, gg(true)) } else { 0 }
      wc + uc
    }

    def writeToMysql(connectStr: String, uids: List[String]): Int = {
      if (uids.size == 0) {
        return 0
      }
      assert(uids.size <= 100)

      val sqlPrefix = "INSERT INTO daily_fresh_user (uid, epoch_day) VALUES "
      val sql = sqlPrefix + (uids map { uid => "('" + uid + "', '" + (System.currentTimeMillis() / 1000) / (24 * 60 * 60) + "')" }).mkString(",")

      var dbConn: Connection = null
      var insertStmt: PreparedStatement = null
      try {
        dbConn = DriverManager.getConnection(connectStr)

        insertStmt = dbConn.prepareStatement(sql)
        insertStmt.executeUpdate()
      } catch {
        case e: SQLException => {
          //          println("insert fail: " + sql)
          logger.error("insert fail: " + sql)
          -1
        }
        case t: Throwable => {
          //          println("insert fail: " + sql)
          logger.error("insert fail: " + sql)
          -1
        }
      } finally {
        if (insertStmt != null) {
          insertStmt.close()
        }
        if (dbConn != null) {
          dbConn.close()
        }
      }
    }

    (get7dayUids(connectStr, idx) filter {
      uid => checkFreshness(uid, conns)
    } grouped { 100 } map {
      uids =>
        tryUpdateFreshness(connectStr, uids)
    }).sum
  }

  def main(args: Array[String]): Unit = {
    val dbUrl = args(0)
    val addrs = args(1)
    val connectStr = MysqlPool.setupDriver("UserProfile", dbUrl)
    val conns = Util.makePooledConns(addrs)

    (0 until 256) map {
      idx =>
        {
          new Thread {
            override def run {
              handleOneTable(connectStr, idx, conns)
            }
          }
        }
    } foreach { _.start }
  }
}