package ufs
import scala.io.Source
import com.redis._
import java.io.File
import scala.sys.process.ProcessBuilder.Source

object LocalBatchSimilarUser {
  case class SuRecord(uid: String, value: String)

  //  def getFs(confPath: String): FileSystem = {
  //    val conf = new Configuration()
  //    val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
  //    val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")
  //
  //    conf.addResource(hdfsCoreSitePath)
  //    conf.addResource(hdfsHDFSSitePath)
  //    FileSystem.get(conf)
  //  }

  def read(filePath: String): Iterator[SuRecord] = {

    //    val factory = new CompressionCodecFactory(new Configuration)
    //    val path = new Path(filePath)
    //    val codec = factory.getCodec(path)
    try {
      //      val file = codec.createInputStream(fs.open(path))
      //      val lines = Source.createBufferedSource(file).getLines()
      val lines = Source.fromFile(filePath).getLines()

      lines.filter(x => {val fields = x.split("[\t]+"); fields.length > 3 && fields(1).toLowerCase.contains("coolpad")}).map(x => {
        val fields = x.split("[\t]+")
        //      val prefix = """{"ts":1473249654,"app":"","weighted":"""
        //      val suffix = "}"
        new SuRecord(fields(2), fields(3))
      })
    } catch {
      case t: Throwable =>
        t.printStackTrace() // TODO: handle error
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

  def writeSingleFile(filePath: String, interval: Int, conns: Array[RedisClient]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    println(filePath, timestamp)
    //    val tasks = read(fs, filePath).toArray.groupBy { case SuRecord(uid, hash, _) => (Util.getIdx(uid, "", conns.length), hash) }
    //      .groupBy { case ((idx, _), _) => idx }.map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }

    val tasks = read(filePath).toArray.groupBy { case SuRecord(uid, _) => Util.getIdx(uid, "", conns.length) }
      .map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }
    tasks foreach { _.join }
  }

  def main(args: Array[String]): Unit = {
    //    val confPath = args(0)
    val filePath = args(0)

    val workerIdx = args(1).toInt
    val workerNum = args(2).toInt
    //    val threadNum = args(4).toInt
    val interval = args(3).toInt //milliseconds
    val addrs = args(4)

    val conns = Util.makeConns(addrs)
//    assert(conns.length == 6)

    //    val fs = getFs(confPath)
    //    val path = new Path(filePath)

    //    val status = fs.listStatus(path)
    val files = (new File(filePath)).listFiles().filter { _.isFile() }.toList

    files.map { entry => entry.getPath.toString }
      .filter { entry => Util.getIdx(entry, "importsimilaruser", workerNum) == workerIdx }.foreach {
        f => writeSingleFile(f, interval, conns)
      }
  }
}