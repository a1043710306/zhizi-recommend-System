package com.inveno.core.bean;

import java.io.Serializable;

public class ChannelSDK implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2097736810975990117L;

	/**
	 * '渠道|国家|语言'
	 */
	private String channelkey;
	
	/**
	 * '频道分类'
	 */
	private String jvalue;

	public String getChannelkey() {
		return channelkey;
	}

	public void setChannelkey(String channelkey) {
		this.channelkey = channelkey;
	}

	public String getJvalue() {
		return jvalue;
	}

	public void setJvalue(String jvalue) {
		this.jvalue = jvalue;
	}
	
	
	
	
	
}
