package com.inveno.core.process.init;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.inveno.common.bean.Context;
import com.inveno.core.process.IProcess;
 
/**
 * 
  * @ClassName: initProcess
  * @Description: 初选处理
  * @author huangyiming
  * @date 2016年4月6日 下午1:52:34
  *
 */
public interface ICandidateScreen extends IProcess{

	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor);

}
