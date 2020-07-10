package com.inveno.core.listener;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.inveno.core.sever.TriggerCache;
 
/**
 * 任务监听器
 *
 * 2016-1-07
 * @author liyuanyi
 */
public class ScheduleListener implements ServletContextListener {
	
	public static  Log LOG = LogFactory.getLog(ScheduleListener.class);
	
	public void contextDestroyed(ServletContextEvent event) {
		 
	}
	
	public void contextInitialized(ServletContextEvent event) {
		LOG.info("begin  start zhiziCore ....");

		try {
			TriggerCache.updateCache();
		} catch (Exception e) {
			LOG.error("contextInitialized update cache error",e);
		}
	}

}
