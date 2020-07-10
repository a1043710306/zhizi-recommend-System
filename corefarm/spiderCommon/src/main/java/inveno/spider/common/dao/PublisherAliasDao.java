package inveno.spider.common.dao;

import inveno.spider.common.model.PublisherAlias;

public interface PublisherAliasDao {

	int insert(PublisherAlias record);

	PublisherAlias selectByAlias(String alias);
}
