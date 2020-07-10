package inveno.spider.common.dao.impl;

import java.util.List;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ImageUrlFilterDao;
import inveno.spider.common.model.ImageUrlFilter;

public class ImageUrlFilterDaoImpl implements ImageUrlFilterDao {

	@Override
	public List<ImageUrlFilter> find()
	{
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.ImageUrlFilterMapper.select", null);
	}

}
