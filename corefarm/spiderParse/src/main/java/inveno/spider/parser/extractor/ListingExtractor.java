package inveno.spider.parser.extractor;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.Constants;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.base.Page.Meta;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.ListingConfig;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.ArticleStore;
import inveno.spider.parser.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class ListingExtractor implements Extractor
{
    private static final Logger log = LoggerFactory.make();

    private String mType; // blog or news
    private Date mFromDate;
    private ListingConfig mListingConfig;
    private boolean mConvertUnicodeSimpToTrad;
    private ParseStrategy mParseStrategy;
    private ArticleStore mArticleStore;
    private CrawlerReport mCrawlerReport;

    public ListingExtractor(String type, Date fromDate,
            ListingConfig listingConfig, ParseStrategy parseStrategy,
            ArticleStore articleStore, CrawlerReport crawlerReport)
    {
        mType = type;
        mFromDate = fromDate;
        mListingConfig = listingConfig;
        mParseStrategy = parseStrategy;
        mArticleStore = articleStore;
        mCrawlerReport = crawlerReport;
    }

    public String getCharset()
    {
        return mListingConfig.getCharset();
    }

    public List<Page> extract(Page page)
    {
        log.info("ListingExtract " + page.getUrl());
        List<Page> newPages = new ArrayList<Page>();
        try
        {
            PubdateFilter pubdateFilter = new PubdateFilter(new Date());
            boolean isContentPage = mListingConfig.getIsContentPage();

            String[] links = extractLinks(page);
            String[] titles = extractTitles(page, links.length);
            String[] summarys = extractSummarys(page, links.length);
            String[] authors = extractAuthors(page, links.length);
            String[] sections = extractSections(page, links.length);
            String[] pagenos = extractPagenos(page, links.length);
            Date[] dates = extractDates(page, links.length);

            // construct content pages to be crawled
            for (int i = 0, count = links.length; i < count; i++)
            {


                String link = Utils.calculateLink(page.getBaseUrl(), links[i],
                        page.getCharset());

                if (mArticleStore.contains(link))
                {
                    continue;
                }
                Page contentPage = null;
                if (isContentPage)
                {
                    contentPage = new Page(page.getId(), link,
                            Extractor.Type.Content, 1, page.getAncestorUrl(),
                            page.getCharset(), i, page.getHtml());
                } else
                {
                    contentPage = new Page(page.getId(), link,
                            Extractor.Type.Content, 1, page.getAncestorUrl());

                }
                contentPage.putAllMeta(page.getMeta());
                if (titles != null)
                    contentPage.putMeta(Page.Meta.title, titles[i]);
                if (authors != null)
                    contentPage.putMeta(Page.Meta.author, authors[i]);
                if (sections != null)
                    contentPage.putMeta(Page.Meta.section, sections[i]);
                if (pagenos != null)
                    contentPage.putMeta(Page.Meta.pageno, pagenos[i]);
                if (dates != null && dates[i] != null) {
                    contentPage.putMeta(Page.Meta.date, dates[i]);
                    if (mFromDate != null) {
                        if (dates[i].before(mFromDate))
                            continue;
                    }
                    if (dates[i].before(Constants.EARLIEST_DATE))
                        continue;
                    if ("news".equals(mType)
                            && pubdateFilter.isEarlier(dates[i]))
                        continue;
                }
                contentPage.putMeta(Page.Meta.refererUrl, page.getUrl());
                
                //
                contentPage.setProfileName(page.getProfileName());
                contentPage.setPubCode(page.getPubCode());
                contentPage.setAncestorUrl(page.getUrl());
                
                contentPage.setBatchId(page.getBatchId());
                contentPage.setRssId(String.valueOf(page.getRssId()));
                contentPage.setTypeCode(page.getTypeCode());
                contentPage.setSource(page.getSource());
                contentPage.setLevel(page.getLevel());
                contentPage.putMeta(Meta.tags, page.getMeta(Meta.tags));
                contentPage.putMeta(Meta.location, page.getMeta(Meta.location));
                contentPage.putMeta(Meta.isOpenComment, page.getMeta(Meta.isOpenComment));
                contentPage.putMeta(Meta.dataType, page.getMeta(Meta.dataType));
                contentPage.putMeta(Meta.infoType, page.getMeta(Meta.infoType));
                contentPage.putMeta(Meta.checkCategoryFlag, page.getMeta(Meta.checkCategoryFlag));
                contentPage.putMeta(Meta.categoryName, page.getMeta(Meta.categoryName));
                
                newPages.add(contentPage);
            }

            mCrawlerReport.addListingEvent(
                    (String) page.getMeta(Page.Meta.author),
                    (String) page.getMeta(Page.Meta.section), page.getUrl(),
                    links.length, newPages.size());
            String nextpageLink = extractNextPageLink(page);
            if (nextpageLink != null)
            {
                Page nextListingPage = new Page(page.getId(),
                        Utils.calculateLink(page.getBaseUrl(), nextpageLink,
                                page.getCharset()), Extractor.Type.Listing,
                        page.getPageNum() + 1, page.getAncestorUrl());
                nextListingPage.putAllMeta(page.getMeta());
                
                nextListingPage.setProfileName(page.getProfileName());
                nextListingPage.setPubCode(page.getPubCode());
                nextListingPage.setAncestorUrl(page.getUrl());
                
                nextListingPage.setBatchId(page.getBatchId());
                
                nextListingPage.setRssId(String.valueOf(page.getRssId()));
                nextListingPage.setTypeCode(page.getTypeCode());
                nextListingPage.setSource(page.getSource());
                nextListingPage.setLevel(page.getLevel());
                nextListingPage.putMeta(Meta.tags, page.getMeta(Meta.tags));
                nextListingPage.putMeta(Meta.location, page.getMeta(Meta.location));
                nextListingPage.putMeta(Meta.isOpenComment, page.getMeta(Meta.isOpenComment));
                nextListingPage.putMeta(Meta.dataType, page.getMeta(Meta.dataType));
                nextListingPage.putMeta(Meta.infoType, page.getMeta(Meta.infoType));
                nextListingPage.putMeta(Meta.checkCategoryFlag, page.getMeta(Meta.checkCategoryFlag));
                nextListingPage.putMeta(Meta.categoryName, page.getMeta(Meta.categoryName));
                
                newPages.add(nextListingPage);
            }
            return newPages;
        } catch (ExtractException e)
        {
            mCrawlerReport.reportListingFailure(page, mListingConfig, e);
            return newPages;
        } catch (Throwable e)
        {
            log.error("", e);
            mCrawlerReport.reportGeneralError("ListingExtractor.extract", page,
                    e);
            return Collections.emptyList();
        }
    }

    private String[] extractLinks(Page page) throws ExtractException
    {
        Path linkPath = mListingConfig.getLinkPath();
        String[] links = mParseStrategy.extractLinkPath(page.getHtml(),
                mListingConfig.getHtml2xml(), linkPath);
        if (links.length == 0)
        {
            throw new ExtractException("Fail to extract links");
        }
        return links;
    }

    private String[] extractTitles(Page page, int expectedNum)
            throws ExtractException
    {
        String[] titles = null;
        Path titlePath = mListingConfig.getTitlePath();
        if (titlePath != null)
        {
            titles = mParseStrategy.extractTitlePath(page.getHtml(),
                    mListingConfig.getHtml2xml(), titlePath);

            if (titles.length != expectedNum)
            {
                throw new ExtractException("Fail to extract titles expecting "
                        + expectedNum + " but instead get " + titles.length);
            }
        }
        return titles;
    }
    private String[] extractSummarys(Page page, int expectedNum)
            throws ExtractException
            {
        String[] summarys = null;
        Path summaryPath = mListingConfig.getSummaryPath();
        if (summaryPath != null)
        {
            summarys = mParseStrategy.extractSummaryPath(page.getHtml(),
                    mListingConfig.getHtml2xml(), summaryPath);
            
            if (summarys.length != expectedNum)
            {
                throw new ExtractException("Fail to extract titles expecting "
                        + expectedNum + " but instead get " + summarys.length);
            }
        }
        return summarys;
            }

    private String[] extractAuthors(Page page, int expectedNum)
    {
        try
        {
            String[] authors = null;
            Path authorPath = mListingConfig.getAuthorPath();
            if (authorPath != null)
            {
                authors = mParseStrategy.extractAuthorPath(page.getHtml(),
                        mListingConfig.getHtml2xml(), authorPath);

                if (authors.length != expectedNum)
                {
                    throw new ExtractException(
                            "Fail to extract authors expecting " + expectedNum
                                    + " but instead get " + authors.length);
                }
            }
            return authors;

        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportListingFailure(page, mListingConfig, e);
            log.error("", e);
            return null;
        }
    }

    private String[] extractSections(Page page, int expectedNum)
    {
        try
        {
            String[] sections = null;
            Path sectionPath = mListingConfig.getSectionPath();
            if (sectionPath != null)
            {
                sections = mParseStrategy.extractSectionPath(page.getHtml(),
                        mListingConfig.getHtml2xml(), sectionPath);

                if (sections.length != expectedNum)
                {
                    throw new ExtractException(
                            "Fail to extract sections expecting " + expectedNum
                                    + " but instead get " + sections.length);
                }
            }
            return sections;

        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportListingFailure(page, mListingConfig, e);
            log.error("", e);
            return null;
        }
    }

    private String[] extractPagenos(Page page, int expectedNum)
    {
        try
        {
            String[] pagenos = null;
            Path pagenoPath = mListingConfig.getPagenoPath();
            if (pagenoPath != null)
            {
                pagenos = mParseStrategy.extractPagenoPath(page.getHtml(),
                        mListingConfig.getHtml2xml(), pagenoPath);

                if (pagenos.length != expectedNum)
                {
                    throw new ExtractException(
                            "Fail to extract pagenos expecting " + expectedNum
                                    + " but instead get " + pagenos.length);
                }
            }
            return pagenos;

        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportListingFailure(page, mListingConfig, e);
            log.error("", e);
            return null;
        }
    }

    private Date[] extractDates(Page page, int expectedNum)
            throws ExtractException
    {
        Date[] dates = null;
        DatePath datePath = mListingConfig.getDatePath();
        if (datePath != null)
        {
            dates = mParseStrategy.extractDatePath(page.getHtml(),
                    mListingConfig.getHtml2xml(), datePath, false);

            if (dates.length != expectedNum)
            {
                throw new ExtractException("Fail to extract dates expecting "
                        + expectedNum + " but instead get " + dates.length);
            }
        }
        return dates;
    }

    private String extractNextPageLink(Page page) throws ExtractException
    {
        NextPagePath nextpagePath = mListingConfig.getNextpagePath();
        if (nextpagePath != null
                && page.getPageNum() < nextpagePath.getMaxpages())
        {
            String nextpageLink = mParseStrategy.extractNextPagePath(
                    page.getHtml(), mListingConfig.getHtml2xml(),
                    page.getCharset(), page.getUrl(), page.getBaseUrl(),
                    nextpagePath);
            return nextpageLink;

        }
        return null;
    }

    public Strategy getHtml2XmlStrategy()
    {
        return mListingConfig.getHtml2xml();
    }
}
