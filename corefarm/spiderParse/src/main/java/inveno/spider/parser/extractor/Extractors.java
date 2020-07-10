package inveno.spider.parser.extractor;

import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.ArticleStore;


public class Extractors implements ExtractorsSuit
{
    private ListingExtractor mListingExtractor;
    private FeedExtractor mFeedExtractor;
    private ContentExtractor mContentExtractor;
    private ParseStrategy mParseStrategy;
    
    private JsonListingExtractor jsonListingExtractor;
    private JsonContentExtractor jsonContentExtractor;
    
    private JsonExtractor jsonExtractor;

    public Extractors(Profile profile, ArticleStore pageStore, CrawlerReport crawlerReport, ParseStrategy parseStrategy)
    {
        mParseStrategy    = parseStrategy;
        mListingExtractor = new ListingExtractor(profile.getType(), profile.getFromDate(), profile.getListingConfig(), mParseStrategy, pageStore, crawlerReport);
        mFeedExtractor    = new FeedExtractor(profile.getType(), profile.getFromDate(), profile.getFeedConfig(), mParseStrategy, pageStore, crawlerReport);
        mContentExtractor = new ContentExtractor(profile, mParseStrategy, pageStore, crawlerReport);
        jsonExtractor     = new JsonExtractor(profile, mParseStrategy, pageStore, crawlerReport);
    }
    
    public Extractor getExtractor(Extractor.Type type)
    {
        if (type==Extractor.Type.Listing)
        {
            return mListingExtractor;
        }
        else if(type==Extractor.Type.Feed)
        {
            return mFeedExtractor;
        }
        else if(type==Extractor.Type.Content)
        {
            return mContentExtractor;
        }
        else if(type == Extractor.Type.API)
        {
        	return jsonExtractor;
        }
        else
        {
            throw new RuntimeException("Invalid extractor type " + type);
        }
    }
}
