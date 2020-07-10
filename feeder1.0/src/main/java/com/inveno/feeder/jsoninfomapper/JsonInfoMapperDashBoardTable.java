package com.inveno.feeder.jsoninfomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public class JsonInfoMapperDashBoardTable implements RowMapper<Map<String, String>> {

	@Override
	public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("source", String.valueOf(rs.getDouble("ctr")));
		return jsonMap;
	}

}
