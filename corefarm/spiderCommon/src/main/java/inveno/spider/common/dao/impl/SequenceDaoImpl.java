package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SequenceDao;


public class SequenceDaoImpl implements SequenceDao {

	@Override
	public String querySequenceVal(String seqName) {
		String sequen =SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SequenceMapper.querySequenceVal", seqName);
	    return sequen;
	}
}
