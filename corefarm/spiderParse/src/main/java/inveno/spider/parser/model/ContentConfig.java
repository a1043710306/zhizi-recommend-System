package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;

public class ContentConfig implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String mCharset;
    private Html2Xml.Strategy mHtml2xml;
    private ContentPath mContentPath;
    private Path mTitlePath; // optional
    private Path summaryPath; // optional
    private Path mAuthorPath; // optional
    private Path mSectionPath; // optional
    private Path mPagenoPath; // optional
    private DatePath mDatePath; // optional
    private NextPagePath mNextpagePath; // optional
    private ImagePath imagePath; // optional
    
  //post
    private Path postLinkPath;//optional
    private ClickNumPath postNumPath;//optional

    private ManualRedirectConfig mManualRedirectConfig;

    public ContentConfig()
    {
    }

    public ContentConfig(String charset, Html2Xml.Strategy html2xml,
            ContentPath contentPath, Path titlePath, Path summaryPath,
            Path authorPath, Path sectionPath, Path pagenoPath,
            DatePath datePath, NextPagePath nextpagePath,
            ManualRedirectConfig manualRedirectConfig,ImagePath imagePath,
            Path postLinkPath,ClickNumPath postNumPath)
    {
        mCharset = charset;
        mHtml2xml = html2xml;
        mContentPath = contentPath;
        mTitlePath = titlePath;
        this.setSummaryPath(summaryPath);
        mAuthorPath = authorPath;
        mSectionPath = sectionPath;
        mPagenoPath = pagenoPath;
        mDatePath = datePath;
        mNextpagePath = nextpagePath;
        mManualRedirectConfig = manualRedirectConfig;
        this.imagePath=imagePath;
        this.postLinkPath= postLinkPath;
        this.postNumPath=postNumPath;
    }

    public Path getAuthorPath()
    {
        return mAuthorPath;
    }

    public String getCharset()
    {
        return mCharset;
    }

    public ContentPath getContentPath()
    {
        return mContentPath;
    }

    public DatePath getDatePath()
    {
        return mDatePath;
    }

    public Html2Xml.Strategy getHtml2xml()
    {
        return mHtml2xml;
    }

    public ImagePath getImagePath()
    {
        return imagePath;
    }

    public ManualRedirectConfig getManualRedirectConfig()
    {
        return mManualRedirectConfig;
    }

    public NextPagePath getNextpagePath()
    {
        return mNextpagePath;
    }

    public Path getPagenoPath()
    {
        return mPagenoPath;
    }

    public Path getPostLinkPath()
    {
        return postLinkPath;
    }

    public ClickNumPath getPostNumPath()
    {
        return postNumPath;
    }

    public Path getSectionPath()
    {
        return mSectionPath;
    }

    public Path getSummaryPath()
    {
        return summaryPath;
    }

    public Path getTitlePath()
    {
        return mTitlePath;
    }

    public void setAuthorPath(Path authorPath)
    {
        mAuthorPath = authorPath;
    }

    public void setCharset(String charset)
    {
        mCharset = charset;
    }

    public void setContentPath(ContentPath contentPath)
    {
        mContentPath = contentPath;
    }

    public void setDatePath(DatePath datePath)
    {
        mDatePath = datePath;
    }

    public void setHtml2xml(Html2Xml.Strategy html2xml)
    {
        mHtml2xml = html2xml;
    }

    public void setImagePath(ImagePath imagePath)
    {
        this.imagePath = imagePath;
    }

    public void setManualRedirectConfig(
            ManualRedirectConfig mManualRedirectConfig)
    {
        this.mManualRedirectConfig = mManualRedirectConfig;
    }

    public void setNextpagePath(NextPagePath nextpagePath)
    {
        mNextpagePath = nextpagePath;
    }

    public void setPagenoPath(Path pagenoPath)
    {
        mPagenoPath = pagenoPath;
    }

    public void setPostLinkPath(Path postLinkPath)
    {
        this.postLinkPath = postLinkPath;
    }

    public void setPostNumPath(ClickNumPath postNumPath)
    {
        this.postNumPath = postNumPath;
    }

    public void setSectionPath(Path sectionPath)
    {
        mSectionPath = sectionPath;
    }

    public void setSummaryPath(Path summaryPath)
    {
        this.summaryPath = summaryPath;
    }

    public void setTitlePath(Path titlePath)
    {
        mTitlePath = titlePath;
    }
}
