package inveno.spider.parser.model;

import inveno.spider.common.Constants;
import inveno.spider.common.RedisHelper;
import inveno.spider.common.model.Browser;
import inveno.spider.parser.builder.XmlProfileReader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;


public class Profile implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static Profile readFromCache(String profileName)
    {
        String key=profileName;
        Profile profile = RedisHelper.getInstance().getObject(key);
        return profile;
    }
    
    public static Profile readFromFile(String profileName)
    {
        try
        {
            File file = new File(Constants.PROFILE_PATH, profileName + ".xml");
            //File file = new File(profileName);

            if (!file.exists())
                return null;
            Profile profile = XmlProfileReader.parse(FileUtils
                    .readFileToString(file, "UTF-8"));
            String temp = StringUtils.remove(file.getName(), ".xml"); 
            profile.setProfileName(temp);

            try
            {
                String seq = temp.replaceAll(profile.getPubCode(), "")
                        .replaceAll("_", "");
                if (seq.length() > 0)
                {
                    profile.setProfileSeq(Integer.parseInt(seq));
                }
            } catch (Exception ex)
            {
            }

            return profile;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String mType; // blog or forum
    private String mPubcode;
    private String mUrl;
    private String mUrlEncoding;
    private Date mFromDate;
    private String mOutputCharset;
    private boolean mListingJavascriptProcess;
    private boolean mContentJavascriptProcess;
    private boolean mReplaceWrongBr;
    private int mListingAddDays;
    private String profileName;
    private Browser.Type browserType;
    private int coolDownTime;
    private boolean referer;
    private Date lastUpdateTime;
    private String updateUser;

    // either one or both
    private List<ListingPage> mFeeds;

    private List<ListingPage> mListings;
    
    private List<ListingPage> mApis;

    // optional
    private FeedConfig mFeedConfig;

    private ListingConfig mListingConfig;

    private ContentConfig mContentConfig;
    private PostConfig postConfig;
    private int profileSeq;
    
    /**
     * The time offset for web site,
     * Default value is zero.<br/>
     */
    private int timeOffset;
    private String cookie;
    
    private int lazyLoadingTime;
    
    private List<ApiConfig> apiConfigs;
    
    public List<ApiConfig> getApiConfigs() {
		return apiConfigs;
	}

	public void setApiConfigs(List<ApiConfig> apiConfigs) {
		this.apiConfigs = apiConfigs;
	}

	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	public Profile()
    {
    }
    public Profile(String type,String profileName, String pubcode, String url, String urlEncoding,
            Date fromDate, String outputCharset,
            boolean listingJavascriptProcess, boolean contentJavascriptProcess,
            boolean replaceWrongBr,
            int listingAddDays, List<ListingPage> feeds,
            List<ListingPage> listings, FeedConfig feedConfig,
            ListingConfig listingConfig, ContentConfig contentConfig,PostConfig postConfig,
            Date lastUpdateTime,String updateUser,
            Browser.Type browserType,int coolDownTime,int timeOffset,String cookie,
            final int lazyLoadingTime,final int profileSeq,boolean referer)
    {
        mType = type;
        setProfileName(profileName);
        mPubcode = pubcode;
        mUrl = url;
        mUrlEncoding = urlEncoding;
        mFromDate = fromDate;
        mOutputCharset = outputCharset;
        mListingJavascriptProcess = listingJavascriptProcess;
        mContentJavascriptProcess = contentJavascriptProcess;
        mReplaceWrongBr = replaceWrongBr;
        mListingAddDays = listingAddDays;
        mFeeds = feeds;
        mListings = listings;
        mFeedConfig = feedConfig;
        mListingConfig = listingConfig;
        mContentConfig = contentConfig;
        this.postConfig=postConfig; 
        setLastUpdateTime(lastUpdateTime);
        setUpdateUser(updateUser);
        
        setBrowserType(browserType);
        setCoolDownTime(coolDownTime);
        setTimeOffset(timeOffset);
        setCookie(cookie);
        this.lazyLoadingTime = lazyLoadingTime;
        this.profileSeq = profileSeq;
        setReferer(referer);
    }
    public Profile(String type,String profileName, String pubcode, String url, String urlEncoding,
            Date fromDate, String outputCharset,
            boolean listingJavascriptProcess, boolean contentJavascriptProcess,
            boolean replaceWrongBr,
            int listingAddDays, List<ListingPage> feeds,
            List<ListingPage> listings, FeedConfig feedConfig,
            ListingConfig listingConfig, ContentConfig contentConfig,PostConfig postConfig,
            Date lastUpdateTime,String updateUser,
            Browser.Type browserType,int coolDownTime,int timeOffset,String cookie,
            final int lazyLoadingTime,final int profileSeq,boolean referer,List<ApiConfig> apiConfigs)
    {
        mType = type;
        setProfileName(profileName);
        mPubcode = pubcode;
        mUrl = url;
        mUrlEncoding = urlEncoding;
        mFromDate = fromDate;
        mOutputCharset = outputCharset;
        mListingJavascriptProcess = listingJavascriptProcess;
        mContentJavascriptProcess = contentJavascriptProcess;
        mReplaceWrongBr = replaceWrongBr;
        mListingAddDays = listingAddDays;
        mFeeds = feeds;
        mListings = listings;
        mFeedConfig = feedConfig;
        mListingConfig = listingConfig;
        mContentConfig = contentConfig;
        this.apiConfigs = apiConfigs;
        this.postConfig=postConfig; 
        setLastUpdateTime(lastUpdateTime);
        setUpdateUser(updateUser);
        
        setBrowserType(browserType);
        setCoolDownTime(coolDownTime);
        setTimeOffset(timeOffset);
        setCookie(cookie);
        this.lazyLoadingTime = lazyLoadingTime;
        this.profileSeq = profileSeq;
        setReferer(referer);
    }
    public ContentConfig getContentConfig()
    {
        return mContentConfig;
    }

    public FeedConfig getFeedConfig()
    {
        return mFeedConfig;
    }

    public List<ListingPage> getFeeds()
    {
        return mFeeds;
    }
     
    public Date getFromDate()
    {
        return mFromDate;
    }

    public Date getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    public int getListingAddDays()
    {
        return mListingAddDays;
    }

    public ListingConfig getListingConfig()
    {
        return mListingConfig;
    }

    public List<ListingPage> getListings()
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

    public int getProfileSeq()
    {
        return profileSeq;
    }

    public String getPubCode()
    {
        return mPubcode;
    }

    public String getType()
    {
        return mType;
    }

    public String getUpdateUser()
    {
        return updateUser;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public String getUrlEncoding()
    {
        return mUrlEncoding;
    }

    public boolean isContentJavascriptProcess()
    {
        return mContentJavascriptProcess;
    }

    public boolean isListingJavascriptProcess()
    {
        return mListingJavascriptProcess;
    }


    public boolean isReplaceWrongBr()
    {
        return mReplaceWrongBr;
    }

    public void setContentConfig(ContentConfig mContentConfig)
    {
        this.mContentConfig = mContentConfig;
    }

    public void setContentJavascriptProcess(boolean contentJavascriptProcess)
    {
        mContentJavascriptProcess = contentJavascriptProcess;
    }

    public void setFeedConfig(FeedConfig mFeedConfig)
    {
        this.mFeedConfig = mFeedConfig;
    }

    public void setFeeds(List<ListingPage> mFeeds)
    {
        this.mFeeds = mFeeds;
    }
    public void setFromDate(Date fromDate)
    {
        mFromDate = fromDate;
    }

    public void setLastUpdateTime(Date lastUpdateTime)
    {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setListingAddDays(int listingAddDays)
    {
        mListingAddDays = listingAddDays;
    }

    public void setListingConfig(ListingConfig mListingConfig)
    {
        this.mListingConfig = mListingConfig;
    }

    public void setListingJavascriptProcess(boolean listingJavascriptProcess)
    {
        mListingJavascriptProcess = listingJavascriptProcess;
    }

    public void setListings(List<ListingPage> mListings)
    {
        this.mListings = mListings;
    }


    public void setOutputCharset(String mOutputCharset)
    {
        this.mOutputCharset = mOutputCharset;
    }

    public void setProfileName(String profileName)
    {
        this.profileName = profileName;
    }

    public void setProfileSeq(int profileSeq)
    {
        this.profileSeq = profileSeq;
    }

    public void setPubcode(String mPubcode)
    {
        this.mPubcode = mPubcode;
    }

    public void setReplaceWrongBr(boolean replaceWrongBr)
    {
        mReplaceWrongBr = replaceWrongBr;
    }

    public void setType(String mType)
    {
        this.mType = mType;
    }

    public void setUpdateUser(String updateUser)
    {
        this.updateUser = updateUser;
    }

    public void setUrl(String mUrl)
    {
        this.mUrl = mUrl;
    }

    public void setUrlEncoding(String mUrlEncoding)
    {
        this.mUrlEncoding = mUrlEncoding;
    }
    public Browser.Type getBrowserType()
    {
        return browserType;
    }
    public void setBrowserType(Browser.Type browserType)
    {
        this.browserType = browserType;
    }
    public int getCoolDownTime()
    {
        return coolDownTime;
    }
    public void setCoolDownTime(int coolDownTime)
    {
        this.coolDownTime = coolDownTime;
    }
    public int getTimeOffset()
    {
        return timeOffset;
    }
    public void setTimeOffset(int timeOffset)
    {
        this.timeOffset = timeOffset;
    }
    
    public int getLazyLoadingTime()
    {
        return lazyLoadingTime;
    }

    public boolean isReferer()
    {
        return referer;
    }

    public void setReferer(boolean referer)
    {
        this.referer = referer;
    }

    public PostConfig getPostConfig()
    {
        return postConfig;
    }

    public void setPostConfig(PostConfig postConfig)
    {
        this.postConfig = postConfig;
    }

	public void setApis(List<ListingPage> apis) {
		this.mApis = apis;
	}

	public List<ListingPage> getmApis() {
		return mApis;
	}

	public void setmApis(List<ListingPage> mApis) {
		this.mApis = mApis;
	}
    
}
