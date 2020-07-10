package inveno.spider.parser.extractor;


import inveno.spider.parser.base.ContentFormatter;
import inveno.spider.parser.base.DateParser;
import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.ParseStrategy.DateExtractionStrategy;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.idclass.other.XPathHelper;
import inveno.spider.parser.model.RssArticle;
import inveno.spider.parser.model.RssPath;
import inveno.spider.parser.utils.Utils;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

import inveno.spider.common.utils.LoggerFactory;
import org.apache.log4j.Logger;

public class ExtractRss
{
	private static final Logger LOG = LoggerFactory.make();

	private static String standardRSSDateFormat="EEE, dd MMM yyyy hh:mm:ss Z";
	private static DateParser dp = new DateParser(DateParser.TIME_OFFSET_CHINA);
	private static Pattern pattern_zh = Pattern.compile("(\\S+), (\\d+) (\\S+) (\\d+){1,4} (\\d+):(\\d+):(\\d+) \\+(\\d+)");
	private static Pattern pattern_us = Pattern.compile("(\\S+), (\\d+) (\\S+) (\\d+){1,4} (\\d+):(\\d+):(\\d+) (\\S+)");
	
	
	public static RssArticle[] extract2(String src, XPathHelper xpathHelper, Html2Xml.Strategy strategy) throws ExtractException
	{
		try
		{
			src = formatRssElement(src);
			StringReader reader = new StringReader(src);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(reader);
			List<SyndEntryImpl> entries = feed.getEntries();
			RssArticle[] article = new RssArticle[entries.size()];
			for (int i = 0; i < entries.size(); i++)
			{
				SyndEntryImpl entry = entries.get(i);
				String title = ContentFormatter.formatTitle(entry.getTitle());
				String link = StringEscapeUtils.unescapeHtml(Utils.formatLink(entry.getLink()));

				Date date = entry.getPublishedDate();
				if (date==null)
					date = new Date();   // default to today
				String html = null;
				if (entry.getDescription()!=null)
				{
					html = entry.getDescription().getValue();
				}
				else if (entry.getContents().size()>0)
				{
					html = ((SyndContent)entry.getContents().get(0)).getValue();
				}
				String formattedHtml =null;
				if (html!=null)
				{
					formattedHtml = format(html, xpathHelper, strategy);
				}
				article[i] = new RssArticle(title, link, date, formattedHtml);
			}
			return article;
		}
		catch (Exception e)
		{
			LOG.fatal("[extract2]", e);
			throw new ExtractException("Fail to extract content from Rss", e);
		}
	}
	
	
	public static RssArticle[] extract(String src, XPathHelper xpathHelper, Html2Xml.Strategy strategy,String charset, RssPath rssPath) throws ExtractException
	{
		try
		{
			LOG.info("[extract] src=" + src);
			src = formatRssElement(src);
			List<String> titles = null;
			List<String> links  = null;
			List<String> dates  = null;
			List<String> contents = null;
			List<String> sources  = null;
			List<String> descriptions = null;
			List<String> tags = null;
			if (rssPath == null)
			{
				titles       = xpathHelper.getListString("//item/title", src, charset);
				links        = xpathHelper.getListString("//item/link", src, charset);
				dates        = xpathHelper.getListString("//item/pubdate", src, charset);
				contents     = xpathHelper.getListString("//item/content", src, charset);
				sources      = xpathHelper.getListString("//item/source", src, charset);
				descriptions = xpathHelper.getListString("//item/description", src, charset);
				tags         = xpathHelper.getListString("//item/tags", src, charset);
			}
			else
			{
				if (rssPath.getTitle() == null)
					throw new Exception("naming of node title cannot be null.");
				titles       = xpathHelper.getListString(rssPath.getTitle().toLowerCase(), src, charset);
				if (rssPath.getLink() == null)
					throw new Exception("naming of node link cannot be null.");
				links        = xpathHelper.getListString(rssPath.getLink().toLowerCase(), src, charset);
				if (rssPath.getPubDate() == null)
					throw new Exception("naming of node pubdate cannot be null.");
				dates        = xpathHelper.getListString(rssPath.getPubDate().toLowerCase(), src, charset);
				if (rssPath.getContent() == null)
					throw new Exception("naming of node content cannot be null.");
				contents     = xpathHelper.getListString(rssPath.getContent().toLowerCase(), src, charset);
				if (rssPath.getSource() == null)
					throw new Exception("naming of node source cannot be null.");
				sources      = xpathHelper.getListString(rssPath.getSource().toLowerCase(), src, charset);
				if (rssPath.getDescription() == null)
					throw new Exception("naming of node description cannot be null.");
				descriptions = xpathHelper.getListString(rssPath.getDescription().toLowerCase(), src, charset);
				if (rssPath.getTags() != null)
					tags = xpathHelper.getListString(rssPath.getTags().toLowerCase(), src, charset);
			}

			//if the content is exists,then use it for content,else use description.
			if (contents==null || contents.size()==0)
			{
				contents = descriptions;
			}

			RssArticle[] article = new RssArticle[links.size()];
			for (int i = 0, count=links.size(); i < count; i++)
			{
				String title = ContentFormatter.formatTitle(titles.get(i));
				String link = StringEscapeUtils.unescapeHtml(Utils.formatLink(links.get(i)));
				
				Date date = null;
				String dateStr = dates.get(i);
				Matcher matcher_us = pattern_us.matcher(dateStr);
				Matcher matcher_zh = pattern_zh.matcher(dateStr);
				if (matcher_zh.matches())
				{
					date = dp.extractDate(dateStr, DateExtractionStrategy.Custom, standardRSSDateFormat, "us"); 
				}
				else if (matcher_us.matches())
				{
					date = dp.extractDate(dateStr, DateExtractionStrategy.Custom, standardRSSDateFormat, "us"); 
				}
				else
				{
					date = dp.extractDate(dateStr, DateExtractionStrategy.Simple, null, null);
				}
				
				// default to today
				if (date==null)
					date = new Date();
				String html = null;
				
				if (contents != null && contents.size() > 0)
				{
					html = contents.get(i);
				}
				
				String formattedHtml = null;
				if (html != null)
				{
					formattedHtml = format(html, xpathHelper, strategy);
				}
				
				String author = null;
				if (sources != null && sources.size() > 0)
				{
					author = sources.get(i);
				}
				
				article[i] = new RssArticle(title, link, date, formattedHtml, author);
			}
			return article;
		}
		catch (Exception e)
		{
			LOG.fatal("[extract]", e);
			throw new ExtractException("Fail to extract content from Rss", e);
		}
	}

	private static String format(String html, XPathHelper xpathHelper, Html2Xml.Strategy html2xml) throws ExtractException
	{
		String xml = Html2Xml.convert(html, html2xml);
		Node node = xpathHelper.toNode(xml);
		return ContentFormatter.format(node);
	}
	
	/**
	 * converted the first letter which is upper case of rss element to lower case.
	 * @param rss
	 * @return
	 */
	private static String formatRssElement(String rss)
	{
		HashMap<String, String> convertElements = new HashMap<String, String>();
		Pattern pattern = Pattern.compile("<([a-zA-Z][a-zA-Z]+?)>");
		Matcher matcher = pattern.matcher(rss);
		while (matcher.find())
		{
			String element = matcher.group(1);
			if (!convertElements.containsKey(element))
			{
				String convert = element.toLowerCase();
				convertElements.put(element, convert);
			}
		}
		for (Map.Entry<String, String> entry : convertElements.entrySet())
		{
			String element = entry.getKey();
			String convert = entry.getValue();
			rss = rss.replaceAll(element, convert);
		}
		return rss;
	}
}
