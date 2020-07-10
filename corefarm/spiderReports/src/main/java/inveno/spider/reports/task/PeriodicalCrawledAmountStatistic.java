package inveno.spider.reports.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ContentFacade;
import inveno.spider.reports.facade.DashboardFacade;
import inveno.spider.reports.facade.ReportFacade;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.util.TextUtil;


/**
 * Created by Genix.Li on 2016/05/06.
 */
public class PeriodicalCrawledAmountStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(PeriodicalCrawledAmountStatistic.class);
	private static final Logger falconLogger = Logger.getLogger("inveno.spider.falcon");

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATE_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATE_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";
	public static final char   SEPARATER_SOURCE_TYPE   = '/';

	private String targetTableName = "t_article_crawler";
	private boolean fDebugMode = false;
	private boolean fPublishByTContent = true;
	private boolean fFalconLog = false;
	private boolean fApplyContentType = true;

	public PeriodicalCrawledAmountStatistic()
	{
		if ("false".equalsIgnoreCase(System.getProperty("applyContentType")))
			fApplyContentType = false;
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
	private HashMap<String, Integer> getSourcePublishCount(Date startTime, Date endTime)
	{
		HashMap<String, Integer> mSourcePublishCount = new HashMap<String, Integer>();
		ArrayList alContent = (fPublishByTContent) ? ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentAfterAuditingByTimeRange(startTime, endTime)
												   : ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentByTurnDataInTimeRange(startTime, endTime);
		for (int i = 0; alContent != null && i < alContent.size(); i++)
		{
			HashMap mContent = (HashMap)alContent.get(i);
			String source     = ((String)mContent.get("source")).toLowerCase();
			String sourceType = ((String)mContent.get("source_type")).toLowerCase();
			String country    = capitalFirstCharacter((String)mContent.get("country"));
			String language   = capitalFirstCharacter((String)mContent.get("language"));

			int increament = 1;
			if (fApplyContentType)
			{
				String contentType = getDisplayContentType(PrimitiveTypeUtil.getInt( mContent.get("content_type") ));
				//log.debug("categories=" + mContent.get("categories"));
				String[] arrCategory = getDisplayCategory((String)mContent.get("categories"));

				String keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language, contentType, "all"}, String.valueOf(SEPARATER_SOURCE_TYPE));
				mSourcePublishCount = increaseAmount(mSourcePublishCount, keySourceInfo, increament);
				for (int j = 0; j < arrCategory.length; j++)
				{
					keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language, contentType, arrCategory[j]}, String.valueOf(SEPARATER_SOURCE_TYPE));
					mSourcePublishCount = increaseAmount(mSourcePublishCount, keySourceInfo, increament);
				}
			}
			else
			{
				String keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language}, String.valueOf(SEPARATER_SOURCE_TYPE));
				mSourcePublishCount = increaseAmount(mSourcePublishCount, keySourceInfo, increament);
			}
		}
		return mSourcePublishCount;
	}
	private String toFalconLog(String timestamp, String language, int publishAmount)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(timestamp);
		sb.append("||");
		String endpoint = StringUtils.isEmpty(System.getProperty("falcon.endpoint")) ? "crawler" : System.getProperty("falcon.endpoint");
		String metric = "crawler.query.available-publish-amount-" + language;
		String value  = String.valueOf(publishAmount);
		String count  = "1";
		String counterType = "ORIGINAL";
		String step   = "3600";
		String tags   = "";
		String[] data = new String[]{
			endpoint, metric, value, count, counterType, step, tags
		};
		sb.append(TextUtil.getString(data, "&&"));
		return sb.toString();
	}
	public void reportStatistic(String type, String[] arrSourceInfo, Date startTime, Date endTime, String sqlDatePattern) throws Exception
	{
		try
		{
			HashSet<String> hsSourceInfo = new HashSet<String>(java.util.Arrays.asList(arrSourceInfo));
			HashSet<String> hsSource = new HashSet<String>();
			for (int i = 0;  arrSourceInfo != null && i < arrSourceInfo.length; i++)
			{
				String[] s = TextUtil.getStringList(arrSourceInfo[i], SEPARATER_SOURCE_TYPE);
				if (s != null && s.length > 1)
					hsSource.add( s[0] );
			}

			String statisticTS_day  = DateUtil.dateToString(startTime, DATE_PATTERN_DAILY);
			String statisticTS_hour = DateUtil.dateToString(startTime, DATE_PATTERN_HOURLY);
			String statisticTS = null;
			if (DashboardFacade.MONITOR_TYPE_DAILY.equalsIgnoreCase(type))
				statisticTS = statisticTS_day;
			else
				statisticTS = statisticTS_hour;

			if (TimeZone.getTimeZone("UTC").equals(TimeZone.getDefault()))
			{
				startTime = convertToCSTTime(startTime);
				endTime   = convertToCSTTime(endTime);
			}

			log.debug(TimeZone.getDefault() + "\t" + statisticTS + " query from " + startTime + " to " + endTime);

			String[] arrSource = (String[])hsSource.toArray(new String[0]);
			ArrayList alCrawlerCount = DashboardFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listCrawlerArticleCount(arrSource, startTime, endTime, sqlDatePattern);
			HashMap<String, Integer> mSourcePublishCount = getSourcePublishCount(startTime, endTime);

			ArrayList alReport = new ArrayList();
			HashMap<String, Integer> mSourceCrawlerCount = new HashMap<String, Integer>();
			HashSet<String> hsKeyInfo = new HashSet<String>();
			int total = 0;
			for (int i = 0; i < alCrawlerCount.size(); i++)
			{
				HashMap mCrawlerCount = (HashMap)alCrawlerCount.get(i);
				String current_source = (String)mCrawlerCount.get("source");
				String source      = ((String)mCrawlerCount.get("source")).toLowerCase();
				String sourceType  = ((String)mCrawlerCount.get("source_type")).toLowerCase();
				String country     = capitalFirstCharacter((String)mCrawlerCount.get("country"));
				String language    = capitalFirstCharacter((String)mCrawlerCount.get("language"));

				int increament = (null == mCrawlerCount.get("article_count")) ? 0 : PrimitiveTypeUtil.getInt( mCrawlerCount.get("article_count") );
				total += increament;
				if (fApplyContentType)
				{
					String contentType = getDisplayContentType(PrimitiveTypeUtil.getInt( mCrawlerCount.get("content_type") ));
					//log.debug("categories=" + mCrawlerCount.get("categories"));
					String[] arrCategory = getDisplayCategory((String)mCrawlerCount.get("categories"));

					String keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language, contentType, "all"}, String.valueOf(SEPARATER_SOURCE_TYPE));
					mSourceCrawlerCount = increaseAmount(mSourceCrawlerCount, keySourceInfo, increament);
					hsKeyInfo.add(keySourceInfo);
					for (int j = 0; j < arrCategory.length; j++)
					{
						keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language, contentType, arrCategory[j]}, String.valueOf(SEPARATER_SOURCE_TYPE));
						mSourceCrawlerCount = increaseAmount(mSourceCrawlerCount, keySourceInfo, increament);
						hsKeyInfo.add(keySourceInfo);
					}
				}
				else
				{
					String keySourceInfo = TextUtil.getString(new String[]{source, sourceType, country, language}, String.valueOf(SEPARATER_SOURCE_TYPE));
					mSourceCrawlerCount = increaseAmount(mSourceCrawlerCount, keySourceInfo, increament);
					hsKeyInfo.add(keySourceInfo);
				}
			}

			//only increment for category="all" to falon monitor.
			HashMap<String, Integer> mLanguagePublishedCount = new HashMap<String, Integer>();
			int keyLength = (fApplyContentType) ? 6 : 4;
			log.info("hsKeyInfo count:" + hsKeyInfo.size());
			for (String keySourceInfo : hsKeyInfo)
			{
				String[] s = keySourceInfo.split(String.valueOf(SEPARATER_SOURCE_TYPE));
				if (s.length < keyLength)
				{
					log.error("invalid key " + keySourceInfo);
					continue;
				}
				String source      = s[0];
				String sourceType  = s[1];
				String country     = s[2];
				String language    = s[3];
				String contentType = (fApplyContentType) ? s[4] : null;
				String category    = (fApplyContentType) ? s[5] : "all";

				HashMap mReport = new HashMap();
				mReport.put("timestamp", statisticTS);
				mReport.put("type", type);
				mReport.put("source", source);
				mReport.put("source_type", sourceType);
				mReport.put("country", country);
				mReport.put("language", language);
				if (fApplyContentType)
				{
					mReport.put("content_type", contentType);
					mReport.put("category", category);
				}
				int crawlerAmount = (null == mSourceCrawlerCount.get(keySourceInfo)) ? 0 : ((Integer)mSourceCrawlerCount.get(keySourceInfo)).intValue();
				mReport.put("crawler_amount", crawlerAmount);
				int publishAmount = (null == mSourcePublishCount.get(keySourceInfo)) ? 0 : ((Integer)mSourcePublishCount.get(keySourceInfo)).intValue();
				mReport.put("publish_amount", publishAmount);
				alReport.add(mReport);

				if ("all".equalsIgnoreCase(category))
				{
					int amount = 0;
					if (null != mLanguagePublishedCount.get(language))
					{
						amount = ((Integer)mLanguagePublishedCount.get(language)).intValue();
					}
					amount += publishAmount;
					mLanguagePublishedCount.put(language, amount);
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
				String[] primaryKeyFields = (fApplyContentType) ? new String[]{"timestamp", "type", "source", "source_type", "country", "language", "content_type", "category"}
																: new String[]{"timestamp", "type", "source", "source_type", "country", "language"};
				DashboardFacade.getInstance(AbstractDBFacade.DBNAME_DASHBOARD).reportData(targetTableName, primaryKeyFields, alReport);
			}
			if (fFalconLog)
			{
				for (Map.Entry<String, Integer> entry : mLanguagePublishedCount.entrySet())
				{
					String language = (String)entry.getKey();
					int publishAmount = ((Integer)entry.getValue()).intValue();
					if (StringUtils.isEmpty(language))
						continue;
					String data = toFalconLog(statisticTS, language, publishAmount);
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
		formatter.printHelp("java " + PeriodicalCrawledAmountStatistic.class.getCanonicalName(), createOptions());
	}
	private static Options createOptions()
	{
		Options options = new Options();

		options.addOption(
				Option.builder().argName("type")
				.longOpt("type")
				.hasArg(true)
				.desc("type for statistic.[DAILY, HOURLY]")
				.required(true)
				.build()
		);

		options.addOption(
				Option.builder().argName("source")
				.longOpt("source")
				.hasArg(true)
				.desc("source list for statistic.[format: comma-separated string, like: \"微信/app,头条/app\".")
				.required(false)
				.build()
		);

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
				.desc("determine table name to save result w/ primary key {timestamp, type, source, source_type, country, language}")
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

			PeriodicalCrawledAmountStatistic task = new PeriodicalCrawledAmountStatistic();
			if (cmd.hasOption("debug"))
			{
				task.setDebugMode( Boolean.valueOf(cmd.getOptionValue("debug")).booleanValue() );
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
			String reportType  = cmd.getOptionValue("type");
			String[] arrSource = (cmd.hasOption("source")) ? TextUtil.getStringList(cmd.getOptionValue("source")) : null;
			ArrayList<String> alSourceInfo = new ArrayList<String>();
			if (arrSource == null)
			{
				ArrayList alSource = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getArticleSource();
				HashSet<String> hsSource = new HashSet<String>();
				for (int i = 0; i < alSource.size(); i++)
				{
					HashMap mSource = (HashMap)alSource.get(i);
					hsSource.add( (String)mSource.get("source") );
				}
				arrSource = (String[])hsSource.toArray(new String[0]);

				String[] arrSourceType = new String[]{"rss", "web", "app"};
				for (int j = 0; arrSourceType != null && j < arrSourceType.length; j++)
				{
					for (int i = 0; arrSource != null && i < arrSource.length; i++)
					{
						alSourceInfo.add(TextUtil.getString(new String[]{arrSource[i], arrSourceType[j]}, String.valueOf(SEPARATER_SOURCE_TYPE)));
					}
				}
			}
			else
			{
				for (int i = 0; i < arrSource.length; i++)
				{
					String[] s = arrSource[i].split(String.valueOf(SEPARATER_SOURCE_TYPE));
					String source = s[0];
					String sourceType = (s.length > 1) ? s[1] : "web";
					alSourceInfo.add(TextUtil.getString(new String[]{source, sourceType}, String.valueOf(SEPARATER_SOURCE_TYPE)));
				}
			}
			String[] arrSourceInfo = (String[])alSourceInfo.toArray(new String[0]);

			String sqlDatePattern = null;
			int periodIncrementField = -1;
			int nHistoryPeriodSlot = -1;
			if (DashboardFacade.MONITOR_TYPE_HOURLY.equalsIgnoreCase(reportType))
			{
				reportType     = DashboardFacade.MONITOR_TYPE_HOURLY;
				sqlDatePattern = SQL_DATE_PATTERN_HOURLY;	
				periodIncrementField = Calendar.HOUR;
				nHistoryPeriodSlot   = (cmd.hasOption("history")) ? Integer.parseInt(cmd.getOptionValue("history")) : 3;
			}
			else if (DashboardFacade.MONITOR_TYPE_DAILY.equalsIgnoreCase(reportType))
			{
				reportType     = DashboardFacade.MONITOR_TYPE_DAILY;
				sqlDatePattern = SQL_DATE_PATTERN_DAILY;
				periodIncrementField = Calendar.DAY_OF_YEAR;
				nHistoryPeriodSlot   = (cmd.hasOption("history")) ? Integer.parseInt(cmd.getOptionValue("history")) : 1;
			}
			else
			{
				throw new Exception("Unsupported report_type " + reportType);
			}

			Calendar c = Calendar.getInstance();
			if (cmd.hasOption("date") && DashboardFacade.MONITOR_TYPE_DAILY.equalsIgnoreCase(reportType))
			{
				Date evaluateDate = DateUtil.stringToDate(cmd.getOptionValue("date"), "yyyy-MM-dd");
				c.setTime(evaluateDate);
				c.add(periodIncrementField, 1);
			}
			Date finishTime = c.getTime();
			if (DashboardFacade.MONITOR_TYPE_DAILY.equalsIgnoreCase(reportType))
			{
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
			}
			c.add(periodIncrementField, -1 * nHistoryPeriodSlot);

			while (true)
			{
				if (c.getTime().getTime() >= finishTime.getTime()) {
					log.info("break while.");
					break;
				}

				Date startTime = c.getTime();
				c.add(periodIncrementField, 1);
				Date endTime = c.getTime();

				startTime = DateUtil.stringToDate(DateUtil.dateToString(startTime, DATE_PATTERN_HOURLY), DATE_PATTERN_HOURLY);
				endTime   = DateUtil.stringToDate(DateUtil.dateToString(endTime,   DATE_PATTERN_HOURLY), DATE_PATTERN_HOURLY);
				task.reportStatistic(reportType, arrSourceInfo, startTime, endTime, sqlDatePattern);
				log.info("PeriodicalCrawledAmountStatistic end date:" + new Date());
			}
		}
		catch (Exception e)
		{
			log.error("PeriodicalCrawledAmountStatistic main has error:", e);
		}
	}
}
