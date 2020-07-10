package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SourceFeedFirmAppMappingDao;
import inveno.spider.common.model.SourceFeedFirmAppMapping;

import java.util.HashMap;

public class SourceFeedFirmAppMappingDaoImpl implements SourceFeedFirmAppMappingDao
{
	@Override
	public SourceFeedFirmAppMapping queryFirmMapping(String sourceType, String source, String sourceFeeds, String channel)
	{
		HashMap map = new HashMap();
		map.put("sourceType", sourceType);
		map.put("source", source);
		map.put("sourceFeeds", sourceFeeds);
		map.put("channel", channel);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedFirmAppMappingMapper.select", map);
	}
	@Override
	public SourceFeedFirmAppMapping queryFirmMappingBySourceFeedId(int sourceFeedsId)
	{
		HashMap map = new HashMap();
		map.put("sourceFeedsId", sourceFeedsId);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedFirmAppMappingMapper.selectBySourceFeedId", map);
	}
}
