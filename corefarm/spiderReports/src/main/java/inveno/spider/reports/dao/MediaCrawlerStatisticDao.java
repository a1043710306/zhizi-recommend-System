package inveno.spider.reports.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import inveno.spider.reports.util.GenerateTableName;
import inveno.spider.reports.util.JdbcTemplateFactory;
import tw.qing.sys.StringManager;

public class MediaCrawlerStatisticDao {
	
	private JdbcTemplate JdbcTemplate = null;
	private String uname = null;
	private String passwd = null;
	private String conenctUrl = null;
	
	public MediaCrawlerStatisticDao(){
		try
		{
			StringManager smgr = StringManager.getManager("system");
			uname  = smgr.getString("lwdba.pool.crawler.userName");
			passwd = smgr.getString("lwdba.pool.crawler.password");
			conenctUrl   = smgr.getString("lwdba.pool.crawler.driverURL");
		}
		catch (Exception e)
		{
			System.err.println(e);
			System.exit(-1);
		}
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(conenctUrl);
		dataSource.setUsername(uname);
		dataSource.setPassword(passwd);
		this.JdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void save(HashMap<String,Object> object){
		String sql = "insert into t_media_stat_daily(media,q_media_info,num,date)  values (?,?,?,?)";
		int res = this.JdbcTemplate.update(sql,  new Object[] {object.get("media"),object.get("q_media_info"),object.get("num"),object.get("date")});
		System.err.println(res);
	}
	
	/**
	 * 获取media 统计值
	 * @param info
	 * @return
	 */
	public int getMediaStatisticNum(String info,String date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar calendar = Calendar.getInstance();
		//int day = calendar.get(Calendar.DAY_OF_MONTH)-1;   
		calendar.add(Calendar.DAY_OF_MONTH,-1);
		String startTime = format.format(calendar.getTime());
		
		String currentTableName = GenerateTableName.generatorTableName(false);
		String beforeTableName = GenerateTableName.generatorTableName(true);
		String sql = "select count(1) as num from ( " +"select content_id from "+beforeTableName + " where discovery_time>='"+startTime+"' and discovery_time<='" + date + "'and  content like '%"+info+"%'  union all "+"select content_id from "+currentTableName + " where discovery_time>='"+startTime+"' and discovery_time<='" + date + "' and content like '%"+info+"%' ) as dbinfo ";
		System.out.println(sql);
		Map<String, Object> rows = JdbcTemplateFactory.getQueryTemplate().queryForMap(sql);
		return (int)rows.get("num");
	}
	
}
