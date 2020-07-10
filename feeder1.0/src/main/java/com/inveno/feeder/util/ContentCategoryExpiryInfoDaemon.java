package com.inveno.feeder.util;

import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import com.inveno.common.enumtype.ContentType;
import com.inveno.feeder.datainfo.CategoryInfo;
import com.inveno.feeder.Feeder;
import com.inveno.feeder.ClientJDBCTemplate;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.JedisCluster;

/**
 * Created by dell on 2016/5/17.
 */
public class ContentCategoryExpiryInfoDaemon extends TerminatableThread
{
	private static final Logger log = Logger.getLogger(ContentCategoryExpiryInfoDaemon.class);

	public static final String KEY_EXPIRY_HOUR = "content_category_expiry_hour";

	public static final String KEY_CONTENT_TYPE_VERSION_EXPIRY_HOUR = "contenttype_version_category_expiryhour";

	private static ContentCategoryExpiryInfoDaemon instance = null;

	private static final String PROPERTIES_FILE_DAEMON = "daemons.properties";

	private static final int DEFAULT_REDIS_EXPIRE = 30 * 86400;
	private static int RELOAD_PERIOD     = 30 * 60 * 1000;
	private static int INACCURACY_PERIOD =  5 * 60 * 1000;

	private JedisCluster jedisCache;

	static
	{
		try
		{
			Properties props = Feeder.loadProperties(PROPERTIES_FILE_DAEMON);
			if (props != null)
			{
				String strReloadPeriod = props.getProperty("ContentCategoryExpiryInfoDaemon.reloadPeriod");
				if (StringUtils.isNotEmpty(strReloadPeriod))
					RELOAD_PERIOD = Integer.parseInt(strReloadPeriod);
				String strInaccuracyPeriod = props.getProperty("ContentCategoryExpiryInfoDaemon.inaccuracyPeriod");
				if (StringUtils.isNotEmpty(strInaccuracyPeriod))
					INACCURACY_PERIOD = Integer.parseInt(strInaccuracyPeriod);
			}
			log.info("ContentCategoryExpiryInfoDaemon.reloadPeriod=" + RELOAD_PERIOD);
			log.info("ContentCategoryExpiryInfoDaemon.inaccuracyPeriod=" + INACCURACY_PERIOD);
		}
		catch (Exception e)
		{
			log.fatal("[ContentCategoryExpiryInfoDaemon]", e);
		}
		finally
		{

		}
	}
	public static synchronized ContentCategoryExpiryInfoDaemon getInstance()
	{
		if (instance == null)
		{
			instance = new ContentCategoryExpiryInfoDaemon();
		}
		return instance;
	}
	private ContentCategoryExpiryInfoDaemon()
	{
	}

	public void setRedisClient(JedisCluster _jedisCache)
	{
		jedisCache = _jedisCache;
		log.info("jedisCache:" + jedisCache);
	}

	public void run()
	{
		doTask();
	}

