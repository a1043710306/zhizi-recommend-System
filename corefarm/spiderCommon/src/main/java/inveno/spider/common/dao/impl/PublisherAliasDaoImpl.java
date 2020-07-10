package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.PublisherAliasDao;
import inveno.spider.common.model.PublisherAlias;

public class PublisherAliasDaoImpl implements PublisherAliasDao {

	@Override
	public int insert(PublisherAlias record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.PublisherAliasMapper.insert", record);
	}

	@Override
	public PublisherAlias selectByAlias(String alias) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.PublisherAliasMapper.selectByAlias", alias);
	}
}
