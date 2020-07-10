package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.FilterWordDao;
import inveno.spider.common.model.FilterWord;

import java.util.List;

public class FilterWordDaoImpl implements FilterWordDao {

	@Override
	public List<FilterWord> find() {
		List<FilterWord> filterWordList = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.FilterWordMapper.select", null);
		return filterWordList;
	}

}
