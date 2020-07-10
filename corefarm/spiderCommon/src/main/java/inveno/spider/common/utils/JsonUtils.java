package inveno.spider.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;

public class JsonUtils
{
	public static String toJsonString(JsonElement e)
	{
		return NotMapModelBuilder.getInstance().toJson(e);
	}
	public static Map<String, Object> toMap(JsonElement e)
	{
		Map<String, Object> map = null;
		if (e instanceof JsonObject)
		{
			map = new HashMap<String, Object>();
			for (Map.Entry<String, JsonElement> entry : e.getAsJsonObject().entrySet())
			{
				String key = entry.getKey();
				Object value = toJavaObject(entry.getValue());
				map.put(key, value);
			}
		}
		return map;
	}
	public static Object toJavaObject(JsonElement e)
	{
		if (e instanceof JsonArray)
		{
			JsonArray objData = e.getAsJsonArray();
			ArrayList alData = new ArrayList();
			for (JsonElement item : objData)
			{
				alData.add(toJavaObject(item));
			}
			return alData;
		}
		else if (e instanceof JsonObject)
		{
			JsonObject obj = (JsonObject)e;
			Map mData = new HashMap();
			for (Map.Entry<String, JsonElement> entry : obj.entrySet())
			{
				mData.put(entry.getKey(), toJavaObject(entry.getValue()));
			}
			return mData;
		}
		else if (e instanceof JsonPrimitive)
		{
			JsonPrimitive obj = (JsonPrimitive)e;
			if (obj.isBoolean())
			{
				return obj.getAsBoolean();
			}
			else if (obj.isNumber())
			{
				return obj.getAsNumber();
			}
			else
			{
				return obj.getAsString();
			}
		}
		else
		{
			return null;
		}
	}
}