package com.inveno.server.contentgroup.facade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.sql.Timestamp;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.StringUtils;

import com.inveno.server.contentgroup.datainfo.EditorTableEntry;
import com.inveno.server.contentgroup.infomapper.InfoMapperTEditor;

public class ContentFacade extends AbstractFacade
{
	private static final Logger log = Logger.getLogger(ContentFacade.class);

	public static final int STATUS_GROUP_INACTIVE = 0;
	public static final int STATUS_GROUP_ACTIVE   = 1;
	public static final int GROUP_UNLOCKED        = 0;
	public static final int GROUP_LOCKED          = 1;

	private static ContentFacade instance = null;

	public static synchronized ContentFacade getInstance()
	{
		if (instance == null)
		{
			instance = new ContentFacade();
		}
		return instance;
	}
	private ContentFacade()
	{
	}
	public Map<String, Object> getSimilarGroupByTitle(int contentType, String md5)
	{
		Map<String, Object> mResult = null;
		try
		{
			String sql = getSql("getSimilarGroupByTitle");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentType, md5});
			if (!CollectionUtils.isEmpty(dataList))
				mResult = dataList.get(0);
		}
		catch (Exception e)
		{
			log.fatal("[getSimilarGroupByTitle]", e);
		}
		return mResult;
	}
	public Map<String, Object> getSimilarGroupByContent(int contentType, String md5)
	{
		Map<String, Object> mResult = null;
		try
		{
			String sql = getSql("getSimilarGroupByContent");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentType, md5});
			if (!CollectionUtils.isEmpty(dataList))
				mResult = dataList.get(0);
		}
		catch (Exception e)
		{
			log.fatal("[getSimilarGroupByContent]", e);
		}
		return mResult;
	}
	public Map<String, Object> getSimilarGroupByImage(int contentType, String md5)
	{
		Map<String, Object> mResult = null;
		try
		{
			String sql = getSql("getSimilarGroupByImage");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentType, md5});
			if (!CollectionUtils.isEmpty(dataList))
				mResult = dataList.get(0);
		}
		catch (Exception e)
		{
			log.fatal("[getSimilarGroupByImage]", e);
		}
		return mResult;
	}
	public boolean createGroup(String groupId, int contentType, String titleMd5, String contentMd5, String imageMd5)
	{
		TransactionStatus status = dbmgrTrans.getTransaction(def);
		try
		{
			String sql = null;
			boolean bDoUpdate = (null != getGroupInfo(groupId));
			if (bDoUpdate) {
				sql = getSql("updateGroup");
				dbmgr.update(sql, new Object[]{titleMd5, contentMd5, imageMd5, groupId});
			} else {
				sql = getSql("createGroup");
				dbmgr.update(sql, new Object[]{groupId, groupId, contentType, titleMd5, contentMd5, imageMd5});
			}
			sql = getSql("insertItem2Group");
			dbmgr.update(sql, new Object[]{groupId, groupId});
			dbmgrTrans.commit(status);
			return true;
		}
		catch (Exception e)
		{
			log.fatal("[createGroup]", e);
			dbmgrTrans.rollback(status);
		}
		return false;
	}
	public void insertItem2Group(String groupId, String contentId)
	{
		try
		{
			String sql = getSql("insertItem2Group");
			dbmgr.update(sql, new Object[]{groupId, contentId});
		}
		catch (Exception e)
		{
			log.fatal("[insertItem2Group]", e);
		}
	}
	public boolean notifyOffshelfGroup(String groupId)
	{
		try
		{
			String sql = getSql("notifyOffshelfGroup");
			dbmgr.update(sql, new Object[]{groupId});
			return true;
		}
		catch (Exception e)
		{
			log.fatal("[notifyOffshelfGroup]", e);
		}
		return false;
	}
	public boolean updateGroupActiveItem(String groupId, String activeItemId, int status, int isLock)
	{
		try
		{
			String sql = getSql("updateGroupActiveItem");
			dbmgr.update(sql, new Object[]{activeItemId, status, isLock, groupId});
			return true;
		}
		catch (Exception e)
		{
			log.fatal("[updateGroupActiveItem]", e);
		}
		return false;
	}
	public Map<String, String> getGroupIdByItemId(String[] arrItemId)
	{
		Map<String, String> mItem2Group = new HashMap<String, String>();
		try
		{
			if (arrItemId != null && arrItemId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrItemId.length; i++)
				{
					String contentId = arrItemId[i];
					if (i > 0)
						sb.append(",");
					sb.append(StringUtils.quote(contentId));
				}

				String sql = StringUtils.replace(getSql("getGroupIdByItemId"), "?", sb.toString());
				List<Map<String, Object>> dataList = dbmgr.queryForList(sql);
				for (Map<String, Object> mData : dataList)
				{
					String groupId = (String)mData.get("group_id");
					String itemId = (String)mData.get("item_id");
					mItem2Group.put(itemId, groupId);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("[getGroupIdByItemId]", e);
		}
		return mItem2Group;
	}
	public String getGroupIdByItemId(String itemId)
	{
		String groupId = null;
		try
		{
			String sql = getSql("getGroupIdByItemId");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{itemId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				groupId = (String)((Map<String, Object>)dataList.get(0)).get("group_id");
			}
		}
		catch (Exception e)
		{
			log.fatal("[getGroupIdByItemId]", e);
		}
		return groupId;
	}
	public String getGroupIdByActiveItemId(String activeItemId)
	{
		String groupId = null;
		try
		{
			String sql = getSql("getGroupIdByActiveItemId");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{activeItemId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				groupId = (String)((Map<String, Object>)dataList.get(0)).get("group_id");
			}
		}
		catch (Exception e)
		{
			log.fatal("[getGroupIdByActiveItemId]", e);
		}
		return groupId;
	}
	public Map<String, Object> getGroupInfo(String groupId)
	{
		Map<String, Object> mData = null;
		try
		{
			String sql = getSql("getGroupInfo");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{groupId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				mData = (Map<String, Object>)dataList.get(0);
			}
		}
		catch (Exception e)
		{
			log.fatal("[getGroupInfo]", e);
		}
		return mData;
	}
	public boolean isLockGroup(String groupId)
	{
		boolean fLocked = true;
		try
		{
			Map<String, Object> mData = getGroupInfo(groupId);
			if (mData != null)
			{
				fLocked = (GROUP_LOCKED == (Integer)mData.get("is_lock")) ? true : false;
			}
		}
		catch (Exception e)
		{
			log.fatal("[isLockGroup]", e);
		}
		return fLocked;
	}
	public String[] getGroupItemList(String groupId)
	{
		String[] arrItemId = null;
		try
		{
			String sql = getSql("getGroupItemList");
			List<Map<String, Object>> alData = dbmgr.queryForList(sql, new Object[]{groupId});
			ArrayList<String> alItemId = new ArrayList<String>();
			for (Map<String, Object> mData : alData)
			{
				String itemId = (String)mData.get("item_id");
				alItemId.add(itemId);
			}
			arrItemId = (String[])alItemId.toArray(new String[0]);
		}
		catch (Exception e)
		{
			log.fatal("[getGroupItemList]", e);
		}
		return arrItemId;
	}
	public List<Map<String, Object>> listEnforceOffshelfInfoByGroupId(String groupId)
	{
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try
		{
			String[] arrContentId = getGroupItemList(groupId);
			List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
			for (Map<String, String> mTableIndex : alTableIndex)
			{
				String sql = getSql("listEnforceOffshelfInfoList");
				sql = String.format(sql, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
				sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
				alResult.addAll( dbmgr.queryForList(sql) );
			}
		}
		catch (Exception e)
		{
			log.fatal("[listEnforceOffshelfInfoByGroupId]", e);
		}
		return alResult;
	}
	public Collection<String> listActiveInfoIdByGroupId(String groupId)
	{
		Collection<String> alItemId = new ArrayList<String>();
		List<Map<String, Object>> alData = listActiveInfoByGroupId(groupId);
		if (!CollectionUtils.isEmpty(alData))
		{
			for (Map<String, Object> mData : alData)
			{
				String contentId = (String)mData.get("content_id");
				alItemId.add(contentId);
			}
		}
		return alItemId;
	}
	public List<Map<String, Object>> listActiveInfoByGroupId(String groupId)
	{
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try
		{
			String[] arrContentId = getGroupItemList(groupId);
			List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
			for (Map<String, String> mTableIndex : alTableIndex)
			{
				String sql = getSql("getActiveInfoByIDList");
				sql = String.format(sql, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
				sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
				alResult.addAll( dbmgr.queryForList(sql) );
			}
		}
		catch (Exception e)
		{
			log.fatal("[listActiveInfoByGroupId]", e);
		}
		return alResult;
	}
	public Collection<String> offshelfEntireGroup(String groupId)
	{
		Collection<String> alItemId = new ArrayList<String>();
		String sql = null;

		String[] arrContentId = getGroupItemList(groupId);
		if (arrContentId != null && arrContentId.length > 0)
		{
			alItemId.addAll( (List<String>)java.util.Arrays.asList(arrContentId) );
			TransactionStatus status = dbmgrTrans.getTransaction(def);
			try
			{
				List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
				for (Map<String, String> mTableIndex : alTableIndex)
				{
					sql = getSql("notifyUpdateContent");
					sql = String.format(sql, (String)mTableIndex.get("content"));
					sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
					log.info("[offshelfEntireGroup]" + sql);
					dbmgr.update(sql);

					sql = getSql("notifyUpdateSignal");
					sql = String.format(sql, (String)mTableIndex.get("signal"));
					sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
					log.info("[offshelfEntireGroup]" + sql);
					dbmgr.update(sql);

					sql = getSql("notifyUpdateEditor");
					sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
					log.info("[offshelfEntireGroup]" + sql);
					dbmgr.update(sql);
				}
				dbmgrTrans.commit(status);
			}
			catch (Exception e)
			{
				log.fatal("[offshelfEntireGroup]", e);
				dbmgrTrans.rollback(status);
			}
		}

		sql = getSql("notifyOffshelfGroup");
		log.info("[notifyOffshelfGroup] " + sql);
		dbmgr.update(sql, new Object[]{groupId});
		return alItemId;
	}
	public String[] listExistingGroupItemMapping(String[] arrItemId)
	{
		ArrayList<String> alData = new ArrayList<String>();
		try
		{
			if (arrItemId != null && arrItemId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrItemId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(StringUtils.quote(arrItemId[i]));
				}

				String sql = StringUtils.replace(getSql("listGroupItemMapping"), "?", sb.toString());
				log.info("[listExistingGroupItemMapping] " + sql);
				List<Map<String, Object>> dataList = dbmgr.queryForList(sql);
				for (int j = 0; j < dataList.size(); j++)
				{
					String item_id = (String)((Map<String, Object>)dataList.get(j)).get("item_id");
					alData.add(item_id);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("[listExistingGroupItemMapping]", e);
		}
		return (String[])alData.toArray(new String[0]);
	}
	public boolean notifyUpdateGroupItem(String[] arrItemId)
	{
		try
		{
			String[] arrExistingItemId = listExistingGroupItemMapping(arrItemId);
			if (arrExistingItemId != null && arrExistingItemId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrExistingItemId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(StringUtils.quote(arrExistingItemId[i]));
				}
				String sql = StringUtils.replace(getSql("notifyUpdateGroupItem"), "?", sb.toString());
				log.info("[notifyUpdateGroupItem] " + sql);
				dbmgr.update(sql);
			}
			return true;
		}
		catch (Exception e)
		{
			log.fatal("[notifyUpdateGroupItem]", e);
		}
		return false;
	}
	public boolean notifyUpdateGroupItem(String itemId)
	{
		try
		{
			String sql = getSql("notifyUpdateGroupItem");
			dbmgr.update(sql, new Object[]{itemId});
			return true;
		}
		catch (Exception e)
		{
			log.fatal("[notifyUpdateGroupItem]", e);
		}
		return false;
	}
	private Map<String, Object> enumEffectiveMap(Map<String, Object> mEditor)
	{
		Map<String, Object> mResult = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : mEditor.entrySet() )
		{
			String fieldName = entry.getKey();
			Object fieldValue = entry.getValue();
			boolean fOverwrite = false;
			if (fieldValue != null)
			{
				if (fieldValue instanceof Integer)
				{
					fOverwrite = (((Integer)fieldValue).intValue() != -1);
				}
				else
				{
					fOverwrite = true;
				}
			}
			if (fOverwrite)
			{
				mResult.put(fieldName, fieldValue);
			}
		}
		return mResult;
	}
	public List<Map<String, Object>> getInfoByIDList(String[] arrContentId)
	{
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try
		{
			List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
			log.debug("alTableIndex:" + alTableIndex);
			for (Map<String, String> mTableIndex : alTableIndex)
			{
				String sql = getSql("getInfoByIDList");
				sql = String.format(sql, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
				sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
				alResult.addAll( dbmgr.queryForList(sql) );
			}
			for (int j = 0; j < alResult.size(); j++)
			{
				String contentId = (String)((Map<String, Object>)alResult.get(j)).get("content_id");
				Map<String, Object> mEditor = getEditInfoByID(contentId);
				if (mEditor != null)
				{
					((Map<String, Object>)alResult.get(j)).putAll( enumEffectiveMap(mEditor) );
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("[getInfoByIDList]", e);
		}
		return alResult;
	}
	public List<Map<String, Object>> listContent() throws Exception
	{
		String sql = getSql("listContent");
		return dbmgr.queryForList(sql);
	}
	public List<Map<String, Object>> listContentInTimeRange(java.util.Date startTime, java.util.Date endTime) throws Exception
	{
		String sql = getSql("listContentInTimeRange");
		return dbmgr.queryForList(sql, new Object[]{startTime, endTime});
	}
	public Map<String, Object> getEditInfoByID(String contentId)
	{
		Map<String, Object> mResult = null;
		try
		{
			String sql = getSql("getEditInfoByID");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				mResult = (Map<String, Object>)dataList.get(0);
			}
		}
		catch (Exception e)
		{
			log.fatal("[getEditInfoByID]", e);
		}
		return mResult;
	}
	public Map<String, Object> getInfoByID(String contentId)
	{
		Map<String, Object> info = null;
		try
		{
			Map<String, String> mTableName = getTableNameByID(contentId);
			String sql = getSql("getInfoByID");
			sql = String.format(sql, (String)mTableName.get("content"), (String)mTableName.get("signal"));
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				Map<String, Object> mEditor = getEditInfoByID(contentId);
				if (mEditor != null)
				{
					((Map<String, Object>)dataList.get(0)).putAll( enumEffectiveMap(mEditor) );
				}
				info = (Map<String, Object>)dataList.get(0);
			}
		}
		catch (Exception e)
		{
			log.fatal("[getInfoByID]", e);
		}
		return info;
	}
	private Object[] doIntegrityCheck(List<Map<String, Object>> dataList)
	{
		List<Map<String, Object>> newDataList = new ArrayList<Map<String, Object>>();
		HashSet<String> hsOffshelfIds = new HashSet<String>();

		// Need to check content state to do further action.
		if (!CollectionUtils.isEmpty(dataList))
		{
			for (Map<String, Object> item : dataList) 
			{
				boolean fEffective = false;
				String contentId = (String)item.get("content_id");
				int content_state = (Integer)item.get("state");
				log.debug("process content_id=" + contentId + ", state=" + content_state);
				if (1 == content_state)
				{
					fEffective = true;
				}
				else
				{
					hsOffshelfIds.add(contentId);
				}

				if (fEffective)
				{
					newDataList.add(item);
				}
			}
		}
		return new Object[]{newDataList, hsOffshelfIds};
	}
	public Map<String, EditorTableEntry> getTeditorDatas(String[] arrContentId)
	{
		Map<String, EditorTableEntry> mData = null;
		if (arrContentId != null && arrContentId.length > 0)
		{
			mData = new HashMap<String, EditorTableEntry>();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arrContentId.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(StringUtils.quote(arrContentId[i]));
			}
			String strContentIdList = sb.toString();
			String order = " ORDER BY update_time";
			String sql = "SELECT * FROM t_editor WHERE content_id in (" + strContentIdList + ") and status=0";
			List<EditorTableEntry> info_list = dbmgr.query(sql+order, new InfoMapperTEditor());
			for (EditorTableEntry entry : info_list)
			{
				String contentId = entry.getContentId();
				mData.put(contentId, entry);
			}
		}
		return mData;
	}
	public String getTableNameSuffixByID(String contentId)
	{
		String mResult = null;
		try
		{
			String sql = getSql("getTableNameSuffixByID");
			List<Map<String, Object>> dataList = dbmgr.queryForList(sql, new Object[]{contentId});
			if (!CollectionUtils.isEmpty(dataList))
			{
				mResult = (String)((Map<String, Object>)dataList.get(0)).get("table_name");
			}
		}
		catch (Exception e)
		{
			log.fatal("[getTableNameSuffixByID]", e);
		}
		return mResult;
	}
	private Map<String, String> getTableNameBySuffix(String tableNameSuffix)
	{
		Map<String, String> mResult = new HashMap<String, String>();
		String contentTableName = "t_content";
		String signalTableName  = "t_signal";
		String extendTableName  = "t_content_extend";
		if (!StringUtils.isEmpty(tableNameSuffix))
		{
			contentTableName += "_" + tableNameSuffix;
			signalTableName  += "_" + tableNameSuffix;
			extendTableName  += "_" + tableNameSuffix;
		}
		mResult.put("content", contentTableName);
		mResult.put("signal", signalTableName);
		mResult.put("extend", extendTableName);
		return mResult;
	}
	public List<Map<String, String>> getDistinctTableNameSuffixByID(String[] arrContentId)
	{
		List<Map<String, String>> alResult = new ArrayList<Map<String, String>>();
		if (arrContentId != null && arrContentId.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arrContentId.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(StringUtils.quote(arrContentId[i]));
			}
			String strContentIdList = sb.toString();
			try
			{
				String sql = StringUtils.replace(getSql("getDistinctTableNameSuffixByID"), "?", strContentIdList);
				List<Map<String, Object>> alData = dbmgr.queryForList(sql);
				log.debug("getDistinctTableNameSuffixByID.alData:" + alData.size());
				Map<String, HashSet<String>> mTableIndex = new HashMap<String, HashSet<String>>();
				for (Map<String, Object> mData : alData)
				{
					String tableName = (String)mData.get("table_name");
					String contentId = (String)mData.get("content_id");
					HashSet<String> hs = mTableIndex.get(tableName);
					if (hs == null)
					{
						hs = new HashSet<String>();
						mTableIndex.put(tableName, hs);
					}
					hs.add(contentId);
				}
				for (Map.Entry<String, HashSet<String>> entry : mTableIndex.entrySet())
				{
					String suffix = entry.getKey();
					String[] s = (String[])entry.getValue().toArray(new String[0]);
					Map<String, String> mIndex = getTableNameBySuffix(suffix);
					sb = new StringBuffer();
					for (int i = 0; s != null && i < s.length; i++)
					{
						if (i > 0) sb.append(",");
						sb.append(StringUtils.quote(s[i]));
					}
					mIndex.put("contentIdList", sb.toString());
					alResult.add( mIndex );
				}
			}
			catch (Exception e)
			{
				alResult.clear();
				log.fatal("[getDistinctTableNameSuffixByID]", e);
			}
		}
		return alResult;
	}
	public Map<String, String> getTableNameByID(String contentId)
	{
		String tableNameSuffix = getTableNameSuffixByID(contentId);
		return getTableNameBySuffix(tableNameSuffix);
	}
	private String[] getTableNameSuffix(String time_after)
	{
		Date date = null;
		try
		{
			date = org.apache.commons.lang.time.DateUtils.parseDate(time_after, new String[]{"yyyy-MM-dd HH:mm:ss.SSS"});
		}
		catch (Exception e)
		{
			date = new Date();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		java.text.DecimalFormat df = new java.text.DecimalFormat("00");
		if (month <= 0) {
			return new String[]{String.valueOf(year-1) + "_12" , String.valueOf(year) + "_" + df.format(month+1)};
		} else {
			return new String[]{String.valueOf(year) + "_" + df.format(month), String.valueOf(year) + "_" + df.format(month+1)};
		}
	}
	private Map<String, Object> prepareInfosFromTables(String[] arrContentId)
	{
		List<Map<String, Object>> dataList = null;
		HashSet<String> hsOffshelfIds = null;
		ArrayList<Object> lists = new ArrayList<Object>();
		if (arrContentId != null && arrContentId.length > 0)
		{
			List<Map<String, Object>> alData = getInfoByIDList(arrContentId);
			Object[] obj = doIntegrityCheck(alData);
			dataList      = (List<Map<String, Object>>)obj[0];
			hsOffshelfIds = (HashSet<String>)obj[1];
		}
		HashMap<String, Object> mResult = new HashMap<String, Object>();
		mResult.put("onshelf", dataList);
		mResult.put("offshelf", hsOffshelfIds);
		mResult.put("editor", getTeditorDatas(arrContentId));
		return mResult;
	}
	public Map<String, Object> prepareInfosToBuildGroup(String[] arrContentId)
	{
		List<Map<String, Object>> dataList = null;
		HashSet<String> hsOffshelfIds = null;
		if (arrContentId != null && arrContentId.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arrContentId.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(StringUtils.quote(arrContentId[i]));
			}
			String strContentIdList = sb.toString();
			String SQL = "SELECT * FROM t_content C INNER JOIN t_signal S ON S.content_id=C.content_id" + 
						 " WHERE S.content_id in (" + strContentIdList + ")";
			log.info("prepareInfosToBuildGroup SQL: " + SQL);
			Object[] obj = doIntegrityCheck(dbmgr.queryForList(SQL));
			dataList = (List<Map<String, Object>>)obj[0];
			hsOffshelfIds = (HashSet<String>)obj[1];
		}

		HashMap<String, Object> mResult = new HashMap<String, Object>();
		mResult.put("onshelf", dataList);
		mResult.put("offshelf", hsOffshelfIds);
		mResult.put("editor", getTeditorDatas(arrContentId));
		return mResult;
	}
	private List<Map<String, Object>> prepareInfosFromTables(String[] tableNameSuffix, String time_after, int limit_count)
	{
		String order = " ORDER BY S.update_time";
		String limit = " LIMIT 0, " + limit_count;

		List<Map<String, Object>> alData = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < tableNameSuffix.length; i++)
		{
			String content_table_name = "t_content_" + tableNameSuffix[i];
			String signal_table_name  = "t_signal_" + tableNameSuffix[i];
			String sql = "SELECT * FROM " + content_table_name + " C INNER JOIN " + signal_table_name + " S ON S.content_id=C.content_id WHERE S.update_time >= '" + time_after + "'";

			log.info(dbmgr + "\tSQL: "+sql+order+limit);
			alData.addAll( dbmgr.queryForList(sql+order+limit) );
		}
		Collections.sort(alData, new java.util.Comparator<Map<String, Object>>(){
			public int compare(Map<String, Object> o1, Map<String, Object> o2)
			{
				return ((Timestamp)o1.get("update_time")).compareTo((Timestamp)o2.get("update_time"));
			}
			public boolean equals(Object obj)
			{
				return this.equals(obj);
			}
		});
		return alData;
	}
	public Map<String, Object> prepareInfosToBuildGroup(String time_after, int limit_count)
	{
		Map<String, Object> mResult = null;
		HashSet<String> hsContentId = new HashSet<String>();

		String[] tableNameSuffix = getTableNameSuffix(time_after);
		List<Map<String, Object>> alData = prepareInfosFromTables(tableNameSuffix, time_after, limit_count);
		log.debug("prepareInfosToBuildGroup alData:" + alData.size());
		String strLastContentId  = null;
		String strLastUpdateTime = time_after;
		if (alData.size() > 0)
		{
			Map<String, Object> lastOne = alData.get(alData.size()-1);
			String update_time = ((Timestamp)lastOne.get("update_time")).toString();
			if (update_time.toString().equals(time_after))
			{
				int nNewLimit = Integer.valueOf(limit_count) * 2; //increase fetch number
				int nTryCount = 3; //try 2*limit_count -> 4*limit_count -> 8*limit_count
				while (true)
				{
					if (nTryCount <= 0)
						break;
					nTryCount--;

					alData = prepareInfosFromTables(tableNameSuffix, time_after, nNewLimit);

					if (alData.size() > 0)
					{
						lastOne = alData.get(alData.size()-1);
						update_time = ((Timestamp)lastOne.get("update_time")).toString();
						if (update_time.toString().equals(time_after) == false)
						{
							break;
						}
						else
						{
							nNewLimit *= 2; //continue to increase fetch number
						}
					}
					else
					{
						break;
					}
				}
			}
			for (Map<String, Object> item : alData)
			{
				hsContentId.add((String)item.get("content_id"));
			}
			String sql = "select content_id from t_editor where update_time >= " + StringUtils.quote(time_after) + " and update_time < " + StringUtils.quote(update_time) + " and status=0";
			alData = dbmgr.queryForList(sql);
			for (Map<String, Object> item : alData)
			{
				hsContentId.add((String)item.get("content_id"));
			}
			log.debug("prepareInfosToBuildGroup hsContentId:" + hsContentId.size());
			mResult = prepareInfosFromTables((String[])hsContentId.toArray(new String[0]));
			strLastContentId  = (String)lastOne.get("content_id");
			strLastUpdateTime = update_time.toString();
		}

		mResult.put("lastContentId", strLastContentId);
		mResult.put("lastUpdateTime", strLastUpdateTime);
		return mResult;
	}
}
