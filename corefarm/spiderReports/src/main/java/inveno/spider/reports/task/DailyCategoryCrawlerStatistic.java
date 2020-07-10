package inveno.spider.reports.task;

import java.util.*;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.common.util.JsonUtils;
import inveno.spider.common.mail.MailSender;

import com.google.gson.*;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
import tw.qing.util.PrimitiveTypeUtil;


/**
 * Created by Genix.Li on 2016/4/22.
 */
public class DailyCategoryCrawlerStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailyCategoryCrawlerStatistic.class);

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
		formatter.printHelp("java " + DailyCategoryCrawlerStatistic.class.getCanonicalName(), createOptions());
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

			ArrayList alSource = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getArticleSource();
			HashSet<String> hsSource = new HashSet<String>();
			for (int i = 0; i < alSource.size(); i++)
			{
				HashMap mSource = (HashMap)alSource.get(i);
				hsSource.add( (String)mSource.get("source") );
			}
			String[] source = (String[])hsSource.toArray(new String[0]);

			HashSet<String> hsTransitArticle = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getDailyTransitArticle(dateString);
			ArrayList alCrawledArticle = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getDailyCrawledArticle(source, dateString);

			Gson gson =  new Gson();
			HashMap<String, HashMap<Integer, Integer>> mLanguageCrawlStatistic = new HashMap<String, HashMap<Integer, Integer>>();
			for (int i = 0; alCrawledArticle != null && i < alCrawledArticle.size(); i++)
			{
				HashMap mArticle = (HashMap)alCrawledArticle.get(i);
				String contentId = (String)mArticle.get("content_id");
				String language = (String)mArticle.get("language");
				HashMap<Integer, Integer> mCrawlerStatistic = mLanguageCrawlStatistic.get(language);
				if (mCrawlerStatistic == null)
				{
					mCrawlerStatistic = new HashMap<Integer, Integer>();
					mLanguageCrawlStatistic.put(language, mCrawlerStatistic);
				}

				String jsonCategory = (String)mArticle.get("categories");
				HashMap mVersionCategory = (HashMap)JsonUtils.toJavaObject(gson.fromJson(jsonCategory, JsonElement.class));
				ArrayList alCategory = (ArrayList)mVersionCategory.get("v4");
				if (alCategory == null)
				{
					log.debug("no category : " + contentId);
				}
				else
				{
					for (int j = 0; j < alCategory.size(); j++)
					{
						int categoryId = Integer.parseInt( String.valueOf(((HashMap)alCategory.get(j)).get("category")) );
						int totalCount = (null == mCrawlerStatistic.get(categoryId)) ? 0 : ((Integer)mCrawlerStatistic.get(categoryId)).intValue();
						totalCount++;
						mCrawlerStatistic.put(categoryId, totalCount);
					}
				}
			}

			String[] language = mLanguageCrawlStatistic.keySet().toArray(new String[0]);
			for (int j = 0; j < language.length; j++)
			{
				HashMap<Integer, Integer> mCrawlerStatistic = mLanguageCrawlStatistic.get(language[j]);
				Integer[] categoryId = mCrawlerStatistic.keySet().toArray(new Integer[0]);
				for (int i = 0; i < categoryId.length; i++)
				{
					System.out.println(language[j] + "\t" + categoryId[i] + "\t" + mCrawlerStatistic.get(categoryId[i]));
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
}
