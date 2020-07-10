package com.inveno.feeder.jsondatastruct;

import java.util.Map;

public class JSONDataStruct2
{
	private Map<String, Map<String, Object>> jsondata;

	public JSONDataStruct2() {}
	
	public void setJsondata(Map<String, Map<String, Object>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, Map<String, Object>> getJsondata()
	{
		return this.jsondata;
	}
	
}