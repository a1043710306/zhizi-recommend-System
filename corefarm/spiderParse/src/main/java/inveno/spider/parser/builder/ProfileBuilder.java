package inveno.spider.parser.builder;

import inveno.spider.common.model.Browser;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.base.DateParser;
import inveno.spider.parser.model.ApiConfig;
import inveno.spider.parser.model.ContentConfig;
import inveno.spider.parser.model.FeedConfig;
import inveno.spider.parser.model.ListingConfig;
import inveno.spider.parser.model.ListingPage;
import inveno.spider.parser.model.PostConfig;
import inveno.spider.parser.model.Profile;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ProfileBuilder implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String mType;
    private String profileName;
    private String mPubcode;
    private String mUrl;
    private String mUrlEncoding;
    private String mFromDate;
    private String mOutputCharset;
    private String mListingJavascriptProcess;
    private String mContentJavascriptProcess;
    private String mReplaceWrongBr;
    private String mListingAddDays;
    private String browserType = "MSIE6"; // default is MSIE6

    private int coolDownTime;
    private Integer timeOffset;

    private String cookie;

    private Integer lazyLoadingTime = 2000;// default is 2 seconds.
    private Integer profileSeq = 0;// default 0;
    private String referer;
    
    
    private transient List<ListingPageBuilder> mFeeds = new ArrayList<ListingPageBuilder>();

    private transient List<ListingPageBuilder> mListings = new ArrayList<ListingPageBuilder>();
    private transient FeedConfigBuilder mFeedConfigBuilder = null;
    private transient ListingConfigBuilder mListingConfigBuilder = null;
    private transient ContentConfigBuilder mContentConfigBuilder = null;
    private transient PostConfigBuilder postConfigBuilder = null;
    
    private transient List<ApiConfig> apiConfigs = new ArrayList<ApiConfig>();

    private static final Logger LOG = LoggerFactory.make();
    private static final String date_format = "yyyyMMdd";

    private static ThreadLocal<DateFormat> threadlocal = new ThreadLocal<DateFormat>()
    {
        protected synchronized DateFormat initialValue()
        {
            return new SimpleDateFormat(date_format);
        }
    };

    public List<ApiConfig> getApiConfigs() {
		return apiConfigs;
	}

	public void setApiConfigs(List<ApiConfig> apiConfigs) {
		this.apiConfigs = apiConfigs;
	}

	public static DateFormat getDateFormat()
    {
        return threadlocal.get();
    }

    public void addFeed(ListingPageBuilder b)
    {
        mFeeds.add(b);
    }

    // public ProfileBuilder()
    // {
    // if(coolDownTime==0)
    // {
    // setCoolDownTime(mType.equalsIgnoreCase("forum")?2000:500);
    // }
    // }

    public void addListing(ListingPageBuilder b)
    {
        mListings.add(b);
    }
    
    public void addApiConfig(ApiConfig config)
    {
        apiConfigs.add(config);
    }

    public Profile build(String xml)
    {
        setProfileName(xml.substring(0, xml.indexOf(".")));
        List<ListingPage> feeds = new ArrayList<ListingPage>();
        for (ListingPageBuilder builder : mFeeds)
            feeds.add(builder.build());
        List<ListingPage> listings = new ArrayList<ListingPage>();
        for (ListingPageBuilder builder : mListings)
            listings.add(builder.build());
        
        List<ApiConfig> mApiConfigs = new ArrayList<ApiConfig>();
        for (ApiConfig apiConfig : apiConfigs)
        	mApiConfigs.add(apiConfig);

        FeedConfig feedConfig = mFeedConfigBuilder == null ? null
                : mFeedConfigBuilder.build();
        ListingConfig listingConfig = mListingConfigBuilder == null ? null
                : mListingConfigBuilder.build();
        ContentConfig contentConfig = mContentConfigBuilder == null ? null
                : mContentConfigBuilder.build();
        
        PostConfig postConfig = postConfigBuilder == null ? null
                : postConfigBuilder.build();

        Date fromDate = null;
        try
        {
            if (mFromDate != null)
                fromDate = new SimpleDateFormat("yyyyMMdd").parse(mFromDate);
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
        int listingAddDays = 0;
        if (!StringUtils.isBlank(mListingAddDays))
            listingAddDays = Integer.parseInt(mListingAddDays);

        return new Profile(mType, getProfileName(), mPubcode, mUrl,
                mUrlEncoding, fromDate,
                mOutputCharset,
                "true".equalsIgnoreCase(mListingJavascriptProcess),
                "true".equalsIgnoreCase(mContentJavascriptProcess),
                "true".equalsIgnoreCase(mReplaceWrongBr),
                listingAddDays, feeds,
                listings, feedConfig, listingConfig, contentConfig,postConfig, new Date(),
                "system", Browser.convert(browserType),
                coolDownTime == 0 ? (mType.equalsIgnoreCase("forum") ? 2000
                        : 500) : coolDownTime,
                null == getTimeOffset() ? DateParser.TIME_OFFSET_CHINA
                        : getTimeOffset(), getCookie(), getLazyLoadingTime(),
                getProfileSeq(),"true".equalsIgnoreCase(referer),mApiConfigs);
    }

    public String getBrowserType()
    {
        return browserType;
    }

    public ContentConfigBuilder getContentConfigBuilder()
    {
        return mContentConfigBuilder;
    }

    public String getContentJavascriptProcess()
    {
        return mContentJavascriptProcess;
    }

    public String getCookie()
    {
        return cookie;
    }

    public int getCoolDownTime()
    {
        return coolDownTime;
    }

    public FeedConfigBuilder getFeedConfigBuilder()
    {
        return mFeedConfigBuilder;
    }

    public List<ListingPageBuilder> getFeeds()
    {
        return mFeeds;
    }

    public String getFromDate()
    {
        return mFromDate;
    }

    public Integer getLazyLoadingTime()
    {
        return lazyLoadingTime;
    }

    public String getListingAddDays()
    {
        return mListingAddDays;
    }

    public ListingConfigBuilder getListingConfigBuilder()
    {
        return mListingConfigBuilder;
    }

    public String getListingJavascriptProcess()
    {
        return mListingJavascriptProcess;
    }

    public List<ListingPageBuilder> getListings()
    {
        return mListings;
    }

    public String getOutputCharset()
    {
        return mOutputCharset;
    }

    public String getProfileName()
    {
        return profileName;
    }

    public Integer getProfileSeq()
    {
        return profileSeq;
    }

    public String getPubcode()
    {
        return mPubcode;
    }

    public String getReplaceWrongBr()
    {
        return mReplaceWrongBr;
    }

    public Integer getTimeOffset()
    {
        return timeOffset;
    }

    public String getType()
    {
        return mType;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public String getUrlEncoding()
    {
        return mUrlEncoding;
    }

    public void setBrowserType(String browserType)
    {
        this.browserType = browserType;
    }

    public void setContentConfigBuilder(
            ContentConfigBuilder contentConfigBuilder)
    {
        mContentConfigBuilder = contentConfigBuilder;
    }

    public void setContentJavascriptProcess(String contentJavascriptProcess)
    {
        mContentJavascriptProcess = contentJavascriptProcess;
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }

    public void setCoolDownTime(int coolDownTime)
    {
        this.coolDownTime = coolDownTime;
    }

    public void setFeedConfigBuilder(FeedConfigBuilder feedConfigBuilder)
    {
        mFeedConfigBuilder = feedConfigBuilder;
    }

    public void setFromDate(String fromDate)
    {
        mFromDate = fromDate;
    }

    public void setLazyLoadingTime(Integer lazyLoadingTime)
    {
        this.lazyLoadingTime = lazyLoadingTime;
    }

    public void setListingAddDays(String listingAddDays)
    {
        mListingAddDays = listingAddDays;
    }

    public void setListingConfigBuilder(
            ListingConfigBuilder listingConfigBuilder)
    {
        mListingConfigBuilder = listingConfigBuilder;
    }

    public void setListingJavascriptProcess(String listingJavascriptProcess)
    {
        mListingJavascriptProcess = listingJavascriptProcess;
    }

    public void setOutputCharset(String outputCharset)
    {
        mOutputCharset = outputCharset;
    }

    public void setProfileName(String profileName)
    {
        this.profileName = profileName;
    }

    public void setProfileSeq(Integer profileSeq)
    {
        this.profileSeq = profileSeq;
    }

    public void setPubcode(String pubcode)
    {
        mPubcode = pubcode;
    }

    public void setReplaceWrongBr(String replaceWrongBr)
    {
        mReplaceWrongBr = replaceWrongBr;
    }

    public void setTimeOffset(Integer timeOffset)
    {
        this.timeOffset = timeOffset;
    }

    public void setType(String type)
    {
        mType = type;
    }

    public void setUrl(String url)
    {
        mUrl = url;
    }

    public void setUrlEncoding(String urlEncoding)
    {
        mUrlEncoding = urlEncoding;
    }

    public String getReferer()
    {
        return referer;
    }

    public void setReferer(String referer)
    {
        this.referer = referer;
    }

    public PostConfigBuilder getPostConfigBuilder()
    {
        return postConfigBuilder;
    }

    public void setPostConfigBuilder(PostConfigBuilder postConfigBuilder)
    {
        this.postConfigBuilder = postConfigBuilder;
    }
}
