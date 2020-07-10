package inveno.spider.common.dao;

import inveno.spider.common.model.Publisher;

public interface PublisherDao {

	int insert(Publisher record);

	Publisher selectByName(String name);

	int updateStatusByPrimaryKey(int status, int id);
	
}
