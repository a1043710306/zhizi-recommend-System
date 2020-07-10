package inveno.spider.reports.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

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
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.util.TextUtil;


/**
 * Created by Genix.Li on 2016/08/06.
 *
 * Statistic published amount for each channel(firm_app)
 */
public class PeriodicalProductArticleAmountStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(PeriodicalProductArticleAmountStatistic.class);

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATE_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATE_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";
	public static final char   SEPARATER_SOURCE_TYPE   = '/';

	private String[] staticsticType = new String[]{"overall", "artificial"};
	private String targetTableName = "t_article_audited";
	private boolean fUseUTCTime = false;
	private boolean fDebugMode = false;
	private String availableLanguage = "zh_CN";

	public PeriodicalProductArticleAmountStatistic()
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
	private String[] permutateKey(String[] arrProductId, String[] arrLanguage, String[] arrCategories)
	{
		arrProductId  = addOptionAll(arrProductId);
		arrLanguage   = addOptionAll(arrLanguage);
		arrCategories = addOptionAll(arrCategories);
		HashSet<String> hsKey = new HashSet<String>();
		for (int i = 0; i < arrProductId.length; i++)
		{
			for (int j = 0; j < arrLanguage.length; j++)
			{
				for (int k = 0; k < arrCategories.length; k++)
				{
					String keyInfo = TextUtil.getString(new String[]{arrProductId[i], arrLanguage[j], arrCategories[k]}, String.valueOf(SEPARATER_SOURCE_TYPE));
					hsKey.add(keyInfo);
				}
			}
		}
		return (String[])hsKey.toArray(new String[0]);
	}
	private HashMap<String, HashMap<String, Integer>> calculateProductArticleCount(ArrayList alContent)
	{
		HashMap<String, HashMap<String, Integer>> mResult = new HashMap<String, HashMap<String, Integer>>();
		for (int i = 0; i < staticsticType.length; i++)
			mResult.put(staticsticType[i], new HashMap<String, Integer>());
		//
		HashSet<String> hsAllProduct  = enumAllProduct();
		HashSet<String> hsAllLanguage = enumAvailableLanguage();
		HashSet<String> hsAllCategory = enumAllCategory();
		//statisic dimension : product, language, category
		for (int i = 0; alContent != null && i < alContent.size(); i++)
		{
			HashMap mContent = (HashMap)alContent.get(i);
			String language = (String)mContent.get("language");
			if (StringUtils.isEmpty(language))
				language = FIELD_VALUE_UNKNOWN;
			hsAllLanguage.add(language);

			boolean hasArtificialLog = (null != mContent.get("operate_time"));

			int increament = 1;
			String[] arrProductId  = enumFirmApp( (String)mContent.get("firm_app") );
			String[] arrLanguage   = new String[]{ language };
			String[] arrCategories = getDisplayCategory( (String)mContent.get("categories") );

			String[] arrKeyInfo = permutateKey(arrProductId, arrLanguage, arrCategories);
			HashMap<String, Integer> mOverallCount    = (HashMap<String, Integer>)mResult.get("overall");
			HashMap<String, Integer> mArtificialCount = (HashMap<String, Integer>)mResult.get("artificial");
			for (int j = 0; j < arrKeyInfo.length; j++)
			{
				mOverallCount = increaseAmount(mOverallCount, arrKeyInfo[j], increament);
				if (hasArtificialLog)
				{
					mArtificialCount = increaseAmount(mArtificialCount, arrKeyInfo[j], increament);
				}
			}
		}

		String[] arrProductId  = (String[])hsAllProduct.toArray(new String[0]);
		String[] arrLanguage   = (String[])hsAllLanguage.toArray(new String[0]);
		String[] arrCategories = (String[])hsAllCategory.toArray(new String[0]);
		String[] arrKeyInfo = permutateKey(arrProductId, arrLanguage, arrCategories);
		for (int i = 0; i < staticsticType.length; i++)
		{
			HashMap<String, Integer> mProductArticleCount = (HashMap<String, Integer>)mResult.get(staticsticType[i]);
			for (int j = 0; j < arrKeyInfo.length; j++)
			{
				mProductArticleCount = increaseAmount(mProductArticleCount, arrKeyInfo[j], 0);
			}
		}
		return mResult;
	}
	private HashMap<String, HashMap<String, Integer>> getProductPublishArticleCount(Date startTime, Date endTime)
	{
		//overall publish content
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentAfterAuditingByTimeRange(startTime, endTime);
		return  calculateProductArticleCount(alContent);
	}
	private HashMap<String, HashMap<String, Integer>> getProductOffshelfArticleCount(Date startTime, Date endTime)
	{
		//overall offshelf content
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listOffshelfContentAfterAuditingByTimeRange(startTime, endTime);
		return calculateProductArticleCount(alContent);
	}
	private HashMap<String, HashMap<String, Integer>> getProductCheckingArticleCount(Date startTime, Date endTime)
	{
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listCheckingContentAfterAuditingByTimeRange(startTime, endTime);
		return calculateProductArticleCount(alContent);
	}
	private HashMap<String, HashMap<String, Integer>> getProductPublishArticleCountInSlot(Date startTime, Date endTime)
	{
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentInSlotRangeAfterAuditingByStateTimeRange(startTime, endTime);
		return calculateProductArticleCount(alContent);
	}
	private HashMap<String, HashMap<String, Integer>> getProductOffshelfArticleCountInSlot(Date startTime, Date endTime)
	{
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listOffshelfContentInSlotRangeAfterAuditingByStateTimeRange(startTime, endTime);
		return calculateProductArticleCount(alContent);
	}
	public void reportStatistic(Date startTime, Date endTime, Date slotStartTime, Date slotEndTime) throws Exception
	{
		try
		{
			HashMap<String, HashMap<String, Integer>> mProductPublishArticleCount  = getProductPublishArticleCount(startTime, endTime);
			HashMap<String, HashMap<String, Integer>> mProductOffshelfArticleCount = getProductOffshelfArticleCount(startTime, endTime);
			HashMap<String, HashMap<String, Integer>> mProductCheckingArticleCount = getProductCheckingArticleCount(startTime, endTime);
			HashMap<String, HashMap<String, Integer>> mProductPublishArticleInSlotCount  = getProductPublishArticleCountInSlot(slotStartTime, slotEndTime);
			HashMap<String, HashMap<String, Integer>> mProductOffshelfArticleInSlotCount = getProductOffshelfArticleCountInSlot(slotStartTime, slotEndTime);
			if (fUseUTCTime)
			{
				startTime = convertToUTCTime(startTime);
				endTime   = convertToUTCTime(endTime);
				slotStartTime = convertToUTCTime(slotStartTime);
				slotEndTime   = convertToUTCTime(slotEndTime);
			}
			ArrayList alReport = new ArrayList();
			String statisticTS_day  = DateUtil.dateToString(slotStartTime, DATE_PATTERN_DAILY);
			String statisticTS_hour = DateUtil.dateToString(slotStartTime, DATE_PATTERN_HOURLY);
			String statisticTS_min  = DateUtil.dateToString(slotStartTime, DATE_PATTERN_MINUTE);

			String[] arrKeyInfo = (String[])(mProductPublishArticleCount.get("overall")).keySet().toArray(new String[0]);
			for (int i = 0; i < arrKeyInfo.length; i++)
			{
				String[] s = TextUtil.getStringList(arrKeyInfo[i], SEPARATER_SOURCE_TYPE);
				String productId = s[0];
				String language  = s[1];
				String category  = s[2];
				HashMap mReport = new HashMap();
				mReport.put("timestamp_day", statisticTS_day);
				mReport.put("timestamp_hour", statisticTS_hour);
				mReport.put("timestamp_min", statisticTS_min);
				mReport.put("product_id", productId);
				mReport.put("language", language);
				mReport.put("category", category);
				int overallPublishAmount     = (mProductPublishArticleCount.get("overall").get(arrKeyInfo[i]) == null) ? 0 : PrimitiveTypeUtil.getInt( mProductPublishArticleCount.get("overall").get(arrKeyInfo[i]) );
				int overallCheckingAmount    = (mProductCheckingArticleCount.get("overall").get(arrKeyInfo[i]) == null) ? 0 : PrimitiveTypeUtil.getInt( mProductCheckingArticleCount.get("overall").get(arrKeyInfo[i]) );
				int overallOffshelfAmount    = (mProductOffshelfArticleCount.get("overall").get(arrKeyInfo[i]) == null) ? 0 : PrimitiveTypeUtil.getInt( mProductOffshelfArticleCount.get("overall").get(arrKeyInfo[i]) );
				int artificialPublishAmount  = (mProductPublishArticleInSlotCount.get("artificial").get(arrKeyInfo[i]) == null) ? 0 : PrimitiveTypeUtil.getInt( mProductPublishArticleInSlotCount.get("artificial").get(arrKeyInfo[i]) );
				int artificialOffshelfAmount = (mProductOffshelfArticleInSlotCount.get("artificial").get(arrKeyInfo[i]) == null) ? 0 : PrimitiveTypeUtil.getInt( mProductOffshelfArticleInSlotCount.get("artificial").get(arrKeyInfo[i]) );
				mReport.put("publish_amount_overall", overallPublishAmount);
				mReport.put("checking_amount_overall", overallCheckingAmount);
				mReport.put("offshelf_amount_overall", overallOffshelfAmount);
				mReport.put("publish_amount_artificial", artificialPublishAmount);
				mReport.put("offshelf_amount_artificial", artificialOffshelfAmount);
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
				String[] primaryKeyFields = new String[]{"timestamp_day", "timestamp_hour", "timestamp_min", "product_id", "language", "category"};
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
		formatter.printHelp("java " + PeriodicalProductArticleAmountStatistic.class.getCanonicalName(), createOptions());
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

			PeriodicalProductArticleAmountStatistic task = new PeriodicalProductArticleAmountStatistic();
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
			int nAlignedIncrement = 10;
			int periodIncrementField = Calendar.MINUTE;

			Calendar c = Calendar.getInstance();
			Date startTime  = null;
			Date statisticSlotStartTime = null;
			Date statisticSlotEndTime = null;
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
				statisticSlotStartTime = c.getTime();
				c.add(Calendar.MINUTE, nAlignedIncrement);
				statisticSlotEndTime = c.getTime();
			}
			Date endTime = c.getTime();
			c.add(Calendar.HOUR, -24);
			startTime = c.getTime();
			startTime = DateUtil.stringToDate(DateUtil.dateToString(startTime, DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
			endTime   = DateUtil.stringToDate(DateUtil.dateToString(endTime,   DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
			statisticSlotStartTime = DateUtil.stringToDate(DateUtil.dateToString(statisticSlotStartTime,   DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
			statisticSlotEndTime   = DateUtil.stringToDate(DateUtil.dateToString(statisticSlotEndTime,   DATE_PATTERN_MINUTE), DATE_PATTERN_MINUTE);
			task.reportStatistic(startTime, endTime, statisticSlotStartTime, statisticSlotEndTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
