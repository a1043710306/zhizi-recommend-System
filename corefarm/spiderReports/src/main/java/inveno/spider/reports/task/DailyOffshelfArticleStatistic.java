package inveno.spider.reports.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import inveno.spider.reports.dao.OffshelfArticleStatistic;
import inveno.spider.reports.dao.OffshelfArticleStatisticDao;
import inveno.spider.reports.facade.ContentFacade;
import tw.qing.sys.StringManager;

public class DailyOffshelfArticleStatistic {

	private static final Logger log = Logger.getLogger(DailyOffshelfArticleStatistic.class);

	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
	
	public void doStatisticOffshelfArticle(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy_MM");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String tableName = "t_content_" + simpleDate.format(cal.getTime());
		String startTime = df.format(cal.getTime());
		String endTime = df.format(new Date());
		ArrayList offshelfRes = ContentFacade.getInstance().getOffshelfArticleCount(tableName, startTime, endTime);
		for(Object oneShelfRes:offshelfRes){
			HashMap shelfHash = (HashMap)oneShelfRes;
			int offshelf_code = Integer.parseInt(shelfHash.get("offshelf_code").toString());
			String offshelf_reason = (String)shelfHash.get("offshelf_reason");
//			offshelf_reason = offshelf_reason.replace("\"", "'");
			int static_count = Integer.parseInt(shelfHash.get("static_count").toString());
			String days = (String)shelfHash.get("days");
			String language = (String)shelfHash.get("language");
			log.info(String.format("offshelf_code:%d;offshelf_reason:%s;static_count:%d;days:%s;language:%s;",offshelf_code,offshelf_reason,static_count,days,language));
			OffshelfArticleStatistic offshelfArticleStatistic = new OffshelfArticleStatistic(offshelf_code,offshelf_reason,days,static_count,language);
			OffshelfArticleStatisticDao.save(offshelfArticleStatistic);
		}
	}
	
	
	
	public static void main(String[]args){
		DailyOffshelfArticleStatistic dailyOffshelfArticle = new DailyOffshelfArticleStatistic();
		try{
			dailyOffshelfArticle.doStatisticOffshelfArticle();
		}catch(Exception e){
			log.fatal("", e);
		}
	}
}
