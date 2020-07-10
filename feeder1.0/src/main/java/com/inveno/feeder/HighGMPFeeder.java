package com.inveno.feeder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.feeder.datainfo.*;
import com.inveno.feeder.infomapper.*;

import redis.clients.jedis.JedisCluster;

public class HighGMPFeeder
{
	private static final Logger flowLogger = Logger.getLogger("gmpfeeder.flow");

	private static final String PROPERTIES_FILE_TIMESTAMP  = "timestamp_highgmp.properties";
	private static final String PROPERTIES_FILE_CONNECTION = "connection-info.properties";

	private static boolean fDebugMode = true;

	private static boolean bOnline = false;

	private static boolean bOversea = false;

	private static boolean bWriteEditorHDFS = false;

	//redis cluster for detail
	private static JedisCluster jedis;

	private static ClientJDBCTemplate clientJDBCTemplate;

	private static ClientKafkaConsumer clientKafka = null;

	private static boolean is_using_kafka = false;
	private static String strKafkaBootstrapServers;
	private static String strKafkaGroupID;
	private static String strKafkaTopic;

	private static HashSet<String> hsLastProcessContentId = new HashSet<String>();

	public static Set<String> enumerateContentIdList(List<FeederInfo> info_list)
	{
		Set<String> hsContentId = new HashSet<String>();
		if (!CollectionUtils.isEmpty(info_list))
		{
			for (FeederInfo info : info_list)
			{
				hsContentId.add(info.getContent_id());
			}
		}
		return hsContentId;
	}
	private static void notifyPrimaryUpdate(String article_id)
	{
		String articleid_updated_key = article_id+"::updated::withinhalfhour";
		if (!fDebugMode)
		{
			jedis.set(articleid_updated_key, "1");
			jedis.expire(articleid_updated_key, 1800); //expire after 30 mins..
		}
		flowLogger.info("\t\t  The article "+article_id+" has set "+articleid_updated_key);
	}
	private static void doStoreIntoRedisHash(List<FeederInfo> info_list, String prefix_string, String field_name, boolean fOverwriteHighGMPChecked)
	{
		if (info_list.size() > 0)
		{
			for (FeederInfo info : info_list)
			{
				String contentId = info.getContent_id();
				String groupId = clientJDBCTemplate.getGroupId(contentId);

				byte[] empDtl = null;
				TSerializer serializer = new TSerializer();
				try
				{
					if (fOverwriteHighGMPChecked)
					{
						info.setHighGmpCheck(1);
					}
					empDtl = serializer.serialize(info);
				}
				catch (TException e)
				{
					flowLogger.error("[doStoreIntoRedisHash]", e);
				}

				if (!fDebugMode)
				{
					jedis.hset((prefix_string + "_" + groupId).getBytes(), field_name.getBytes(), empDtl);
					flowLogger.info("\t\t[doStoreIntoRedisHash] overwriteHighGMPChecked=" + fOverwriteHighGMPChecked + " groupId=" + groupId + " contentId=" + contentId + " access " + field_name + ".");
					notifyPrimaryUpdate(groupId);
				}
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for " + field_name);
		}
	}

	@SuppressWarnings("unchecked")
	public static String process(String strLastWorkingTS, String limit_count, boolean bOversea) throws ParseException
	{
		long startTime = System.nanoTime();

		long beginTime;

		List<Map<String, Object>> alOnshelfInfo = null;
		HashSet<String> hsOffshelfIds = null;
		ArrayList<Object> lists = new ArrayList<Object>();
		Map<String, EditorTableEntry> mEditorEntry = null;
		String strLastUpdateTime = strLastWorkingTS;
		if (is_using_kafka)
		{
			flowLogger.info("Start to fetch articles from kafka");
			ArrayList<String> alContentId = clientKafka.acquireFeederTask();
			flowLogger.info("End to fetch articles from kafka");
			flowLogger.info("\tFetched from kafka cost: " + (System.nanoTime() - startTime)/1000000 + " ms");

			Map<String, Object> mData = clientJDBCTemplate.listInfos((String[])alContentId.toArray(new String[0]), bOversea, false);
			flowLogger.info("\tQuery MySQL cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
			alOnshelfInfo = (List<Map<String, Object>>)mData.get("onshelf");
			hsOffshelfIds = (HashSet<String>)mData.get("offshelf");
			mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");
		}
		else
		{
			Map<String, Object> mData = clientJDBCTemplate.listHighImpressionCheckInfo(strLastWorkingTS, bOversea);
			alOnshelfInfo = (List<Map<String, Object>>)mData.get("onshelf");
			hsOffshelfIds = (HashSet<String>)mData.get("offshelf");
			mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");
			strLastUpdateTime = (String)mData.get("lastUpdateTime");
		}
		flowLogger.info("");

		boolean fHasNewInfo = false;
		List<FeederInfo> alFeederInfo = null;
		Set<String> hsCurrentProcessedContentId = null;
		if (!CollectionUtils.isEmpty(alOnshelfInfo))
		{
			alFeederInfo = (new InfoMapperPrediction()).mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry);
			hsCurrentProcessedContentId = enumerateContentIdList(alFeederInfo);

			if (!CollectionUtils.isEmpty(hsLastProcessContentId) && !CollectionUtils.isEmpty(hsCurrentProcessedContentId))
			{
				if (hsLastProcessContentId.size() == hsCurrentProcessedContentId.size())
				{
					HashSet<String> hsTmp = new HashSet<String>();
					hsTmp.addAll(hsCurrentProcessedContentId);
					hsTmp.removeAll(hsLastProcessContentId);
					fHasNewInfo = (!CollectionUtils.isEmpty(hsTmp));
				}
				else
				{
					fHasNewInfo = true;
				}
			}
			else
			{
				fHasNewInfo = true;
			}
		}
		flowLogger.info("hsLastProcessContentId:" + hsLastProcessContentId);
		flowLogger.info("hsCurrentProcessedContentId:" + hsCurrentProcessedContentId);
		flowLogger.info("fHasNewInfo:" + fHasNewInfo);

		if (fHasNewInfo && !CollectionUtils.isEmpty(alFeederInfo))
		{
			flowLogger.info("\tBegin save Prediction into Redis...");
			beginTime = System.nanoTime();
			doStoreIntoRedisHash(alFeederInfo, "news", "Prediction", true);
			flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
			flowLogger.info("");

			if (hsLastProcessContentId != null)
			{
				hsLastProcessContentId.clear();
				hsLastProcessContentId.addAll(hsCurrentProcessedContentId);
			}
		}
		else
		{
			flowLogger.info("\tNo new data to process...");
			flowLogger.info("");
		}

		int nUpdateCount = (!fHasNewInfo || CollectionUtils.isEmpty(alOnshelfInfo)) ? 0 : alOnshelfInfo.size();
		flowLogger.info("Update Count: " + nUpdateCount);
		flowLogger.info("Next timestamp: " + strLastUpdateTime);
		flowLogger.info("Spent time: " + (System.nanoTime() - startTime)/1000000 + " ms");
		flowLogger.info("");
		return strLastUpdateTime;
	}

	public static void main(String[] args)
	{
		Runnable r = new Runnable() {
			public void run() {
				boolean flag = true;
				while (flag)
				{
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
					long current_datetime = new Date().getTime();
					flowLogger.info("Processing...");
					flowLogger.info("Current Date and Time in CST time zone: " + fmt.format(current_datetime));

					String run_period_secs = "10";
					String strLastWorkingTS = null;
					String limit_count = "1000";
					try
					{
						Properties props = Feeder.loadProperties(PROPERTIES_FILE_TIMESTAMP);
						run_period_secs = props.getProperty("run_period_secs");
						strLastWorkingTS = props.getProperty("timestamp");
						limit_count = props.getProperty("fetch_limit_count");
						flowLogger.info("Working timestamp: " + strLastWorkingTS);
					}
					catch (Exception e)
					{
						flowLogger.fatal("[main]", e);
					}

					String lastUpdateTime = strLastWorkingTS;
					try
					{
						lastUpdateTime = process(strLastWorkingTS, limit_count, bOversea);
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						flowLogger.fatal("[process]", e);
					}

					if (lastUpdateTime != null)
					{
						Properties props = new Properties();
						//props.setProperty("timestamp", "2006-06-25 00:00:00.0");
						props.setProperty("timestamp", lastUpdateTime);
						props.setProperty("run_period_secs", run_period_secs);
						props.setProperty("fetch_limit_count", limit_count);
						Feeder.storeProperties(props, PROPERTIES_FILE_TIMESTAMP, "timestamp for feeder access");
					}

					try
					{
						Thread.sleep(Integer.valueOf(run_period_secs)*1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		};

		String strRedisDetailIPWithPort = null;
		try
		{
			Properties props = Feeder.loadProperties(PROPERTIES_FILE_CONNECTION);

			fDebugMode = Boolean.valueOf(props.getProperty("is-debug-mode"));
			bOnline = Boolean.valueOf(props.getProperty("is-online"));
			if (bOnline)
			{
				strRedisDetailIPWithPort = props.getProperty("online-redis-ip_port");
			}
			else
			{
				strRedisDetailIPWithPort = props.getProperty("local-redis-ip_port");
			}

			bOversea = Boolean.valueOf(props.getProperty("is-oversea"));

			is_using_kafka = Boolean.valueOf(props.getProperty("is-using-kafka"));
			strKafkaBootstrapServers = props.getProperty("kafka-servers");
			strKafkaGroupID = props.getProperty("kafka-group-id");
			strKafkaTopic = props.getProperty("kafka-topic");

		}
		catch (Exception ex)
		{
		}
		flowLogger.info("Working Redis: " + strRedisDetailIPWithPort);

		jedis = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisDetailIPWithPort.split(";"));

		ApplicationContext context = new FileSystemXmlApplicationContext("beans-config.xml");
		clientJDBCTemplate = ClientJDBCTemplate.getInstance();

		if (is_using_kafka && strKafkaBootstrapServers.isEmpty() == false && strKafkaGroupID.isEmpty() == false && strKafkaTopic.isEmpty() == false)
		{
			clientKafka = new ClientKafkaConsumer(strKafkaBootstrapServers, strKafkaGroupID, strKafkaTopic);
		}

		flowLogger.info("Connection to server sucessfully");

		Thread t = new Thread(r);
		// Lets run Thread in background..
		// Sometimes you need to run thread in background for your Timer application..
		t.start(); // starts thread in background..
		// t.run(); // is going to execute the code in the thread's run method on the current thread..
		flowLogger.info("Thread started...\n");

		((ConfigurableApplicationContext)context).close();
	}
}