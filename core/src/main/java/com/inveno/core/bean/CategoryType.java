package com.inveno.core.bean;

import java.io.Serializable;
import java.util.Date;

public class CategoryType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2431423162154075674L;
	
	private int id;
	
	
	private String category_name;
	
	
	private String intro;
	
	
	private Date create_time;
	
	
	private int type;


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getCategory_name() {
		return category_name;
	}


	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}


	public String getIntro() {
		return intro;
	}


	public void setIntro(String intro) {
		this.intro = intro;
	}


	public Date getCreate_time() {
		return create_time;
	}


	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}
	
	
	

}
