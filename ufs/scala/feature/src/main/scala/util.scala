package ufs

import java.util.concurrent._
import scala.util.DynamicVariable
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import com.redis._
import java.text.SimpleDateFormat

object Util {
  def getIdx(uid: String, salt: String, denominator: Int): Int = {
    import java.security.MessageDigest
    import java.nio.ByteBuffer
    val md5 = MessageDigest.getInstance("MD5").digest((uid + salt).getBytes)
    // println(uid)
    val intIdx = ByteBuffer.wrap(md5.slice(0, 4).reverse).getInt // % denominator
    val longIdx = intIdx & 0x00000000ffffffffL
    (longIdx % denominator.toLong).toInt
  }

  def makeConns(addrs: String): Array[RedisClient] = {
    // ssdb_conn=192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1
    addrs.split(",").map(x => {
      val hp = x.split(":")
      new RedisClient(hp(0), hp(1).toInt)
    })
  }

  def makePooledConns(addrs: String): Array[RedisClientPool] = {
    // ssdb_conn=192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1
    addrs.split(",").map(x => {
      val hp = x.split(":")
      new RedisClientPool(hp(0), hp(1).toInt)
    })
  }

  val forkJoinPool = new ForkJoinPool

  abstract class TaskScheduler {
    def schedule[T](body: => T): ForkJoinTask[T]
    def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
      val right = task {
        taskB
      }
      val left = taskA
      (left, right.join())
    }
  }

  class DefaultTaskScheduler extends TaskScheduler {
    def schedule[T](body: => T): ForkJoinTask[T] = {
      val t = new RecursiveTask[T] {
        def compute = body
      }
      Thread.currentThread match {
        case wt: ForkJoinWorkerThread =>
          t.fork()
        case _ =>
          forkJoinPool.execute(t)
      }
      t
    }
  }

  val scheduler =
    new DynamicVariable[TaskScheduler](new DefaultTaskScheduler)

  def task[T](body: => T): ForkJoinTask[T] = {
    scheduler.value.schedule(body)
  }

  def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
    scheduler.value.parallel(taskA, taskB)
  }

  def parallel[A, B, C, D](taskA: => A, taskB: => B, taskC: => C, taskD: => D): (A, B, C, D) = {
    val ta = task { taskA }
    val tb = task { taskB }
    val tc = task { taskC }
    val td = taskD
    (ta.join(), tb.join(), tc.join(), td)
  }

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

  def parseDateTime(dt: String, tz: Int): Int = {
    try {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val date = df.parse(dt)
      (date.getTime() / 1000).toInt - tz * 60 * 60
    } catch {
      case t: Throwable => {
        t.printStackTrace() // TODO: handle error
        (System.currentTimeMillis() / 1000).toInt
      }
    }
  }
}
