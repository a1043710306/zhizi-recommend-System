package ufs

import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus }
import com.redis._
import java.util.concurrent._

object BatchZwUfs {

  case class UfsTag(uid: String, value: String)

  def getFs(confPath: String): FileSystem = {
    val conf = new Configuration()
    val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
    val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

    conf.addResource(hdfsCoreSitePath)
    conf.addResource(hdfsHDFSSitePath)
    FileSystem.get(conf)
  }

  def read(fs: FileSystem, filePath: String): Stream[Seq[UfsTag]] = {

    val path = new Path(filePath)
    val file = fs.open(path)
    val lines = Source.createBufferedSource(file).getLines()

    lines.filter(x => x.split("[\t]+").length == 2).map(x => {
      val fields = x.split("[\t]+")
      new UfsTag(fields(0), fields(1))
    }).grouped(150).toStream
  }

  def connWrite(hash: String, conn: RedisClientPool, mkt: List[UfsTag]): Boolean = {
    val kvs = mkt.map {
      x => x.uid -> x.value
    }.toMap

    conn withClient {
      x =>
        {
          val t = x.hmset(hash, kvs)
          println("write ret: " + t + " size: " + kvs.size)
          t
        }
    }
  }

  def writeSingleFile(hash: String, fs: FileSystem, filePath: String,
                      conns: Array[RedisClientPool], threadpool: ExecutorService) = {

    val res = read(fs, filePath).flatMap {
      _.groupBy {
        x => Util.getIdx(x.uid, "", conns.size)
      } map {
        case (idx, lst) => threadpool.submit(new Callable[Boolean] {
          override def call(): Boolean = {
            connWrite(hash, conns(idx), lst.toList)
          }
        })
      }
    } map {
      _.get
    } groupBy {
      x => x
    }
  }

  def main(args: Array[String]): Unit = {
    val confPath = args(0)
    val filePath = args(1)

    val threadNum = args(2).toInt
    val addrs = args(3)
    val hash = args(4)

    val executorService = Executors.newFixedThreadPool(threadNum)

    val conns = Util.makePooledConns(addrs)
//    assert(conns.length == 6)

    val fs = getFs(confPath)
    val path = new Path(filePath)

    val status = fs.listStatus(path)

    status.map {
      entry => entry.getPath.toString
    } foreach {
      f => writeSingleFile(hash, fs, f, conns, executorService)
    }

    executorService.shutdown()
  }
}

// nohup java -cp feature-assembly-1.0.jar feature.BatchUfs /etc/hadoop/conf /user/hive/warehouse/db_nlp.db/m_user_all_tags_ufs_format_d/statdate=20160906 0 2 6 5 "192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1" > batch-ufs.log 2>&1 &

// nohup java -cp feature-assembly-1.0.jar feature.BatchUfs /etc/hadoop/conf /user/hive/warehouse/db_nlp.db/m_user_all_tags_ufs_format_d/statdate=20160906 1 2 6 5 "192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1" > batch-ufs.log 2>&1 &
