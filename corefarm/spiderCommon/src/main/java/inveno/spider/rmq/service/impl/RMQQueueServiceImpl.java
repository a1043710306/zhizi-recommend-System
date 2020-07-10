package inveno.spider.rmq.service.impl;

import inveno.spider.common.Constants;
import inveno.spider.common.utils.ModelBuilder;
import inveno.spider.rmq.http.AuthorizationException;
import inveno.spider.rmq.http.HttpService;
import inveno.spider.rmq.http.HttpServiceImpl;
import inveno.spider.rmq.model.Filter;
import inveno.spider.rmq.model.FilterList;
import inveno.spider.rmq.model.QueuePrefix;
import inveno.spider.rmq.model.RMQAPI;
import inveno.spider.rmq.model.RMQQueue;
import inveno.spider.rmq.model.RMQQueueComparator;
import inveno.spider.rmq.model.RMQQueueFilter;
import inveno.spider.rmq.service.RMQQueueService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public class RMQQueueServiceImpl implements RMQQueueService
{
    private HttpService httpService;

    public RMQQueueServiceImpl()
    {
        httpService = new HttpServiceImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.rmq.service.RMQQueueService#loadAll()
     */
    @Override
    public List<RMQQueue> loadAll()
    {
        List<RMQQueue> list = new ArrayList<RMQQueue>();
        try
        {
            String result = httpService.get(Constants.RABBIT_USERNAME,
                    Constants.RABBIT_PASSWORD, RMQAPI.API_GET_QUEUE);

            TypeToken<List<RMQQueue>> typeToken = new TypeToken<List<RMQQueue>>()
            {
            };
            list = ModelBuilder.getInstance().buildList(result, typeToken);
            Collections.sort(list, new RMQQueueComparator());
        } catch (AuthorizationException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.rmq.service.RMQQueueService#loadCrawlQueue()
     */
    @Override
    public List<RMQQueue> loadCrawlQueue(String prefix)
    {
        List<RMQQueue> originalList = loadAll();
        Filter<RMQQueue, String> filter = new RMQQueueFilter();
        List<RMQQueue> temp = new FilterList<String>().filterList(originalList,
                filter, prefix);
        return temp;
    }

    @Override
    public List<RMQQueue> loadCrawlQueue()
    {
        List<RMQQueue> originalList = loadAll();
        Filter<RMQQueue, String> filter = new RMQQueueFilter();
        List<RMQQueue> temp = new FilterList<String>().filterList(originalList,
                filter, QueuePrefix.SPIDER.getValue());
        return temp;
    }

}
