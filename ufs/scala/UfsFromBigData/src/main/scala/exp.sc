package bdu
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature

import collection.JavaConversions._
import collection.JavaConverters._
import collection.mutable.HashMap

object exp {
  def parseSignalTags(json: String): Map[String, Map[String, Double]] = {
    val obj = JSON.parseObject(json)
    (obj.keySet() map {
      ver =>
        {
          val vtags = obj.getJSONObject(ver)
          val tm = vtags.keySet() map {
            word: String =>
              {
                (word, vtags.getJSONObject(word).getDouble("weight").toDouble)
              }
          }

          (ver, tm.toMap)
        }
    }).toMap
  }                                               //> parseSignalTags: (json: String)Map[String,Map[String,Double]]
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  /*
  val conf = new SparkConf().setAppName("ufs from big data")
  val sc = new SparkContext(conf)
  val lines = sc.parallelize(List("hello, world"))
  lines
  */
  val json = """{"app":"coolpad","app_lan":"zh_cn","app_ver":"9.02.010_VER_2014.08.16_17:32:42","article_click_extra":{"content_id":"1153202981359580906","content_type":"advertisement_third_party","cpack":{"strategy":"unknown"}},"event_id":"3","event_time":"1482767934","gate_ip":"192.168.1.28","language":"zh_cn","log_time":"2016-12-26 23:58:54","log_type":"click","model":"Coolpad 8675","network":"","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"865267020889985","upack":{"ad_configid":"100001","biz_configid":"b9","news_configid":"302"}}"""
                                                  //> json  : String = {"app":"coolpad","app_lan":"zh_cn","app_ver":"9.02.010_VER
                                                  //| _2014.08.16_17:32:42","article_click_extra":{"content_id":"1153202981359580
                                                  //| 906","content_type":"advertisement_third_party","cpack":{"strategy":"unknow
                                                  //| n"}},"event_id":"3","event_time":"1482767934","gate_ip":"192.168.1.28","lan
                                                  //| guage":"zh_cn","log_time":"2016-12-26 23:58:54","log_type":"click","model":
                                                  //| "Coolpad 8675","network":"","product_id":"coolpad","promotion":"coolpad","p
                                                  //| rotocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1"
                                                  //| ,"position_type":"0"},"uid":"865267020889985","upack":{"ad_configid":"10000
                                                  //| 1","biz_configid":"b9","news_configid":"302"}}

  val obj = JSON.parseObject(json)                //> obj  : com.alibaba.fastjson.JSONObject = {"app":"coolpad","gate_ip":"192.16
                                                  //| 8.1.28","app_lan":"zh_cn","language":"zh_cn","upack":{"ad_configid":"100001
                                                  //| ","biz_configid":"b9","news_configid":"302"},"log_time":"2016-12-26 23:58:5
                                                  //| 4","network":"","uid":"865267020889985","log_type":"click","protocol":"http
                                                  //| ","event_id":"3","scenario":{"channel":"0","position":"1","position_type":"
                                                  //| 0","desc":"waterfall"},"app_ver":"9.02.010_VER_2014.08.16_17:32:42","produc
                                                  //| t_id":"coolpad","model":"Coolpad 8675","article_click_extra":{"content_type
                                                  //| ":"advertisement_third_party","content_id":"1153202981359580906","cpack":{"
                                                  //| strategy":"unknown"}},"event_time":"1482767934","promotion":"coolpad"}

  obj.get("app")                                  //> res0: Object = coolpad
  val x = obj.getJSONObject("article_click_extra")//> x  : com.alibaba.fastjson.JSONObject = {"content_type":"advertisement_third
                                                  //| _party","content_id":"1153202981359580906","cpack":{"strategy":"unknown"}}
  x.getString("content_id")                       //> res1: String = 1153202981359580906
  (0 to 100).grouped(13)                          //> res2: Iterator[scala.collection.immutable.IndexedSeq[Int]] = non-empty iter
                                                  //| ator
  classOf[List[Int]]                              //> res3: Class[List[Int]](classOf[scala.collection.immutable.List]) = class sc
                                                  //| ala.collection.immutable.List
  "fsdfs".getClass                                //> res4: Class[?0] = class java.lang.String
  "123".toDouble                                  //> res5: Double = 123.0

