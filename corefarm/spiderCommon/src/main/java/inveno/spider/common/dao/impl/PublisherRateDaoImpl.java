package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.PublisherRateDao;
import inveno.spider.common.model.PublisherRate;

import java.util.List;

public class PublisherRateDaoImpl implements PublisherRateDao {

	@Override
	public List<PublisherRate> find()
	{
		List<PublisherRate> alData = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.PublisherRateMapper.select", null);
		return alData;
	}
}
