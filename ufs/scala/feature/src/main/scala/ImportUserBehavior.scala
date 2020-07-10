package ufs
import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus }
import org.apache.hadoop.io.compress.CompressionCodec
import org.apache.hadoop.io.compress.CompressionCodecFactory
import com.redis._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{ read, write }
import com.sun.beans.decoder.FalseElementHandler

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ImportUserBehavior {
  implicit val formats = DefaultFormats
  val logger = LoggerFactory.getLogger(getClass)

  case class UserBehavior(uid: String, json: String)

  def getFs(confPath: String): FileSystem = {
    val conf = new Configuration()
    val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
    val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

    conf.addResource(hdfsCoreSitePath)
    conf.addResource(hdfsHDFSSitePath)
    FileSystem.get(conf)
  }

  def read(fs: FileSystem, filePath: String): Iterator[UserBehavior] = {

    //    val factory = new CompressionCodecFactory(new Configuration)
    //    val path = new Path(filePath)
    //    val codec = factory.getCodec(path)
    try {
      val path = new Path(filePath)
      val file = fs.open(path)
      val lines = Source.createBufferedSource(file).getLines()

      //      val file = codec.createInputStream(fs.open(path))
      //      val lines = Source.createBufferedSource(file).getLines()

      lines filter {
        x =>
          {
            try {
              val fields = x.split("[\t]+")
              fields.length >= 2
              //              fields.length >= 5 && fields(2).toFloat > 0
            } catch {
              case t: Throwable => {
                t.printStackTrace() // TODO: handle error
                false
              }
            }
          }
      } map {
        x =>
          {
            val fields = x.split("[\t]+")
            new UserBehavior(fields(0), fields(1))
          }
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace() // TODO: handle error
        Iterator[UserBehavior]()
    }

  }

  def connWrite(idx: Int, conn: RedisClientPool, interval: Int, mkt: Array[UserBehavior]) = {
    val hash = "zhizi.user.behavior"

    val lst = mkt
    var i = 0
    val step = 50
    while (i < lst.length) {
      val st = lst.slice(i, i + step) map {
        ug: UserBehavior =>
          {
            //            val mug = Map(("date" -> ug.date), ("ctr") -> ug.ctr)
            ug.uid -> ug.json
          }
      }

      try {
        conn.withClient {
          client =>
            {
              val ret = client.hmset(hash, st)
              logger.info(idx + "," + hash + "," + i + "," + ret)
            }
        }
      } catch {
        case e: Exception => logger.error(idx + "," + hash + "," + i + "," + e)
      }
      i += step
      Thread.sleep(interval)
    }
  }

  def writeSingleFile(fs: FileSystem, filePath: String, interval: Int, conns: Array[RedisClientPool]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    logger.info(filePath + "," + timestamp)

    val tasks = read(fs, filePath).toArray.groupBy { case UserBehavior(uid, _) => Util.getIdx(uid, "", conns.length) }
      .map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }
    tasks foreach { _.join }
  }

  def main(args: Array[String]): Unit = {
    val confPath = args(0)
    val filePath = args(1)

    val workerIdx = args(2).toInt
    val workerNum = args(3).toInt
    val interval = args(4).toInt //milliseconds
    val addrs = args(5)

    val conns = Util.makePooledConns(addrs)
    //    assert(conns.length == 6)

    val fs = getFs(confPath)
    val path = new Path(filePath)

    val status = fs.listStatus(path)

    status.map { entry => entry.getPath.toString }
      .filter { entry => Util.getIdx(entry, "usrgmp", workerNum) == workerIdx }.foreach {
        f => writeSingleFile(fs, f, interval, conns)
      }
  }
}
