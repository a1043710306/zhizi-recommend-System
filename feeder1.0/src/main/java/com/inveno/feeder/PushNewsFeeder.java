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
import java.util.Calendar;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.inveno.feeder.constant.FeederConstants;
import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.feeder.datainfo.*;
import com.inveno.feeder.infomapper.*;

import redis.clients.jedis.JedisCluster;

public class PushNewsFeeder
{
	private static final Logger flowLogger = Logger.getLogger("feeder.flow");

	private static final String PROPERTIES_FILE_TIMESTAMP  = "timestamp_pushnews.properties";
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
	private static void doStoreIntoRedisHash(String contentId, String prefix_string, String field_name, boolean fOverwriteAppendPromotionPic)
	{
		TDeserializer deserializer = new TDeserializer();
		FeederInfo feederInfo = new FeederInfo();
		try {
			String key_name = prefix_string + "_" + contentId;
			byte[] byteKey = jedis.hget(key_name.getBytes(), field_name.getBytes());
			flowLogger.info("\t\t[doStoreIntoRedisHash] jedis hget key_name=" + key_name + " field_name=" + field_name + " field_value= " + byteKey);

			deserializer.deserialize(feederInfo, byteKey);

			byte[] empDtl = null;
			TSerializer serializer = new TSerializer();
			if (fOverwriteAppendPromotionPic)
			{
				feederInfo.setAppend_promotion_pic(1);
			}
			empDtl = serializer.serialize(feederInfo);
			if (!fDebugMode) {
				jedis.hset((prefix_string + "_" + contentId).getBytes(), field_name.getBytes(), empDtl);
			}
			flowLogger.info("\t\t[doStoreIntoRedisHash] overwriteAppendPromotionPic=" + fOverwriteAppendPromotionPic + " contentId=" + contentId + " access " + field_name + ".");
		}
		catch (Exception e)
		{
			flowLogger.error("[doStoreIntoRedisHash]", e);
		}
	}
	public static boolean isPushNews(Map<String, Object> mPushNews) {
		boolean bPushNews = false;
		Date push_begin_time = null;
		String contentId = (String)mPushNews.get("content_id");
		String itemId = (String)mPushNews.get("item_id");
		if (mPushNews != null) {
			try {
				Date date = (Date)mPushNews.get("begin_time");
				push_begin_time = date;
				SimpleDateFormat fmt = new SimpleDateFormat(FeederConstants.DATE_TIMESTAMP_FORMAT);
				fmt.setTimeZone(TimeZone.getTimeZone("GMT-6:00"));
				String strDate = fmt.format(date);
				fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
				date = fmt.parse(strDate);
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
				int hour_of_day = c.get(Calendar.HOUR_OF_DAY);
				if (day_of_week == 0) {
					//Sunday
					bPushNews = (hour_of_day >= 10 && hour_of_day <= 22);
				} else if (day_of_week == 6) {
					//Saturday
					bPushNews = (hour_of_day >= 11 && hour_of_day <= 22);
				} else {
					//week working days
					bPushNews = (hour_of_day >= 8 && hour_of_day <= 22);
				}
			} catch (Exception e) {
				flowLogger.fatal("[isPushNews]", e);
			}
		}
		flowLogger.info("contentId:" + contentId + "\titemId=" + itemId + "\tbegin_time:" + push_begin_time + "\tisPushNews:" + bPushNews);
		return bPushNews;
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
		
		Map<String, Object> mData = clientJDBCTemplate.listPushNews(strLastWorkingTS);
		alOnshelfInfo = (List<Map<String, Object>>)mData.get("onshelf");
		strLastUpdateTime = (String)mData.get("lastUpdateTime");

		boolean fHasNewInfo = false;
		List<FeederInfo> alFeederInfo = null;
		Set<String> hsCurrentProcessedContentId = new HashSet<String>();
		if (!CollectionUtils.isEmpty(alOnshelfInfo))
		{
			for (Map<String, Object> mInfo : alOnshelfInfo) {
				hsCurrentProcessedContentId.add((String)mInfo.get("item_id"));
			}

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

		if (fHasNewInfo && !CollectionUtils.isEmpty(alOnshelfInfo))
		{
			flowLogger.info("\tBegin save API into Redis...");
			beginTime = System.nanoTime();
			for (Map<String, Object> mInfo : alOnshelfInfo) {
				String itemId = (String)mInfo.get("item_id");
				if (isPushNews(mInfo)) {
					doStoreIntoRedisHash(itemId, "news", "API", true);
				}
			}
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