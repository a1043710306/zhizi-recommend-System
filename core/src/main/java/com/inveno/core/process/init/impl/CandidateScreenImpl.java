package com.inveno.core.process.init.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.inveno.common.bean.Context;
import com.inveno.core.process.init.ICandidateScreen;
import com.inveno.core.process.init.PolicyInterface;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

public class CandidateScreenImpl implements ICandidateScreen {
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	private Log errlogger = LogFactory.getLog("toErrFile");
	
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		return threadPoolTaskExecutor;
	}
	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}

	public boolean process(Context context) {
		String uid = context.getUid();
		List<String> classNameList = new ArrayList<String>();
		classNameList.add("primarySelectionImpl");
		long cur = System.currentTimeMillis();

		String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "initclass");
		if (StringUtils.isEmpty(strClass)) {
			strClass = context.getComponentConfiguration("default/initclass");
		}
		if (StringUtils.isNotEmpty(strClass)) {
			classNameList = Arrays.asList(strClass.split(";"));
		}
		
		if (logger.isDebugEnabled()) {
			long end  = System.currentTimeMillis();
			logger.debug("begin CandidateScreenImpl ,the policy classNameList is:" + classNameList +" ,and uid: " + context.getUid() 
					+" ,abtest :" + context.getAbtestVersion() 
					+" time is " + (end-cur) +" ,and cur: " + System.currentTimeMillis()	);
		}
		
		List<ResponParam> resultList = new ArrayList<ResponParam>();
		
		List<FutureTask<List<ResponParam>>> taskList = new ArrayList<FutureTask<List<ResponParam>>>();  
		CountDownLatch latch = new CountDownLatch(classNameList.size());
		for (String className : classNameList) {
			FutureTask<List<ResponParam>> ft = new FutureTask<List<ResponParam>>(() -> {
				PolicyInterface<List<ResponParam>> policy = (PolicyInterface<List<ResponParam>>)SysUtil.getBean(className);
				List<ResponParam> list = new ArrayList<ResponParam>();
				try {
					list = policy.process(context);
				} catch (Exception e) {
					logger.error("===  policy.process invoke gmp Exception,and abtest "+  context.getAbtestVersion() +" uid is :"+ uid +"===",e);
					errlogger.error("uid is :"+ uid +"  process Exception," + e);
				} finally {
					if (logger.isDebugEnabled()) {
						long end  = System.currentTimeMillis();
						logger.debug("uid is: "+ uid +" time is : " +  + (end-cur) +" ,and cur = " + System.currentTimeMillis());
					}
					latch.countDown();
				}
				return list;
			});
			taskList.add(ft);
			threadPoolTaskExecutor.submit(ft);
		}
		     
		if(logger.isDebugEnabled()){
			long end  = System.currentTimeMillis();
			logger.debug("the policy taskList is :" + taskList +"and uid:" + context.getUid() +",abtest :" + context. getAbtestVersion() 
					+" time is " + (end-cur)  +",and cur = " + System.currentTimeMillis()	);
		}
		
		int iCandidateScreenTimeOut = NumberUtils.toInt(context.getComponentConfiguration("timeOut/candidateScreenTimeOut"), 250);
		
		try {
			latch.await(iCandidateScreenTimeOut, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			logger.error("uid is :"+ uid +"  InterruptedException ", e1);
		}

		boolean bSuccess = true;
		for (FutureTask<List<ResponParam>> futureTask : taskList) {
			try {
				List<ResponParam> list = futureTask.get(10, TimeUnit.MILLISECONDS);
				if (CollectionUtils.isNotEmpty(list)) {
					resultList.addAll(list);
				}
			} catch (InterruptedException e) {
				logger.warn("uid is: "+ uid +" CandidateScreenImpl and uid is: "+ context.getUid() +" futureTask InterruptedException and " ,e);
				errlogger.error("uid is :"+ uid +" happen error :" + e);
				bSuccess = false;
			} catch (ExecutionException e) {
				logger.warn("uid is: "+ uid +" CandidateScreenImpl and uid is: "+ context.getUid() +" futureTask ExecutionException and " ,e);
				errlogger.error("uid is :"+ uid +" happen error :" + e);
				bSuccess = false;
			} catch (TimeoutException e) {
				logger.warn("uid is: "+ uid +" CandidateScreenImpl and uid is: "+ context.getUid() +" futureTask TimeoutException and " ,e);
				errlogger.error("uid is :"+ uid +" CandidateScreenImpl and uid is: "+ context.getUid() +"futureTask TimeoutException and ", e);
				futureTask.cancel(true);
				bSuccess = false;
			}
		}

		context.setResponseParamList(resultList);
		context.setResponseParamListBak(resultList);
		long end  = System.currentTimeMillis();
		logger.info("uid is: "+ uid +" end CandidateScreenImpl result size is : "+resultList.size()+" uid:" + context.getUid() 
				+" ,abtest :" + context.getAbtestVersion()  + " time is " + (end-cur) +",and cur = " + System.currentTimeMillis() );
		return bSuccess;
	}
}
