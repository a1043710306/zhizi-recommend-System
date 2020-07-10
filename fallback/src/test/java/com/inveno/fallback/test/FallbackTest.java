package com.inveno.fallback.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.service.impl.EsQueryAndWriterToRedisServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="classpath:spring/applicationcontext-*.xml")
public class FallbackTest {
	
	@Autowired
	private EsQueryAndWriterToRedisServiceImpl esQueryAndWriterToRedisServiceImpl;

	//sorce计算测试
	@Test
	public void testFallbackServiceScore(){
		List<MessageContentEntry> messageContentEntryList = new ArrayList<MessageContentEntry>();
		for(int i =0;i<5;i++){
			MessageContentEntry messageContentModel = new MessageContentEntry();
			messageContentModel.setDwelltimeValue(2.5+i);
			messageContentModel.setGmpValue(0.4+i);
			messageContentEntryList.add(messageContentModel);
		}
		System.out.println("before:"+messageContentEntryList);
		messageContentEntryList = esQueryAndWriterToRedisServiceImpl.processHandler(messageContentEntryList, 0.35, 0.24);
		System.out.println("after:"+messageContentEntryList);
	}
	
	
}
