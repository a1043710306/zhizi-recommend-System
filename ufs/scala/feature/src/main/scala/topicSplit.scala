package ufs

// import org.slf4j.LoggerFactory
// import com.typesafe.scalalogging.slf4j._
// 
// import akka.actor.ActorSystem
// import akka.stream.ActorMaterializer
// import akka.stream.ClosedShape
// import akka.stream.scaladsl.{Sink, Source, GraphDSL, RunnableGraph, Broadcast, Flow}
// import com.softwaremill.react.kafka.KafkaMessages._
// import org.apache.kafka.common.serialization.{StringSerializer, StringDeserializer}
// import com.softwaremill.react.kafka.{ProducerMessage, ConsumerProperties, ProducerProperties, ReactiveKafka}
// import org.reactivestreams.{Publisher, Subscriber }
// 
// import akka.stream.ActorAttributes
// import akka.stream.Supervision
// 
// // import akka.actor.SupervisorStrategy.Resume
// // import akka.stream.actor.{ActorSubscriber, WatermarkRequestStrategy, RequestStrategy}
// // import akka.actor.{Props, Actor, ActorLogging, OneForOneStrategy, SupervisorStrategy}
// // import com.softwaremill.react.kafka.{ ProducerProperties, ReactiveKafka }
// // import com.typesafe.config.Config
// // import kafka.serializer.StringEncoder
// 
// // import scala.collection.JavaConversions._
// 
// object TopicSplit {
//     implicit val actorSystem = ActorSystem("ReactiveKafka")
//     implicit val materializer = ActorMaterializer.create(actorSystem)
//     case class ClickEvent(uid: String)
// 
//     // val logger = Logger(LoggerFactory.getLogger(getClass))
//     // logger.info("hhhhhelllo")
// 
//     def main(args: Array[String]): Unit = {
//         val brokers = args(0)
//         val sourceTopic = args(1)
//         val consumerGroup = args(2)
//         val sinkTopic = args(3)
//         val numOfSinkTopic = args(4).toInt
//         assert(numOfSinkTopic > 0)
//         // val myIndex = args(5).toInt
//         // assert(myIndex >= 0 && myIndex < numOfSinkTopic)
// 
//         val kafka = new ReactiveKafka()
//         val publisher: Publisher[StringConsumerRecord] = kafka.consume(ConsumerProperties(
//             bootstrapServers = brokers,
//             topic = sourceTopic,
//             groupId = consumerGroup,
//             valueDeserializer = new StringDeserializer()
//         ))
//         val subscribers: Array[Subscriber[StringProducerMessage]] = {
//         // val subscribers  = {
//         //    (0 until numOfSinkTopic).map{i => Sink.actorSubscriber(Props(new ActorSupervisor(sinkTopic, brokers, i)))
//             (0 until numOfSinkTopic).map{i => 
//                 // kafka.publish(Props(new ActorSupervisor(sinkTopic, brokers, i)))
//                 kafka.publish(ProducerProperties(
//                 bootstrapServers = brokers,
//                 topic = sinkTopic + "-" + i.toString,
//                 valueSerializer = new StringSerializer())).withAttributes(ActorAttributes.supervisionStrategy(sinkDecider))
//             }.toArray
//         }
// 
//         def getIndexByMsg(msg: String): Int = {
//             def getIdx(uid: String, denominator: Int): Int = {
//                 import java.security.MessageDigest
//                 import java.nio.ByteBuffer
//                 val md5 = MessageDigest.getInstance("MD5").digest((uid+"ufs.salt&*(").getBytes)
//                 // println(uid)
//                 val intIdx = ByteBuffer.wrap(md5.slice(0,4).reverse).getInt// % denominator
//                 val longIdx = intIdx & 0x00000000ffffffffL
//                 (longIdx % denominator.toLong).toInt
//             }
// 
//             import org.json4s._
//             import org.json4s.native.JsonMethods._
//             import org.json4s.JsonDSL.WithBigDecimal._
//             import org.json4s.native.Serialization.{read, write}
//             implicit val formats = DefaultFormats
// 
//             val ce = read[ClickEvent](msg)
//             // sinkTopic + "-" + getIdx(ce.uid, numOfSinkTopic).toString
//             getIdx(ce.uid, numOfSinkTopic)
//         }
// 
//         val sinkDecider: Supervision.Decider = {
//             case _ => Supervision.Restart // Your error handling
//         }
// 
//         val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
//             import GraphDSL.Implicits._
// 
//             val bcast = b.add(Broadcast[StringConsumerRecord](numOfSinkTopic))
//             Source.fromPublisher(publisher) ~> bcast.in
//             for (i <- (0 until numOfSinkTopic)) {
//                 bcast.out(i) ~> Flow[StringConsumerRecord].filter{m => getIndexByMsg(m.value) == i}
//                     // .map{m => /* println("---", getIndexByMsg(m.value), m.value);*/ProducerMessage(m.value)} ~> Sink.fromSubscriber(subscribers(i)).withAttributes(ActorAttributes.supervisionStrategy(sinkDecider))
//                     .map{m => /* println("---", getIndexByMsg(m.value), m.value);*/ProducerMessage(m.value)} ~> Sink.fromSubscriber(subscribers(i))
//                     // .map{m => println("---", getIndexByMsg(m.value), m.value);ProducerMessage(m.value)} ~> subscribers(i) 
//             }
//             ClosedShape
//         })
//         g.run()
//     }
// }
// 
// class ActorSupervisor(sinkTopic: String, brokers: String, idx: Int)(implicit val materializer: akka.stream.Materializer) extends ActorSubscriber with ActorLogging {
//     val subscriber: String = "Subscriber"
// 
//     override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
//         case e =>
//             log.error("Exception: {}", e)
//             Resume
//     }
// 
//     override val requestStrategy: RequestStrategy = WatermarkRequestStrategy(10)
// 
//     private def createSupervisedSubscriberActor() = {
//         val kafka = new ReactiveKafka()
// 
//         val subscriberProperties = ProducerProperties(
//                 bootstrapServers = brokers,
//                 topic = sinkTopic + "-" + idx.toString,
//                 valueSerializer = new StringSerializer()
//             )
// 
//         val subscriberActorProps: Props = kafka.producerActorProps(subscriberProperties)
//         context.actorOf(subscriberActorProps, subscriber)
//     }
// 
//     override def aroundPreStart() = {
//         createSupervisedSubscriberActor()
//     }
// 
//     override def receive = {
//         case msg => context.child(subscriber) match {
//             case Some(ca) => ca ! msg
//             case None =>
//         }
//     }
// }

