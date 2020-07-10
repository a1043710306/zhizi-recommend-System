package com.inveno.fallback.task.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.service.FallbackService;
import com.inveno.fallback.task.FallbackTask;

@Component
public class EsQueryAndWriterToRedisFallbackTaskImpl implements FallbackTask {

private static final Logger logger = LoggerFactory.getLogger(EsQueryAndWriterToRedisFallbackTaskImpl.class);
	
	@Autowired
	private FallbackService esQueryAndWriterToRedisServiceImpl;
	public void execute() {
		// TODO Auto-generated method stub
		logger.debug("start execute task...");
		String result = esQueryAndWriterToRedisServiceImpl.handler();
		logger.debug("EsQueryAndWriterToRedisFallbackTaskImpl service result :"+result);
	}

}
