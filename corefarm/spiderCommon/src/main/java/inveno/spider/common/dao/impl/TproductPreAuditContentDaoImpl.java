package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.TproductPreAuditContentDao;
import inveno.spider.common.model.TproductPreAuditContent;

public class TproductPreAuditContentDaoImpl implements TproductPreAuditContentDao {

	@Override
	public int insert(TproductPreAuditContent record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.TproductPreAuditContentMapper.insert", record);
	}

}
