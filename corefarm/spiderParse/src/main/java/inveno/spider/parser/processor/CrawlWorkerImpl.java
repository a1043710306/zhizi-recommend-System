package inveno.spider.parser.processor;

import inveno.spider.common.model.GetResult;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.base.CrawlQueue;
import inveno.spider.parser.base.CrawlQueueImpl;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.base.ParseStrategyImpl;
import inveno.spider.parser.extractor.Extractor;
import inveno.spider.parser.extractor.Extractors;
import inveno.spider.parser.extractor.ExtractorsSuit;
import inveno.spider.parser.extractor.Extractor.Type;
import inveno.spider.parser.model.ListingPage;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.ArticleStore;
import inveno.spider.parser.store.ArticleStoreImpl;
import inveno.spider.parser.utils.Utils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class CrawlWorkerImpl extends CrawlerWorkerAdaptor
{
    private static final Logger LOG = LoggerFactory.make();

    private Profile mProfile;
    private CrawlerReport mCrawlerReport;
    private ParseStrategy mParseStrategy;
    private CrawlQueue mCrawlQueue;
    private Extractors mExtractors;
    private ArticleStore mArticleStore;
    private ProgressHelper mProgressHelper;
    private Progress mProgress;

    private Page page;

    /**
     * Load profile from cache.
     * 
     * @param profileName
     */
    public CrawlWorkerImpl(Profile profile, Page page)
    {
        mProgressHelper = new ProgressHelper();
        mProgress = new Progress();

        mProfile = profile;
        this.page = page;
    }
    
    public CrawlWorkerImpl(Profile profile,String pubCode)
    {
        mProgressHelper = new ProgressHelper();
        mProgress = new Progress();

        mProfile = profile;
        this.page = null;
    }

    /**
     * Load profile from local xml.
     * 
     * @param profile
     */
    public CrawlWorkerImpl(Profile profile)
    {
        mProgressHelper = new ProgressHelper();
        mProgress = new Progress();
        mProfile = profile;
    }

    private void init()
    {
        mCrawlerReport = new CrawlerReport(getPubCode(), getProfileName());
        mCrawlerReport.setPlannedStartTime(new Timestamp(null == this
                .getPlannedStartTime() ? System.currentTimeMillis() : this
                .getPlannedStartTime().getTime()));

        mParseStrategy = new ParseStrategyImpl(true/* date can null */, mProfile.getTimeOffset());
        mCrawlQueue = new CrawlQueueImpl(this.mProfile);

        mArticleStore = new ArticleStoreImpl(getPubCode());

        mExtractors = new Extractors(mProfile, mArticleStore, mCrawlerReport, mParseStrategy);
    }

    public Profile getProfile()
    {
        return mProfile;
    }

    public void addProgressListener(ProgressListener listener)
    {
        mProgressHelper.addProgressListener(listener);
    }

    public Progress getProgress()
    {
        return mProgress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlerWorker#execute()
     */
    public void execute()
    {
        try
        {
            MDC.put("pubCode", getPubCode());

            init();
            afterInit();

            long tmpTimestamp;
            LOG.info("Start crawling " + getProfileName());
            mCrawlerReport.setStartTime(new Timestamp(System.currentTimeMillis()));

            mProgressHelper.updateProgress(this, mProgress.copy());

            long lastQueryTime = 0;

            if (page != null)
            {

                mProgress.addCurrent(1);

                try
                {
                    tmpTimestamp = System.currentTimeMillis();

                    Extractor extractor = mExtractors.getExtractor(page.getType());

                    GetResult result = null;
                    result = this.page.getResult();
                    lastQueryTime = result.getEnd();

                    Exception ex = result.getException();
                    final String exceptionmessage = (null != ex ? (null != ex.getMessage() ? ex.getMessage() : ex.getCause().getMessage()) : "");

                    if (reportTimeout(result.getDuration(), (null == ex) ? "Too much time cost to get this url." : exceptionmessage, ex))
                    {
                        String message = "Get url(" + page.getUrl() + ") time out, " + result.getDuration() + "ms";
                        LOG.info(message);
                    }
                    if (ex != null)
                    {
                        LOG.info(exceptionmessage);
                    }
                    mCrawlerReport.addTotalNetworkDuration(result.getDuration());

                    // extract info
                    LOG.info("extractor :" + extractor.getClass().getCanonicalName());
                    if (result.isSuccess() && page.getHtml().getHtml() != null)
                    {
                        List<Page> newPages = extractor.extract(page);
                        mCrawlQueue.submitAll(newPages);
                        mProgress.addTotal(newPages.size());

                    }

                    long totalDuration = System.currentTimeMillis() - tmpTimestamp;
                    mCrawlerReport.addTotalExtractionDuration(totalDuration - result.getDuration());

                }
                catch (Exception ex)
                {
                    LOG.error("", ex);
                    mCrawlerReport.reportGeneralError("CrawlWorkerImpl.execute", ex);
                }
                finally
                {
                    mProgressHelper.updateProgress(this, mProgress.copy());
                }
            }

            int count = mCrawlerReport.getArticleCount();
            if (count > 0)
            {
                LOG.info(count + " new article's parse Log written for " + getProfileName());
            }
            else
            {
                LOG.info("No new articles for " + getProfileName());
            }

        }
        catch (Throwable e)
        {
            LOG.error("", e);
            mCrawlerReport.reportGeneralError("CrawlWorkerImpl.execute", e);
        }
        finally
        {
            try
            {
                mArticleStore.close();
            }
            catch (Throwable e)
            {
                LOG.error("", e);
                mCrawlerReport.reportGeneralError("CrawlWorkerImpl.execute", e);
            }

            // write report
            mCrawlerReport.setEndTime(new Timestamp(System.currentTimeMillis()));
            mCrawlerReport.writeToFile(true);

            double duration = (mCrawlerReport.getEndTime().getTime() - mCrawlerReport.getStartTime().getTime()) / 1000.0;
            LOG.info("Finish " + getPubCode() + " in " + duration + " sec");

            mProgressHelper.updateProgress(this, mProgress.copy());
            MDC.remove("pubCode");
        }
    }

    private void addPageCount(Type type)
    {
        switch (type)
        {
        case Listing:
            mCrawlerReport.addListingPageCount();
            break;
        case Feed:
            mCrawlerReport.addFeedPageCount();
            break;
        case Content:
            mCrawlerReport.addContentPageCount();
        }
    }

    public CrawlQueue getCrawlQueue()
    {
        return this.mCrawlQueue;
    }

    public ExtractorsSuit getExtractorsSuit()
    {
        return mExtractors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getPubType()
     */
    public String getPubType()
    {
        return this.mProfile.getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getProfileName()
     */
    public String getProfileName()
    {
        return mProfile.getProfileName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getLastUpdateTime()
     */
    public Date getLastUpdateTime()
    {
        return mProfile.getLastUpdateTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getUpdateUser()
     */
    public String getUpdateUser()
    {
        return mProfile.getUpdateUser();
    }

    public String getPubCode()
    {
        return mProfile.getPubCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getCoolDownTime()
     */
    @Override
    public int getCoolDownTime()
    {
        return mProfile.getCoolDownTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see wisers.crawler.batch.processor.CrawlWorker#getLazyLoadingTime()
     */
    @Override
    public int getLazyLoadingTime()
    {
        return mProfile.getLazyLoadingTime();
    }

}
