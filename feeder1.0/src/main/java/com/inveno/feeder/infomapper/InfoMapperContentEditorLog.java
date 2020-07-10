package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.datainfo.ContentEditorLog;

/**
 * 
 * @author klaus liu
 *
 */
public class InfoMapperContentEditorLog implements RowMapper<ContentEditorLog>{

	@Override
	public ContentEditorLog mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		ContentEditorLog contentEditorLog = new ContentEditorLog();
		contentEditorLog.setId(rs.getInt("id"));
		contentEditorLog.setContentId(rs.getString("content_id"));
		contentEditorLog.setEditorType(rs.getInt("edit_type"));
		contentEditorLog.setOperator(rs.getString("operator"));
		contentEditorLog.setOperateTime(rs.getTimestamp("operate_time"));
		return contentEditorLog;
	}

}
