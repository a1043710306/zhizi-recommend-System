package inveno.spider.common.dao.impl;

import java.util.HashMap;
import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.PublisherDao;
import inveno.spider.common.model.Publisher;

public class PublisherDaoImpl implements PublisherDao {

	@Override
	public int insert(Publisher record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.PublisherMapper.insert", record);
	}

	@Override
	public Publisher selectByName(String name) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.PublisherMapper.selectByName", name);
	}

	@Override
	public int updateStatusByPrimaryKey(int s, int id) {
		int status = s;
		if(status != 1)
			status = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("status", status);
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.PublisherMapper.updateByPrimaryKeySelective", map);
	}
}
