package com.inveno.fallback.qconf.impl;

import net.qihoo.qconf.Qconf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.inveno.fallback.qconf.FallbackQconf;


@Component
public class FallbackQconfImpl implements FallbackQconf {

	private static final Logger logger = LoggerFactory.getLogger(FallbackQconfImpl.class);
	
	public Double getValue(String key) {
		// TODO Auto-generated method stub
		Double result = null;
		try {
			//不能在win上运行
			if( !System.getProperties().getProperty("os.name").startsWith("Windows") ){
				result =  Double.parseDouble(Qconf.getConf(key));
			}
		} catch (Exception e) {
			logger.error("get Qconf exception ", e);
		}
		return result;
	}
}
