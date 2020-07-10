package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.inveno.feeder.model.ImagesEntry;
import org.springframework.jdbc.core.RowMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.inveno.feeder.Feeder;
import com.inveno.feeder.datainfo.EditorTableEntry;
import com.inveno.feeder.ClientJDBCTemplate;
import com.inveno.feeder.jsondatastruct.JSONDataStruct1L;
import com.inveno.feeder.thrift.*;

public class InfoMapperAPI implements RowMapper<FeederInfo>
{
	private static final Logger flowLogger    = Logger.getLogger("feeder.flow");

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
		String mixtureSource = Feeder.getMixtureSource();
		List<FeederInfo> info_list = new ArrayList<FeederInfo>();
		
		for (Map<String, Object> info_map : info_map_list)
		{
			//flowLogger.info("InfoMapperAPI info_map="+JSON.toJSONString(info_map));

			FeederInfo info = new FeederInfo();
			String contentId = (String)info_map.get("content_id");
			info.setContent_id(contentId);
			info.setLocal((String)info_map.get("local"));
			info.setLanguage((String)info_map.get("language"));
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
			info.setSourceType((String)info_map.get("source_type"));
			info.setPublisher((String)info_map.get("publisher"));
			info.setSource((String)info_map.get("source"));
			info.setAuthor((String)info_map.get("author"));
			info.setKeywords((String)info_map.get("keywords_new"));
            info.setTags((String)info_map.get("tags"));
			//flowLogger.info("InfoMapperAPI info_map getkeywords="+info_map.get("keywords"));
			String jsonCategories = (String)info_map.get("categories");
			String categories = "";
			if (StringUtils.isNotEmpty(jsonCategories))
			{
				List<String> alCategories = Feeder.getCategoriesListByVersion(jsonCategories);
				categories = StringUtils.join(alCategories, ",");
			}
			info.setCategories(categories);
			info.setLink((String)info_map.get("link"));
			info.setTitle((String)info_map.get("title"));
			info.setContent((String)info_map.get("content"));
			info.setSummary((String)info_map.get("summary"));
			info.setCopyright((String)info_map.get("copyright"));
			info.setHasCopyright((Integer)info_map.get("has_copyright"));
			info.setBodyImages((String)info_map.get("body_images"));
			info.setListImages((String)info_map.get("list_images"));
			info.setSourceCommentCount(info_map.get("source_comment_count")!=null ? (Integer)info_map.get("source_comment_count"):0);
			info.setFallImage((String)info_map.get("fall_image"));
			info.setDisplayListImages((String)info_map.get("display_list_images"));
			info.setIsOpenComment(String.valueOf(info_map.get("is_open_comment")));
			info.setCommentCount(info_map.get("comment_count")!=null ? (Integer)info_map.get("comment_count"):0);
			info.setFirm_app((String)info_map.get("firm_app"));
			info.setLink_type((Integer)info_map.get("link_type"));
			info.setDisplay_type((Integer)info_map.get("display_type"));
			info.setSourceItemId((String)info_map.get("source_item_id"));
			info.setDuration((Integer)info_map.get("duration"));
			info.setState((Integer)info_map.get("state"));
			info.setDisplay_thumbnails((String)info_map.get("display_thumbnails"));
			info.setGroup_id((String)info_map.get("group_id"));
			info.setContentQuality((int)((null == info_map.get("content_quality")) ? 0 : (Integer)info_map.get("content_quality")));
			info.setIs_display_ad(NumberUtils.toInt(String.valueOf(info_map.get("is_display_ad")), 0));
			info.setUpdate_time(com.inveno.feeder.util.DateUtils.getStringDateFormat(info_map.get("update_time"), "yyyy-MM-dd HH:mm:ss.SSS"));
			String flag = String.valueOf((null == info_map.get("flag")) ? 0 : info_map.get("flag"));
			if (!"0".equalsIgnoreCase(flag))
			{
				info.setFlag(flag);
			}
			info.setThirdparty_topics(info_map.get("topics")==null?"":(String)info_map.get("topics"));
			if (info_map.get("publish_time") != null)
			{
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				try {
					if (info_map.get("publish_time") instanceof Long) {
						//Todo: find the root cause on this time mismatch..
						ts = new Timestamp((long)info_map.get("publish_time") - 28800000l);
					}
					else
						ts = new Timestamp(fmt.parse(info_map.get("publish_time").toString()).getTime());
				} catch (ParseException e) {
				}
				info.setPublish_time_ts((int)(ts.getTime()/1000));
			}
			else
				info.setPublish_time_ts(0);
			
			// format: {"v1":[1013213247,1013030796,1013421848]}
			List<String> listRelatedArticles = new ArrayList<String>();
//			flowLogger.info("InfoMapperAPI info_map contentId=" + info.getContent_id() + " strRelatedArticles="+info_map.get("related_articles"));
			String strRelatedArticles = (String)info_map.get("related_articles");
			JSONDataStruct1L jsondata1l = null;
			Map<String, List<String>> maps1l = null;
			//split "related_articles"
			String jsonString = "{\"jsondata\":" + strRelatedArticles +"}";
			try
			{
				jsondata1l = JSON.parseObject(jsonString, JSONDataStruct1L.class);
				maps1l = jsondata1l.getJsondata();
				if (maps1l != null) {
					for ( String key : maps1l.keySet() ) 
					{
						listRelatedArticles.addAll(maps1l.get(key));
						break; //ToDo: use property to config later..
					}
				}
			}
			catch (Exception e)
			{
                //e.printStackTrace();
            }
//			flowLogger.info("InfoMapperAPI info_map " + info.getContent_id() + " listRelatedArticles="+JSON.toJSONString(listRelatedArticles));
            if (Feeder.isEnableContentGroup())
            {
				List<String> alRelativeGroupIds = ClientJDBCTemplate.getInstance().getDistinctGroupId(listRelatedArticles);
				Map<String, String> simHashMap = ClientJDBCTemplate.getInstance().getSignalList(alRelativeGroupIds);
				List<String> newAlRelativeGroupIds = Feeder.getHammingDistance(simHashMap);
				Collections.sort(newAlRelativeGroupIds, Collections.reverseOrder());
				if(alRelativeGroupIds.size()!= newAlRelativeGroupIds.size()){
                    flowLogger.info("convert related_articles content_id=" + info.getContent_id() + "\tfrom " + listRelatedArticles + " to " + alRelativeGroupIds + " to "+ newAlRelativeGroupIds);
                }
				flowLogger.debug("convert related_articles content_id=" + info.getContent_id() + "\tfrom " + listRelatedArticles + " to " + alRelativeGroupIds + " to "+ newAlRelativeGroupIds);
				info.setRecomms(newAlRelativeGroupIds);
			}
			else
			{
				Collections.sort(listRelatedArticles, Collections.reverseOrder());
				info.setRecomms(listRelatedArticles);
			}
			info.setComicCategories((String)info_map.get("categories_comic"));

			if (mEditorEntry != null)
			{
				EditorTableEntry editorEntry = mEditorEntry.get(contentId);
				info = Feeder.applyEditorData(info, editorEntry);
				jsonCategories = info.getCategories();
				if (editorEntry != null && StringUtils.isNotEmpty(editorEntry.getCategories()))
				{
					List<String> alCategories = Feeder.getCategoriesListByVersion(jsonCategories);
					categories = StringUtils.join(alCategories, ",");
					info.setCategories(categories);
				}
			}
			//混合订阅源处理逻辑
			if(StringUtils.isNotEmpty(mixtureSource)){
				flowLogger.info("mixtureSource:"+mixtureSource);
				String sources [] = mixtureSource.split(",");
				List<String> sourceList = Arrays.asList(sources);
				if(sourceList.contains(info.getSource())){
					info.setSource(info.getPublisher());
					flowLogger.info("mixtureSource:source"+info.getSource());
				}
			}
			info_list.add(info);
		}

		return info_list;
	}

}