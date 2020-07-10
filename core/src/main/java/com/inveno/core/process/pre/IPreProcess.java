package com.inveno.core.process.pre;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import redis.clients.jedis.JedisCluster;

import com.inveno.common.bean.Context;
import com.inveno.core.process.IProcess;
 

public interface IPreProcess extends IProcess {

	public void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor threadPoolTaskExecutor);
	
	public void setJedisCluster(JedisCluster jedisCluster);
	
}
