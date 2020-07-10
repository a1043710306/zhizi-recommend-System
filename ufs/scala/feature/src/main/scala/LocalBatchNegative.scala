package ufs
import scala.io.Source
import com.redis._
import java.io.File
import scala.sys.process.ProcessBuilder.Source

object LocalBatchNegative {

  case class NegTag(uid: String, hash: String, value: String)

  def read(filePath: String): Iterator[NegTag] = {
    try {
      val hashPrefix = "zhizi.negative.impression."
      val prefix = """{"ts":1479355881,"app":"","weighted":"""
      val suffix = "}"
      val lines = Source.fromFile(filePath).getLines()

      lines.filter { x =>
        {
          val fields = x.split("[\t]+")
//          println(fields.length)
          if (fields.length != 2) {
            false
          } else {
            val pfs = fields(0).split("[|]+");
//            println(pfs.length, pfs(0), pfs(1))
            pfs.length == 4
          }
        }
      }.map { x =>
        {
          val fields = x.split("[\t]+")
          val json = prefix + fields(1) + suffix
          val pfs = fields(0).split("[|]+");
          val uid = pfs(0)
          val hash = hashPrefix + pfs(3)
          new NegTag(uid, hash, json)
        }
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace() // TODO: handle error
        Iterator[NegTag]()
    }

  }

  def connWrite(idx: Int, conn: RedisClient, interval: Int, mkt: Map[(Int, String), Array[NegTag]]) = {
    // mkt.foreach{}
    mkt.foreach {
      case ((_, hash), lst) => {
        var i = 0
        val step = 50
        while (i < lst.length) {
          val st = lst.slice(i, i + step).map { case NegTag(uid, _, value) => uid -> value }.toMap
          try {
            val ret = conn.hmset(hash, st)
            println(idx, hash, i, ret)
          } catch {
            case e: Exception => println(idx, hash, i, e)
          }
          i += step
          Thread.sleep(interval)
        }
      }
    }
  }


  def writeSingleFile(filePath: String, interval: Int, conns: Array[RedisClient]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    println(filePath, timestamp)
    val tasks = read(filePath).toArray.groupBy { case NegTag(uid, hash, _) => (Util.getIdx(uid, "", conns.length), hash) }
      .groupBy { case ((idx, _), _) => idx }.map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }
    tasks foreach { _.join }
  }

  def main(args: Array[String]): Unit = {
    val filePath = args(0)

    val workerIdx = args(1).toInt
    val workerNum = args(2).toInt
    //    val threadNum = args(4).toInt
    val interval = args(3).toInt //milliseconds
    val addrs = args(4)

    val conns = Util.makeConns(addrs)
    val files = (new File(filePath)).listFiles().filter { _.isFile() }.toList

    files.map { entry => entry.getPath.toString }
      .filter { entry => Util.getIdx(entry, "importnegative", workerNum) == workerIdx }.foreach {
        f => writeSingleFile(f, interval, conns)
      }
  }
}