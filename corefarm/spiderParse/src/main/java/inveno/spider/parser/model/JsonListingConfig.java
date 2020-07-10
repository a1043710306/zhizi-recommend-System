package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;


public class JsonListingConfig implements Serializable{
    private String mCharset;
    private Path mLinkPath;
    private Path mTitlePath;       // optional
    private Path mAuthorPath;       // optional
    private Path mSectionPath;      // optional
    private Path mPagenoPath;      // optional
    private DatePath mDatePath;        // optional
    private NextPagePath mNextpagePath;    // optional
    private boolean mIsContentPage;
    
    public JsonListingConfig(){}
    public JsonListingConfig(String charset,boolean isContentPage, Path linkPath, Path titlePath, Path authorPath, Path sectionPath, Path pagenoPath, DatePath datePath, NextPagePath nextpagePath) {
        mCharset = charset;
        mIsContentPage=isContentPage;
        mLinkPath = linkPath;
        mTitlePath = titlePath;
        mAuthorPath = authorPath;
        mSectionPath = sectionPath;
        mPagenoPath = pagenoPath;
        mDatePath = datePath;
        mNextpagePath = nextpagePath;
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
	public NextPagePath getNextpagePath() {
		return mNextpagePath;
	}
	public void setNextpagePath(NextPagePath nextpagePath) {
		mNextpagePath = nextpagePath;
	}
}
