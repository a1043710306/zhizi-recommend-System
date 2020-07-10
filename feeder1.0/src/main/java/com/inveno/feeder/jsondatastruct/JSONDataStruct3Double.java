package com.inveno.feeder.jsondatastruct;

import java.util.Map;

public class JSONDataStruct3Double
{
	private Map<String, Map<String, Map<String, Double>>> jsondata;

	public JSONDataStruct3Double() {}
	
	public void setJsondata(Map<String, Map<String, Map<String, Double>>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, Map<String, Map<String, Double>>> getJsondata()
	{
		return this.jsondata;
	}
	
}