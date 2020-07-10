package inveno.spider.reports.dao;

import java.text.Format;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.impl.tool.PrettyPrinter;

import inveno.spider.reports.util.Utils;
import tw.qing.util.DateUtil;

public class BodyImageStatistic {
	private int id;
	private String language;
	private int body_images_count;
	private int state;
	private Date statistic_time;
	private int count;
	
	public BodyImageStatistic(){
		
	}
	
	public BodyImageStatistic( String language,int body_images_count,int state,Date statistic_time,int count){
		this.language = language;
		this.body_images_count = body_images_count;
		this.state = state;
		this.statistic_time = statistic_time;
		this.count = count;
	}
	
	public BodyImageStatistic( String language,int body_images_count,int state,String statistic_time,int count){
		this.language = language;
		this.body_images_count = body_images_count;
		this.state = state;
		this.statistic_time = DateUtil.stringToDate(statistic_time);
		this.count = count;
	}
	
	public String toString(){
		return new Formatter().format("%s %s %s %s %s %s", id,language,body_images_count,state,DateUtil.dateToString(statistic_time,"yyyy-MM-dd"),count).toString();
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
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public int getBody_images_count() {
		return body_images_count;
	}
	public void setBody_images_count(int body_images_count) {
		this.body_images_count = body_images_count;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
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
