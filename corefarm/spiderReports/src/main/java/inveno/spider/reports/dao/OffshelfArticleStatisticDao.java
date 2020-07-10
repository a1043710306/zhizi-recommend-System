package inveno.spider.reports.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import inveno.spider.reports.util.JdbcTemplateFactory;

public class OffshelfArticleStatisticDao {

	private JdbcTemplate JdbcTemplate = null;
	private String uname = null;
	private String passwd = null;
	private String conenctUrl = null;
	
	public OffshelfArticleStatisticDao(){
//		StringManager smgr = StringManager.getManager("system");
	}
	
	public static  void save(OffshelfArticleStatistic object_static){
		String sql = String.format("insert into daily_offshelf_article_statistic(offshelf_code,offshelf_reason,static_count,statistic_date,language)  VALUES (%d,\"%s\",%d,'%s','%s')", object_static.getOffshelf_code(),object_static.getOffshelf_reason(),object_static.getStatic_count(),object_static.getStatistic_date(), object_static.getLanguage());
		JdbcTemplateFactory.geJdbcTemplate().execute(sql);
		System.err.println("offshelf article statistic sql : " + sql);
	}
	
	public static boolean query(String statistic_date,String offshelf_code){
		
		return false;
	}
	
	
	
	public static void main(String[] args) {
		
	}
	 
	 
	 
}
