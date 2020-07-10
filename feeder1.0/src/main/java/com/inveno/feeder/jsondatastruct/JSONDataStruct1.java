package com.inveno.feeder.jsondatastruct;

import java.util.Map;

public class JSONDataStruct1
{
	private Map<String, Object> jsondata;

	public JSONDataStruct1() {}
	
	public void setJsondata(Map<String, Object> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, Object> getJsondata()
	{
		return this.jsondata;
	}
	
}