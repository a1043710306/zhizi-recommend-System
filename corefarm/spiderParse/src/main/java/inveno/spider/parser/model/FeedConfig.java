package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;


public class FeedConfig implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String mCharset;
    private Html2Xml.Strategy mHtml2xml;
    private boolean mFullContent;
    
    private RssPath rssPath;

    public FeedConfig()
    {
    }

    public FeedConfig(String charset, Html2Xml.Strategy html2xml,
            boolean fullContent)
    {
        mCharset = charset;
        mHtml2xml = html2xml;
        mFullContent = fullContent;
    }
    
    public FeedConfig(String charset, Html2Xml.Strategy html2xml,
            boolean fullContent,RssPath rssPath)
    {
        mCharset = charset;
        mHtml2xml = html2xml;
        mFullContent = fullContent;
        rssPath = rssPath;
    }

    public String getCharset()
    {
        return mCharset;
    }

    public void setCharset(String mCharset)
    {
        this.mCharset = mCharset;
    }

    public Html2Xml.Strategy getHtml2xml()
    {
        return mHtml2xml;
    }

    public void setHtml2xml(Html2Xml.Strategy mHtml2xml)
    {
        this.mHtml2xml = mHtml2xml;
    }

    public boolean isFullContent()
    {
        return mFullContent;
    }

    public void setFullContent(boolean fullContent)
    {
        this.mFullContent = fullContent;
    }

	public RssPath getRssPath() {
		return rssPath;
	}

	public void setRssPath(RssPath rssPath) {
		this.rssPath = rssPath;
	}
    
}
