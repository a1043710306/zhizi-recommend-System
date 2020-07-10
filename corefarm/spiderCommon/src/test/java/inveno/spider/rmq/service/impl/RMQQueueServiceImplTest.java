package inveno.spider.rmq.service.impl;

import static org.junit.Assert.*;
import inveno.spider.rmq.model.RMQQueue;
import inveno.spider.rmq.service.RMQQueueService;
import inveno.spider.rmq.service.impl.RMQQueueServiceImpl;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public class RMQQueueServiceImplTest
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    /**
     * Test method for {@link inveno.spider.rmq.service.impl.RMQQueueServiceImpl#loadAll()}.
     */
    @Test
    public void testLoadAll()
    {
        RMQQueueService service = new RMQQueueServiceImpl();
        List<RMQQueue> list = service.loadAll();
        for(RMQQueue q:list)
        {
            System.out.println(q);
        }
    }
    @Test
    public void testLoadCrawlQueue()
    {
        long start=System.currentTimeMillis();
        RMQQueueService service = new RMQQueueServiceImpl();
        List<RMQQueue> list = service.loadCrawlQueue();
        System.out.println("==========size:"+list.size()+"========="+(System.currentTimeMillis()-start));
        for(RMQQueue q:list)
        {
            System.out.println(q);
        }
    }

}
