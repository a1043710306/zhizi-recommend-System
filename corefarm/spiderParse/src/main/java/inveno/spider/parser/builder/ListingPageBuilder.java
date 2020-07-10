package inveno.spider.parser.builder;

import inveno.spider.parser.model.ListingPage;


public class ListingPageBuilder
{
    private String mAuthor;
    private String mSection;
    private String mUrl;

    public String getAuthor()
    {
        return mAuthor;
    }

    public void setAuthor(String author)
    {
        mAuthor = author;
    }

    public String getSection()
    {
        return mSection;
    }

    public void setSection(String section)
    {
        mSection = section;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setUrl(String url)
    {
        mUrl = url;
    }

    public ListingPage build()
    {
        return new ListingPage(mAuthor, mSection, mUrl);
    }
}
