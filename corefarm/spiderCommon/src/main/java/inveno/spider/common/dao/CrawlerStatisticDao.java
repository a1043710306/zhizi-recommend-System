package inveno.spider.common.dao;

import inveno.spider.common.model.CrawlerStatistic;

public interface CrawlerStatisticDao
{
	CrawlerStatistic find(java.util.Date timestamp, String type, String source, String sourceType);
	void increaseCrawledAmount(java.util.Date timestamp, String type, String source, String sourceType);
	void increaseWarehousedAmount(java.util.Date timestamp, String type, String source, String sourceType);
	void increasePublishedAmount(java.util.Date timestamp, String type, String source, String sourceType);
	void increaseDeliveredAmount(java.util.Date timestamp, String type, String source, String sourceType);
}
