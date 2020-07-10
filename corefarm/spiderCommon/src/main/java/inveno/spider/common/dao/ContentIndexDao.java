package inveno.spider.common.dao;

import inveno.spider.common.model.ContentIndex;

public interface ContentIndexDao {

	int insert(ContentIndex record);

	ContentIndex selectByContentId(String contentId);

	int updateByContentId(ContentIndex record);

}