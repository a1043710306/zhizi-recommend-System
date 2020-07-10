package inveno.spider.reports.task;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.common.mail.MailSender;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DailySourceCategoryStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailySourceCategoryStatistic.class);

	private static String SMTP_HOST;
	private static int    SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

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
		formatter.printHelp("java " + DailySourceCategoryStatistic.class.getCanonicalName(), createOptions());
	}

	private static Options createOptions()
	{
		Options options = new Options();

		options.addOption(
				Option.builder().argName("source")
				.longOpt("source")
				.hasArg(true)
				.desc("source list for statistic.[format: comma-separated string, like: instanews,daily.")
				.required(true)
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

			String source = cmd.getOptionValue("source");
			HashMap<String, HashMap<String, Integer>> mVersionStatistic = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getArticleCountGroupByCategory(source);
			HashMap<String, String> mChannelCategory = ReportFacade.getInstance().getChannelCategoryMappingBySource(source);
			HashMap<String, String> mSystemCategory  = ReportFacade.getInstance().getSystemCategoryNameMappingBySource(source);

			StringBuffer sb = new StringBuffer();
			sb.append("<meta charset=\"UTF-8\">\n");
			for (String version : mVersionStatistic.keySet())
			{
				sb.append("<h2> Version Category Statistic for " + version + "</h2>\n");
				sb.append("statistic time:" + DateUtil.dateToString(new Date(), DATETIME_PATTERN_MINUTE));
				sb.append("<br/>\n");
				sb.append("<table border=\"1\" style=\"solid black\">\n");
				sb.append("<tr>\n");
				sb.append("<td align=\"center\">channel_category_name</td>\n");
				sb.append("<td align=\"center\">channel_category_id</td>\n");
				sb.append("<td align=\"center\">categoryName</td>\n");
				sb.append("<td align=\"center\">articleCount</td>\n");
				sb.append("</tr>\n");
				HashMap<String, Integer> mStatistic = mVersionStatistic.get(version);
				for (Map.Entry<String, Integer> entry : mStatistic.entrySet())
				{
					String channel_category_id = (String)entry.getKey();
					String channel_category_name = mChannelCategory.get(channel_category_id);
					String category_name = (String)mSystemCategory.get(channel_category_id);
					sb.append("<tr>\n");
					sb.append("<td>" + channel_category_name + "</td>\n");
					sb.append("<td>" + channel_category_id + "</td>\n");
					sb.append("<td>\n");
					if (category_name != null)
						sb.append(category_name);
					sb.append("</td>\n");
					sb.append("<td align=\"right\">" + entry.getValue() + "</td>\n");
					sb.append("</tr>\n");
				}
				sb.append("</table>\n");
			}

			String mailContent = sb.toString();

			String configPrefix = DailySourceCategoryStatistic.class.getSimpleName() + ".";
			StringManager smgr = StringManager.getManager("system");
			String mailSender = smgr.getString("report.mail.sender");
			String[] mailReceiver = TextUtil.getStringList( smgr.getString(configPrefix + "report.mail.receiver") );
			String mailSubject = smgr.getString(configPrefix + "report.mail.subject", source);
			MailSender ms = new MailSender(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_TLS);
			ms.setMailFrom(mailSender);
			ms.setSubject(mailSubject);
			ms.setMailTo(mailReceiver);
			ms.setHtmlContent(mailContent);
			ms.send();
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
}
