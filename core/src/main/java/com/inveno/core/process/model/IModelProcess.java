package com.inveno.core.process.model;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.inveno.common.bean.Context;
import com.inveno.core.process.IProcess;


public interface IModelProcess extends IProcess {

	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor);

}
