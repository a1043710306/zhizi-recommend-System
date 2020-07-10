package inveno.spider.reports.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import inveno.spider.reports.util.JdbcTemplateFactory;
import tw.qing.sys.StringManager;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;;
public class ProductCrawlerStatisticDao {
	
	private JdbcTemplate JdbcTemplate = null;
	private String uname = null;
	private String passwd = null;
	private String conenctUrl = null;
	
	
	public ProductCrawlerStatisticDao(){
		
		try
		{
			StringManager smgr = StringManager.getManager("system");
			uname  = smgr.getString("DailySourceCrawlerStatistic.mysql.username");
			passwd = smgr.getString("DailySourceCrawlerStatistic.mysql.passwd");
			conenctUrl   = smgr.getString("DailySourceCrawlerStatistic.mysql.host");
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
	
	public void get(int id){
		this.JdbcTemplate.query("select * from daily_product_crawler_statistic where seed = 'india'",  new RowCallbackHandler() {  
            public void processRow(ResultSet rs) throws SQLException {  
                System.err.println(rs.getString("firmapp"));  
                System.err.println(rs.getString("type"));  
                System.err.println(rs.getString("statistic_time"));  
                System.err.println(rs.getString("count"));  
            }  
        });  
	}

	
	public  void save(ProductCrawlerStatistic object){
		String sql = "INSERT INTO daily_product_crawler_statistic(firmapp,type,statistic_time,count)  VALUES (?,?,?,?)";
		int res = this.JdbcTemplate.update(sql,  new Object[] {object.getFirmapp(),object.getType(),object.getStatistic_time(),object.getCount()});
		System.err.println(res);
	}
	 public static void main(String[] args) {
		new ProductCrawlerStatisticDao().save(new ProductCrawlerStatistic("11111","tet",new Date(),100));

		List<ProductCrawlerStatistic> sourceCrawlerStatisticsList = new ArrayList<ProductCrawlerStatistic>();
		sourceCrawlerStatisticsList.add(new ProductCrawlerStatistic("11","22",new Date(),100));
		sourceCrawlerStatisticsList.add(new ProductCrawlerStatistic("22","33",new Date(),101));
		sourceCrawlerStatisticsList.add(new ProductCrawlerStatistic("33","44",new Date(),102));
		new ProductCrawlerStatisticDao().batchUpdate(sourceCrawlerStatisticsList);
	}
	 
	 public void batchUpdate(final List<ProductCrawlerStatistic> productCrawlerStatisticsList){
		String sql = "INSERT INTO daily_product_crawler_statistic(firmapp,type,statistic_time,count)  VALUES (?,?,?,?)";
		JdbcTemplateFactory.geJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            
            public void setValues(PreparedStatement ps, int index) throws SQLException {
            	ProductCrawlerStatistic productCrawlerStatistic = productCrawlerStatisticsList.get(index);
                ps.setString(1, productCrawlerStatistic.getFirmapp());
                ps.setString(2,productCrawlerStatistic.getType());
                ps.setDate(3,new java.sql.Date(productCrawlerStatistic.getStatistic_time().getTime()));
                ps.setInt(4,productCrawlerStatistic.getCount());
            }
            
            public int getBatchSize() {
                return productCrawlerStatisticsList.size();
            }
        });
	}
}


