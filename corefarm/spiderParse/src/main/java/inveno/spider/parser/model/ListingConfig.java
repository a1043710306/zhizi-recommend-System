package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;


public class ListingConfig implements Serializable{
    private String mCharset;
    private Html2Xml.Strategy mHtml2xml;
    private Path mLinkPath;
    private Path mTitlePath;       // optional
    private Path summaryPath;       // optional
    private Path mAuthorPath;       // optional
    private Path mSectionPath;      // optional
    private Path mPagenoPath;      // optional
    private DatePath mDatePath;        // optional
    private ReplyNumPath mReplyNumberPath;  //optional
    private ClickNumPath mClickNumberPath;  //optional
    private NextPagePath mNextpagePath;    // optional
    private boolean mIsContentPage;

    private ManualRedirectConfig mManualRedirectConfig;
    
    public ListingConfig(){}
    public ListingConfig(String charset,boolean isContentPage, Html2Xml.Strategy html2xml, Path linkPath, Path titlePath,Path summaryPath, Path authorPath, Path sectionPath, Path pagenoPath, DatePath datePath, ReplyNumPath replyNumberPath, ClickNumPath clickNumberPath, NextPagePath nextpagePath, ManualRedirectConfig manualRedirectConfig) {
        mCharset = charset;
        mHtml2xml = html2xml;
        mIsContentPage=isContentPage;
        mLinkPath = linkPath;
        mTitlePath = titlePath;
        this.setSummaryPath(summaryPath);
        mAuthorPath = authorPath;
        mSectionPath = sectionPath;
        mPagenoPath = pagenoPath;
        mDatePath = datePath;
        mReplyNumberPath = replyNumberPath;
        mClickNumberPath = clickNumberPath;
        mNextpagePath = nextpagePath;
        mManualRedirectConfig=manualRedirectConfig;
    }
    
    public boolean getIsContentPage() {
		return mIsContentPage;
	}
	public void setIsContentPage(boolean isContentPage) {
		mIsContentPage = isContentPage;
	}
	public String getCharset() {
		return mCharset;
	}
	public void setCharset(String charset) {
		mCharset = charset;
	}
	public Html2Xml.Strategy getHtml2xml() {
		return mHtml2xml;
	}
	public void setHtml2xml(Html2Xml.Strategy html2xml) {
		mHtml2xml = html2xml;
	}
	public Path getLinkPath() {
		return mLinkPath;
	}
	public void setLinkPath(Path linkPath) {
		mLinkPath = linkPath;
	}
	public Path getTitlePath() {
		return mTitlePath;
	}
	public void setTitlePath(Path titlePath) {
		mTitlePath = titlePath;
	}
	public Path getAuthorPath() {
		return mAuthorPath;
	}
	public void setAuthorPath(Path authorPath) {
		mAuthorPath = authorPath;
	}
	public Path getSectionPath() {
		return mSectionPath;
	}
	public void setSectionPath(Path sectionPath) {
		mSectionPath = sectionPath;
	}
	public Path getPagenoPath() {
		return mPagenoPath;
	}
	public void setPagenoPath(Path pagenoPath) {
		mPagenoPath = pagenoPath;
	}
	public DatePath getDatePath() {
		return mDatePath;
	}
	public void setDatePath(DatePath datePath) {
		mDatePath = datePath;
	}
	public ReplyNumPath getReplyNumberPath() {
		return mReplyNumberPath;
	}
	public void setReplyNumberPath(ReplyNumPath replyNumberPath) {
		mReplyNumberPath = replyNumberPath;
	}
	public ClickNumPath getClickNumberPath() {
		return mClickNumberPath;
	}
	public void setClickNumberPath(ClickNumPath clickNumberPath) {
		mClickNumberPath = clickNumberPath;
	}
	public NextPagePath getNextpagePath() {
		return mNextpagePath;
	}
	public void setNextpagePath(NextPagePath nextpagePath) {
		mNextpagePath = nextpagePath;
	}
	public ManualRedirectConfig getManualRedirectConfig() {
		return mManualRedirectConfig;
	}
	public void setManualRedirectConfig(
			ManualRedirectConfig mManualRedirectConfig) {
		this.mManualRedirectConfig = mManualRedirectConfig;
	}
    public Path getSummaryPath()
    {
        return summaryPath;
    }
    public void setSummaryPath(Path summaryPath)
    {
        this.summaryPath = summaryPath;
    }
}
