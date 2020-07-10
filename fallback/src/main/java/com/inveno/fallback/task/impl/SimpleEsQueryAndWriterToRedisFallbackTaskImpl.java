package com.inveno.fallback.task.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.inveno.fallback.service.FallbackService;
import com.inveno.fallback.task.FallbackTask;

public class SimpleEsQueryAndWriterToRedisFallbackTaskImpl implements
		FallbackTask {

	private static final Logger logger = LoggerFactory.getLogger(SimpleEsQueryAndWriterToRedisFallbackTaskImpl.class);
	
	@Autowired
	private FallbackService simpleEsQueryAndWriterToRedisFallbackTaskImpl;
	
	
	public void execute() {
		// TODO Auto-generated method stub
		logger.debug("start execute task...");
		String result = simpleEsQueryAndWriterToRedisFallbackTaskImpl.handler();
		logger.debug("SimpleEsQueryAndWriterToRedisFallbackTaskImpl service result :"+result);       
	}

}
