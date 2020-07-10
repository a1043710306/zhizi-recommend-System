package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.alibaba.fastjson.JSON;
import com.inveno.thrift.*;
import com.inveno.feeder.jsondatastruct.JSONDataStruct1L;
import com.inveno.feeder.thrift.*;
import com.inveno.feeder.ClientJDBCTemplate;
import com.inveno.feeder.Feeder;
import com.inveno.feeder.datainfo.EditorTableEntry;

public class InfoMapperInfo implements RowMapper<Info>
{
	private static final Logger flowLogger    = Logger.getLogger("feeder.flow");

	@Override
	public Info mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Info> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list)
	{
		return mapIntoFeederInfoList(info_map_list, null);
	}
	public List<Info> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list, Map<String, EditorTableEntry> mEditorEntry)
	{
		List<Info> info_list = new ArrayList<Info>();
		
		for (Map<String, Object> info_map : info_map_list)
		{
			Info info = new Info();
			String contentId = (String)info_map.get("content_id");
			info.feederInfo = new FeederInfo();
			info.feederInfo.setContent_id(contentId);
			info.feederInfo.setCp_version((String)info_map.get("cp_version"));
			info.feederInfo.setTier((String)info_map.get("tier"));
			info.feederInfo.setImportant_level(info_map.get("important_level")==null ? 0:(Integer)info_map.get("important_level"));
			info.feederInfo.setLocal((String)info_map.get("local"));
			info.feederInfo.setCategories((String)info_map.get("categories"));
			info.feederInfo.setAdult_score((String)info_map.get("adult_score"));
			info.feederInfo.setNews_score((String)info_map.get("newsy_score"));
			info.feederInfo.setEmotion((String)info_map.get("emotion"));
			info.feederInfo.setLanguage((String)info_map.get("language"));
			info.feederInfo.setKeywords((String)info_map.get("keywords"));
			info.feederInfo.setTags((String)info_map.get("tags"));
			info.feederInfo.setNer_person((String)info_map.get("ner_person"));
			info.feederInfo.setNer_location((String)info_map.get("ner_location"));
			info.feederInfo.setNer_organization((String)info_map.get("ner_organization"));
			info.feederInfo.setMd5((String)info_map.get("md5"));
			info.feederInfo.setSimhash((String)info_map.get("simhash"));
			info.feederInfo.setType((String)info_map.get("type"));
			info.feederInfo.setDisplay_thumbnails((String)info_map.get("display_thumbnails"));
			if (info_map.get("type") != null)
				info.feederInfo.setType((String)info_map.get("type"));
			else
				info.feederInfo.setType(""); //not set any type as default
			if (info_map.get("content_type") != null)
			{
				info.feederInfo.setContent_type(String.valueOf((Integer)info_map.get("content_type")));
				info.feederInfo.setType(String.valueOf( (int)(Math.log((Integer)info_map.get("content_type"))/Math.log(2)) ));
			}
			else
			{
				String content_type = "1";
				if (info.getType() != null && info.getType().isEmpty() == false)
					content_type = String.valueOf(1<<Integer.valueOf(info.getType())); // type 0 => content_type 1; type 1 => content_type 2
				info.feederInfo.setContent_type(content_type);
			}
			info.feederInfo.setPublish_time(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("publish_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			info.feederInfo.setUpdate_time(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("update_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			info.feederInfo.setList_image_count((Integer)info_map.get("list_images_count"));
			info.feederInfo.setListImagesCount((Integer)info_map.get("list_images_count")); //redundant...
			info.feederInfo.setBody_image_count((Integer)info_map.get("body_images_count"));
			info.feederInfo.setBodyImagesCount((Integer)info_map.get("body_images_count")); //redundant...
			
			info.feederInfo.setPublisher((String)info_map.get("publisher"));
			info.feederInfo.setSourceType((String)info_map.get("source_type"));
			info.feederInfo.setSource((String)info_map.get("source"));
			info.feederInfo.setSourceFeeds((String)info_map.get("source_feeds"));
			info.feederInfo.setChannel((String)info_map.get("channel"));
			info.feederInfo.setAuthor((String)info_map.get("author"));
			info.feederInfo.setDiscoveryTime(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("discovery_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			info.feederInfo.setLink((String)info_map.get("link"));
			info.feederInfo.setTitle((String)info_map.get("title"));
			info.feederInfo.setContent((String)info_map.get("content"));
			info.feederInfo.setSummary((String)info_map.get("summary"));
			info.feederInfo.setCopyright((String)info_map.get("copyright"));
			info.feederInfo.setHasCopyright((Integer)info_map.get("has_copyright"));
			info.feederInfo.setBodyImages((String)info_map.get("body_images"));
			info.feederInfo.setListImages((String)info_map.get("list_images"));
			if (info_map.get("publisher_pagerank_score") instanceof java.math.BigDecimal)
				info.feederInfo.setPublisherPagerankScore(((java.math.BigDecimal)info_map.get("publisher_pagerank_score")).doubleValue());
			else
				info.feederInfo.setPublisherPagerankScore((Double)info_map.get("publisher_pagerank_score"));
			info.feederInfo.setSourceCommentCount((Integer)info_map.get("source_comment_count"));
			info.feederInfo.setFallImage((String)info_map.get("fall_image"));
			info.feederInfo.setDisplayListImages((String)info_map.get("display_list_images"));
			info.feederInfo.setIsOpenComment(String.valueOf(info_map.get("is_open_comment")));
			info.feederInfo.setCommentCount(info_map.get("comment_count")!=null ? (Integer)info_map.get("comment_count"):0);
			info.feederInfo.setFirm_app((String)info_map.get("firm_app"));
			info.feederInfo.setLink_type((Integer)info_map.get("link_type"));
			info.feederInfo.setDisplay_type((Integer)info_map.get("display_type"));
			info.feederInfo.setRate(info_map.get("rate")!=null ? (Integer)info_map.get("rate"):0);
			info.feederInfo.setContentQuality((int)((null == info_map.get("content_quality")) ? 0 : (Integer)info_map.get("content_quality")));

			if (mEditorEntry != null)
			{
				info.feederInfo = Feeder.applyEditorData(info.feederInfo, mEditorEntry.get(contentId));
			}
			// format: {"v1":[1013213247,1013030796,1013421848]}
			List<String> listRelatedArticles = new ArrayList<String>();
			String strRelatedArticles = (String)info_map.get("related_articles");
			JSONDataStruct1L jsondata1l = null;
			Map<String, List<String>> maps1l = null;
			//split "related_articles"
			String jsonString = "{\"jsondata\":" + strRelatedArticles +"}";
			try {
				jsondata1l = JSON.parseObject(jsonString, JSONDataStruct1L.class);
				maps1l = jsondata1l.getJsondata();
				if (maps1l != null) {
					for ( String key : maps1l.keySet() ) 
					{
						listRelatedArticles.addAll(maps1l.get(key));
						break; //ToDo: use property to config later..
					}
				}
			} catch (Exception e) {
				flowLogger.warn("This article "+info.feederInfo.getContent_id()+" cannot get related_articles!");
            }
			List<String> alRelativeGroupIds = ClientJDBCTemplate.getInstance().getDistinctGroupId(listRelatedArticles);
			Collections.sort(alRelativeGroupIds, Collections.reverseOrder());
			info.setRecomms(alRelativeGroupIds);

			info.setStemplate("NULL");									//?
			info.setTags((String)info_map.get("tags"));
			info.setBody((String)info_map.get("content"));
			info.setReply_count(info_map.get("comment_count")!=null ? (Integer)info_map.get("comment_count"):0);
			info.setRssid(Integer.valueOf(info_map.get("item_uuid")!=null ? (((String)info_map.get("item_uuid")).equals("")==false ? (String)info_map.get("item_uuid"):"0"):"0"));
			info.setSource_url((String)info_map.get("link"));
			info.setSource_mining((String)info_map.get("source"));
			info.setInfoid((String)info_map.get("content_id"));
			String summary = (info_map.get("summary") == null) ? "" : (String)info_map.get("summary");
			info.setSnippet(summary);
			info.setType(info.feederInfo.getType());
			info.setCategory(""); //中文表示的来源及大小分类
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
					e.printStackTrace();
				}
				info.setTm((int)(ts.getTime()/1000));
			}
			else
				info.setTm(0);
			info.setTitle((String)info_map.get("title"));
			info.setSource((String)info_map.get("publisher"));
			
			int nType = Integer.valueOf(info.feederInfo.getType());
			if (0 <= nType && nType <= 2) {
				
				info.setDatatype("1");
				info.setIsextend((byte)1);
				
				ExtendJson extJson = new ExtendJson();
				if (info.feederInfo.getLink_type() == 1)
					extJson.setFirst(2);
				else if (info.feederInfo.getLink_type() == 2)
					extJson.setFirst(3);
				else if (info.feederInfo.getLink_type() == 4)
					extJson.setFirst(7);
				else
					extJson.setFirst(3);
				extJson.setLinkurl((String)info_map.get("link"));
				extJson.setOriginurl((String)info_map.get("link"));
				extJson.setThird_url((String)info_map.get("link"));
				info.setJson(extJson);
			} else {
				
				info.setDatatype("1");
				info.setIsextend((byte)0);
			}
			
			List<String> listCategories = Feeder.getCategoriesListByVersion(info.feederInfo.getCategories());
			info.setTags(listCategories.toString());
			
			info.img = null;
			info.iscomm = Integer.valueOf(info.feederInfo.getIsOpenComment());
			info.recomms = listRelatedArticles;
			info.banner = 0;
			info.country = null;
			info.lang = Feeder.getMainLanguageByVersion(info.feederInfo.getLanguage(), "v1");
			
			info_list.add(info);
		}

		return info_list;
	}

}