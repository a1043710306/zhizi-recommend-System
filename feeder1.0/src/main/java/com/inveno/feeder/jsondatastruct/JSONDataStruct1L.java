package com.inveno.feeder.jsondatastruct;

import java.util.List;
import java.util.Map;

public class JSONDataStruct1L
{
	private Map<String, List<String>> jsondata;

	public JSONDataStruct1L() {}
	
	public void setJsondata(Map<String, List<String>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, List<String>> getJsondata()
	{
		return this.jsondata;
	}
	
}