// import kafka.producer.ProducerConfig
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
// import kafka.producer.Producer
// import scala.util.Random
// import kafka.producer.KeyedMessage
// import java.util.Date
// import kafka.consumer.Consumer
// import kafka.consumer.ConsumerConfig
// import kafka.utils._
// import kafka.utils.Logging
// import kafka.consumer.KafkaStream
import scala.collection.JavaConversions._


object TopicSplit {
    case class ClickEvent(uid: String)

    def main(args: Array[String]): Unit = {
        val brokers = args(0)
        val sourceTopic = args(1)
        val consumerGroup = args(2)
        val sinkTopic = args(3)
        val numOfSinkTopic = args(4).toInt
        assert(numOfSinkTopic > 0)
        val sinkTopics = (0 until numOfSinkTopic).map{x => sinkTopic+"-"+x.toString}

        def getIndexByMsg(msg: String): Int = {
            def getIdx(uid: String, denominator: Int): Int = {
                import java.security.MessageDigest
                import java.nio.ByteBuffer
                val md5 = MessageDigest.getInstance("MD5").digest((uid+"ufs.salt&*(").getBytes)
                // println(uid)
                val intIdx = ByteBuffer.wrap(md5.slice(0,4).reverse).getInt// % denominator
                val longIdx = intIdx & 0x00000000ffffffffL
                (longIdx % denominator.toLong).toInt
            }

            import org.json4s._
            import org.json4s.native.JsonMethods._
            import org.json4s.JsonDSL.WithBigDecimal._
            import org.json4s.native.Serialization.{read, write}
            implicit val formats = DefaultFormats

            val ce = read[ClickEvent](msg)
            getIdx(ce.uid, numOfSinkTopic)
        }

        val consumer = Util.getComsumer(brokers, consumerGroup)
        consumer.subscribe(List(sourceTopic))
        val producer = Util.getProducer(brokers)
        while (true) {
            val records = consumer.poll(100)
            records foreach {record => {
                val idx = getIndexByMsg(record.value)
                // println("-----", idx, record.value)
                producer.send(new ProducerRecord(sinkTopics(idx), record.value))
            }
            }
        }
      
    }
}

