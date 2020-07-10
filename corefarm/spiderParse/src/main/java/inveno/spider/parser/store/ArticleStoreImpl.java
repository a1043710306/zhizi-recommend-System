package inveno.spider.parser.store;

import inveno.spider.common.RabbitmqHelper;
import inveno.spider.common.RedisHelper;
import inveno.spider.common.utils.LoggerFactory;



import inveno.spider.parser.utils.Utils;
import inveno.spider.rmq.model.QueuePrefix;

import org.apache.log4j.Logger;

public class ArticleStoreImpl implements ArticleStore
{
    private static final Logger LOG = LoggerFactory.make();


    public ArticleStoreImpl(String pubcode)
    {
        
    }

    public boolean contains(String url)
    {
        String key = Utils.getMD5Str(url);
        if (RedisHelper.getInstance().exists(key))
        {
            return true;
        }
        return false;
    }

    /**
     * Save article to RMQ,then put URL to redis.
     */
    public void save(Article article)
    {
    	LOG.info("SYS_MESSAGE_QUEUE: SYS_MESSAGE_QUEUE");
    	LOG.info("article: "+article);
//        RabbitmqHelper.getInstance().sendMessage(QueuePrefix.SYS_MESSAGE_QUEUE.name(), article);
    }

    public void close()
    {
    }

}
