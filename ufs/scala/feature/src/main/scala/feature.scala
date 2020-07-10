package ufs

import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

case class Record(uid:String, hourOrNet:String, impression:String, click:String, activeDays:String)

object Hdfs {
    def read(confPath: String, filePath: String): Iterator[Record] = {
        val conf = new Configuration()
        val hdfsCoreSitePath = new Path(confPath + "/core-site.xml")
        val hdfsHDFSSitePath = new Path(confPath + "/hdfs-site.xml")

        conf.addResource(hdfsCoreSitePath)
        conf.addResource(hdfsHDFSSitePath)
        val fileSystem = FileSystem.get(conf)

        val path = new Path(filePath)
        val file = fileSystem.open(path)
        val lines = Source.createBufferedSource(file).getLines()

        lines.filter(x => x.split("[\t]+").length == 6).map(x => {
            val fields = x.split("[\t]+")
            new Record(fields(0), fields(1), fields(2), fields(3), fields(5))
        }
        )
    }
    
    def makeJson(records: Iterator[Record], numOfClient: Int): Map[Int, Map[String, String]] = {
        import org.json4s._
        import org.json4s.native.JsonMethods._
        import org.json4s.JsonDSL.WithBigDecimal._
        // import org.json4s.native.Serialization.{read, write}
        implicit val formats = DefaultFormats

        val timestamp: Long = System.currentTimeMillis / 1000
        def makeUserJson(recordList: List[Record]): String = {
            val tm = recordList.map{
                case Record(_, nw, impl, cli, act) => (nw, Map[String, String]("impl"->impl, "cli"->cli, "act"->act))
            }.toMap
            compact(render(("ts" -> timestamp)~("data" -> tm)))
        }

        records.toList.groupBy{
            case Record(uid, _, _, _, _) => uid
        }.groupBy{
            case (uid, _) => Util.getIdx(uid, "", numOfClient)
        }.map{
            case (idx, userList) => (idx, userList.map{case (uid, recordList) =>(uid, makeUserJson(recordList))})
        }
    }

    def main(args: Array[String]): Unit = {
        val confPath = args(0)
        val filePath = args(1)
        val addrs = args(2)

        val hashKey = if (filePath contains "user-network-feature") "zhizi.ufs.feature.network" else "zhizi.ufs.feature.hour"
        val conns = Util.makeConns(addrs)
        val res = makeJson(read(confPath, filePath), 6)
        for (i <- 0 until 6) {
            val thread = new Thread {
                val conn = conns(i)
                override def run {
                    var count = 0
                    for ((uid, userJson) <- res(i)) {
                        // println(i, uid, userJson)
                        conn.hset(hashKey, uid, userJson)
                        count += 1
                        if (count % 1000 == 0) println(i, count, uid)
                    }
                }
            }
            thread.start
        }
    }
}
