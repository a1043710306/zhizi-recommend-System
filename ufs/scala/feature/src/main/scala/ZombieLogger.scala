package ufs

import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{ read, write }

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

//import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.PreparedStatement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
//import com.sksamuel.elastic4s.ElasticClient
//import com.sksamuel.elastic4s.ElasticDsl._
//import com.sksamuel.elastic4s.ElasticsearchClientUri

import java.util.concurrent._

object ZombieLogger {
  val logger = LoggerFactory.getLogger(getClass)

  def updateOneTable(connectStr: String, idx: Int, lst: List[(String, Int)]): Int = {
    def updateOneUser(connectStr: String, idx: Int, uid: String, ts: Int): Int = {
      val tableName = "user_profile_" + idx

      val sql = s"UPDATE $tableName SET token_invalid_time = '$ts' WHERE uid = '$uid'"
      var dbConn: Connection = null
      var updateStmt: PreparedStatement = null
      try {
        dbConn = DriverManager.getConnection(connectStr)

        updateStmt = dbConn.prepareStatement(sql)
        val res = updateStmt.executeUpdate()
        // logger.error("update result: " + res + ", sql: " + sql)
        res
      } catch {
        case e: SQLException => {
          logger.error("exception: " + e.getMessage)
          0
        }
        case t: Throwable => {
          logger.error("exception: " + t.getMessage)
          0
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

    (lst map {
      case (uid, ts) => updateOneUser(connectStr, idx, uid, ts)
    }).sum
  }

  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    val clusterName = args(2)

    val dbUrl = args(3)
    val connectStr = MysqlPool.setupDriver("UserProfile", dbUrl)

    val settings = Settings.settingsBuilder().put("cluster.name", clusterName).build()
    val client = TransportClient.builder().settings(settings).build().
      addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port))

    var offset = 0
    val step = 10000
    var isFinished = false
    val tableNum = 256
    val executorService = Executors.newFixedThreadPool(tableNum)
    while (!isFinished) {
      val response = client.prepareSearch("tpushuser")
        .setTypes("tpushuser")
        //      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setQuery(QueryBuilders.termQuery("status", 0)) // Query
        .setQuery(QueryBuilders.rangeQuery("diable_time").from((System.currentTimeMillis() / (24 * 60 * 60 * 1000) - 1) * (24 * 60 * 60 * 1000))) // Filter
        .setFetchSource(List("uid", "status", "diable_time").toArray, null)
        .setFrom(offset).setSize(step) //.setExplain(true)
        .execute()
        .actionGet()

      if (response.getHits.size == 0) {
        isFinished = true
      } else {
        logger.info("records size: " + response.getHits.size)
        offset += response.getHits.size
        response.getHits map { _.getSource } map {
          x => (x("uid").toString(), x("diable_time").toString())
        } filter {
          case (_, st) => {
            try {
              val ts = st.toLong
              ts > Integer.MAX_VALUE
            } catch {
              case t: Throwable => false // TODO: handle error
            }
          }
        } map {
          case (uid, ts) => (uid, (ts.toLong / 1000).toInt)
        } groupBy {
          case (uid, _) => Util.getIdx(uid, "", tableNum)
        } map {
          case (idx, lst) => executorService.submit(new Callable[Int] {
            override def call(): Int = {
              updateOneTable(connectStr, idx, lst.toList)
            }
          })
        } foreach {
          _.get
          //          x => println(x.get())
        }
      }
    }

    executorService.shutdown()
    client.close()

    //    println(resp.await)
  }
}