package com.inveno.feeder.datainfo;

import java.io.Serializable;
import java.util.Date;

public class SourceSubscriptionEntry implements Serializable{

	private static final long serialVersionUID = 1L;
	//主键ID
	private Integer id;
	//订阅ID
	private String subscribeId;
	//源名称
	private String rssName;
	//渠道名称
	private String channelName;
	//创建时间
	private Date createTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSubscribeId() {
		return subscribeId;
	}
	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	public String getRssName() {
		return rssName;
	}
	public void setRssName(String rssName) {
		this.rssName = rssName;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public String toString(){
		return "id="+id+",subscribeId="+subscribeId+",rssName="+rssName+",channelName="+channelName+",createTime="+createTime;
	}
	
}
