package inveno.spider.reports.dao;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inveno.spider.reports.util.Utils;
import tw.qing.util.DateUtil;

public class OffshelfArticleStatistic {
	
	private int id;
	private int offshelf_code;
	private String offshelf_reason;
	private int static_count;
	private String statistic_date;
	private String language;
	

	
	/**
	 * 
	 * @param offshelf_code    下架code
	 * @param offshelf_reason  下架原因
	 * @param statistic_date   统计日期
	 * @param static_count     统计数量
	 */
	public OffshelfArticleStatistic(int offshelf_code,String offshelf_reason,String statistic_date,int static_count,String language){
		this.setOffshelf_code(offshelf_code);
		this.setOffshelf_reason(offshelf_reason);
		this.setStatic_count(static_count);
		this.setStatistic_date(statistic_date);
		this.setLanguage(language);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOffshelf_code() {
		return offshelf_code;
	}

	public void setOffshelf_code(int offshelf_code) {
		this.offshelf_code = offshelf_code;
	}

	public String getOffshelf_reason() {
		return offshelf_reason;
	}

	public void setOffshelf_reason(String offshelf_reason) {
		this.offshelf_reason = offshelf_reason;
	}

	public int getStatic_count() {
		return static_count;
	}

	public void setStatic_count(int static_count) {
		this.static_count = static_count;
	}

	public String getStatistic_date() {
		return statistic_date;
	}

	public void setStatistic_date(String statistic_date) {
		this.statistic_date = statistic_date;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	
}
