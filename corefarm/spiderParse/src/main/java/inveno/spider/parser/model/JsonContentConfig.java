package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.Html2Xml;


public class JsonContentConfig implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String mCharset;
    private Path mContentPath;
    private Path mTitlePath;       // optional
    private Path mAuthorPath;       // optional
    private Path mSectionPath;      // optional
    private Path mPagenoPath;		// optional
    private DatePath mDatePath;        // optional
    private NextPagePath mNextpagePath;    // optional
    
    private ManualRedirectConfig mManualRedirectConfig;

    public JsonContentConfig(){}
    public JsonContentConfig(String charset,Path contentPath, Path titlePath, Path authorPath, Path sectionPath, Path pagenoPath, DatePath datePath, NextPagePath nextpagePath) {
        mCharset = charset;
        mContentPath = contentPath;
        mTitlePath = titlePath;
        mAuthorPath = authorPath;
        mSectionPath = sectionPath;
        mPagenoPath = pagenoPath;
        mDatePath = datePath;
        mNextpagePath = nextpagePath;
    }
	public String getCharset() {
		return mCharset;
	}
	public void setCharset(String charset) {
		mCharset = charset;
	}
	public Path getContentPath() {
		return mContentPath;
	}
	public void setContentPath(ContentPath contentPath) {
		mContentPath = contentPath;
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
	public ManualRedirectConfig getManualRedirectConfig() {
		return mManualRedirectConfig;
	}
	public void setManualRedirectConfig(
			ManualRedirectConfig mManualRedirectConfig) {
		this.mManualRedirectConfig = mManualRedirectConfig;
	}
}
