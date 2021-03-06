package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.jdbc.core.RowMapper;

import com.inveno.feeder.thrift.*;
import com.inveno.feeder.Feeder;
import com.inveno.feeder.datainfo.EditorTableEntry;

public class InfoMapperPrediction implements RowMapper<FeederInfo> 
{
	@Override
	public FeederInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

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
			info.setCp_version((String)info_map.get("cp_version"));
			info.setTier((String)info_map.get("tier"));
			info.setImportant_level(info_map.get("important_level")==null ? 0:(Integer)info_map.get("important_level"));
			info.setLocal((String)info_map.get("local"));
			info.setCategories((String)info_map.get("categories"));
			info.setAdult_score((String)info_map.get("adult_score"));
			info.setNews_score((String)info_map.get("newsy_score"));
			info.setEmotion((String)info_map.get("emotion"));
			String strJsonLanguage = (String)info_map.get("language");
			info.setLanguage(strJsonLanguage);
			int availableLanguage = Feeder.getAvailableLanguage(strJsonLanguage);
			info.setAvailableLanguage(availableLanguage);
			info.setKeywords((String)info_map.get("keywords_new"));
			info.setTags((String)info_map.get("tags"));
			info.setNer_person((String)info_map.get("ner_person"));
			info.setNer_location((String)info_map.get("ner_location"));
			info.setNer_organization((String)info_map.get("ner_organization"));
			info.setMd5((String)info_map.get("md5"));
			info.setSimhash((String)info_map.get("simhash"));
			if (info_map.get("type") != null)
				info.setType((String)info_map.get("type"));
			else
				info.setType(""); //not set any type as default
			if (info_map.get("content_type") != null)
			{
				info.setContent_type(String.valueOf((Integer)info_map.get("content_type")));
				info.setType(String.valueOf( (int)(Math.log((Integer)info_map.get("content_type"))/Math.log(2)) ));
			}
			else
			{
				String content_type = "1";
				if (info.getType() != null && info.getType().isEmpty() == false)
					content_type = String.valueOf(1<<Integer.valueOf(info.getType())); // type 0 => content_type 1; type 1 => content_type 2
				info.setContent_type(content_type);
			}
			info.setPublish_time(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("publish_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			info.setList_image_count((Integer)info_map.get("list_images_count"));
			info.setListImagesCount((Integer)info_map.get("list_images_count")); //redundant...
			info.setBody_image_count((Integer)info_map.get("body_images_count"));
			info.setBodyImagesCount((Integer)info_map.get("body_images_count")); //redundant...
			info.setListImages((String)info_map.get("list_images"));
			info.setLink((String)info_map.get("link"));
			info.setPublisher((String)info_map.get("publisher"));
			info.setWord_count((String)info_map.get("word_count"));
			info.setGroup_id((String)info_map.get("group_id"));
			info.setAuthor((String)info_map.get("author"));
			info.setSourceType((String)info_map.get("source_type"));
			info.setSource((String)info_map.get("source"));
			info.setSourceFeeds((String)info_map.get("source_feeds"));
			info.setChannel((String)info_map.get("channel"));
			info.setDiscoveryTime(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("discovery_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			info.setTitle((String)info_map.get("title"));
			info.setHasCopyright((Integer)info_map.get("has_copyright"));
			if (info_map.get("publisher_pagerank_score") instanceof java.math.BigDecimal)
				info.setPublisherPagerankScore(((java.math.BigDecimal)info_map.get("publisher_pagerank_score")).doubleValue());
			else
				info.setPublisherPagerankScore((Double)info_map.get("publisher_pagerank_score"));
			info.setSourceCommentCount((Integer)info_map.get("source_comment_count"));
			info.setCommentCount(info_map.get("comment_count")!=null ? (Integer)info_map.get("comment_count"):0);
			info.setFirm_app((String)info_map.get("firm_app"));
			info.setLink_type((Integer)info_map.get("link_type"));
			info.setDisplay_type((Integer)info_map.get("display_type"));
			info.setDuration((Integer)info_map.get("duration"));
			info.setThirdparty_topics(info_map.get("topics")==null?"":(String)info_map.get("topics"));

			if (info_map.get("publish_time") != null)
			{
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				try {
					if (info_map.get("publish_time") instanceof Long)
						ts = new Timestamp((long)info_map.get("publish_time") - 28800000l); //Todo: find the root cause on this time mismatch..
					else
						ts = new Timestamp(fmt.parse(info_map.get("publish_time").toString()).getTime());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				info.setPublish_time_ts((int)(ts.getTime()/1000));
			}
			else
				info.setPublish_time_ts(0);

			info.setTags_title((String)info_map.get("tags_title"));
			info.setItem_cf_topic((String)info_map.get("item_cf_topic"));
			info.setHighGmpCheck((int)((null == info_map.get("high_gmp_checked")) ? 0 : (Integer)info_map.get("high_gmp_checked")));
			info.setContentQuality((int)((null == info_map.get("content_quality")) ? 0 : (Integer)info_map.get("content_quality")));
			if (mEditorEntry != null)
			{
				info = Feeder.applyEditorData(info, mEditorEntry.get(contentId));
			}
			info.setSourceFeedsUrl((String)info_map.get("source_feeds_url"));
			info_list.add(info);
		}

		return info_list;
	}
}