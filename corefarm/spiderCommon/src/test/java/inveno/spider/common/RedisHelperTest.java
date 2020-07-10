package inveno.spider.common;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class RedisHelperTest
{

    @Test
    public void testSetString()
    {
        RedisHelper.getInstance().set("key-123", "123455");
    }

    @Test
    public void testGetString()
    {
        String value = RedisHelper.getInstance().get("key-123");
        assertEquals("123455", value);
    }
    
    @Test
    public void testSetObject()
    {
        String result = "http://www.baidu.com";
        RedisHelper.getInstance().set("key-1234", result);
    }
    
    @Test
    public void testGetObject()
    {
        String value = RedisHelper.getInstance().getObject("key-1234");
        System.out.println(value);
    }
    
    @Test
    public void testGetObject2()
    {
        List<String> values = RedisHelper.getInstance().getObject(RedisHelper.Key.KEY_QUEUE_LIST.name());
        for(String v:values)
        {
        System.out.println(v);
        }
    }
    
    @Test
    public void testDelete()
    {
        String key="wm_news163cn";
        RedisHelper.getInstance().delete(key); 
    }
    


}
