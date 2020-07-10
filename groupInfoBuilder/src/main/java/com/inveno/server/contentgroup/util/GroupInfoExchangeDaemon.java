package com.inveno.server.contentgroup.util;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import redis.clients.jedis.JedisCluster;

import com.inveno.server.contentgroup.facade.ContentFacade;

/**
 * Created by dell on 2016/5/17.
 */
public class GroupInfoExchangeDaemon extends TerminatableThread
{
	private static final Logger log = Logger.getLogger(GroupInfoExchangeDaemon.class);

	public static final String QUEUE_GROUP_INFO = "gp_write_group_info";
	public static final String QUEUE_ITEM_INFO  = "gp_write_item_info";

	private static GroupInfoExchangeDaemon instance = null;

	private boolean fRunning = false;

	private JedisCluster jedisCache;

	public static synchronized GroupInfoExchangeDaemon getInstance()
	{
		if (instance == null)
		{
			instance = new GroupInfoExchangeDaemon();
		}
		return instance;
	}
	private GroupInfoExchangeDaemon()
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

	public void saveGroupInfo(String groupId, String inactiveItemId, String activeItemId)
	{
		HashMap mData = new HashMap();
		mData.put("group_id", groupId);
		mData.put("inactive", inactiveItemId);
		mData.put("active", activeItemId);
		mData.put("timestamp", System.currentTimeMillis());
		String json = FastJsonConverter.writeValue(mData);
		if (jedisCache != null)
		{
			jedisCache.rpush(QUEUE_GROUP_INFO, json);
		}
		log.info("[QUEUE_GROUP_INFO] " + json);
	}
	private void doTask()
	{
		log.info("GroupInfoExchangeDaemon.start");
		while (true)
		{
			if (isTerminated())
			{
				break;
			}

			if (jedisCache != null)
			{
				if (jedisCache.llen(QUEUE_ITEM_INFO) > 0L)
				{
					String json = (jedisCache.lpop(QUEUE_ITEM_INFO));
					try
					{
						HashMap mData = (HashMap)FastJsonConverter.readValue(HashMap.class, json);
						String groupId = (String)mData.get("group_id");
						String activeItemId = (String)mData.get("active");
						int status = ContentFacade.STATUS_GROUP_ACTIVE;
						int isLock = ContentFacade.GROUP_LOCKED;
						if (mData.get("status") != null)
						{
							status = Integer.parseInt(String.valueOf(mData.get("status")));
						}
						if (mData.get("is_lock") != null)
						{
							isLock = Integer.parseInt(String.valueOf(mData.get("is_lock")));
						}
						boolean bDoUpdate = false;
						if (!StringUtils.isEmpty(groupId))
						{
							if (status == ContentFacade.STATUS_GROUP_INACTIVE)
							{
								ContentFacade.getInstance().offshelfEntireGroup(groupId);
							}
							bDoUpdate = DBRedundantInfoHelper.getInstance().updateGroupActiveItem(groupId, activeItemId, status, isLock, true);
						}
						if (bDoUpdate) {
							log.info("[QUEUE_ITEM_INFO] processing done : " + json);
						} else {
							log.info("[QUEUE_ITEM_INFO] processing rejected : " + json);
						}
					}
					catch (Exception e)
					{
						log.fatal("[QUEUE_ITEM_INFO] invalid protocol : " + json);
					}
				}
			}
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (Exception e)
			{
			}
		}
		log.info("GroupInfoExchangeDaemon.finish");
	}
	public static void main(String[] args)
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run()
				{
					GroupInfoExchangeDaemon.getInstance().terminated();
					while (true)
					{
						if (GroupInfoExchangeDaemon.getInstance().isTerminated())
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
			GroupInfoExchangeDaemon.getInstance().start();
		}
		catch (Exception e)
		{
			log.fatal("[main]", e);
		}
	}
}
