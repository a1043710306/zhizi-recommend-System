package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.test.LinkPublisherDao;
import inveno.spider.common.model.test.LinkPublisher;

public class LinkPublisherDaoImplTest implements LinkPublisherDao {

	@Override
	public int insert(LinkPublisher record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.test.LinkPublisherMapper.insert", record);
	}

}
