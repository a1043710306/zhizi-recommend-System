package inveno.spider.parser.builder;

import org.apache.commons.lang.StringUtils;

import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.model.FeedConfig;
import inveno.spider.parser.model.RssPath;


public class FeedConfigBuilder
{
    private String mFull_content;
    private String mCharset;
    private String mHtml2xml;
//    private RssPath rssPath;
    
    private String title;
	
	private String pubDate;
	
	private String content;
	
	private String description;
	
	private String source;
	
	private String tags;
	
	private String link;
    
    
//    public RssPath getRssPath() {
//		return rssPath;
//	}
//
//	public void setRssPath(RssPath rssPath) {
//		this.rssPath = rssPath;
//	}
	
	public void setRssAllPath(String title,String content,String pubDate,String description,String source,String tags,String link)
	{
		this.title = title;
		this.content = content;
		this.pubDate = pubDate;
		this.description = description;
		this.source = source;
		this.tags = tags;
		this.link = link;
	}
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getFull_content()
    {
        return mFull_content;
    }

    public void setFull_content(String full_content)
    {
        mFull_content = full_content;
    }

    public String getCharset()
    {
        return mCharset;
    }

    public void setCharset(String charset)
    {
        mCharset = charset;
    }

    public String getHtml2xml()
    {
        return mHtml2xml;
    }

    public void setHtml2xml(String html2xml)
    {
        mHtml2xml = html2xml;
    }

    public FeedConfig build()
    {
    	FeedConfig feedConfig = new FeedConfig(mCharset,
                mHtml2xml == null ? Html2Xml.Strategy.tagSoup
                        : Html2Xml.Strategy.valueOf(mHtml2xml),
                "true".equalsIgnoreCase(mFull_content));
    	
    	if(StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(content))
    	{
    		RssPath rssPath = new RssPath(title,pubDate,content,description,source,tags,link);
    		feedConfig.setRssPath(rssPath);
    	}
        return feedConfig;
    }
}
