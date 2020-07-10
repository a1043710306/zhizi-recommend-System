package com.inveno.sire.monitor;


import com.inveno.sire.config.CuratorFrameworkConfig;
import com.inveno.sire.service.IService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CuratorWatch {

	private static final Logger logger = LoggerFactory.getLogger(CuratorWatch.class);
	
	@Autowired
	private CuratorFrameworkConfig curatorFrameworkConfig;

    @Autowired
    private IService serviceSupport;
	
    public void treeWatch()throws Exception{
		TreeCache treeCache = new TreeCache(curatorFrameworkConfig.getZkclient(), curatorFrameworkConfig.getPath());
        treeCache.start();
        logger.debug("TreeWatch start Listening zookeeper ...");
        
        TreeCacheListener tcl = new TreeCacheListener() {  
            @SuppressWarnings("incomplete-switch")
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            	 switch (event.getType())
                 {
                     case NODE_ADDED:
                     {
                         break;
                     }
                     case NODE_UPDATED:
                     {
                    	 String message = "时间点："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())+
                    			 ",更新节点: " + ZKPaths.getNodeFromPath(event.getData().getPath())+
                  			   ",节点路径:"+ event.getData().getPath()+","+"节点内容:" + new String(event.getData().getData(),"UTF-8");
                    	 logger.debug(message);
                         logger.debug("start syscdatas...");
                         serviceSupport.syncDatas();
                         logger.debug("end syscdatas...");
                         break;
                     }
                     case NODE_REMOVED:
                     {
                         break;
                     }
                 }
            	
            }  
        };
        //注册监听
        treeCache.getListenable().addListener(tcl);
    }
    
}
