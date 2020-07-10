package inveno.spider.common.dao.impl;

import java.util.Map;
import java.util.HashMap;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.CrawlerStatisticDao;
import inveno.spider.common.model.CrawlerStatistic;

public class CrawlerStatisticDaoImpl implements CrawlerStatisticDao
{
	public CrawlerStatistic find(java.util.Date timestamp, String type, String source, String sourceType)
	{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timestamp", timestamp);
		map.put("type", type);
		map.put("source", source);
		map.put("sourceType", sourceType);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.CrawlerStatisticMapper.select", map);
	}
	public void increaseCrawledAmount(java.util.Date timestamp, String type, String source, String sourceType)
	{
		CrawlerStatistic bean = find(timestamp, type, source, sourceType);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timestamp", timestamp);
		map.put("type", type);
		map.put("source", source);
		map.put("sourceType", sourceType);
		if (bean == null)
		{
			map.put("crawledAmount", 1);
			map.put("warehousedAmount", 0);
			map.put("publishedAmount", 0);
			map.put("deliveredAmount", 0);
			SessionFactory.getInstance().insert("inveno.spider.common.mapper.CrawlerStatisticMapper.insert", map);
		}
		else
			SessionFactory.getInstance().update("inveno.spider.common.mapper.CrawlerStatisticMapper.increaseCrawledAmount", map);
	}
	public void increaseWarehousedAmount(java.util.Date timestamp, String type, String source, String sourceType)
	{
		CrawlerStatistic bean = find(timestamp, type, source, sourceType);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timestamp", timestamp);
		map.put("type", type);
		map.put("source", source);
		map.put("sourceType", sourceType);
		if (bean == null)
		{
			map.put("crawledAmount", 0);
			map.put("warehousedAmount", 1);
			map.put("publishedAmount", 0);
			map.put("deliveredAmount", 0);
			SessionFactory.getInstance().insert("inveno.spider.common.mapper.CrawlerStatisticMapper.insert", map);
		}
		else
			SessionFactory.getInstance().update("inveno.spider.common.mapper.CrawlerStatisticMapper.increaseWarehousedAmount", map);
	}
	public void increasePublishedAmount(java.util.Date timestamp, String type, String source, String sourceType)
	{
		CrawlerStatistic bean = find(timestamp, type, source, sourceType);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timestamp", timestamp);
		map.put("type", type);
		map.put("source", source);
		map.put("sourceType", sourceType);
		if (bean == null)
		{
			map.put("crawledAmount", 0);
			map.put("warehousedAmount", 0);
			map.put("publishedAmount", 1);
			map.put("deliveredAmount", 0);
			SessionFactory.getInstance().insert("inveno.spider.common.mapper.CrawlerStatisticMapper.insert", map);
		}
		else
			SessionFactory.getInstance().update("inveno.spider.common.mapper.CrawlerStatisticMapper.increasePublishedAmount", map);
	}
	public void increaseDeliveredAmount(java.util.Date timestamp, String type, String source, String sourceType)
	{
		CrawlerStatistic bean = find(timestamp, type, source, sourceType);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timestamp", timestamp);
		map.put("type", type);
		map.put("source", source);
		map.put("sourceType", sourceType);
		if (bean == null)
		{
			map.put("crawledAmount", 0);
			map.put("warehousedAmount", 0);
			map.put("publishedAmount", 0);
			map.put("deliveredAmount", 1);
			SessionFactory.getInstance().insert("inveno.spider.common.mapper.CrawlerStatisticMapper.insert", map);
		}
		else
			SessionFactory.getInstance().update("inveno.spider.common.mapper.CrawlerStatisticMapper.increaseDeliveredAmount", map);
	}
}
