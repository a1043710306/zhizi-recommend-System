package com.inveno.core.process.post.process;

import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inveno.thrift.InitProcessImplTest;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations = { "classpath:applicationContext-resources.xml", "classpath:spring-mvc.xml",
		"classpath:applicationContext-trigger.xml","classpath:applicationContext-ehcache.xml" })
public class GroupIdControlTest {
	
	@Test
	public void bTest() throws TException{
		InitProcessImplTest.testInitProcess();
	}

}
