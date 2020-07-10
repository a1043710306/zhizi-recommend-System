package inveno.spider.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import inveno.spider.common.model.Content;
import inveno.spider.common.model.Img;
import inveno.spider.rmq.model.RMQQueue;
import inveno.spider.rmq.service.RMQQueueService;
import inveno.spider.rmq.service.impl.RMQQueueServiceImpl;

public class RabbitmqHelperTest
{
    private static final String QUEUE_NAME="normal_news_meishij";

    @Test
    public void testSendMessage()
    {
       String result = "<html><body>我是谁</body></html>";
     //RabbitmqHelper.getInstance().sendMessage("hello",result);
    }

    @Test
    public void testGetMessage()
    {
        
//     Page page = RabbitmqHelper.getInstance().getMessage(QUEUE_NAME,Page.class);
//     System.out.println(page);
    }
    
    @Test
    public void testGetMessageNonblocking()
    {
//       Page page = RabbitmqHelper.getInstance().getMessageNonblocking(QUEUE_NAME,Page.class);
//       System.out.println(page);
    }
    
    @Test
    public void testGetCount()
    {
        int count = RabbitmqHelper.getInstance().getCount("hello");
        System.out.println(count);
    }
    
    @Test
    public void testCleanerQueue()
    {
        long start=System.currentTimeMillis();
        List<String> queueList = new ArrayList<String>();
        RMQQueueService service = new RMQQueueServiceImpl();
        List<RMQQueue> list = service.loadCrawlQueue();
        System.out.println("==========size:"+list.size()+"========="+(System.currentTimeMillis()-start));
        for(RMQQueue q:list)
        {
            //System.out.println(q.getName());
            if(q.getCount()>0)
            {
                queueList.add(q.getName());
            }
        }
        
        //queueList = RedisHelper.getInstance().getObject(RedisHelper.Key.KEY_QUEUE_LIST.name());
        if(null==queueList || queueList.size()==0)
        {
            return;
        }
        
        for(String queue:queueList)
        {
//            RabbitmqHelper.getInstance().getMessagesNonblocking(queue,1000,Page.class);
            System.out.println(queue);
        }
    }
    
    public static void main(String[] args)
    {
        long start=System.currentTimeMillis();
        List<String> queueList = new ArrayList<String>();
        RMQQueueService service = new RMQQueueServiceImpl();
        List<RMQQueue> list = service.loadCrawlQueue();
        System.out.println("==========size:"+list.size()+"========="+(System.currentTimeMillis()-start));
        for(RMQQueue q:list)
        {
            //System.out.println(q.getName());
            if(q.getCount()>0)
            {
                queueList.add(q.getName());
            }
        }
        
        //queueList = RedisHelper.getInstance().getObject(RedisHelper.Key.KEY_QUEUE_LIST.name());
        if(null==queueList || queueList.size()==0)
        {
            return;
        }
        
        for(String queue:queueList)
        {
//            RabbitmqHelper.getInstance().getMessagesNonblocking(queue,1000,Page.class);
            System.out.println(queue);
        }
    }
    
    
    @Test
    public void testString2Object() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("{")
        .append("\"v1\":[")
          .append("{")
            .append("\"str\":\"The Tonight Show\"")
          .append("},")
          .append("{")
            .append("\"str\":\"Jimmy Fallon\"")
          .append("}")
        .append("]")
      .append("}");
    	System.out.println(sb.toString());
    	HashMap map = JSON.parseObject(sb.toString(), HashMap.class);
    	System.out.println(map);
    	Content content =new Content();
    	String json = content.getObjectToJson(map);
    	System.out.println(json);
    	
    	String aa = "[{\"src\":\"http://cloudimg.hotoday.in/v1/icon?id=1128672772848446399&size=980*617&fmt=.jpeg\",\"format\":\"jpeg\",\"width\":980,\"height\":617,\"desc\":\"\"}]";
		JSONArray array = JSONArray.parseArray(aa);
		Img[] tArray = new Img[array.size()];
		for(int i=0;i<array.size();i++) {
			tArray[i] = JSONObject.toJavaObject(array.getJSONObject(i), Img.class);
			System.out.println(tArray[i].toString());
		}
		System.out.println(tArray.length);
    }
    
    public void testObject2String() {
    	Content content = new Content();
    	content.setContentId("id-123");
    	content.setKeywords("{}");
    }

}
