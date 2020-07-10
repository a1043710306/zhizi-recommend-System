package com.inveno.core.process.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.inveno.common.bean.Context;
import com.inveno.core.process.model.IModelPolicy;
import com.inveno.core.process.model.IModelProcess;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

/**
 * 
 * @author robinson
 *
 */
public  class ModelProcessImpl implements IModelProcess {
	
	private Log logger = LogFactory.getLog(this.getClass());
	private Log errlogger = LogFactory.getLog("toErrFile");
	
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		return threadPoolTaskExecutor;
	}
	
	@Override
	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}

	@Override
	public boolean process(Context context)  {
		boolean bSuccess = true;

		if (context.getRecommendInfoData() != null) {

			String uid =  context.getUid();
			if (logger.isTraceEnabled()) {
				logger.debug("uid is :"+ uid +", ModelProcessImpl,result is " + context.getResponseParamList() +",and cur = " + System.currentTimeMillis() );
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("uid is: "+ uid +", begin ModelProcessImpl,size is " + context.getResponseParamList().size() +",and cur: " + System.currentTimeMillis() );
			}
			
			long cur = System.currentTimeMillis();
			long end  = System.currentTimeMillis();
			
			List<String> classNameList = new ArrayList<String>();
			classNameList.add("rankingModelProcessPolicy");
			String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "modelclass");
			
			int modelTimeOut = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "modelTimeOut"), 75);
			if (StringUtils.isNotEmpty(strClass) ){
				classNameList = Arrays.asList(strClass.split(";"));
			}
			
			int doRankModelTask =  NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_GBDT, "ifDoRank"), 0);
			if(doRankModelTask == 0){//不做主模型处理
				if(logger.isDebugEnabled()){
					logger.debug("begin ModelProcessImpl ,not do Rank Model Task,and uid: " + context.getUid() 
							+" ,abtest :" + context.getAbtestVersion() 
							+" time is " + (System.currentTimeMillis()-cur) +" ,and cur: " + System.currentTimeMillis()	);
				}
				return  bSuccess;
			}
			if(logger.isDebugEnabled()){
				logger.debug("begin ModelProcessImpl ,the Model classNameList is:" + classNameList +" ,and uid: " + context.getUid() 
						+" ,abtest :" + context.getAbtestVersion() 
						+" time is " + (System.currentTimeMillis()-cur) +" ,and cur: " + System.currentTimeMillis()	);
			}

			for (String className : classNameList) {
				IModelPolicy<Boolean> policy = (IModelPolicy<Boolean>) SysUtil.getBean(className);
				try {
					bSuccess &= policy.process(context);
				} catch (TException e) {
					logger.error("===  policy.process invoke ModelProcessImpl Exception," + "and abtest " + context.getAbtestVersion() + " uid is :" + uid + "==="+e, e.getCause());
					errlogger.error("===  policy.process invoke ModelProcessImpl Exception," + "and abtest " + context.getAbtestVersion() + " uid is :" + uid + "===" +e + e.getCause());
				} finally {
					if (logger.isDebugEnabled()) {
						logger.debug("uid is: " + uid + " time is : " + +(System.currentTimeMillis() - cur) + " ,and cur = " + System.currentTimeMillis());
					}
				}
			}

			end  = System.currentTimeMillis();
			logger.info("uid is: "+ uid +" ,end readFile ModelProcessImpl,end ModelProcessImpl time is " + (end-cur) 
					+",and size is " + context.getResponseParamList().size() +",and cur: " + System.currentTimeMillis() );
		}

		return bSuccess;
	}
}
