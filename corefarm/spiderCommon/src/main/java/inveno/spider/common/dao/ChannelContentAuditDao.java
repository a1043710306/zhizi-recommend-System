package inveno.spider.common.dao;

import inveno.spider.common.model.ChannelContentAudit;

import java.util.List;

public interface ChannelContentAuditDao
{
	void save(ChannelContentAudit article);
	void update(ChannelContentAudit cca);
	ChannelContentAudit queryByUrl(String url);
	List<ChannelContentAudit> findAll(String startDate,String endDate,int postion,int size);
	List<ChannelContentAudit> findAll(String date,int postion,int size);
}
