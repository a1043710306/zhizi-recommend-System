package inveno.spider.common.dao.impl;

import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ImageSexyCheckDao;
import inveno.spider.common.model.ImgSexyResult;

public class ImageSexyCheckDaoImpl implements ImageSexyCheckDao {

	@Override
	public int insert(ImgSexyResult record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.ImageSexyCheckMapper.insert", record);
	}

	@Override
	public ImgSexyResult selectLastest2MonthByContentIdAndImgUrl(Map<String, String> map) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ImageSexyCheckMapper.selectLastest2MonthByContentIdAndImgUrl", map);
	}

}
