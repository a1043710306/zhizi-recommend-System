package inveno.spider.common.dao.impl;

import static org.junit.Assert.*;

import java.util.List;

import inveno.spider.common.dao.SeedDao;
import inveno.spider.common.model.Seed;

import org.junit.Test;

public class SeedDaoImplTest
{
    private SeedDao seedDao = new SeedDaoImpl();

    @Test
    public void testLoadByProfileId()
    {
       List<Seed> seeds = seedDao.loadByProfileId(621);
       
       for(Seed s:seeds)
       {
           System.out.println(s.getUrl());
           System.out.println(s.getLevel());
           System.out.println(s.getDataType());
           System.out.println(s.getIsOpenComment());
           System.out.println("------------");
       }
    }
    
    @Test
    public void testFindByName()
    {
        List<Seed> seeds = seedDao.findByName("21CN", 1, 20, null, null);
        
        for(Seed s:seeds)
        {
            System.out.println(s.getUrl());
        }
    }

}
