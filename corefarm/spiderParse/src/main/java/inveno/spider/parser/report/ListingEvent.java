package inveno.spider.parser.report;

public class ListingEvent {
    private String mAuthor;
    private String mSection;
    private String mUrl;
    private int mAllArticles;
    private int mNewArticles;
    public ListingEvent(String author, String section, String url, int allArticles, int newArticles) {
        mAuthor = author;
        mSection = section;
        mUrl = url;
        mAllArticles = allArticles;
        mNewArticles = newArticles;
    }
    public String getAuthor() {
        return mAuthor;
    }
    public String getSection() {
        return mSection;
    }
    public String getUrl() {
        return mUrl;
    }
    public int getAllArticles() {
        return mAllArticles;
    }
    public int getNewArticles() {
        return mNewArticles;
    }
}
