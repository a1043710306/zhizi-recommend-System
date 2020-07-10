package inveno.spider.common.dao;

import inveno.spider.common.model.SourceFeed;

import java.util.List;

public interface SourceFeedDao
{
    SourceFeed querySourceFeed(String sourceType, String source, String sourceFeeds, String channel);
    SourceFeed querySourceFeedByUrl(String sourceType, String sourceFeedsUrl);
}
