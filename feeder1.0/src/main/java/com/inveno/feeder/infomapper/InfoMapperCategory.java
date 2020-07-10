package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.datainfo.CategoryInfo;

public class InfoMapperCategory implements RowMapper<CategoryInfo>{

	@Override
	public CategoryInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
	
		CategoryInfo info = new CategoryInfo();
		info.setId(rs.getInt("id"));
		info.setVersion(rs.getInt("version"));
		info.setCategoryName(rs.getString("category_name"));
		info.setIntro(rs.getString("intro"));
		info.setCreateTime(rs.getTimestamp("create_time"));
		info.setExpiryHour(rs.getInt("expiry_hour"));
		try
		{
			info.setContentType(rs.getInt("content_type"));
		}
		catch (SQLException e)
		{
			info.setContentType(0);
		}
		return info;
	}
}