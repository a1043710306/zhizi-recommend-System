package inveno.spider.common.dao.impl;

import java.util.List;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.CategoryVersionMappingDao;
import inveno.spider.common.model.CategoryVersionMapping;

public class CategoryVersionMappingDaoImpl implements CategoryVersionMappingDao
{
	@Override
	public List<CategoryVersionMapping> selectAll()
	{
		final String sqlId = "inveno.spider.common.mapper.CategoryVersionMappingMapper.selectAll";
		return SessionFactory.getInstance().selectList(sqlId, null);
	}
}
