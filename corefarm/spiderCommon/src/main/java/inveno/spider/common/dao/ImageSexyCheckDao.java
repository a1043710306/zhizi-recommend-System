package inveno.spider.common.dao;

import java.util.Map;

import inveno.spider.common.model.ImgSexyResult;

public interface ImageSexyCheckDao
{
	int insert(ImgSexyResult record);
	
	ImgSexyResult selectLastest2MonthByContentIdAndImgUrl(Map<String, String> map);
}