  val tags = """{"v8":{"眼霜":{"weight":0.108},"苹果肌":{"weight":0.108},"爽肤水":{"weight":0.105},"抬头纹":{"weight":0.102},"眼睑":{"weight":0.101},"眼角":{"weight":0.099},"眼睛":{"weight":0.095},"鱼尾纹":{"weight":0.095},"保湿":{"weight":0.093},"刘海":{"weight":0.093}},"v11":{"眼霜":{"weight":1.083},"苹果肌":{"weight":1.082},"爽肤水":{"weight":1.047},"抬头纹":{"weight":1.021},"眼睑":{"weight":1.013},"眼角":{"weight":0.995},"眼睛":{"weight":0.95},"鱼尾纹":{"weight":0.949},"保湿":{"weight":0.933},"刘海":{"weight":0.928}},"v24":{"男神":{"weight":1},"眼霜":{"weight":0.7542855472335797},"卸妆":{"weight":0.6586380527290895},"鱼尾纹":{"weight":0.4957132054691099},"爽肤水":{"weight":0.45082924070126706},"按摩":{"weight":0.3845784612447207},"无名指":{"weight":0.3840672097745096},"眼睑":{"weight":0.38196436731923405},"眼角":{"weight":0.28720858553933104},"精华":{"weight":0.2129500815178438},"运气":{"weight":0.180152048963966},"李东旭":{"weight":0.17853501032927968},"抬头纹":{"weight":0.16486908381623275},"孔侑":{"weight":0.15120466901396312},"肌肉":{"weight":0.14279890379907256},"细胞再生":{"weight":0.13556855209749444},"苹果肌":{"weight":0.12072307899899073},"李敏镐":{"weight":0.1012961011973007},"单眼皮":{"weight":0.096638698647289},"跑男":{"weight":0.09453441736486434},"米粒":{"weight":0.0771972233461235},"女大学生":{"weight":0.056278008741082054},"洁面":{"weight":0.04270110253202174},"人鱼":{"weight":0.04131863501965953},"奶酪":{"weight":0.037490195865187835},"补水":{"weight":0.015289347911287011},"穴位":{"weight":0.00971502884617914},"刘海":{"weight":0.00940237074696156},"保湿":{"weight":0.0012640611361128455},"中指":{"weight":0}},"v1":{"男神":{"weight":5},"卸妆":{"weight":3},"按摩":{"weight":3},"眼霜":{"weight":3},"运气":{"weight":2},"眼角":{"weight":2},"眼睑":{"weight":2},"精华":{"weight":2},"鱼尾纹":{"weight":2},"肌肉":{"weight":2},"爽肤水":{"weight":2},"无名指":{"weight":2},"单眼皮":{"weight":1},"心爱":{"weight":1},"细胞再生":{"weight":1},"平凡":{"weight":1},"弹性":{"weight":1},"人鱼":{"weight":1},"保湿":{"weight":1},"抬头纹":{"weight":1},"洁面":{"weight":1},"女大学生":{"weight":1},"苹果肌":{"weight":1},"眼睛":{"weight":1},"李东旭":{"weight":1},"刘海":{"weight":1},"穴位":{"weight":1},"跑男":{"weight":1},"米粒":{"weight":1},"中指":{"weight":1}},"v18":{"227_护理（主要面部）":{"weight":2.877274},"211_化妆":{"weight":1.353674},"9_影视（剧、明星、较杂）":{"weight":0.801264},"57_情感（男女）":{"weight":0.643597},"188_洗洁":{"weight":0.590755},"30_人物风姿":{"weight":0.403214},"269_健身（动作、身体部位）":{"weight":0.330221}}}"""
                                                  //> tags  : String = {"v8":{"眼霜":{"weight":0.108},"苹果肌":{"weight":0.1
                                                  //| 08},"爽肤水":{"weight":0.105},"抬头纹":{"weight":0.102},"眼睑":{"we
                                                  //| ight":0.101},"眼角":{"weight":0.099},"眼睛":{"weight":0.095},"鱼尾纹
                                                  //| ":{"weight":0.095},"保湿":{"weight":0.093},"刘海":{"weight":0.093}},"v1
                                                  //| 1":{"眼霜":{"weight":1.083},"苹果肌":{"weight":1.082},"爽肤水":{"we
                                                  //| ight":1.047},"抬头纹":{"weight":1.021},"眼睑":{"weight":1.013},"眼角
                                                  //| ":{"weight":0.995},"眼睛":{"weight":0.95},"鱼尾纹":{"weight":0.949},"�
                                                  //| ��湿":{"weight":0.933},"刘海":{"weight":0.928}},"v24":{"男神":{"weight
                                                  //| ":1},"眼霜":{"weight":0.7542855472335797},"卸妆":{"weight":0.6586380527
                                                  //| 290895},"鱼尾纹":{"weight":0.4957132054691099},"爽肤水":{"weight":0.4
                                                  //| 5082924070126706},"按摩":{"weight":0.3845784612447207},"无名指":{"weig
                                                  //| ht":0.3840672097745096},"眼睑":{"weight":0.38196436731923405},"眼角":{"
                                                  //| weight":0.28720858553933104},"精华":{"weight":0.2129500815178438},"运气
                                                  //| ":{"weight":0.180152048963966},"李东旭":{"weight":0.17853501032927968},"
                                                  //| 抬头纹":{"weig
                                                  //| Output exceeds cutoff limit.

