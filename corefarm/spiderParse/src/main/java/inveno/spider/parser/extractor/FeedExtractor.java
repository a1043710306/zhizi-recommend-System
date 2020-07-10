package inveno.spider.parser.extractor;


import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.Constants;
import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.base.Page.Meta;
import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.model.FeedConfig;
import inveno.spider.parser.model.RssArticle;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.Article;
import inveno.spider.parser.store.ArticleStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FeedExtractor implements Extractor {
    private static final Logger log = LoggerFactory.make();
    
    private String mType; // blog or news
    private Date mFromDate;
    private FeedConfig mFeedConfig;
    private ParseStrategy mParseStrategy;
    private ArticleStore mArticleStore;
    private CrawlerReport mCrawlerReport;
    
    public FeedExtractor(String type, Date fromDate, FeedConfig feedConfig, ParseStrategy parseStrategy,
            ArticleStore articleStore, CrawlerReport crawlerReport) {
        mType = type;
        mFromDate = fromDate;
        mFeedConfig = feedConfig;
        mParseStrategy = parseStrategy;
        mArticleStore = articleStore;
        mCrawlerReport = crawlerReport;
    }

    public String getCharset() {
        return mFeedConfig.getCharset();
    }

    public List<Page> extract(Page page) {
        log.info("FeedExtract " + page.getUrl());
        try
        {
            PubdateFilter pubdateFilter = new PubdateFilter(new Date());

            boolean isFullContent = mFeedConfig.isFullContent();
            RssArticle[] articles = mParseStrategy.extractRssArticles(page.getHtml().getHtml(), isFullContent, mFeedConfig.getHtml2xml(),page.getCharset(),mFeedConfig.getRssPath());
            if (isFullContent) {
                // save directly
                int newArticles = 0;
                for (RssArticle rssArticle : articles)
                {
                    if (mArticleStore.contains(rssArticle.getUrl()))
                    {
                        log.info("[FeedExtract] duplicated url " + rssArticle.getUrl());
                        continue;
                    }
                    if (mFromDate != null)
                    {
                    	if (rssArticle.getDate().before(mFromDate))
                        {
                            log.info("[FeedExtract] no new data 1");
                    		continue;
                        }
                    }
                    if (rssArticle.getDate().before(Constants.EARLIEST_DATE))
                    {
                        log.info("[FeedExtract] no new data 2");
                        continue;
                    }
                	
                    if ("news".equals(mType) && pubdateFilter.isEarlier(rssArticle.getDate()))
                    {
                        log.info("[FeedExtract] no new data 3");
                        continue;
                    }

                    String url = rssArticle.getUrl();
                    Date date = rssArticle.getDate();
                    String title = rssArticle.getTitle();
                    String summary = "";
                    String content = rssArticle.getContent();
                    String author = (String) page.getMeta(Meta.author);
                    String section = (String) page.getMeta(Meta.section);
                    String pageno = (String) page.getMeta(Meta.pageno);
                    Map<String,String> images = extractImagesByContent(content,page);

                    Article article = new Article(url, date, title,summary, content, author, section, pageno, page.getUrl());
                    
                    article.setProfileName(page.getProfileName());
                    article.setPubCode(page.getPubCode());
                    
                    article.setBatchId(page.getBatchId());
                    article.setRssId(String.valueOf(page.getRssId()));
                    article.setTypeCode(page.getTypeCode());
                    article.setSource(page.getSource());
                    article.setLevel(page.getLevel());
                    
                    String tags =(String) page.getMeta(Meta.tags);
                    if(null!=tags)
                    {
                        article.setTags(tags);
                    }
                    
                    String location =(String) page.getMeta(Meta.location);
                    if(null!=location)
                    {
                        article.setLocation(location);
                    }
                    
                    String isOpenComment =(String) page.getMeta(Meta.isOpenComment);
                    if(null!=isOpenComment)
                    {
                        article.setIsOpenComment(Integer.valueOf(isOpenComment));
                    }
                    
                    String dataType =(String) page.getMeta(Meta.dataType);
                    if(null!=dataType)
                    {
                        article.setDataType(Integer.valueOf(dataType));
                    }
                    
                    String infoType = (String) page.getMeta(Meta.infoType);
                    if (null != infoType)
                    {
                        article.setInfoType(Integer.valueOf(infoType));
                    }
                    
                    String categoryName = (String) page.getMeta(Meta.categoryName);
                    if (null != categoryName)
                    {
                        article.setCategoryName(categoryName);
                    }
                    
                    String checkCategoryFlag = (String) page.getMeta(Meta.checkCategoryFlag);
                    if (null != checkCategoryFlag)
                    {
                        article.setCheckCategoryFlag(Integer.valueOf(checkCategoryFlag));
                    }
                    
                    if (images!=null)
                    {
                        article.getImages().putAll(images);
                    }
                    
                    if(ArticleVerifier.verifyArticle(article, mType, mFromDate)) {
                        mArticleStore.save(article); // article.filePath will be filled with saved filepath
                        newArticles++;
                    }
                    mCrawlerReport.addArticle(article);
                }

                mCrawlerReport.addFeedsEvent(
                        (String)page.getMeta(Page.Meta.author),
                        (String)page.getMeta(Page.Meta.section),
                        page.getUrl(), articles.length, newArticles);
                
                
                return Collections.emptyList();

            } else {
                // need to get full content from original page
                List<Page> contentPages = new ArrayList<Page>();
                for (RssArticle article : articles) {
                    if(mArticleStore.contains(article.getUrl()))
                        continue;
                    if(mFromDate!=null){
                    	if(article.getDate().before(mFromDate)) 
                    		continue;
                    }
                    if(article.getDate().before(Constants.EARLIEST_DATE))
                        continue;
                	
                    if("news".equals(mType) && pubdateFilter.isEarlier(article.getDate()))
                        continue;

                    Page contentPage = new Page(page.getId(),article.getUrl(),
                            Extractor.Type.Content, 1,page.getAncestorUrl());
                    contentPage.putAllMeta(page.getMeta());
                    contentPage.putMeta(Page.Meta.date, article.getDate());
                    contentPage.putMeta(Page.Meta.title, article.getTitle());
                    contentPage.putMeta(Page.Meta.refererUrl, page.getUrl());
                    
                    contentPage.setProfileName(page.getProfileName());
                    contentPage.setPubCode(page.getPubCode());
                    contentPage.setAncestorUrl(page.getUrl());
                    
                    contentPage.setBatchId(page.getBatchId());
                    contentPage.setRssId(String.valueOf(page.getRssId()));
                    contentPage.setTypeCode(page.getTypeCode());
                    contentPage.setSource(page.getSource()); 
                    contentPage.setLevel(page.getLevel());
                    
                    String tags =(String) page.getMeta(Meta.tags);
                    if(null!=tags)
                    {
                        contentPage.putMeta(Meta.tags, tags);
                    }
                    
                    String location =(String) page.getMeta(Meta.location);
                    if(null!=location)
                    {
                        contentPage.putMeta(Meta.location,location);
                    }
                    
                    String isOpenComment =(String) page.getMeta(Meta.isOpenComment);
                    if(null!=isOpenComment)
                    {
                        contentPage.putMeta(Meta.isOpenComment,isOpenComment);
                    }
                    
                    String dataType =(String) page.getMeta(Meta.dataType);
                    if(null!=dataType)
                    {
                        contentPage.putMeta(Meta.dataType,dataType);
                    }
                    
                    String infoType = (String) page.getMeta(Meta.infoType);
                    if (null != infoType)
                    {
                    	contentPage.putMeta(Meta.infoType, infoType);
                    }
                    
                    String categoryName = (String) page.getMeta(Meta.categoryName);
                    if (!StringUtils.isNotEmpty(categoryName))
                    {
                    	contentPage.putMeta(Meta.categoryName, categoryName);
                    }
                    
                    String checkCategoryFlag = (String) page.getMeta(Meta.checkCategoryFlag);
                    if (StringUtils.isNotEmpty(checkCategoryFlag))
                    {
                    	contentPage.putMeta(Meta.checkCategoryFlag, checkCategoryFlag);
                    }
                    
                    if(page.getImages()!=null)
                    {
                        contentPage.getImages().putAll(page.getImages());
                    }
                    
                    
                    contentPages.add(contentPage);
                }
                
                mCrawlerReport.addFeedsEvent(
                        (String)page.getMeta(Page.Meta.author),
                        (String)page.getMeta(Page.Meta.section),
                        page.getUrl(), articles.length, contentPages.size());
                
                return contentPages;
            }
        } catch (ExtractException e) {
            mCrawlerReport.reportFeedsFailure(page, mFeedConfig, e);
            return Collections.emptyList();
        } catch (Throwable e) {
            log.error("", e);
            mCrawlerReport.reportGeneralError("FeedExtractor.extract", page, e);
            return Collections.emptyList();
        }
    }
    
    private Map<String, String> extractImagesByContent(String content,Page page)
    {
        if(null==content)
        {
            return null;
        }
        Map<String,String> images = mParseStrategy.extractContentImagesPath("",content,page.getCharset(), page.getUrl(), page.getBaseUrl());
        
        return images;
    }    

    public Strategy getHtml2XmlStrategy() {
        return mFeedConfig.getHtml2xml();
    }
}
