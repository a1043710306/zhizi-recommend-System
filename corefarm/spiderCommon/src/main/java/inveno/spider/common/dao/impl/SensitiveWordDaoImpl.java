package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SensitiveWordDao;
import inveno.spider.common.model.SensitiveWord;

import java.util.List;

public class SensitiveWordDaoImpl implements SensitiveWordDao {

	@Override
	public List<SensitiveWord> find(int id) {
		SensitiveWord sword = new SensitiveWord();
		sword.setId(id);
		List<SensitiveWord> sensitiveWordList = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.SensitiveWordMapper.select", sword);
		return sensitiveWordList;
	}
}
