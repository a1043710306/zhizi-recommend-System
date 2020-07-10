package com.inveno.common.bean;

import java.io.Serializable;

public class StgInfo implements Serializable {
	
	String infoId;
	
	int position;
	
	public StgInfo(){}

	public StgInfo(String infoId, int position) {
		super();
		this.infoId = infoId;
		this.position = position;
	}

	public String getInfoId() {
		return infoId;
	}

	public void setInfoId(String infoId) {
		this.infoId = infoId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public int hashCode() {
 		return Integer.parseInt(this.getInfoId()) ;
	}

}
