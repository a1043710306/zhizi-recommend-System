package inveno.spider.parser.utils;

import inveno.spider.parser.base.Page;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MapSerializer implements JsonDeserializer<Map<Object, Object>>
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement,
     * java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
     */
    @Override
    public Map<Object, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject jsonObject = json.getAsJsonObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        for (Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            String keyString = entry.getKey();
            Object key = convert(keyString);
            String value = entry.getValue().getAsString();
            if(key.getClass().getName().contains("Meta") )
            {
                if (keyString.equalsIgnoreCase("date"))
                {
                    map.put(key, parseDate(value));
                } else if (keyString.equalsIgnoreCase("reply") || keyString.equalsIgnoreCase("click") || keyString.equalsIgnoreCase("errorTimes"))
                {
                    map.put(key, parseInt(value));
                } else
                {
                    map.put(key, value);
                }
            }else
            {
                map.put(key, value);
            }
        }

        return map;
    }
    
    private Object convert(String key)
    {
        Page.Meta meta = Page.Meta.convert(key);
        return null!=meta?meta:key;
    }

    private Date parseDate(String dateStr)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            Date date = df.parse(dateStr);
            return new Date(date.getTime());
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Integer parseInt(String value)
    {
        return Integer.valueOf(value);
    }

    // /* (non-Javadoc)
    // * @see com.google.gson.JsonSerializer#serialize(java.lang.Object,
    // java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
    // */
    // @Override
    // public JsonElement serialize(Map<String,Object> src, Type typeOfSrc,
    // JsonSerializationContext context)
    // {
    // return null;
    // }

}
