package com.inveno.feeder; 

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inveno.feeder.datainfo.CategoryInfo;
import com.inveno.feeder.datainfo.ContentEditorLog;
import com.inveno.feeder.datainfo.EditorTableEntry;
import com.inveno.feeder.datainfo.SourceSubscriptionEntry;
import com.inveno.feeder.infomapper.InfoMapperAPI;
import com.inveno.feeder.infomapper.InfoMapperCategory;
import com.inveno.feeder.infomapper.InfoMapperContentEditorLog;
import com.inveno.feeder.infomapper.InfoMapperES;
import com.inveno.feeder.infomapper.InfoMapperSourceSubscription;
import com.inveno.feeder.infomapper.InfoMapperTEditor;
import com.inveno.feeder.jsoninfomapper.JsonInfoMapperHDFS;
import com.inveno.feeder.model.ImagesEntry;
import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.feeder.util.FastJsonConverter;
import com.inveno.feeder.util.FeederCheckUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientJDBCTemplate implements ClientDAO 
{
	private static final Logger log = Logger.getLogger(ClientJDBCTemplate.class);

	@SuppressWarnings("unused")
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;

	private String last_update_time;
	private int last_update_count;

	private Set<String> removable_ids;
	private Set<String> hsModifiedGroup;
	private FeederCheckUtil feederCheckUtil = new FeederCheckUtil();

	private static ClientJDBCTemplate instance = null;

	public static synchronized ClientJDBCTemplate getInstance()
	{
		if (instance == null)
		{
			instance = new ClientJDBCTemplate();
		}
		return instance;
	}

	@Override
	public void setDataSource(DataSource dataSource) 
	{
		this.dataSource = dataSource;
		this.jdbcTemplateObject = new JdbcTemplate(dataSource);

		this.last_update_time = null;
		this.last_update_count = 0;

		this.removable_ids = new HashSet<String>();
		this.removable_ids.clear();

		this.hsModifiedGroup = new HashSet<String>();
	}

	public String getLastUpdateTime()
	{
		return this.last_update_time;
	}

	public int getLastUpdateCount()
	{
		return this.last_update_count;
	}

	public Set<String> getRemovableIDs()
	{
		return this.removable_ids;
	}

	public Set<String> getModifiedGroupID()
	{
		return this.hsModifiedGroup;
	}
	private String getInfoSQLByID(String contentId, boolean bOversea)
	{
		String SQL = "SELECT * FROM %s C INNER JOIN %s S ON S.content_id=C.content_id WHERE S.content_id='" + contentId + "'";
		if (bOversea)
		{
			List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(new String[]{contentId});
			if (alTableIndex.size() > 0)
			{
				Map<String, String> mTableIndex = alTableIndex.get(0);
				SQL = String.format(SQL, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
			}
			else
			{
				SQL = String.format(SQL, "t_content", "t_signal");
			}
		}
		else
		{
			SQL = String.format(SQL, "t_content", "t_signal");
		}
		return SQL;
	}
	public Map<String, Object> listPushNews(String time_after) {
		Map<String, Object> mResult = new HashMap<String, Object>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {
			String SQL = "SELECT * FROM t_push_news WHERE product='noticias' and languages='Spanish' and app_versions is not NULL and promotions is not null and create_time >= " + StringUtils.quote(time_after);
			String order = " order by create_time";
			log.info(SQL+order);
			dataList.addAll( jdbcTemplateObject.queryForList(SQL+order) );
		} catch (Exception e) {
			log.fatal("[listPushNews]", e);
		}

		String strLastUpdateTime = time_after;
		if (!CollectionUtils.isEmpty(dataList))
		{
			Map<String, Object> mLastData = (Map<String, Object>)dataList.get(dataList.size() - 1);
			strLastUpdateTime = ((Timestamp)mLastData.get("create_time")).toString();
		}
		mResult.put("onshelf", dataList);
		mResult.put("lastUpdateTime", strLastUpdateTime);
		return mResult;
	}
	public Map<String, Object> getPushNews(String contentId) {
		Map<String, Object> mResult = null;
		try {
			String SQL = "SELECT * FROM t_push_news WHERE product='noticias' and languages='Spanish' and app_versions is not NULL and promotions is not null and item_id=" + StringUtils.quote(contentId);
			List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(SQL);
			if (dataList.size() > 0) {
				mResult = dataList.get(0);
			}
		} catch (Exception e) {
			log.fatal("[getPushNews]", e);
		}
		return mResult;
	}
	public FeederInfo getInfoByID(String contentId, boolean bOversea) 
	{
		String SQL = getInfoSQLByID(contentId, bOversea);
		log.info("SQL: " + SQL);
		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(SQL);
		List<FeederInfo> alInfo = (new InfoMapperAPI()).mapIntoFeederInfoList(dataList);
		FeederInfo info = null;
		if (alInfo.size() > 0)
			info = (FeederInfo)alInfo.get(0);
		return info;
	}
	public List<Map<String, Object>> listSceneraioCategoryMapping() {
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try {
			String sql = "select product, language, scenario_id, scenario_name, category_types from t_product_scenario_mapping where status=0 order by scenario_id";
			alResult.addAll( jdbcTemplateObject.queryForList(sql) );
		} catch (Exception e) {
			log.fatal("[listSceneraioCategoryMapping]", e);
		}
		return alResult;
	}
	public List<Map<String, Object>> getInfoByIDList(String[] arrContentId, boolean bOversea)
	{
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try
		{
			String SQL = "SELECT * FROM %s C INNER JOIN %s S ON S.content_id=C.content_id WHERE S.content_id in (?)";
			if (bOversea)
			{
				List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
				for (Map<String, String> mTableIndex : alTableIndex)
				{
					String sql = String.format(SQL, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
					sql = StringUtils.replace(sql, "?", (String)mTableIndex.get("contentIdList"));
					alResult.addAll( jdbcTemplateObject.queryForList(sql) );
				}
			}
			else
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrContentId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(StringUtils.quote(arrContentId[i]));
				}
				String strContentIdList = sb.toString();
				String sql = String.format(SQL, "t_content", "t_signal");
				sql = StringUtils.replace(sql, "?", strContentIdList);
				alResult.addAll( jdbcTemplateObject.queryForList(sql) );
			}
		}
		catch (Exception e)
		{
			log.fatal("[getInfoByIDList]", e);
		}
		return alResult;
	}
	public FeederInfo getInfoForESByID(String contentId, boolean bOversea)
	{
		String SQL = getInfoSQLByID(contentId, bOversea);
		FeederInfo info = jdbcTemplateObject.queryForObject(SQL, new Object[]{contentId}, new InfoMapperES());
		return info;
	}
	public List<Map<String, Object>> listComicChapterByContentId(String contentId, String time_after) {
		List<Map<String, Object>> alResult = new ArrayList<Map<String, Object>>();
		try {
			String sql = "select * from t_content_comic where content_id = "  + StringUtils.quote(contentId);
			if (!StringUtils.isEmpty(time_after)) {
				sql += " and update_time >= " + StringUtils.quote(time_after);
			}
			sql += " order by chapter";
			log.info("[listComicChapterByContentId] sql = " + sql);
			alResult.addAll( jdbcTemplateObject.queryForList(sql) );
		} catch (Exception e) {
			log.fatal("[listComicChapterByContentId]", e);
		}
		return alResult;
	}
	public List<Map<String, Object>> getGroupInfo(Set<String> hsGroupId)
	{
		List<Map<String, Object>> alData = null;
		if (!CollectionUtils.isEmpty(hsGroupId))
		{
			String[] arrGroupId = (String[])hsGroupId.toArray(new String[0]);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arrGroupId.length; i++)
			{
				String groupId = arrGroupId[i];
				if (i > 0)
					sb.append(",");
				sb.append(StringUtils.quote(groupId));
			}
			String strItemIdList = sb.toString();
			String sql = "select * from t_content_group_info where group_id in (" + strItemIdList + ")";
			alData = jdbcTemplateObject.queryForList(sql);
		}
		return alData;
	}
	public String getGroupId(String itemId)
	{
		String groupId = itemId;
		String sql = "select group_id from t_content_group_item_mapping where item_id='" + itemId + "'";
		List<Map<String, Object>> alData = jdbcTemplateObject.queryForList(sql);
		if (!CollectionUtils.isEmpty(alData))
		{
			Map<String, Object> mData = (Map<String, Object>)alData.get(0);
			groupId = (String)mData.get("group_id");
		}
		return groupId;
	}
	public List<String> getDistinctGroupId(Collection<String> alItemId)
	{
		List<String> alGroupId = new ArrayList<String>();
		if (!CollectionUtils.isEmpty(alItemId))
		{
			StringBuffer sb = new StringBuffer();
			boolean fFirst = true;
			for (String itemId : alItemId)
			{
				if (!fFirst)
					sb.append(",");
				sb.append(StringUtils.quote(itemId));
				fFirst = false;
			}
			String strItemIdList = sb.toString();
			String sql = "select distinct group_id from t_content_group_item_mapping where item_id in (" + strItemIdList + ")";
			List<Map<String, Object>> alData = jdbcTemplateObject.queryForList(sql);
			for (Map<String, Object> mData : alData)
			{
				String groupId = (String)mData.get("group_id");
				if (!StringUtils.isEmpty(groupId))
				{
					alGroupId.add(groupId);
				}
			}
		}
		return alGroupId;
	}

	public Map<String,ImagesEntry> getListImagesByContentIds(Collection<String> alItemId)
	{
		Map<String,ImagesEntry> imagesEntryMap = new HashMap<>(alItemId.size());
		if (!CollectionUtils.isEmpty(alItemId))
		{
			StringBuffer sb = new StringBuffer();
			boolean fFirst = true;
			for (String itemId : alItemId)
			{
				if (!fFirst)
					sb.append(",");
				sb.append(StringUtils.quote(itemId));
				fFirst = false;
			}
			String strItemIdList = sb.toString();
			String sql = "select content_id,image_hash_map from t_image_hash where content_id in (" + strItemIdList + ")";
			List<Map<String, Object>> alData = jdbcTemplateObject.queryForList(sql);
			for (Map<String, Object> mData : alData)
			{
				String contentId = (String)mData.get("content_id");
				String imageHashMap = (String)mData.get("image_hash_map");
				if (!StringUtils.isEmpty(contentId)&&StringUtils.isEmpty(imageHashMap))
				{
					ImagesEntry imagesEntry = JSON.parseObject(imageHashMap,ImagesEntry.class);
					imagesEntryMap.put(contentId,imagesEntry);
				}
			}
		}
		return imagesEntryMap;
	}

	public Map<String,String> getSignalList(Collection<String> alItemId)
	{
		Date date = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String todMonth = fmt.format(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); // 设置为当前时间
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
		date = calendar.getTime();
		String lastMonth = fmt.format(date);

		Map<String,String> signalMap = new HashMap<>(alItemId.size());
		if (!CollectionUtils.isEmpty(alItemId))
		{
			StringBuffer sb = new StringBuffer();
			boolean fFirst = true;
			for (String itemId : alItemId)
			{
				if (!fFirst)
					sb.append(",");
				sb.append(StringUtils.quote(itemId));
				fFirst = false;
			}
			String strItemIdList = sb.toString();
			String sql = "select content_id,simhash from t_signal_" + todMonth + " where content_id in (" + strItemIdList + ") UNION ALL select content_id,simhash from t_signal_" + lastMonth + " where content_id in (" + strItemIdList + ")";
			List<Map<String, Object>> alData = jdbcTemplateObject.queryForList(sql);
			for (Map<String, Object> mData : alData)
			{
				String contentId = (String)mData.get("content_id");
				String simhash = (String)mData.get("simhash");
				if (!StringUtils.isEmpty(contentId)&&!StringUtils.isEmpty(simhash))
				{
					signalMap.put(contentId,simhash);
				}
			}
		}
		return signalMap;
	}

	public List<Map<String, Object>> buildImageSimHash(List<Map<String, Object>> alData)
	{
		if (!CollectionUtils.isEmpty(alData))
		{
			for (Map<String, Object> mData : alData)
			{
				String jsonBodyImages = (String)mData.get("body_images");
				String jsonListImages = (String)mData.get("list_images");
				String jsonImageSimhash = (String)mData.get("image_hash_map");

				mData.put("body_images", buildImageSimhash(jsonBodyImages, jsonImageSimhash));
				mData.put("list_images", buildImageSimhash(jsonListImages, jsonImageSimhash));
			}
		}
		return alData;
	}
	public String buildImageSimhash(String json_image, String json_simhash)
	{
		String result = null;
		try
		{
			HashMap mImageHash    = (StringUtils.isEmpty(json_simhash)) ? new HashMap() : FastJsonConverter.readValue(HashMap.class, json_simhash);
			ArrayList alBodyImage = FastJsonConverter.readValue(ArrayList.class, json_image);
			if (CollectionUtils.isEmpty(alBodyImage))
				alBodyImage = new ArrayList();
			for (int i = 0; i < alBodyImage.size(); i++)
			{
				JSONObject objBodyImage = (JSONObject)alBodyImage.get(i);
				String src = objBodyImage.getString("src");
				String simhash = (String)mImageHash.get(src);
				if (!StringUtils.isEmpty(simhash))
				{
					objBodyImage.put("simhash", simhash);
				}
			}
			result = FastJsonConverter.writeValue(alBodyImage);
		}
		catch (Exception e)
		{
			//ignore
		}
		return result;
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
				Map<String, HashSet<String>> mTableIndex = new HashMap<String, HashSet<String>>();
				String sql = "select * from t_index where content_id in (" + strContentIdList + ")";
				List<Map<String, Object>> alData = jdbcTemplateObject.queryForList(sql);
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

	@SuppressWarnings("unchecked")
	public Map<String, Object> listInfos(String[] arrContentId, boolean bOversea, boolean integrityCheck)
	{
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		HashSet<String> hsOffshelfIds = new HashSet<String>();
		ArrayList<Object> lists = new ArrayList<Object>();

		if (arrContentId != null && arrContentId.length > 0)
		{
			if (bOversea)
			{
				List<Map<String, String>> alTableIndex = getDistinctTableNameSuffixByID(arrContentId);
				for (Map<String, String> mTableIndex : alTableIndex)
				{
					String SQL = "SELECT C.*, S.*, GIM.*, C.keywords AS keywords_new, EX.content_quality, EX.categories_comic, EX.is_display_ad, EX.topics, IH.image_hash_map FROM t_content_group_item_mapping GIM"
							+ " STRAIGHT_JOIN %s C ON GIM.item_id=C.content_id"
							+ " INNER JOIN %s S ON S.content_id=C.content_id"
							+ " LEFT JOIN t_content_extend EX on EX.content_id=C.content_id"
							+ " LEFT JOIN t_image_hash IH on IH.content_id=C.content_id"
							+ " WHERE GIM.item_id in (?)";
					SQL = String.format(SQL, (String)mTableIndex.get("content"), (String)mTableIndex.get("signal"));
					SQL = StringUtils.replace(SQL, "?", (String)mTableIndex.get("contentIdList"));
					log.info("listInfos SQL: " + SQL);
					Object[] obj = doIntegrityCheck(jdbcTemplateObject.queryForList(SQL), integrityCheck);
					dataList.addAll( buildImageSimHash((List<Map<String, Object>>)obj[0]) );

					/*//修改于2020年4月16日12点38分 by tiancheng
					List<Map<String,Object>> video=getVideoContent();
					Object[] obj2=doIntegrityCheck(video,false);
					dataList.addAll(buildImageSimHash((List<Map<String, Object>>)obj2[0]));
					log.info("videolist size: " + video.size());*/


					hsOffshelfIds.addAll( (HashSet<String>)obj[1] );
				}
			}
			else
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrContentId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(StringUtils.quote(arrContentId[i]));
				}
				String strContentIdList = sb.toString();
				String SQL = "SELECT * FROM %s C"
						+ " INNER JOIN %s S ON S.content_id=C.content_id"
						+ " WHERE S.content_id in (" + strContentIdList + ")";
				SQL = String.format(SQL, "t_content", "t_signal");
				log.info("listInfos SQL: " + SQL);
				Object[] obj = doIntegrityCheck(jdbcTemplateObject.queryForList(SQL), integrityCheck);
				dataList = buildImageSimHash((List<Map<String, Object>>)obj[0]);

				/*//修改于2020年4月16日12点38分 by tiancheng
				List<Map<String,Object>> video=getVideoContent();
				Object[] obj2=doIntegrityCheck(video,false);
				dataList.addAll(buildImageSimHash((List<Map<String, Object>>)obj2[0]));
				log.info("videolist size: " + video.size());*/

				hsOffshelfIds = (HashSet<String>)obj[1];
			}
		}
		//处理视频资讯

		HashMap<String, Object> mResult = new HashMap<String, Object>();
		mResult.put("onshelf", dataList);
		mResult.put("offshelf", hsOffshelfIds);
		mResult.put("editor", getTeditorDatas(arrContentId));
		return mResult;
	}
	public Map<String, Object> listInfos(String time_after, String limit_count, boolean bOversea, boolean fIntegrityCheck)
	{
		Map<String, Object> mResult = null;
		HashSet<String> hsContentId = new HashSet<String>();
		String order = null;
		String limit = " LIMIT 0, " + limit_count;
		String sql   = null;
		if (bOversea) {
			sql = "select item_id as content_id, update_time from t_content_group_item_mapping GIM where update_time>=" + StringUtils.quote(time_after);
			order = " ORDER BY GIM.update_time";
		} else {
			sql = "select content_id from t_signal S where S.update_time>=" + StringUtils.quote(time_after);
			order = " ORDER BY S.update_time";
		}

		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(sql+order+limit);
		if (dataList.size() > 0)
		{
			Map<String, Object> lastOne = dataList.get(dataList.size()-1);
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

					dataList = jdbcTemplateObject.queryForList(sql+order+" LIMIT 0, "+String.valueOf(nNewLimit));
					log.info("SQL: "+sql+order+" LIMIT 0, "+String.valueOf(nNewLimit));
					// Need to check content state to do further action. 

					if (dataList.size() > 0)
					{
						lastOne = dataList.get(dataList.size()-1);
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
			for (Map<String, Object> item : dataList)
			{
				hsContentId.add((String)item.get("content_id"));
			}
			sql = "select content_id from t_editor where update_time >= " + StringUtils.quote(time_after) + " and update_time < " + StringUtils.quote(update_time) + " and status=0";
			dataList = jdbcTemplateObject.queryForList(sql);
			for (Map<String, Object> item : dataList)
			{
				hsContentId.add((String)item.get("content_id"));
			}
			//added by genix.li, to monitor table t_content_comic
			if (bOversea) {
				sql = "select distinct content_id from t_content_comic where update_time >= " + StringUtils.quote(time_after) + " and update_time < " + StringUtils.quote(update_time);
				dataList = jdbcTemplateObject.queryForList(sql);
				for (Map<String, Object> item : dataList)
				{
					hsContentId.add((String)item.get("content_id"));
				}
			}
			mResult = listInfos((String[])hsContentId.toArray(new String[0]), bOversea, fIntegrityCheck);
			this.last_update_count = hsContentId.size();
			this.last_update_time = update_time;
			this.hsModifiedGroup.clear();
			this.hsModifiedGroup.addAll(getDistinctGroupId(hsContentId));
			log.info("preparing data count=" + last_update_count + "/" + hsContentId.size() +" time_after=" + time_after + " update_time=" + update_time + ":" + hsContentId);
		}
		return mResult;
	}

	public boolean isHighImpressionCheckInfo(String contentId)
	{
		boolean fHighImpressionChecked = false;
		String sql = "select content_id, operate_time from t_content_edit_log where edit_type=62 and content_id='" + contentId + "'";
		log.info("isHighImpressionCheckInfo SQL:" + sql);
		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(sql);
		if (!CollectionUtils.isEmpty(dataList))
		{
			fHighImpressionChecked = (dataList.size() > 0);
		}
		return fHighImpressionChecked;
	}
	public Map<String, Object> listHighImpressionCheckInfo(String time_after, boolean bOversea)
	{
		String sql = "select content_id, operate_time from t_content_edit_log where edit_type=62 and operate_time >='" + time_after + "' order by operate_time";
		log.info("listHighImpressionCheckInfo SQL:" + sql);
		HashSet<String> hsContentId = new HashSet<String>();
		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(sql);
		String strLastUpdateTime = time_after;
		if (!CollectionUtils.isEmpty(dataList))
		{
			for (Map<String, Object> mData : dataList)
			{
				String contentId = (String)mData.get("content_id");
				hsContentId.add( contentId );
			}

			Map<String, Object> mLastData = (Map<String, Object>)dataList.get(dataList.size() - 1);
			strLastUpdateTime = ((Timestamp)mLastData.get("operate_time")).toString();
		}

		HashMap<String, Object> mResult = new HashMap<String, Object>();
		mResult.putAll( listInfos((String[])hsContentId.toArray(new String[0]), bOversea, false) );
		mResult.put("lastUpdateTime", strLastUpdateTime);
		return mResult;
	}

	private Object[] doIntegrityCheck(List<Map<String, Object>> dataList, boolean integrityCheck)
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
				if (1 == content_state)
				{
					if (integrityCheck)
					{
						//完成性检查
						String srcJson = (String)item.get("list_images");
						List<ImagesEntry> imagesModels = new ArrayList<ImagesEntry>();
						List<JSONObject> imagesObjects = FastJsonConverter.readValue(List.class, srcJson);
						for(JSONObject imagesObject : imagesObjects)
						{
							String imagesSrc = imagesObject.toString();
							ImagesEntry imagesModel = FastJsonConverter.readValue(ImagesEntry.class, imagesSrc);
							imagesModels.add(imagesModel);
						}
						fEffective = feederCheckUtil.checkLink(contentId, imagesModels); 
					}
					else
					{
						fEffective = true;
					}
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

	public List<EditorTableEntry> getTeditorDatas(String time_after) 
	{
		String order = " ORDER BY update_time";
		String limit = " LIMIT 0, 1000";
		String SQL = "SELECT * FROM t_editor WHERE update_time >= '" + time_after + "' and status=0";
		List<EditorTableEntry> info_list = jdbcTemplateObject.query(SQL+order+limit, new InfoMapperTEditor());
		return info_list;
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
			String SQL = "SELECT * FROM t_editor WHERE content_id in (" + strContentIdList + ") and status=0";
			List<EditorTableEntry> info_list = jdbcTemplateObject.query(SQL+order, new InfoMapperTEditor());
			for (EditorTableEntry entry : info_list)
			{
				String contentId = entry.getContentId();
				mData.put(contentId, entry);
			}
		}
		return mData;
	}
	public List<EditorTableEntry> getTeditorDatas(Set<String> hsContentId) 
	{
		List<EditorTableEntry> info_list = null;
		if (!CollectionUtils.isEmpty(hsContentId))
		{
			String[] arrContentId = (String[])hsContentId.toArray(new String[0]);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arrContentId.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(StringUtils.quote(arrContentId[i]));
			}
			String strContentIdList = sb.toString();
			String order = " ORDER BY update_time";
			String SQL = "SELECT * FROM t_editor WHERE content_id in (" + strContentIdList + ") and status=0";
			info_list = jdbcTemplateObject.query(SQL+order, new InfoMapperTEditor());
		}
		return info_list;
	}

	public List<SourceSubscriptionEntry> getSourceSubscriptionInfos()
	{
		String SQL = "select * from t_source_subscription";
		List<SourceSubscriptionEntry> info_list = jdbcTemplateObject.query(SQL, new InfoMapperSourceSubscription());
		return info_list;
	}

	public List<CategoryInfo> listCategoryInfoForOversea()
	{
		String SQL = "select C.id, CE.version, C.category_name, C.intro, C.create_time, CE.content_type, CE.expiry_hour from t_category C join t_category_expiry CE on CE.category_id=C.id order by CE.content_type, C.id";
		List<CategoryInfo> info_list = jdbcTemplateObject.query(SQL, new InfoMapperCategory());
		return info_list;
	}
	public List<CategoryInfo> listCategoryInfo(int category_system_version)
	{
		String SQL = "SELECT * FROM t_category where version=" + category_system_version + " ORDER BY id";
		List<CategoryInfo> info_list = jdbcTemplateObject.query(SQL, new InfoMapperCategory());

		return info_list;
	}

	public Map<String,Integer> getContentEditorLog(Set<String> removableIds,Integer editType){
		Map<String,Integer> returnMap = new HashMap<String,Integer>();
		StringBuffer sql = new StringBuffer("select id,content_id,edit_type,operator,operate_time from t_content_edit_log where content_id in(");
		for(String removableId : removableIds){
			sql.append("'"+removableId+"',");
		}
		sql.append(")");
		sql.deleteCharAt(sql.lastIndexOf(","));

		sql.append(" and edit_type = "+editType);
		List<ContentEditorLog> contentEditorLogs = jdbcTemplateObject.query(sql.toString(), new InfoMapperContentEditorLog());
		for(ContentEditorLog contentEditorLog : contentEditorLogs){
			returnMap.put(contentEditorLog.getContentId(), contentEditorLog.getEditorType());
		}
		return returnMap;
	}

	public List<Map<String,String>> listEditorFlags(Map<String,String> contentIds, boolean bOversea)
	{
		Set<String> contentIdSets = contentIds.keySet();
		if (contentIdSets == null || contentIdSets.size() == 0 )
		{
			log.info("no flag be updated ...");
			return null;
		}

		String[] arrContentId = (String[])contentIdSets.toArray(new String[0]);
		List<Map<String, Object>> lists = getInfoByIDList(arrContentId, bOversea);
		List<Map<String,String>> hdfsEditorFlags = new JsonInfoMapperHDFS().mapIntoFeederInfoList(lists);
		log.info("query editor flag size ="+hdfsEditorFlags.size()+" by sql...");
		for (Map<String,String> hdfsEditorMap : hdfsEditorFlags)
		{
			String contentId = hdfsEditorMap.get("content_id");
			String flag = contentIds.get(contentId);
			log.info("contentId = "+contentId+",flag ="+flag);
			hdfsEditorMap.put("flag", flag == null ? "" : flag);
		}
		return hdfsEditorFlags;
	}

	public Set<String> getHighQualityNews(){
		Set<String> contentIds = new HashSet<String>();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		long datetime = new Date().getTime() - (86400000l * 40);
		StringBuffer sql = new StringBuffer("select group_id from t_high_quality_news where check_status in(3,5) and update_time > ");
		sql.append("'");
		sql.append(fmt.format(datetime));
		sql.append("'");
		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(sql.toString());
		log.info("getHighQualityNews SQL: " + sql.toString());
		if (!CollectionUtils.isEmpty(dataList))
		{
			for (Map<String, Object> mData : dataList)
			{
				String contentId = (String)mData.get("group_id");
				contentIds.add( contentId );
			}
		}
		return contentIds;
	}

	public double getExploreCtrByPublisherAndCategory(String product,String publisher,String category){
	    double ctr = 0.0;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date time = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String timestamp = fmt.format(calendar.getTime());
        StringBuffer sql = new StringBuffer("select avg_ctr from t_explore_publisher_category_ctr where timestamp>=");
        sql.append("'");
        sql.append(timestamp);
        sql.append("'");
        sql.append(" and product=");
        sql.append("'");
        sql.append(product);
        sql.append("'");
        sql.append(" and publisher=");
        sql.append("'");
        sql.append(StringEscapeUtils.escapeSql(publisher));
        sql.append("'");
        sql.append(" and category=");
        sql.append("'");
        sql.append(category);
        sql.append("'");
        sql.append(" order by timestamp desc limit 1");
        List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(sql.toString());
        log.info("getExploreCtrByPublisherAndCategory SQL: " + sql.toString());
        if (!CollectionUtils.isEmpty(dataList))
        {
            for (Map<String, Object> mData : dataList)
            {
                ctr = (Double) mData.get("avg_ctr");
            }
        }
        return ctr;
    }

	/**
	 *
	 * @return  根据t_index 查表名  然后根据表名查视频contentId
	 */
    public List<Map<String,Object>> getVideoContent(){
		String videoSql="SELECT * FROM t_content_%s C INNER JOIN t_signal_%s S WHERE C.`content_id`=S.`content_id`  " +
				"AND content_type=2 AND source='Risapp' AND state=1 ";
		///*String videoSql="select * from t_content_%s where content_type=1  and state=1";
		List<String> tableNames=Arrays.asList("2019_11","2019_12","2020_01","2020_02","2020_03");
		List<Map<String,Object>> videoContentId=new LinkedList<>();
		if (tableNames!=null&&tableNames.size()>0){
			for (String date: tableNames){
				String sql=String.format(videoSql,date,date);
				videoContentId.addAll(jdbcTemplateObject.queryForList(sql));
			}
		}

		return videoContentId;
	}

	public static void main(String args[]){

		Properties properties= new Properties();
		DriverManagerDataSource dataSource=new DriverManagerDataSource();
		dataSource.setUrl("jdbc:mysql://192.168.1.180:3306/cms_xz?useUnicode=true&amp;characterEncoding=UTF8");
		dataSource.setUsername("root");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setPassword("root");
		ClientJDBCTemplate clientJDBCTemplate=ClientJDBCTemplate.getInstance();
		clientJDBCTemplate.setDataSource(dataSource);
		List<Map<String,Object>> videos=clientJDBCTemplate.getVideoContent();
		System.out.println(videos.size());
	}

}