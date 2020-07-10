package inveno.spider.reports.dao;

import java.util.Date;

import tw.qing.util.DateUtil;

public class ProductCrawlerStatistic {

	private int id;
	private String firmapp;
	private String type;
	private Date statistic_time;
	private int count;
	
	public ProductCrawlerStatistic(String firmapp,String type,Date statistic_time,int count){
		this.firmapp = firmapp;
		this.type = type;
		this.statistic_time = statistic_time;
		this.count = count;
	}
	
	
	public ProductCrawlerStatistic(String firmapp,String type,String statistic_time,int count){
		this.firmapp = firmapp;
		this.type = type;
		this.statistic_time = DateUtil.stringToDate(statistic_time);
		this.count = count;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirmapp() {
		return firmapp;
	}
	public void setFirmapp(String firmapp) {
		this.firmapp = firmapp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
