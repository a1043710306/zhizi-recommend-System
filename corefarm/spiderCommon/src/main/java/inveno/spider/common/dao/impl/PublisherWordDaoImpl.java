package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.PublisherWordDao;
import inveno.spider.common.model.PublisherWord;

import java.util.List;

public class PublisherWordDaoImpl implements PublisherWordDao {

	@Override
	public List<PublisherWord> find()
	{
		List<PublisherWord> filterWordList = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.PublisherWordMapper.select", null);
		return filterWordList;
	}

}
