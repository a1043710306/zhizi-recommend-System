package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ChannelContentAuditDao;
import inveno.spider.common.model.ChannelContent;
import inveno.spider.common.model.ChannelContentAudit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelContentAuditDaoImpl implements ChannelContentAuditDao
{

    @Override
    public void save(ChannelContentAudit article)
    {
        SessionFactory.getInstance().insert("inveno.spider.common.mapper.ChannelContentAuditMapper.insert", article);
    }

    @Override
    public void update(ChannelContentAudit cca)
    {
        SessionFactory.getInstance().update("inveno.spider.common.mapper.ChannelContentAuditMapper.update", cca);
    }

    @Override
    public ChannelContentAudit queryByUrl(String url)
    {
        return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ChannelContentAuditMapper.queryByUrl", url);  
    }

    @Override
    public List<ChannelContentAudit> findAll(String startDate,String endDate,int postion,int size)
    {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("startTime", startDate+" 00:00:00");
        map.put("endTime", endDate+" 23:59:59");
        map.put("postion", postion);
        map.put("size", size);
        return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.ChannelContentAuditMapper.selectRecent", map);
    }

    @Override
    public List<ChannelContentAudit> findAll(String date, int postion,int size)
    {
        return findAll(date,date,postion,size);
    }

}
