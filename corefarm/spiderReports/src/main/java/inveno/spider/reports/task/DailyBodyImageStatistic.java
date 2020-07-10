package inveno.spider.reports.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import inveno.spider.reports.dao.BodyImageStatistic;
import inveno.spider.reports.dao.BodyImageStatisticDao;
import tw.qing.util.DateUtil;

public class DailyBodyImageStatistic extends AbstractStatistic {
	private static final Logger log = Logger.getLogger(DailyBodyImageStatistic.class);

	
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
	
	private static void printCliHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + DailyProductCrawlerStatistic.class.getCanonicalName(), createOptions());
	}
	
	public static void main(String[] args) {
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
			
			log.info("query body Image Statistics With State One start time : " + startTime + " end time : "+endTime);
			List<BodyImageStatistic> bodyImageStatisticsListWithStateOne = new BodyImageStatisticDao().getListByStateOne(startTime, endTime);
			new BodyImageStatisticDao().batchUpdate(bodyImageStatisticsListWithStateOne);
			
			log.info("query body Image Statistics With State Not One start time : " + startTime + " end time : "+endTime);
			List<BodyImageStatistic> bodyImageStatisticsListWithStateNotOne = new BodyImageStatisticDao().getListByStateNotOne(startTime, endTime);
			new BodyImageStatisticDao().batchUpdate(bodyImageStatisticsListWithStateNotOne);
			
			
		}catch (Exception e)
		{
			log.fatal("", e);
		}
	
	}
	
}
