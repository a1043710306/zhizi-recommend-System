package ufs
import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus }
import org.apache.hadoop.io.compress.CompressionCodec
import org.apache.hadoop.io.compress.CompressionCodecFactory
import com.redis._

object BatchSimilarUser {
  case class SuRecord(uid: String, value: String)

  def getFs(confPath: String): FileSystem = {
    val conf = new Configuration()
    val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
    val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

    conf.addResource(hdfsCoreSitePath)
    conf.addResource(hdfsHDFSSitePath)
    FileSystem.get(conf)
  }

  def read(fs: FileSystem, filePath: String): Iterator[SuRecord] = {

    val factory = new CompressionCodecFactory(new Configuration)
    val path = new Path(filePath)
    val codec = factory.getCodec(path)
    try {
      val file = codec.createInputStream(fs.open(path))
      val lines = Source.createBufferedSource(file).getLines()

      lines.filter(x => x.split("[\t]+").length > 3).map(x => {
        val fields = x.split("[\t]+")
        //      val prefix = """{"ts":1473249654,"app":"","weighted":"""
        //      val suffix = "}"
        new SuRecord(fields(2), fields(3))
      })
    } catch {
      case t: Throwable => t.printStackTrace() // TODO: handle error
      Iterator[SuRecord]()
    }

  }

  def connWrite(idx: Int, conn: RedisClient, interval: Int, mkt: Array[SuRecord]) = {
    val hash = "zhizi.cf.similar_user"
    val lst = mkt
    var i = 0
    val step = 50
    while (i < lst.length) {
      val st = lst.slice(i, i + step).map { case SuRecord(uid, value) => uid -> value }.toMap
      try {
        val ret = conn.hmset(hash, st)
        println(idx, hash, i, ret)
      } catch {
        case e: Exception => println(idx, hash, i, e)
      }
      i += step
      // println(hash, st)
      Thread.sleep(interval)
    }
    //      }
    //    }
  }

  def writeSingleFile(fs: FileSystem, filePath: String, interval: Int, conns: Array[RedisClient]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    println(filePath, timestamp)
    //    val tasks = read(fs, filePath).toArray.groupBy { case SuRecord(uid, hash, _) => (Util.getIdx(uid, "", conns.length), hash) }
    //      .groupBy { case ((idx, _), _) => idx }.map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }

    val tasks = read(fs, filePath).toArray.groupBy { case SuRecord(uid, _) => Util.getIdx(uid, "", conns.length) }
      .map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }
    tasks foreach { _.join }
  }

  def main(args: Array[String]): Unit = {
    val confPath = args(0)
    val filePath = args(1)

    val workerIdx = args(2).toInt
    val workerNum = args(3).toInt
//    val threadNum = args(4).toInt
    val interval = args(4).toInt //milliseconds
    val addrs = args(5)

    val conns = Util.makeConns(addrs)
    assert(conns.length == 6)

    val fs = getFs(confPath)
    val path = new Path(filePath)

    val status = fs.listStatus(path)

    status.map { entry => entry.getPath.toString }
      .filter { entry => Util.getIdx(entry, "importsimilaruser", workerNum) == workerIdx }.foreach {
        f => writeSingleFile(fs, f, interval, conns)
      }
  }
}
