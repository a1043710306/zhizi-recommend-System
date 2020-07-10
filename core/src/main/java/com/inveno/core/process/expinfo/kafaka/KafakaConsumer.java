package com.inveno.core.process.expinfo.kafaka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import redis.clients.jedis.JedisCluster;

@Deprecated
public class KafakaConsumer {
	
	
	private final ConsumerConnector consumer;
	private final String topic;
	private ExecutorService executor;
	private JedisCluster jedisCluster;
	
	private Log flowExpLog = LogFactory.getLog("flowExpTriggerLog");
//	public static Logger flowExpLog = Logger.getLogger("flowExpLog");

	public KafakaConsumer(String a_zookeeper, String a_groupId, String a_topic,JedisCluster jedisCluster) {
		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(a_zookeeper, a_groupId));
		this.topic = a_topic;
		this.jedisCluster =  jedisCluster;
	}

	public void shutdown() {
		if (consumer != null) {
			consumer.shutdown();
		}
		if (executor != null) {
			executor.shutdown();
		}
	}

	public void run(int a_numThreads) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(a_numThreads));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

		// now launch all the threads
		executor = Executors.newFixedThreadPool(a_numThreads);

		// now create an object to consume the messages
		@SuppressWarnings("unused")
		int threadNumber = 0;
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.submit(()->{
				try {
					ConsumerIterator<byte[], byte[]> it = stream.iterator();
					while (it.hasNext()) {
						String infoIdMsg = new String(it.next().message());
						String[] infoMsg = infoIdMsg.split("#");
						String infoId  = infoMsg[0];
						String product = infoMsg[1];
						String lan = infoMsg[2];
						
						
						String eagerly_key = "expinfo_"+product+"_"+lan +"_eagerly";
						String flowing_key = "expinfo_"+product+"_"+lan +"_ing";
						
						String  impressionOfInfoIdKey =  "expinfo::"+product +"::"+lan +"::impressionOfInfoIdKey";
						flowExpLog.info("infoIdMsg is "+ infoIdMsg +" and remove from " + eagerly_key +" , "+flowing_key +" , "+impressionOfInfoIdKey);
						jedisCluster.hdel(impressionOfInfoIdKey, infoId);
						jedisCluster.zrem(eagerly_key, infoId);
						jedisCluster.zrem(flowing_key, infoId);
					}
				} catch (Exception e) {
					flowExpLog.error("executor.submit error" ,e);
				}
			});
			threadNumber++;
		}
	}
	
	private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
		Properties props = new Properties();
		props.put("zookeeper.connect", a_zookeeper);
		props.put("group.id", a_groupId);
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		return new ConsumerConfig(props);
	}


}
