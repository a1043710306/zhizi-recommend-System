package com.inveno.feeder;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;


public class ClientKafkaConsumer
{
	private static final Logger kafkaLogger = Logger.getLogger("feeder.kafka");

	private String server;
	private String group;
	private String topic;

	KafkaConsumer<String, String> consumer;
	private volatile long lastTime;

	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
	private ExecutorService singleExecutor;
	
	
	private static void writeKafkaConsumerLog(String log_str, String mode)
	{
		//System.out.println(log_str);
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String today = fmt.format(new Date());
		
		fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		
		try {
			
			File fLogFilename = new File("./logs/"+today+"_kafkaConsumer.log");
			if (!fLogFilename.exists())
			{
				fLogFilename.createNewFile();
			}
			
			FileWriter fileWritter = new FileWriter(fLogFilename.getAbsolutePath(), true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write("["+fmt.format(new Date())+"]\t["+mode+"] "+log_str+"\n");
	        bufferWritter.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public ClientKafkaConsumer(String strBootstrapServers, String strGroupId, String strTopic) {
		
		this.server = strBootstrapServers;
		this.group = strGroupId;
		this.topic = strTopic;
		
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void init() throws Exception {
		Preconditions.checkNotNull(this.server, "kafka.server is required");
		Preconditions.checkNotNull(this.group, "kafka.group is required");
		Preconditions.checkNotNull(this.topic, "kafka.topic is required");

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, this.group);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "6000");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		//props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "10485760");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Arrays.asList(this.topic));

		this.consumer = kafkaConsumer;
		this.singleExecutor = Executors.newSingleThreadExecutor();

		kafkaLogger.info("Init kafkaConsumer finished");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> acquireFeederTask()
	{
		ArrayList<String> alContentId = new ArrayList<String>();

		int nFetchedNum = 0;
		while (nFetchedNum <= 0) 
		{
			try
			{
				ConsumerRecords<String, String> records = consumer.poll(1000);
				if (records.count() > 0)
				{
					for (TopicPartition partition : records.partitions())
					{
						List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
						for (ConsumerRecord<String, String> record : partitionRecords)
						{
							Map<String, Object> mapp = JSON.parseObject(record.value(), Map.class);
							if (mapp != null)
							{
								String contentId = (String)mapp.get("content_id");
								alContentId.add( contentId );
								nFetchedNum++;
							}
							else
							{
								kafkaLogger.info("Error to get data!!");
							}
						}

						long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
						consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastoffset + 1)));
					}
				}
				else
				{
					Thread.sleep(1000);
					kafkaLogger.info("No message received!");
				}
			}
			catch (Exception e)
			{
				kafkaLogger.error("Exception: ", e);
			}
		}
		kafkaLogger.info("The total fetched number is " + nFetchedNum);
		return alContentId;
	}
	
	public int processContent(List<Map<String, Object>> listOnline, List<Map<String, Object>> listOffline)
	{
		if (listOnline == null || listOffline == null)
			return -1;
		
		runPollContent(listOnline, listOffline);

		return 0;
	}
	
	@SuppressWarnings("unchecked")
	private void runPollContent(List<Map<String, Object>> listOnline, List<Map<String, Object>> listOffline)
	{
		int nFetchedNum = 0;
		while (nFetchedNum <= 0) 
		{
			try
			{
				ConsumerRecords<String, String> records = consumer.poll(1000);
				if (records.count() > 0)
				{
					for (TopicPartition partition : records.partitions())
					{
						final Set<String> removableIDs = new HashSet<String>();

						List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
						for (ConsumerRecord<String, String> record : partitionRecords) {

							Map<String, Object> mapp = JSON.parseObject(record.value(), Map.class);
							if (mapp != null) {

								kafkaLogger.info(mapp.get("content_id") + " state:" + mapp.get("state"));

								int state = (Integer)mapp.get("state");
								switch (state) {
								case 1: {
									listOnline.add(mapp);
									break;
								}
								default:
									listOffline.add(mapp);
									removableIDs.add((String)mapp.get("content_id"));
									break;
								}

								nFetchedNum++;
							} else {
								kafkaLogger.info("Error to get data!!");
							}

						}

						long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
						consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastoffset + 1)));
					}
				}
				else
				{
					Thread.sleep(1000);
					kafkaLogger.info("No message received!");
				}
			}
			catch (Exception e)
			{
				kafkaLogger.error("Exception: ", e);
			}
		}
		kafkaLogger.info("The total fetched number is " + nFetchedNum);
	}
	
//	private class pollContentThread implements Runnable {
//
//		private List<Map<String, Object>> m_listOnline = null;
//		private List<Map<String, Object>> m_listOffline = null;
//		
//		public pollContentThread(List<Map<String, Object>> listOnline, List<Map<String, Object>> listOffline) {
//			m_listOnline = listOnline;
//			m_listOffline = listOffline;
//		}
//
//		public void run() {
//			
//			writeKafkaConsumerLog("[TID:"+Thread.currentThread().getId()+"] BEGIN poll thread...", "POLLTHREAD");
//			
//			runPollContent(m_listOnline, m_listOffline);
//			
//			writeKafkaConsumerLog("[TID:"+Thread.currentThread().getId()+"] END poll thread", "POLLTHREAD");
//		}
//		
//	}
	
	public int processEditor(List<Map<String, Object>> listEditor) {
		
		if (listEditor == null)
			return -1;
		
		int nFetchedNum = 0;
		while (nFetchedNum <= 0)
		{
			try {
				
				ConsumerRecords<String, String> records = consumer.poll(100);
				if (records.count() > 0) {
					
					for (TopicPartition partition : records.partitions()) {
						
						List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
						for (ConsumerRecord<String, String> record : partitionRecords) {
							
							@SuppressWarnings("unchecked")
							Map<String, Object> mapp = JSON.parseObject(record.value(), Map.class);
							writeKafkaConsumerLog("message: ["+record.offset()+" of "+record.partition()+"] "+mapp.get("content_id")+":"+mapp.get("status"), "EDITOR");

							int status = (int)mapp.get("status");
							if (status == 0) {
								listEditor.add(mapp);
								nFetchedNum++;
							}
						}

						long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
						consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastoffset + 1)));
					}
				}
				
			} catch (Exception e) {
				writeKafkaConsumerLog("Exception: "+e, "EDITOR");
			}
		}
		writeKafkaConsumerLog("The total fetched number is "+nFetchedNum, "EDITOR");
		
		return nFetchedNum;
	}

	@PreDestroy
	public void shutdown() {
		try {
			singleExecutor.shutdown();
			consumer.close();
		} catch (Exception e) {
			writeKafkaConsumerLog("Destroy kafkaConsumer error! "+e, "UNINIT");
		}
		writeKafkaConsumerLog("Destroy kafkaConsumer finished", "UNINIT");
	}

	public String poll() {
		try {
			return this.queue.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			writeKafkaConsumerLog("poll error!!!"+e, "POLL");
			return null;
		}
	}

	public long getLastTime() {
		return this.lastTime;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	

}
