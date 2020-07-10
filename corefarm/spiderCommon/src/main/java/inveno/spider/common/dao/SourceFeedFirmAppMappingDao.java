package inveno.spider.common.dao;

import inveno.spider.common.model.SourceFeedFirmAppMapping;

import java.util.List;

public interface SourceFeedFirmAppMappingDao
{
	SourceFeedFirmAppMapping queryFirmMapping(String sourceType, String source, String sourceFeeds, String channel);
	SourceFeedFirmAppMapping queryFirmMappingBySourceFeedId(int sourceFeedId);
}
