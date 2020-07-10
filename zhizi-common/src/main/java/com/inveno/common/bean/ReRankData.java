package com.inveno.common.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.inveno.thrift.ResponParam;

public class ReRankData implements Serializable {

	public ResponParam responParam;
	public Set<String> tags = new HashSet<String>();

	public ResponParam getResponParam() {
		return responParam;
	}

	public void setResponParam(ResponParam responParam) {
		this.responParam = responParam;
	}

	public Set<String> getTags() {
		return tags;
	}
	

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	
}
