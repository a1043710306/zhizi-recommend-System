package inveno.spider.parser.builder;

import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.ParseStrategy.ClickNumExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.DateExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.PageStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;
import inveno.spider.parser.model.ClickNumPath;
import inveno.spider.parser.model.ContentConfig;
import inveno.spider.parser.model.ContentPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.ImagePath;
import inveno.spider.parser.model.ManualRedirectConfig;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;

public class ContentConfigBuilder
{
    private String mCharset;
    private String mHtml2xml;
    private ContentPath mContentPath;
    private Path mTitlePath;
    private Path summaryPath;
    private Path mAuthorPath;
    private Path mSectionPath;
    private Path mPagenoPath;
    private DatePath mDatePath;
    private NextPagePath mNextpagePath;
    private ImagePath imagePath;
    
    //post
    private Path postLinkPath;
    private ClickNumPath postNumPath;

    private ManualRedirectConfig mMannualRedirectConfig;

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

    public void setContentPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy,
            String filterNodes, String replaceLabelToDiv,String outputURL)
    {
        mContentPath = new ContentPath(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy, filterNodes,
                !(replaceLabelToDiv == null || "false"
                        .equalsIgnoreCase(replaceLabelToDiv)),
                !(outputURL == null || "false".equalsIgnoreCase(outputURL)));
    }

    public void setTitlePath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy,
            String additionalPath)
    {
        mTitlePath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy, additionalPath);
    }

    public void setSummaryPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        summaryPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }
    public void setAuthorPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        mAuthorPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }

    public void setSectionPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        mSectionPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }

    public void setPagenoPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        mPagenoPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }
    public void setPostLinkPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        postLinkPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }
    public void setPostNumPath(String path, String strategy,String numStrategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        ClickNumExtractionStrategy likeNumStrategy = ClickNumExtractionStrategy
                .valueOf(numStrategy == null ? "Loose" : "Strict");
        postNumPath = new ClickNumPath(PathStrategy.valueOf(strategy), path,
                likeNumStrategy, regularExpression, replaceWith, matchStrategy);        
    }

    public void setDatePath(String path, String strategy, String dateStrategy,
            String pattern, String country, String regularExpression,
            String replaceWith, String matchStrategy)
    {
        mDatePath = new DatePath(PathStrategy.valueOf(strategy), path,
                DateExtractionStrategy.valueOf(dateStrategy), pattern, country,
                regularExpression, replaceWith, matchStrategy);
    }

    public void setNextpagePath(String path, String strategy, String maxpages,
            String pageStrategy, String regularExpression, String replaceWith,
            String matchStrategy)
    {
        mNextpagePath = new NextPagePath(PathStrategy.valueOf(strategy), path,
                Integer.parseInt(maxpages), PageStrategy.valueOf(pageStrategy),
                regularExpression, replaceWith, matchStrategy);
    }

    public ManualRedirectConfig getMannualRedirectConfig()
    {
        return mMannualRedirectConfig;
    }
    
    public void setImagePath(String srcTag)
    {
        this.imagePath = new ImagePath(srcTag);
    }

    public void setMannualRedirectConfig(String ifMathRedirect,
            String redirectStrategy, String regularExpression,
            String replaceWith)
    {
        this.mMannualRedirectConfig = new ManualRedirectConfig(ifMathRedirect,
                redirectStrategy, regularExpression, replaceWith);
    }

    public ContentConfig build()
    {
        return new ContentConfig(mCharset,
                mHtml2xml == null ? Html2Xml.Strategy.tagSoup
                        : Html2Xml.Strategy.valueOf(mHtml2xml), mContentPath,
                mTitlePath,summaryPath, mAuthorPath, mSectionPath, mPagenoPath, mDatePath,
                mNextpagePath, mMannualRedirectConfig,imagePath,postLinkPath,postNumPath);
    }
}
