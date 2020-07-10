package inveno.spider.parser.processor;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.Constants;
import inveno.spider.parser.base.CrawlQueue;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.extractor.Extractor;
import inveno.spider.parser.extractor.ExtractorsSuit;
import inveno.spider.parser.extractor.Extractor.Type;
import inveno.spider.parser.model.ManualRedirectConfig;
import inveno.spider.parser.model.Profile;

import java.util.Date;

import org.apache.log4j.Logger;


public abstract class CrawlerWorkerAdaptor  implements CrawlWorker{
	private static final Logger log = LoggerFactory.make();
	
	public abstract CrawlQueue getCrawlQueue();
	
	
    //用来在此类初始化以后执行一些业务操作
    protected void afterInit(){}
    
    /**
     * Planned start time.
     */
    private Date plannedStartTime;
    
    public void setPlannedStartTime(Date plannedStartTime)
    {
        this.plannedStartTime = plannedStartTime;
    }
    public Date getPlannedStartTime()
    {
        return this.plannedStartTime;
    }    
    
    /**
     * 上报超时日志
     * @param eventUrl
     * @param durationTime
     * @param msg
     * @param ex
     */
    public boolean reportTimeout(long durationTime,String msg,Exception ex){
        try{
            if(durationTime/1000 > Long.parseLong(Constants.TIMEOUT_THRESHHOLD)){
                              
                if(null!=ex){
                    ex.printStackTrace();
                }
                
                return true;
            }
        }catch(Exception e){
            log.error(e);
        } 
        return false;
    }
    
    /**
     * 手动重定向配置
     * @param profile
     * @param type
     * @return
     */
    public ManualRedirectConfig getMannualRedirectConfig(Profile profile,Type type){
    	if(Extractor.Type.Listing==type){
    		return profile.getListingConfig().getManualRedirectConfig();
    	}else if(Extractor.Type.Content==type){
    		return profile.getContentConfig().getManualRedirectConfig();
    	}
    	return null;
    }
    
    /**
     * 如果发生错误，加入队列，等待下次调度
     * @param page
     * @param progress
     */
    public boolean reCrawlOnErr(Page page){
    	if(null==page.getMeta().get(Page.Meta.errorTimes)) //if not inited,init it.
    		page.getMeta().put(Page.Meta.errorTimes, 0);
    	
    	if((Integer)page.getMeta().get(Page.Meta.errorTimes)<Integer.parseInt(Constants.MAX_ERROR_TIMES_PER_PAGE)){
    		getProgress().addTotal(1);
        	
    		page.getMeta().put(Page.Meta.errorTimes, (Integer)(page.getMeta().get(Page.Meta.errorTimes))+1);
    		getCrawlQueue().submit(page);
        	
        	log.info("Recrawl on error:"+page.getUrl()+"\ttimes:"+page.getMeta().get(Page.Meta.errorTimes));
        	return true;
    	}
    	
    	return false;
    }
    
    /**
     * 此worker的内容分析套件。
     * @return
     */
    protected ExtractorsSuit getExtractorsSuit(){return null;}
}
