package com.inveno.server.contentgroup.util;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.inveno.common.enumtype.ContentType;
import com.inveno.common.util.ContextUtils;
import com.inveno.server.contentgroup.GroupInfoBuilder;
import com.inveno.server.contentgroup.facade.ContentFacade;
import com.inveno.server.contentgroup.queue.TaskQueueProducer;

import cn.inveno.nlp.md5validation.impl.Md5ValidationV1;
import redis.clients.jedis.JedisCluster;

public class DBRedundantInfoHelper
{
	private static final Logger log = Logger.getLogger(DBRedundantInfoHelper.class);

	public static final int CONTENT_STATE_NORMAL   = 1;
	public static final int CONTENT_STATE_AUDIT    = 2;
	public static final int CONTENT_STATE_OFFSHELF = 3;

	public static final int COPYRIGHT_LEVEL_NONE      = 0;
	public static final int COPYRIGHT_LEVEL_UNKNOWN   = 1;
	public static final int COPYRIGHT_LEVEL_NORMAL    = 2;
	public static final int COPYRIGHT_LEVEL_IMPORTANT = 3;

	private static DBRedundantInfoHelper instance = null;

	private static final String PREFIX_GROUP_DETAIL = "FEEDER:ITEM2GROUP";

	private static boolean ENABLE_TASK_QUEUE = false;
	private static boolean ENABLE_NOTIFY_HONEYBEE = false;
	private static boolean ENABLE_WRITE_DATABASE = true;
	private static boolean ENABLE_COMPARE_MD5 = true;
	private static HashSet<String> RESERVED_SOURCE_SET = new HashSet<String>();
	private String CATEGORY_VERSION = "v1";

	private JedisCluster jedisCache;
	private TaskQueueProducer taskQueue;

