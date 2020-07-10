package com.inveno.fallback.model;

import java.io.Serializable;

import com.inveno.fallback.constant.FallbackConstant;

public class MessageContentEntry implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String groupId;
	
	private Integer publishTimeTs;
	
	private Double gmpValue;
	
	private Double dwelltimeValue;
	
	private Double score;
	
	private Integer type;
	
	private Integer linkType;
	
	private Integer displayType;
	
	private String contentType;
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getLinkType() {
		return linkType;
	}
	public void setLinkType(Integer linkType) {
		this.linkType = linkType;
	}
	public Integer getDisplayType() {
		return displayType;
	}
	public void setDisplayType(Integer displayType) {
		this.displayType = displayType;
	}
	public Double getDwelltimeValue() {
		return dwelltimeValue;
	}
	public void setDwelltimeValue(Double dwelltimeValue) {
		this.dwelltimeValue = dwelltimeValue;
	}
	
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getPublishTimeTs() {
		return publishTimeTs;
	}
	public void setPublishTimeTs(Integer publishTimeTs) {
		this.publishTimeTs = publishTimeTs;
	}
	
	public Double getGmpValue() {
		return gmpValue;
	}
	public void setGmpValue(Double gmpValue) {
		this.gmpValue = gmpValue;
	}
	
	@Override
	public String toString() {
		return  "publishtime=" +publishTimeTs +",Score="+score+",member="+groupId+FallbackConstant.FALLBACK_SUFFIX_WELL+contentType
				+FallbackConstant.FALLBACK_SUFFIX_WELL+linkType+FallbackConstant.FALLBACK_SUFFIX_WELL+displayType;
	}
	
}
