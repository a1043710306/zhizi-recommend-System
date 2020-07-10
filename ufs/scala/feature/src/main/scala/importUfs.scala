package ufs

import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus }
import com.redis._

object BatchUfs {

  case class UfsTag(uid: String, hash: String, value: String)

  def getFs(confPath: String): FileSystem = {
    val conf = new Configuration()
    val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
    val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

    conf.addResource(hdfsCoreSitePath)
    conf.addResource(hdfsHDFSSitePath)
    FileSystem.get(conf)
  }

  def read(fs: FileSystem, filePath: String): Iterator[UfsTag] = {
    // val conf = new Configuration()
    // val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
    // val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

    // conf.addResource(hdfsCoreSitePath)
    // conf.addResource(hdfsHDFSSitePath)
    // val fileSystem = FileSystem.get(conf)
    // val fileSystem = getFs(

    val path = new Path(filePath)
    val file = fs.open(path)
    val lines = Source.createBufferedSource(file).getLines()

    val hashPrefix = "zhizi.ufs.tag."
    lines.filter(x => x.split("[\t]+").length == 3).map(x => {
      val fields = x.split("[\t]+")
      val prefix = """{"ts":1473249654,"app":"","weighted":"""
      val suffix = "}"
      new UfsTag(fields(0), hashPrefix + fields(1), prefix + fields(2) + suffix)
    })
  }

  def connWrite(idx: Int, conn: RedisClient, interval: Int, mkt: Map[(Int, String), Array[UfsTag]]) = {
    // mkt.foreach{}
    mkt.foreach {
      case ((_, hash), lst) => {
        var i = 0
        val step = 50
        while (i < lst.length) {
          val st = lst.slice(i, i + step).map { case UfsTag(uid, _, value) => uid -> value }.toMap
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
      }
    }
  }

  def writeSingleFile(fs: FileSystem, filePath: String, interval: Int, conns: Array[RedisClient]) = {
    val timestamp: Long = System.currentTimeMillis / 1000
    println(filePath, timestamp)
    val tasks = read(fs, filePath).toArray.groupBy { case UfsTag(uid, hash, _) => (Util.getIdx(uid, "", conns.length), hash) }
      .groupBy { case ((idx, _), _) => idx }.map { case (idx, mkt) => Util.task(connWrite(idx, conns(idx), interval, mkt)) }
    tasks foreach { _.join }
  }

  def main(args: Array[String]): Unit = {
    val confPath = args(0)
    val filePath = args(1)

    val workerIdx = args(2).toInt
    val workerNum = args(3).toInt
    val threadNum = args(4).toInt
    val interval = args(5).toInt //milliseconds
    val addrs = args(6)

    val conns = Util.makeConns(addrs)
    assert(conns.length == 6)

    val fs = getFs(confPath)
    val path = new Path(filePath)

    val status = fs.listStatus(path)

    // (status map {item => item.getPath.toString}).foreach(println)
    status.map { entry => entry.getPath.toString }
      .filter { entry => Util.getIdx(entry, "distbwtproc", workerNum) == workerIdx }.foreach {
        f => writeSingleFile(fs, f, interval, conns)
      }
    // .groupBy{entry => Util.getIdx(entry, "distbwtthread", threadNum)}.foreach{
    //     case (idx, lst) => {println(idx); lst foreach println}
    // }
  }
}

// nohup java -cp feature-assembly-1.0.jar feature.BatchUfs /etc/hadoop/conf /user/hive/warehouse/db_nlp.db/m_user_all_tags_ufs_format_d/statdate=20160906 0 2 6 5 "192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1" > batch-ufs.log 2>&1 &

// nohup java -cp feature-assembly-1.0.jar feature.BatchUfs /etc/hadoop/conf /user/hive/warehouse/db_nlp.db/m_user_all_tags_ufs_format_d/statdate=20160906 1 2 6 5 "192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1" > batch-ufs.log 2>&1 &
