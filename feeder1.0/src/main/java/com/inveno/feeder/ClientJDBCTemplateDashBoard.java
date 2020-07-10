package com.inveno.feeder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.inveno.feeder.jsoninfomapper.*;

/**
 * 
 * @author klausliu
 * 查询dashboard source数据
 *
 */
public class ClientJDBCTemplateDashBoard implements ClientDAO{

	@SuppressWarnings("unused")
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
	
	@Override
	public void setDataSource(DataSource ds) {
		
		this.dataSource = ds;
		this.jdbcTemplateObject = new JdbcTemplate(ds);
		
	}
	
	public Map<String, Double> getSourceMap(int beforeDays) 
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -beforeDays);
		String strBeforeDaysZeroTime = fmt.format(cal.getTime())+" 00:00:00";
		String SQL = "select source, sum(click) as click , sum(impression) as impression, sum(click)/sum(impression) as ctr from t_daily_article where timestamp="+strBeforeDaysZeroTime+
				    " and scenario=all and scenario_channel=all and app_ver=all and language=all group by source;";
		
		List<Map<String, String>> sourceList = jdbcTemplateObject.query(SQL,new JsonInfoMapperDashBoardTable());
		Map<String, Double> sourceMap =  new HashMap<String,Double>();
		for(Map<String, String> source : sourceList){
			sourceMap.put("source", Double.valueOf(source.get("source")));
		}
		return sourceMap;
	}
}
