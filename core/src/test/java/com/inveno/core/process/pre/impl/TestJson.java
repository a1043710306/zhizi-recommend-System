package com.inveno.core.process.pre.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class TestJson {
	
	public static void main(String[] args) {
		
		
		List<Integer> categoryIds = new ArrayList<Integer>();
		String json = "{\"categoryId\":[\"\"],\"rank\":10}";
		JSONObject jsonObject = JSONObject.parseObject(json);
		System.out.println(jsonObject.getJSONArray("categoryId").toString());
		categoryIds = JSON.parseArray(jsonObject.getJSONArray("categoryId").toString(), Integer.class);
		
		System.out.println(categoryIds);
	}

}
