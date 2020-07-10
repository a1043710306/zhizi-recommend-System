package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.feeder.Feeder;
import com.inveno.feeder.datainfo.EditorTableEntry;
import com.inveno.feeder.datainfo.SourceSubscriptionEntry;

public class InfoMapperSourceSubscription implements RowMapper<SourceSubscriptionEntry>{

	//获取订阅ID
	@Override
	public SourceSubscriptionEntry mapRow(ResultSet rs, int rowNum) throws SQLException 
	{
		SourceSubscriptionEntry sourceSubscriptionModel = new SourceSubscriptionEntry();
		sourceSubscriptionModel.setSubscribeId(rs.getString("subscribe_id"));
		sourceSubscriptionModel.setChannelName(rs.getString("channel_name"));
		sourceSubscriptionModel.setRssName(rs.getString("rss_name"));
		sourceSubscriptionModel.setId(rs.getInt("id"));
		sourceSubscriptionModel.setCreateTime(rs.getDate("create_time"));
		return sourceSubscriptionModel;
	}

	//处理t-content
	public List<FeederInfo> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list)
	{
		return mapIntoFeederInfoList(info_map_list, null);
	}
	public List<FeederInfo> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list, Map<String, EditorTableEntry> mEditorEntry)
	{
		List<FeederInfo> info_list = new ArrayList<FeederInfo>();
		
		for (Map<String, Object> info_map : info_map_list)
		{
			FeederInfo info = new FeederInfo();
			String contentId = (String)info_map.get("content_id");
			info.setContent_id(contentId);
			info.setSource((String)info_map.get("source"));
			info.setChannel((String)info_map.get("channel"));
			info.setPublish_time(info_map.get("publish_time") == null ? "" :info_map.get("publish_time").toString());
			if (mEditorEntry != null)
			{
				info = Feeder.applyEditorData(info, mEditorEntry.get(contentId));
			}
			info_list.add(info);
		}
		return info_list;
	}

}
