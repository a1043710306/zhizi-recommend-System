package com.inveno.feeder.infomapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.inveno.feeder.Feeder;
import com.inveno.feeder.ClientJDBCTemplate;
import com.inveno.feeder.util.FastJsonConverter;
import com.inveno.feeder.util.DateUtils;
import com.inveno.feeder.constant.FeederConstants;
import com.inveno.feeder.thrift.*;

public class InfoMapperChapter implements RowMapper<FeederInfo>
{
	private static final Logger flowLogger = Logger.getLogger("feeder.flow");

	@Override
	public FeederInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<FeederInfo> mapIntoFeederInfoList(List<Map<String, Object>> info_map_list, String time_after)
	{
		List<FeederInfo> info_list = new ArrayList<FeederInfo>();
		
		for (Map<String, Object> info_map : info_map_list)
		{
			String contentId = (String)info_map.get("content_id");
			String strContentType = String.valueOf(info_map.get("content_type"));
			int contentType = NumberUtils.toInt(strContentType);
			if (Feeder.isComicInfo(contentType)) {
				List<Map<String, Object>> alChapter = ClientJDBCTemplate.getInstance().listComicChapterByContentId(contentId, time_after);
				if (CollectionUtils.isNotEmpty(alChapter)) {
					FeederInfo feederInfo = new FeederInfo();
					feederInfo.setContent_id(contentId);
					//fixed to pass required field checking
					feederInfo.setLocal("");
					feederInfo.setCategories("");
					feederInfo.setLanguage("");
					feederInfo.setKeywords("");
					feederInfo.setType(String.valueOf((int)(Math.log(contentType)/Math.log(2)) ));
					feederInfo.setContent_type(strContentType);
					List<ChapterInfo> alChapters = new ArrayList<ChapterInfo>();
					long tsLatestPublishTime = -1;
					String strLatestPublishTime = null;
					for (Map<String, Object> mChapter : alChapter) {
						ChapterInfo chapterInfo = new ChapterInfo();
						String title = (String)mChapter.get("title");
						chapterInfo.setContent_id(contentId);
						chapterInfo.setChapter((Integer)mChapter.get("chapter"));
						chapterInfo.setTitle(title);
						chapterInfo.setBodyImages((String)mChapter.get("body_images"));
						chapterInfo.setBodyImagesCount((Integer)mChapter.get("body_images_count"));
						try {
							long tsPublishTime = (DateUtils.stringToDate(mChapter.get("publish_time").toString(), new Date())).getTime();
							String strPublishTime = DateUtils.dateToString(new Date(tsPublishTime));
							flowLogger.info("content_id=" + contentId + " chapter " + chapterInfo.getChapter() + " publish_time:" + strPublishTime + " ts:" + tsPublishTime);
							chapterInfo.setPublish_time_ts((long)(tsPublishTime / (long)1000L));
							chapterInfo.setPublish_time(strPublishTime);
							if (tsPublishTime > tsLatestPublishTime) {
								tsLatestPublishTime = tsPublishTime;
								strLatestPublishTime = strPublishTime;
							}
						}
						catch (Exception e) {
							flowLogger.fatal("[mapIntoFeederInfoList]", e);
						}
						alChapters.add(chapterInfo);
					}
					feederInfo.setPublish_time_ts((int)(tsLatestPublishTime / (long)1000L));
					feederInfo.setPublish_time(strLatestPublishTime);
					feederInfo.setChapterInfoList(alChapters);
					info_list.add(feederInfo);
				}
			}
		}

		return info_list;
	}

}