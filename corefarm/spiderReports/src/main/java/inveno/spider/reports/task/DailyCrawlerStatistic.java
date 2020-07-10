package inveno.spider.reports.task;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import inveno.spider.common.mail.MailSender;
import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.reports.util.ReportHelper;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.util.TextUtil;

import java.util.*;

/**
 * Created by Genix.Li on 2016/05/06.
 */
public class DailyCrawlerStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailyCrawlerStatistic.class);

	private static String SMTP_HOST;
	private static int    SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

	private String strStatisticTime;
	private String strArticleFetchDate;
	private java.io.File fReportDirectory;

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

	public DailyCrawlerStatistic(String _strArticleFetchDate)
	{
		Calendar c = Calendar.getInstance();
		strStatisticTime    = DateUtil.dateToString(c.getTime(), DATETIME_PATTERN_MINUTE);
		strArticleFetchDate = _strArticleFetchDate;
		fReportDirectory = new java.io.File("reports" + java.io.File.separator + strArticleFetchDate);
		if (!fReportDirectory.exists())
			fReportDirectory.mkdirs();
	}

	private String determineCrawlerName(String rss_name, int getType)
	{
		String crawler_name = "unknown";
		if (rss_name.indexOf("dailyhunt") >= 0)
		{
			crawler_name = "dailyhunt";
		}
		else if (rss_name.indexOf("hubii") >= 0)
		{
			crawler_name = "hubii";
		}
		else if (getType == 99)
		{
			crawler_name = "newshoo";
		}
		else if (getType == 5)
		{
			crawler_name = "inveno";
		}
		return crawler_name;
	}
	private String[] determineCountry(int rssId, HashMap<Integer, HashSet<Integer>> mFirmSource)
	{
		ArrayList<String> alCountry = new ArrayList<String>();
		if ( ((HashSet)mFirmSource.get(54)).contains(rssId) )
			alCountry.add("India");
		else if ( ((HashSet)mFirmSource.get(55)).contains(rssId) )
			alCountry.add("Indonesia");
		else if ( ((HashSet)mFirmSource.get(56)).contains(rssId) )
			alCountry.add("Malaysia");
		else if ( ((HashSet)mFirmSource.get(57)).contains(rssId) )
			alCountry.add("Vietnam");
		/*
		else
			alCountry.add("Unknown");
		*/
		return (String[])alCountry.toArray(new String[0]);
	}
	private String generateContentInCategoryMajor(String[] arrCrawlerName, HashSet<String> hsCategory, HashMap<String, Integer> mCrawlerCount, HashMap<String, Integer> mPublishCount)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<meta charset=\"UTF-8\" />");
		sb.append("<style type=\"text/css\">");
		sb.append("table, th, td {");
		sb.append("border: 1px solid black;");
		sb.append("}");
		sb.append("</style>");
		sb.append("统计时间：" + strStatisticTime);
		sb.append("<br/>");
		sb.append("文章入库日期：" + strArticleFetchDate);
		sb.append("<br/>");
		sb.append("<table>");
		sb.append("<tr bgcolor=\"#54823\">");
		sb.append("<td rowspan=\"2\" align=\"center\">抓取类型</td>");
		for (String category_name : hsCategory)
		{
			sb.append("<td colspan=\"2\" align=\"center\">" + category_name + "</td>");
		}
		sb.append("<td colspan=\"2\" bgcolor=\"#FFF2CC\">总计</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		for (String category_name : hsCategory)
		{
			sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">抓取量</td>");
			sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">发布量</td>");
		}
		sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">抓取量</td>");
		sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">发布量</td>");
		sb.append("</tr>");

		HashMap<String, Integer> mPublishCategoryCount = new HashMap<String, Integer>();
		HashMap<String, Integer> mCrawlerCategoryCount = new HashMap<String, Integer>();
		for (int i = 0; i < arrCrawlerName.length; i++)
		{
			String[] s = TextUtil.getStringList(arrCrawlerName[i]);
			String country     = s[0];
			String language    = s[1];
			String crawler_name = s[2];
			sb.append("<tr>");
			sb.append("<td>" + TextUtil.getString(new String[]{country, language, crawler_name}, " ") + "</td>");
			int totalPublishCount = 0;
			int totalCrawlerCount = 0;
			for (String category_name : hsCategory)
			{
				String key = TextUtil.getString(new String[]{arrCrawlerName[i], category_name});
				int crawlerCount = (null == mCrawlerCount.get(key)) ? 0 : PrimitiveTypeUtil.getInt(mCrawlerCount.get(key));
				int publishCount = (null == mPublishCount.get(key)) ? 0 : PrimitiveTypeUtil.getInt(mPublishCount.get(key));
				sb.append("<td align=\"right\">" + crawlerCount + "</td>");
				sb.append("<td align=\"right\">" + publishCount + "</td>");
				totalCrawlerCount += crawlerCount;
				totalPublishCount += publishCount;
				int categoryCrawlerCount = (null == mCrawlerCategoryCount.get(category_name)) ? 0 : PrimitiveTypeUtil.getInt(mCrawlerCategoryCount.get(category_name));
				int categoryPublishCount = (null == mPublishCategoryCount.get(category_name)) ? 0 : PrimitiveTypeUtil.getInt(mPublishCategoryCount.get(category_name));
				categoryPublishCount += publishCount;
				categoryCrawlerCount += crawlerCount;
				mCrawlerCategoryCount.put(category_name, categoryCrawlerCount);
				mPublishCategoryCount.put(category_name, categoryPublishCount);
			}
			sb.append("<td align=\"right\">" + totalCrawlerCount + "</td>");
			sb.append("<td align=\"right\">" + totalPublishCount + "</td>");
			sb.append("</tr>");
		}
		sb.append("<tr>");
		sb.append("<td bgcolor=\"#FFF2CC\" align=\"center\">总计</td>");
		for (String category_name : hsCategory)
		{
			int categoryCrawlerCount = (null == mCrawlerCategoryCount.get(category_name)) ? 0 : PrimitiveTypeUtil.getInt(mCrawlerCategoryCount.get(category_name));
			int categoryPublishCount = (null == mPublishCategoryCount.get(category_name)) ? 0 : PrimitiveTypeUtil.getInt(mPublishCategoryCount.get(category_name));
			sb.append("<td align=\"right\" bgcolor=\"#FFF2CC\">" + categoryCrawlerCount + "</td>");
			sb.append("<td align=\"right\" bgcolor=\"#FFF2CC\">" + categoryPublishCount + "</td>");
		}		
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	private String generateContentInCountryMajor(String[] arrCrawlerName, HashSet<String> hsCategory, HashMap<String, Integer> mCrawlerCount, HashMap<String, Integer> mPublishCount)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<meta charset=\"UTF-8\" />");
		sb.append("<style type=\"text/css\">");
		sb.append("table, th, td {");
		sb.append("border: 1px solid black;");
		sb.append("}");
		sb.append("</style>");
		sb.append("统计时间：" + strStatisticTime);
		sb.append("<br/>");
		sb.append("文章入库日期：" + strArticleFetchDate);
		sb.append("<br/>");
		sb.append("<table>");
		sb.append("<tr bgcolor=\"#54823\">");
		sb.append("<td colspan=\"2\" align=\"center\">抓取类型</td>");
		for (int i = 0; i < arrCrawlerName.length; i++)
		{
			String[] s = TextUtil.getStringList(arrCrawlerName[i]);
			String country     = s[0];
			String language    = s[1];
			String crawler_name = s[2];
			sb.append("<td>" + TextUtil.getString(new String[]{country, language, crawler_name}, "<br/>") + "</td>");
		}
		sb.append("<td bgcolor=\"#FFF2CC\" valign=\"middle\" align=\"center\">总计</td>");
		sb.append("</tr>");

		HashMap<String, Integer> mTotalCrawlerCount = new HashMap<String, Integer>();
		HashMap<String, Integer> mTotalPublishCount = new HashMap<String, Integer>();
		for (String category_name : hsCategory)
		{
			for (int j = 0; j < 2; j++)
			{
				sb.append("<tr>");
				if (j == 0)
				{
					sb.append("<td rowspan=\"2\" bgcolor=\"#54823\" valign=\"middle\" align=\"center\">" + category_name + "</td>");
					sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">抓取量</td>");
				}
				else
				{
					sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">发布量</td>");
				}
				int categoryCount = 0;
				for (int i = 0; i < arrCrawlerName.length; i++)
				{
					String key = TextUtil.getString(new String[]{arrCrawlerName[i], category_name});
					HashMap mCount = (j == 0) ? mCrawlerCount : mPublishCount;
					int count = (null == mCount.get(key)) ? 0 : PrimitiveTypeUtil.getInt(mCount.get(key));
					sb.append("<td align=\"right\">" + count + "</td>");
					categoryCount += count;

					HashMap mTotalCount = (j == 0) ? mTotalCrawlerCount : mTotalPublishCount;
					int totalCount = (null == mTotalCount.get(arrCrawlerName[i])) ? 0 : PrimitiveTypeUtil.getInt(mTotalCount.get(arrCrawlerName[i]));
					totalCount += count;
					mTotalCount.put(arrCrawlerName[i], totalCount);
				}
				sb.append("<td align=\"right\">" + categoryCount + "</td>");
				sb.append("</tr>");
			}
		}
		for (int j = 0; j < 2; j++)
		{
			sb.append("<tr>");
			if (j == 0)
			{
				sb.append("<td rowspan=\"2\" bgcolor=\"#FFF2CC\" valign=\"middle\" align=\"center\">总计</td>");
				sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">抓取量</td>");
			}
			else
			{
				sb.append("<td bgcolor=\"#E2EFDA\" align=\"center\">发布量</td>");
			}
			for (int i = 0; i < arrCrawlerName.length; i++)
			{
				String key = arrCrawlerName[i];
				HashMap mTotalCount = (j == 0) ? mTotalCrawlerCount : mTotalPublishCount;
				int count = (null == mTotalCount.get(key)) ? 0 : PrimitiveTypeUtil.getInt(mTotalCount.get(key));
				sb.append("<td align=\"right\">" + count + "</td>");
			}
			sb.append("</tr>");
		}
		
		return sb.toString();
	}
	public ArrayList<Object> generateFilterReasonAttach(String templateFileName, HashMap<String, ArrayList<String>> hmCrawlerRss, String strArticleFetchDate) throws Exception
	{
		ArrayList<Object> alAttach = new ArrayList<Object>();
		ArrayList alReason = ReportFacade.getInstance().listFilteredReason();
		for (Map.Entry<String, ArrayList<String>> rssEntry : hmCrawlerRss.entrySet())
		{
			HashMap mMeta    = new HashMap();
			ArrayList alData = new ArrayList();

			String crawlerName = rssEntry.getKey();
			String[] arrRssId  = (String[])((ArrayList<String>)rssEntry.getValue()).toArray(new String[0]);

			HashMap<String, HashMap> mRssRelease = ReportFacade.getInstance().getRssReleaseFlag(arrRssId);
			ArrayList alCandidateArticle = ReportFacade.getInstance().getDailyCandidateArticle(arrRssId, strArticleFetchDate);
			ArrayList alArticle = new ArrayList();
			if (alCandidateArticle != null) alArticle.addAll( alCandidateArticle );
			HashMap mFilteredReason = new HashMap();
			for (int j = 0; alArticle != null && j < alArticle.size(); j++)
			{
				HashMap mArticle = ((HashMap)alArticle.get(j));
				String rssId = String.valueOf(mArticle.get("rss_id"));

				HashMap mRssFlag = (HashMap)mRssRelease.get(rssId);
				int releaseFlag = PrimitiveTypeUtil.getInt( mRssFlag.get("releaseFlag") );
				boolean fChannelRelease = (releaseFlag == 1);
				if (mArticle.get("filtered") != null)
				{
					int filtered = PrimitiveTypeUtil.getInt(mArticle.get("filtered"));
					if (fChannelRelease && filtered == 0)
						filtered = -1;
					int nFilteredCount = (null == mFilteredReason.get( filtered )) ? 0 : PrimitiveTypeUtil.getInt(mFilteredReason.get(filtered));
					nFilteredCount++;
					mFilteredReason.put(filtered, nFilteredCount);
				}
			}

			for (int j = 0; j < alReason.size(); j++)
			{
				HashMap mReason = (HashMap)alReason.get(j);
				int code = Integer.parseInt(String.valueOf(mReason.get("code")));
				HashMap mData = new HashMap();
				mData.put("code", code);
				mData.put("type_name", mReason.get("type_name"));
				if (null == mFilteredReason.get(code))
					mData.put("filtered_count", 0);
				else
					mData.put("filtered_count", PrimitiveTypeUtil.getInt(mFilteredReason.remove(code)));
				alData.add(mData);
			}
			for (Object obj : mFilteredReason.entrySet())
			{
				Map.Entry entry = (Map.Entry)obj;
				int code = Integer.parseInt(String.valueOf(entry.getKey()));
				HashMap mData = new HashMap();
				mData.put("code", code);
				mData.put("type_name", "not auto release / unknown");
				mData.put("filtered_count", PrimitiveTypeUtil.getInt(mFilteredReason.get(code)));
				alData.add(mData);
			}
			String reportFileName = "statistic_filtered_reason_" + crawlerName + "_" + strArticleFetchDate + ".html";
			String filename = ReportHelper.prepareReportFile(new java.io.File(templateFileName), fReportDirectory, reportFileName, mMeta, alData);
			alAttach.add(filename);
		}
		return alAttach;
	}
	public HashMap<String, Object> generateMailContent(String templateFileName) throws Exception
	{
		ArrayList alPublishCount = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listPublishContentArticleCount(strArticleFetchDate);
		ArrayList alCrawlerCount = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listCrawlerContentArticleCount(strArticleFetchDate);
		//K: "country,language,crawler_name,catgory", V: article count
		HashMap<String, Integer> mCrawlerCount = new HashMap<String, Integer>();
		HashMap<String, Integer> mPublishCount = new HashMap<String, Integer>();
		HashSet<String> hsCategory = new HashSet<String>();

		HashMap<String, ArrayList<String>> hmCrawlerRss = new HashMap<String, ArrayList<String>>();
		HashMap<Integer, HashSet<Integer>> mFirmSource = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getFirmSource();
		//calculate publish count
		for (int i = 0; i < alPublishCount.size(); i++)
		{
			HashMap mPublishInfo = (HashMap)alPublishCount.get(i);
			int rss_id = PrimitiveTypeUtil.getInt( mPublishInfo.get("id") );
			String rss_name  = ((String)mPublishInfo.get("rss_name")).toLowerCase();
			String[] arrCountry = determineCountry(rss_id, mFirmSource);
			String language  = ((String)mPublishInfo.get("rss_language")).toLowerCase();
			String category  = ((String)mPublishInfo.get("category_name")).toLowerCase();
			int articleCount = PrimitiveTypeUtil.getInt(mPublishInfo.get("article_count"));
			int getType = PrimitiveTypeUtil.getInt(mPublishInfo.get("get_type"));
			String crawler_name = determineCrawlerName(rss_name, getType);

			hsCategory.add( category );
			for (int j = 0; arrCountry != null && j < arrCountry.length; j++)
			{
				String country = arrCountry[j];
				String full_crawler_name = TextUtil.getString( new String[]{country, language, crawler_name} );

				ArrayList<String> alCrawlerRss = (ArrayList<String>)hmCrawlerRss.get(full_crawler_name);
				if (alCrawlerRss == null)
				{
					alCrawlerRss = new ArrayList<String>();
					hmCrawlerRss.put(full_crawler_name, alCrawlerRss);
				}
				if (alCrawlerRss.indexOf(String.valueOf(rss_id)) < 0)
					alCrawlerRss.add(String.valueOf(rss_id));

				String key = TextUtil.getString(new String[]{country, language, crawler_name, category});
				Integer count = null;
				//加總發佈量
				count = (Integer)mPublishCount.get(key);
				if (null == count)
				{
					count = new Integer(0);
				}
				count += articleCount;
				mPublishCount.put(key, count);
				//加總爬取量=已發佈量+未發佈量 (先加已發佈量)
				count = (Integer)mCrawlerCount.get(key);
				if (null == count)
				{
					count = new Integer(0);
				}
				count += articleCount;
				mCrawlerCount.put(key, count);
			}
		}
		//calculate crawler count, and adding it to publish count
		for (int i = 0; i < alCrawlerCount.size(); i++)
		{
			HashMap mCrawlerInfo = (HashMap)alCrawlerCount.get(i);
			int rss_id = PrimitiveTypeUtil.getInt( mCrawlerInfo.get("id") );
			String rss_name  = ((String)mCrawlerInfo.get("rss_name")).toLowerCase();
			String[] arrCountry = determineCountry(rss_id, mFirmSource);
			String language  = ((String)mCrawlerInfo.get("rss_language")).toLowerCase();
			String category  = ((String)mCrawlerInfo.get("category_name")).toLowerCase();
			int articleCount = PrimitiveTypeUtil.getInt(mCrawlerInfo.get("article_count"));
			int getType = PrimitiveTypeUtil.getInt(mCrawlerInfo.get("get_type"));
			String crawler_name = determineCrawlerName(rss_name, getType);

			hsCategory.add( category );
			for (int j = 0; arrCountry != null && j < arrCountry.length; j++)
			{
				String country = arrCountry[j];
				String full_crawler_name = TextUtil.getString( new String[]{country, language, crawler_name} );

				ArrayList<String> alCrawlerRss = (ArrayList<String>)hmCrawlerRss.get(full_crawler_name);
				if (alCrawlerRss == null)
				{
					alCrawlerRss = new ArrayList<String>();
					hmCrawlerRss.put(full_crawler_name, alCrawlerRss);
				}
				if (alCrawlerRss.indexOf(String.valueOf(rss_id)) < 0)
					alCrawlerRss.add(String.valueOf(rss_id));

				String key = TextUtil.getString(new String[]{country, language, crawler_name, category});
				//加總爬取量=已發佈量+未發佈量 (加上未發佈量)
				Integer count = (Integer)mCrawlerCount.get(key);
				if (null == count)
				{
					count = new Integer(0);
				}
				count += articleCount;
				mCrawlerCount.put(key, count);
			}
		}

		ArrayList<String> allKeys = new ArrayList<String>();
		String[] arrCrawlerName = (String[])hmCrawlerRss.keySet().toArray(new String[0]);
		Arrays.sort(arrCrawlerName);
		for (int i = 0; i < arrCrawlerName.length; i++)
		{
			for (String category_name : hsCategory)
			{
				allKeys.add( TextUtil.getString(new String[]{arrCrawlerName[i], category_name}) );
			}
		}

		HashMap hmMail = new HashMap();
		hmMail.put("content", generateContentInCountryMajor(arrCrawlerName, hsCategory, mCrawlerCount, mPublishCount));
		hmMail.put("attachment", generateFilterReasonAttach("source_filter_reasons.tpl", hmCrawlerRss, strArticleFetchDate));
		return hmMail;
	}

	private static void printCliHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + DailyCrawlerStatistic.class.getCanonicalName(), createOptions());
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

			DailyCrawlerStatistic task = new DailyCrawlerStatistic(dateString);
			String configPrefix = DailyCrawlerStatistic.class.getSimpleName() + ".";
			HashMap mData = task.generateMailContent(null);
			ArrayList<Object> alAttached = new ArrayList<Object>();
			String mailContent = (String)mData.get("content");
			if (null != mData.get("attachment"))
			{
				alAttached.addAll((ArrayList<Object>)mData.get("attachment"));
			}
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
