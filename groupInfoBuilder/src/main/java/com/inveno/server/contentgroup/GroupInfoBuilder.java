package com.inveno.server.contentgroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.inveno.server.contentgroup.datainfo.EditorTableEntry;
import com.inveno.server.contentgroup.facade.ContentFacade;
import com.inveno.server.contentgroup.util.DBRedundantInfoHelper;
import com.inveno.server.contentgroup.util.GroupInfoExchangeDaemon;
import com.inveno.server.contentgroup.util.DateUtils;
import com.inveno.server.contentgroup.util.SystemUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import org.apache.log4j.Logger;

public class GroupInfoBuilder
{
	private static final Logger log = Logger.getLogger(GroupInfoBuilder.class);

	private static final Logger monitorLogger = Logger.getLogger("monitor");

	private static final String PROPERTIES_FILE_OFFSHELF   = "timestamp_offshelf.properties";
	private static final String PROPERTIES_FILE_ONSHELF    = "timestamp_onshelf.properties";
	private static final String PROPERTIES_FILE_CONNECTION = "connection-info.properties";
	private static final String RESOURCE_BUNDLE_CONFIG     = "configs";

	private static boolean bOnline = false;

	//redis cluster for cache
	private static JedisCluster jedisCache;

	private static boolean is_using_kafka = false;
	private static String strKafkaBootstrapServers;
	private static String strKafkaGroupID;
	private static String strKafkaTopic;

