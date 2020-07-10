package com.inveno.feeder.jsondatastruct;

import java.util.List;
import java.util.Map;

public class JSONDataStructL2Double {
	
	private List<Map<String, Map<String, Double>>> jsondata;

	public JSONDataStructL2Double() {
	}
	
	public void setJsondata( List<Map<String, Map<String, Double>>> jsondata) {
		this.jsondata = jsondata;
	}
	
	public List<Map<String, Map<String, Double>>> getJsondata() {
		return this.jsondata;
	}

}