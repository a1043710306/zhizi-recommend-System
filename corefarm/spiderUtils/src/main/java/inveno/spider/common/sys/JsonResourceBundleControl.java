package inveno.spider.common.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import inveno.spider.common.util.JsonUtils;

import com.google.gson.*;

public class JsonResourceBundleControl extends ResourceBundle.Control
{
	private static final String CHARSET = "UTF-8";
	private static final String FORMAT_JSON = "json";

	@Override
	public List<String> getFormats(String arg0)
	{
		return Arrays.asList(FORMAT_JSON);
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException
	{
		if (!FORMAT_JSON.equals(format))
		{
			return null;
		}

		String bundleName   = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, format);

		InputStream is = loader.getResourceAsStream(resourceName);
		if (is == null)
		{
			// Can happen, for example, if ResourceBundle searches a '_nl.json' file while you only have a '_nl_NL.json' file
			return null;
		}

		// Read file as String
		BufferedReader br = new BufferedReader( new InputStreamReader(is, CHARSET) );
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
		}
		br.close();

		// Read the json data and iterate through all keys adding the values to a JsonResourceBundle
		JsonElement e = (new Gson()).fromJson(sb.toString(), JsonElement.class);
		JsonObject obj = e.getAsJsonObject();
		JsonResourceBundle rb = new JsonResourceBundle();
		for (Map.Entry<String,JsonElement> entry : obj.entrySet())
		{
			String key = entry.getKey();
			JsonElement ve = (JsonElement)entry.getValue();
			if (ve instanceof JsonPrimitive)
				rb.put(key, JsonUtils.toJavaObject(ve));
			else
			{
				rb.putAll( JsonUtils.toMap(ve, key) );
			}
		}

		return rb;
	}

	private static class JsonResourceBundle extends ResourceBundle
	{
		private Map<String, Object> data = new HashMap<String, Object>();

		@Override
		public Enumeration<String> getKeys()
		{
			return Collections.enumeration(data.keySet());
		}

		@Override
		protected Object handleGetObject(String key)
		{
			return data.get(key);
		}

		public void putAll(Map<String, Object> map)
		{
			data.putAll(map);
		}

		public void put(String key, Object value)
		{
			data.put(key, value);
		}
	}
}