	public static void storeProperties(Properties props, String properties_file_name, String comments)
	{
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(new File(properties_file_name));
			props.store(writer, comments);
		}
		catch (Exception e)
		{
			log.error("\t\t  start init write " + properties_file_name + " error ...", e);
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.flush();
					writer.close();
				}
				catch (Exception e)
				{
					//ignore
				}
			}
		}
	}
	public static Properties loadProperties(String properties_file_name)
	{
		FileReader reader = null;
		Properties props = new Properties();
		try
		{
			reader = new FileReader(new File(properties_file_name));
			props.load(reader);
		}
		catch (Exception e)
		{
			log.error("\t\t  start init read " + properties_file_name + " error ...", e);
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				//ignore
			}
		}
		return props;
	}
	public static List<String> getCategoriesListByVersion(String strCategoriesJSON, String version)
	{
		List<String> listCategories = new ArrayList<String>();

		try
		{
			Map maps3_d = (Map)JSON.parseObject(strCategoriesJSON, Map.class);
			if (maps3_d != null)
			{
				int applyIndex = 0;
				String[] candidateVersion = version.split(",");
				for (int i = 0; i < candidateVersion.length; i++)
				{
					if (null != maps3_d.get(candidateVersion[i]))
					{
						applyIndex = i;
						break;
					}
				}
				String applyVersion = candidateVersion[applyIndex];
				Map mapCatIDsWithWeight = (Map)maps3_d.get(applyVersion);
				Iterator it = mapCatIDsWithWeight.entrySet().iterator();
				double maxWeight = -Double.MAX_VALUE;
				while (it.hasNext())
				{
					Map.Entry entry = (Map.Entry)it.next();
					String categoryId = (String)entry.getKey();
					double weight = ((Number)((Map)entry.getValue()).get("weight")).doubleValue();
					if (weight > maxWeight)
					{
						listCategories.clear();
						listCategories.add(categoryId);
						maxWeight = weight;
					}
					else if (weight == maxWeight)
					{
						listCategories.add(categoryId);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("[getCategoriesListByVersion]", e);
		}

		return listCategories;
	}

	public static Map<String, Object> applyEditorData(Map<String, Object> mInfo, EditorTableEntry editorEntry)
	{
		if (editorEntry != null)
		{
			if (StringUtils.isNotEmpty(editorEntry.getTitle()))
				mInfo.put("title", editorEntry.getTitle());

			if (StringUtils.isNotEmpty(editorEntry.getCategories()))
				mInfo.put("categories", editorEntry.getCategories());

			if (StringUtils.isNotEmpty(editorEntry.getContent()))
				mInfo.put("content", editorEntry.getContent());

			if (editorEntry.getContentType() != null && editorEntry.getContentType() != -1)
				mInfo.put("content_type", editorEntry.getContentType());
		}
		return mInfo;
	}

	@SuppressWarnings("unchecked")
	public static String processOffshelf(String time_after, int limit_count) {
		long startTime = System.nanoTime();

		long beginTime;
		//building group info first.
		Map<String, Object> mData = ContentFacade.getInstance().prepareInfosToBuildGroup(time_after, limit_count);
		HashSet<String> hsRemoveIds = (HashSet<String>)mData.get("offshelf");
		log.info("[processOffshelf]\tBegin save Group into Redis...");
		log.info("[processOffshelf]\tremovableIDs: " + hsRemoveIds);
		beginTime = System.nanoTime();
		log.info("[processOffshelf]\t >> doRemoveGroupInfo");
		DBRedundantInfoHelper.getInstance().doRemoveGroupInfo(hsRemoveIds);
		log.info("[processOffshelf]\t << doRemoveGroupInfo");
		log.info("[processOffshelf]\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
		String lastContentId  = (String)mData.get("lastContentId");
		String lastUpdateTime = (String)mData.get("lastUpdateTime");
		log.info("[processOffshelf]\tlast content_id=" + lastContentId + "\tlastInfo.update_time=" + lastUpdateTime);

		long delay = System.currentTimeMillis() - DateUtils.stringToTimestampMillis(lastUpdateTime);
		monitorLogger.info(SystemUtils.getIPAddress(true)+"&&group.offshelf.process-time-delay&&"+ delay +"&&0&&AVG&&60&&");
		return lastUpdateTime;
	}
	@SuppressWarnings("unchecked")
	public static String processOnshelf(String time_after, int limit_count) {
		long startTime = System.nanoTime();

		long beginTime;
		//building group info first.
		Map<String, Object> mData = ContentFacade.getInstance().prepareInfosToBuildGroup(time_after, limit_count);
		List<Map<String, Object>> alInfo = (List<Map<String, Object>>)mData.get("onshelf");
		Map<String, EditorTableEntry> mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");
		for (Map<String, Object> mInfo : alInfo)
		{
			String contentId = (String)mInfo.get("content_id");
			mInfo = applyEditorData(mInfo, mEditorEntry.get(contentId));
		}
		beginTime = System.nanoTime();
		log.info("[processOnshelf]\tBegin save Group into Redis...");
		log.info("[processOnshelf]\t >> doStoreGroupInfo");
		DBRedundantInfoHelper.getInstance().doStoreGroupInfo(alInfo);
		log.info("[processOnshelf]\t << doStoreGroupInfo");
		log.info("[processOnshelf]\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
		log.info("[processOnshelf]\tprocess info count:" + ((alInfo == null) ? 0 : alInfo.size()) );
		String lastContentId  = (String)mData.get("lastContentId");
		String lastUpdateTime = (String)mData.get("lastUpdateTime");
		log.info("[processOnshelf]\tlast content_id=" + lastContentId + "\tlastInfo.update_time=" + lastUpdateTime);
		long delay = System.currentTimeMillis() - DateUtils.stringToTimestampMillis(lastUpdateTime);
		monitorLogger.info(SystemUtils.getIPAddress(true)+"&&group.onshelf.process-time-delay&&"+ delay +"&&0&&AVG&&60&&");
		return lastUpdateTime;
	}
	@SuppressWarnings("unchecked")
	public static String process(String time_after, int limit_count)
	{
		long startTime = System.nanoTime();

		long beginTime;
		//building group info first.
		Map<String, Object> mData = ContentFacade.getInstance().prepareInfosToBuildGroup(time_after, limit_count);
		List<Map<String, Object>> alInfo = (List<Map<String, Object>>)mData.get("onshelf");
		Map<String, EditorTableEntry> mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");
		for (Map<String, Object> mInfo : alInfo)
		{
			String contentId = (String)mInfo.get("content_id");
			mInfo = applyEditorData(mInfo, mEditorEntry.get(contentId));
		}
		HashSet<String> hsRemoveIds = (HashSet<String>)mData.get("offshelf");
		log.info("\tBegin save Group into Redis...");
		log.info("\tremovableIDs: " + hsRemoveIds);
		beginTime = System.nanoTime();
		log.info("\t >> doRemoveGroupInfo");
		DBRedundantInfoHelper.getInstance().doRemoveGroupInfo(hsRemoveIds);
		log.info("\t << doRemoveGroupInfo");
		log.info("\t >> doStoreGroupInfo");
		DBRedundantInfoHelper.getInstance().doStoreGroupInfo(alInfo);
		log.info("\t << doStoreGroupInfo");
		log.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
		log.info("process info count:" + ((alInfo == null) ? 0 : alInfo.size()) );
		String lastContentId  = (String)mData.get("lastContentId");
		String lastUpdateTime = (String)mData.get("lastUpdateTime");
		log.info("last content_id=" + lastContentId + "\tlastInfo.update_time=" + lastUpdateTime);
		return lastUpdateTime;
	}

	public static void main(String[] args)
	{
		Runnable runnableOffshelf = new Runnable() {
			public void run() {
				boolean flag = true;
				while (flag)
				{
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
					long current_datetime = new Date().getTime();
					log.info("Processing...");
					log.info("Current Date and Time in CST time zone: " + fmt.format(current_datetime));

					String record_expired_days = "1";
					String run_period_secs = "10";
					String time_after = null;
					int limit_count = 1000;
					try {
						Properties props = loadProperties(PROPERTIES_FILE_OFFSHELF);
						record_expired_days = props.getProperty("record_expired_days", "1");
						run_period_secs = props.getProperty("run_period_secs", "10");
						time_after = props.getProperty("timestamp");
						limit_count = Integer.parseInt(props.getProperty("fetch_limit_count", "1000"));
						log.info("Working timestamp: " + time_after);
					}
					catch (Exception e)
					{
						log.fatal("", e);
					}

					String lastUpdateTime = time_after;
					try
					{
						lastUpdateTime = processOffshelf(time_after, limit_count);
					}
					catch (Exception e)
					{
						log.fatal("", e);
					}

					if (lastUpdateTime != null)
					{
						Properties props = new Properties();
						props.setProperty("timestamp", lastUpdateTime);
						props.setProperty("record_expired_days", record_expired_days);
						props.setProperty("run_period_secs", run_period_secs);
						props.setProperty("fetch_limit_count", String.valueOf(limit_count));
						storeProperties(props, PROPERTIES_FILE_OFFSHELF, "timestamp for feeder access");
					}

					try
					{
						Thread.sleep(Integer.valueOf(run_period_secs)*1000);
					}
					catch (InterruptedException e)
					{
						log.fatal("", e);
					}
				}
			}
		};

		Runnable runnableOnshelf = new Runnable() {
			public void run() {
				boolean flag = true;
				while (flag)
				{
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
					long current_datetime = new Date().getTime();
					log.info("Processing...");
					log.info("Current Date and Time in CST time zone: " + fmt.format(current_datetime));

					String record_expired_days = "1";
					String run_period_secs = "10";
					String time_after = null;
					int limit_count = 1000;
					try {
						Properties props = loadProperties(PROPERTIES_FILE_ONSHELF);
						record_expired_days = props.getProperty("record_expired_days", "1");
						run_period_secs = props.getProperty("run_period_secs", "10");
						time_after = props.getProperty("timestamp");
						limit_count = Integer.parseInt(props.getProperty("fetch_limit_count", "1000"));
						log.info("Working timestamp: " + time_after);
					}
					catch (Exception e)
					{
						log.fatal("", e);
					}

					String lastUpdateTime = time_after;
					try
					{
						lastUpdateTime = processOnshelf(time_after, limit_count);
					}
					catch (Exception e)
					{
						log.fatal("", e);
					}

					if (lastUpdateTime != null)
					{
						Properties props = new Properties();
						props.setProperty("timestamp", lastUpdateTime);
						props.setProperty("record_expired_days", record_expired_days);
						props.setProperty("run_period_secs", run_period_secs);
						props.setProperty("fetch_limit_count", String.valueOf(limit_count));
						storeProperties(props, PROPERTIES_FILE_ONSHELF, "timestamp for feeder access");
					}

					try
					{
						Thread.sleep(Integer.valueOf(run_period_secs)*1000);
					}
					catch (InterruptedException e)
					{
						log.fatal("", e);
					}
				}
			}
		};

		String strRedisIPWithPort = null;
		try
		{
			Properties props = loadProperties(PROPERTIES_FILE_CONNECTION);

			bOnline = Boolean.valueOf(props.getProperty("is-online"));
			if (bOnline)
			{
				strRedisIPWithPort = props.getProperty("online-redis-ip_port");
			}
			else
			{
				strRedisIPWithPort = props.getProperty("local-redis-ip_port");
			}

			is_using_kafka = Boolean.valueOf(props.getProperty("is-using-kafka"));
			strKafkaBootstrapServers = props.getProperty("kafka-servers");
			strKafkaGroupID = props.getProperty("kafka-group-id");
			strKafkaTopic = props.getProperty("kafka-topic");
		}
		catch (Exception ex)
		{
		}
		log.info("Working Redis: " + strRedisIPWithPort);

		jedisCache = com.inveno.server.contentgroup.util.JedisHelper.newClientInstance(strRedisIPWithPort.split(";"));

		java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(RESOURCE_BUNDLE_CONFIG);
		boolean fEnableGroupExchange = "true".equalsIgnoreCase(bundle.getString("enable_groupinfo_exchange"));

		ApplicationContext context = new FileSystemXmlApplicationContext("beans-config.xml");
		DBRedundantInfoHelper.getInstance().setRedisClient(jedisCache);
		GroupInfoExchangeDaemon.getInstance().setRedisClient(jedisCache);
		if (fEnableGroupExchange)
		{
			GroupInfoExchangeDaemon.getInstance().start();
		}

		log.info("Connection to server sucessfully");

		(new Thread(runnableOffshelf)).start();
		(new Thread(runnableOnshelf)).start();
		log.info("Thread started...\n");
	}
}
