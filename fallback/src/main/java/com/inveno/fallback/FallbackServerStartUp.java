package com.inveno.fallback;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FallbackServerStartUp {
	
	private static final Logger logger = LoggerFactory.getLogger(FallbackServerStartUp.class);
	
	public static void main(String[] args) {
		
		final ServiceLoader<IServiceLoader> loader = ServiceLoader.load(IServiceLoader.class);
		
		try {
			//开始启动
			if(logger.isDebugEnabled()){
				logger.debug("start loader Fallback service IServiceLoader..");
			}
			for(IServiceLoader service : loader){
				service.loadService();
			}
			if(logger.isDebugEnabled()){
				logger.debug("Fallback服务启动成功");
			}
		} catch (Exception e) {
			logger.error("Fallback服务启动失败",e);
			System.exit(1);
		}
		
	}
}
