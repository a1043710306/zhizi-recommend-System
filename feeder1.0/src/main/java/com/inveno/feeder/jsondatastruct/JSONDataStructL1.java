package com.inveno.feeder.jsondatastruct;

import java.util.List;
import java.util.Map;

public class JSONDataStructL1
{
	private List<Map<String, String>> jsondata;

	public JSONDataStructL1() {}
	
	public void setJsondata(List<Map<String, String>> jsondata)
	{
		this.jsondata = jsondata;
	}
	public List<Map<String, String>> getJsondata()
	{
		return this.jsondata;
	}
	
}