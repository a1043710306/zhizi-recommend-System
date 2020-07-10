package com.inveno.feeder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.inveno.feeder.jsoninfomapper.*;

public class ClientJDBCTemplateAD implements ClientDAO 
{
	@SuppressWarnings("unused")
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
	
	@Override
	public void setDataSource(DataSource ds) {
		
		this.dataSource = ds;
		this.jdbcTemplateObject = new JdbcTemplate(ds);
		
	}
	
	public List<Map<String, String>> listADTableEntry() 
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		Calendar cal = Calendar.getInstance();
		String strTodayZeroTime = fmt.format(cal.getTime())+" 00:00:00";
        cal.add(Calendar.DATE, -1);
		String strYesterdayZeroTime = fmt.format(cal.getTime())+" 00:00:00";
		String SQL = "SELECT * FROM t_dsp_ad WHERE (update_time >= '" + strYesterdayZeroTime + "' AND update_time < '"+strTodayZeroTime+"') OR date_end >= '"+strYesterdayZeroTime+"'";
		
		List<Map<String, String>> info_list = jdbcTemplateObject.query(SQL+" ORDER BY ad_id", new JsonInfoMapperADTable());
		
		return info_list;
	}

	

	
}