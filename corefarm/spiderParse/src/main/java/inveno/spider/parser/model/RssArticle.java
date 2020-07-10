package inveno.spider.parser.model;

import java.util.Date;

public class RssArticle
{
    private String mTitle;
    private String mUrl;
    private Date mDate;
    private String mContent;
    private String source;//similar to author

    public RssArticle(String title, String url, Date date, String content)
    {
        mTitle = title;
        mUrl = url;
        mDate = date;
        mContent = content;
    }
    public RssArticle(String title, String url, Date date, String content,String author)
    {
        mTitle = title;
        mUrl = url;
        mDate = date;
        mContent = content;
        source = author;
    }
    public String getTitle()
    {
        return mTitle;
    }
    public String getUrl()
    {
        return mUrl;
    }
    public Date getDate()
    {
        return mDate;
    }
    public String getContent()
    {
        return mContent;
    }
    public String getSource()
    {
        return source;
    }
    public void setSource(String source)
    {
        this.source = source;
    }
}
