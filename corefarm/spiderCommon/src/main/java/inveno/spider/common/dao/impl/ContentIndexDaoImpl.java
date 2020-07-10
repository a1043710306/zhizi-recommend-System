package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ContentIndexDao;
import inveno.spider.common.model.ContentIndex;

public class ContentIndexDaoImpl implements ContentIndexDao {

	@Override
	public int insert(ContentIndex record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.ContentIndexMapper.insert", record);
	}

	@Override
	public ContentIndex selectByContentId(String contentId) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ContentIndexMapper.selectByContentId", contentId);
	}

	@Override
	public int updateByContentId(ContentIndex record) {
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.ContentIndexMapper.updateByContentId", record);
	}

}
