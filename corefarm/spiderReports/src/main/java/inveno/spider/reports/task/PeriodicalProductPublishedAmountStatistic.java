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
public class PeriodicalProductPublishedAmountStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(PeriodicalProductPublishedAmountStatistic.class);

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATE_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATE_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";
	public static final char   SEPARATER_SOURCE_TYPE   = '/';

	private String targetTableName = "t_article_publish";
	private boolean fUseUTCTime = false;
	private boolean fDebugMode = false;
	private boolean fPublishByTContent = true;
	private String availableLanguage = "zh_CN";
	private boolean fApplyContentType = true;
	private boolean fApplyBodyImagesCount = true;

	public PeriodicalProductPublishedAmountStatistic()
	{
		if ("false".equalsIgnoreCase(System.getProperty("applyContentType")))
			fApplyContentType = false;
		if ("false".equalsIgnoreCase(System.getProperty("applyBodyImagesCount")))
			fApplyBodyImagesCount = false;
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
	private String[] permutateKey(String[] arrProductId, String[] arrLanguage)
	{
		arrProductId  = addOptionAll(arrProductId);
		arrLanguage   = addOptionAll(arrLanguage);
		HashSet<String> hsKey = new HashSet<String>();
		for (int i = 0; i < arrProductId.length; i++)
		{
			for (int j = 0; j < arrLanguage.length; j++)
			{
				String keyInfo = TextUtil.getString(new String[]{arrProductId[i], arrLanguage[j]}, String.valueOf(SEPARATER_SOURCE_TYPE));
				hsKey.add(keyInfo);
			}
		}
		return (String[])hsKey.toArray(new String[0]);
	}
	private String[] permutateKey(String[] arrProductId, String[] arrLanguage, String[] arrContentType, String[] arrBodyImagesCountHint)
	{
		arrProductId  = addOptionAll(arrProductId);
		arrLanguage   = addOptionAll(arrLanguage);
		arrContentType = addOptionAll(arrContentType);
		arrBodyImagesCountHint = addOptionAll(arrBodyImagesCountHint);
		HashSet<String> hsKey = new HashSet<String>();
		for (int i = 0; i < arrProductId.length; i++)
		{
			for (int j = 0; j < arrLanguage.length; j++)
			{
				for (int k = 0; k < arrContentType.length; k++)
				{
					for (int m = 0; m < arrBodyImagesCountHint.length; m++)
					{
						String keyInfo = TextUtil.getString(new String[]{arrProductId[i], arrLanguage[j], arrContentType[k], arrBodyImagesCountHint[m]}, String.valueOf(SEPARATER_SOURCE_TYPE));
						hsKey.add(keyInfo);
					}
				}
			}
		}
		return (String[])hsKey.toArray(new String[0]);
	}
	private HashMap<String, Integer> getProductPublishCount(Date startTime, Date endTime)
	{
		HashMap<String, Integer> mProductPublishCount = new HashMap<String, Integer>();
		ArrayList alContent = (fPublishByTContent) ? ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentAfterAuditingByTimeRange(startTime, endTime)
												   : ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentByTurnDataInTimeRange(startTime, endTime);
		HashSet<String> hsAllLanguage = enumAvailableLanguage();
		HashSet<String> hsAllProduct  = enumAllProduct();
		HashSet<String> hsAllContentType = enumAllContentType();
		HashSet<String> hsAllBodyImagesCountHint = enumAllBodyImageCountHint();

		String boundDate = System.getProperty("utcdate");
		Date boundDateStart = null;
		Date boundDateEnd = null;
		if (!StringUtils.isEmpty(boundDate))
		{
			boundDateStart = DateUtil.stringToDate(System.getProperty("utcdate"), "yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(boundDateStart);
			c.add(Calendar.HOUR, 8);
			c.add(Calendar.DAY_OF_YEAR, 1);
			boundDateEnd = c.getTime();
		}
		HashMap<String, HashSet<String>> mProductContentId = new HashMap<String, HashSet<String>>();

		for (int i = 0; alContent != null && i < alContent.size(); i++)
		{
			HashMap mContent = (HashMap)alContent.get(i);
			String contentId = (String)mContent.get("content_id");
			String language = capitalFirstCharacter((String)mContent.get("language"));
			Date discoveryTime = (Date)mContent.get("discovery_time");
			if (StringUtils.isEmpty(language))
				language = FIELD_VALUE_UNKNOWN;
			hsAllLanguage.add(language);
			int bodyImagesCount = (null == mContent.get("body_images_count")) ? 0 : PrimitiveTypeUtil.getInt(mContent.get("body_images_count"));
			String bodyImagesCountHint =  getDisplayBodyImagesCountHint( bodyImagesCount );

			int increament = 1;
			if (fApplyContentType)
			{
				String contentType = getDisplayContentType(PrimitiveTypeUtil.getInt( mContent.get("content_type") ));
				String[] arrProductId   = enumFirmApp( (String)mContent.get("firm_app") );
				String[] arrLanguage    = new String[]{ language };
				String[] arrContentType = new String[]{ contentType };
				String[] arrBodyImagesCountHint = (fApplyBodyImagesCount) ? new String[]{ bodyImagesCountHint } : new String[0];
				String[] arrKeyInfo    = permutateKey(arrProductId, arrLanguage, arrContentType, arrBodyImagesCountHint);
				for (int j = 0; j < arrKeyInfo.length; j++)
				{
					mProductPublishCount = increaseAmount(mProductPublishCount, arrKeyInfo[j], increament);
				}
				//for tracing throughput and available publish amount
				for (int j = 0; j < arrProductId.length; j++)
				{
					String productKey = arrProductId[j] + "." + language;
					HashSet<String> hsContentId = (HashSet<String>)mProductContentId.get(productKey);
					if (null == hsContentId)
					{
						hsContentId = new HashSet<String>();
						mProductContentId.put(productKey, hsContentId);
					}
					if (boundDateStart != null && boundDateEnd != null && discoveryTime.after(boundDateStart) && discoveryTime.before(boundDateEnd))
					{
						hsContentId.add(contentId);
					}
				}
			}
			else
			{
				String[] arrProductId = enumFirmApp( (String)mContent.get("firm_app") );
				String[] arrLanguage  = new String[]{ language };
				String[] arrKeyInfo   = permutateKey(arrProductId, arrLanguage);
				for (int j = 0; j < arrKeyInfo.length; j++)
				{
					mProductPublishCount = increaseAmount(mProductPublishCount, arrKeyInfo[j], increament);
				}
			}
		}

		if (mProductContentId.size() > 0)
		{
			for (String productKey : mProductContentId.keySet())
			{
				System.out.println(productKey + "\tcontentId: " + mProductContentId.get(productKey));
			}
		}

		int increament = 0;
		String[] arrProductId  = (String[])hsAllProduct.toArray(new String[0]);
		String[] arrLanguage   = (String[])hsAllLanguage.toArray(new String[0]);
		String[] arrBodyImagesCountHint = (String[])hsAllBodyImagesCountHint.toArray(new String[0]);
		if (fApplyContentType)
		{
			String[] arrContentType = (String[])hsAllContentType.toArray(new String[0]);
			String[] arrKeyInfo = permutateKey(arrProductId, arrLanguage, arrContentType, arrBodyImagesCountHint);
			for (int j = 0; j < arrKeyInfo.length; j++)
			{
				mProductPublishCount = increaseAmount(mProductPublishCount, arrKeyInfo[j], 0);
			}
		}
		else
		{
			String[] arrKeyInfo = permutateKey(arrProductId, arrLanguage);
			for (int j = 0; j < arrKeyInfo.length; j++)
			{
				mProductPublishCount = increaseAmount(mProductPublishCount, arrKeyInfo[j], 0);
			}
		}
		return mProductPublishCount;
	}

	public void reportStatistic(Date startTime, Date endTime) throws Exception
	{
		try
		{
			HashMap<String, Integer> mProductPublishCount = getProductPublishCount(startTime, endTime);
			if (fUseUTCTime)
			{
				startTime = convertToUTCTime(startTime);
				endTime   = convertToUTCTime(endTime);
			}
			ArrayList alReport = new ArrayList();
			String statisticTS_day  = DateUtil.dateToString(startTime, DATE_PATTERN_DAILY);
			String statisticTS_hour = DateUtil.dateToString(startTime, DATE_PATTERN_HOURLY);
			String statisticTS_min  = DateUtil.dateToString(startTime, DATE_PATTERN_MINUTE);
			for (Map.Entry<String, Integer> entry : mProductPublishCount.entrySet())
			{
				String keyInfo    = entry.getKey();
				String[] s = TextUtil.getStringList(keyInfo, SEPARATER_SOURCE_TYPE);
				String productId   = s[0];
				String language    = s[1];
				String contentType = (fApplyContentType) ? s[2] : null;
				String bodyImagesCountHint  = (s.length > 3) ? s[3] : "all";

				int publishAmount  = entry.getValue();
				HashMap mReport = new HashMap();
				mReport.put("timestamp_day", statisticTS_day);
				mReport.put("timestamp_hour", statisticTS_hour);
				mReport.put("timestamp_min", statisticTS_min);
				mReport.put("product_id", productId);
				mReport.put("language", language);
				if (fApplyContentType)
				{
					mReport.put("content_type", contentType);
				}
				mReport.put("body_images_count", bodyImagesCountHint);
				mReport.put("article_available_amount", publishAmount);
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
				String[] primaryKeyFields = (fApplyContentType) ? new String[]{"timestamp_day", "timestamp_hour", "timestamp_min", "language", "product_id", "content_type", "body_images_count"}
																: new String[]{"timestamp_day", "timestamp_hour", "timestamp_min", "language", "product_id"};
				DashboardFacade.getInstance(AbstractDBFacade.DBNAME_DASHBOARD).reportData(targetTableName, primaryKeyFields, alReport);
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
		formatter.printHelp("java " + PeriodicalProductPublishedAmountStatistic.class.getCanonicalName(), createOptions());
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

			PeriodicalProductPublishedAmountStatistic task = new PeriodicalProductPublishedAmountStatistic();
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
			task.setAvailableLanguage( cmd.getOptionValue("availableLanguage") );

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
				log.info("reportStatistic end time:" + new Date());
			}
		}
		catch (Exception e)
		{
			log.error("process PeriodicalProductPublishedAmountStatistic has exception:", e);
		}
	}
}
