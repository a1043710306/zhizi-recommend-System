package inveno.spider.reports.task;

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


/**
 * Created by Genix.Li on 2016/08/06.
 *
 * Statistic published amount for each channel(firm_app)
 */
public class PeriodicalProductOffshelfAmountStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(PeriodicalProductOffshelfAmountStatistic.class);
	private static final Logger falconLogger = Logger.getLogger("inveno.spider.falcon");

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATE_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATE_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";
	public static final char   SEPARATER_SOURCE_TYPE   = '/';

	private String targetTableName = "t_article_offshelf";
	private boolean fUseUTCTime = false;
	private boolean fDebugMode = false;
	private boolean fFalconLog = false;
	private String availableLanguage = "zh_CN";

	public PeriodicalProductOffshelfAmountStatistic()
	{
	}
	public void setAvailableLanguage(String _availableLanguage)
	{
		availableLanguage = _availableLanguage;
	}
	public String getAvailableLanguage()
	{
		return availableLanguage;
	}
	public void setTargetTableName(String _targetTableName)
	{
		targetTableName = _targetTableName;
	}
	public String getTargetTableName()
	{
		return targetTableName;
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
	private HashSet<String> enumAvailableLanguage()
	{
		HashSet<String> hsLanguage = new HashSet<String>();
		hsLanguage.add("all");
		String[] s = TextUtil.getStringList(availableLanguage);
		for (int i = 0; s != null && i < s.length; i++)
		{
			hsLanguage.add(s[i]);
		}
		return hsLanguage;
	}
	private HashMap<String, Integer> getProductOffshelfCount(Date startTime, Date endTime)
	{
		HashMap<String, Integer> mProductOffshelfCount = new HashMap<String, Integer>();
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listOffshelfContentAfterAuditingByTimeRange(startTime, endTime);
		HashSet<String> hsAllLanguage = enumAvailableLanguage();
		HashSet<String> hsAllProduct  = enumAllProduct();
		for (int i = 0; alContent != null && i < alContent.size(); i++)
		{
			HashMap mContent = (HashMap)alContent.get(i);
			String language = (String)mContent.get("language");
			if (StringUtils.isEmpty(language))
				language = FIELD_VALUE_UNKNOWN;
			String offshelfCode = String.valueOf(mContent.get("offshelf_code"));
			hsAllLanguage.add(language);

			int increament = 1;
			String[] arrApp = enumFirmApp( (String)mContent.get("firm_app") );

			String keyInfo = TextUtil.getString(new String[]{"all", "all", "all"}, String.valueOf(SEPARATER_SOURCE_TYPE));
			mProductOffshelfCount = increaseAmount(mProductOffshelfCount, keyInfo, increament);
			keyInfo = TextUtil.getString(new String[]{"all", "all", offshelfCode}, String.valueOf(SEPARATER_SOURCE_TYPE));
			mProductOffshelfCount = increaseAmount(mProductOffshelfCount, keyInfo, increament);
			for (int j = 0; arrApp != null && j < arrApp.length; j++)
			{
				String app = arrApp[j];
				keyInfo = TextUtil.getString(new String[]{"all", app, offshelfCode}, String.valueOf(SEPARATER_SOURCE_TYPE));
				mProductOffshelfCount = increaseAmount(mProductOffshelfCount, keyInfo, increament);
				keyInfo = TextUtil.getString(new String[]{language, app, offshelfCode}, String.valueOf(SEPARATER_SOURCE_TYPE));
				mProductOffshelfCount = increaseAmount(mProductOffshelfCount, keyInfo, increament);
			}
		}

		int increament = 0;
		ArrayList alOffshelfCode = ContentFacade.getInstance().listOffshelfCode();
		for (int i = 0; i < alOffshelfCode.size(); i++)
		{
			HashMap mOffshelfCode = (HashMap)alOffshelfCode.get(i);
			String offshelfCode = (String)mOffshelfCode.get("value");
			String keyInfo = TextUtil.getString(new String[]{"all", "all", offshelfCode}, String.valueOf(SEPARATER_SOURCE_TYPE));
			for (String language : hsAllLanguage)
			{
				for (String app : hsAllProduct)
				{
					keyInfo = TextUtil.getString(new String[]{language, app, offshelfCode}, String.valueOf(SEPARATER_SOURCE_TYPE));
					mProductOffshelfCount = increaseAmount(mProductOffshelfCount, keyInfo, 0);
				}
			}
		}
		return mProductOffshelfCount;
	}
	private String toFalconLog(HashMap mReport)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(mReport.get("timestamp_min")));
		sb.append("||");
		String endpoint = StringUtils.isEmpty(System.getProperty("falcon.endpoint")) ? "crawler" : System.getProperty("falcon.endpoint");
		String metric = "crawler.offshelf.article-count-" + String.valueOf(mReport.get("product_id")) + "-" +  String.valueOf(mReport.get("language")) + "-" + String.valueOf(mReport.get("offshelf_code"));
		String value  = String.valueOf(mReport.get("article_offshelf_amount"));
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
	public void reportStatistic(Date startTime, Date endTime) throws Exception
	{
		try
		{
			HashMap<String, Integer> mProductOffshelfCount = getProductOffshelfCount(startTime, endTime);
			if (fUseUTCTime)
			{
				startTime = convertToUTCTime(startTime);
				endTime   = convertToUTCTime(endTime);
			}
			ArrayList alReport = new ArrayList();
			String statisticTS_day  = DateUtil.dateToString(startTime, DATE_PATTERN_DAILY);
			String statisticTS_hour = DateUtil.dateToString(startTime, DATE_PATTERN_HOURLY);
			String statisticTS_min  = DateUtil.dateToString(startTime, DATE_PATTERN_MINUTE);
			for (Map.Entry<String, Integer> entry : mProductOffshelfCount.entrySet())
			{
				String keyInfo    = entry.getKey();
				String[] s = TextUtil.getStringList(keyInfo, SEPARATER_SOURCE_TYPE);
				String language     = s[0];
				String productId    = s[1];
				String offshelfCode = s[2];
				int offshelfAmount   = entry.getValue();
				HashMap mReport = new HashMap();
				mReport.put("timestamp_day", statisticTS_day);
				mReport.put("timestamp_hour", statisticTS_hour);
				mReport.put("timestamp_min", statisticTS_min);
				mReport.put("product_id", productId);
				mReport.put("language", language);
				mReport.put("offshelf_code", offshelfCode);
				mReport.put("article_offshelf_amount", offshelfAmount);
				alReport.add(mReport);
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
				String[] primaryKeyFields = new String[]{"timestamp_day", "timestamp_hour", "timestamp_min", "language", "offshelf_code", "product_id"};
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
		formatter.printHelp("java " + PeriodicalProductOffshelfAmountStatistic.class.getCanonicalName(), createOptions());
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
				Option.builder().argName("availableLanguage")
				.longOpt("availableLanguage")
				.hasArg(true)
				.desc("indicate all available language w/ comma-separated string, like 'zh_CN' or 'English,Hindi,Indonesian'")
				.required(true)
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

			PeriodicalProductOffshelfAmountStatistic task = new PeriodicalProductOffshelfAmountStatistic();
			if (cmd.hasOption("debug"))
			{
				task.setDebugMode( Boolean.valueOf(cmd.getOptionValue("debug")).booleanValue() );
			}
			if (cmd.hasOption("useUTCTime"))
			{
				task.setUseUTCTime( Boolean.valueOf(cmd.getOptionValue("useUTCTime")).booleanValue() );
			}
			if (cmd.hasOption("targetTable"))
			{
				task.setTargetTableName( cmd.getOptionValue("targetTable") );
			}
			task.setAvailableLanguage( cmd.getOptionValue("availableLanguage") );
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
				task.reportStatistic(startTime, endTime);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
