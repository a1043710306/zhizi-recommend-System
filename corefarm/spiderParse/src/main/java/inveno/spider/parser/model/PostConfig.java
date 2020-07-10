package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;

public class PostConfig implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String mCharset;
    private Html2Xml.Strategy mHtml2xml;
    
    private Path authorPhotoPath;
    private Path authorPath;
    private ContentPath commentPath;
    private ClickNumPath likeNumPath;
    private DatePath postTimePath;
    private NextPagePath nextpagePath;
    private ManualRedirectConfig manualRedirectConfig;

    public PostConfig()
    {
    }

    public PostConfig(String charset, Html2Xml.Strategy html2xml,
            ContentPath commentPath, 
            Path authorPhotoPath, 
            Path authorPath, 
            ClickNumPath likeNumPath, 
            DatePath postTimePath, 
            NextPagePath nextpagePath,
            ManualRedirectConfig manualRedirectConfig)
    {
        mCharset = charset;
        mHtml2xml = html2xml;
        this.commentPath = commentPath;
        this.authorPhotoPath = authorPhotoPath;
        this.authorPath = authorPath;
        
        this.likeNumPath = likeNumPath;
        this.postTimePath = postTimePath;
        this.nextpagePath = nextpagePath;
        this.manualRedirectConfig = manualRedirectConfig;
    }

    public Path getAuthorPath()
    {
        return authorPath;
    }

    public Path getAuthorPhotoPath()
    {
        return authorPhotoPath;
    }
    
    public String getCharset()
    {
        return mCharset;
    }
    public ContentPath getCommentPath()
    {
        return commentPath;
    }
    public Html2Xml.Strategy getHtml2xml()
    {
        return mHtml2xml;
    }

    public ClickNumPath getLikeNumPath()
    {
        return likeNumPath;
    }

    public ManualRedirectConfig getManualRedirectConfig()
    {
        return manualRedirectConfig;
    }

    public NextPagePath getNextpagePath()
    {
        return nextpagePath;
    }

    public DatePath getPostTimePath()
    {
        return postTimePath;
    }

    public void setAuthorPath(Path authorPath)
    {
        this.authorPath = authorPath;
    }

    public void setAuthorPhotoPath(Path authorPhotoPath)
    {
        this.authorPhotoPath = authorPhotoPath;
    }

    public void setCharset(String charset)
    {
        mCharset = charset;
    }

    public void setCommentPath(ContentPath commentPath)
    {
        this.commentPath = commentPath;
    }


    public void setHtml2xml(Html2Xml.Strategy html2xml)
    {
        mHtml2xml = html2xml;
    }

    public void setLikeNumPath(ClickNumPath likeNumPath)
    {
        this.likeNumPath = likeNumPath;
    }


    public void setManualRedirectConfig(
            ManualRedirectConfig mManualRedirectConfig)
    {
        this.manualRedirectConfig = mManualRedirectConfig;
    }


    public void setNextpagePath(NextPagePath nextpagePath)
    {
        this.nextpagePath = nextpagePath;
    }

    public void setPostTimePath(DatePath postTimePath)
    {
        this.postTimePath = postTimePath;
    }
}
