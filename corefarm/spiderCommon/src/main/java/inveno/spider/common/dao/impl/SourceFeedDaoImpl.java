package inveno.spider.common.dao.impl;

import inveno.spider.common.Constants;
import inveno.spider.common.ReadSessionFactory;
import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SourceFeedDao;
import inveno.spider.common.model.SourceFeed;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

public class SourceFeedDaoImpl implements SourceFeedDao {

	@Override
	public SourceFeed querySourceFeed(String sourceType, String source, String sourceFeeds, String channel)
	{
		HashMap map = new HashMap();
		map.put("sourceType", sourceType.trim());
		map.put("source", source.trim());
		if (sourceFeeds != null)
			map.put("sourceFeeds", sourceFeeds.trim());
		if (channel != null)
			map.put("channel", channel.trim());
		if(StringUtils.isNotBlank(Constants.ENV_CODE) && Constants.ENV_CODE.equalsIgnoreCase("domestic"))
			return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedMapper.select", map);
    	return ReadSessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedMapper.selectSeedAndExtend", map);
	}
	@Override
	public SourceFeed querySourceFeedByUrl(String sourceType, String sourceFeedsUrl)
	{
		HashMap map = new HashMap();
		map.put("sourceType", sourceType.trim());
		map.put("sourceFeedsUrl", sourceFeedsUrl.trim());
		if(StringUtils.isNotBlank(Constants.ENV_CODE) && Constants.ENV_CODE.equalsIgnoreCase("domestic"))
			return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedMapper.selectByUrl", map);
		return ReadSessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SourceFeedMapper.selectSeedAndExtendByUrl", map);
	}
}