	private void doTask()
	{
		log.info("ContentCategoryExpiryInfoDaemon.start");
		while (true)
		{
			if (isTerminated())
			{
				break;
			}

			long loopStart = System.currentTimeMillis();
			if (jedisCache != null)
			{
				Map<String, Map<String, Integer>> mContentTypeCategoryExpiry = new HashMap<String, Map<String, Integer>>();
				List<CategoryInfo> alCategoryInfo = ClientJDBCTemplate.getInstance().listCategoryInfo(Feeder.getCategorySystemVersion());
				Map<String, Integer> mCategoryExpiry = new HashMap<String, Integer>();
				for (CategoryInfo categoryInfo : alCategoryInfo)
				{
					mCategoryExpiry.put(String.valueOf(categoryInfo.getId()), categoryInfo.getExpiryHour());
				}
				mContentTypeCategoryExpiry.put(String.valueOf(ContentType.DEFAULT.getValue()), mCategoryExpiry);

				alCategoryInfo = ClientJDBCTemplate.getInstance().listCategoryInfoForOversea();
				for (CategoryInfo categoryInfo : alCategoryInfo)
				{
					String strContentType = String.valueOf( categoryInfo.getContentType() );
					//comment by Genix.Li@2017/09/05, add field `version` in table t_category_expiry, and filter with version=2 for backward compatible
					if (categoryInfo.getVersion() != 2)
						continue;
					mCategoryExpiry = mContentTypeCategoryExpiry.get(strContentType);
					if (mCategoryExpiry == null)
					{
						mCategoryExpiry = new HashMap<String, Integer>();
						mContentTypeCategoryExpiry.put(strContentType, mCategoryExpiry);
					}
					mCategoryExpiry.put(String.valueOf(categoryInfo.getId()), categoryInfo.getExpiryHour());
				}

				for (Map.Entry<String, Map<String, Integer>> entry : mContentTypeCategoryExpiry.entrySet())
				{
					String contentType = entry.getKey();
					String jsonExpiryHour = com.alibaba.fastjson.JSON.toJSONString( entry.getValue() );
					jedisCache.hset(KEY_EXPIRY_HOUR, contentType, jsonExpiryHour);
				}
				log.info("ContentCategoryExpiryInfoDaemon update category info");
				jedisCache.expire(KEY_EXPIRY_HOUR, DEFAULT_REDIS_EXPIRE);

				//set new key `content_type_version_category_expiry_hour` for set category expiry hours by version for each content_type
				Map<String, Map<Integer, Map<String, Integer>>> mContentTypeVersionCategoryExpiry = new HashMap<String, Map<Integer, Map<String, Integer>>>();
				for (CategoryInfo categoryInfo : alCategoryInfo)
				{
					String strContentType = String.valueOf( categoryInfo.getContentType() );
					int version = categoryInfo.getVersion();
					Map<Integer, Map<String, Integer>> mVersionCategoryExpiry = mContentTypeVersionCategoryExpiry.get(strContentType);
					if (mVersionCategoryExpiry == null) {
						mVersionCategoryExpiry = new HashMap<Integer, Map<String, Integer>>();
						mContentTypeVersionCategoryExpiry.put(strContentType, mVersionCategoryExpiry);
					}
					mCategoryExpiry = (Map<String, Integer>)mVersionCategoryExpiry.get(version);
					if (mCategoryExpiry == null) {
						mCategoryExpiry = new HashMap<String, Integer>();
						mVersionCategoryExpiry.put(version, mCategoryExpiry);
					}
					mCategoryExpiry.put(String.valueOf(categoryInfo.getId()), categoryInfo.getExpiryHour());
				}
				for (Map.Entry<String, Map<Integer, Map<String, Integer>>> entry : mContentTypeVersionCategoryExpiry.entrySet())
				{
					String contentType = entry.getKey();
					String jsonExpiryHour = com.alibaba.fastjson.JSON.toJSONString( entry.getValue() );
					jedisCache.hset(KEY_CONTENT_TYPE_VERSION_EXPIRY_HOUR, contentType, jsonExpiryHour);
				}

				jedisCache.expire(KEY_CONTENT_TYPE_VERSION_EXPIRY_HOUR, DEFAULT_REDIS_EXPIRE);
			}

			try
			{
				while (!isTerminated())
				{
					long loopEnd = System.currentTimeMillis();
					if (loopEnd - loopStart >= RELOAD_PERIOD)
						break;
					Thread.currentThread().sleep(INACCURACY_PERIOD);
				}
			}
			catch (Exception e)
			{
			}
		}
		log.info("ContentCategoryExpiryInfoDaemon.finish");
	}
	public static void main(String[] args)
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run()
				{
					ContentCategoryExpiryInfoDaemon.getInstance().terminated();
					while (true)
					{
						if (ContentCategoryExpiryInfoDaemon.getInstance().isTerminated())
							break;
						try
						{
							Thread.currentThread().sleep(100);
						}
						catch (Exception e)
						{
							log.fatal("[Termination]", e);
						}
					}
				}
			});
			ContentCategoryExpiryInfoDaemon.getInstance().start();
		}
		catch (Exception e)
		{
			log.fatal("[main]", e);
		}
	}
}
