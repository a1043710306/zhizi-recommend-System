package inveno.spider.common.dao.impl;

import static org.junit.Assert.*;
import inveno.spider.common.model.ChannelContentAudit;

import java.util.List;

import org.junit.Test;

public class ChannelContentAuditDaoImplTest
{

    @Test
    public void testSave()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testFindAllStringStringIntInt()
    {
        ChannelContentAuditDaoImpl dao = new ChannelContentAuditDaoImpl();
        List<ChannelContentAudit> list = dao.findAll("2014-11-24","2014-11-24", 0, 10);
        for(ChannelContentAudit c:list)
        {
            System.out.println(c.getTitle());
        }
    }

    @Test
    public void testFindAllStringIntInt()
    {
        ChannelContentAuditDaoImpl dao = new ChannelContentAuditDaoImpl();
    }

}
