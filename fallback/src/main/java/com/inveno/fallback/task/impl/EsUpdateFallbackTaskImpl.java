package com.inveno.fallback.task.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.service.FallbackService;
import com.inveno.fallback.task.FallbackTask;

@Component
public class EsUpdateFallbackTaskImpl implements FallbackTask {

	
	private static final Logger logger = LoggerFactory.getLogger(EsUpdateFallbackTaskImpl.class);
	@Autowired
	private FallbackService esUpdateServiceImpl;
	
	//执行任务
	public void execute() {
		// TODO Auto-generated method stub
	   logger.debug("start EsUpdateFallbackTaskImpl execute task...");
       String result = esUpdateServiceImpl.handler();
       logger.debug("EsUpdateFallbackTask service result :"+result);       
	}

}
