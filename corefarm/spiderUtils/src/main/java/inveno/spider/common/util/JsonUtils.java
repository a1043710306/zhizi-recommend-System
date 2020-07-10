package inveno.spider.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;

public class JsonUtils
{
	public static String toJsonString(JsonElement e)
	{
		return (new GsonBuilder().create()).toJson(e);
	}
	public static Map<String, Object> toMap(JsonElement e)
	{
		return toMap(e, null);
	}
	public static Map<String, Object> toMap(JsonElement e, String prefix)
	{
		Map<String, Object> map = null;
		if (e instanceof JsonObject)
		{
			map = new HashMap<String, Object>();
			for (Map.Entry<String, JsonElement> entry : e.getAsJsonObject().entrySet())
			{
				String key = entry.getKey();
				Object value = toJavaObject(entry.getValue(), key);
				if (null != prefix)
					key = prefix + "." + key;
				map.put(key, value);
			}
		}
		return map;
	}
	public static Object toJavaObject(JsonElement e)
	{
		return toJavaObject(e, null);
	}
	public static Object toJavaObject(JsonElement e, String prefix)
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
		else if (e instanceof JsonPrimitive)
		{
			JsonPrimitive objData = e.getAsJsonPrimitive();
			if (objData.isBoolean())
				return objData.getAsBoolean();
			else if (objData.isNumber())
				return objData.getAsNumber();
			else
				return objData.getAsString();
		}
		else
		{
			return toMap(e, prefix);
		}
	}
}
