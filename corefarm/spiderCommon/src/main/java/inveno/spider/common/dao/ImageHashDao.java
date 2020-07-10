package inveno.spider.common.dao;

import inveno.spider.common.model.ImageHash;

public interface ImageHashDao {

	int insert(ImageHash record);

	ImageHash selectByContentId(String contentId);

	int updateByContentId(ImageHash record);

}