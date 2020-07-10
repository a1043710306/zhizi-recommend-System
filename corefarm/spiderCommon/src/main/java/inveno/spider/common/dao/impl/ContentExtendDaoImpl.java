package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ContentExtendDao;
import inveno.spider.common.model.ContentExtend;

public class ContentExtendDaoImpl implements ContentExtendDao {

	@Override
	public int insert(ContentExtend record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.ContentExtendMapper.insert", record);
	}

	@Override
	public ContentExtend selectByContentId(String contentId) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ContentExtendMapper.selectByContentId", contentId);
	}

	@Override
	public int updateByContentId(ContentExtend record) {
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.ContentExtendMapper.updateByContentId", record);
	}

}
