package com.inveno.zhiziArticleFilter;

import java.io.Serializable;

public class ArticleProperty implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String content_id;
	public String product_id;
	
	public long publish_timestamp;
	public double adult_score;
	public String firm_app;
	
	public String type;
	public String link_type;
	public String display_type;
	
	public String language;
	
	
	public String getContent_id() {
		return content_id;
	}
	public void setContent_id(String content_id) {
		this.content_id = content_id;
	}
	
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	
	public long getPublish_timestamp() {
		return publish_timestamp;
	}
	public void setPublish_timestamp(long publish_timestamp) {
		this.publish_timestamp = publish_timestamp;
	}
	
	public double getAdult_score() {
		return adult_score;
	}
	public void setAdult_score(double adult_score) {
		this.adult_score = adult_score;
	}
	
	public String getFirm_app() {
		return firm_app;
	}
	public void setFirm_app(String firm_app) {
		this.firm_app = firm_app;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLink_type() {
		return link_type;
	}
	public void setLink_type(String link_type) {
		this.link_type = link_type;
	}
	
	public String getDisplay_type() {
		return display_type;
	}
	public void setDisplay_type(String display_type) {
		this.display_type = display_type;
	}
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	
}