  // val vs = JSON.parseObject(tags, classOf[Map[String, Map[String, Map[String, Double]]]])
  val vs = JSON.parseObject(tags)                 //> vs  : com.alibaba.fastjson.JSONObject = {"v8":{"鱼尾纹":{"weight":0.095}
                                                  //| ,"苹果肌":{"weight":0.108},"爽肤水":{"weight":0.105},"眼睛":{"weigh
                                                  //| t":0.095},"刘海":{"weight":0.093},"眼角":{"weight":0.099},"保湿":{"we
                                                  //| ight":0.093},"眼睑":{"weight":0.101},"抬头纹":{"weight":0.102},"眼霜
                                                  //| ":{"weight":0.108}},"v11":{"鱼尾纹":{"weight":0.949},"苹果肌":{"weigh
                                                  //| t":1.082},"爽肤水":{"weight":1.047},"眼睛":{"weight":0.95},"刘海":{"
                                                  //| weight":0.928},"眼角":{"weight":0.995},"保湿":{"weight":0.933},"眼睑"
                                                  //| :{"weight":1.013},"抬头纹":{"weight":1.021},"眼霜":{"weight":1.083}},"
                                                  //| v24":{"单眼皮":{"weight":0.096638698647289},"卸妆":{"weight":0.6586380
                                                  //| 527290895},"细胞再生":{"weight":0.13556855209749444},"人鱼":{"weight"
                                                  //| :0.04131863501965953},"保湿":{"weight":0.0012640611361128455},"男神":{"
                                                  //| weight":1},"按摩":{"weight":0.3845784612447207},"抬头纹":{"weight":0.1
                                                  //| 6486908381623275},"洁面":{"weight":0.04270110253202174},"女大学生":{"
                                                  //| weight":0.056278008741082054},"苹果肌":{"weight":0.12072307899899073},"�
                                                  //| ��东旭":{"weight"
                                                  //| Output exceeds cutoff limit.

  // vs.getJSONObject("v1")
  vs.entrySet foreach {
    x =>
      {
        println(x.getKey)
      }                                           //> v8
                                                  //| v11
                                                  //| v24
                                                  //| v1
                                                  //| v18
  }

