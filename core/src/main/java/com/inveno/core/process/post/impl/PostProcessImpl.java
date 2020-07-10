package com.inveno.core.process.post.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.inveno.common.bean.Context;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.process.post.IPostProcess;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

public class PostProcessImpl implements IPostProcess
{
	private Log logger = LogFactory.getLog(this.getClass());

	private Log logELK = LogFactory.getLog("elklog");

	@Override
	public boolean process(Context context) {

		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		String uid =  context.getUid();
		List<String> classNameList = new ArrayList<String>();
		classNameList.add("eroticFrequencyControl");
		long cur = System.currentTimeMillis();

		String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "postclass");
		if (StringUtils.isNotEmpty(strClass)) {
			classNameList = Arrays.asList(strClass.split(";"));
//			if(logger.isDebugEnabled()){
//				classNameList = new ArrayList<String>();
//				classNameList.add("eroticFrequencyControl");
//			//2018年3月13日 11:56:13 ---remove yezi test
//			//midNightEroticFrequencyControl;sourceRankMixedControl;simHashControl;videoMixControl
//			classNameList.add("midNightEroticFrequencyControl");
//			classNameList.add("sourceRankMixedControl");
//			classNameList.add("simHashControl");
//			
//			classNameList.add("multiStrategyMixControl");
//			//2018年3月13日 11:56:13 ---remove yezi test
//			}
		}
		
		
		 logger.debug(strRequestInfo + " begin PostProcessImpl, size is " + context.getResponseParamList().size());
		//logger.debug(strRequestInfo + " begin PostProcessImpl, size is " + context.getResponseParamList().size());

		for (String className : classNameList) {
			@SuppressWarnings("unchecked")
			IPostPolicy<List<ResponParam>> policy = (IPostPolicy<List<ResponParam>>)SysUtil.getBean(className);
			try {
				policy.process(context);
			} catch (TException e) {
				logger.error(strRequestInfo + " PostProcessImpl.process " + className + " Exception", e);
			} catch (Exception e) {				
				logger.error(strRequestInfo + " PostProcessImpl.process " + className + " Exception", e);
			} finally {
				logger.debug(strRequestInfo + " done PostProcess w/ " + className + ", size is " + context.getResponseParamList().size());
			}
		}
		long end = System.currentTimeMillis();
		monitorLog.addResTimeLogByProduct(context, MonitorType.RULE_RESPONSE_TIME, (end - cur));
		logger.debug(strRequestInfo + " end PostProcessImpl.process, size is "+ context.getResponseParamList().size());
		

		boolean bOutputResultData = Boolean.parseBoolean(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "ifOutputResult"));
		if (bOutputResultData) {
			ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
			for (ResponParam param : context.getResponseParamList()) {
				HashMap<String, Object> mData = new HashMap<String, Object>();
				mData.put("contentId", param.getInfoid());
				mData.put("strategy", param.getStrategy());
				alDataInput.add(mData);
			}
			HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
			mDumpLog.put("module", "core");
			mDumpLog.put("event_time", System.currentTimeMillis());
			mDumpLog.put("uid", uid);
			mDumpLog.put("app", context.getApp());
			mDumpLog.put("scenario", context.getScenario());
			mDumpLog.put("iostream", "postprocess");
			mDumpLog.put("data", alDataInput);
			String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
			logELK.info(strLogMsg);
		}
		
//		if(logger.isDebugEnabled()){
//			///yezi add log --for end postProcess debug
//			
//			ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//			for (ResponParam param : context.getResponseParamList()) {
//				HashMap<String, Object> mData = new HashMap<String, Object>();
//				mData.put("contentId", param.getInfoid());
//				mData.put("strategy", param.getStrategy());
//				alDataInput.add(mData);
//			}
//			HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//			mDumpLog.put("module", "core");
//			mDumpLog.put("event_time", System.currentTimeMillis());
//			mDumpLog.put("uid", uid);
//			mDumpLog.put("app", context.getApp());
//			mDumpLog.put("scenario", context.getScenario());
//			mDumpLog.put("iostream", "postprocess");
//			mDumpLog.put("data", alDataInput);
//			String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
//			logger.debug("1 end PostProcessImpl.process----"+strLogMsg);
//		}
		
		
		return true;
	}

	private MonitorLog monitorLog;

	@Override
	public void setMonitorLog(MonitorLog monitorLog) {
		this.monitorLog = monitorLog;
	}
}
