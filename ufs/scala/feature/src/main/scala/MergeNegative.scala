package ufs

import java.text.SimpleDateFormat
import java.text.ParseException
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.Serialization.{read, write}

import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

import scala.collection.JavaConversions._
import org.omg.PortableInterceptor.NON_EXISTENT

object MergeNegative {
	implicit val formats = DefaultFormats

case class Extra(content_id: String, content_type: String)
case class Upack(news_configid: String)

case class ICEvent(uid: String, infoId: String, app: String, configId: String, eventTime: String, logTime: String, sign: String);
case class IEvent(product_id: String, event_time:String, log_time: String, uid: String, article_impression_extra: Extra, upack: Upack)
case class CEvent(product_id: String, event_time:String, log_time: String, uid: String, article_click_extra: Extra, upack: Upack)
case class NegativeRes(uid: String, json: String)




def getProducer(brokers: String): KafkaProducer[String, String] = {
	val props = new Properties()
	props.put("bootstrap.servers", brokers)
	props.put("acks", "all")
	props.put("retries", "0")
	props.put("batch.size", "16384")
	props.put("linger.ms", "1")
	props.put("buffer.memory", "33554432")
	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

	// Producer<String, String> producer = new KafkaProducer<>(props)
	new KafkaProducer[String, String](props)
}

def getComsumer(brokers: String, group: String): KafkaConsumer[String, String] = {
	val props = new Properties()
	props.put("bootstrap.servers", brokers)
	props.put("group.id", group)
	props.put("enable.auto.commit", "true")
	props.put("auto.commit.interval.ms", "1000")
	props.put("auto.offset.reset", "earliest")
	props.put("session.timeout.ms", "30000")
	props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
	props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
	// KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)
	new KafkaConsumer[String, String](props)
}

def extractEventTime(str: String): Int = {
	var eventTime = str.toLong
			if (str.length > 10) {
				eventTime = eventTime / 1000
			}
	eventTime.toInt
}

def extractLogTime(str: String): Int = {
	val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	try {
		val date = df.parse(str);
		(date.getTime() / 1000).toInt;
	} catch {
	case t: Throwable => (System.currentTimeMillis() / 1000).toInt
	}
}

def parseImpression(json: String) :Option[ICEvent] = {
	//{"app":"coolpad","app_lan":"zh_cn","app_ver":"other","article_impression_extra":{"content_id":"48621742","content_type":"news","cpack":{"strategy":"recommendation"},"server_time":"2016-10-26 19:42:03"},"event_id":"2","event_time":"1477482123","gate_ip":"192.168.1.28","language":"zh_cn","log_time":"2016-10-26 19:42:03","log_type":"request","mcc":"","model":"Coolpad+7295C","network":"wifi","osv":"","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"862073022342129","upack":{"abtest_ver":"205","ad_configid":"1","biz_configid":"b1","news_configid":"205"}}

	try {
		val res = read[IEvent](json)
				//		  println(res)
				//				  if ((res.article_impression_extra contains "content_id") && (res.upack contains "news_configid")) {
				//					  Some(new ICEvent(res.uid, res.article_impression_extra("content_id"), 
				//							  res.product_id, res.upack("news_configid"), extractEventTime(res.event_time).toString(),
				//							  extractLogTime(res.log_time).toString(), "1"))
				//				  } else {
				//					  None
				//				  }
				if (res.article_impression_extra.content_type == "news") {
					Some(new ICEvent(res.uid, res.article_impression_extra.content_id, 
							res.product_id, res.upack.news_configid, extractEventTime(res.event_time).toString(),
							extractLogTime(res.log_time).toString(), "1"))
				} else {
					None
				}
	} catch {
	case t: Throwable => None // TODO: handle error
	}

}

def parseClick(json: String) :Option[ICEvent] = {
	// {"app":"emui","app_lan":"zh_cn","app_ver":"unknown","article_click_extra":{"content_id":"48651885","content_type":"news","cpack":{"strategy":"recommendation"}},"event_id":"3","event_time":"1477481781","gate_ip":"192.168.1.102","language":"zh_cn","log_time":"2016-10-26 19:36:21","log_type":"uad","product_id":"emui","promotion":"emui","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"01011509102153001201000004395105","upack":{"abtest_ver":"166","ad_configid":"a1","biz_configid":"b4","news_configid":"166"}}

	try {
		val res = read[CEvent](json)
				//		  println(res)
				//				  if ((res.article_click_extra contains "content_id") && (res.upack contains "news_configid")) {
				//					  Some(new ICEvent(res.uid, res.article_click_extra("content_id"), 
				//							  res.product_id, res.upack("news_configid"), extractEventTime(res.event_time).toString(),
				//							  extractLogTime(res.log_time).toString(), "-1"))
				//				  } else {
				//					  None
				//				  }
				if (res.article_click_extra.content_type == "news") {
					Some(new ICEvent(res.uid, res.article_click_extra.content_id, 
							res.product_id, res.upack.news_configid, extractEventTime(res.event_time).toString(),
							extractLogTime(res.log_time).toString(), "-1"))
				} else {
					None
				}
	} catch {
	case t: Throwable => None // TODO: handle error
	}
}

def makeJson(ev: ICEvent): String = {
	//""//compact(render(ev))
	//  case class ICEvent(uid: String, infoId: String, app: String, configId: String, eventTime: String, logTime: String, sign: String);

	val json = ("uid" -> ev.uid)~("infoId" -> ev.infoId)~("app"->ev.app)~("configId"->ev.configId)~("eventTime"->ev.eventTime)~("logTime"->ev.logTime)~("sign"->ev.sign)
			compact(render(json))
}

def makeNegativeMsg(role: String, json: String): Option[NegativeRes] = {
	if (role == "click") {
		parseClick(json) match {
		case Some(event) => Some(new NegativeRes(event.uid, makeJson(event)))
		case _ => None
		}
	} else if (role == "impression") {
		parseImpression(json) match {
		case Some(event) => Some(new NegativeRes(event.uid, makeJson(event)))
		case _ => None
		}
	} else {
		None
	}
}

def main(args: Array[String]): Unit = {
	//    val j = """{"aid":"f8da906fffd497cc","api_ver":"2.0.0","app_lan":"spanish","app_ver":"1.0.6.0.0.3","article_impression_extra":{"content_id":"1027473270","content_type":"news","cpack":{"strategy":"fallback"},"server_time":"1480264575","view_mode":"1"},"brand":"Huawei","event_id":"2","event_time":"1480264577","gate_ip":"172.31.6.161","imei":"860715030050670","language":"es_ar","log_time":"2016-11-27 16:36:40","log_type":"report","mcc":"AR","mnc":"07","model":"ALE-L23","network":"4g","osv":"5.0.1","platform":"android","product_id":"noticiasboom","promotion":"gp","protocol":"https","report_time":"1480264598","scenario":{"channel":"0x05","channel_desc":"entertainment","desc":"long_listpage","position":"0x01","position_desc":"long_listpage","position_type":"0x01","position_type_desc":"long_listpage"},"seq":"154","sid":"622566338","tk":"78e137b33543b16676dc5ad29709bd95","uid":"01011611260412175301000163614202","upack":{"ad_configid":"12","biz_configid":"50","news_configid":"46"}}"""  
	////    parseImpression(j)
	//    println(makeNegativeMsg("impression", j))
	//    
	//    val k = """{"aid":"d7f850128a6be7f5","api_ver":"2.0.0","app_lan":"hindi","app_ver":"2.2.8.0.0.4","article_click_extra":{"click_type":"2","content_id":"1026921161","content_type":"news","cpack":{"strategy":"recommendation"},"view_mode":"1"},"brand":"OPPO","event_id":"3","event_time":"1480020673","gate_ip":"172.31.6.161","imei":"860370037381973","language":"or_in","log_time":"2016-11-24 20:51:14","log_type":"report","mcc":"IN","mnc":"864","model":"A37f","network":"4g","osv":"5.1.1","platform":"android","product_id":"hotoday","promotion":"gp","protocol":"https","report_time":"1480020673","scenario":{"channel":"0x00","channel_desc":"foryou","desc":"long_listpage","position":"0x01","position_desc":"long_listpage","position_type":"0x01","position_type_desc":"long_listpage"},"seq":"4","sid":"448691099","tk":"96bac555eb0864c23628a053e9bedb31","uid":"01011610281343314801000182052901","upack":{"ad_configid":"18","biz_configid":"10","news_configid":"108"}}"""
	//    parseClick(k)
	//    
	//    return

	val brokers = args(0)
			val upstream = args(1)
			val group = args(2)
			val downstream = args(3)
			val dnum = args(4).toInt

			val targetTopics = (0 until dnum).map{x => downstream+"-"+x.toString}

	val role = args(5)

			def getIndexByUid(uid: String): Int = {
		def getIdx(uid: String, denominator: Int): Int = {
				import java.security.MessageDigest
				import java.nio.ByteBuffer
				val md5 = MessageDigest.getInstance("MD5").digest((uid+"ufs.salt&*(").getBytes)
				// println(uid)
				val intIdx = ByteBuffer.wrap(md5.slice(0,4).reverse).getInt// % denominator
				val longIdx = intIdx & 0x00000000ffffffffL
				(longIdx % denominator.toLong).toInt
		}

		//            import org.json4s._
		//            import org.json4s.native.JsonMethods._
		//            import org.json4s.JsonDSL.WithBigDecimal._
		//            import org.json4s.native.Serialization.{read, write}
		//            implicit val formats = DefaultFormats
		//
		//            // val ce = read[ClickEvent](msg)
		getIdx(uid, dnum)
	}

	val consumer = getComsumer(brokers, group)
			consumer.subscribe(List(upstream))
			val producer = getProducer(brokers)
			while (true) {
				val records = consumer.poll(100)
						records foreach {record => {
							//                val idx = getIndexByMsg(record.value)
							// println("-----", idx, record.value)
							makeNegativeMsg(role, record.value) match {
							case Some(res) => producer.send(new ProducerRecord(targetTopics(getIndexByUid(res.uid)), res.json))
							case _ => //println(record.value)
							}
						}
				}
			}
}
}