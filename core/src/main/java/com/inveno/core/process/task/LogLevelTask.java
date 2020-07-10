/*package com.inveno.core.process.task;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("logLevelTask")
@Deprecated
public class LogLevelTask {
	
	private Log logger =  LogFactory.getLog("cmslog");
	
	@Scheduled(cron="* 0/5 * * * ? ")
	public void changeLogLv(){
		logger.info(" begin changeLogLv");
		Calendar ca = Calendar.getInstance();
		int minute = ca.get(Calendar.MINUTE);
		int a = minute/10;
		Level lv = Level.ERROR;
		if( a == 0 ){
			lv = Level.INFO; 
		}else if ( a >= 1 &&  a <=3) {
			lv = Level.TRACE;
		}else if (a>= 4 && a <= 5) {
			lv = Level.ERROR;
		}else {
			lv = Level.WARN;
		}
		
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration log4jCfg = ctx.getConfiguration();
		LoggerConfig rootLoggerCfg = log4jCfg.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		rootLoggerCfg.setLevel(lv);
		
		logger.info(" begin changeLogLv lv is :" + lv);
		
	}

}
*/