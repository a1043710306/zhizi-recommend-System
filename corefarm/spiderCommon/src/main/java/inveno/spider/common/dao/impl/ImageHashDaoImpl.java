package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ImageHashDao;
import inveno.spider.common.model.ImageHash;

public class ImageHashDaoImpl implements ImageHashDao {

	@Override
	public int insert(ImageHash record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.ImageHashMapper.insert", record);
	}

	@Override
	public ImageHash selectByContentId(String contentId) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ImageHashMapper.selectByContentId", contentId);
	}

	@Override
	public int updateByContentId(ImageHash record) {
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.ImageHashMapper.updateByContentId", record);
	}

}
