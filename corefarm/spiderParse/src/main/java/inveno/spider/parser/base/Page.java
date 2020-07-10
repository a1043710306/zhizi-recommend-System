package inveno.spider.parser.base;


import inveno.spider.common.model.GetResult;
import inveno.spider.parser.extractor.Extractor;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Page implements Serializable {
	public static enum Meta {
		section, pageno, author, date, title,summary, content, pageOneUrl, refererUrl, postUrl, urlId, reply, click, errorTimes,
		tags,location,isOpenComment,dataType,infoType,categoryName,checkCategoryFlag;
		
		public static Meta convert(String key)
		{
		   Meta[] all = Meta.values();
		   for(int i=0,count=all.length;i<count;i++)
		   {
		       if(key.equalsIgnoreCase(all[i].name()))
		       {
		           return all[i];
		       }
		   }
		   return null;
		}
	}

	private static final long serialVersionUID = 2L;

	private String mUrl;
	private Extractor.Type mType;
	private int mPageNum;
	private Map<Meta, Object> mMeta;
	private Html mHtml;
	private String mCharset;
	private String mBaseUrl;
	private String ancestorUrl;
	private int id;
	private Date focusDate;
	private String sectionAlias;
	private int mPageNo;
	
	private String profileName;
	private String pubCode;
	private Map<String,String> images;
	private GetResult result;
	
	private String rssId;
	private String source;
	private String typeCode;
	private int level;//the same to table s_rss(info_level)
	

    /**
	 *调度批次,同一批次调度的ID相同,以便于后期的统计。 
	 */
	private long batchId;

	public int getLevel()
	{
	    return level;
	}
	
	public void setLevel(int level)
	{
	    this.level = level;
	}
    public Page()
	{
	    images = new HashMap<String,String>();  
	    mMeta = new HashMap<Meta, Object>();
	}

    public Page(int id, String url, Extractor.Type type, int pageNum,
			String accessUrl) {
	    this();
		if (pageNum <= 0)
			throw new IllegalArgumentException("Page num must be >= 1");
		this.id = id;
		mUrl = url;
		mType = type;
		mPageNum = pageNum;
		mMeta = new HashMap<Meta, Object>();
		mHtml = null;
		mCharset = null;
		mBaseUrl = null;
		ancestorUrl = accessUrl;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);

		mMeta.put(Page.Meta.errorTimes, 0);
	}

    public Page(int id, String url, Extractor.Type type, int pageNum,
			String accessUrl,String charset, int pageNo,Html html) {
	    this();
		if (pageNum <= 0)
			throw new IllegalArgumentException("Page num must be >= 1");
		this.id = id;
		mUrl = url;
		mType = type;
		mPageNum = pageNum;
		mMeta = new HashMap<Meta, Object>();
		mHtml = null;
		mCharset = null;
		mBaseUrl = null;
		ancestorUrl = accessUrl;
		mPageNo=pageNo;
		mCharset=charset;
		mHtml=html;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);

		mMeta.put(Page.Meta.errorTimes, 0);
	}

    public Page(int id,String sectAlias, String url, Extractor.Type type, int pageNum,
			String accessUrl) {
	    this();
		if (pageNum <= 0)
			throw new IllegalArgumentException("Page num must be >= 1");
		this.id = id;
		mUrl = url;
		mType = type;
		mPageNum = pageNum;
		mMeta = new HashMap<Meta, Object>();
		mHtml = null;
		mCharset = null;
		mBaseUrl = null;
		sectionAlias=sectAlias;
		ancestorUrl = accessUrl;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);

		mMeta.put(Page.Meta.errorTimes, 0);
	}

    @Deprecated
	public Page(String url, Extractor.Type type, int pageNum) {
	    this();
		if (pageNum <= 0)
			throw new IllegalArgumentException("Page num must be >= 1");
		mUrl = url;
		mType = type;
		mPageNum = pageNum;
		mMeta = new HashMap<Meta, Object>();
		mHtml = null;
		mCharset = null;
		mBaseUrl = null;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);

		mMeta.put(Page.Meta.errorTimes, 0);
	}

    public Page(String url,String sectAlias, Extractor.Type type, int pageNum) {
	    this();
		if (pageNum <= 0)
			throw new IllegalArgumentException("Page num must be >= 1");
		mUrl = url;
		mType = type;
		mPageNum = pageNum;
		mMeta = new HashMap<Meta, Object>();
		mHtml = null;
		mCharset = null;
		mBaseUrl = null;
		sectionAlias=sectAlias;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);

		mMeta.put(Page.Meta.errorTimes, 0);
	}

	
	public String getAncestorUrl() {
		return ancestorUrl;
	}
	
	public String getBaseUrl() {
		return mBaseUrl;
	}
	
	public String getCharset() {
		return mCharset;
	}
	public Date getFocusDate() {
		return focusDate;
	}
	 

	public Html getHtml() {
		return mHtml;
	}

    public int getId() {
		return id;
	}

    public Map<String,String> getImages()
	{
	    return this.images;
	}

    public Map<Meta, Object> getMeta() {
		return mMeta;
	}

    public Object getMeta(Meta key) {
		return mMeta.get(key);
	}

	public int getPageNo() {
		return mPageNo;
	}

	public int getPageNum() {
		return mPageNum;
	}

	public String getProfileName()
    {
        return profileName;
    }

	public String getPubCode()
    {
        return pubCode;
    }

	public GetResult getResult()
    {
        return result;
    }

	public String getRssId()
    {
        return rssId;
    }

	public String getSectionAlias() {
		return sectionAlias;
	}
	public String getSource()
    {
        return source;
    }

	public Extractor.Type getType() {
		return mType;
	}

	public String getTypeCode()
    {
        return typeCode;
    }
	public String getUrl() {
		return mUrl;
	}
	
	public void putAllMeta(Map<Meta, Object> meta) {
		for (Meta key : Meta.values()) {
			if (mMeta.get(key) == null)
				mMeta.put(key, meta.get(key));
		}
	}

	public void putImage(String originUrl,String localUrl)
	{
	    images.put(originUrl, localUrl);
	}

	public void putImages(Map<String,String> images)
	{
	    if(null==images)
	    {
	        return;
	    }
	    this.images.putAll(images);
//	    for(Entry<String,String> entry:images.entrySet())
//	    {
//	        putImage(entry.getKey(), entry.getValue());
//	    }
	}

	public void putMeta(Meta key, Object value) {
		mMeta.put(key, value);
	}

	public void setAncestorUrl(String ancestorUrl) {
		this.ancestorUrl = ancestorUrl;
	}

	public void setBaseUrl(String baseUrl) {
		mBaseUrl = baseUrl;
	}

	public void setCharset(String charset) {
		mCharset = charset;
	}

	public void setFocusDate(Date focusDate) {
		this.focusDate = focusDate;
	}

	public void setHtml(Html html) {
		mHtml = html;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPageNo(int pageNo) {
		mPageNo = pageNo;
	}

	public void setProfileName(String profileName)
    {
        this.profileName = profileName;
    }

	public void setPubCode(String pubCode)
    {
        this.pubCode = pubCode;
    }

	public void setResult(GetResult result)
    {
        this.result = result;
    }

	public void setRssId(String rssId)
    {
        this.rssId = rssId;
    }

	public void setSectionAlias(String sectionAlias) {
		this.sectionAlias = sectionAlias;
	}

	public void setSource(String source)
    {
        this.source = source;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public void setUrl(String url) {
		mUrl = url;
		if (mPageNum == 1)
			mMeta.put(Meta.pageOneUrl, mUrl);
	}

    public long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }
}
