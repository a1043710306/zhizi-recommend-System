package com.inveno.core.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.inveno.common.bean.Context;
import com.inveno.core.process.init.ICandidateScreen;
import com.inveno.core.process.last.ILastProcess;
import com.inveno.core.process.model.IModelProcess;
import com.inveno.core.process.post.IPostProcess;
import com.inveno.core.process.pre.IPreProcess;
import com.inveno.core.util.RequestTrackingHelper;

/**
 * 
  * @ClassName: PipleLine
  * @Description: 数据处理流程
  * @author huangyiming
  * @date 2016年4月25日 下午4:16:26
  *
 */
public class PipleLine {
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 存放配置
	 */
	public Map<String, String> confMap = new HashMap<String, String>();

	/**
	 * 存放流程
	 */
	public Map<String, Object> processMap = new HashMap<String, Object>();

	/**
	 * 存放流程
	 */
	public List<Object> exeFlow = new ArrayList<Object>();


	public void invoke(Context context) {
		String requestId = context.getRequestId();
		long tsStart, tsEnd;

		context.setPipeLineInvokeTimeout(true);//先设定有超时

		boolean bSuccess = true;
		for (Object object : exeFlow) {
			if (object != null)
			{
				tsStart = System.currentTimeMillis();

				String moduleName = null;
				IProcess iProcess = null;
				try
				{
					iProcess = (IProcess) object;
					if (iProcess instanceof IPreProcess)
						moduleName = "core.pipeline.preprocess";
					else if (iProcess instanceof IPostProcess)
						moduleName = "core.pipeline.postprocess";
					else if (iProcess instanceof ICandidateScreen)
						moduleName = "core.pipeline.primary_selection";
					else if (iProcess instanceof IModelProcess)
						moduleName = "core.pipeline.model";
					else if (iProcess instanceof ILastProcess)
						moduleName = "core.pipeline.last";
					else
						moduleName = "core.pipeline.unknown";
					

					if (bSuccess && !context.isAsynPipeLineTimeout()) {
						bSuccess = iProcess.process(context);
						if(!bSuccess){//当前处理失败
							logger.error(context.toString() + " PipleLine invokeFlow failed in " + moduleName);
						}
						if(context.isAsynPipeLineTimeout()){// 处理pipline时 有超时情况
							logger.error(context.toString() + " PipleLine invokeFlow timeout in " + moduleName);
						}
					} else {//没有调起来的模块
						logger.error(context.toString() +  ",PipleLine not invokeFlow : " + moduleName);
					}
				}
				catch (Exception e)
				{
					logger.error(context.toString() + " PipleLine invokeFlow Exception in " + moduleName, e);
				}

				tsEnd = System.currentTimeMillis();
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, moduleName);
			}
		}
		
	}

	/**
	 * 
	  * invokeByMap(处理流程集合，执行顺序在这里确定)
 	  * @Title: invokeByMap
 	  * @param @param processMap
	  * @param @param context    设定文件
	  * @return void    返回类型
	  * @throws
	 */
	public void invokeFlow(Context context) {
		String requestId = context.getRequestId();
		long tsStart, tsEnd;
		//初选
		tsStart = System.currentTimeMillis();
		try {
			if (processMap.containsKey("candidateScreen")) {
				ICandidateScreen candidateScreen = (ICandidateScreen)processMap.get("candidateScreen");
				candidateScreen.process(context);
			}
		} catch (Exception e) {
			logger.error(context.toString() + " PipleLine invokeFlow Exception", e);
		}
		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.pipeline.primary_selection");
		
		tsStart = System.currentTimeMillis();
		try {
			//前置处理
			if (processMap.containsKey("preProcess")) {
				IPreProcess iPreProcess = (IPreProcess)processMap.get("preProcess");
				iPreProcess.process(context);
			}
		} catch (Exception e) {
			logger.error(context.toString() + " PipleLine invokeFlow Exception", e);
		}
		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.pipeline.preprocess");

		tsStart = System.currentTimeMillis();
		try {
			if (processMap.containsKey("modelProcess")) {
				IModelProcess iModelProcess = (IModelProcess)processMap.get("modelProcess");
				iModelProcess.process(context);
			}
		} catch (Exception e) {
			logger.error(context.toString() + " PipleLine invokeFlow Exception", e);
		}
		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.pipeline.model");

		tsStart = System.currentTimeMillis();
		try {
			if (processMap.containsKey("post")) {
				IPostProcess iPostProcess = (IPostProcess)processMap.get("post");
				iPostProcess.process(context);
			}
		} catch (Exception e) {
			logger.error(context.toString() + " PipleLine invokeFlow Exception", e);
		}
		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.pipeline.postprocss");
		
		
		tsStart = System.currentTimeMillis();
		try {
			if (processMap.containsKey("last")) {
				ILastProcess iLastProcess = (ILastProcess)processMap.get("last");
				iLastProcess.process(context);
			}
		} catch (Exception e) {
			logger.error(context.toString() + " PipleLine invokeFlow Exception", e);
		}
		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.pipeline.lastprocss");
	}
}
