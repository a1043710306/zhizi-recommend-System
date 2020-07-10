package inveno.spider.common.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.CategoryDao;
import inveno.spider.common.model.Category;

public class CategoryDaoImpl implements CategoryDao {

	@Override
	public List<Category> find() {
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.CategoryMapper.select", null);
	}
	public List<Category> listCategory(int version)
	{
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("version", version);
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.CategoryMapper.listCategory", map);
	}
}
