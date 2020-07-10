package inveno.spider.parser.builder;

import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.ParseStrategy.ClickNumExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.DateExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.PageStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;
import inveno.spider.parser.model.ClickNumPath;
import inveno.spider.parser.model.ContentPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.ManualRedirectConfig;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.model.PostConfig;

public class PostConfigBuilder
{
    private String charset;
    private String html2xml;

    private Path authorPhotoPath;
    private Path authorPath;
    private ContentPath commentPath;
    private ClickNumPath likeNumPath;
    private DatePath postTimePath;
    private NextPagePath nextpagePath;

    private ManualRedirectConfig manualRedirectConfig;

    public PostConfig build()
    {
        return new PostConfig(charset,
                html2xml == null ? Html2Xml.Strategy.tagSoup
                        : Html2Xml.Strategy.valueOf(html2xml), commentPath,
                authorPhotoPath, authorPath, likeNumPath, postTimePath,
                nextpagePath, manualRedirectConfig);
    }

    public Path getAuthorPath()
    {
        return authorPath;
    }

    public Path getAuthorPhotoPath()
    {
        return authorPhotoPath;
    }

    public String getCharset()
    {
        return charset;
    }

    public ContentPath getCommentPath()
    {
        return commentPath;
    }

    public String getHtml2xml()
    {
        return html2xml;
    }

    public Path getLikeNumPath()
    {
        return likeNumPath;
    }

    public ManualRedirectConfig getManualRedirectConfig()
    {
        return manualRedirectConfig;
    }

    public NextPagePath getNextpagePath()
    {
        return nextpagePath;
    }

    public DatePath getPostTimePath()
    {
        return postTimePath;
    }

    public void setAuthorPath(Path authorPath)
    {
        this.authorPath = authorPath;
    }

    public void setAuthorPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        authorPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy);
    }

    public void setAuthorPhotoPath(Path authorPhotoPath)
    {
        this.authorPhotoPath = authorPhotoPath;
    }

    public void setAuthorPhotoPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy,
            String additionalPath)
    {
        authorPhotoPath = new Path(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy, additionalPath);
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public void setCommentPath(ContentPath commentPath)
    {
        this.commentPath = commentPath;
    }

    public void setCommentPath(String path, String strategy,
            String regularExpression, String replaceWith, String matchStrategy,
            String filterNodes, String replaceLabelToDiv, String outputURL)
    {
        commentPath = new ContentPath(PathStrategy.valueOf(strategy), path,
                regularExpression, replaceWith, matchStrategy, filterNodes,
                !(replaceLabelToDiv == null || "false"
                        .equalsIgnoreCase(replaceLabelToDiv)),
                !(outputURL == null || "false".equalsIgnoreCase(outputURL)));
    }

    public void setHtml2xml(String html2xml)
    {
        this.html2xml = html2xml;
    }

    public void setLikeNumPath(String path, String strategy,
            String numStrategy, String regularExpression, String replaceWith,
            String matchStrategy)
    {
        ClickNumExtractionStrategy likeNumStrategy = ClickNumExtractionStrategy
                .valueOf(numStrategy == null ? "Loose" : "Strict");
        likeNumPath = new ClickNumPath(PathStrategy.valueOf(strategy), path,
                likeNumStrategy, regularExpression, replaceWith, matchStrategy);
    }

    public void setMannualRedirectConfig(String ifMathRedirect,
            String redirectStrategy, String regularExpression,
            String replaceWith)
    {
        this.manualRedirectConfig = new ManualRedirectConfig(ifMathRedirect,
                redirectStrategy, regularExpression, replaceWith);
    }

    public void setManualRedirectConfig(
            ManualRedirectConfig manualRedirectConfig)
    {
        this.manualRedirectConfig = manualRedirectConfig;
    }

    public void setNextpagePath(NextPagePath nextpagePath)
    {
        this.nextpagePath = nextpagePath;
    }

    public void setNextpagePath(String path, String strategy, String maxpages,
            String pageStrategy, String regularExpression, String replaceWith,
            String matchStrategy)
    {
        nextpagePath = new NextPagePath(PathStrategy.valueOf(strategy), path,
                Integer.parseInt(maxpages), PageStrategy.valueOf(pageStrategy),
                regularExpression, replaceWith, matchStrategy);
    }

    public void setPostTimePath(DatePath postTimePath)
    {
        this.postTimePath = postTimePath;
    }

    public void setPostTimePath(String path, String strategy,
            String dateStrategy, String pattern, String country,
            String regularExpression, String replaceWith, String matchStrategy)
    {
        postTimePath = new DatePath(PathStrategy.valueOf(strategy), path,
                DateExtractionStrategy.valueOf(dateStrategy), pattern, country,
                regularExpression, replaceWith, matchStrategy);
    }
}
