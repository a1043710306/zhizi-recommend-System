package inveno.spider.rmq.http;

import static org.junit.Assert.*;
import inveno.spider.common.Constants;
import inveno.spider.common.utils.ModelBuilder;
import inveno.spider.rmq.http.AuthorizationException;
import inveno.spider.rmq.http.HttpService;
import inveno.spider.rmq.http.HttpServiceImpl;
import inveno.spider.rmq.model.RMQAPI;
import inveno.spider.rmq.model.RMQQueue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;


/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public class HttpServiceImplTest
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    /**
     * Test method for
     * {@link inveno.spider.rmq.http.HttpServiceImpl#get(java.util.String,java.util.String,java.util.String)}.
     */
    @Test
    public void testGet()
    {
        HttpService httpService = new HttpServiceImpl();
        try
        {
           String result =  httpService.get(Constants.RABBIT_USERNAME, Constants.RABBIT_PASSWORD, RMQAPI.API_GET_QUEUE);
           System.out.println(result);

        } catch (AuthorizationException e)
        {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testToJson()
    {
        RMQQueue q = new RMQQueue("a",20);
        List<RMQQueue> list = new ArrayList<RMQQueue>();
        list.add(q);
        System.out.println(ModelBuilder.getInstance().toJson(list));
    }
    @Test
    public void testJsonToObject()
    {
        String json="[{\"name\":\"a\",\"messages\":20,\"te\":xx}]";
        List<RMQQueue> list = new ArrayList<RMQQueue>();
        
        TypeToken<List<RMQQueue>> typeToken = new TypeToken<List<RMQQueue>>() { };
        list = ModelBuilder.getInstance().buildList(json, typeToken);
        
        for(RMQQueue q:list)
        {
            System.out.println(q.getName());
        }
    }

}
