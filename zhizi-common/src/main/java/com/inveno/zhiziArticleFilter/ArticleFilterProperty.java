package com.inveno.zhiziArticleFilter;

import java.io.Serializable;

import com.inveno.thrift.ZhiziListReq;

public class ArticleFilterProperty implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double 	adult_score_upperbound;
	private long 	publish_time_expired_before;
	
	private ZhiziListReq zhizi_request;
	
	
	public void setAdultScoreUpperbound(double score) {
		this.adult_score_upperbound = score;
	}
	public double getAdultScoreUpperbound() {
		return this.adult_score_upperbound;
	}
	
	public void setPublishTimeExpiredBefore(long time_before) {
		this.publish_time_expired_before = time_before;
	}
	public long getPublishTimeExpiredBefore() {
		return this.publish_time_expired_before;
	}
	
	public ZhiziListReq getZhizi_request() {
		return zhizi_request;
	}
	public void setZhizi_request(ZhiziListReq zhizi_request) {
		this.zhizi_request = zhizi_request;
	}
	
}