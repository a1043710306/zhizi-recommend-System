package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SourceDao;
import inveno.spider.common.model.Source;

import java.util.List;

public class SourceDaoImpl implements SourceDao {

	@Override
	public List<Source> find() {
		List<Source> sourceList = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.SourceMapper.select", null);
		return sourceList;
	} 
}
