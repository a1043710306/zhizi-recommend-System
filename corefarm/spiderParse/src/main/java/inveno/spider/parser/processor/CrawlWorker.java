package inveno.spider.parser.processor;

import java.util.Date;

public interface CrawlWorker {

    String getPubCode();
    
    String getProfileName();
    
    Date getLastUpdateTime();
    
    String getUpdateUser();

    void execute();

    Progress getProgress();

    void addProgressListener(ProgressListener listener);
    
    
    String getPubType();
    
    /**
     * Get cool-down time from profile.
     * @return
     */
    int getCoolDownTime();

    /**
     * Added to the job queue,set planned start time.
     */
    void setPlannedStartTime(Date plannedStartTime);
    
    /**
     * If enable Java script parse function(listingJavascriptProcess or contentJavascriptProcess is true),then use it.
     * @return
     * Delay time,the unit is millisecond(default is 2000ms).
     */
    int getLazyLoadingTime();

}