  val kk = parseSignalTags(tags)                  //> kk  : Map[String,Map[String,Double]] = Map(v24 -> Map(男神 -> 1.0, 抬头
                                                  //| 纹 -> 0.16486908381623275, 米粒 -> 0.0771972233461235, 无名指 -> 0.38
                                                  //| 40672097745096, 李敏镐 -> 0.1012961011973007, 洁面 -> 0.04270110253202
                                                  //| 174, 眼睑 -> 0.38196436731923405, 人鱼 -> 0.04131863501965953, 爽肤�
                                                  //| � -> 0.45082924070126706, 鱼尾纹 -> 0.4957132054691099, 补水 -> 0.0152
                                                  //| 89347911287011, 苹果肌 -> 0.12072307899899073, 孔侑 -> 0.1512046690139
                                                  //| 6312, 女大学生 -> 0.056278008741082054, 眼霜 -> 0.7542855472335797, �
                                                  //| ��海 -> 0.00940237074696156, 细胞再生 -> 0.13556855209749444, 肌肉 -
                                                  //| > 0.14279890379907256, 保湿 -> 0.0012640611361128455, 眼角 -> 0.2872085
                                                  //| 8553933104, 李东旭 -> 0.17853501032927968, 中指 -> 0.0, 精华 -> 0.21
                                                  //| 29500815178438, 运气 -> 0.180152048963966, 跑男 -> 0.09453441736486434,
                                                  //|  奶酪 -> 0.037490195865187835, 单眼皮 -> 0.096638698647289, 穴位 -> 
                                                  //| 0.00971502884617914, 按摩 -> 0.3845784612447207, 卸妆 -> 0.658638052729
                                                  //| 0895), v8 -> Map(抬头纹 -> 0.102, 眼睑 -> 0.101, 爽肤水 -> 0.105, �
                                                  //| ��尾纹 -> 0.095, 眼睛 
                                                  //| Output exceeds cutoff limit.

  val res = JSON.toJSONString(kk.map{case (k, v) => k->v.asJava}.asJava, false)
                                                  //> res  : String = {"v24":{"男神":1.0,"抬头纹":0.16486908381623275,"米�
                                                  //| �":0.0771972233461235,"无名指":0.3840672097745096,"李敏镐":0.10129610
                                                  //| 11973007,"洁面":0.04270110253202174,"眼睑":0.38196436731923405,"人鱼"
                                                  //| :0.04131863501965953,"爽肤水":0.45082924070126706,"鱼尾纹":0.49571320
                                                  //| 54691099,"补水":0.015289347911287011,"苹果肌":0.12072307899899073,"孔
                                                  //| 侑":0.15120466901396312,"女大学生":0.056278008741082054,"眼霜":0.754
                                                  //| 2855472335797,"刘海":0.00940237074696156,"细胞再生":0.135568552097494
                                                  //| 44,"肌肉":0.14279890379907256,"保湿":0.0012640611361128455,"眼角":0.2
                                                  //| 8720858553933104,"李东旭":0.17853501032927968,"中指":0.0,"精华":0.21
                                                  //| 29500815178438,"运气":0.180152048963966,"跑男":0.09453441736486434,"奶
                                                  //| 酪":0.037490195865187835,"单眼皮":0.096638698647289,"穴位":0.00971502
                                                  //| 884617914,"按摩":0.3845784612447207,"卸妆":0.6586380527290895},"v8":{"�
                                                  //| ��头纹":0.102,"眼睑":0.101,"爽肤水":0.105,"鱼尾纹":0.095,"眼睛"
                                                  //| :0.095,"苹果肌":0.108,"眼霜":0.108,"刘海":0.093,"保湿":0.093,"眼�
                                                  //| ��":0.099},"v1":{"男神":5.0,"抬头纹":1.0,"米粒":1.0,"无名
                                                  //| Output exceeds cutoff limit.

  var map = new HashMap[String, Any]()            //> map  : scala.collection.mutable.HashMap[String,Any] = Map()
  map.put("id", 123)                              //> res6: Option[Any] = None
  map.put("name", "张三")                           //> res7: Option[Any] = None
  map                                             //> res8: scala.collection.mutable.HashMap[String,Any] = Map(name -> 张三, id
                                                  //|  -> 123)

  val text = JSON.toJSONString(map.asJava, true)  //> text  : String = {
                                                  //| 	"name":"张三",
                                                  //| 	"id":123
                                                  //| }
  
  //foreach {
  //  case (k, v) => {
  //    println(k)
  //    v foreach {
  //      case (a, b) => println(a, b.toString.substring(0, Integer.min(5, b.toString.length)))
  //    }
  //  }
  //}
}