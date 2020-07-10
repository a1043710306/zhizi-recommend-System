package inveno.spider.reports.facade;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.Constants;
import inveno.spider.reports.task.PeriodicalProductPublishedAmountStatistic;
import inveno.spider.reports.util.GenerateTableName;
import inveno.spider.reports.util.JdbcTemplateFactory;
import tw.qing.lwdba.DBRow;
import tw.qing.lwdba.SQLUtil;
import tw.qing.lwdba.TransSQLExecutor;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class ContentFacade extends AbstractDBFacade
{
	private static final Logger log = Logger.getLogger(ContentFacade.class);
	private static HashMap<String, ContentFacade> mInstance = new HashMap<String, ContentFacade>();
	private String dbName = null;

	public static synchronized ContentFacade getInstance()
	{
		return getInstance("default");
	}
	public static synchronized ContentFacade getInstance(String _dbName)
	{
		ContentFacade instance = mInstance.get(_dbName);
		if (instance == null)
		{
			try
			{
				instance = new ContentFacade(_dbName);
			}
			catch (Exception e)
			{
				log.fatal("{}", e);
			}
		}
		return instance;
	}
	private ContentFacade(String _dbName) throws java.sql.SQLException, java.lang.ClassNotFoundException
	{
		super(_dbName);
		dbName = _dbName;
	}

	public HashMap getVersionCategoryMapping()
	{
		HashMap mResult = new HashMap();
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.getVersionCategoryMapping");
			ArrayList alData = sqlQueryRows(sql);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				mResult.put( PrimitiveTypeUtil.getInt(mData.get("category_id")), PrimitiveTypeUtil.getInt(mData.get("mapping_category_id")) );
			}
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	public ArrayList getContent(ArrayList<String> alContentId)
	{
		ArrayList alResult = null;
		try
		{
			StringBuffer sbNewsId = new StringBuffer();
			for (int i = 0; i < alContentId.size(); i++)
			{
				String contentId = (String)alContentId.get(i);
				if (i > 0)
					sbNewsId.append(",");
				sbNewsId.append(SQLUtil.getSQLValue(contentId, getDatabaseType()));
			}
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("content.listContent", sbNewsId, currentTableName, beforeTableName);
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
	public HashMap getChannelContent(int contentId)
	{
		HashMap mResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.getChannelContent", contentId);
			ArrayList alData = sqlQueryRows(sql);
			if (alData.size() > 0)
				mResult = (HashMap)alData.get(0);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	public HashMap getContent(int contentId)
	{
		HashMap mResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("content.getContent", String.valueOf(contentId), currentTableName,beforeTableName);
			ArrayList alData = sqlQueryRows(sql);
			if (alData.size() > 0)
				mResult = (HashMap)alData.get(0);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return mResult;
	}
	public String getLanguage(int news_id)
	{
		String result = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.getLanguage", news_id);
			ArrayList alData = sqlQueryRows(sql);
			if (alData.size() > 0)
			{
				HashMap mData = (HashMap)alData.get(0);
				result = (String)mData.get("rss_language");
			}
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
		return result;
	}
	public ArrayList listContentByTimeRange(Date startTime, Date endTime)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String startTimeStr = DateUtil.dateToString(startTime, "yyyy-MM-dd HH:mm:ss");
			String endTimeStr = DateUtil.dateToString(endTime, "yyyy-MM-dd HH:mm:ss");
			String sqlFormat = "select content_id, source, source_type, content_type, language, country, content_type, categories, firm_app, body_images_count, state, offshelf_code, offshelf_reason from %s where discovery_time>='%s' and discovery_time<'%s' union all select content_id, source, source_type, content_type, language, country, content_type, categories, firm_app, body_images_count, state, offshelf_code, offshelf_reason from %s where discovery_time>='%s' and discovery_time<'%s'";
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
	public ArrayList listContentByTurnDataInTimeRange(Date startTime, Date endTime, int state)
	{
		ArrayList alResult = null;
		try
		{
			log.info("listContentByTurnDataInTimeRange start...");
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String startTimeStr = DateUtil.dateToString(startTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_MINUTE);
			String endTimeStr = DateUtil.dateToString(endTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_MINUTE);
			String sqlFormat = Constants.get("content.listContentByTurnDataInTimeRange");
			String sql = String.format(sqlFormat, currentTableName, startTimeStr, endTimeStr, beforeTableName, startTimeStr, endTimeStr, state);
//			String sql = sqlManager.getSQL("content.listContentByTurnDataInTimeRange", new Object[]{startTime, endTime, state, currentTableName, beforeTableName});
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
	public ArrayList listContentByStateInTimeRange(Date startTime, Date endTime, int state)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sql = sqlManager.getSQL("content.listContentByStateInTimeRange", new Object[]{startTime, endTime, state, currentTableName, beforeTableName});
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
	public ArrayList listPublishContentByTurnDataInTimeRange(Date startTime, Date endTime)
	{
		return listContentByTurnDataInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_NORMAL);
	}
	public ArrayList listPublishContentInTimeRange(Date startTime, Date endTime)
	{
		return listContentByStateInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_NORMAL);
	}
	public ArrayList listOffshelfContentByTurnDataInTimeRange(Date startTime, Date endTime)
	{
		return listContentByTurnDataInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_OFFSHELF);
	}
	public ArrayList listOffshelfContentInTimeRange(Date startTime, Date endTime)
	{
		return listContentByStateInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_OFFSHELF);
	}
	public ArrayList listCheckingContentByTurnDataInTimeRange(Date startTime, Date endTime)
	{
		return listContentByTurnDataInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_AUDIT);
	}
	public ArrayList listCheckingContentInTimeRange(Date startTime, Date endTime)
	{
		return listContentByStateInTimeRange(startTime, endTime, inveno.spider.common.model.Content.STATE_AUDIT);
	}
	public ArrayList listPublishContentAfterAuditingByTimeRange(Date startTime, Date endTime)
	{
		return listContentAfterAuditingByStateTimeRange(startTime, endTime, 118, inveno.spider.common.model.Content.STATE_NORMAL);
	}
	public ArrayList listOffshelfContentAfterAuditingByTimeRange(Date startTime, Date endTime)
	{
		return listContentAfterAuditingByStateTimeRange(startTime, endTime, 117, inveno.spider.common.model.Content.STATE_OFFSHELF);
	}
	public ArrayList listCheckingContentAfterAuditingByTimeRange(Date startTime, Date endTime)
	{
		return listContentAfterAuditingByStateTimeRange(startTime, endTime, 51, inveno.spider.common.model.Content.STATE_AUDIT);
	}
	public ArrayList listContentAfterAuditingByStateTimeRange(Date startTime, Date endTime, int editType, int state)
	{
		ArrayList alResult = null;
		try
		{
			log.info("query listContentAfterAuditingByStateTimeRange start");
			long queryStart = System.currentTimeMillis();
			String currentTableName = GenerateTableName.generatorTableName(false);
			String beforeTableName = GenerateTableName.generatorTableName(true);
			String sqlFormat = Constants.get("content.listContentAfterAuditingByStateTimeRange");
			String startTimeStr = DateUtil.dateToString(startTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_MINUTE);
			String endTimeStr = DateUtil.dateToString(endTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_MINUTE);
			String sql = String.format(sqlFormat, currentTableName, startTimeStr, endTimeStr, beforeTableName, startTimeStr, endTimeStr, startTimeStr, editType, state);
//			String sql = sqlManager.getSQL("content.listContentAfterAuditingByStateTimeRange", new Object[]{startTime, endTime, editType, state, currentTableName, beforeTableName});
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
	public ArrayList listPublishContentInSlotRangeAfterAuditingByStateTimeRange(Date startTime, Date endTime)
	{
		return listContentInSlotRangeAfterAuditingByStateTimeRange(startTime, endTime, 118, inveno.spider.common.model.Content.STATE_NORMAL);
	}
	public ArrayList listOffshelfContentInSlotRangeAfterAuditingByStateTimeRange(Date startTime, Date endTime)
	{
		return listContentInSlotRangeAfterAuditingByStateTimeRange(startTime, endTime, 117, inveno.spider.common.model.Content.STATE_OFFSHELF);
	}
	public ArrayList listContentInSlotRangeAfterAuditingByStateTimeRange(Date startTime, Date endTime, int editType, int state)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			Object[] args = new Object[]{startTime, endTime, editType, state};
			String sql = sqlManager.getSQL("content.listContentInSlotRangeAfterAuditingByStateTimeRange", args);
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
	public ArrayList listTurnDataByTimeRange(Date startTime, Date endTime)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listTurnDataByTimeRange", startTime, endTime);
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
	public ArrayList listTurnData(int news_id)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listTurnData", news_id);
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
	public ArrayList listAllProduct()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listAllProduct");
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("listAllProduct error:", e);
			alResult = new ArrayList();
		}
		return alResult;
	}
	public ArrayList listOffshelfCode()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listOffshelfCode");
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
	public ArrayList listFirmMapping(int rssId)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listFirmApp", rssId);
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
	public ArrayList getSourceFeeds(String source, String channel)
	{
		return null;
	}
	public ArrayList listCategoryByVersion(int version)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listCategoryByVersion", version);
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
	public String getSQLValue(Object obj)
	{
		return SQLUtil.getSQLValue(obj, getDatabaseType());
	}
	public int getScenarioArticleCount(String language, HashMap<Integer, StringBuffer> mCriteriaCategory, Date publishTime, String sqlDateFormat)
	{
		int articleCount = 0;
		try
		{
			long queryStart = System.currentTimeMillis();
			int idx = 0;
			StringBuffer sbCriteria = new StringBuffer();
			for (Map.Entry<Integer, StringBuffer> entry : mCriteriaCategory.entrySet())
			{
				if (idx > 0)
					sbCriteria.append(" or ");
				int expiryHour = (Integer)entry.getKey();
				StringBuffer criteria = (StringBuffer)entry.getValue();
				sbCriteria.append("(");
				sbCriteria.append( sqlManager.getSQL("content.scenarioCategoryCriteria", criteria, publishTime, sqlDateFormat, expiryHour) );
				sbCriteria.append(")");
			}
			String sql = sqlManager.getSQL("content.getScenarioArticleCount", language, sbCriteria);
			ArrayList alData = sqlQueryRows(sql);
			if (alData.size() > 0)
			{
				HashMap mData = (HashMap)alData.get(0);
				articleCount = PrimitiveTypeUtil.getInt(mData.get("article_count"));
			}
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alData.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			//log.fatal("", e);
		}
		return articleCount;
	}
	public ArrayList listProductScenario()
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listProductScenario");
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
	public ArrayList listSourceFeeds(String crawlerType)
	{
		ArrayList alResult = null;
		try
		{
			long queryStart = System.currentTimeMillis();
			String sql = sqlManager.getSQL("content.listSourceFeeds",crawlerType);
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
	
	/**
	 * 获取下架文章的count
	 * @return
	 */
	public ArrayList getOffshelfArticleCount(String tableName,String startTime,String endTime){
		ArrayList alResult = null;
		try{
			long queryStart = System.currentTimeMillis();
			String sql = String.format("select count(1) static_count,offshelf_code,offshelf_reason, date_format(update_time, '%s') days,language from %s where state =3  and update_time >= '%s' and  update_time <'%s'  group by language,offshelf_code,days order by days asc;", "%Y-%m-%d 00:00:00",tableName,startTime,endTime);
			System.out.println(sql);
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("query count:" + alResult.size() + " spent times: " + (queryEnd - queryStart) + "ms");
		}catch(Exception e){
			log.fatal("", e);
		}
		return alResult;
	}
	
	
	public void save(HashMap<String,Object> object){
		String sql =String.format("insert into t_media_stat_daily(media,q_media_info,num,date)  values ('%s','%s','%s','%s')", object.get("media"),object.get("q_media_info"),object.get("num"),object.get("date")) ;
		int res = 0;
		System.out.println(sql);
		try {
			res = sqlUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(res);
	}
	
	/**
	 * 获取media 统计值
	 * @param info
	 * @return
	 */
	public long getMediaStatisticNum(String info,String date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(format.parse(date));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		calendar.add(Calendar.DAY_OF_MONTH,-1);
		String startTime = format.format(calendar.getTime());
		String [] params = info.split(",");
		String currentTableName = GenerateTableName.generatorTableName(false);
		String beforeTableName = GenerateTableName.generatorTableName(true);
		String sql = "select count(1) as num from ( " +"select content_id from "+beforeTableName + " where discovery_time>='"+startTime+"' and discovery_time<='" + date + "' and content like '%"+params[0]+"%' and  content like '%"+params[1]+"%'  union all "+"select content_id from "+currentTableName + " where discovery_time>='"+startTime+"' and discovery_time<='" + date + "' and content like '%"+params[0]+"%' and  content like '%"+params[1]+"%' ) as dbinfo ";
		System.out.println(sql);
		ArrayList rows = null;
		long num = 0;
		try {
			rows =  sqlQueryRows(sql);
			for(Object oneRows:rows){
				HashMap res = (HashMap)oneRows;
				num = (long)res.get("num");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return num;
	}
	
	public ArrayList getRecentSevenDaysMedia(String startDate,String endDate){
		String sql = "select date,max(case media when 'twitter' then num else 0 end) as twitter,max(case media when 'facebook' then num else 0 end)as facebook,max(case media when 'youtube' then  num else 0 end)as youtube,max(case media when 'instagram' then num else 0 end) as instagram from t_media_stat_daily where date>'"+startDate+"' and date <='"+endDate+"' group by date order by date desc";
		ArrayList rows = null;
		try {
			rows = sqlQueryRows(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rows;
	}
	
	
	public void executeUpdate(String sql) throws Exception
	{
	}
	public void updateData(HashMap mData) throws Exception
	{
		TransSQLExecutor tse = null;
		try
		{
			tse = new TransSQLExecutor(dbName);
			DBRow dr = new DBRow("t_content", new String[]{"content_id"});
			dr.setRow(mData);
			String sql = dr.toUpdateString();
			tse.executeUpdate(sql);
			dr = new DBRow("t_signal", new String[]{"content_id"});
			dr.setColumn("content_id", mData.get("content_id"));
			dr.setColumn("update_time", mData.get("update_time"));
			sql = dr.toUpdateString();
			tse.executeUpdate(sql);
			dr = new DBRow("t_editor", new String[]{"content_id"});
			dr.setColumn("content_id", mData.get("content_id"));
			dr.setColumn("update_time", mData.get("update_time"));
			sql = dr.toUpdateString();
			tse.executeUpdate(sql);
			dr = new DBRow("t_content_group_item_mapping", new String[]{"item_id"});
			dr.setColumn("content_id", mData.get("item_id"));
			dr.setColumn("update_time", mData.get("update_time"));
			sql = dr.toUpdateString();
			tse.executeUpdate(sql);
			tse.commit();
		}
		catch (Exception e)
		{
			log.fatal("", e);
			tse.rollback();
		}
		finally
		{
			tse.close();
		}
	}
}