	private DBRedundantInfoHelper()
	{
		taskQueue = com.inveno.server.contentgroup.queue.ClientKafkaProducer.getInstance();

		try
		{
			java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("configs");
			CATEGORY_VERSION       = bundle.getString("category_version");
			ENABLE_COMPARE_MD5     = "true".equalsIgnoreCase(bundle.getString("enable_compare_md5")) ? true : false;
			ENABLE_WRITE_DATABASE  = "true".equalsIgnoreCase(bundle.getString("enable_writedb")) ? true : false;
			ENABLE_TASK_QUEUE      = "true".equalsIgnoreCase(bundle.getString("enable_task_queue")) ? true : false;
			ENABLE_NOTIFY_HONEYBEE = "true".equalsIgnoreCase(bundle.getString("enable_notify_honeybee")) ? true : false;
			String str_source_list = bundle.getString("reserved_source_list");
			if (StringUtils.isNotEmpty(str_source_list))
			{
				String[] s = StringUtils.split(str_source_list, ",");
				for (int i = 0; s != null && i < s.length; i++)
					RESERVED_SOURCE_SET.add(s[i].trim());
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}

	public static synchronized DBRedundantInfoHelper getInstance()
	{
		if (instance == null)
			instance = new DBRedundantInfoHelper();
		return instance;
	}

	public void setRedisClient(JedisCluster _jedisCache)
	{
		jedisCache = _jedisCache;
		log.info("jedisCache:" + jedisCache);
	}

	public static Date string2Date(String strDate)
	{
		return string2Date(strDate, "yyyy-MM-dd HH:mm:ss.SSS");
	}
	public static Date string2Date(String strDate, String pattern)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(pattern);
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		Date publishTime = new Date();
		if (StringUtils.isNotEmpty(strDate))
		{
			try
			{
				publishTime = fmt.parse(strDate);
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				log.warn("[string2Date]", e);
			}
		}
		return publishTime;
	}
	private int getSourceTypePriority(String source)
	{
		int priority = 0;
		String[] arr_source_type = new String[]{"app", "blog", "web", "rss", "socialmedia", "selfmedia"};
		for (int i = 0; i < arr_source_type.length; i++)
		{
			if (arr_source_type[i].equalsIgnoreCase(source))
			{
				priority = i;
				break;
			}
		}
		return priority;
	}
	public String checkSimilarTitle(String contentId, int contentType, String md5)
	{
		Map<String, Object> mSimilar = ContentFacade.getInstance().getSimilarGroupByTitle(contentType, md5);
		String existingId = (null == mSimilar) ? null : (String)mSimilar.get("group_id");
		log.info("[similarTitle] contentId=" + contentId + " md5=" + md5 + " existingId=" + existingId);
		return existingId;
	}
	public String checkSimilarContent(String contentId, int contentType, String md5)
	{
		Map<String, Object> mSimilar = ContentFacade.getInstance().getSimilarGroupByContent(contentType, md5);
		String existingId = (null == mSimilar) ? null : (String)mSimilar.get("group_id");
		log.info("[similarContent] contentId=" + contentId + " md5=" + md5 + " existingId=" + existingId);
		return existingId;
	}
	public String checkSimilarImage(String contentId, int contentType, String md5)
	{
		Map<String, Object> mSimilar = ContentFacade.getInstance().getSimilarGroupByImage(contentType, md5);
		String existingId = (null == mSimilar) ? null : (String)mSimilar.get("group_id");
		log.info("[similarContent] contentId=" + contentId + " md5=" + md5 + " existingId=" + existingId);
		return existingId;
	}
	private HashMap<String, Object> toComparableMap(Map<String, Object> info)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("contentId", info.get("content_id"));
		map.put("source", info.get("source"));
		map.put("copyright", NumberUtils.toInt(String.valueOf(info.get("copyright")), COPYRIGHT_LEVEL_UNKNOWN));
		map.put("sourceType", info.get("source_type"));
		map.put("sourceTypePriority", getSourceTypePriority((String)info.get("source_type")));
		map.put("rate", info.get("rate"));
		map.put("publishTime", info.get("publish_time"));
		return map;
	}
	private Map<String, Object> selectSurvivalContentByPriority(Map<String, Object> incomingInfo, Map<String, Object> existingInfo)
	{
		Map<String, Object> survivalInfo = null;
		if (incomingInfo == null && existingInfo == null)
			survivalInfo = null;
		else if (existingInfo == null)
			survivalInfo = incomingInfo;
		else
		{
			HashMap mIncomingInfo = toComparableMap(incomingInfo);
			HashMap mExistingInfo = toComparableMap(existingInfo);
			String incomingSource = (String)mIncomingInfo.get("source");
			String existingSource = (String)mExistingInfo.get("source");
			int incomingCopyright = (Integer)mIncomingInfo.get("copyright");
			int existingCopyright = (Integer)mExistingInfo.get("copyright");
			int incomingSourceTypePriority = (Integer)mIncomingInfo.get("sourceTypePriority");
			int existingSourceTypePriority = (Integer)mExistingInfo.get("sourceTypePriority");
			int incomingRate = (Integer)mIncomingInfo.get("rate");
			int existingRate = (Integer)mExistingInfo.get("rate");
			Date incomingPublishTime = (Date)mIncomingInfo.get("publishTime");
			Date existingPublishTime = (Date)mExistingInfo.get("publishTime");

			boolean flag = false;
			if (StringUtils.isNotEmpty(incomingSource) && RESERVED_SOURCE_SET.contains(incomingSource))
			{
				survivalInfo = incomingInfo;
			}
			else if (StringUtils.isNotEmpty(existingSource) && RESERVED_SOURCE_SET.contains(existingSource))
			{
				survivalInfo = existingInfo;
			}
			else if ((incomingCopyright > existingCopyright) ||
					 ((incomingCopyright == existingCopyright) && (incomingSourceTypePriority > existingSourceTypePriority)) ||
					 ((incomingCopyright == existingCopyright) && (incomingSourceTypePriority == existingSourceTypePriority) && (incomingRate > existingRate)) ||
					 ((incomingCopyright == existingCopyright) && (incomingSourceTypePriority == existingSourceTypePriority) && (incomingRate == existingRate) && ((incomingPublishTime != null && existingPublishTime != null && incomingPublishTime.after(existingPublishTime))) ))
			{
				survivalInfo = incomingInfo;
			}
			else
			{
				survivalInfo = existingInfo;
			}
			log.info("[selectSurvivalContentByPriority], incoming=" + FastJsonConverter.writeValue(mIncomingInfo) + "\texisting=" + FastJsonConverter.writeValue(mExistingInfo));
		}
		return survivalInfo;
	}
	public boolean hasEnforeceOffshelfInfo(String groupId)
	{
		List<Map<String, Object>> alInfo = ContentFacade.getInstance().listEnforceOffshelfInfoByGroupId(groupId);
		if (!CollectionUtils.isEmpty(alInfo))
			return true;
		else
			return false;
	}
	public String selectActiveItemId(String groupId)
	{
		String activeItemId = null;
		List<Map<String, Object>> alInfo = ContentFacade.getInstance().listActiveInfoByGroupId(groupId);
		if (!CollectionUtils.isEmpty(alInfo))
		{
			Map<String, Object> survivalInfo = alInfo.get(0);
			if (alInfo.size() > 1)
			{
				for (int j = 1; j < alInfo.size(); j++)
				{
					survivalInfo = selectSurvivalContentByPriority(alInfo.get(j), survivalInfo);
				}
			}
			activeItemId = (String)survivalInfo.get("content_id");
		}
		log.info("[selectActiveItemId] groupId=" + groupId + "\tactiveItemId=" + activeItemId);
		return activeItemId;
	}
	public String processRedundantMD5(Map<String, Object> incomingInfo, boolean fRequireCompareTitleMD5, boolean fRequireCompareContentMD5, boolean fRequireCompareImageMD5)
	{
		String existingId = null;
		String itemId     = (String)incomingInfo.get("content_id");
		int contentType   = Integer.parseInt(String.valueOf(incomingInfo.get("content_type")));
		try {
			if (fRequireCompareTitleMD5 || fRequireCompareContentMD5)
			{
				String md5Title   = (new cn.inveno.nlp.md5validation.impl.Md5ValidationV1()).execute((String)incomingInfo.get("title"), "");
				String md5Content = (new cn.inveno.nlp.md5validation.impl.Md5ValidationV1()).execute("", (String)incomingInfo.get("content"));
				String md5Image   = (null == incomingInfo.get("fall_image")) ? "" : org.apache.commons.codec.digest.DigestUtils.md5Hex((String)incomingInfo.get("fall_image"));
				//check similar incomingInfo
				String titleGroupId   = (!fRequireCompareTitleMD5)   ? null : checkSimilarTitle(itemId, contentType, md5Title);
				String contentGroupId = (!fRequireCompareContentMD5) ? null : checkSimilarContent(itemId, contentType, md5Content);
				String imageGroupId   = (!fRequireCompareImageMD5)   ? null : checkSimilarImage(itemId, contentType, md5Image);
				String groupId = null;
				if (StringUtils.isNotEmpty(titleGroupId))
					groupId = titleGroupId;
				else if (StringUtils.isNotEmpty(contentGroupId))
					groupId = contentGroupId;

				if (StringUtils.isNotEmpty(groupId))
				{
					if (ENABLE_WRITE_DATABASE)
					{
						ContentFacade.getInstance().insertItem2Group(groupId, itemId);
						log.info("[insertItem2Group] groupId=" + groupId + " itemId=" + itemId);
					}
					notifyGroupItemSetChanged(groupId, itemId, true);
					log.info("[notifyGroupItemSetChanged] groupId=" + groupId + " itemId=" + itemId);
				}
				else if (ENABLE_WRITE_DATABASE)
				{
					boolean bCreate = ContentFacade.getInstance().createGroup(itemId, contentType, md5Title, md5Content, md5Image);
					log.info("[createGroup] groupId=" + itemId + " contentType=" + contentType + " md5Title=" + md5Title + " md5Content=" + md5Content + " md5Image=" + md5Image + " action return=" + bCreate);
					//added by Genix.Li@2017/03/24, Fixed to avoid missing update t_content_group_item_mapping.update_time if item_mapping already exists.
					ContentFacade.getInstance().notifyUpdateGroupItem(itemId);
					log.info("[notifyUpdateGroupItem] groupId=" + itemId);
					GroupInfoExchangeDaemon.getInstance().saveGroupInfo(itemId, null, itemId);
					log.info("[saveGroupInfo] groupId=" + itemId + " itemId=" + itemId);
				}
			}
			else if (ENABLE_WRITE_DATABASE)
			{
				String groupId = ContentFacade.getInstance().getGroupIdByItemId(itemId);
				//comment by Genix.Li@2017/03/03, Fixed to update content_group_item_mapping if bypassing get group w/ md5.
				if (StringUtils.isNotEmpty(groupId))
				{
					ContentFacade.getInstance().notifyUpdateGroupItem(itemId);
					log.info("[notifyUpdateGroupItem] groupId=" + itemId);
				}
				else
				{
					boolean bCreate = ContentFacade.getInstance().createGroup(itemId, contentType, "", "", "");
					log.info("[createGroup] groupId=" + itemId + " contentType=" + contentType + " action return=" + bCreate);
					GroupInfoExchangeDaemon.getInstance().saveGroupInfo(itemId, null, itemId);
					log.info("[saveGroupInfo] groupId=" + itemId + "\titemId=" + itemId);
				}
			}
		} catch (Exception e ) {
			log.fatal("processRedundantMD5 item_id=" + itemId, e);
		}
		return existingId;
	}

