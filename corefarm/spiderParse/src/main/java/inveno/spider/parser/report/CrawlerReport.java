package inveno.spider.parser.report;



import inveno.spider.common.utils.CrawlErrorLogger;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.Constants;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.model.ContentConfig;
import inveno.spider.parser.model.FeedConfig;
import inveno.spider.parser.model.JsonContentConfig;
import inveno.spider.parser.model.JsonListingConfig;
import inveno.spider.parser.model.ListingConfig;
import inveno.spider.parser.processor.CrawlerWorkerAdaptor;
import inveno.spider.parser.store.Article;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class CrawlerReport {
    private static final Logger log = LoggerFactory.make();
    private transient final ArticleReportVerifier mArticleVerifier;

    private String mPubcode;
    private String mProfileName;
    private Timestamp mPlannedStartTime;
    private Timestamp mStartTime;
    private Timestamp mEndTime;

    private long mTotalNetworkDuration = 0;
    private long mTotalExtractionDuration = 0;
    private int mListingPageCount = 0;
    private int mFeedPageCount = 0;
    private int mContentPageCount = 0;
    private int mArticleCount = 0;
    private int mTotalArticleLength = 0;
    
    private List<ArticleEvent> mArticleEvents = new ArrayList<ArticleEvent>();
    private List<ListingEvent> mListingEvents = new ArrayList<ListingEvent>();
    private List<ListingEvent> mFeedEvents = new ArrayList<ListingEvent>();
    private List<CrawlerEvent> mEvents = new ArrayList<CrawlerEvent>();
    
    private long lastRefreshTime=0;
    private ObjectOutputStream coreOut;
    private CrawlerWorkerAdaptor worker;
    

    public CrawlerReport(CrawlerWorkerAdaptor worker,String pubcode, String profileName) {
        mPubcode = pubcode;
        mProfileName = profileName;
        mArticleVerifier = new ArticleReportVerifier();
        
        this.worker=worker;
    }
    
    public CrawlerReport(String pubcode, String profileName) {
        mPubcode = pubcode;
        mProfileName = profileName;
        mArticleVerifier = new ArticleReportVerifier();
    }
    


    public long getAvgNetworkDuration() {
        int allPageCount = mListingPageCount + mFeedPageCount + mContentPageCount;
        return allPageCount>0 ? mTotalNetworkDuration / allPageCount : 0;
    }

    public long getAvgExtractionDuration() {
        int allPageCount = mListingPageCount + mFeedPageCount + mContentPageCount;
        return allPageCount>0 ? mTotalExtractionDuration / allPageCount : 0;
    }

    public int getListingPageCount() {
        return mListingPageCount;
    }

    public int getFeedPageCount() {
        return mFeedPageCount;
    }

    public int getContentPageCount() {
        return mContentPageCount;
    }

    public int getArticleCount() {
        return mArticleCount;
    }

    public int getAvgArticleLength() {
        if(mArticleCount == 0) return 0;
        return mTotalArticleLength / mArticleCount;
    }

    public String getPubcode() {
        return mPubcode;
    }

    public Timestamp getPlannedStartTime() {
        return mPlannedStartTime;
    }

    public void setPlannedStartTime(Timestamp plannedStartTime) {
        mPlannedStartTime = plannedStartTime;
    }

    public Timestamp getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Timestamp startTime) {
        mStartTime = startTime;
        lastRefreshTime=startTime.getTime();
    }

    public Timestamp getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Timestamp endTime) {
        mEndTime = endTime;
    }

    public List<CrawlerEvent> getEvents() {
        return mEvents;
    }

    public List<ArticleEvent> getArticleEvents() {
        return mArticleEvents;
    }

    public List<ListingEvent> getListingEvents() {
        return mListingEvents;
    }

    public List<ListingEvent> getFeedEvents() {
        return mFeedEvents;
    }

    public void reportHttpFailure(Page page, Exception ex) {
    	if(null==worker || !worker.reCrawlOnErr(page)){
    		log.warn("Warning: Fail to get url " + page.getUrl(), ex);
    		CrawlErrorLogger.warn("Warning: Fail to get url " + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
            	mEvents.add(new CrawlerEvent(CrawlerEvent.Type.other, "*Warning: Fail to get url", page.getUrl(), ex));
                writeCoreFile(page);
            }
    	}
    }

    public void reportRetry(String url, int times, Exception ex) {
        log.warn("Warning: Retried " + times + " times getting url" + url, ex);
        CrawlErrorLogger.warn("Warning: Retried " + times + " times getting url" + url);
        if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS)
            mEvents.add(new CrawlerEvent(CrawlerEvent.Type.other, "Warning: Retried " + times + " times getting url", url, ex));
    }

    public void reportFeedsFailure(Page page, FeedConfig feedConfig, ExtractException ex) {
    	if(null==worker || !worker.reCrawlOnErr(page)){
    		log.error(CrawlerEvent.Type.feeds.toString() + " " + page.getUrl(), ex);
    		CrawlErrorLogger.error(CrawlerEvent.Type.feeds.toString() + " " + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
            	mEvents.add(new CrawlerEvent(CrawlerEvent.Type.feeds, "*"+ex.getMessage(), page.getUrl(), ex));
                writeCoreFile(page);
            }
    	}
    }

    public void reportListingFailure(Page page, ListingConfig listingConfig, ExtractException ex) {
    	if(null==worker || !worker.reCrawlOnErr(page)){
    		log.error(CrawlerEvent.Type.listing.toString() + " " + page.getUrl(), ex);
    		CrawlErrorLogger.error(CrawlerEvent.Type.listing.toString() + " " + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
            	mEvents.add(new CrawlerEvent(CrawlerEvent.Type.listing, "*"+ex.getMessage(), page.getUrl(), ex));                
                writeCoreFile(page);
            }
    	}
    }

    public void reportContentPageFailure(Page page, ContentConfig contentConfig, ExtractException ex) {
    	if(null==worker || !worker.reCrawlOnErr(page)){
    		log.error(CrawlerEvent.Type.content.toString() + " " + (page.getMeta(Page.Meta.title)!=null?(page.getMeta(Page.Meta.title)+" "):"") + page.getUrl(), ex);
    		CrawlErrorLogger.error(CrawlerEvent.Type.content.toString() + " " + (page.getMeta(Page.Meta.title)!=null?(page.getMeta(Page.Meta.title)+" "):"") + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
            	mEvents.add(new CrawlerEvent(CrawlerEvent.Type.content, "*"+ex.getMessage(), page.getUrl(), ex));                
                writeCoreFile(page);
            }
    	}
    }
    public void reportJsonListingFailure(Page page, JsonListingConfig listingConfig, ExtractException ex) {
        if(null==worker || !worker.reCrawlOnErr(page)){
            log.error(CrawlerEvent.Type.jsonListing.toString() + " " + page.getUrl(), ex);
            CrawlErrorLogger.error(CrawlerEvent.Type.jsonListing.toString() + " " + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
                mEvents.add(new CrawlerEvent(CrawlerEvent.Type.jsonListing, "*"+ex.getMessage(), page.getUrl(), ex));                
                writeCoreFile(page);
            }
        }
    }
    
    public void reportJsonContentPageFailure(Page page, JsonContentConfig contentConfig, ExtractException ex) {
        if(null==worker || !worker.reCrawlOnErr(page)){
            log.error(CrawlerEvent.Type.jsonContent.toString() + " " + (page.getMeta(Page.Meta.title)!=null?(page.getMeta(Page.Meta.title)+" "):"") + page.getUrl(), ex);
            CrawlErrorLogger.error(CrawlerEvent.Type.jsonContent.toString() + " " + (page.getMeta(Page.Meta.title)!=null?(page.getMeta(Page.Meta.title)+" "):"") + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
                mEvents.add(new CrawlerEvent(CrawlerEvent.Type.jsonContent, "*"+ex.getMessage(), page.getUrl(), ex));                
                writeCoreFile(page);
            }
        }
    }
    
    public void reportGeneralError(String message, Throwable e) {
        log.error(message, e);
        if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS)
            mEvents.add(new CrawlerEvent(CrawlerEvent.Type.other, message + ": " + e.getMessage(), null, e));
    }
    
    public void reportGeneralError(String message, Page page, Throwable e) {
    	if(null==worker || !worker.reCrawlOnErr(page)){
            log.error(message + ": " + page.getUrl(), e);
            CrawlErrorLogger.error(message + ": " + page.getUrl());
            if(Constants.REPORT_MAX_EVENTS<0 || mEvents.size()<Constants.REPORT_MAX_EVENTS){
            	mEvents.add(new CrawlerEvent(CrawlerEvent.Type.other, "*"+message + ": " + e.getMessage(), page.getUrl(), e));
                writeCoreFile(page);
            }
    	}
    }

    public void addTotalNetworkDuration(long value) {
        mTotalNetworkDuration += value;
    }

    public void addTotalExtractionDuration(long value) {
        mTotalExtractionDuration += value;
    }

    public void addListingPageCount() {
        mListingPageCount++;
    }

    public void addFeedPageCount() {
        mFeedPageCount++;
    }

    public void addContentPageCount() {
        mContentPageCount++;
    }

    public void addListingEvent(String author, String section, String url, int allArticles, int newArticles) {
        log.info("listing: " + url + " " + allArticles + "/" + newArticles);
        mListingEvents.add(new ListingEvent(author, section, url, allArticles, newArticles));
    }
    
    public void addFeedsEvent(String author, String section, String url, int allArticles, int newArticles) {
        log.info("feeds: " + url + " " + allArticles + "/" + newArticles);
        mFeedEvents.add(new ListingEvent(author, section, url, allArticles, newArticles));
    }
    
    public void addArticle(Article article) {
        mArticleCount++;
        mTotalArticleLength += article.getContent()==null?0:article.getContent().length();
        
        ArticleEvent articleEvent = new ArticleEvent(article.getAuthor(), article.getSection(), article.getPageno(), article.getTitle(), article.getUrl(), article.getRefererUrl(), article.getFilePath(), article.getCharset());
        mArticleEvents.add(articleEvent);
        mArticleVerifier.verify(article, articleEvent);
        
        log.info("Article: " + article.getTitle() + " " + article.getUrl() + " Length: " + (article.getContent()==null?0:article.getContent().length()) +
                (articleEvent.getMsgs()!=null?(", "+articleEvent.getMsgs()):""));
    }

    public void writeToFile(boolean closeCoreFile) {
//        File dir = new File(Constants.REPORT_DIRECTORY, mProfileName);
//        while(!dir.exists()) dir.mkdirs();
//        SimpleDateFormat fileNameFormat = new SimpleDateFormat(Constants.REPORT_FILE_FORMAT);
//        File file = new File(dir, fileNameFormat.format(getStartTime()) + ".xml");
//        
//        String xml = CrawlerReportXmlBuilder.toXml(this);
//        try {
//            FileUtils.writeStringToFile(file, xml, "UTF-8");
//            
//            if(closeCoreFile){//是否关闭core文件
//            	closeCoreFile();
//            }
//        } catch (IOException e) {
//            log.error(e);
//        }
        

    }
    
    /**
     * 根据report_refresh_time设置，每个指定时间间隔更新报告.
     */
    public void writeToFileByTimeStrategy(){
//    	if(null!=Constants.REPORT_REFRESH_TIME){
//    		long now=new Date().getTime();
//    		if(now-lastRefreshTime > Integer.parseInt(Constants.REPORT_REFRESH_TIME)*1000*60){
//    			setEndTime(new Timestamp(System.currentTimeMillis()));
//    			writeToFile(false);
//    			
//    			lastRefreshTime=now;
//    		}
//    	}
    }
    
    /**
     *打开core文件
     */
    private void openCoreFile(){
//    	try{
//    		
//    		File dir = new File(Constants.REPORT_DIRECTORY, mProfileName);
//        	SimpleDateFormat fileNameFormat = new SimpleDateFormat(Constants.REPORT_FILE_FORMAT);
//        	File file = new File(dir, fileNameFormat.format(getStartTime()) + ".core");
//        	
//        	if(!file.exists()){
//        		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
//        	}
//        	
//        	coreOut = new ObjectOutputStream(new FileOutputStream(file));
//    	}catch(Exception ex){
//    		log.error(ex);
//    	}
    }
    
    /**
     * 关闭core文件
     */
    private void closeCoreFile(){
//    	try{
//    		if(null!=coreOut){
//    			coreOut.flush();    			
//    			coreOut.close();    			
//    			coreOut=null;
//    		}
//    	}catch(Exception ex){
//    		log.error(ex);
//    	}
    }
    
    /**
     * 写core文件
     * @param page
     */
    private void writeCoreFile(Page page){
//    	try{
//    		if(null==coreOut) openCoreFile();
//    		
//    		coreOut.writeObject(page);
//    		coreOut.flush();
//    	}catch(Exception ex){
//    		log.error(ex);
//    	}
    }
    
    /**
     * 读取已经持久化在core文件里的page对象
     * @param file
     * @param from  从第n个开始,n=0为第一个
     * @param end	到第n个结束
     * @return
     */
    public static List<Page> readCoreFile(File file,int from,int end){
    	List<Page> pages=new ArrayList<Page>();
    	
    	try{
    		ObjectInputStream coreIn = new ObjectInputStream(/*new ZipInputStream(*/new FileInputStream(file)/*)*/);
    		Page page=null;
    		
    		//读page对象
    		int pos=0;
    		while((page=(Page)coreIn.readObject())!=null && pos>=from && pos++<end) pages.add(page);
    		
    		coreIn.close();
    	}catch(Exception ex){
    		log.error(ex);
    	}
    	
    	return pages;
    }
}
