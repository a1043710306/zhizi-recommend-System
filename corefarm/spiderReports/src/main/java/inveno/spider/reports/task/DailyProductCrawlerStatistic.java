package inveno.spider.reports.task;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.dao.ProductCrawlerStatistic;
import inveno.spider.reports.dao.ProductCrawlerStatisticDao;
import inveno.spider.reports.facade.ContentFacade;
import inveno.spider.reports.util.Utils;
import inveno.spider.common.mail.MailSender;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
import tw.qing.util.PrimitiveTypeUtil;

import java.io.IOException;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DailyProductCrawlerStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailyProductCrawlerStatistic.class);

	private static String SMTP_HOST;
	private static int    SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

	public static final int nDay = 7;

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
		formatter.printHelp("java " + DailyProductCrawlerStatistic.class.getCanonicalName(), createOptions());
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
				Option.builder().argName("source")
				.longOpt("source")
				.hasArg(true)
				.desc("source list for statistic.[format: comma-separated string, like: instanews,daily.")
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
	 * K: crawlerType
	 * V: HashMap(K:source, V:saveCount){"hotoday-English", "hotoday-Hindi", "mata-Indonesian", "noticias-Spanish", "noticiasboom-Spanish","noticiasboomchile","noticiasboomcolombia"};
	 */
	public HashMap<String, HashMap<String, Integer>> getProductArticleCountByTimeRange(Date startTime, Date endTime)
	{
		String[] arrAvailableLanguage = new String[]{"English", "Hindi", "Indonesian", "Spanish"};
		HashMap<String, HashMap<String, Integer>> mResult = new HashMap<String, HashMap<String, Integer>>();
		//
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listContentByTimeRange(startTime, endTime);
		for (int i = 0; i < alContent.size(); i++)
		{
			HashMap mContent = (HashMap)alContent.get(i);
			String[] arrCrawledApp = new String[]{"hotoday", "mata", "noticias", "noticiasboom","noticiasboomchile","noticiasboomcolombia"};
			String[] arrPublishApp = enumFirmApp( (String)mContent.get("firm_app") );
			String language = capitalFirstCharacter((String)mContent.get("language"));
			int state = PrimitiveTypeUtil.getInt(mContent.get("state"));
			int increament = 1;
			for (int j = 0; j < arrCrawledApp.length; j++)
			{
				String[] arrLanguage = ("multilingual".equalsIgnoreCase(language)) ? arrAvailableLanguage : new String[]{language};
				for (int k = 0; k < arrLanguage.length; k++)
				{
					String product = arrCrawledApp[j] + "-" + arrLanguage[k];
					HashMap<String, Integer> mDetail = (HashMap<String, Integer>)mResult.get(product);
					if (mDetail == null)
					{
						mDetail = new HashMap<String, Integer>();
						mDetail.put("warehoused", 0);
						mDetail.put("available",  0);
						mResult.put(product, mDetail);
					}

					mDetail = increaseAmount(mDetail, "warehoused", increament);
				}
			}
			for (int j = 0; j < arrPublishApp.length; j++)
			{
				String[] arrLanguage = ("multilingual".equalsIgnoreCase(language)) ? arrAvailableLanguage : new String[]{language};
				for (int k = 0; k < arrLanguage.length; k++)
				{
					String product = arrPublishApp[j] + "-" + arrLanguage[k];
					HashMap<String, Integer> mDetail = (HashMap<String, Integer>)mResult.get(product);
					if (mDetail == null)
					{
						mDetail = new HashMap<String, Integer>();
						mDetail.put("warehoused", 0);
						mDetail.put("available",  0);
						mResult.put(product, mDetail);
					}

					if (inveno.spider.common.model.Content.STATE_NORMAL == state)
					{
						mDetail = increaseAmount(mDetail, "available", increament);
					}
				}
			}
		}

		return mResult;
	}
	private static String generateMailContent(Date queryTime, HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<h2>Daily Product Crawler Statistic</h2>\n");
		sb.append("statistic time:" + DateUtil.dateToString(new Date(), DATETIME_PATTERN_MINUTE));
		sb.append("<br/>\n");
		sb.append("<table border=\"1\" style=\"solid black\">\n");
		sb.append("<tr>\n");
		sb.append("<td align=\"center\">firmapp</td>\n");
		sb.append("<td align=\"center\">type</td>\n");

		String[] arrSortedDisplayProduct = new String[]{"hotoday-English", "hotoday-Hindi", "mata-Indonesian", "noticias-Spanish", "noticiasboom-Spanish","noticiasboomchile-Spanish","noticiasboomcolombia-Spanish"};
		for (int i = 0; i < nDay; i++)
		{
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			c.add(Calendar.DAY_OF_YEAR, -i);
			Date startTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
			sb.append("<td align=\"center\">" + queryDateString + "</td>\n");
		}
		sb.append("</tr>\n");
		for (int j = 0; j < arrSortedDisplayProduct.length; j++)
		{
			String sortedDisplayProduct = arrSortedDisplayProduct[j];
			HashMap<String, HashMap<String, Integer>> mDailyData = mResult.get(sortedDisplayProduct);
			if (mDailyData == null) continue;
			sb.append("<tr>\n");
			sb.append("<td rowspan=\"2\">" + sortedDisplayProduct + "</td>");
			sb.append("<td>crawler_amount</td>");
			for (int i = 0; i < nDay; i++)
			{
				Calendar c = Calendar.getInstance();
				c.setTime(queryTime);
				c.add(Calendar.DAY_OF_YEAR, -i);
				Date startTime = c.getTime();
				String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
				int amount = 0;
				if (mDailyData.get(queryDateString) != null)
				{
					amount = (null == mDailyData.get(queryDateString).get("warehoused")) ? 0 : mDailyData.get(queryDateString).get("warehoused");
				}
				sb.append("<td align=\"center\">" + amount + "</td>\n");
			}
			sb.append("</tr>\n");
			sb.append("<tr>\n");
			sb.append("<td>avalible_publish_amount</td>");
			for (int i = 0; i < nDay; i++)
			{
				Calendar c = Calendar.getInstance();
				c.setTime(queryTime);
				c.add(Calendar.DAY_OF_YEAR, -i);
				Date startTime = c.getTime();
				String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
				int amount = 0;
				if (mDailyData.get(queryDateString) != null)
				{
					amount = (null == mDailyData.get(queryDateString).get("available")) ? 0 : mDailyData.get(queryDateString).get("available");
				}
				sb.append("<td align=\"center\">" + amount + "</td>\n");
			}
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

		return sb.toString();
	}
	
	
	private static void generateWeixinContentAndSend(String[] weichatReceiver, Date queryTime, HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult)
	{
		String[] arrSortedDisplayProduct = new String[]{"hotoday-English", "hotoday-Hindi", "mata-Indonesian", "noticias-Spanish", "noticiasboom-Spanish"};

		for (int j = 0; j < arrSortedDisplayProduct.length; j++)
		{
			StringBuffer sb = new StringBuffer();
			sb.append("Product Crawler Statistic \\n");
			sb.append("--------------------------\\n");
			String sortedDisplayProduct = arrSortedDisplayProduct[j];
			HashMap<String, HashMap<String, Integer>> mDailyData = mResult.get(sortedDisplayProduct);
			if (mDailyData == null) continue;

			sb.append("firmapp : " +sortedDisplayProduct + "\\n");
			sb.append("--------------------------\\n");
			for (int i = 0; i < nDay; i++)
			{
				
				Calendar c = Calendar.getInstance();
				c.setTime(queryTime);
				c.add(Calendar.DAY_OF_YEAR, -i);
				Date startTime = c.getTime();
				String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
				sb.append("date  : " + queryDateString +" \\n" );
				int crawler_amount = 0;
				if (mDailyData.get(queryDateString) != null)
				{
					crawler_amount = (null == mDailyData.get(queryDateString).get("warehoused")) ? 0 : mDailyData.get(queryDateString).get("warehoused");
				}
				sb.append("crawler amount : " + crawler_amount +" \\n" );

				
				int avalible_publish_amount = 0;
				if (mDailyData.get(queryDateString) != null)
				{
					avalible_publish_amount = (null == mDailyData.get(queryDateString).get("available")) ? 0 : mDailyData.get(queryDateString).get("available");
				}
				sb.append("avalible amount : " + avalible_publish_amount +" \\n" );			
				sb.append("--------------------------\\n");

			}
			
			try {
				Utils.sendContentToWeichat(weichatReceiver,sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		

	}
	
	
	public static String generateMailAttachment(Date queryTime, HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult, String reportDirectory, String reportFileName) throws Exception
	{
		String html = generateMailContent(queryTime, mResult);
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
	
	
	/**
	 * 查询数据并插入数据库
	 * auhtor : jianjie.zhu
	 * @param dateString
	 */
	public static void insertYestodayDataToMysql(String dateString){
		Calendar c = Calendar.getInstance();
		if (dateString == null || DateUtil.stringToDate(dateString)==null) {
			return;
		}
	
		//设置开始时间
		Date startTime = DateUtil.stringToDate(dateString);
		
		//设置结束时间
		c.setTime(DateUtil.stringToDate(dateString));
		c.add(Calendar.DAY_OF_YEAR, 1);
		Date endTime = c.getTime();
		
		DailyProductCrawlerStatistic task = new DailyProductCrawlerStatistic();
		HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
		HashMap<String, HashMap<String, Integer>> mProductArticleAmount = task.getProductArticleCountByTimeRange(startTime, endTime);
		for (String product : mProductArticleAmount.keySet())
		{
			//date, ["warehoused", "available"], amount
			HashMap<String, HashMap<String, Integer>> mDailyData = (HashMap<String, HashMap<String, Integer>>)mResult.get(product);
			if (mDailyData == null)
			{
				mDailyData = new HashMap<String, HashMap<String, Integer>>();
				mResult.put(product, mDailyData);
			}
			mDailyData.put(dateString, mProductArticleAmount.get(product));
		}
		generateSqlAndOperateMysql(dateString, mResult);
	}
	
	
	/**
	 * 查询数据并插入数据库
	 * auhtor : jianjie.zhu
	 * @param dateString  {"hotoday-English", "hotoday-Hindi", "mata-Indonesian", "noticias-Spanish", "noticiasboom-Spanish","noticiasboomchile","noticiasboomcolombia"};
	 */
	private static void generateSqlAndOperateMysql(String queryTime, HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult)
	{
		

		String[] arrSortedDisplayProduct = new String[]{"hotoday-English", "hotoday-Hindi", "mata-Indonesian", "noticias-Spanish", "noticiasboom-Spanish","noticiasboomchile-Spanish","noticiasboomcolombia-Spanish"};
		List<ProductCrawlerStatistic> productCrawlerStatisticsList = new ArrayList<ProductCrawlerStatistic>();
		for (int j = 0; j < arrSortedDisplayProduct.length; j++)
		{
			String sortedDisplayProduct = arrSortedDisplayProduct[j];
			HashMap<String, HashMap<String, Integer>> mDailyData = mResult.get(sortedDisplayProduct);
			if (mDailyData == null) continue;
			
			int warehoused = 0;
			if (mDailyData.get(queryTime) != null)
			{
				warehoused = (null == mDailyData.get(queryTime).get("warehoused")) ? 0 : mDailyData.get(queryTime).get("warehoused");
			}
			productCrawlerStatisticsList.add(new  ProductCrawlerStatistic(sortedDisplayProduct, "crawler_amount", queryTime, warehoused));
			

			int available = 0;
			if (mDailyData.get(queryTime) != null)
			{
				available = (null == mDailyData.get(queryTime).get("available")) ? 0 : mDailyData.get(queryTime).get("available");
			}
			productCrawlerStatisticsList.add(new  ProductCrawlerStatistic(sortedDisplayProduct, "avalible_publish_amount", queryTime, available));

		}
		
		new ProductCrawlerStatisticDao().batchUpdate(productCrawlerStatisticsList);

	}
	
	public static void main(String[] args)
	{
		try
		{
			String dateString = parserParamters(args);
			
			if (dateString == null) return ;
			
			// modify by jianjie.zhu 2016-11-22
			final String target = new String(dateString);
			new Thread(new Runnable() {
				
				public void run() {
					insertYestodayDataToMysql(target);		
				}
			}).start();
			
			
			
			genixSendMailLogic(dateString);
		
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}

	private static String parserParamters(String[] args) throws Exception {
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
			return null;
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
		return dateString;
	}

	private static void genixSendMailLogic(String dateString)
			throws Exception, MessagingException, AddressException, IOException {
		Date queryTime = DateUtil.stringToDate(dateString, DATE_PATTERN);
		//prouduct, date, ["warehoused", "available"], amount
		HashMap<String, HashMap<String, HashMap<String, Integer>>> mResult = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
		for (int i = 0; i < nDay; i++)
		{
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			c.add(Calendar.DAY_OF_YEAR, -i);
			Date startTime = c.getTime();
			c.add(Calendar.DAY_OF_YEAR, 1);
			Date endTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);

			//product, ["warehoused", "available"], amount
			DailyProductCrawlerStatistic task = new DailyProductCrawlerStatistic();
			HashMap<String, HashMap<String, Integer>> mProductArticleAmount = task.getProductArticleCountByTimeRange(startTime, endTime);
			for (String product : mProductArticleAmount.keySet())
			{
				//date, ["warehoused", "available"], amount
				HashMap<String, HashMap<String, Integer>> mDailyData = (HashMap<String, HashMap<String, Integer>>)mResult.get(product);
				if (mDailyData == null)
				{
					mDailyData = new HashMap<String, HashMap<String, Integer>>();
					mResult.put(product, mDailyData);
				}
				mDailyData.put(queryDateString, mProductArticleAmount.get(product));
			}
		}

		String reportDirectory = "./DailyProductCrawlerStatistic";
		String reportFileName = "daily_product_crawler_detail_" + dateString + ".html";
		String mailContent = generateMailContent(queryTime, mResult);
		ArrayList<Object> alAttached = new ArrayList<Object>();
		alAttached.add( generateMailAttachment(queryTime, mResult, reportDirectory, reportFileName) );

		String configPrefix = DailyProductCrawlerStatistic.class.getSimpleName() + ".";
		StringManager smgr = StringManager.getManager("system");
		String mailSender = smgr.getString("report.mail.sender");
		String[] mailReceiver = TextUtil.getStringList( smgr.getString(configPrefix + "report.mail.receiver") );
		String mailSubject = smgr.getString(configPrefix + "report.mail.subject");
		
		
		// send content to weichat   modify bu jianjie.zhu 2016-11-23
		//String[] weichatReceiver = TextUtil.getStringList(smgr.getString("report.weichat.receiver"));
		//generateWeixinContentAndSend(weichatReceiver,queryTime, mResult);
		
		
		MailSender ms = new MailSender(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_TLS);
		ms.setMailFrom(mailSender);
		ms.setSubject(mailSubject);
		ms.setMailTo(mailReceiver);
		ms.setHtmlContent(mailContent);
		ms.send();
	}
}
