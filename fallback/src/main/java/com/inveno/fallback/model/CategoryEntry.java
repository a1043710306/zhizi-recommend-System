package com.inveno.fallback.model;

public class CategoryEntry {

	private Integer categoryId;
	
	private String intro;
	
	private Integer expiryHour;

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public Integer getExpiryHour() {
		return expiryHour;
	}

	public void setExpiryHour(Integer expiryHour) {
		this.expiryHour = expiryHour;
	}

	@Override
	public String toString() {
		return "CategoryEntry [categoryId=" + categoryId + ", intro=" + intro + ", expiryHour=" + expiryHour + "]";
	}

}
