package inveno.spider.reports.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.mail.MailSender;
import inveno.spider.reports.dao.MediaCrawlerStatisticDao;
import inveno.spider.reports.facade.ContentFacade;
import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
public class DailyMediaCrawlerStatistic {
	private static final Logger log = Logger.getLogger(DailyMediaCrawlerStatistic.class);

	private static String _smtp_host;
	private static int    _smtp_port;
	private static boolean _smtp_tls;
	private static String _smtp_username;
	private static String _smtp_password;

	private final String _twitter_style = "blockquote,twitter-";
	private final String _facebook_style = "iframe,https://www.facebook.com/plugins/post.php";
	private final String _youtube_style = "iframe,www.youtube.com";
	private final String _instagram_style = "blockquote,instagram-media";
	
	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
			_smtp_host     = System.getProperty("smtp.host", smgr.getString("smtp.host"));
			_smtp_port     = Integer.parseInt(System.getProperty("smtp.port", smgr.getString("smtp.port")));
			_smtp_tls      = Boolean.valueOf(System.getProperty("smtp.tls", smgr.getString("smtp.tls"))).booleanValue();
			_smtp_username = System.getProperty("smtp.username", smgr.getString("smtp.username"));
			_smtp_password = System.getProperty("smtp.password", smgr.getString("smtp.password"));
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
	
	public DailyMediaCrawlerStatistic(){
		
	}
	
	public static void main(String[]args){
		DailyMediaCrawlerStatistic daily = new DailyMediaCrawlerStatistic();
		try {
			daily.staticMediaStyleNumDaily();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 统计媒体每日爬取量
	 * @throws Exception
	 */
	public void staticMediaStyleNumDaily() throws Exception{
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, 0);
		Date date = c.getTime();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		staticTwitterStyleNumDaily(sf.format(date));
		staticFacebookStyleNumDaily(sf.format(date));
		staticyoutubeStyleNumDaily(sf.format(date));
		staticInstagramStyleNumDaily(sf.format(date));
		SimpleDateFormat show = new SimpleDateFormat("yyyy-MM-dd");
		Calendar showCal = Calendar.getInstance();
		showCal.setTime(new Date());
		showCal.add(Calendar.DAY_OF_YEAR, -1);
		Date showDate = showCal.getTime(); 
		String showDateStr = show.format(showDate);
		sendToMailInfo(showDateStr);
	}
	
	
	public HashMap<String,Object> staticTwitterStyleNumDaily(String date) throws ParseException{
		long num = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getMediaStatisticNum(_twitter_style,date);
		HashMap<String,Object> daily_media = new HashMap<String,Object>(); 
		daily_media.put("num", num);
		daily_media.put("q_media_info", _twitter_style);
		daily_media.put("media", "twitter");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy-MM-dd 00:00:00").parse(date));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		daily_media.put("date", new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime()));
		ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).save(daily_media);
		return daily_media;
	}
	
