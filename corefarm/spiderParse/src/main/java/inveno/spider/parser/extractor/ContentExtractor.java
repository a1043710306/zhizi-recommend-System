package inveno.spider.parser.extractor;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.base.Page.Meta;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.model.ContentConfig;
import inveno.spider.parser.model.ContentPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.Article;
import inveno.spider.parser.store.ArticleStore;
import inveno.spider.parser.utils.Similarity;
import inveno.spider.parser.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ContentExtractor implements Extractor
{
    private static final Logger LOG = LoggerFactory.make();

    private Profile profile;
    private String mType; // blog or news
    private Date mFromDate;
    private ContentConfig mContentConfig;
    private ParseStrategy mParseStrategy;
    private ArticleStore mArticleStore;
    private CrawlerReport mCrawlerReport;

    public ContentExtractor(Profile profile, ParseStrategy parseStrategy,
            ArticleStore articleStore, CrawlerReport crawlerReport)
    {
        this.profile = profile;
        mType = profile.getType();
        mFromDate = profile.getFromDate();
        mContentConfig = profile.getContentConfig();
        mParseStrategy = parseStrategy;
        mArticleStore = articleStore;
        mCrawlerReport = crawlerReport;
    }

    public String getCharset()
    {
        return mContentConfig.getCharset();
    }

    public List<Page> extract(Page page)
    {
        // redirect
        String first_url = (String) page.getMeta(Page.Meta.pageOneUrl);
        if (mArticleStore.contains(first_url))
            return Collections.emptyList();

        LOG.info("ContentExtract " + page.getUrl());
        try
        {
            String content = extractContent(page, page.getPageNum());
            String title = extractTitle(page);
            String summary = extractSummary(page);
            String author = extractAuthor(page);
            String section = extractSection(page);
            String pageno = extractPageno(page);
            Date date = extractDate(page);
            String nextpageLink = extractNextPageLink(page);
            Map<String, String> images = extractImagesByContent(content, page);

            if (nextpageLink != null)
            {
                // have more content page
                Page nextPage = new Page(page.getId(), Utils.calculateLink(
                        page.getBaseUrl(), nextpageLink, page.getCharset()),
                        Extractor.Type.Content, page.getPageNum() + 1,
                        page.getAncestorUrl());
                nextPage.putAllMeta(page.getMeta());
                String lastPageContent = (String) nextPage
                        .getMeta(Page.Meta.content);
                if (StringUtils.isBlank(lastPageContent))
                {
                    nextPage.putMeta(Page.Meta.content, content);
                } else
                {
                    nextPage.putMeta(Page.Meta.content, lastPageContent
                            + "<br/>" + content);
                }
                if (title != null)
                    nextPage.putMeta(Page.Meta.title, title);

                // put summary into page on the first time for only.
                String tmp = (String) nextPage.getMeta(Page.Meta.summary);
                if (tmp == null && summary != null)
                {
                    nextPage.putMeta(Page.Meta.summary, summary);
                }

                if (author != null)
                    nextPage.putMeta(Page.Meta.author, author);
                if (section != null)
                    nextPage.putMeta(Page.Meta.section, section);
                if (pageno != null)
                    nextPage.putMeta(Page.Meta.pageno, pageno);
                if (date != null)
                    nextPage.putMeta(Page.Meta.date, date);

                // add old images
                if (null != page.getImages() && page.getImages().size() > 0)
                {
                    nextPage.getImages().putAll(page.getImages());
                }

                if (images != null)
                {
                    nextPage.putImages(images);
                }

                nextPage.setProfileName(page.getProfileName());
                nextPage.setPubCode(page.getPubCode());
                nextPage.setAncestorUrl(page.getUrl());

                nextPage.setBatchId(page.getBatchId());

                nextPage.setRssId(String.valueOf(page.getRssId()));
                nextPage.setTypeCode(page.getTypeCode());
                nextPage.setSource(page.getSource());
                nextPage.setLevel(page.getLevel());
                nextPage.putMeta(Meta.tags, page.getMeta(Meta.tags));
                nextPage.putMeta(Meta.location, page.getMeta(Meta.location));
                nextPage.putMeta(Meta.isOpenComment,
                        page.getMeta(Meta.isOpenComment));
                nextPage.putMeta(Meta.dataType, page.getMeta(Meta.dataType));
                nextPage.putMeta(Meta.categoryName, page.getMeta(Meta.categoryName));
                nextPage.putMeta(Meta.checkCategoryFlag, page.getMeta(Meta.checkCategoryFlag));
                return Collections.singletonList(nextPage);
            }

            // save
            String url = (String) page.getMeta(Page.Meta.pageOneUrl);
            if (date == null)
                date = (Date) page.getMeta(Page.Meta.date);
            if (title == null)
                title = (String) page.getMeta(Page.Meta.title);

            // put summary into page on the first time for only.
            String tmp = (String) page.getMeta(Page.Meta.summary);
            if (tmp != null)
            {
                summary = tmp;
            }

            if (author == null)
                author = (String) page.getMeta(Page.Meta.author);
            if (section == null)
                section = (String) page.getMeta(Page.Meta.section);
            if (pageno == null)
                pageno = (String) page.getMeta(Page.Meta.pageno);
            String refererUrl = page.getAncestorUrl();

            String lastPageContent = (String) page.getMeta(Page.Meta.content);
            if (StringUtils.isNotBlank(lastPageContent))
                content = lastPageContent + "<br/>" + content;

            Map<String, String> cacheImages = page.getImages();
            if (null != cacheImages && !cacheImages.isEmpty())
            {
                images.putAll(cacheImages);
            }

            if (date == null)
            {
                if (null == profile.getListingConfig().getDatePath()
                        && null == profile.getContentConfig().getDatePath())
                {// 如没配置时间，取当天
                    date = new Date(); // default to today
                } else
                {// 放弃此页

                    throw new ExtractException(
                            "date path error, and can't extract date now.");
                }
            }

            Article article = new Article(url, date, title, summary, content,
                    author, section, pageno, refererUrl);

            article.setBatchId(page.getBatchId());
            article.setProfileName(page.getProfileName());
            article.setRssId(page.getRssId());
            article.setTypeCode(page.getTypeCode());
            article.setSource(page.getSource());
            article.setLevel(page.getLevel());

            String tags = (String) page.getMeta(Meta.tags);
            if (null != tags)
            {
                article.setTags(tags);
            }

            String location = (String) page.getMeta(Meta.location);
            if (null != location)
            {
                article.setLocation(location);
            }

            String isOpenComment = (String) page.getMeta(Meta.isOpenComment);
            if (null != isOpenComment)
            {
                article.setIsOpenComment(Integer.valueOf(isOpenComment));
            }
            
            String dataType = (String) page.getMeta(Meta.dataType);
            if (null != dataType)
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
            
            if (images != null)
            {
                article.getImages().putAll(images);
            }
            article.setPubCode(page.getPubCode());
            if (ArticleVerifier.verifyArticle(article, mType, mFromDate))
            {
                mArticleStore.save(article); // article.filePath will be filled
                mCrawlerReport.addArticle(article); // with saved filepath
                return Collections.emptyList();
            }
            else
            {
                mCrawlerReport.addArticle(article);
                return Collections.emptyList();
            }
        } catch (ExtractException e)
        {
            mCrawlerReport.reportContentPageFailure(page, mContentConfig, e);
            return Collections.emptyList();
        } catch (Throwable e)
        {
            LOG.error("", e);
            mCrawlerReport.reportGeneralError("ContentExtractor.extract", page,
                    e);
            return Collections.emptyList();
        }
    }

    private Map<String, String> extractImagesByContent(String content, Page page)
    {
        if (null == content)
        {
            return null;
        }
        Map<String, String> images = mParseStrategy.extractContentImagesPath(
                null != mContentConfig.getImagePath() ? mContentConfig
                        .getImagePath().getSrcTag() : "", content, page
                        .getCharset(), page.getUrl(), page.getBaseUrl());

        return images;
    }

    private String extractContent(Page page, int pageNum)
            throws ExtractException
    {
        ContentPath contentPath = mContentConfig.getContentPath();

        String[] contents = mParseStrategy.extractContentPath(page.getHtml(),
                mContentConfig.getHtml2xml(), contentPath);
        if (null != profile.getListingConfig() ? profile.getListingConfig()
                .getIsContentPage() : false)
        {
            if (StringUtils.isBlank(contents[page.getPageNo()]))
            {
                if (pageNum > 1)
                    return "";
                else
                    throw new ExtractException(
                            "Content is blank, and may be filtered!");
            }

            return StringUtils.trimToNull(contents[page.getPageNo()]);

        } else
        {
            if (contents.length == 0 || contents.length > 1)
            {
                if (pageNum > 1)
                    return "";
                else
                {
                    throw new ExtractException(
                            "Fail to extract content, content node length = "
                                    + contents.length);
                }
            }
            if (StringUtils.isBlank(contents[0]))
            {
                if (pageNum > 1)
                    return "";
                else
                    throw new ExtractException(
                            "Content is blank, and may be filtered!");
            }
            
            //replace img tag to correct tags.
            if(!StringUtils.isEmpty(contents[0]) && null!=mContentConfig.getImagePath())
            {
                contents[0] = StringUtils.replace(contents[0], mContentConfig.getImagePath().getSrcTag(), "src");
            }

            return StringUtils.trimToNull(contents[0]);
        }
    }

    private String extractTitle(Page page) throws ExtractException
    {
        Path titlePath = mContentConfig.getTitlePath();
        if (titlePath != null)
        {
            String[] titles = mParseStrategy.extractTitlePath(page.getHtml(),
                    mContentConfig.getHtml2xml(), titlePath);

            if (titles.length == 0 || titles.length > 1)
            {
                throw new ExtractException(
                        "Fail to extract title, title node length = "
                                + titles.length);
            }
            return StringUtils.trimToNull(titles[0]);
        }
        return null;
    }

    private String extractSummary(Page page) throws ExtractException
    {
        Path summaryPath = mContentConfig.getSummaryPath();
        if (summaryPath != null)
        {
            String[] summarys = mParseStrategy.extractSummaryPath(
                    page.getHtml(), mContentConfig.getHtml2xml(), summaryPath);

            if (summarys.length == 0 || summarys.length > 1)
            {
                throw new ExtractException(
                        "Fail to extract summary, summary node length = "
                                + summarys.length);
            }
            return StringUtils.trimToNull(summarys[0]);
        }
        return null;
    }

    private String extractAuthor(Page page)
    {
        try
        {
            Path authorPath = mContentConfig.getAuthorPath();
            if (authorPath != null)
            {
                String[] authors = mParseStrategy.extractAuthorPath(
                        page.getHtml(), mContentConfig.getHtml2xml(),
                        authorPath);

                if (authors.length == 0 || authors.length > 1)
                {
                    throw new ExtractException(
                            "Fail to extract author, author node length = "
                                    + authors.length);
                }
                return StringUtils.trimToNull(authors[0]);
            }
            return null;
        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportContentPageFailure(page, mContentConfig, e);
            return null;
        }
    }

    private String extractSection(Page page)
    {
        try
        {
            Path sectionPath = mContentConfig.getSectionPath();
            if (sectionPath != null)
            {
                String[] section = mParseStrategy.extractSectionPath(
                        page.getHtml(), mContentConfig.getHtml2xml(),
                        sectionPath);

                if (section.length == 0 || section.length > 1)
                {
                    throw new ExtractException(
                            "Fail to extract section, section node length = "
                                    + section.length);
                }
                return StringUtils.trimToNull(section[0]);
            }
            return null;
        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportContentPageFailure(page, mContentConfig, e);
            return null;
        }
    }

    private String extractPageno(Page page)
    {
        try
        {
            Path pagenoPath = mContentConfig.getPagenoPath();
            if (pagenoPath != null)
            {
                String[] pageno = mParseStrategy.extractPagenoPath(
                        page.getHtml(), mContentConfig.getHtml2xml(),
                        pagenoPath);

                if (pageno.length == 0 || pageno.length > 1)
                {
                    throw new ExtractException(
                            "Fail to extract pageno, pageno node length = "
                                    + pageno.length);
                }
                return StringUtils.trimToNull(pageno[0]);
            }
            return null;
        } catch (ExtractException e)
        {
            // failure to extract this should not affect extracting the doc
            mCrawlerReport.reportContentPageFailure(page, mContentConfig, e);
            return null;
        }
    }

    private Date extractDate(Page page) throws ExtractException
    {
        // extract dates
        DatePath datePath = mContentConfig.getDatePath();
        if (datePath != null)
        {
            Date[] dates = mParseStrategy.extractDatePath(page.getHtml(),
                    mContentConfig.getHtml2xml(), datePath, true);

            if (dates.length == 0 || dates.length > 1)
            {
                throw new ExtractException(
                        "Fail to extract date, date node length = "
                                + dates.length);
            }
            return dates[0];
        }
        return null;
    }

    private String extractNextPageLink(Page page) throws ExtractException
    {
        NextPagePath nextpagePath = mContentConfig.getNextpagePath();
        if (nextpagePath != null
                && page.getPageNum() < nextpagePath.getMaxpages())
        {
            String nextpageLink = mParseStrategy.extractNextPagePath(
                    page.getHtml(), mContentConfig.getHtml2xml(),
                    page.getCharset(), page.getUrl(), page.getBaseUrl(),
                    nextpagePath);
            if (isTheFinalPage(page, nextpageLink))
            {
                return null;
            }
            return nextpageLink;

        }
        return null;
    }

    private boolean isTheFinalPage(final Page page, final String nextPageLink)
    {
        // must calculate link at the first.
        String url = Utils.calculateLink(page.getBaseUrl(), nextPageLink,
                page.getCharset());
        // if current link to the same page with next or ancestor,then it means
        // to current page is the final page.
        if (nextPageLink != null)
        {
            return page.getUrl().equalsIgnoreCase(url)
                    || page.getAncestorUrl().equalsIgnoreCase(url)
                    || !getSimilarity(url, page.getUrl());
        }
        return true;

    }

    public Strategy getHtml2XmlStrategy()
    {
        return mContentConfig.getHtml2xml();
    }

    /**
     * 相似度大于90%，那么表示两个串基本上相同。
     * 
     * @param str
     * @param target
     * @return
     */
    private boolean getSimilarity(String str, String target)
    {
        float rate = Similarity.getSimilarityRatio(str, target);
        return rate > 0.90;
    }

}
