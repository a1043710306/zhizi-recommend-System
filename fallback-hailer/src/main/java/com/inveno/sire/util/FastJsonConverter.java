package com.inveno.sire.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FastJsonConverter {

	public static String writeValue(Object obj) {
		return JSON.toJSONString(obj,new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect });
	}

	public static <T> T readValue(Class<T> type, String json) {
		return JSON.parseObject(json,type);
	}
	
}
