package inveno.spider.reports.task;

import java.io.IOException;
import java.util.*;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.sys.JsonStringManager;
import inveno.spider.reports.facade.ContentFacade;
import inveno.spider.reports.facade.DashboardFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.reports.util.SourceFeedFilter;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.google.gson.*;

import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.sys.StringManager;

//use httpclient 4.x
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by Genix.Li on 2016/08/06.
 *
 * Statistic published amount for each channel(firm_app)
 */
public class PeriodicalCumulativePublishedAmountStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(PeriodicalCumulativePublishedAmountStatistic.class);
	private static final Logger falconLogger = Logger.getLogger("inveno.spider.falcon");

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATE_PATTERN_MINUTE = "%Y-%m-%d %H:%i:00";
	public static final String SQL_DATE_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATE_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";
	public static final char   SEPARATER_SOURCE_TYPE   = '/';

	public static String ELASTICSEARCH_QUERY_URI = null;
	public static String ELASTICSEARCH_FIELD_CATEGORY = null;
	public static String ELASTICSEARCH_FIELD_LANGUAGE = null;
	public static String ELASTICSEARCH_FIELD_FIRMAPP  = null;

	private String targetTableName = "t_article_feeder";
	private boolean fUseUTCTime = false;
	private boolean fDebugMode = false;
	private boolean fPublishByTContent = true;
	private boolean fFalconLog = false;

	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
			ELASTICSEARCH_QUERY_URI      = smgr.getString("elasticsearch.restful.uri");
			ELASTICSEARCH_FIELD_CATEGORY = smgr.getString("elasticsearch.field.category");
			ELASTICSEARCH_FIELD_LANGUAGE = smgr.getString("elasticsearch.field.language");
			ELASTICSEARCH_FIELD_FIRMAPP  = smgr.getString("elasticsearch.field.firmapp");
		}
		catch (Exception e)
		{
			log.fatal("[Exit]", e);
			System.exit(-1);
		}
	}
	public PeriodicalCumulativePublishedAmountStatistic()
	{
	}
	public void setTargetTableName(String _targetTableName)
	{
		targetTableName = _targetTableName;
	}
	public String getTargetTableName()
	{
		return targetTableName;
	}
	public void setPublishByTContent(boolean _fPublishByTContent)
	{
		fPublishByTContent = _fPublishByTContent;
	}
	public boolean isPublishByTContent()
	{
		return fPublishByTContent;
	}
	public void setUseUTCTime(boolean _fUseUTCTime)
	{
		fUseUTCTime = _fUseUTCTime;
	}
	public boolean isUseUTCTime()
	{
		return fUseUTCTime;
	}
	public void setDebugMode(boolean _fDebugMode)
	{
		fDebugMode = _fDebugMode;
	}
	public boolean isDebugMode()
	{
		return fDebugMode;
	}
	public void setFalconLog(boolean _fFalconLog)
	{
		fFalconLog = _fFalconLog;
	}
	public boolean isFalconLogEnabled()
	{
		return fFalconLog;
	}
	private HashMap<Integer, Integer> buildCategoryExpiryHourMapping(int version)
	{
		HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
		ArrayList alCategory = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listCategoryByVersion(version);
		for (int i = 0; i < alCategory.size(); i++)
		{
			HashMap mCategory = (HashMap)alCategory.get(i);
			int categoryId = PrimitiveTypeUtil.getInt(mCategory.get("id"));
			int expiryHour = PrimitiveTypeUtil.getInt(mCategory.get("expiry_hour"));
			mapping.put(categoryId, expiryHour);
		}
		return mapping;
	}
	private HashMap<String, ArrayList> buildProductScenarioMapping()
	{
		HashMap<String, ArrayList> mapping = new HashMap<String, ArrayList>();
		ArrayList alProductScenario = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listProductScenario();
		for (int i = 0; i < alProductScenario.size(); i++)
		{
			HashMap mProductScenario = (HashMap)alProductScenario.get(i);
			String product = (String)mProductScenario.get("product");
			ArrayList alData = (ArrayList)mapping.get(product);
			if (alData == null)
			{
				alData = new ArrayList();
				mapping.put(product, alData);
			}
			alData.add(mProductScenario);
		}
		return mapping;
	}
	private HashMap<Integer, StringBuffer> buildCategoryCriteria(String fieldName, HashMap<Integer, Integer> mCategoryExpiry, String[] arrCategoryId)
	{
		HashMap<Integer, StringBuffer> mCriteria = new HashMap<Integer, StringBuffer>();
		HashMap<Integer, Integer> mCriteriaIdx = new HashMap<Integer, Integer>();
		for (int i = 0; i < arrCategoryId.length; i++)
		{
			int categoryId = Integer.parseInt(arrCategoryId[i]);
			int expiryHour = mCategoryExpiry.get(categoryId);
			StringBuffer sb = (StringBuffer)mCriteria.get(expiryHour);
			if (sb == null)
			{
				sb = new StringBuffer();
				mCriteriaIdx.put(expiryHour, 0);
				mCriteria.put(expiryHour, sb);
			}
			int idx = mCriteriaIdx.get(expiryHour);
			if (idx > 0)
				sb.append(" or ");
			sb.append("(");
			sb.append(fieldName);
			sb.append(" like ");
			String fieldValue = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getSQLValue("%\"category\":" + arrCategoryId[i] + "%");
			sb.append(fieldValue);
			sb.append(")");
			mCriteriaIdx.put(expiryHour, (++idx));
		}
		return mCriteria;
	}
	private String toFalconLog(HashMap mReport)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(mReport.get("timestamp_min")));
		sb.append("||");
		String endpoint = StringUtils.isEmpty(System.getProperty("falcon.endpoint")) ? "crawler" : System.getProperty("falcon.endpoint");
		String metric = "crawler.query.article-count-" + String.valueOf(mReport.get("product")) + "-" +  String.valueOf(mReport.get("language")) + "-" + String.valueOf(mReport.get("scenario_channel_id"));
		String value  = String.valueOf(mReport.get("article_available_amount"));
		String count  = "1";
		String counterType = "ORIGINAL";
		String step   = "600";
		String tags   = "";
		String[] data = new String[]{
			endpoint, metric, value, count, counterType, step, tags
		};
		sb.append(TextUtil.getString(data, "&&"));
		return sb.toString();
	}
	private int getArticleCount(String product, String language, String[] categoryId)
	{
		int nArticle = 0;
		String query = prepareQuery(product, language, categoryId);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try
		{
			HttpPost post = new HttpPost(ELASTICSEARCH_QUERY_URI);
			post.setEntity(new org.apache.http.entity.StringEntity(query));
			response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			entity.writeTo(os);
			EntityUtils.consume(entity);
			os.flush();
			os.close();
			String json = os.toString();
			HashMap mResult = (HashMap)inveno.spider.common.utils.JsonUtils.toJavaObject((new Gson()).fromJson(json, JsonElement.class));
			nArticle = Integer.parseInt(String.valueOf(((HashMap)mResult.get("hits")).get("total")));
		}
		catch (Exception e)
		{
			if (response != null)
			{
				try
				{
					response.close();
				}
				catch (IOException ioe)
				{
					//ignore
				}
			}
		}
		finally
		{
			try
			{
				httpclient.close();
			}
			catch (IOException ioe)
			{
				//ignore
			}
		}
		log.debug("query=" + query + "\tresult=" + nArticle);
		return nArticle;
	}
	private String prepareQuery(String product, String language, String[] categoryId)
	{
		ArrayList alShould = new ArrayList();
		HashMap mQueryString, mFieldCondition;
		for (int i = 0; i < categoryId.length; i++)
		{
			mQueryString = new HashMap();
			mFieldCondition = new HashMap();
			mFieldCondition.put(ELASTICSEARCH_FIELD_CATEGORY, categoryId[i]);
			mQueryString.put("term", mFieldCondition);
			alShould.add(mQueryString);
		}
		ArrayList alMust = new ArrayList();
		mQueryString = new HashMap();
		mFieldCondition = new HashMap();
		mFieldCondition.put("query", language);
		mFieldCondition.put("default_field", ELASTICSEARCH_FIELD_LANGUAGE);
		mQueryString.put("query_string", mFieldCondition);
		alMust.add(mQueryString);
		mQueryString = new HashMap();
		mFieldCondition = new HashMap();
		mFieldCondition.put("query", product);
		mFieldCondition.put("default_field", ELASTICSEARCH_FIELD_FIRMAPP);
		mQueryString.put("query_string", mFieldCondition);
		alMust.add(mQueryString);
		HashMap mQuery = new HashMap();
		HashMap mBoolean = new HashMap();
		HashMap mCriteria = new HashMap();
		mCriteria.put("must", alMust);
		mCriteria.put("should", alShould);
		mBoolean.put("bool", mCriteria);
		mQuery.put("filter", mBoolean);
		mQuery.put("from", 0);
		mQuery.put("size", 0);
		return (new Gson()).toJson(mQuery);
	}
	public void reportStatistic(Date statisticTime) throws Exception
	{
		try
		{
			if (fUseUTCTime)
			{
				statisticTime = convertToUTCTime(statisticTime);
			}
			int categoryVersion = 4;

			HashMap<String, ArrayList> mProductScenario = buildProductScenarioMapping();
			ArrayList alReport = new ArrayList();
			String statisticTS_day  = DateUtil.dateToString(statisticTime, DATE_PATTERN_DAILY);
			String statisticTS_hour = DateUtil.dateToString(statisticTime, DATE_PATTERN_HOURLY);
			String statisticTS_min  = DateUtil.dateToString(statisticTime, DATE_PATTERN_MINUTE);
			for (Map.Entry<String, ArrayList> entry : mProductScenario.entrySet())
			{
				String product =(String)entry.getKey();
				ArrayList alScenario = (ArrayList)entry.getValue();
				for (int i = 0; i < alScenario.size(); i++)
				{
					HashMap mScenario = (HashMap)alScenario.get(i);
					if (null == mScenario.get("scenario_id"))
						continue;
					int scenarioId = PrimitiveTypeUtil.getInt(mScenario.get("scenario_id"));
					String[] arrCategory = TextUtil.getStringList((String)mScenario.get("category_types"));
					if (arrCategory == null || arrCategory.length <= 0)
						continue;
					String scenarioName = (String)mScenario.get("scenario_name");
					String language = (String)mScenario.get("language");
					int articleCount = getArticleCount(product, language, arrCategory);

					HashMap mReport = new HashMap();
					mReport.put("timestamp_day", statisticTS_day);
					mReport.put("timestamp_hour", statisticTS_hour);
					mReport.put("timestamp_min", statisticTS_min);
					mReport.put("product", product);
					mReport.put("language", language);
					mReport.put("scenario_channel_id", scenarioId);
					mReport.put("scenario_channel_name", scenarioName);
					mReport.put("article_available_amount", articleCount);
					alReport.add(mReport);
				}
			}
			if (fDebugMode)
			{
				for (int i = 0; i < alReport.size(); i++)
				{
					HashMap mReport = (HashMap)alReport.get(i);
					log.debug(mReport);
				}
			}
			else
			{
				String[] primaryKeyFields = new String[]{"timestamp_day", "timestamp_hour", "timestamp_min", "language", "product", "scenario_channel_id"};
				DashboardFacade.getInstance(AbstractDBFacade.DBNAME_DASHBOARD).reportData(targetTableName, primaryKeyFields, alReport);
			}
			if (fFalconLog)
			{
				for (int i = 0; i < alReport.size(); i++)
				{
					HashMap mReport = (HashMap)alReport.get(i);
					String data = toFalconLog(mReport);
					falconLogger.info(data);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("[reportStatistic]", e);
		}
	}
	private static void printCliHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + PeriodicalCumulativePublishedAmountStatistic.class.getCanonicalName(), createOptions());
	}
	private static Options createOptions()
	{
		Options options = new Options();

		options.addOption(
				Option.builder().argName("date")
				.longOpt("date")
				.hasArg(true)
				.desc("date for statistic, ONLY works for statistic type DAILY.[format: yyyy-MM-dd.")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("history")
				.longOpt("history")
				.hasArg(true)
				.desc("history slot for statistic.[format: number, default 3 for HOURLY, 1 for DAILY.")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("publishByTContent")
				.longOpt("publishByTContent")
				.hasArg(true)
				.desc("indicate publish article is from t_content or not.[true/false]")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("targetTable")
				.longOpt("targetTable")
				.hasArg(true)
				.desc("determine table name to save result w/ primary key {timestamp_day, timestamp_hour, timestamp_min, product_id}")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("useUTCTime")
				.longOpt("useUTCTime")
				.hasArg(true)
				.desc("switch if apply utc time.[true/false]")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("debug")
				.longOpt("debug")
				.hasArg(true)
				.desc("switch debug mode on/off.[true/false]")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("falconLog")
				.longOpt("falconLog")
				.hasArg(true)
				.desc("switch falconLog mode on/off.[true/false]")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("help")
				.longOpt("help")
				.hasArg(false)
				.desc("print help messages.")
				.required(false)
				.build()
		);

		return options;
	}
	public static void main(String[] args)
	{
		try
		{
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = null;
			try
			{
				cmd = parser.parse(createOptions(), args);
			}
			catch (ParseException e)
			{
				printCliHelp();
				throw new Exception("Error in parsing argument:" + e.getMessage());
			}

			if (cmd.hasOption("help"))
			{
				printCliHelp();
				return;
			}

			PeriodicalCumulativePublishedAmountStatistic task = new PeriodicalCumulativePublishedAmountStatistic();
			if (cmd.hasOption("debug"))
			{
				task.setDebugMode( Boolean.valueOf(cmd.getOptionValue("debug")).booleanValue() );
			}
			if (cmd.hasOption("useUTCTime"))
			{
				task.setUseUTCTime( Boolean.valueOf(cmd.getOptionValue("useUTCTime")).booleanValue() );
			}
			if (cmd.hasOption("publishByTContent"))
			{
				task.setPublishByTContent( Boolean.valueOf(cmd.getOptionValue("publishByTContent")).booleanValue() );
			}
			if (cmd.hasOption("targetTable"))
			{
				task.setTargetTableName( cmd.getOptionValue("targetTable") );
			}
			if (cmd.hasOption("falconLog"))
			{
				task.setFalconLog( Boolean.valueOf(cmd.getOptionValue("falconLog")).booleanValue() );
			}
			int nAlignedIncrement = 10;
			int periodIncrementField = Calendar.MINUTE;

			Calendar c = Calendar.getInstance();
			Date startTime  = null;
			if (cmd.hasOption("date"))
			{
				Date evaluateDate = DateUtil.stringToDate(cmd.getOptionValue("date"), "yyyy-MM-dd");
				c.setTime(evaluateDate);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				startTime  = c.getTime();
				c.add(Calendar.DAY_OF_YEAR, 1);
			}
			else
			{
				int aligned_minute = ((int)c.get(Calendar.MINUTE) / nAlignedIncrement - 1) * nAlignedIncrement;
				c.set(Calendar.MINUTE, aligned_minute);
				c.set(Calendar.SECOND, 0);
				startTime = c.getTime();
				c.add(Calendar.MINUTE, nAlignedIncrement);
			}
			Date finishTime = c.getTime();
			c.setTime(startTime);

			while (true)
			{
				if (c.getTime().getTime() >= finishTime.getTime() || c.getTime().getTime() >= System.currentTimeMillis())
					break;

				startTime = c.getTime();
				c.add(periodIncrementField, nAlignedIncrement);
				Date endTime = c.getTime();

				startTime = DateUtil.stringToDate(DateUtil.dateToString(startTime, DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
				endTime   = DateUtil.stringToDate(DateUtil.dateToString(endTime,   DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
				task.reportStatistic(endTime);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
