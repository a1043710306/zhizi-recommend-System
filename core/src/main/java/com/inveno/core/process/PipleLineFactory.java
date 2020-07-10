package com.inveno.core.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.inveno.common.bean.Context;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.init.ICandidateScreen;
import com.inveno.core.process.last.ILastProcess;
import com.inveno.core.process.model.IModelProcess;
import com.inveno.core.process.post.IPostProcess;
import com.inveno.core.process.pre.IPreProcess;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;

import redis.clients.jedis.JedisCluster;

/**
 * 
 * @ClassName: PipleLineFactory
 * @Description: PipleLine工厂类 
 * @author huangyiming
 * @date 2016年4月25日 下午2:34:48
 *
*/
public class PipleLineFactory
{
	public static Map<String, PipleLine> pipleLineMap = new HashMap<String, PipleLine>();
	public static Log logger = LogFactory.getLog(PipleLineFactory.class);

	public static PipleLine getPipleLine(Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		String abtest = context.getAbtestVersion();
		String uid = context.getUid();
		//默认配置的值
		String strDefaultPipeline = "candinateScreen=com.inveno.core.process.init.impl.CandidateScreenImpl,preProcess=com.inveno.core.process.pre.impl.PreProcessImpl,model=com.inveno.core.process.model.impl.ModelProcessImpl,post=com.inveno.core.process.post.impl.PostProcessImpl,last=com.inveno.core.process.last.impl.LastProcessImpl";
	
		String strPipeline = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "pipeLine");
	
		if (StringUtils.isEmpty(strPipeline)) {
			//默认配置
			strPipeline = context.getComponentConfiguration("default/pipeLine");
		}
		if (StringUtils.isEmpty(strPipeline)) {
			strPipeline = strDefaultPipeline;
		}
		PipleLine pipleLine = pipleLineMap.get(abtest);
		//pipleline有缓存或者缓存没有更新则直接从缓存中获取
		if (pipleLine != null && pipleLine.confMap.containsValue(strPipeline))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("getpipleline from map,userid="+ uid + ",abversion:"+ abtest +",and  cur is " +(System.currentTimeMillis()));
			}
			return pipleLine; 
		} 
		logger.info("no map,userid="+ uid  + ",abversion:"+ abtest);
		PipleLine pipleLineObj = new PipleLine();
		pipleLineMap.put(abtest, pipleLineObj);
		//
		//配置不存在或者配置已修改>>读取配置放入缓存再处理
		pipleLineObj.confMap.put("processFlow", strPipeline);
		logger.info("pipleline = "+ strPipeline);
		String [] confs = strPipeline.split(",");
		List<String> classNameList = new ArrayList<String>(); 
		if (confs != null && confs.length > 0)
		{
			for (int i = 0; i < confs.length; i++)
			{
				if (StringUtils.isNotEmpty(confs[i]))
				{
					String conf = confs[i];
					String className = conf.split("=")[1];
					classNameList.add(className);
				}
			}
		}

		for (int i = 0; i < classNameList.size(); i++)
		{
			String className = classNameList.get(i);
			try
			{
				//截取class
				//初选
				if (Class.forName(className).newInstance() instanceof ICandidateScreen)
				{
					ICandidateScreen candidateScreen = (ICandidateScreen) Class.forName(className).newInstance();
					org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor threadPoolTaskExecutor = SysUtil.getBean("threadPoolTaskExecutor");
					candidateScreen.setThreadPoolTaskExecutor(threadPoolTaskExecutor);
					pipleLineObj.processMap.put("candidateScreen", candidateScreen);
					pipleLineObj.exeFlow.add(candidateScreen);
					continue;
				}
				//前置过滤
				if (Class.forName(className).newInstance() instanceof IPreProcess)
				{
					IPreProcess preProcess = (IPreProcess)Class.forName(className).newInstance();
					org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor threadPoolTaskExecutor = SysUtil.getBean("threadPoolTaskExecutor");
					JedisCluster jedisCluster = SysUtil.getBean("jedisCluster");
					preProcess.setThreadPoolTaskExecutor(threadPoolTaskExecutor);
					preProcess.setJedisCluster(jedisCluster);
					pipleLineObj.processMap.put("preProcess", preProcess);
					pipleLineObj.exeFlow.add(preProcess);
					continue;
				}

				//GBDT
				if (Class.forName(className).newInstance() instanceof IModelProcess)
				{
					IModelProcess modelProcess = (IModelProcess)Class.forName(className).newInstance();
					org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor threadPoolTaskExecutor = SysUtil.getBean("threadPoolTaskExecutor");
					modelProcess.setThreadPoolTaskExecutor(threadPoolTaskExecutor);
					pipleLineObj.processMap.put("modelProcess", modelProcess);
					pipleLineObj.exeFlow.add(modelProcess);
					continue;
				}

				//POST
				if (Class.forName(className).newInstance() instanceof IPostProcess)
				{
					IPostProcess iPostProcess = (IPostProcess)Class.forName(className).newInstance();
					MonitorLog monitorLog = SysUtil.getBean("monitorLog");
					iPostProcess.setMonitorLog(monitorLog);
					pipleLineObj.processMap.put("post", iPostProcess);
					pipleLineObj.exeFlow.add(iPostProcess);
					continue;
				}
				
				//LAST
				if (Class.forName(className).newInstance() instanceof ILastProcess)
				{
					ILastProcess iLastProcess = (ILastProcess)Class.forName(className).newInstance();
					MonitorLog monitorLog = SysUtil.getBean("monitorLog");
					iLastProcess.setMonitorLog(monitorLog);
					pipleLineObj.processMap.put("last", iLastProcess);
					pipleLineObj.exeFlow.add(iLastProcess);
					continue;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return pipleLineObj;
	}
}
