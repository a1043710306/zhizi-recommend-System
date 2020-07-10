package inveno.spider.reports.task;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.common.mail.MailSender;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
import tw.qing.util.PrimitiveTypeUtil;

import java.util.*;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DailyLanguageCrawlerStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailyLanguageCrawlerStatistic.class);

	private static String SMTP_HOST;
	private static int    SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

	public static final char SEPARATER_SOURCE_TYPE = '/';

	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
			SMTP_HOST     = System.getProperty("smtp.host", smgr.getString("smtp.host"));
			SMTP_PORT     = Integer.parseInt(System.getProperty("smtp.port", smgr.getString("smtp.port")));
			SMTP_TLS      = Boolean.valueOf(System.getProperty("smtp.tls", smgr.getString("smtp.tls"))).booleanValue();
			SMTP_USERNAME = System.getProperty("smtp.username", smgr.getString("smtp.username"));
			SMTP_PASSWORD = System.getProperty("smtp.password", smgr.getString("smtp.password"));
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}

	private static void printCliHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + DailyLanguageCrawlerStatistic.class.getCanonicalName(), createOptions());
	}

	private static Options createOptions()
	{
		Options options = new Options();

		options.addOption(
				Option.builder().argName("date")
				.longOpt("date")
				.hasArg(true)
				.desc("discovery_date for statistic.[format: yyyy-MM-dd, default: yesterday]")
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
	/**
	 * K: country+language
	 * V: HashMap(K:["warehoused","published"], V:HashMap(K:source, V:saveCount)
	 */
	public Map<String, HashMap<String, HashMap<String, Integer>>> getLanguageArticleCountByTimeRange(Date startTime, Date endTime)
	{
		Map<String, HashMap<String, HashMap<String, Integer>>> mResult = new TreeMap<String, HashMap<String, HashMap<String, Integer>>>();
		ArrayList alData = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getLanguageArticleCountByTimeRange(startTime, endTime);
		for (int i = 0; i < alData.size(); i++)
		{
			HashMap mData = (HashMap)alData.get(i);
			String country  = capitalFirstCharacter((String)mData.get("country"));
			String language = capitalFirstCharacter((String)mData.get("language"));

			String locale = country + SEPARATER_SOURCE_TYPE + language;
			HashMap<String, HashMap<String, Integer>> mDetail = (HashMap<String, HashMap<String, Integer>>)mResult.get(locale);
			if (mDetail == null)
			{
				mDetail = new HashMap<String, HashMap<String, Integer>>();
				mDetail.put("warehoused", new HashMap<String, Integer>());
				mDetail.put("available",  new HashMap<String, Integer>());
				mResult.put(locale, mDetail);
			}
			String source = String.valueOf(mData.get("source"));
			int state = PrimitiveTypeUtil.getInt(mData.get("state"));
			int increament = PrimitiveTypeUtil.getInt(mData.get("save_count"));

			HashMap<String, Integer> mSourceAmount = (HashMap<String, Integer>)mDetail.get("warehoused");
			mSourceAmount = increaseAmount(mSourceAmount, source, increament);
			if (inveno.spider.common.model.Content.STATE_NORMAL == state)
			{
				mSourceAmount = (HashMap<String, Integer>)mDetail.get("available");
				mSourceAmount = increaseAmount(mSourceAmount, source, increament);
			}
		}

		return mResult;
	}
	public static String[] determineDisplaySource(HashMap<String, Integer> mData)
	{
		ArrayList<String> alSource = new ArrayList<String>();
		List<Map.Entry<String, Integer>> alData = new ArrayList<Map.Entry<String, Integer>>(mData.entrySet());
		Collections.sort(alData, new Comparator<Map.Entry<String, Integer>>() {
			//降序排序
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
			{
				return (o2.getValue() - o1.getValue());
			}
		});

		for (Map.Entry<String, Integer> entry : alData)
		{
			alSource.add((String)entry.getKey());
		}
		return (String[])alSource.toArray(new String[0]);
	}
	private static String generateMailContent(Date queryTime, Map<String, HashMap<String, HashMap<String, Integer>>> mResult, int topNSource)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<h2> Daily Language Crawler Statistic </h2>\n");
		sb.append("statistic time:" + DateUtil.dateToString(new Date(), DATETIME_PATTERN_MINUTE));

		sb.append("<br/>\n");
		sb.append("<table border=\"1\" style=\"solid black\">\n");
		sb.append("<tr>\n");
		sb.append("<td align=\"center\">country</td>\n");
		sb.append("<td align=\"center\">language</td>\n");
		sb.append("<td align=\"center\">topN</td>\n");
		sb.append("<td align=\"center\">source</td>\n");
		sb.append("<td align=\"center\">warehoused amount<br/>抓取量</td>\n");
		sb.append("<td align=\"center\">available amount<br/>可发布量</td>\n");
		sb.append("</tr>\n");

		for (String locale : mResult.keySet())
		{
			HashMap<String, HashMap<String, Integer>> mDailyData = mResult.get(locale);
			String[] arrSortedDisplaySource = determineDisplaySource(mDailyData.get("warehoused"));
			int nRow = (topNSource <= 0) ? arrSortedDisplaySource.length : Math.min(arrSortedDisplaySource.length, topNSource);
			String[] s = locale.split(String.valueOf(SEPARATER_SOURCE_TYPE));
			String country  = (s.length > 0) ? s[0] : "";
			String language = (s.length > 1) ? s[1] : "";
			for (int j = 0; j < nRow; j++)
			{
				String sortedDisplaySource = arrSortedDisplaySource[j];
				sb.append("<tr>\n");
				if (j == 0)
				{
					sb.append("<td align=\"center\" rowspan=\"" + nRow + "\">" + country + "</td>\n");
					sb.append("<td align=\"center\" rowspan=\"" + nRow + "\">" + language + "</td>\n");
				}
				sb.append("<td align=\"center\">" + (j+1) + "</td>");
				sb.append("<td>" + sortedDisplaySource + "</td>");
				int warehousedAmount = (null == mDailyData.get("warehoused").get(sortedDisplaySource)) ? 0 : PrimitiveTypeUtil.getInt(mDailyData.get("warehoused").get(sortedDisplaySource));
				int availableAmount  = (null == mDailyData.get("available").get(sortedDisplaySource)) ? 0 : PrimitiveTypeUtil.getInt(mDailyData.get("available").get(sortedDisplaySource));
				sb.append("<td>" + warehousedAmount + "</td>");
				sb.append("<td>" + availableAmount + "</td>");
				sb.append("</tr>\n");
			}
		}
		sb.append("</table>\n");
		return sb.toString();
	}
	public static String generateMailAttachment(Date queryTime, Map<String, HashMap<String, HashMap<String, Integer>>> mResult, String reportDirectory, String reportFileName) throws Exception
	{
		String html = generateMailContent(queryTime, mResult, -1);
		java.io.File tempFile = null;
		try
		{
			tempFile = java.io.File.createTempFile(reportFileName, "");
			org.apache.commons.io.FileUtils.writeStringToFile(tempFile, html, "utf8");
			java.io.File reportFile = new java.io.File(reportDirectory, reportFileName);
			org.apache.commons.io.FileUtils.copyFile(tempFile, reportFile);
			return reportFile.getCanonicalPath();
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (tempFile != null)
				tempFile.deleteOnExit();
		}
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

			String dateString = null;
			if (cmd.hasOption("date"))
			{
				dateString  = cmd.getOptionValue("date");
			}
			else
			{
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_YEAR, -1);
				dateString = DateUtil.dateToString(c.getTime(), DATE_PATTERN);
			}

			Date queryTime = DateUtil.stringToDate(dateString, DATE_PATTERN);
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			Date startTime = c.getTime();
			c.add(Calendar.DAY_OF_YEAR, 1);
			Date endTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);

			//country+language, ["warehousedAmount","availablePublishAmount"], source, amount
			DailyLanguageCrawlerStatistic task = new DailyLanguageCrawlerStatistic();
			Map<String, HashMap<String, HashMap<String, Integer>>> mResult = task.getLanguageArticleCountByTimeRange(startTime, endTime);

			String reportDirectory = "./DailyLanguageCrawlerStatistic";
			String reportFileName = "daily_language_crawler_detail_" + dateString + ".html";
			String mailContent = generateMailContent(queryTime, mResult, 20);
			ArrayList<Object> alAttached = new ArrayList<Object>();
			alAttached.add( generateMailAttachment(queryTime, mResult, reportDirectory, reportFileName) );

			String configPrefix = DailyLanguageCrawlerStatistic.class.getSimpleName() + ".";
			StringManager smgr = StringManager.getManager("system");
			String mailSender = smgr.getString("report.mail.sender");
			String[] mailReceiver = TextUtil.getStringList( smgr.getString(configPrefix + "report.mail.receiver") );
			String mailSubject = smgr.getString(configPrefix + "report.mail.subject");
			MailSender ms = new MailSender(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_TLS);
			ms.setMailFrom(mailSender);
			ms.setSubject(mailSubject);
			ms.setMailTo(mailReceiver);
			ms.setHtmlContent(mailContent);
			if (alAttached.size() > 0)
			{
				Object[] attached = alAttached.toArray();
				ms.setAttachedFile(attached);
			}
			ms.send();
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
}
