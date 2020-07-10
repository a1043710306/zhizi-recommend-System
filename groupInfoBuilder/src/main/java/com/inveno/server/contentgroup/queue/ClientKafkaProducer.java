package com.inveno.server.contentgroup.queue;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.common.base.Preconditions;

public class ClientKafkaProducer implements TaskQueueProducer
{
	private static final Logger logger = Logger.getLogger(ClientKafkaProducer.class);

	private static ClientKafkaProducer instance = null;

	private String server;
	private String topic;
	// The producer is thread safe
	private Producer<String, String> producer;

	public static synchronized ClientKafkaProducer getInstance()
	{
		if (instance == null)
		{
			ResourceBundle bundle = ResourceBundle.getBundle("connection-info");
			String server = bundle.getString("kafka-servers");
			String topic  = bundle.getString("kafka-topic");
			instance = new ClientKafkaProducer(server, topic);
		}
		return instance;
	}
	private ClientKafkaProducer(String _server, String _topic)
	{
		server = _server;
		topic  = _topic;

		Preconditions.checkNotNull(this.server, "kafka.server is required");

		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
		props.put(ProducerConfig.ACKS_CONFIG, "1");// 1：只保证发送到leader成功，all：保证leader,follower都成功
		props.put(ProducerConfig.RETRIES_CONFIG, 1);// 重试次数
		// props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 5);// 延迟发送，以聚合更多消息
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * 1024 * 1024L);// 缓存
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

		this.producer = new KafkaProducer<>(props);
		logger.info("init producer finished!!!");
	}

	public void destroy()
	{
		try
		{
			producer.close();
		}
		catch (Exception e)
		{
			logger.error("destroy kafkaProducer error!", e);
		}
		logger.info("destroy kafkaProducer finished!!!");
	}

	public Future<RecordMetadata> send(String topic, String value)
	{
		logger.info("send to topic <" + topic + "> w/ data : " + value);
		return producer.send(new ProducerRecord<String, String>(topic, value));
	}
}
