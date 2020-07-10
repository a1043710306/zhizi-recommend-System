package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.LinkPublisherDao;
import inveno.spider.common.model.LinkPublisher;

public class LinkPublisherDaoImpl implements LinkPublisherDao {

	@Override
	public int insert(LinkPublisher record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.LinkPublisherMapper.insert", record);
	}

}
