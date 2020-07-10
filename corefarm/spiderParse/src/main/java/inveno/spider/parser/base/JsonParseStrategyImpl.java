package inveno.spider.parser.base;



import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.exception.AnalyzeException;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.idclass.other.JsonPathHelper;
import inveno.spider.parser.model.ClickNumPath;
import inveno.spider.parser.model.ContentPath;
import inveno.spider.parser.model.DatePath;
import inveno.spider.parser.model.NextPagePath;
import inveno.spider.parser.model.Path;
import inveno.spider.parser.model.ReplyNumPath;
import inveno.spider.parser.model.RssArticle;
import inveno.spider.parser.model.RssPath;
import inveno.spider.parser.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class JsonParseStrategyImpl implements ParseStrategy {
    private JsonPathHelper mXPathHelper = new JsonPathHelper();
    private DateParser dateParser = null;
    
    private static final String HTML_TAG_LABEL="label";
    private static final String HTML_TAG_DIV="div";
    
    public JsonParseStrategyImpl(){
        dateParser = new DateParser(DateParser.TIME_OFFSET_CHINA);
    }
    

    public JsonParseStrategyImpl(boolean isDateCanNull){
        this();
    	//设置dateparser.isDateCanNull标记，date提取结果可为null.
    	this.dateParser.setDateCanNull(isDateCanNull);
    }
    public JsonParseStrategyImpl(boolean isDateCanNull,int timeOffset)
    {
        dateParser = new DateParser(timeOffset);
      //设置dateparser.isDateCanNull标记，date提取结果可为null.
        this.dateParser.setDateCanNull(isDateCanNull);
        
    }

    public String detectCharset(byte[] body, String charset) {
        return CharsetDetector.getCharset(body, charset);
    }

    public String getBaseUrl(Html html, Html2Xml.Strategy html2xmlStrategy) {
        return null;
    }

    public String analyzeLinkPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return null;
    }

    public String[] extractLinkPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path linkPath) throws ExtractException {
        
    	List<String> nodelist = mXPathHelper.getListString(linkPath.getPath(), html.getHtml());
    	int count = nodelist.size();
    	String[] strResult = new String[count];
    	for(int i=0; i<count; i++){
    		strResult[i] = Utils.formatLink(nodelist.get(i));
    		strResult[i] = Utils.getRegExpReplace(strResult[i], linkPath.getRegularExpression(), 
    				linkPath.getReplaceWith(), linkPath.getMatchStrategy());
    		//TODO unescape html
            if (linkPath.isUnescapeHtml())
            {
                strResult[i] = StringEscapeUtils.unescapeHtml(strResult[i]);
            }
    	}
    	return strResult;
    }


    public String analyzeTitlePath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return null;
    }

    public String[] extractTitlePath(Html html, Html2Xml.Strategy html2xmlStrategy, 
    		Path titlePath, boolean convertUnicodeSimpToTrad) throws ExtractException {
        String json = html.getHtml();
        List<String> additionalTitleList =null;
        if(null!=titlePath.getAdditionalPath())
        {
            additionalTitleList = mXPathHelper.getListString(titlePath.getAdditionalPath(), json);
        }
        
        List<String> nodelist = mXPathHelper.getListString(titlePath.getPath(), json);
        
        int count=nodelist.size();
        String[] strResult = new String[count];
        String[] additionalTitles = new String[count];
        if(null!=additionalTitleList)
        {
            //if have only one additional title,then fill other additional title.
            if(additionalTitleList.size()!=count)
            {
                String additionalTitle = additionalTitleList.get(0).trim();
                for(int i=0;i<count;i++)
                {
                    additionalTitles[i] = additionalTitle; 
                }
            }else
            {
                for(int i=0; i<count; i++)
                {
                    additionalTitles[i]= additionalTitleList.get(i).trim();
                }
            }
        }
        
        for(int i=0; i<count; i++){
            strResult[i] = ContentFormatter
                    .formatTitle(additionalTitles[i] == null ? nodelist.get(i): additionalTitles[i].concat(" ")
                            .concat(nodelist.get(i)));
    		strResult[i] = Utils.getRegExpReplace(strResult[i], titlePath.getRegularExpression(), 
    				titlePath.getReplaceWith(), titlePath.getMatchStrategy());
    		
    		//转换特殊字符
    		strResult[i] = Utils.convertCharactersByConfig(strResult[i]);
    	}
    	return strResult;
    }
    
    public String analyzeAuthorPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return analyzeTitlePath(html, html2xmlStrategy, strategy, nodeInfo);
    }

    public String[] extractAuthorPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path authorPath) throws ExtractException {
        return extractTitlePath(html, html2xmlStrategy, authorPath, false);
    }
    
    public String analyzeSectionPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return analyzeTitlePath(html, html2xmlStrategy, strategy, nodeInfo);
    }

    public String[] extractSectionPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path sectionPath) throws ExtractException {
        return extractTitlePath(html, html2xmlStrategy, sectionPath, false);
    }
    
    public String analyzePagenoPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return analyzeTitlePath(html, html2xmlStrategy, strategy, nodeInfo);
    }

    public String[] extractPagenoPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            Path pagenoPath) throws ExtractException {
        return extractTitlePath(html, html2xmlStrategy, pagenoPath, false);
    }

    public String analyzeDatePath(String html, Html2Xml.Strategy html2xmlStrategy, PathStrategy strategy,
            DateExtractionStrategy deStrategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return null;
    }

    public Date[] extractDatePath(Html html, Html2Xml.Strategy html2xmlStrategy,
    		DatePath datePath, boolean isSingle) throws ExtractException {

        String json = html.getHtml();
    	List<String> nodelist = mXPathHelper.getListString(datePath.getPath(), json);
    	
    	if(isSingle) {
    		if(nodelist.size()==0) throw new ExtractException("Fail to extract date, date node length = 0 ");
    		
    		Date date=null;
    	    ExtractException ex = null;
	        for(int i=0,count=nodelist.size(); i<count; i++) {//如果date_path指定多个结点，取一个有效的结点
	            String dateStr = ContentFormatter.formatTitle(nodelist.get(i));
	            dateStr = Utils.getRegExpReplace(dateStr, datePath.getRegularExpression(), 
	            		datePath.getReplaceWith(), datePath.getMatchStrategy());
	            try {
	            	date=dateParser.extractDate(dateStr, datePath.getDateStrategy(), datePath.getPattern(), datePath.getCountry());
	                if(null!=date)
	            		return new Date[]{date};
	            } catch(ExtractException e) {
	                ex = e;
	                continue;
	            }
	        }
	        
	        //如果date_path指定的结点，没取有效的时间
	        if(null!=ex) //有异常报异常，没异常说明可以支持返回null
	        	throw ex; 
	        else
	        	return new Date[]{null};
    	} else {
    	    Date[] results = new Date[nodelist.size()];
    	    for(int i=0,count=nodelist.size(); i<count; i++) {
                String dateStr = ContentFormatter.formatTitle(nodelist.get(i));
                dateStr = Utils.getRegExpReplace(dateStr, datePath.getRegularExpression(), 
                		datePath.getReplaceWith(), datePath.getMatchStrategy());
                results[i] = dateParser.extractDate(dateStr, datePath.getDateStrategy(), datePath.getPattern(), datePath.getCountry());
            }
            return results;
    	}
    }
    

    public String analyzeReplyNumberPath(String html, Html2Xml.Strategy html2xmlStrategy, PathStrategy strategy, 
    		ReplyNumExtractionStrategy numStrategy, NodeInfo... nodeInfo) throws AnalyzeException{
        return null;
    }

    public int[] extractReplyNumberPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            ReplyNumPath replyPath) throws ExtractException{
        return null;
    	
    }
    
    public String analyzeClickNumberPath(String html, Html2Xml.Strategy html2xmlStrategy, PathStrategy strategy, 
    		ClickNumExtractionStrategy numStrategy, NodeInfo... nodeInfo) throws AnalyzeException{
        return null;
    }

    public int[] extractClickNumberPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            ClickNumPath clickPath) throws ExtractException{
        return null;
    	
    }

    public String analyzeContentPath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return null;
    }

    public String[] extractContentPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            ContentPath contentPath) throws ExtractException {
        
        Html tempHtml = null;
        if (contentPath.isReplaceLabelToDiv())
        {
            tempHtml = new Html(StringUtils.replace(html.getHtml(), HTML_TAG_LABEL, HTML_TAG_DIV));
        }
        else
        {
            tempHtml = html; 
        }
        
        String json = tempHtml.getHtml();
        
        
    	List<String> nodeList = mXPathHelper.getListString(contentPath.getPath(), json);
    	int count = nodeList.size();
    	String[] results = new String[count];
    	for (int i = 0; i < count; i++) {
    		results[i] = nodeList.get(i);
            results[i] = Utils.getRegExpReplace(results[i], contentPath.getRegularExpression(), 
            		contentPath.getReplaceWith(), contentPath.getMatchStrategy());
        }
    	return results;
    }
    
    public String[] extractContentPath(Html html, Html2Xml.Strategy html2xmlStrategy,
            ContentPath contentPath, boolean convertContentUnicodeSimpToTrad) throws ExtractException {
    	String[] results = extractContentPath(html, html2xmlStrategy, contentPath);
    	
    	//转换特殊字符
    	for(int i=0;i<results.length;i++){
    		results[i] = Utils.convertCharactersByConfig(results[i]);
    	}
    	
    	return results;
    }
    
    

    public String analyzeNextPagePath(String html, Html2Xml.Strategy html2xmlStrategy,
            PathStrategy strategy, NodeInfo... nodeInfo) throws AnalyzeException {
        return null;
    }
    
    public String extractNextPagePath(Html html, Html2Xml.Strategy html2xmlStrategy, String charset, String currentUrl,
            String baseUrl, NextPagePath nextpagePath) throws ExtractException {
    	return null;
    }


    // RSS listing page
    public RssArticle[] extractRssArticles(String rss, boolean extractContent, Html2Xml.Strategy html2xmlStrategy,String charset,RssPath rssPath) throws ExtractException {
    	return null;
    }


    @Override
    public String[] extractTitlePath(Html html, Strategy html2xmlStrategy,
            Path titlePath) throws ExtractException
    {
        String xml = html.getXml(html2xmlStrategy);
        List<String> additionalTitleList =null;
        if(null!=titlePath.getAdditionalPath())
        {
            additionalTitleList = mXPathHelper.getListString(titlePath.getAdditionalPath(), xml);
        }
        
        List<String> nodelist = mXPathHelper.getListString(titlePath.getPath(), xml);
        
        int count=nodelist.size();
        String[] strResult = new String[count];
        String[] additionalTitles = new String[count];
        if(null!=additionalTitleList)
        {
            //if have only one additional title,then fill other additional title.
            if(additionalTitleList.size()!=count)
            {
                String additionalTitle = additionalTitleList.get(0).trim();
                for(int i=0;i<count;i++)
                {
                    additionalTitles[i] = additionalTitle; 
                }
            }else
            {
                for(int i=0; i<count; i++)
                {
                    additionalTitles[i]= additionalTitleList.get(i).trim();
                }
            }
        }
        
        for(int i=0; i<count; i++){
            strResult[i] = ContentFormatter
                    .formatTitle(additionalTitles[i] == null ? nodelist.get(i).trim() : additionalTitles[i].concat(" ")
                            .concat(nodelist.get(i).trim()));
            strResult[i] = Utils.getRegExpReplace(strResult[i], titlePath.getRegularExpression(), 
                    titlePath.getReplaceWith(), titlePath.getMatchStrategy());
            
            //转换特殊字符
            strResult[i] = Utils.convertCharactersByConfig(strResult[i]);
        }
        return strResult;

    }


    @Override
    public Map<String, String> extractContentImagesPath(String srcTag,String content,
            String charset, String currentUrl, String baseUrl)
    {
        Map<String, String> images = new HashMap<String,String>();
        //<img width="600" height="479" oldsrc="W020140422439377864641.jpg" src="./W020140422439377864641.jpg" alt="" complete="complete" style="border-right-width: 0px; border-top-width: 0px; border-bottom-width: 0px; border-left-width: 0px">
        //<img alt_src="http://k.sinaimg.cn/n/transform/20150113/1Y9c-avxeafr9901952.jpg/w291h291df3.jpg" alt="" />
        Pattern pattern = null;
        if(StringUtils.isEmpty(srcTag))
        {
            pattern = Pattern.compile(" src=\"(.*?)\"");
        }else
        {
            pattern = Pattern.compile(srcTag+"=\"(.*?)\""); 
        }
        Matcher matcher = pattern.matcher(content);
        while(matcher.find())
        {
            String url = matcher.group(1);
            String url_all=matcher.group(1);
            if(!url_all.startsWith("http://") && !url_all.startsWith("https://"))
            {
                url_all = Utils.calculateLink(baseUrl, url_all, charset);
            }
            images.put(url, url_all);
        }
        return images;

    }


    @Override
    public String[] extractSummaryPath(Html html, Strategy html2xmlStrategy,
            Path summaryPath) throws ExtractException
    {
        return null;
    }
}
