package com.inveno.feeder.jsondatastruct;

import java.util.Map;

public class JSONDataStruct2Double
{
	private Map<String, Map<String, Double>> jsondata;

	public JSONDataStruct2Double() {}
	
	public void setJsondata(Map<String, Map<String, Double>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, Map<String, Double>> getJsondata()
	{
		return this.jsondata;
	}
	
}