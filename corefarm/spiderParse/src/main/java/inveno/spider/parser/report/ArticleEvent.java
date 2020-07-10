package inveno.spider.parser.report;

public class ArticleEvent {
    private String mAuthor;
    private String mSection;
    private String mPageno;
    private String mTitle;
    private String mUrl;
    private String mRefererUrl;
    private String mFilePath;
    private String mCharset;
    private String mMsgs;
    public ArticleEvent(String author, String section, String pageno, String title, String url, String refererUrl, String filePath, String charset) {
        mAuthor = author;
        mSection = section;
        mPageno = pageno;
        mTitle = title;
        mUrl = url;
        mRefererUrl = refererUrl;
        mFilePath = filePath;
        mCharset = charset;
        mMsgs = null;
    }
    public String getAuthor() {
        return mAuthor;
    }
    public String getSection() {
        return mSection;
    }
    public String getPageno() {
		return mPageno;
	}
	public String getTitle() {
        return mTitle;
    }
    public String getUrl() {
        return mUrl;
    }
    public String getRefererUrl() {
        return mRefererUrl;
    }
    public String getFilePath() {
        return mFilePath;
    }
    public String getCharset() {
		return mCharset;
	}
	public String getMsgs() {
        return mMsgs;
    }
    public void appendMessage(String msg) {
        if(mMsgs==null)
            mMsgs = msg;
        else
            mMsgs += ", " + msg;
    }
}
