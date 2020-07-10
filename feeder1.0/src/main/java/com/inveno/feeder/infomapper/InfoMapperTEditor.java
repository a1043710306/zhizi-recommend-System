package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.Feeder;
import com.inveno.feeder.datainfo.EditorTableEntry;

public class InfoMapperTEditor implements RowMapper<EditorTableEntry>
{
	@Override
	public EditorTableEntry mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		// TODO Auto-generated method stub
		EditorTableEntry info = new EditorTableEntry();
		info.setContentId(rs.getString("content_id"));
		info.setAdultScore(rs.getString("adult_score"));
		info.setFirmApp(rs.getString("firm_app"));
		info.setCategories(rs.getString("categories"));
		info.setTitle(rs.getString("title"));
		info.setLink(rs.getString("link"));
		info.setPublisher(rs.getString("publisher"));
		info.setAuthor(rs.getString("author"));
		info.setIs_open_comment(rs.getInt("is_open_comment"));
		info.setCopyright(rs.getString("copyright"));
		info.setHasCopyright(rs.getInt("has_copyright"));
		info.setFallImage(rs.getString("fall_image"));
		info.setLanguage(rs.getString("language"));
		info.setLinkType(rs.getInt("link_type"));
		info.setDisplayType(rs.getInt("display_type"));
		info.setPublishTime(rs.getString("publish_time"));
		info.setFlag(rs.getString("flag"));	
		info.setType(rs.getString("type"));
		info.setContentType(rs.getInt("content_type"));
		info.setContent(rs.getString("content"));
		info.setBodyImages(rs.getString("body_images"));
		info.setBodyImagesCount(rs.getInt("body_images_count"));
		info.setListImages(rs.getString("list_images"));
		info.setListImagesCount(rs.getInt("list_images_count"));
		info.setSourceType(rs.getString("source_type"));
		info.setSource(rs.getString("source"));
		info.setSourceFeeds(rs.getString("source_feeds"));
		info.setChannel(rs.getString("channel"));
		try
		{
			if (rs.findColumn("content_quality") >= 0)
			{
				info.setContentQuality(rs.getInt("content_quality"));
			}
		}
		catch (Exception e)
		{
			//ignore
		}
		info.setPicAdultScore(rs.getString("pic_adult_score"));
		return info;
	}

}
