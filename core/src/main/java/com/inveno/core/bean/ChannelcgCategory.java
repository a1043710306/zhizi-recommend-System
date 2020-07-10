package com.inveno.core.bean;

import java.io.Serializable;
import java.util.List;

public class ChannelcgCategory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -841943031410514922L;
	
	
	private List<Category> ChannelCategoryReq;
	
	
	private String Interface;


	public List<Category> getChannelCategoryReq() {
		return ChannelCategoryReq;
	}


	public void setChannelCategoryReq(List<Category> channelCategoryReq) {
		ChannelCategoryReq = channelCategoryReq;
	}


	public String getInterface() {
		return Interface;
	}


	public void setInterface(String interface1) {
		Interface = interface1;
	}
	
	
	

}
