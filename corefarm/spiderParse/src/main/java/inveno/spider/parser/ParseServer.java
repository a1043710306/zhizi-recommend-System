package inveno.spider.parser;

import inveno.spider.common.RabbitmqHelper;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.processor.CrawlWorker;
import inveno.spider.parser.processor.CrawlWorkerImpl;
import inveno.spider.rmq.model.QueuePrefix;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;


public class ParseServer
{
    private AtomicBoolean start;
    private static ParseServer instance = null;
    private static final Logger LOG = LoggerFactory.make();

    public synchronized static ParseServer getInstance()
    {
        if (null == instance)
        {
            instance = new ParseServer();
        }
        return instance;
    }

    private ParseServer()
    {
        start = new AtomicBoolean(false);
        
        new Thread(){
            public void run() {
                execute();
            };
        }.start();
    }

    public void stop()
    {
        start.set(false);
        LOG.info("Stopped parse server.");
    }

    public void start()
    {
        start.set(true);
        LOG.info("Started parse server.");

    }

    public boolean isStarted()
    {
        return start.get() ? true : false;
    }
    
    public String getAllWaitingJobs()
    {
        return ParseManager.getInstance().getAllWaitingJobs();
    }
    
    public String getRunningJobs()
    {
        return ParseManager.getInstance().getRunningJobs();
    }    

    private void execute()
    {
        
        while(true)
        {
            if(!ParseServer.getInstance().isStarted())
            {
                LOG.info("Parse server has been stopped.");
                sleep(5000);
                continue;
            }
            
            Page page = RabbitmqHelper.getInstance().getMessage(QueuePrefix.SYS_HTMLSOURCE_QUEUE.name(),Page.class);
            if(null==page)
            {
                LOG.info("Page is empty.");
                sleep(500);
                continue;
            }
            String profileName = page.getProfileName();
            Profile profile = Profile.readFromCache(profileName);
            if(null==profile)
            {
                LOG.info("Profile("+profileName+") is not exists.");
                sleep(500);
                continue;
            }

            CrawlWorker worker = null;
            if ("news".equalsIgnoreCase(profile.getType()) ||
                    "blog".equalsIgnoreCase(profile.getType()) )
                worker = new CrawlWorkerImpl(profile,page);
            
            if(null==worker)
            {
                LOG.info("Unknow media type.");
                sleep(500);
                continue;
            }
            
            if(!ParseManager.getInstance().submitJob(worker))
            {
              //if the pool is full,then take it back.
                RabbitmqHelper.getInstance().sendMessage(QueuePrefix.SYS_HTMLSOURCE_QUEUE.name(), page);
                sleep(5000);
            }
            sleep(50);
        }

    }
    
    private static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
