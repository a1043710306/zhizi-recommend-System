package inveno.spider.common.dao;

import inveno.spider.common.model.ContentExtend;

public interface ContentExtendDao {

	int insert(ContentExtend record);

	ContentExtend selectByContentId(String contentId);

	int updateByContentId(ContentExtend record);

}