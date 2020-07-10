package com.inveno.feeder.jsoninfomapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.datainfo.EditorTableEntry;

public class JsonInfoMapperDruid implements RowMapper<Map<String, String>>
{
	@Override
	public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Map<String, String>> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list)
	{
		return mapIntoFeederInfoList(info_map_list, null);
	}
	public List<Map<String, String>> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list, Map<String, EditorTableEntry> mEditorEntry)
	{
		List<Map<String, String>> info_list = new ArrayList<Map<String, String>>();

		for (Map<String, Object> info_map : info_map_list)
		{
			Map<String, String> jsonMap = new HashMap<String, String>();
			jsonMap.put("content_id", (String)info_map.get("content_id"));
			jsonMap.put("cp_version", (String)info_map.get("cp_version"));
			jsonMap.put("tier", (String)info_map.get("tier"));
			jsonMap.put("important_level", String.valueOf(info_map.get("important_level")));
			jsonMap.put("local", (String)info_map.get("local"));
			jsonMap.put("categories", (String)info_map.get("categories"));
			jsonMap.put("adult_score", (String)info_map.get("adult_score"));
			jsonMap.put("newsy_score", (String)info_map.get("newsy_score"));
			jsonMap.put("emotion", (String)info_map.get("emotion"));
			jsonMap.put("language", (String)info_map.get("language"));
			jsonMap.put("keywords", (String)info_map.get("keywords"));
			jsonMap.put("tags", (String)info_map.get("tags"));
			jsonMap.put("ner_person", (String)info_map.get("ner_person"));
			jsonMap.put("ner_location", (String)info_map.get("ner_location"));
			jsonMap.put("ner_organization", (String)info_map.get("ner_organization"));
			if (info_map.get("type") != null)
				jsonMap.put("type", (String)info_map.get("type"));
			else
				jsonMap.put("type", ""); //not set any type as default
			if (info_map.get("content_type") != null)
			{
				jsonMap.put("content_type", String.valueOf((Integer)info_map.get("content_type")));
				jsonMap.put("type", String.valueOf( (int)(Math.log((Integer)info_map.get("content_type"))/Math.log(2)) ));
			}
			else
			{
				String content_type = "1";
				if (jsonMap.get("type") != null && jsonMap.get("type").isEmpty() == false)
					content_type = String.valueOf(1<<Integer.valueOf(jsonMap.get("type"))); // type 0 => content_type 1; type 1 => content_type 2
				jsonMap.put("content_type", content_type);
			}
			jsonMap.put("publish_time", info_map.get("publish_time")!=null ? info_map.get("publish_time").toString():"");
			jsonMap.put("word_count", (String)info_map.get("word_count"));

			jsonMap.put("publisher", (String)info_map.get("publisher"));
			jsonMap.put("sourceType", (String)info_map.get("source_type"));
			jsonMap.put("source", (String)info_map.get("source"));
			jsonMap.put("sourceFeeds", (String)info_map.get("source_feeds"));
			jsonMap.put("channel", (String)info_map.get("channel"));
			jsonMap.put("discoveryTime", info_map.get("discovery_time").toString());
			jsonMap.put("hasCopyright", String.valueOf(info_map.get("has_copyright")));
			jsonMap.put("sourceCommentCount", String.valueOf(info_map.get("source_comment_count")));
			jsonMap.put("commentCount", String.valueOf(info_map.get("comment_count")!=null ? (Integer)info_map.get("comment_count"):0));
			jsonMap.put("link_type", String.valueOf(info_map.get("link_type")));
			jsonMap.put("display_type", String.valueOf(info_map.get("display_type")));
			jsonMap.put("rate", String.valueOf(info_map.get("rate")));
			jsonMap.put("flag", String.valueOf(info_map.get("flag")));

			info_list.add(jsonMap);
		}

		return info_list;
	}

}