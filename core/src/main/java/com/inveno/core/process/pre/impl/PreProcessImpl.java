package com.inveno.core.process.pre.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.inveno.common.bean.Context;
import com.inveno.core.process.pre.IPrePolicy;
import com.inveno.core.process.pre.IPreProcess;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;

/**
 * 
  * @ClassName: BeforeProcessImpl
  * @Description: 前置过滤处理
  * @author huangyiming
  * @date 2016年4月6日 下午3:49:57
  *
 */
public  class PreProcessImpl implements IPreProcess {
	private Log logger = LogFactory.getLog(this.getClass());
	private Log errlogger = LogFactory.getLog("toErrFile");
	
 	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
 	
	private JedisCluster jedisCluster;
	
	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		return threadPoolTaskExecutor;
	}
	
	@Override
 	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}
	
	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	@Override
	public boolean process(Context context)  {
		
		if (context.getRecommendInfoData() != null) {
			
			String uid =  context.getUid();
			if(logger.isTraceEnabled()){
				logger.debug("uid is :"+ uid +",PreProcessImpl,result is " + context.getResponseParamList() +",and cur = " + System.currentTimeMillis() );
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("uid is: "+ uid +"  ,begin PreProcessImpl,size is " + context.getResponseParamList().size() +",and cur: " + System.currentTimeMillis() );
			}
			
			long cur = System.currentTimeMillis();
			long end  = System.currentTimeMillis();
			end  = System.currentTimeMillis();
			
			List<String> classNameList = new ArrayList<String>();
			String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "preclass");
			
			if (StringUtils.isNotEmpty(strClass)) {
				classNameList = Arrays.asList(strClass.split(";"));
			}
			if(!strClass.contains("initInterestExploreAndBoostStrategyPolicy")){
				classNameList.add("initInterestExploreAndBoostStrategyPolicy");
			}
	 		
	 		if(logger.isDebugEnabled()){
	 			logger.debug("begin PreProcessImpl ,the Pre classNameList is:" + classNameList 
	 					+" ,and uid: " + context.getUid() +" ,abtest :" + context.getAbtestVersion() 
	 					+" time is " + (System.currentTimeMillis()-cur) +" ,and cur: " + System.currentTimeMillis()	);
	 		}
	 		if( logger.isDebugEnabled() ){
				logger.debug(" uid is "  + context.getUid() + " abtest "+  context.getAbtestVersion() 
						+"  threadPoolTaskExecutor msg:  corepoolSize" + threadPoolTaskExecutor.getCorePoolSize() 
						+ " ," + threadPoolTaskExecutor.getThreadPoolExecutor().getCorePoolSize()
						+"," + threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size());
			}

			for (String className : classNameList) {
				IPrePolicy<List<ResponParam>> policy = (IPrePolicy<List<ResponParam>>) SysUtil.getBean(className);
				try {
					policy.process(context);
				} catch (TException e) {
					logger.error("===  policy.process invoke PreProcessImpl Exception,and abtest " + context.getAbtestVersion() + " uid is :" + uid + "===", e);
					errlogger.error("===  policy.process invoke PreProcessImpl Exception,and abtest " + context.getAbtestVersion() + " uid is :" + uid + "===" + e);
				} finally {
					if (logger.isDebugEnabled()) {
						logger.debug("uid is: " + uid + " time is : " + +(System.currentTimeMillis() - cur) + " ,and cur = " + System.currentTimeMillis());
					}
				}
	 		}
			
			end  = System.currentTimeMillis();
			logger.info("uid is: "+ uid +" ,end readFile PreProcessImpl,end PreProcessImpl time is "
					+ (end-cur) +",and size is " + context.getResponseParamList().size() +",and cur: " + System.currentTimeMillis() );
			
			if(logger.isTraceEnabled()){
				logger.trace("uid is: "+ uid +" end readFile PreProcessImpl time is,result is " + context.getResponseParamList() +",and cur: " + System.currentTimeMillis() );
			}
			
			if( context.getResponseParamList().size() == 0 ){
				logger.warn("uid is : "+ uid +" primary selection returns empty list.");
				errlogger.warn("uid is : "+ uid +" primary selection returns empty list.");
			}
		}
		return true;
	}
}
