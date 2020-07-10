package com.inveno.junit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inveno.common.handler.RedisClusterUtil;
 
/**
 * JUnit4 + Spring 注解进行单元测试，测试通过Spring注解获得Properties文件的值
 * @author huangyiming
 *
 * @version $id:ConfigPropertyTest.java,v 0.1 2015年8月7日 下午2:21:26 luolin Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:spring-mvc.xml"})
public class JunitTest {
 
    
     
    /**
     * 测试Spring注解获取properties文件的值
     */
    @Test
    public void test() {
    	RedisClusterUtil.getInstance().set("zhizi:toplist:total:tm", (System.currentTimeMillis()) + "" );
		RedisClusterUtil.getInstance().rpush("zhizi:toplist:total", "88", "99", "10");
		
		System.out.println(RedisClusterUtil.getInstance().get("zhizi:toplist:total:tm"));
    }
 
}

