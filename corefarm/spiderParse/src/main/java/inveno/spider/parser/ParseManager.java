package inveno.spider.parser;


import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.processor.CrawlWorker;
import inveno.spider.parser.processor.Progress;
import inveno.spider.parser.processor.ProgressListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class ParseManager implements ProgressListener {
    private static final Logger log = LoggerFactory.make();
    
    /* have to use this because there's no good way of passing this into quartz job */
    private static ParseManager instance = new ParseManager();
    public static ParseManager getInstance() {
        return instance;
    }
    
    private ParseManager(){
        new Runner().start();
    }
    private Set<String> mRunningPubcodes = Collections.synchronizedSet(new HashSet<String>());
    private List<CrawlWorker> mRunningJobs = Collections.synchronizedList(new LinkedList<CrawlWorker>());

    private AtomicBoolean isProgressUpdated = new AtomicBoolean(false);

    private ExecutorService mExecutor = Executors.newFixedThreadPool(Constants.PARSER_CONCURRENT_JOBS);
    private Semaphore mAvailableSlots = new Semaphore(Constants.PARSER_CONCURRENT_JOBS);
    private Map<String, Integer> mProfileNameWaitingMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    private List<CrawlWorker> mWorkerWaitingQueue = Collections.synchronizedList(new LinkedList<CrawlWorker>());
    private IdentityHashMap<CrawlWorker, Date> mWorkerStartTimeMap = new IdentityHashMap<CrawlWorker, Date>();

    class Runner extends Thread {
        @Override public void run() {
            while(true) {
                try {
                    mAvailableSlots.acquire();  // ensure there's available slot to execute new job

                    while(mWorkerWaitingQueue.isEmpty()) Thread.sleep(1000);    // wait until has next job

                    CrawlWorker worker = takeNextNotRunningWorker();    // wait until has next runnable job
                    while(worker==null) {
                        Thread.sleep(1000);
                        worker = takeNextNotRunningWorker();
                    }
                    
                    final CrawlWorker w = worker;

                    mExecutor.submit(new Runnable() {
                        public void run() {
                            try {
                                mRunningJobs.add(w);
                                w.addProgressListener(ParseManager.this);
                                w.execute();
                            } catch (Exception e) {
                                log.error("", e);
                            } finally {
                                mRunningJobs.remove(w);
                                mRunningPubcodes.remove(w.getPubCode());
                                mAvailableSlots.release();
                            }
                        }
                    });

                } catch (Throwable e) {
                    log.error("", e);
                }
            }
        }
    }
    
    private CrawlWorker takeNextNotRunningWorker() {
        synchronized (mWorkerWaitingQueue) {
            CrawlWorker result = null;
            for (CrawlWorker worker : mWorkerWaitingQueue) {
                if(!mRunningPubcodes.contains(worker.getPubCode())) {
                    result = worker; break;
                }
            }
            if(result!=null) {
                mWorkerWaitingQueue.remove(result);
                decreaseWaitingNumbers(result.getProfileName());
                mWorkerStartTimeMap.remove(result);
                mRunningPubcodes.add(result.getPubCode());
            }
            return result;
        }
    }
    
    /**
     * Ensure worker with the same name do not execute concurrently.
     * @param crawlWorker
     */
    public boolean submitJob(CrawlWorker crawlWorker) {
        synchronized(mWorkerWaitingQueue) {
        	int number = getWaitingNumbers(crawlWorker.getProfileName());
        	if(number>=Constants.PARSER_PROFILE_CONCURRENT_WAITING_NUMBERS) return false;
        	
        	Date plannedStartTime = new Date();
        	crawlWorker.setPlannedStartTime(plannedStartTime);
            mWorkerWaitingQueue.add(crawlWorker);
            increaseWaitingNumbers(crawlWorker.getProfileName());
            mWorkerStartTimeMap.put(crawlWorker, plannedStartTime);
            
            return true;
        }
    }
    
    public boolean removeJob(String profileName)
    {
        synchronized(mWorkerWaitingQueue) {
            for(CrawlWorker crawlWorker:mWorkerWaitingQueue)
            {
                if(profileName.equalsIgnoreCase(crawlWorker.getProfileName()))
                {
                    mWorkerWaitingQueue.remove(crawlWorker);
                    mWorkerStartTimeMap.remove(crawlWorker);
                    decreaseWaitingNumbers(profileName);
                    return true;
                }
            }
            return false;
        }
        
    }
    
    private int getWaitingNumbers(String profileName){
    	Integer itg = mProfileNameWaitingMap.get(profileName);
    	if(itg==null) return 0;
    	return itg.intValue();
    }
    private void increaseWaitingNumbers(String profileName){
    	if(mProfileNameWaitingMap.containsKey(profileName)){
    		int num = mProfileNameWaitingMap.get(profileName).intValue();
    		mProfileNameWaitingMap.put(profileName, ++num);
    	} else {
    		mProfileNameWaitingMap.put(profileName, 1);
    	}
    }
    private void decreaseWaitingNumbers(String profileName){
    	if(mProfileNameWaitingMap.containsKey(profileName)){
    		int num = mProfileNameWaitingMap.get(profileName).intValue();
    		mProfileNameWaitingMap.put(profileName, --num);
    	}
    }

    /**
     * Get the list of current running workers.
     * @return
     */
    public List<CrawlWorker> getRunningWorkers() {
        synchronized (mRunningJobs) {
            return new ArrayList<CrawlWorker>(mRunningJobs);
        }
    }
    
    /**
     * Get the list of current waiting workers.
     * @return
     */
    public List<CrawlWorker> getWaitingWorkers() {
        synchronized (mWorkerWaitingQueue) {
            return new ArrayList<CrawlWorker>(mWorkerWaitingQueue);
        }
    }

    public boolean isRunning(String pubcode) {
        return mRunningPubcodes.contains(pubcode);
    }

    public void updateProgress(CrawlWorker worker, Progress progress) {
        isProgressUpdated.set(true);
    }

    public boolean isProgressUpdated() {
        return isProgressUpdated.get();
    }

    public void clearUpdateFlag() {
        isProgressUpdated.set(false);
    }

    public int getNumJobsWaiting() {
        return mWorkerWaitingQueue.size();
    }
    
    public List<String> getAllJobsWaiting() {
        List<String> results = new ArrayList<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        synchronized (mWorkerWaitingQueue) {
            for (CrawlWorker worker : mWorkerWaitingQueue) {
                results.add(worker.getProfileName() + " waiting since " + df.format(mWorkerStartTimeMap.get(worker)));
            }
        }
        return results;
    }
    
    public String getAllWaitingJobs()
    {
        /*css base on bootcss.com
         * */
        
        StringBuilder sb = new StringBuilder();
        sb.append("<ul class='list-group'>");
        synchronized (mProfileNameWaitingMap)
        {
            for (Entry<String, Integer> entry : mProfileNameWaitingMap
                    .entrySet())
            {
                int count = entry.getValue();
                String profileName = entry.getKey();
                if(count>0)
                {
                sb.append("<li class='list-group-item'>").append("<span class='badge'>").append(count)
                        .append("</span>").append(profileName)
                        .append("</li>");
                }
            }
        }
        sb.append("</ul>");
        
        return sb.toString();
    }
    
    public String getRunningJobs()
    {
        Set<String> tmp = new HashSet<String>(mRunningPubcodes);
        StringBuilder sb = new StringBuilder();
        sb.append("<ul class='list-group'>");
            for (String profileName :tmp)
            {
                sb.append("<li class='list-group-item'>").append("<span class='badge'>").append(1)
                        .append("</span>").append(profileName)
                        .append("</li>");
            }
        sb.append("</ul>");
        
        return sb.toString();
    }    
}
