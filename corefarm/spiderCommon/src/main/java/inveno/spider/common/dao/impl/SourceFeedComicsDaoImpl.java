package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SourceFeedComicsDao;
import inveno.spider.common.model.Content;

public class SourceFeedComicsDaoImpl implements SourceFeedComicsDao {

	@Override
	public int insert(Content record) {
		// TODO Auto-generated method stub
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.SourceFeedComicsMapper.insert", record);

	}

}
