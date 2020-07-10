package com.inveno.feeder.jsoninfomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;


public class JsonInfoMapperADTable implements RowMapper<Map<String, String>> 
{
	public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException 
	{
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("ad_id", rs.getString("ad_id"));
		jsonMap.put("user_id", String.valueOf(rs.getInt("user_id")));
		jsonMap.put("plan_id", String.valueOf(rs.getInt("plan_id")));
		jsonMap.put("ad_name", rs.getString("ad_name"));
		jsonMap.put("date_start", rs.getString("date_start"));
		jsonMap.put("date_end", rs.getString("date_end"));
		jsonMap.put("ad_type", rs.getString("ad_type"));
		jsonMap.put("ad_link_url", rs.getString("ad_link_url"));
		jsonMap.put("ad_price", String.valueOf(rs.getFloat("ad_price")));
		jsonMap.put("create_time", rs.getString("create_time"));
		jsonMap.put("update_time", rs.getString("update_time"));
		jsonMap.put("operator", rs.getString("operator"));
		jsonMap.put("ad_source", rs.getString("ad_source"));
		
		jsonMap.put("content_id", rs.getString("ad_id"));
		jsonMap.put("app", "");
		jsonMap.put("abtest_ver", "");
		jsonMap.put("content_type", rs.getString("ad_type"));
		jsonMap.put("title", rs.getString("ad_name"));
		jsonMap.put("source", rs.getString("ad_link_url"));
		jsonMap.put("sourceType", rs.getString("ad_source"));
		jsonMap.put("sourceFeeds", "");
		jsonMap.put("publisher", "");
		jsonMap.put("word_count", "");
		jsonMap.put("commentCount", "");
		jsonMap.put("keywords", "");
		jsonMap.put("copyright", "");
		jsonMap.put("isOpenComment", "");
		jsonMap.put("ner_organization", "");
		jsonMap.put("adult_score", "");
		jsonMap.put("categories", "");
		jsonMap.put("body_images_count", "");
		jsonMap.put("channel", rs.getString("operator"));
		jsonMap.put("update_time", rs.getString("update_time"));
		jsonMap.put("tags", "");
		jsonMap.put("discoveryTime", rs.getString("create_time"));
		jsonMap.put("ner_location", "");
		jsonMap.put("tier", "");
		jsonMap.put("important_level", "");
		jsonMap.put("group_id", "");
		jsonMap.put("ner_person", "");
		jsonMap.put("type", rs.getString("ad_type"));
		jsonMap.put("newsy_score", "");
		jsonMap.put("impression", "");
		jsonMap.put("click", "");
		jsonMap.put("dwelltime", "");
		jsonMap.put("click_user", "");

		return jsonMap;
	}
	
}