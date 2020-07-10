package com.inveno.core.process.explore;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.thrift.ResponParam;

@Component("exploreProcess")
public class ExploreProcess extends AbstractExplore {
	public static Log logger = LogFactory.getLog(ExploreProcess.class);

	@Autowired
	ReadedInfoHandler readFilterInfo;

	/**
	 * 流量探索功能入口
	 * @param exploreParameter 流量探索配置文件实例
	 * @param mRequestIndexResponse 需要补充探索位的索引集合 
	 * 	比如，在foryou里同时进行news、short_video探索，则需要请求方法两次
	 * 	第一次传news的探索位
	 * 	第二次传short_video探索位	
	 * @param hsAllResponse 所有的资讯返回集合，主要用来防止重复下发
	 */
	@Override
	public void getExploreData(Context context, ExploreParameter exploreParameter, Map<Integer, ResponParam> mRequestIndexResponse,
			Set<String> hsAllResponse) {
		logger.info("start explore " + System.currentTimeMillis());

		//step 1. 填补进行池、计算从探索池获取到的这批资讯需要的request数
		checkExploringPool(exploreParameter);

		//step 2. 给流量探索位置分配资讯request
		doExploreProcess(context,exploreParameter, mRequestIndexResponse, hsAllResponse);

		logger.info("end explore " + System.currentTimeMillis());
	}
}
