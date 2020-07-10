package  inveno.spider.parser.base;


import inveno.spider.parser.exception.AnalyzeException;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.model.ClickNumPath;
import inveno.spider.parser.model.ContentPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.model.ReplyNumPath;
import inveno.spider.parser.model.RssArticle;
import inveno.spider.parser.model.RssPath;

import java.util.Date;
import java.util.Map;

public interface ParseStrategy {
    public static enum PathStrategy {FullPath, IdClass}
    public static enum DateExtractionStrategy {Simple, Custom}
    public static enum ReplyNumExtractionStrategy {Strict, Loose}
    public static enum ClickNumExtractionStrategy {Strict, Loose}
    public static enum PageStrategy {NextPage,PageSet}


    public String detectCharset(byte[] body, String charset);

    public String getBaseUrl(Html html, Html2Xml.Strategy html2xmlStrategy);

    public String analyzeLinkPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;

    public String[] extractLinkPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path linkPath) throws ExtractException;


    public String analyzeTitlePath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;

    public String[] extractTitlePath(Html html, Html2Xml.Strategy html2xmlStrategy, 
    		Path titlePath) throws ExtractException;
    
    public String[] extractSummaryPath(Html html, Html2Xml.Strategy html2xmlStrategy, 
            Path summaryPath) throws ExtractException;

    public String analyzeAuthorPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;

    public String[] extractAuthorPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path authorPath) throws ExtractException;

    public String analyzeSectionPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;

    public String[] extractSectionPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path sectionPath) throws ExtractException;
    
    public String analyzePagenoPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;

    public String[] extractPagenoPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path pagenoPath) throws ExtractException;
    
    public String analyzeDatePath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, DateExtractionStrategy deStrategy,
            NodeInfo... nodeInfo) throws AnalyzeException;
    
    public Date[] extractDatePath(Html html, Html2Xml.Strategy html2xmlStrategy,
            DatePath datePath, boolean isSingle) throws ExtractException;

    public String analyzeReplyNumberPath(String html, Html2Xml.Strategy html2xmlStrategy, PathStrategy strategy, 
            ReplyNumExtractionStrategy numStrategy, NodeInfo... nodeInfo) throws AnalyzeException;

	public int[] extractReplyNumberPath(Html html, Html2Xml.Strategy html2xmlStrategy,
	        ReplyNumPath replyNumberPath) throws ExtractException;
	
	public String analyzeClickNumberPath(String html, Html2Xml.Strategy html2xmlStrategy, PathStrategy strategy, 
            ClickNumExtractionStrategy numStrategy, NodeInfo... nodeInfo) throws AnalyzeException;

	public int[] extractClickNumberPath(Html html, Html2Xml.Strategy html2xmlStrategy,
	        ClickNumPath clickNumberPath) throws ExtractException;

	public Map<String,String> extractContentImagesPath(String srcTag,String content,String charset, 
            String currentUrl, String baseUrl);

    
    public String analyzeContentPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;
    
    public String[] extractContentPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            ContentPath contentPath) throws ExtractException;

    public String analyzeNextPagePath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException;
    
    public String extractNextPagePath(Html html, Html2Xml.Strategy html2xmlStrategy, String charset, 
            String currentUrl, String baseUrl, NextPagePath nextpagePath) throws ExtractException;

    // RSS listing page
    public RssArticle[] extractRssArticles(String rss, boolean extractContent, Html2Xml.Strategy html2xmlStrategy,String charset,RssPath rssPath) throws ExtractException;
}
