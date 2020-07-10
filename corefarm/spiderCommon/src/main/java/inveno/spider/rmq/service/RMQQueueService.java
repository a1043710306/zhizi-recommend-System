package inveno.spider.rmq.service;

import inveno.spider.rmq.model.RMQQueue;

import java.util.List;

/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public interface RMQQueueService
{
    List<RMQQueue> loadAll();
    
    /**
     * <i>Deprecated</i>, use <source>loadCrawlQueue(String prefix)</source> instead of it.
     * @return
     */
    @Deprecated
    List<RMQQueue> loadCrawlQueue();
    
    /**
     * Get queue list via prefix.
     * 
     * @param prefix Prefix for queue.<br/>e.g: normal_ , post_
     * @return
     */
    List<RMQQueue> loadCrawlQueue(String prefix);
}
