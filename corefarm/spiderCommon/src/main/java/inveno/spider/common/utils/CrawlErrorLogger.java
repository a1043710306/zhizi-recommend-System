package inveno.spider.common.utils;

import org.apache.log4j.Logger;

public class CrawlErrorLogger {
	private static final Logger log = LoggerFactory.make();
	
	public static void log(String message){
		log.info(message);
	}
	
	public static void warn(String message){
		log.info(message);
	}
	
	public static void error(String message){
		log.info(message);
	}
}
