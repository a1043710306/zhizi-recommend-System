package com.inveno.core.bean;

import java.io.Serializable;

public class Channelcg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3807261806042109812L;
	
	
	private String channel;
	
	
	private String method;
	
	
	private String category;


	public String getChannel() {
		return channel;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}


	public String getMethod() {
		return method;
	}


	public void setMethod(String method) {
		this.method = method;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}
	
	
	

}
