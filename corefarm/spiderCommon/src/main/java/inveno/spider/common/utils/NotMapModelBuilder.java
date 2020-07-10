package inveno.spider.common.utils;

import java.lang.reflect.Modifier;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class NotMapModelBuilder
{
    private static NotMapModelBuilder instance = null;

    public static synchronized NotMapModelBuilder getInstance()
    {
        if (null == instance)
        {
            instance = new NotMapModelBuilder();
        }

        return instance;
    }

    private GsonBuilder gsonBuilder = null;

    private Gson gson = null;

    private NotMapModelBuilder()
    {
        gsonBuilder = new GsonBuilder();

        gson = gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss")
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .create();
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
    
    public <T> String toJson(Object object, Class<T> clazz)
    {
        return gson.toJson(object, clazz);
    }
}
