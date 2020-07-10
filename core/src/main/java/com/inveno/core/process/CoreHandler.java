package com.inveno.core.process;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.result.IResultProcess;
import com.inveno.core.service.ICoreService;
import com.inveno.core.util.QConfUtil;
import com.inveno.core.util.RequestTrackingHelper;
import com.inveno.thrift.HotNewsListReq;
import com.inveno.thrift.NewsListResp;
import com.inveno.thrift.NewsListRespzhizi;
import com.inveno.thrift.RecNewsListReq;
import com.inveno.thrift.ResponseCode;
import com.inveno.thrift.TimelineNewsListReq;
import com.inveno.thrift.UserClickReq;
import com.inveno.thrift.ZhiziListReq;
 
@Component
 public class CoreHandler implements com.inveno.thrift.ZhiziCore.Iface{
 
	public static Log logger = LogFactory.getLog(CoreHandler.class);
	
	public static Log clickLog = LogFactory.getLog("recordClick");

	public Log reqLog = LogFactory.getLog("reqLog");

	public Log resLog = LogFactory.getLog("resLog");

	public Log qcnReqLog = LogFactory.getLog("qcnreqLog");

	public Log qcnResLog = LogFactory.getLog("qcnresLog");

	@Autowired
	private IResultProcess resultProcessAllImpl;
	
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Autowired
	private MonitorLog monitorLog;
	
	@Autowired
	private ICoreService coreServiceImpl;
	
	
	
	@Override	
	public NewsListResp recNewsList(RecNewsListReq req) throws TException {
		
		//add reqLog
		String uid = req.getBase().getUid();
		String app = req.getBase().getApp();
		Context context = new Context(req);
		if( context.getNum() <= 0 ){
			return new NewsListResp(ResponseCode.RC_SUCCESS,Collections.emptyList());
		}
		monitorLog.addCntMonitorLogByConfigId(context, MonitorType.REQUEST_COUNT);
		monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_COUNT);
		monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT);
		
		//由于 new Context 为 设置Scenario为-1,但是需要为0
		context.setScenario(0);
		if(StringUtils.isEmpty(context.getLanguage())){
			context.setLanguage(Constants.ZH_CN);
		}
		
		//分开日志,好用于以后做统计
		if ( req.isNeedBanner() ){
			reqLog.info("qb uid: "+ uid +" ,app:  " + app +" ,and abtest:  "+ req.getAbTestVersion() +" ,REQ: " + JSON.toJSONString(req));
		}else {
			reqLog.info("uid: "+ uid +" ,app:  " + app +" ,and abtest:  "+ req.getAbTestVersion() +" ,REQ: " + JSON.toJSONString(req));
		}
		long begin  = System.currentTimeMillis();
		logger.info(" uid: "+uid +" ,beging CoreHandler process ,app: "+app +" ,and abtestversion: "+req.getAbTestVersion()
				+" ,and getNum:  "+req.getNum()+", and is banner " + req.isNeedBanner() + " and time is "+ begin);
		 
		NewsListResp res =  coreServiceImpl.oldRecNewsRq(context);
		
		long end  = System.currentTimeMillis();
		if( end - begin >=200 ){
			monitorLog.addCntLogByProduct(context,MonitorType.RESPONSE_TIME_GT_200);
		}
		resLog.info("q uid: "+uid +" ,app: " + app + " ,abtest: "+ req.getAbTestVersion() +" and return result is : " 
				+ res +" ,and  result size is "+ context.getResponseParamList().size() +" and time is " + (end - begin));
		
		if(logger.isDebugEnabled()){
			logger.debug(" uid: "+ context.getUid()  +" ,end CoreHandler process return result is : " 
					+ res +"  ,app: "+ context.getApp()  +" ,and abtest: "+ context.getAbtestVersion()
					+" ,and getNum:  "+context.getResponseParamList().size()+ "time is "+ System.currentTimeMillis());
		}
		return res;
	} 
	
	@Override
	public NewsListResp timelineNewsList(TimelineNewsListReq req)throws TException {

		String uid = req.getBase().getUid();
		String app = req.getBase().getApp();
		Context context = new Context(req);
		
		if( context.getNum() <= 0 ){
			return new NewsListResp(ResponseCode.RC_SUCCESS,Collections.emptyList());
		}
		context.setScenario(0);
		
		long begin  = System.currentTimeMillis();
		logger.info(" uid: "+uid +" ,beging CoreHandler process ,app: "+app +"  and channelID "+ req.getChannelId() 
				+" ,and abtestversion: "+req.getAbTestVersion()+" ,and getNum:  "+req.getNum()+",  and time is "+ begin);
		
		monitorLog.addCntMonitorLogByConfigId(context, MonitorType.REQUEST_COUNT);
		monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_COUNT);
		if(StringUtils.isEmpty(context.getLanguage())){
			context.setLanguage(Constants.ZH_CN);
		}
		
		qcnReqLog.info("uid: "+ uid +" ,app:  " + app +" ,and abtest:  "+ req.getAbTestVersion() +" , qcn  REQ: " + JSON.toJSONString(req));
		
		NewsListResp res =  coreServiceImpl.oldTimelineRq(context);
		
		long end  = System.currentTimeMillis();
		if( end - begin >=200 ){
			monitorLog.addCntLogByProduct(context,MonitorType.RESPONSE_TIME_GT_200);
		}
		qcnResLog.info("qcn uid: "+uid +" ,app: " + app +" ,abtest: "+ req.getAbTestVersion() +" and return result is : "
				+ res +" ,and result size is "+ context.getResponseParamList().size() +" and no cache time is " + (end - begin));
		
		if(logger.isDebugEnabled()){
			logger.debug(" uid: "+ context.getUid()  +" ,end CoreHandler process return result is : " + res +"  ,app: "
					+ context.getApp()  +" ,and abtest: "+ context.getAbtestVersion()+" ,and getNum:  "+context.getResponseParamList().size()
					+ "time is "+ System.currentTimeMillis());
		}
		return res;
	}
	
	@Override
	public NewsListRespzhizi recRelatedNewsList(ZhiziListReq req) throws TException {
		long tsStart;
		tsStart = System.currentTimeMillis();
		
		Context context = new Context(req);
	
		logger.info(context.toString() + " begin to get qconf configuration");
	
		
		String abtestVersion = context.getAbtestVersion();
		String path = "/zhizi/abtest/" + abtestVersion;
		Map<String, String> mConfig = QConfUtil.getBatchConf(path);
		if (MapUtils.isNotEmpty(mConfig))
		{
			context.setAbtestConfigurationMap(mConfig);
		}
		path = "/zhizi/core";
		mConfig = QConfUtil.getBatchConf(path);
		if (MapUtils.isNotEmpty(mConfig))
		{
			context.setComponentConfigurationMap(mConfig);
		}
		logger.info(context.toString() + " finish to get qconf configuration. spend time : " + (System.currentTimeMillis() - tsStart) + " ms");

		NewsListRespzhizi response = null;
		if (context.getNum() <= 0 || context.getScenario() <= 0) {
			response = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, Collections.emptyList());
		} else {
			if (StringUtils.isEmpty(context.getLanguage()))
			{
				context.setLanguage(Constants.ZH_CN);
			}
			//兼容hotoday客户端出去的版本在没有选择语言的时候请求使用Unknow的情况
			if (context.getLanguage().equalsIgnoreCase("Unknown"))
			{
				context.setLanguage(Constants.DEFAULT_LANGUAGE);
			}
			context.setOffset(0);
			logger.info(context.toString() + " begin recRelatedNewsList process getNum:" + req.getNum());
			
			monitorLog.addCntMonitorLogByConfigId(context,MonitorType.RELATED_REQUEST_COUNT);
			monitorLog.addCntLogByProduct(context,MonitorType.RELATED_REQUEST_COUNT);
			
			long begin  = System.currentTimeMillis();	

			response = coreServiceImpl.relatedRecommendList(context);
			
			long end  = System.currentTimeMillis();
			if (end - begin >= 200)
			{
				monitorLog.addCntLogByProduct(context,MonitorType.RELATED_RESPONSE_TIME_GT_200);
			}
			
			logger.info(context.toString() + " end recRelatedNewsList process  getNum:" + req.getNum() + " return result is:" + response);
		}

		return response;		
	}
	
	@Override
	public NewsListRespzhizi recNewsListZhizi(ZhiziListReq req) throws TException {
		long tsRequestStart = System.currentTimeMillis();
		Context context = new Context(req);
		String requestId = context.getRequestId();
		long tsStart, tsEnd;

		tsStart = System.currentTimeMillis();
		logger.info(context.toString() + " begin to get qconf configuration");

		String abtestVersion = context.getAbtestVersion();
		String path = "/zhizi/abtest/" + abtestVersion;
		Map<String, String> mConfig = QConfUtil.getBatchConf(path);
		if (MapUtils.isNotEmpty(mConfig))
		{
			context.setAbtestConfigurationMap(mConfig);
		}
		path = "/zhizi/core";
		mConfig = QConfUtil.getBatchConf(path);
		if (MapUtils.isNotEmpty(mConfig))
		{
			context.setComponentConfigurationMap(mConfig);
		}
		logger.info(context.toString() + " finish to get qconf configuration. spend time : " + (System.currentTimeMillis() - tsStart) + " ms");

		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_qconf");

		NewsListRespzhizi response = null;
		if (context.getNum() <= 0 || context.getScenario() <= 0) {
			response = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, Collections.emptyList());
		}
		else {
			if (StringUtils.isEmpty(context.getLanguage()))
			{
				context.setLanguage(Constants.ZH_CN);
			}
			//兼容hotoday客户端出去的版本在没有选择语言的时候请求使用Unknow的情况
			if (context.getLanguage().equalsIgnoreCase("Unknown"))
			{
				context.setLanguage(Constants.DEFAULT_LANGUAGE);
			}
			context.setOffset(0);
			logger.info(context.toString() + " begin CoreHandler process getNum:" + req.getNum());
			
			monitorLog.addCntMonitorLogByConfigId(context,MonitorType.REQUEST_COUNT);
			monitorLog.addCntLogByProduct(context,MonitorType.REQUEST_COUNT);
			
			long begin  = System.currentTimeMillis();
			String uid = req.getUid();
			String app = req.getApp();
			int operation =  req.getOperation();
			
			response = coreServiceImpl.infoList(context);
			
			long end  = System.currentTimeMillis();
			if (end - begin >= 200)
			{
				monitorLog.addCntLogByProduct(context,MonitorType.RESPONSE_TIME_GT_200);
			}
			
			logger.info(context.toString() + " end CoreHandler process  getNum:" + req.getNum() + " return result is:" + response);
		}

		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsRequestStart, tsEnd, requestId, "core.total_cost");

		return response;
	}
	
	@Override
	public void userClick(UserClickReq req) throws TException {}
	
	@Override
	public NewsListResp hotNewsList(HotNewsListReq hotNewsReq) throws TException {
		return null;
	}
	
	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		return threadPoolTaskExecutor;
	}
	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}
	
	public IResultProcess getResultProcessAllImpl() {
		return resultProcessAllImpl;
	}
	public void setResultProcessAllImpl(IResultProcess resultProcessAllImpl) {
		this.resultProcessAllImpl = resultProcessAllImpl;
	}
	 
}
