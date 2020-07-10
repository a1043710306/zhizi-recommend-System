package inveno.spider.parser.store;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Article implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String mUrl;
    private Date mDate;
    private String mTitle;
    private String summary;
    private String mContent;
    private String mAuthor;
    private String mSection;
    private String mPageno;
    private String mRefererUrl; // for report only
    private String mFilePath;   // for report only
    private String mCharset;   // for report only
    
    private String pubCode;
    private String profileName;
    
    private String rssId;
    private String typeCode;
    private String source;
    
    private long batchId;
    
    private Date createTime;
    
    private Map<String,String> images;
    
    private int level;//the same to table s_rss(info_level)
    
    private String tags;
    private String location;
    private int isOpenComment;
    private int dataType;
    private int infoType;
    
    private String categoryName;
    private int checkCategoryFlag;
    private String fallImage;

    /**
     * add for video duration.
     */
    private int second;

    public int getSecond()
    {
        return second;
    }

    public void setSecond(int _second)
    {
        second = _second;
    }

    /**
     * 设定是否由清洗程序下载图片
     * 0 - 下载图片, 1 - 不下载图片
     */
    private int imgFlag = -1;

    public int getImgFlag()
    {
        return imgFlag;
    }

    public void setImgFlag(int imgFlag)
    {
        this.imgFlag = imgFlag;
    }

    public Article()
    {
        images = new HashMap<String,String>(); 
    }
    
    public Map<String,String> getImages()
    {
        return this.images;
    }
    
    public void putImage(String originUrl,String localUrl)
    {
        images.put(originUrl, localUrl);
    }    
    
    public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public Article(String url, Date date, String title,String summary, String content, String author, String section, String pageno, String refererUrl) {
        this();
        mUrl = url;
        mDate = date;
        mTitle = title;
        this.setSummary(summary);
        mContent = content;
        mAuthor = author;
        mSection = section;
        mPageno = pageno;
        mRefererUrl = refererUrl;
        mFilePath = null;
        mCharset = null;
        
        createTime=new Date();
    }
    public final String getUrl() {
        return mUrl;
    }
    public final Date getDate() {
        return mDate;
    }
    public final String getTitle() {
        return mTitle;
    }
    public final String getContent() {
        return mContent;
    }
    public final String getAuthor() {
        return mAuthor;
    }
    public final String getSection() {
        return mSection;
    }
    public String getPageno() {
		return mPageno;
	}
	public String getRefererUrl() {
        return mRefererUrl;
    }
    public String getFilePath() {
        return mFilePath;
    }
    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }
    
    public String getCharset() {
		return mCharset;
	}
	public void setCharset(String charset) {
		mCharset = charset;
	}
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("url:\t").append(mUrl).append("\n");
        if(mDate!=null)
            sb.append("date:\t").append(new SimpleDateFormat("yyyy-MM-dd").format(mDate)).append("\n");
        else
            sb.append("date:\tnull\n");
        sb.append("title:\t").append(mTitle).append("\n");
        sb.append("author:\t").append(mAuthor).append("\n");
        sb.append("section:\t").append(mSection).append("\n");
        sb.append("pageno:\t").append(mPageno).append("\n");
        sb.append("content:\n").append(mContent).append("\n");
        return sb.toString();
    }
    /**
     * @return the pubCode
     */
    public String getPubCode()
    {
        return pubCode;
    }
    /**
     * @param pubcode the pubCode to set
     */
    public void setPubCode(String pubCode)
    {
        this.pubCode = pubCode;
    }
    public Date getCreateTime()
    {
        return createTime;
    }
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getProfileName()
    {
        return profileName;
    }

    public void setProfileName(String profileName)
    {
        this.profileName = profileName;
    }

    public String getRssId()
    {
        return rssId;
    }

    public void setRssId(String rssId)
    {
        this.rssId = rssId;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public int getIsOpenComment()
    {
        return isOpenComment;
    }

    public void setIsOpenComment(int isOpenComment)
    {
        this.isOpenComment = isOpenComment;
    }

    public int getDataType()
    {
        return dataType;
    }

    public void setDataType(int dataType)
    {
        this.dataType = dataType;
    }

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public int getCheckCategoryFlag() {
		return checkCategoryFlag;
	}

	public void setCheckCategoryFlag(int checkCategoryFlag) {
		this.checkCategoryFlag = checkCategoryFlag;
	}

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public Date getmDate() {
		return mDate;
	}

	public void setmDate(Date mDate) {
		this.mDate = mDate;
	}

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getmContent() {
		return mContent;
	}

	public void setmContent(String mContent) {
		this.mContent = mContent;
	}

	public String getmAuthor() {
		return mAuthor;
	}

	public void setmAuthor(String mAuthor) {
		this.mAuthor = mAuthor;
	}

	public String getmSection() {
		return mSection;
	}

	public void setmSection(String mSection) {
		this.mSection = mSection;
	}

	public String getmPageno() {
		return mPageno;
	}

	public void setmPageno(String mPageno) {
		this.mPageno = mPageno;
	}

	public String getmRefererUrl() {
		return mRefererUrl;
	}

	public void setmRefererUrl(String mRefererUrl) {
		this.mRefererUrl = mRefererUrl;
	}

	public String getmFilePath() {
		return mFilePath;
	}

	public void setmFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public String getmCharset() {
		return mCharset;
	}

	public void setmCharset(String mCharset) {
		this.mCharset = mCharset;
	}

	public void setImages(Map<String, String> images) {
		this.images = images;
	}

    public String getFallImage()
    {
        return fallImage;
    }
    public void setFallImage(String fallImage)
    {
        this.fallImage = fallImage;
    }
}
