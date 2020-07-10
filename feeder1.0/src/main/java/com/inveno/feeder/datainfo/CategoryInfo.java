package com.inveno.feeder.datainfo;

import java.io.Serializable;
import java.sql.Timestamp;


public class CategoryInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int version;
	private int contentType;
	private String categoryName;
	private String intro;
	private Timestamp createTime;
	private Integer expiryHour;
	
	public Integer getExpiryHour() {
		return expiryHour;
	}
	public void setExpiryHour(Integer expiryHour) {
		this.expiryHour = expiryHour;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getContentType() {
		return contentType;
	}
	public void setContentType(int _contentType) {
		contentType = _contentType;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	@Override
	public String toString() {
		return "CategoryInfo [id=" + id + ", version=" + version + ", categoryName=" + categoryName + ", intro=" + intro + ", createTime=" + createTime + ", expiryHour=" + expiryHour + "]";
	}
	
	
}