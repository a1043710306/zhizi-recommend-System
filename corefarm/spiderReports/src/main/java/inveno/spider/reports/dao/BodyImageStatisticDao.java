package inveno.spider.reports.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import inveno.spider.reports.util.GenerateTableName;
import inveno.spider.reports.util.JdbcTemplateFactory;
import tw.qing.util.DateUtil;

public class BodyImageStatisticDao {
	public  void save(BodyImageStatistic object){
		String sql = "INSERT INTO daily_body_image_count_statistic(language,body_images_count,state,statistic_time,count)  VALUES (?,?,?,?,?)";
		int res = JdbcTemplateFactory.geJdbcTemplate().update(sql, object.getAttributesArray(sql));
	}
	
	
	public void batchUpdate(final List<BodyImageStatistic> bodyImageStatisticList){
		String sql = "INSERT INTO daily_body_image_count_statistic(language,body_images_count,state,statistic_time,count)  VALUES (?,?,?,?,?)";
		JdbcTemplateFactory.geJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            
            public void setValues(PreparedStatement ps, int index) throws SQLException {
            	BodyImageStatistic bodyImageStatistic = bodyImageStatisticList.get(index);
                ps.setString(1, bodyImageStatistic.getLanguage());
                ps.setInt(2,bodyImageStatistic.getBody_images_count());
                ps.setInt(3,bodyImageStatistic.getState());
                ps.setDate(4,new java.sql.Date(bodyImageStatistic.getStatistic_time().getTime()));
                ps.setInt(5,bodyImageStatistic.getCount());
            }
            
            public int getBatchSize() {
                return bodyImageStatisticList.size();
            }
        });
	}
	
	public List<BodyImageStatistic>  getListByStateOne(Date start_time , Date end_time){
		String start_time_str = DateUtil.dateToString(start_time, "yyyy-MM-dd");
		String end_time_str = DateUtil.dateToString(end_time, "yyyy-MM-dd");
		String currentTableName = GenerateTableName.generatorTableName(false);
		String beforeTableName = GenerateTableName.generatorTableName(true);
//		String sql = "select language,body_images_count,state,count(1) as count "
//				+ " from t_content where discovery_time >='"+start_time_str+"'and discovery_time <'"+end_time_str+"' "
//				+ " and state=1  group by language,body_images_count,state";
		String sql = "select result.language,result.body_images_count,result.state,count(1) as count " 
				   + "from ("
				   + "select language,body_images_count,state from " + beforeTableName + " where discovery_time >='" + start_time_str + "' and discovery_time <'"+end_time_str+"' and state=1"
				   + " union all "
				   + "select language,body_images_count,state from " + currentTableName + " where discovery_time >='" + start_time_str + "' and discovery_time <'"+end_time_str+"' and state=1"
				   + ") result"
				   + " group by result.language,result.body_images_count,result.state";
		
		System.err.println(sql);
		
		List<Map<String, Object>> rows = JdbcTemplateFactory.getQueryTemplate().queryForList(sql);
		List<BodyImageStatistic> bodyImageStatisticList = new ArrayList<BodyImageStatistic>();
		for (Map<String, Object> row : rows) {
			BodyImageStatistic bodyImageStatistic = new BodyImageStatistic();
			bodyImageStatistic.setBody_images_count((Integer) row.get("body_images_count"));
			bodyImageStatistic.setState((Integer) row.get("state"));
			bodyImageStatistic.setLanguage((String) row.get("language"));
			bodyImageStatistic.setCount(Integer.parseInt( row.get("count")+""));
			bodyImageStatistic.setStatistic_time(start_time);
			bodyImageStatisticList.add(bodyImageStatistic);
			
		}
		
		return bodyImageStatisticList;	
	}
	
	
	public List<BodyImageStatistic>  getListByStateNotOne(Date start_time , Date end_time){
		String start_time_str = DateUtil.dateToString(start_time, "yyyy-MM-dd");
		String end_time_str = DateUtil.dateToString(end_time, "yyyy-MM-dd");
		String currentTableName = GenerateTableName.generatorTableName(false);
		String beforeTableName = GenerateTableName.generatorTableName(true);
//		String sql = "select language,body_images_count,state,count(1) as count "
//				+ " from t_content where discovery_time >='"+start_time_str+"'and discovery_time <'"+end_time_str+"' "
//				+ " and state!=1  group by language,body_images_count";
		String sql = "select result.language,result.body_images_count,result.state,count(1) as count " 
				   + "from ("
				   + "select language,body_images_count,state from " + beforeTableName + " where discovery_time >='" + start_time_str + "' and discovery_time <'"+end_time_str+"' and state!=1"
				   + " union all "
				   + "select language,body_images_count,state from " + currentTableName + " where discovery_time >='" + start_time_str + "' and discovery_time <'"+end_time_str+"' and state!=1"
				   + ") result"
				   + " group by result.language,result.body_images_count";
		
		System.err.println(sql);
		
		List<Map<String, Object>> rows = JdbcTemplateFactory.getQueryTemplate().queryForList(sql);
		List<BodyImageStatistic> bodyImageStatisticList = new ArrayList<BodyImageStatistic>();
		for (Map<String, Object> row : rows) {
			BodyImageStatistic bodyImageStatistic = new BodyImageStatistic();
			bodyImageStatistic.setBody_images_count((Integer) row.get("body_images_count"));
			bodyImageStatistic.setState(3);
			bodyImageStatistic.setLanguage((String) row.get("language"));
			bodyImageStatistic.setCount(Integer.parseInt( row.get("count")+""));
			bodyImageStatistic.setStatistic_time(start_time);
			bodyImageStatisticList.add(bodyImageStatistic);
			
		}
		
		return bodyImageStatisticList;
	}
	
	
	public static void main(String[] args) {
		//new BodyImageStatisticDao().save(new BodyImageStatistic("11",22,33,new Date(),100));
		//System.err.println(new BodyImageStatistic("11",22,33,new Date(),100).toString());
		//new BodyImageStatisticDao().getListByStateOne(DateUtil.stringToDate("2016-11-16"), DateUtil.stringToDate("2016-11-17"));
		//new BodyImageStatisticDao().getListByStateNotOne(DateUtil.stringToDate("2016-11-16"), DateUtil.stringToDate("2016-11-17"));
		
		
		for (int i = 0; i < 10 ; i++) {
			Calendar calendar =  Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, -i-1);
			Date start_time = calendar.getTime();
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			Date end_time = calendar.getTime();
			System.err.println("start_time : " + DateUtil.dateToString(start_time,"yyyy-MM-dd")+ " edn_time : "+ DateUtil.dateToString(end_time,"yyyy-MM-dd") );
			new BodyImageStatisticDao().getListByStateOne(start_time, end_time);
			new BodyImageStatisticDao().getListByStateNotOne(start_time, end_time);
		}
	}

}
