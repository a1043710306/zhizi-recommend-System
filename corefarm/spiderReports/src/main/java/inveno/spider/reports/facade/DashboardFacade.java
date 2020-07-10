package inveno.spider.reports.facade;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import inveno.spider.reports.Constants;
import inveno.spider.reports.task.PeriodicalProductPublishedAmountStatistic;
import inveno.spider.reports.util.GenerateTableName;
import tw.qing.lwdba.DBRow;
import tw.qing.lwdba.SQLUtil;
import tw.qing.lwdba.TransSQLExecutor;
import tw.qing.util.DateUtil;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DashboardFacade extends inveno.spider.common.facade.AbstractDBFacade
{
	private static final Logger log = Logger.getLogger(DashboardFacade.class);
	private static HashMap<String, DashboardFacade> mInstance = new HashMap<String, DashboardFacade>();
	private String dbName = null;

	public static final String  MONITOR_TYPE_DAILY  = "daily";
	public static final String  MONITOR_TYPE_HOURLY = "hourly";

	public static synchronized DashboardFacade getInstance()
	{
		return getInstance(DBNAME_DEFAULT);
	}
	public static synchronized DashboardFacade getInstance(String _dbName)
	{
		DashboardFacade instance = mInstance.get(_dbName);
		if (instance == null)
		{
			try
			{
				instance = new DashboardFacade(_dbName);
			}
			catch (Exception e)
			{
				log.fatal("{}", e);
			}
		}
		return instance;
	}
	private DashboardFacade(String _dbName) throws java.sql.SQLException, java.lang.ClassNotFoundException
	{
		super(_dbName);
		dbName = _dbName;
	}
	public ArrayList listCrawlerArticleCountByProduct(String[] source, Date startTime, Date endTime, String sqlDatePattern)
	{
		ArrayList alResult = null;
		try
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
			String sql = sqlManager.getSQL("dashboard.listCrawlerArticleCountByProduct", new Object[]{sb, startTime, endTime, sqlDatePattern, currentTableName, beforeTableName});
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
	public ArrayList listCrawlerArticleCount(String[] source, Date startTime, Date endTime, String sqlDatePattern)
	{
		ArrayList alResult = null;
		try
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
			String sqlFormat = Constants.get("dashboard.listCrawlerArticleCount");
			String startTimeStr = DateUtil.dateToString(startTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_HOURLY);
			String endTimeStr = DateUtil.dateToString(endTime, PeriodicalProductPublishedAmountStatistic.DATE_PATTERN_HOURLY);
			String sql = String.format(sqlFormat, sqlDatePattern, currentTableName, startTimeStr, endTimeStr, sb.toString(), beforeTableName, startTimeStr, endTimeStr, sb.toString());
//			String sql = sqlManager.getSQL("dashboard.listCrawlerArticleCount", new Object[]{sb, startTime, endTime, sqlDatePattern, currentTableName, beforeTableName});
			alResult = sqlQueryRows(sql);
			long queryEnd = System.currentTimeMillis();
			log.info("sql: " + sql + "|query count:" + alResult.size() + " |spent times: " + (queryEnd - queryStart) + "ms");
		}
		catch (Exception e)
		{
			log.fatal("", e);
			alResult = new ArrayList();
		}
		return alResult;
	}
	public void printArticleCountSQL(ArrayList alData) throws SQLException
	{
		try
		{
			String[] primaryKeyFields = new String[]{"timestamp", "type", "source"};
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				DBRow dr = new DBRow("t_monitor_crawler", primaryKeyFields);
				for (int j = 0; j < primaryKeyFields.length; j++)
				{
					dr.setColumn(primaryKeyFields[j], mData.get(primaryKeyFields[j]));
				}
				String sql = dr.toQueryString();
				ArrayList alExistData = sqlQueryRows(sql);
				dr = new DBRow("t_monitor_crawler", new String[]{"id"});
				if (alExistData.size() > 0)
				{
					HashMap mExistData = (HashMap)alExistData.get(0);
					mExistData.putAll(mData);
					dr.setRow( mExistData );
					sql = dr.toUpdateString();
				}
				else
				{
					dr.setRow( mData );
					sql = dr.toInsertString();
				}
				System.out.println(sql);
			}
		}
		catch (Exception e)
		{
			log.fatal("[printArticleCountSQL]", e);
		}
	}
	public void reportData(String tableName, String[] primaryKeyFields, ArrayList alData) throws SQLException
	{
		TransSQLExecutor tse = null;
		log.info("reportData count:" + alData.size());
		try
		{
			tse = new TransSQLExecutor(dbName);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				DBRow dr = new DBRow(tableName, primaryKeyFields);
				for (int j = 0; j < primaryKeyFields.length; j++)
				{
					dr.setColumn(primaryKeyFields[j], mData.get(primaryKeyFields[j]));
				}
				String sql = dr.toQueryString();
				log.info(sql);
				dr = new DBRow(tableName, new String[]{"id"});
				ArrayList alExistData = executeQueryRows(sql, tse);
				if (alExistData.size() > 0)
				{
					HashMap mExistData = (HashMap)alExistData.get(0);
					mExistData.putAll(mData);
					dr.setRow( mExistData );
					sql = dr.toUpdateString();
				}
				else
				{
					dr.setRow( mData );
					sql = dr.toInsertString();
				}
				log.info(sql);
				tse.executeUpdate(sql);
			}
			tse.commit();
		}
		catch (Exception e)
		{
			log.fatal("reportData error:", e);
			tse.rollback();
		}
		finally
		{
			tse.close();
		}
	}
	public void reportArticleCount(ArrayList alData) throws SQLException
	{
		TransSQLExecutor tse = null;
		try
		{
			String[] primaryKeyFields = new String[]{"timestamp", "type", "source", "source_type", "country", "language", "product_id"};
			tse = new TransSQLExecutor(dbName);
			for (int i = 0; i < alData.size(); i++)
			{
				HashMap mData = (HashMap)alData.get(i);
				DBRow dr = new DBRow("t_monitor_crawler", primaryKeyFields);
				for (int j = 0; j < primaryKeyFields.length; j++)
				{
					dr.setColumn(primaryKeyFields[j], mData.get(primaryKeyFields[j]));
				}
				String sql = dr.toQueryString();
				log.info(sql);
				dr = new DBRow("t_monitor_crawler", new String[]{"id"});
				ArrayList alExistData = executeQueryRows(sql, tse);
				if (alExistData.size() > 0)
				{
					HashMap mExistData = (HashMap)alExistData.get(0);
					mExistData.putAll(mData);
					dr.setRow( mExistData );
					sql = dr.toUpdateString();
				}
				else
				{
					dr.setRow( mData );
					sql = dr.toInsertString();
				}
				log.info(sql);
				tse.executeUpdate(sql);
			}
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
