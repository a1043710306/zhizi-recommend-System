package inveno.spider.common.utils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.log4j.Logger;

public class ModelBuilder
{
    private static final Logger log = Logger.getLogger(ModelBuilder.class);

    private static volatile ModelBuilder instance = null;

    public static synchronized ModelBuilder getInstance()
    {
        if (null == instance)
        {
            instance = new ModelBuilder();
        }

        return instance;
    }

    private GsonBuilder gsonBuilder = null;

    private Gson gson = null;

    private ModelBuilder()
    {
        gsonBuilder = new GsonBuilder();

        try
        {
            Class<?> pageMetaClazz = Class.forName("inveno.spider.parser.base.Page$Meta");
            Object pageMetaSerializer = (Class.forName("inveno.spider.parser.utils.PageMetaSerializer")).newInstance();
            Object mapSerializer = (Class.forName("inveno.spider.parser.utils.MapSerializer")).newInstance();
            gson = gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                    .registerTypeAdapter(pageMetaClazz, pageMetaSerializer)
                    .registerTypeAdapter(Map.class, mapSerializer)
                    .disableHtmlEscaping()
                    .create();
        }
        catch (Exception e) 
        {
            log.fatal("[ModelBuilder]", e);
        }
    }

    public <T> T build(String json, Class<T> clazz)
    {
        T model = gson.fromJson(json, clazz);
        
        return model;
    }

    public <T extends Collection<U>, U> T buildList(String json, TypeToken<T> typeToken)
    {
        T objectList = gson.fromJson(json, typeToken.getType());

        return objectList;
    }

    public String toJson(Object object)
    {
        return gson.toJson(object);
    }

}
