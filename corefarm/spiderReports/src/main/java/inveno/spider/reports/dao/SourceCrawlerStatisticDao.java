package inveno.spider.reports.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import inveno.spider.reports.util.JdbcTemplateFactory;;
public class SourceCrawlerStatisticDao {
	
	/*public void get(int id){
		JdbcTemplateFactory.geJdbcTemplate().query("select * from Daily_Source_Crawler_Statistic limit 1", 
				new RowCallbackHandler() {  
            		public void processRow(ResultSet rs) throws SQLException {  
		                System.err.println(rs.getString("firmapp"));  
		                System.err.println(rs.getString("type"));  
		                System.err.println(rs.getString("statistic_time"));  
		                System.err.println(rs.getString("count"));  
            }  
        });  
	}*/

	
	public  void save(SourceCrawlerStatistic object){
		String sql = "INSERT INTO daily_source_crawler_statistic(source,crawler_type,source_feeds_host,statistic_time,count)  VALUES (?,?,?,?,?)";
		int res = JdbcTemplateFactory.geJdbcTemplate().update(sql, object.getAttributesArray(sql));
		System.err.println(res);
	}
	
	 public static void main(String[] args) {
		new SourceCrawlerStatisticDao().save(new SourceCrawlerStatistic("11","22","33",new Date(),100));
		List<SourceCrawlerStatistic> sourceCrawlerStatisticsList = new ArrayList<SourceCrawlerStatistic>();
		sourceCrawlerStatisticsList.add(new SourceCrawlerStatistic("11","22","33",new Date(),100));
		sourceCrawlerStatisticsList.add(new SourceCrawlerStatistic("22","33","44",new Date(),101));
		sourceCrawlerStatisticsList.add(new SourceCrawlerStatistic("33","44","55",new Date(),102));
		new SourceCrawlerStatisticDao().batchUpdate(sourceCrawlerStatisticsList);
		
	}
	 
	 public void batchUpdate(final List<SourceCrawlerStatistic> sourceCrawlerStatisticsList){
		String sql = "INSERT INTO daily_source_crawler_statistic(source,crawler_type,source_feeds_host,statistic_time,count)  VALUES (?,?,?,?,?)";
		JdbcTemplateFactory.geJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            
            public void setValues(PreparedStatement ps, int index) throws SQLException {
            	SourceCrawlerStatistic sourceCrawlerStatistic = sourceCrawlerStatisticsList.get(index);
                ps.setString(1, sourceCrawlerStatistic.getSource());
                ps.setString(2,sourceCrawlerStatistic.getCrawler_type());
                ps.setString(3,sourceCrawlerStatistic.getSource_feeds_host());
                ps.setDate(4,new java.sql.Date(sourceCrawlerStatistic.getStatistic_time().getTime()));
                ps.setInt(5,sourceCrawlerStatistic.getCount());
            }
            
            public int getBatchSize() {
                return sourceCrawlerStatisticsList.size();
            }
        });
	}
}


