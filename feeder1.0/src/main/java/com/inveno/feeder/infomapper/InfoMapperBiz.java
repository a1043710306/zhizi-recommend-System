package com.inveno.feeder.infomapper;

import com.inveno.feeder.datainfo.InteractionInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoMapperBiz implements RowMapper<InteractionInfo>
{

	@Override
	public InteractionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<InteractionInfo> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list)
	{
		List<InteractionInfo> info_list = new ArrayList<InteractionInfo>();
		
		for (Map<String, Object> info_map : info_map_list)
		{
			InteractionInfo info = new InteractionInfo();
			info.setContentId(info_map.get("content_id").toString());
			if (info_map.get("thumbup_count") != null){
				info.setThumbupCount(info_map.get("thumbup_count").toString());
			}else {
				info.setThumbupCount("0");
			}
			if (info_map.get("comment_count") != null){
				info.setCommentCount(info_map.get("comment_count").toString());
			}else{
				info.setCommentCount("0");
			}

			info_list.add(info);
		}

		return info_list;
	}

}