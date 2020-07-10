package inveno.spider.reports.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import inveno.spider.common.util.JsonUtils;
import inveno.spider.reports.util.GenerateTableName;
import inveno.spider.reports.util.SourceFeedFilter;
import tw.qing.lwdba.SQLUtil;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class ReportFacade extends inveno.spider.common.facade.AbstractDBFacade
{
	private static final Logger log = Logger.getLogger(ReportFacade.class);
	private static HashMap<String, ReportFacade> mInstance = new HashMap<String, ReportFacade>();
	private String dbName = null;

	public static synchronized ReportFacade getInstance()
	{
		return getInstance("default");
	}
	public static synchronized ReportFacade getInstance(String _dbName)
	{
		ReportFacade instance = mInstance.get(_dbName);
		if (instance == null)
		{
			try
			{
				instance = new ReportFacade(_dbName);
			}
			catch (Exception e)
			{
				log.fatal("{}", e);
			}
		}
		return instance;
	}
	private ReportFacade(String _dbName) throws java.sql.SQLException, java.lang.ClassNotFoundException
	{
		super(_dbName);
		dbName = _dbName;
	}

	/**
	 * @param source specified source name like: "头条", "微信" ... etc
	 * @return HashMap <K=channel_category_id, V=system_category_id>>
	 */
	public HashMap<String, String> getSystemCategoryIdMappingBySource(String source)
	{
		HashMap<String, String> mCategory = new HashMap<String, String>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getCategoryMappingBySource", source);
			ArrayList alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int i = 0; i < alResult.size(); i++)
			{
				HashMap mData = (HashMap)alResult.get(i);
				String channel_category_id = (null == mData.get("channel_category_id")) ? null : mData.get("channel_category_id").toString();
				String system_category_id  = (null == mData.get("category_id")) ? null : mData.get("category_id").toString();
				if (system_category_id != null)
					mCategory.put(channel_category_id, system_category_id);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mCategory;
	}

	/**
	 * @param source specified source name like: "头条", "微信" ... etc
	 * @return HashMap <K=channel_category_id, V=system_category_name>>
	 */
	public HashMap<String, String> getSystemCategoryNameMappingBySource(String source)
	{
		HashMap<String, String> mCategory = new HashMap<String, String>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getCategoryMappingBySource", source);
			ArrayList alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int i = 0; i < alResult.size(); i++)
			{
				HashMap mData = (HashMap)alResult.get(i);
				String channel_category_id = (null == mData.get("channel_category_id")) ? null : mData.get("channel_category_id").toString();
				String system_category_name  = (null == mData.get("category_id")) ? null : mData.get("category_name").toString();
				if (system_category_name != null)
					mCategory.put(channel_category_id, system_category_name);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mCategory;
	}

	/**
	 * @param source specified source name like: "头条", "微信" ... etc
	 * @return HashMap <K=channel_category_id, V=channel_category_name>>
	 */
	public HashMap<String, String> getChannelCategoryMappingBySource(String source)
	{
		HashMap<String, String> mCategory = new HashMap<String, String>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getCategoryMappingBySource", source);
			ArrayList alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int i = 0; i < alResult.size(); i++)
			{
				HashMap mData = (HashMap)alResult.get(i);
				String channel_category_id = (null == mData.get("channel_category_id")) ? null : mData.get("channel_category_id").toString();
				String channel_category_name  = (null == mData.get("channel_category_name")) ? null : mData.get("channel_category_name").toString();
				if (channel_category_name != null)
					mCategory.put(channel_category_id, channel_category_name);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mCategory;
	}

	public ArrayList getAllRssInfo(String source)
	{
		ArrayList alResult = null;
		try
		{
			String sql = sqlManager.getSQL("report.listSourceFeeds", source);
			alResult = sqlQueryRows(sql);
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public HashMap<String, HashMap> getRssReleaseFlag(String[] arrRssId)
	{
		return getRssReleaseFlag(arrRssId, -1);
	}
	public HashMap<String, HashMap> getRssReleaseFlag(String[] arrRssId, int firmId)
	{
		HashMap<String, HashMap> mResult = new HashMap<String, HashMap>();
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sbRssList = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sbRssList.append(",");
					sbRssList.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = (firmId < 0) ? sqlManager.getSQL("report.getRssReleaseFlag", sbRssList)
										  : sqlManager.getSQL("report.getRssReleaseFlagByFirmId", sbRssList, firmId);
				ArrayList alData = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
				for (int i = 0; alData != null && i < alData.size(); i++)
				{
					HashMap mData = (HashMap)alData.get(i);
					mResult.put( String.valueOf(mData.get("rss_id")), mData);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	public HashMap<String, HashMap<String, String>> getRssInfoBySource(String source)
	{
		HashMap<String, HashMap<String, String>> mResult = new HashMap<String, HashMap<String, String>>();
		try
		{
			String sql = sqlManager.getSQL("report.listSourceFeedsBySource", source);
			ArrayList alData = sqlQueryRows(sql);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				if (mData.get("source_id") == null)
					continue;
				mResult.put(String.valueOf(mData.get("source_id")), mData);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	public HashMap<String, HashMap<String, String>> getRssInfoBySourceFilter(String source, SourceFeedFilter filter)
	{
		HashMap<String, HashMap<String, String>> mResult = new HashMap<String, HashMap<String, String>>();
		try
		{
			int firmId = filter.getFirmId();
			String whereClause = filter.toClauseString(getDatabaseType());
			String sql = (whereClause == null) ? sqlManager.getSQL("report.listFirmSourceFeedsBySource", source, firmId)
											   : sqlManager.getSQL("report.listFirmSourceFeedsBySourceFilter", source, new StringBuffer(whereClause), firmId);
			ArrayList alData = sqlQueryRows(sql);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				mResult.put(String.valueOf(mData.get("source_id")), mData);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	/*
	public HashMap<String, HashMap<String, String>> getRssInfoBySourceLevel(String source, int level)
	{
		HashMap<String, HashMap<String, String>> mResult = new HashMap<String, HashMap<String, String>>();
		try
		{
			HashMap<String, String> mLevel = (HashMap<String, String>)mSourceLevel.get(level);
			String[] s = TextUtil.getStringList((String)mLevel.get("quality"));
			StringBuffer quality = new StringBuffer();
			for (int i = 0; i < s.length; i++)
			{
				if (i > 0)
					quality.append(",");
				quality.append(SQLUtil.getSQLValue(s[i], getDatabaseType()));
			}
			StringBuffer adult_score = new StringBuffer((String)mLevel.get("adult_score"));
			StringBuffer advertisement_score = new StringBuffer((String)mLevel.get("advertisement_score"));
			StringBuffer audience_scale = new StringBuffer((String)mLevel.get("audience_scale"));
			String sql = sqlManager.getSQL("report.listSourceFeedsBySourceLevel", new Object[]{source, quality, adult_score, advertisement_score, audience_scale});
			ArrayList alData = sqlQueryRows(sql);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				mResult.put(String.valueOf(mData.get("source_id")), mData);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	*/
	public ArrayList listSourceFeeds()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.listSourceFeeds");
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	
	public int getDailyCrawledActiveSourceFeedCount(String source, String date)
	{
		int nActiveSourceFeedCount = -1;
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("report.getDailyCrawledActiveSourceFeedCount", source, date, currentTableName, beforeTableName);
			ArrayList alResult = sqlQueryRows(sql);
			HashMap mResult = (HashMap)alResult.get(0);
			nActiveSourceFeedCount = PrimitiveTypeUtil.getInt(mResult.get("source_feeds_count"));
			long queryEnd = System.currentTimeMillis();
			log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return nActiveSourceFeedCount;
	}
	public int getDailyCrawledArticleCount(String source, String date)
	{
		int nArticleCount = -1;
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("report.getDailyCrawledArticleCount", source, date, currentTableName, beforeTableName);
			ArrayList alResult = sqlQueryRows(sql);
			HashMap mResult = (HashMap)alResult.get(0);
			nArticleCount = PrimitiveTypeUtil.getInt(mResult.get("article_count"));
			long queryEnd = System.currentTimeMillis();
			log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return nArticleCount;
	}
	public ArrayList getDailyCandidateArticle(String[] arrRssId, String date)
	{
		ArrayList alResult = null;
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = sqlManager.getSQL("report.getDailyCandidateArticle", sb, date);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return (alResult == null) ? new ArrayList() : alResult;
	}
	public ArrayList getDailyPublishedArticle(String[] arrRssId, String date)
	{
		ArrayList alResult = null;
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sbRssList = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sbRssList.append(",");
					sbRssList.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = sqlManager.getSQL("report.getDailyPublishedArticle", sbRssList, date);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return (alResult == null) ? new ArrayList() : alResult;
	}
	public ArrayList getDailyCandidateArticleByTimeRange(String[] arrRssId, Date startTime, Date endTime)
	{
		ArrayList alResult = null;
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = sqlManager.getSQL("report.getDailyCandidateArticleByTimeRange", sb, startTime, endTime);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return (alResult == null) ? new ArrayList() : alResult;
	}
	public ArrayList getDailyPublishedArticleByTimeRange(String[] arrRssId, Date startTime, Date endTime)
	{
		ArrayList alResult = null;
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sbRssList = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sbRssList.append(",");
					sbRssList.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = sqlManager.getSQL("report.getDailyPublishedArticleByTimeRange", sbRssList, startTime, endTime);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return (alResult == null) ? new ArrayList() : alResult;
	}
	public HashMap<String, HashMap<Object, Object>> getDailyPublishedCount(String[] arrRssId, String date)
	{
		HashMap<String, HashMap<Object, Object>> mPublishCount = new HashMap<String, HashMap<Object, Object>>();
		try
		{
			if (arrRssId != null && arrRssId.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arrRssId.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(SQLUtil.getSQLValue(arrRssId[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String sql = sqlManager.getSQL("report.getDailyPublishedCount", sb, date);
				ArrayList alData = sqlQueryRows(sql);
				for (int i = 0; alData != null && i < alData.size(); i++)
				{
					HashMap mData = (HashMap)alData.get(i);
					mPublishCount.put(String.valueOf(mData.get("rss_id")), mData);
				}
				long queryEnd = System.currentTimeMillis();
				log.info("query <" + sql + "> spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mPublishCount;
	}
	
	public ArrayList getArticleSource(String crawlerType)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getArticleSourceCrawlerType",crawlerType);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	
	public ArrayList getArticleSource()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getArticleSource");
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	
	public ArrayList listSourceInfo()
	{
		return listSourceInfo(null);
	}
	public ArrayList listSourceInfo(String[] source)
	{
		ArrayList alResult = null;
		try
		{
			boolean bHasSource = false;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; source != null && i < source.length; i++)
			{
				if (!bHasSource)
					bHasSource = true;
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(source[i], getDatabaseType()));
			}

			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = (!bHasSource) ? sqlManager.getSQL("report.listSourceInfo", currentTableName, beforeTableName)
									   : sqlManager.getSQL("report.listSourceInfoBySource", sb, currentTableName, beforeTableName);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public HashSet<String> getDailyTransitArticle(String dateString)
	{
		HashSet<String> hsResult = new HashSet<String>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getDailyTransitArticle", dateString);
			ArrayList alData = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				hsResult.add( String.valueOf(mData.get("news_id")) );
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return hsResult;
	}
	public ArrayList getDailyCrawledArticle(String[] source, String dateString)
	{
		ArrayList alResult = new ArrayList();
		try
		{
			if (source != null)
			{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < source.length; i++)
				{
					if (i > 0)
						sb.append(",");
					sb.append(SQLUtil.getSQLValue(source[i], getDatabaseType()));
				}
				long queryStart = System.currentTimeMillis();
				String currentTableName = GenerateTableName.generatorTableName(false);
				String beforeTableName = GenerateTableName.generatorTableName(true);
				String sql = sqlManager.getSQL("report.getDailyCrawledArticle", sb, dateString, currentTableName, beforeTableName);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	/**
	 * @param source specified source name like: "头条", "微信" ... etc
	 * @return HashMap <K=category_version, V=HashMap<K=category_id, V=article_count>>
	 */
	public HashMap<String, HashMap<String, Integer>> getArticleCountGroupByCategory(String source)
	{
		HashMap<String, HashMap<String, Integer>> mVersionStatistic = new HashMap<String, HashMap<String, Integer>>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("report.listContentBySource", source, currentTableName, beforeTableName);
			ArrayList alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			Gson gson = new Gson();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int j = 0; j < alResult.size(); j++)
			{
				HashMap mData = (HashMap)alResult.get(j);
				String categories = (String)mData.get("categories");
				Map<Object, Object> mCategories = (Map<Object, Object>) JsonUtils.toJavaObject(gson.fromJson(categories, JsonElement.class));
				for (Map.Entry<Object, Object> entry : mCategories.entrySet())
				{
					String version = (String)entry.getKey();
					HashMap<String, Integer> mStatistic = mVersionStatistic.get(version);
					if (null == mStatistic)
					{
						mStatistic = new HashMap<String, Integer>();
						mVersionStatistic.put(version, mStatistic);
					}
					ArrayList alCategory = (ArrayList)entry.getValue();
					for (int i = 0; i < alCategory.size(); i++)
					{
						String categoryId = ((HashMap)alCategory.get(i)).get("category").toString();
						int currentCount = (mStatistic.get(categoryId) == null) ? 0 : mStatistic.get(categoryId).intValue();
						currentCount++;
						mStatistic.put(categoryId, currentCount);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mVersionStatistic;
	}
	public ArrayList listCrawlerContentArticleCount(String date)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.listCrawlerContentArticleCount", date);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public ArrayList listPublishContentArticleCount(String date)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.listPublishContentArticleCount", date);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public ArrayList getArticleCountByTimeRange(String[] source,String crawlerType, Date startTime, Date endTime)
	{
		ArrayList alResult = new ArrayList();
		try
		{
				long queryStart = System.currentTimeMillis();
				String currentTableName = GenerateTableName.generatorTableName(false);
				String beforeTableName = GenerateTableName.generatorTableName(true);
				String startTimeStr = DateUtil.dateToString(startTime, "yyyy-MM-dd HH:mm:ss");
				String endTimeStr = DateUtil.dateToString(endTime, "yyyy-MM-dd HH:mm:ss");
				String sqlFormat = "SELECT result.crawler_type, result.source, COUNT(1) save_count FROM (SELECT crawler_type, source FROM %s WHERE discovery_time >= '%s' and discovery_time < '%s' and crawler_type = '%s' union all SELECT crawler_type, source FROM %s WHERE discovery_time >= '%s' and discovery_time < '%s' and crawler_type = '%s') result GROUP BY  result.source";
				String sql = String.format(sqlFormat, currentTableName, startTimeStr, endTimeStr,crawlerType, beforeTableName, startTimeStr, endTimeStr,crawlerType);
				log.info("source crawler static get sql :" + sql);
				alResult = sqlQueryRows(sql);
				long queryEnd = System.currentTimeMillis();
				log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public ArrayList getLanguageArticleCountByTimeRange(Date startTime, Date endTime)
	{
		ArrayList alResult = new ArrayList();
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String startTimeStr = DateUtil.dateToString(startTime, "yyyy-MM-dd HH:mm:ss");
			String endTimeStr = DateUtil.dateToString(endTime, "yyyy-MM-dd HH:mm:ss");
			String sqlFormat = "SELECT result.country, result.language, result.source, result.state, COUNT(1) save_count FROM (SELECT country, language, source, state FROM %s WHERE discovery_time >= '%s' and discovery_time < '%s' union all SELECT country, language, source, state FROM %s WHERE discovery_time >= '%s' and discovery_time < '%s') result GROUP BY result.country, result.language, result.source, result.state";
			String sql = String.format(sqlFormat, currentTableName, startTimeStr, endTimeStr, beforeTableName, startTimeStr, endTimeStr);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public ArrayList listFilteredReason()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.listFilteredReason");
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return alResult;
	}
	public HashMap<Integer, HashSet<Integer>> getFirmSource()
	{
		HashMap<Integer, HashSet<Integer>> mResult = new HashMap<Integer, HashSet<Integer>>();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("report.getFirmSource");
			ArrayList alData = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				int firmId = PrimitiveTypeUtil.getInt( mData.get("firm_id") );
				int rssId  = PrimitiveTypeUtil.getInt( mData.get("rss_id") );
				HashSet<Integer> hsRssList = mResult.get(firmId);
				if (hsRssList == null)
				{
					hsRssList = new HashSet<Integer>();
					mResult.put(firmId, hsRssList);
				}
				hsRssList.add(rssId);
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	
}
