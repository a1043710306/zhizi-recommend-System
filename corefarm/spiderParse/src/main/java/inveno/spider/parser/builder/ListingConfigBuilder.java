package inveno.spider.parser.builder;

import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.ParseStrategy.ClickNumExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.DateExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.PageStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;
import inveno.spider.parser.base.ParseStrategy.ReplyNumExtractionStrategy;
import inveno.spider.parser.model.ClickNumPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.ListingConfig;
import inveno.spider.parser.model.ManualRedirectConfig;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.model.ReplyNumPath;

public class ListingConfigBuilder {
    private String mCharset;
    private String mHtml2xml;
    private Path mLinkPath;
    private Path mTitlePath;
    private Path summaryPath;
    private Path mAuthorPath;
    private Path mSectionPath;
    private Path mPagenoPath;
    private DatePath mDatePath;
    private ReplyNumPath mReplyNumberPath;
    private ClickNumPath mClickNumberPath;
    private NextPagePath mNextpagePath;
    private boolean mIsContentPage;
    
    public boolean getIsContentPage() {
		return mIsContentPage;
	}
	public void setIsContentPage(boolean isContentPage) {
		mIsContentPage = isContentPage;
	}

	private ManualRedirectConfig mManualRedirectConfig;

    public String getCharset() {
        return mCharset;
    }
    public void setCharset(String charset) {
        mCharset = charset;
    }
    public String getHtml2xml() {
        return mHtml2xml;
    }
    public void setHtml2xml(String html2xml) {
        mHtml2xml = html2xml;
    }
    public void setLinkPath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy,String unescapeHtml) {
        mLinkPath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy,!"false".equalsIgnoreCase(unescapeHtml));
    }
    public void setTitlePath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy,String additionalPath) {
        mTitlePath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy,additionalPath);
    }
    public void setSummaryPath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy) {
        summaryPath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy);
    }
    public void setAuthorPath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy) {
        mAuthorPath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy);
    }
    public void setSectionPath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy) {
        mSectionPath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy);
    }
    public void setPagenoPath(String path, String strategy, String regularExpression, String replaceWith, String matchStrategy) {
        mPagenoPath = new Path(PathStrategy.valueOf(strategy), path, regularExpression, replaceWith, matchStrategy);
    }
    public void setDatePath(String path, String strategy, String dateStrategy, String pattern, String country, String regularExpression, String replaceWith, String matchStrategy) {
        mDatePath = new DatePath(PathStrategy.valueOf(strategy), path,DateExtractionStrategy.valueOf(dateStrategy), 
        		pattern, country, regularExpression, replaceWith, matchStrategy);
    }
    public void setReplyNumberPath(String path, String strategy, String numStrategy, String regularExpression, String replaceWith, String matchStrategy) {
        mReplyNumberPath = new ReplyNumPath(PathStrategy.valueOf(strategy), path, 
        		ReplyNumExtractionStrategy.valueOf(numStrategy), regularExpression, replaceWith, matchStrategy);
    }
    public void setClickNumberPath(String path, String strategy, String numStrategy, String regularExpression, String replaceWith, String matchStrategy) {
        mClickNumberPath = new ClickNumPath(PathStrategy.valueOf(strategy), path, 
        		ClickNumExtractionStrategy.valueOf(numStrategy), regularExpression, replaceWith, matchStrategy);
    }
    public void setNextpagePath(String path, String strategy, String maxpages, String pageStrategy, String regularExpression, String replaceWith, String matchStrategy) {
        mNextpagePath = new NextPagePath(PathStrategy.valueOf(strategy), path, Integer.parseInt(maxpages),
        		PageStrategy.valueOf(pageStrategy), regularExpression, replaceWith, matchStrategy);
    }

    public ManualRedirectConfig getManualRedirectConfig() {
		return mManualRedirectConfig;
	}
	public void setManualRedirectConfig(String ifMathRedirect,String redirectStrategy,String regularExpression,String replaceWith,String contentAddRefererUrl,String unescapeHtml) {
		this.mManualRedirectConfig = new ManualRedirectConfig(ifMathRedirect,redirectStrategy,regularExpression,replaceWith,!"false".equalsIgnoreCase(contentAddRefererUrl),"true".equalsIgnoreCase(unescapeHtml));
	}

    public ListingConfig build() {
        return new ListingConfig(mCharset,mIsContentPage, mHtml2xml==null?Html2Xml.Strategy.tagSoup:Html2Xml.Strategy.valueOf(mHtml2xml), mLinkPath, mTitlePath,summaryPath, mAuthorPath, mSectionPath, mPagenoPath, mDatePath, mReplyNumberPath, mClickNumberPath, mNextpagePath,mManualRedirectConfig);
    }
}
