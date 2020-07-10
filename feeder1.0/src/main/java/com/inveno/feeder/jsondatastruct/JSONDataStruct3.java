package com.inveno.feeder.jsondatastruct;

import java.util.Map;

public class JSONDataStruct3
{
	private Map<String, Map<String, Map<String, Object>>> jsondata;

	public JSONDataStruct3() {}
	
	public void setJsondata(Map<String, Map<String, Map<String, Object>>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, Map<String, Map<String, Object>>> getJsondata()
	{
		return this.jsondata;
	}
	
}