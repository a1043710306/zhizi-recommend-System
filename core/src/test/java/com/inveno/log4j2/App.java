/*package com.inveno.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
	
	    static Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	    static Logger qcnlogger = LogManager.getLogger("qcnlog");
	    
	    static public Logger reqLog = LogManager.getLogger("reqLog");
		  
		static public Logger resLog = LogManager.getLogger("resLog");  
		  
		static public Logger qcnReqLog = LogManager.getLogger("qcnreqLog");  
		  
		static public Logger qcnResLog = LogManager.getLogger("qcnresLog");  
		  
		static public Logger qbresLog = LogManager.getLogger("qbresLog");  
	 	
		static public Logger monitorlog = LogManager.getLogger("monitorlog");  
		
		static public Logger cmslog = LogManager.getLogger("cmslog");  
	    
	    public static void main(String[] args) {
	    	qcnlogger.info(" adfdfjjj {} {}",1232131,"qcnlogger");
	    	
	    	reqLog.info(" adfdfjjj {} {}",1232131,"reqLog");
	    	resLog.info(" adfdfjjj {} {}",1232131,"resLog");
	    	qcnReqLog.info(" adfdfjjj {} {}",1232131,"qcnReqLog");
	    	qcnResLog.info(" adfdfjjj {} {}",1232131,"qcnResLog");
	    	qbresLog.info(" adfdfjjj {} {}",1232131,"qbresLog");
	    	
	    	monitorlog.info(" adfdfjjj {} {}",1232131,"monitorlog");
	    	
	    	cmslog.info(" adfdfjjj {} {}",1232131,"cmslog");
	    	
	    	for (int i = 0; i < 10; i++) {
				
	    		logger.trace("trace message");
		        logger.debug("debug message");
		        logger.info("info message");
		        logger.warn("warn message");
		        logger.error("error message");
		        logger.fatal("fatal message");
		        System.out.println("Hello World!");
			}
	        
	    }

}
*/