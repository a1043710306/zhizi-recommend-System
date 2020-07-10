package inveno.spider.reports.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import inveno.spider.common.mail.MailSender;
import inveno.spider.reports.dao.SourceCrawlerStatistic;
import inveno.spider.reports.dao.SourceCrawlerStatisticDao;
import inveno.spider.reports.facade.ContentFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.reports.util.Utils;
import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.util.TextUtil;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DailySourceCrawlerStatistic extends AbstractStatistic {
	private static final Logger log = Logger.getLogger(DailySourceCrawlerStatistic.class);

	private static String SMTP_HOST;
	private static int SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

	public static final int nDay = 7;

	static {
		try {
			StringManager smgr = StringManager.getManager("system");
			SMTP_HOST = System.getProperty("smtp.host", smgr.getString("smtp.host"));
			SMTP_PORT = Integer.parseInt(System.getProperty("smtp.port", smgr.getString("smtp.port")));
			SMTP_TLS = Boolean.valueOf(System.getProperty("smtp.tls", smgr.getString("smtp.tls"))).booleanValue();
			SMTP_USERNAME = System.getProperty("smtp.username", smgr.getString("smtp.username"));
			SMTP_PASSWORD = System.getProperty("smtp.password", smgr.getString("smtp.password"));
		} catch (Exception e) {
			log.fatal("", e);
		}
	}

	private static void printCliHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + DailySourceCrawlerStatistic.class.getCanonicalName(), createOptions());
	}

	private static Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder().argName("crawler_type").longOpt("crawler_type").hasArg(true)
				.desc("crawler type for statistic.[like: webant or aceport.").required(true).build());

		options.addOption(Option.builder().argName("date").longOpt("date").hasArg(true)
				.desc("discovery_date for statistic.[format: yyyy-MM-dd, default: yesterday]").required(false).build());

		options.addOption(Option.builder().argName("source").longOpt("source").hasArg(true)
				.desc("source list for statistic.[format: comma-separated string, like: instanews,daily.")
				.required(false).build());

		options.addOption(Option.builder().argName("help").longOpt("help").hasArg(false).desc("print help messages.")
				.required(false).build());

		return options;
	}

	/**
	 * K: crawlerType V: HashMap(K:source, V:saveCount)
	 */
	public static HashMap<String, Integer> getArticleCountByTimeRange(String[] arrSource,
			String crawlerType, Date startTime, Date endTime) {
		HashMap<String, Integer> mDetail = new HashMap<String,Integer>();
		ArrayList alData = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER)
				.getArticleCountByTimeRange(arrSource, crawlerType, startTime, endTime);
		for (int i = 0; i < alData.size(); i++) {
			HashMap mData = (HashMap) alData.get(i);
			String source = String.valueOf(mData.get("source")).toLowerCase();
			int crawlerCount = PrimitiveTypeUtil.getInt(mData.get("save_count"));
			mDetail.put(source, crawlerCount);
		}
		for (int i = 0; i < arrSource.length; i++) {
			String keySource = arrSource[i].toLowerCase();
			if (!mDetail.containsKey(keySource))
				mDetail.put(arrSource[i], 0);
		}
		log.info("getArticleCountByTimeRange has completed.");
		return mDetail;
	}

	public static String[] determineDisplaySource(HashMap<String, Integer> mData) {
		if (null == mData) {
			return new String[] {};
		}
		ArrayList<String> alSource = new ArrayList<String>();
		List<Map.Entry<String, Integer>> alData = new ArrayList<Map.Entry<String, Integer>>(mData.entrySet());
		Collections.sort(alData, new Comparator<Map.Entry<String, Integer>>() {
			// 降序排序
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});

		for (Map.Entry<String, Integer> entry : alData) {
			alSource.add((String) entry.getKey());
		}
		return (String[]) alSource.toArray(new String[0]);
	}

	public static HashMap<String, String> buildSourceFeedsHostMap(String crawlerType) {
		HashMap<String, String> mResult = new HashMap<String, String>();
		ArrayList alData = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listSourceFeeds(crawlerType);
		for (int i = 0; i < alData.size(); i++) {
			HashMap mData = (HashMap) alData.get(i);
			String source = ((String) mData.get("source")).toLowerCase();
			String sourceFeedsHost = (String) mData.get("source_feeds_host");
			mResult.put(source, sourceFeedsHost);
		}
		return mResult;
	}

	private static String generateMailContent(Date queryTime, String crawlerType,
			HashMap<String, HashMap<String, Integer>> mDailyData, int topNSource) {
		StringBuffer sb = new StringBuffer();

		sb.append("<h2> Daily Source Crawler Statistic For " + crawlerType + "</h2>\n");
		sb.append("statistic time:" + DateUtil.dateToString(new Date(), DATETIME_PATTERN_MINUTE));

		sb.append("<br/>\n");
		sb.append("<table border=\"1\" style=\"solid black\">\n");
		sb.append("<tr>\n");
		sb.append("<td align=\"center\">topN</td>\n");
		sb.append("<td align=\"center\">source</td>\n");
		sb.append("<td align=\"center\">source_feeds_host</td>\n");

		HashMap<String, String> mSourceFeedsHost = buildSourceFeedsHostMap(crawlerType);
		String[] arrSortedDisplaySource = null;
		for (int i = 0; i < nDay; i++) {
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			c.add(Calendar.DAY_OF_YEAR, -i);
			Date startTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
			sb.append("<td align=\"center\">" + queryDateString + "</td>\n");
			if (i == 0)
				arrSortedDisplaySource = determineDisplaySource(mDailyData.get(queryDateString));
		}
		sb.append("</tr>\n");
		for (int j = 0; j < arrSortedDisplaySource.length; j++) {
			if (topNSource > 0 && j >= topNSource)
				break;
			String sortedDisplaySource = arrSortedDisplaySource[j];
			String keySource = sortedDisplaySource.toLowerCase();
			String sourceFeedsHost = (null == mSourceFeedsHost.get(keySource)) ? ""
					: (String) mSourceFeedsHost.get(keySource);
			sb.append("<tr>\n");
			sb.append("<td align=\"center\">" + (j + 1) + "</td>");
			sb.append("<td>" + sortedDisplaySource + "</td>");
			sb.append("<td>" + sourceFeedsHost + "</td>");
			for (int i = 0; i < nDay; i++) {
				Calendar c = Calendar.getInstance();
				c.setTime(queryTime);
				c.add(Calendar.DAY_OF_YEAR, -i);
				Date startTime = c.getTime();
				String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
				int crawlerCount = (null == mDailyData.get(queryDateString).get(keySource)) ? 0
						: mDailyData.get(queryDateString).get(keySource);
				sb.append("<td align=\"center\">" + crawlerCount + "</td>\n");
			}
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

		return sb.toString();
	}

	private static void generateWeixinContentAndSend(String[] weichatReceiver, Date queryTime, String crawlerType,
			HashMap<String, HashMap<String, Integer>> mDailyData, int topNSource) {

		HashMap<String, String> mSourceFeedsHost = buildSourceFeedsHostMap(crawlerType);
		String[] arrSortedDisplaySource = null;

		for (int i = 0; i < nDay; i++) {
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			c.add(Calendar.DAY_OF_YEAR, -i);
			Date startTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
			if (i == 0)
				arrSortedDisplaySource = determineDisplaySource(mDailyData.get(queryDateString));
		}

		for (int j = 0; j < arrSortedDisplaySource.length; j++) {
			StringBuffer sb = new StringBuffer();
			sb.append("Source Crawler Statistic For " + crawlerType + "\\n");
			sb.append("--------------------------\\n");
			if (topNSource > 0 && j >= topNSource)
				break;
			String sortedDisplaySource = arrSortedDisplaySource[j];
			String keySource = sortedDisplaySource.toLowerCase();
			String sourceFeedsHost = (null == mSourceFeedsHost.get(keySource)) ? ""
					: (String) mSourceFeedsHost.get(keySource);
			sb.append("Top : " + (j + 1) + "\\n");
			sb.append("Source : " + sortedDisplaySource + "\\n");
			sb.append("sHost : " + sourceFeedsHost + "\\n");
			for (int i = 0; i < nDay; i++) {
				Calendar c = Calendar.getInstance();
				c.setTime(queryTime);
				c.add(Calendar.DAY_OF_YEAR, -i);
				Date startTime = c.getTime();
				String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
				sb.append("Date  : " + queryDateString + " \\n");
				int crawlerCount = (null == mDailyData.get(queryDateString).get(keySource)) ? 0
						: mDailyData.get(queryDateString).get(keySource);
				sb.append("Crawler Count" + crawlerCount + "\\n");
				sb.append("--------------------------\\n");
			}

			try {
				Utils.sendContentToWeichat(weichatReceiver, sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static String generateZipFile(String sourceFileName) throws Exception {
		File f = new File(sourceFileName);
		try {
			String targetFileName = sourceFileName + ".zip";
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFileName));
			ZipEntry ze = new ZipEntry(f.getName());
			zos.putNextEntry(ze);
			org.apache.commons.io.IOUtils.copy(new FileInputStream(sourceFileName), zos);
			zos.close();
			return targetFileName;
		} catch (Exception e) {
			throw e;
		} finally {
		}
	}

	public static String generateMailAttachment(Date queryTime, String crawlerType,
			HashMap<String, HashMap<String, Integer>> mDailyData, String reportDirectory, String reportFileName)
			throws Exception {
		String html = generateMailContent(queryTime, crawlerType, mDailyData, -1);
		java.io.File tempFile = null;
		try {
			tempFile = java.io.File.createTempFile(reportFileName, "");
			org.apache.commons.io.FileUtils.writeStringToFile(tempFile, html, "utf8");
			java.io.File reportFile = new java.io.File(reportDirectory, reportFileName);
			org.apache.commons.io.FileUtils.copyFile(tempFile, reportFile);
			return generateZipFile(reportFile.getCanonicalPath());
		} catch (Exception e) {
			throw e;
		} finally {
			if (tempFile != null)
				tempFile.deleteOnExit();
		}
	}

	public static void sendMail(String mailSender, String mailSubject, String[] mailReceiver, String mailContent,
			ArrayList<Object> alAttached) {
		try {
			log.info("mailSender:" + mailSender + ",mailSubject:" + mailSubject + ",alAttached size:"
					+ alAttached.size());
			MailSender ms = new MailSender(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_TLS);
			ms.setMailFrom(mailSender);
			ms.setSubject(mailSubject);
			ms.setMailTo(mailReceiver);
			ms.setHtmlContent(mailContent);
			if (alAttached.size() > 0) {
				Object[] attached = alAttached.toArray();
				ms.setAttachedFile(attached);
			}

			boolean fSuccess = false;
			int MAX_SEND_RETRY = 3;
			int retryCount = 0;
			while (true) {
				if (fSuccess || retryCount >= MAX_SEND_RETRY)
					break;
				try {
					ms.send();
					fSuccess = true;
				} catch (Exception e) {
					retryCount++;
					log.warn("[sendMail]", e);
				}
				Thread.currentThread().sleep(5000);
			}
		} catch (Exception e) {
			log.fatal("[sendMail]", e);
		}
	}

	/**
	 * 查询数据并插入数据库 auhtor : jianjie.zhu
	 * 
	 * @param dateString
	 */
	public static void insertYestodayDataToMysql(String dateString, String[] source, String crawlerType) {
		Calendar c = Calendar.getInstance();
		if (dateString == null || DateUtil.stringToDate(dateString) == null) {
			return;
		}
		log.info("insertYestodayDataToMysql has started.");
		// 设置开始时间
		Date startTime = DateUtil.stringToDate(dateString);
		// 设置结束时间
		c.setTime(DateUtil.stringToDate(dateString));
		c.add(Calendar.DAY_OF_YEAR, 1);
		Date endTime = c.getTime();
		HashMap<String, Integer> mCrawlerTypeData = getArticleCountByTimeRange(source, crawlerType,
				startTime, endTime);
		generateSqlAndOperateMysql(dateString, crawlerType, mCrawlerTypeData);
		log.info("insertYestodayDataToMysql has completed.");
	}

	/**
	 * 查询数据并插入数据库 auhtor : jianjie.zhu
	 * 
	 * @param dateString
	 */
	private static void generateSqlAndOperateMysql(String queryDateString, String crawlerType,
			 HashMap<String, Integer> mDailyData) {
		HashMap<String, String> mSourceFeedsHost = buildSourceFeedsHostMap(crawlerType);
		String[] arrSortedDisplaySource = determineDisplaySource(mDailyData);
		List<SourceCrawlerStatistic> sourceCrawlerStatisticsList = new ArrayList<SourceCrawlerStatistic>();
		for (int j = 0; j < arrSortedDisplaySource.length; j++) {
			String sortedDisplaySource = arrSortedDisplaySource[j];
			String keySource = sortedDisplaySource.toLowerCase();
			String sourceFeedsHost = (null == mSourceFeedsHost.get(keySource)) ? ""
					: (String) mSourceFeedsHost.get(keySource);
			int crawlerCount = (null == mDailyData.get(keySource)) ? 0
					: mDailyData.get(keySource);
			SourceCrawlerStatistic sourceCrawlerStatistic = new SourceCrawlerStatistic(keySource, crawlerType,
					sourceFeedsHost, queryDateString, crawlerCount);
			System.out.println("crawlerType: " +crawlerType + "keySource: " + keySource + "|crawlerCount: " + crawlerCount );
			sourceCrawlerStatisticsList.add(sourceCrawlerStatistic);
		}
		new SourceCrawlerStatisticDao().batchUpdate(sourceCrawlerStatisticsList);
		log.info("generateSqlAndOperateMysql has completed.");
	}

	public static void main(String[] args) {
		try {
			final CommandLine cmd = initParameters(args);
			if (cmd == null)
				return;
			final String crawlerType = getCrawlerType(cmd);
			final String dateString = getDateString(cmd);
			final String[] source = getSources(crawlerType);
			// modify by jianjie.zhu 2016-11-25
			new Thread(new Runnable() {
				public void run() {
					String targetDate = new String(dateString); 
					String targetcrawlerType= new String(crawlerType);
					String[] targetSource = Arrays.copyOf(source, source.length); /// ???
					log.info("dateString:" + dateString + ",crawler_type:" + targetcrawlerType);
					insertYestodayDataToMysql(targetDate, targetSource, targetcrawlerType);
				}
			}).start();
			genixSendMailLogic(crawlerType, dateString, source);
		} catch (Exception e) {
			log.fatal("", e);
		}
	}

	private static void genixSendMailLogic(String crawlerType, String dateString, String[] source) throws Exception {
		Date queryTime = DateUtil.stringToDate(dateString, DATE_PATTERN);
		// crawlerType, date, source, crawlerCount
		HashMap<String, HashMap<String, Integer>> mDailyData = new HashMap<String, HashMap<String, Integer>>();
		for (int i = 0; i < nDay; i++) {
			Calendar c = Calendar.getInstance();
			c.setTime(queryTime);
			c.add(Calendar.DAY_OF_YEAR, -i);
			Date startTime = c.getTime();
			c.add(Calendar.DAY_OF_YEAR, 1);
			Date endTime = c.getTime();
			String queryDateString = DateUtil.dateToString(startTime, DATE_PATTERN);
			// crawlerType, source, crawlerCount
			HashMap<String, Integer> mCrawlerTypeData = getArticleCountByTimeRange(source,crawlerType, startTime,
					endTime);
			// date, source, crawlerCount
			mDailyData.put(queryDateString, mCrawlerTypeData);
		}
		String reportDirectory = "./DailySourceCrawlerStatistic";
		String reportFileName = crawlerType + "_daily_source_crawler_detail_" + dateString + ".html";
		String mailContent = generateMailContent(queryTime, crawlerType, mDailyData, 30);
		ArrayList<Object> alAttached = new ArrayList<Object>();
		alAttached.add(generateMailAttachment(queryTime, crawlerType, mDailyData, reportDirectory, reportFileName));
		String configPrefix = DailySourceCrawlerStatistic.class.getSimpleName() + ".";
		StringManager smgr = StringManager.getManager("system");
		String mailSender = smgr.getString("report.mail.sender");
		String[] mailReceiver = TextUtil.getStringList(smgr.getString(configPrefix + "report.mail.receiver"));
		if ("aceport".equalsIgnoreCase(crawlerType)) {
			HashSet<String> hs = new HashSet<String>(Arrays.asList(mailReceiver));
			String[] _mailReceiver = TextUtil
					.getStringList(smgr.getString(configPrefix + "report.mail.receiver." + crawlerType));
			hs.addAll(Arrays.asList(_mailReceiver));
			mailReceiver = (String[]) hs.toArray(new String[0]);
		}
		String mailSubject = "[" + crawlerType + "]" + smgr.getString(configPrefix + "report.mail.subject");
		sendMail(mailSender, mailSubject, mailReceiver, mailContent, alAttached);
	}

	private static String[] getSources(String crawlerType) {
		HashSet<String> hsSource = new HashSet<String>();
		String[] source = null;
		ArrayList alSource = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getArticleSource(crawlerType);
		for (int i = 0; i < alSource.size(); i++) {
			HashMap mSource = (HashMap) alSource.get(i);
			hsSource.add((String) mSource.get("source"));
		}
		source = hsSource.toArray(new String[0]);
		log.info("getSources and source sort completed.");
		return source;
	}

	/**
	 * 获取crawler type
	 * 
	 * @param cmd
	 * @return
	 */
	private static String getCrawlerType(CommandLine cmd) {
		String crawlerType = "";
		if (cmd.hasOption("crawler_type")) {
			crawlerType = cmd.getOptionValue("crawler_type");
		} else {
			crawlerType = "webant";
		}
		return crawlerType;
	}

	private static String getDateString(CommandLine cmd) {
		String dateString = null;
		if (cmd.hasOption("date")) {
			dateString = cmd.getOptionValue("date");
		} else {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_YEAR, -1);
			dateString = DateUtil.dateToString(c.getTime(), DATE_PATTERN);
		}
		return dateString;
	}

	private static CommandLine initParameters(String[] args) throws Exception {
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(createOptions(), args);
		} catch (ParseException e) {
			printCliHelp();
			throw new Exception("Error in parsing argument:" + e.getMessage());
		}

		if (cmd.hasOption("help")) {
			printCliHelp();
			return null;
		}
		return cmd;
	}
}
