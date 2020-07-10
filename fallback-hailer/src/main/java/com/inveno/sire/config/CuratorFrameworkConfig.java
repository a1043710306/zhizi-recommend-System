package com.inveno.sire.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.inveno.sire.model.ZkConfig;

//@Component
public class CuratorFrameworkConfig implements InitializingBean{

	@Autowired
	private ZkConfig zKConfig;
	
	private CuratorFramework zkclient;
	
	private String path;
	
	public CuratorFramework getZkclient() {
		return zkclient;
	}

	public void setZkclient(CuratorFramework zkclient) {
		this.zkclient = zkclient;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
         RetryPolicy rp=new ExponentialBackoffRetry(zKConfig.getBaseSleepTimeMs(), zKConfig.getMaxRetries());
         Builder builder = CuratorFrameworkFactory.builder().connectString(zKConfig.getHost()).connectionTimeoutMs(zKConfig.getConnectionTimeoutMs()).sessionTimeoutMs(zKConfig.getSessionTimeoutMs()).retryPolicy(rp);
         CuratorFramework zclient = builder.build();
         setZkclient(zclient);
         setPath(zKConfig.getPath());
         zclient.start();
	}

}