	public HashMap<String,Object> staticFacebookStyleNumDaily(String date) throws ParseException{
		long num = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getMediaStatisticNum(_facebook_style,date);
		HashMap<String,Object> daily_media = new HashMap<String,Object>();
		daily_media.put("num", num);
		daily_media.put("q_media_info", _facebook_style);
		daily_media.put("media", "facebook");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy-MM-dd 00:00:00").parse(date));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		daily_media.put("date", new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime()));
		ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).save(daily_media);
		return daily_media;
	}
	
	public HashMap<String,Object> staticyoutubeStyleNumDaily(String date) throws ParseException{
		long num = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getMediaStatisticNum(_youtube_style,date);
		HashMap<String,Object> daily_media = new HashMap<String,Object>(); 
		daily_media.put("num", num);
		daily_media.put("q_media_info", _youtube_style);
		daily_media.put("media", "youtube");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy-MM-dd 00:00:00").parse(date));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		daily_media.put("date", new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime()));
		ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).save(daily_media);
		return daily_media;
	}
	
	public HashMap<String,Object> staticInstagramStyleNumDaily(String date) throws ParseException{
		long num = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getMediaStatisticNum(_instagram_style,date);
		HashMap<String,Object> daily_media = new HashMap<String,Object>(); 
		daily_media.put("num", num);
		daily_media.put("q_media_info", _instagram_style);
		daily_media.put("media", "instagram");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy-MM-dd 00:00:00").parse(date));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		daily_media.put("date", new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime()));
		ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).save(daily_media);
		daily_media.put("instagram", num);
		return daily_media;
	}
	
	public void sendToMailInfo(String date) throws Exception{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dbformat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date end_date = sf.parse(date);
		Calendar start_cal = Calendar.getInstance();
		start_cal.setTime(end_date);
		start_cal.add(Calendar.DAY_OF_YEAR, -7);
		String start_date = sf.format(start_cal.getTime());
		ArrayList db_res = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getRecentSevenDaysMedia(start_date,dbformat.format(end_date));
		String report_directory = "./DailyMediaCrawlerStatistic";
		String report_filename = "daily_media_crawler_detail_" + date + ".html";
		String email_content = getEmailContent(db_res);
		StringManager smgr = StringManager.getManager("system");
		String config_prefix = DailyMediaCrawlerStatistic.class.getSimpleName() + ".";
		String mail_sender = smgr.getString("report.mail.sender");
		String[] mail_receiver = TextUtil.getStringList(smgr.getString(config_prefix + "report.mail.receiver"));
		ArrayList<Object> all_attached = new ArrayList<Object>();
		all_attached.add(generateMailAttachment(report_filename, email_content, report_directory));
		String mail_subject = "Daily Media Crawler Statistic";
		sendMail(mail_sender, mail_subject, mail_receiver, email_content, all_attached);
	}
	
	public String getEmailContent(ArrayList  email_info) throws ParseException{
		StringBuffer sb = new StringBuffer();
		sb.append("<h2> Daily Media Crawler Statistic </h2>\n");
		sb.append("statistic time:" + DateUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:00"));
		sb.append("<br/>\n");
		sb.append("<table border=\"1\" style=\"solid black\">\n");
		sb.append("<tr>\n");
		sb.append("<td align=\"center\">date</td>\n");
		sb.append("<td align=\"center\">twitter</td>\n");
		sb.append("<td align=\"center\">facebook</td>\n");
		sb.append("<td align=\"center\">youtube</td>\n");
		sb.append("<td align=\"center\">instagram</td>\n");
		sb.append("</tr>\n");
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		for(Object email :email_info){
			HashMap<String,Object> one_email = (HashMap<String,Object>)email;
			sb.append("<tr>\n");
			sb.append("<td align=\"center\">"+fmt.format(one_email.get("date"))+"</td>\n");
			sb.append("<td align=\"center\">"+one_email.get("twitter")+"</td>\n");
			sb.append("<td align=\"center\">"+one_email.get("facebook")+"</td>\n");
			sb.append("<td align=\"center\">"+one_email.get("youtube")+"</td>\n");
			sb.append("<td align=\"center\">"+one_email.get("instagram")+"</td>\n");
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");
		return sb.toString();
	}
	
	public static String generateMailAttachment(String report_filename,String email_content,String report_dictionay)
			throws Exception {
		String html = email_content;
		java.io.File tempFile = null;
		try {
			tempFile = java.io.File.createTempFile(report_filename, "");
			org.apache.commons.io.FileUtils.writeStringToFile(tempFile, html, "utf8");
			java.io.File reportFile = new java.io.File(report_dictionay, report_filename);
			org.apache.commons.io.FileUtils.copyFile(tempFile, reportFile);
			return generateZipFile(reportFile.getCanonicalPath());
		} catch (Exception e) {
			throw e;
		} finally {
			if (tempFile != null)
				tempFile.deleteOnExit();
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

	
	public static void sendMail(String mail_sender, String mail_subject, String[] mail_receiver, String mail_content,
			ArrayList<Object> alAttached) {
		try {
			log.info("mailsender:" + mail_sender + ",mailsubject:" + mail_subject + ",alAttached size:"
					+ alAttached.size());
			MailSender ms = new MailSender(_smtp_host, _smtp_port, _smtp_username, _smtp_password, _smtp_tls);
			ms.setMailFrom(mail_sender);
			ms.setSubject(mail_subject);
			ms.setMailTo(mail_receiver);
			ms.setHtmlContent(mail_content);
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
	
	
	

	
}