	private static String getGroupDetailKey(String groupId)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(PREFIX_GROUP_DETAIL);
		sb.append(":");
		sb.append(groupId);
		return sb.toString();
	}


	private static final String STR_INTERFACE_WITH_SEMANTIC = ":list:scenario:";
	private Set<String> gSetMemesCategories = new HashSet<String>();
	private Set<String> gSetBeautiesCategories = new HashSet<String>();
	private void loadScenarioCategoriesMappingFromRedis() {
		HashSet<String> hsMemesCategories = new HashSet<String>();
		HashSet<String> hsBeautiesCategories = new HashSet<String>();
		try {
			Map<String, String> mapScenarioCategories = jedisCache.hgetAll("scenario_categoryid_map");
			/**
			 * key format: ${product}:${language}:${interface}:${semantic}:${id}
			 *     sample: noticias:Spanish:list:scenario:65816
			 */
			for (Map.Entry<String, String> entry : mapScenarioCategories.entrySet()) {
				String product_interface_semantic = entry.getKey();
				if (product_interface_semantic.indexOf(STR_INTERFACE_WITH_SEMANTIC) >= 0) {
					com.alibaba.fastjson.JSONObject objCategory = com.alibaba.fastjson.JSONObject.parseObject(entry.getValue());
					Integer[] arrCategoryIds = (Integer[])objCategory.getJSONArray("categoryId").toArray(new Integer[0]);

					String[] s = product_interface_semantic.split(STR_INTERFACE_WITH_SEMANTIC);
					long scenarioId = NumberUtils.toLong(s[s.length-1], 0);
					if (ContextUtils.isMemesChannel(scenarioId)) {
						log.info("loadScenarioCategoriesMappingFromRedis memes: " + objCategory);
						for (Integer categoryId : arrCategoryIds) {
							hsMemesCategories.add(String.valueOf(categoryId));
						}
					} else if (ContextUtils.isBeautyChannel(scenarioId)) {
						log.info("loadScenarioCategoriesMappingFromRedis beauties: " + objCategory);
						for (Integer categoryId : arrCategoryIds) {
							hsBeautiesCategories.add(String.valueOf(categoryId));
						}
					}
				}
			}
			gSetMemesCategories = hsMemesCategories;
			gSetBeautiesCategories = hsBeautiesCategories;
			log.info("CategoriesMappingFromRedis memes: " + gSetMemesCategories);
			log.info("CategoriesMappingFromRedis beauties: " + gSetBeautiesCategories);
		} catch (Exception e) {
			//ignore
		}
	}
	public boolean updateGroupActiveItem(String groupId, String activeItemId)
	{
		return updateGroupActiveItem(groupId, activeItemId, ((StringUtils.isEmpty(activeItemId)) ? ContentFacade.STATUS_GROUP_INACTIVE : ContentFacade.STATUS_GROUP_ACTIVE), ContentFacade.GROUP_UNLOCKED, false);
	}
	public boolean updateGroupActiveItem(String groupId, String activeItemId, int status, int isLocked, boolean fUpdateRedis)
	{
		String inactiveItemId = null;
		if (ContentFacade.GROUP_LOCKED == isLocked && ContentFacade.getInstance().isLockGroup(groupId)) {
			//收到锁定 active_item, 但是 group 未解锁时，不做任何操作，直接返回
			return false;
		}
		if (jedisCache != null && fUpdateRedis)
		{
			String redisKey = getGroupDetailKey(groupId);
			inactiveItemId = jedisCache.get(redisKey);
			if (status == ContentFacade.STATUS_GROUP_INACTIVE || StringUtils.isEmpty(activeItemId))
			{
				log.info("[updateGroupActiveItem] remove from redis for offshelf : " + redisKey);
				jedisCache.del(redisKey);
			}
			else if (!activeItemId.equalsIgnoreCase(inactiveItemId))
			{
				log.info("[updateGroupActiveItem] update to redis for detail : " + redisKey + "=" + activeItemId);
				jedisCache.set(redisKey, activeItemId);
				jedisCache.expire(redisKey, 30*86400); //expire in 30 days.
			}
		}
		else
		{
			Map<String, Object> mGroup = ContentFacade.getInstance().getGroupInfo(groupId);
			if (mGroup != null)
			{
				inactiveItemId = (String)mGroup.get("active_item_id");
			}
		}
		boolean fUpdate = true;
		if (ENABLE_WRITE_DATABASE)
		{
			//modified by Genix.Li@2017/02/14, to keep last activeItemId if group is offshelf.
			if (StringUtils.isEmpty(activeItemId)) {
				fUpdate = ContentFacade.getInstance().notifyOffshelfGroup(groupId);
			}
			else
			{
				fUpdate = ContentFacade.getInstance().updateGroupActiveItem(groupId, activeItemId, status, isLocked);
				//modified by Genix.Li@2017/02/23, in order to notify feeder to update Prediction from activeItemId.
				ContentFacade.getInstance().notifyUpdateGroupItem(activeItemId);
			}
			//notify group exchange if activeItem is changed.
			boolean doGroupExchange = false;
			if (StringUtils.isNotEmpty(activeItemId)) {
				doGroupExchange = (!activeItemId.equalsIgnoreCase(inactiveItemId));
			}
			else if (StringUtils.isNotEmpty(inactiveItemId)) {
				doGroupExchange = true;
			}
			if (fUpdate && doGroupExchange)
			{
				GroupInfoExchangeDaemon.getInstance().saveGroupInfo(groupId, inactiveItemId, activeItemId);
			}
		}
		return fUpdate;
	}
	public void notifyGroupItemSetChanged(Map<String, String> mItem2Group, boolean fCheckGroupLock)
	{
		HashSet<String> hsItem = new HashSet<String>();
		HashSet<String> hsGroup = new HashSet<String>();
		for (Map.Entry<String, String> entry : mItem2Group.entrySet())
		{
			String itemId = (String)entry.getKey();
			String groupId = (String)entry.getValue();
			boolean fSelectActive = (!fCheckGroupLock || (fCheckGroupLock && !ContentFacade.getInstance().isLockGroup(groupId)));
			String activeItemId = null;
			if (hasEnforeceOffshelfInfo(groupId))
			{
				log.info("offshelfEntireGroup " + groupId);
				if (ENABLE_WRITE_DATABASE)
				{
					hsItem.addAll( ContentFacade.getInstance().offshelfEntireGroup(groupId) );
				}
				else
				{
					hsItem.addAll( ContentFacade.getInstance().listActiveInfoIdByGroupId(groupId) );
				}
				hsGroup.add(groupId);
			}
			else if (fSelectActive)
			{
				activeItemId = selectActiveItemId(groupId);
			}
			if (updateGroupActiveItem(groupId, activeItemId))
			{
				hsItem.add(itemId);
				if (StringUtils.isEmpty(activeItemId))
				{
					hsGroup.add(groupId);
				}
			}
		}
		if (ENABLE_NOTIFY_HONEYBEE && !CollectionUtils.isEmpty(hsGroup))
		{
			notifyCleanCache(hsGroup);
		}
		log.info("[notifyGroupItemSetChanged] hsItem=" + hsItem);
		if (!CollectionUtils.isEmpty(hsItem))
		{
			if (ENABLE_TASK_QUEUE)
			{
				notifyFeederInfo(hsItem);
			}
			if (ENABLE_WRITE_DATABASE)
			{
				ContentFacade.getInstance().notifyUpdateGroupItem((String[])hsItem.toArray(new String[0]));
			}
		}
	}
	public boolean notifyGroupItemSetChanged(String groupId, String itemId, boolean fCheckGroupLock)
	{
		HashSet<String> hsItem = new HashSet<String>();
		boolean fSelectActive = (!fCheckGroupLock || (fCheckGroupLock && !ContentFacade.getInstance().isLockGroup(groupId)));
		String activeItemId = null;
		if (hasEnforeceOffshelfInfo(groupId))
		{
			log.info("offshelfEntireGroup " + groupId);
			if (ENABLE_WRITE_DATABASE)
			{
				hsItem.addAll( ContentFacade.getInstance().offshelfEntireGroup(groupId) );
			}
			else
			{
				hsItem.addAll( ContentFacade.getInstance().listActiveInfoIdByGroupId(groupId) );
			}
			if (ENABLE_NOTIFY_HONEYBEE)
			{
				notifyCleanCache(groupId);
			}
		}
		else if (fSelectActive)
		{
			activeItemId = selectActiveItemId(groupId);
			hsItem.add(itemId);
		}
		else
		{
			Map<String, Object> mGroup = ContentFacade.getInstance().getGroupInfo(groupId);
			if (mGroup != null)
			{
				activeItemId = (String)mGroup.get("active_item_id");
			}
			hsItem.add(itemId);
		}
		log.info("[notifyGroupItemSetChanged] hsItem=" + hsItem);
		if (!CollectionUtils.isEmpty(hsItem))
		{
			if (ENABLE_TASK_QUEUE)
			{
				notifyFeederInfo(hsItem);
			}
			if (ENABLE_WRITE_DATABASE)
			{
				ContentFacade.getInstance().notifyUpdateGroupItem((String[])hsItem.toArray(new String[0]));
			}
		}
		return updateGroupActiveItem(groupId, activeItemId);
	}
	public void doRemoveGroupInfo(Set<String> sItemId)
	{
		Set<String> hsRequireNotifyOffshelf = new HashSet<String>();
		Map<String, String> mItem2Group = new HashMap<String, String>();
		if (!CollectionUtils.isEmpty(sItemId))
		{
			mItem2Group.putAll( ContentFacade.getInstance().getGroupIdByItemId((String[])sItemId.toArray(new String[0])) );
		}
		for (String itemId : sItemId)
		{
			String groupId = (String)mItem2Group.get(itemId);
			if (StringUtils.isEmpty(groupId))
			{
				hsRequireNotifyOffshelf.add(itemId);
			}
			else
			{
				Map<String, Object> mGroup = ContentFacade.getInstance().getGroupInfo(groupId);
				if (mGroup == null) {
					mItem2Group.remove(itemId);
				} else if (ContentFacade.STATUS_GROUP_INACTIVE == NumberUtils.toInt(String.valueOf(mGroup.get("status")), ContentFacade.STATUS_GROUP_INACTIVE)) {
					mItem2Group.remove(itemId);
				}
			}
		}
		notifyGroupItemSetChanged(mItem2Group, false);
		if (!CollectionUtils.isEmpty(hsRequireNotifyOffshelf))
		{
			if (ENABLE_TASK_QUEUE)
			{
				notifyFeederInfo(hsRequireNotifyOffshelf);
			}
			else if (ENABLE_WRITE_DATABASE)
			{
				ContentFacade.getInstance().notifyUpdateGroupItem((String[])hsRequireNotifyOffshelf.toArray(new String[0]));
			}
		}
	}
	private void notifyCleanCache(String groupId)
	{
		HashMap mInfo = new HashMap();
		mInfo.put("content_id", groupId);
		mInfo.put("timestamp", System.currentTimeMillis());
		taskQueue.send(TaskQueueProducer.QUEUE_NOTIFY_HONEYBEE, FastJsonConverter.writeValue(mInfo));
	}
	private void notifyCleanCache(Collection<String> alGroup)
	{
		for (String groupId : alGroup)
		{
			HashMap mInfo = new HashMap();
			mInfo.put("content_id", groupId);
			mInfo.put("timestamp", System.currentTimeMillis());
			taskQueue.send(TaskQueueProducer.QUEUE_NOTIFY_HONEYBEE, FastJsonConverter.writeValue(mInfo));
		}
	}
	private void notifyFeederInfo(Collection<String> alItem)
	{
		for (String itemId : alItem)
		{
			HashMap mInfo = new HashMap();
			mInfo.put("content_id", itemId);
			mInfo.put("timestamp", System.currentTimeMillis());
			taskQueue.send(TaskQueueProducer.QUEUE_NOTIFY_FEEDER, FastJsonConverter.writeValue(mInfo));
		}
	}
	public void doStoreGroupInfo(List<Map<String, Object>> info_list)
	{
		loadScenarioCategoriesMappingFromRedis();
		HashSet<String> hsItem = new HashSet<String>();
		if (!CollectionUtils.isEmpty(info_list))
		{
			for (Map<String, Object> mInfo : info_list)
			{
				String contentId   = (String)mInfo.get("content_id");
				String categories  = (String)mInfo.get("categories");
				int contentType = NumberUtils.toInt(String.valueOf(mInfo.get("content_type")));
				List<String> listCategories = GroupInfoBuilder.getCategoriesListByVersion(categories, CATEGORY_VERSION);
				boolean fGIFInfo          = (contentType == ContentType.GIF.getValue());
				boolean fSpecialIssueInfo = (contentType == ContentType.SPECIAL_ISSUE.getValue());
				boolean fMemeInfo         = (contentType == ContentType.MEME.getValue() && CollectionUtils.containsAny(gSetMemesCategories, listCategories));
				boolean fBeautiesInfo     = (contentType == ContentType.BEAUTY.getValue() && CollectionUtils.containsAny(gSetBeautiesCategories, listCategories));
				boolean fVideoInfo        = (contentType == ContentType.VIDEO.getValue());
				boolean fComicInfo        = (contentType == ContentType.COMIC.getValue());
				boolean fRequireCompareTitleMD5   = (ENABLE_COMPARE_MD5 && !fGIFInfo && !fBeautiesInfo && !fSpecialIssueInfo && !fComicInfo && !fMemeInfo);
				boolean fRequireCompareContentMD5 = (ENABLE_COMPARE_MD5 && !fGIFInfo && !fBeautiesInfo && !fVideoInfo && !fSpecialIssueInfo && !fComicInfo && !fMemeInfo);
				boolean fRequireCompareImageMD5   = (ENABLE_COMPARE_MD5 && (contentType == ContentType.MEME.getValue() || contentType == ContentType.BEAUTY.getValue()));
				String existingId = processRedundantMD5(mInfo, fRequireCompareTitleMD5, fRequireCompareContentMD5, fRequireCompareImageMD5);
				if (StringUtils.isEmpty(existingId) || contentId.equalsIgnoreCase(existingId))
				{
					hsItem.add(contentId);
				}
			}
			if (ENABLE_TASK_QUEUE)
			{
				notifyFeederInfo(hsItem);
			}
		}
	}
	public static void main(String[] args)
	{
		try
		{
			org.springframework.context.ApplicationContext context = new org.springframework.context.support.FileSystemXmlApplicationContext("beans-config.xml");
			Date startTime = null, endTime = null;
			if (args.length >= 2)
			{
				startTime = string2Date(args[0], "yyyy-MM-dd");
				endTime   = string2Date(args[1], "yyyy-MM-dd");
			}
			List<Map<String, Object>> alContent = (startTime != null && endTime != null) ? ContentFacade.getInstance().listContentInTimeRange(startTime, endTime) : ContentFacade.getInstance().listContent();
			ArrayList<String[]> alTask = new ArrayList<String[]>();
			ArrayList<String> alBatch = new ArrayList<String>();
			for (int j = 0; j < alContent.size(); j++)
			{
				if (j > 0 && j % 1000 == 0)
				{
					alTask.add( alBatch.toArray(new String[0]) );
					alBatch.clear();
				}
				Map<String, Object> mInfo = (Map<String, Object>)alContent.get(j);
				String contentId = (String)mInfo.get("content_id");
				alBatch.add(contentId);
			}
			if (alBatch.size() > 0)
			{
				alTask.add( alBatch.toArray(new String[0]) );
			}

			for (String[] arrContentId : alTask)
			{
				List<Map<String, Object>> alInfo = ContentFacade.getInstance().getInfoByIDList(arrContentId);
				List<Map<String, Object>> alNewInfo = new java.util.ArrayList<Map<String, Object>>();
				java.util.HashSet<String> hsRemovedContentId = new java.util.HashSet<String>();
				for (Map<String, Object> mInfo : alInfo)
				{
					int state = Integer.parseInt(String.valueOf(mInfo.get("state")));
					if (state == 1)
					{
						alNewInfo.add(mInfo);
					}
					else if (state == 3)
					{
						String contentId  = (String)mInfo.get("content_id");
						hsRemovedContentId.add(contentId);
					}
				}
				DBRedundantInfoHelper.getInstance().doRemoveGroupInfo(hsRemovedContentId);
				DBRedundantInfoHelper.getInstance().doStoreGroupInfo(alNewInfo);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
}
