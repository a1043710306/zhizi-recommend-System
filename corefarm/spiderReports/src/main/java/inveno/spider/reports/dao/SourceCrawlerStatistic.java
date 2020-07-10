package inveno.spider.reports.dao;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inveno.spider.reports.util.Utils;
import tw.qing.util.DateUtil;

public class SourceCrawlerStatistic {

	private int id;
	private String source;
	private String crawler_type;
	private String source_feeds_host;
	private Date statistic_time;
	private int count;
	
	public SourceCrawlerStatistic(String source,String crawler_type, String source_feeds_host, Date statistic_time,int count){
		this.source = source;
		this.crawler_type = crawler_type;
		this.source_feeds_host = source_feeds_host;
		this.statistic_time = statistic_time;
		this.count = count;
	}
	
	
	public SourceCrawlerStatistic(String source,String crawler_type, String source_feeds_host, String statistic_time,int count){
		this.source = source;
		this.crawler_type = crawler_type;
		this.source_feeds_host = source_feeds_host;
		this.statistic_time = DateUtil.stringToDate(statistic_time);
		this.count = count;
	}
	
	public  Object[] getAttributesArray(String sql){
		Pattern p = Pattern.compile("\\([^\\?]*\\)");
        Matcher m = p.matcher(sql);
        if (m.find()) {
        	String[] atrrList = m.group().replace("(", "").replace(")", "").split(",");
        	return Utils.getAttributesArray(atrrList, ",", this);
		} 
		return null;
	}
	
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public String getCrawler_type() {
		return crawler_type;
	}


	public void setCrawler_type(String crawler_type) {
		this.crawler_type = crawler_type;
	}


	public String getSource_feeds_host() {
		return source_feeds_host;
	}


	public void setSource_feeds_host(String source_feeds_host) {
		this.source_feeds_host = source_feeds_host;
	}


	public Date getStatistic_time() {
		return statistic_time;
	}


	public void setStatistic_time(Date statistic_time) {
		this.statistic_time = statistic_time;
	}


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


}
