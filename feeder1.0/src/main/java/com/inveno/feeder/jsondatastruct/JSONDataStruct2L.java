package com.inveno.feeder.jsondatastruct;

import java.util.Map;
import java.util.List;

public class JSONDataStruct2L
{
	private Map<String, List<Map<String, Object>>> jsondata;

	public JSONDataStruct2L() {}
	
	public void setJsondata(Map<String, List<Map<String, Object>>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public Map<String, List<Map<String, Object>>> getJsondata()
	{
		return this.jsondata;
	}
	
}