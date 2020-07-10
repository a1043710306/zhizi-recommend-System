package com.inveno.core.process.expinfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.process.expinfo.face.AbstractExpInfoOfImpression;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;

@Component("memeExpInfoOfImpressionProcess")
public class MEMEExpInfoOfImpressionProcess extends AbstractExpInfoOfImpression {

	public static  Log logger = LogFactory.getLog(MEMEExpInfoOfImpressionProcess.class);

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	/**
	 * 流量探索功能
	 * @param indexList 
	 * @param resultList 
	 * @param resultSet 
	 * @param size 
	 * @param req
	 * @return
	 */
	@Override
	public void getExpInfo(Context context, List<Double> indexListForNoCache, List<ResponParam> resultList, List<Double> indexList, Set<ResponParam> resultSet,boolean useImpression) {

		String app =  context.getApp();

		Map<String,Object> configMap = initConfigMap(context,useImpression);

		if (!ifDoExp(context,configMap)) {
			return ;
		}

		Map<String, String> mExpInfoKey = getExpInfoKey(app, context.getLanguage(), false, true);
		String expinfo_key = mExpInfoKey.get("origin");
		// 正在探索的资讯，正在探索池
		String flowing_key = mExpInfoKey.get("flowing");
		// 已经过下发,可能不够impression的资讯，等待池
		String eagerly_key = mExpInfoKey.get("eagerly");
		// 探索文章的累计impression数
		// redis hash <infoid, impression count> (45980990,55)
		String impressionOfInfoIdKey = mExpInfoKey.get("impression");
		logger.debug("uid is :"+ context.getUid() + " mExpInfoKey is " + mExpInfoKey);

		// 探索下发最大资讯数
		int expRange = (int) configMap.get("expRangeOfMEME");

		// 流量探索比例 配置0.15
 		double flowExpPer = (double) configMap.get("flowExpPer2MEME");

		Set<String> flowingExpInfo = jedisCluster.zrange(flowing_key, 0, expRange);
 		long lengthOfFlowingKey = ( flowingExpInfo != null ? flowingExpInfo.size() : 0 ) ;

		double req2ImpressionPer = 1.4;
		try {
			req2ImpressionPer = Double.parseDouble(context.getComponentConfiguration("task/" + app + "/req2ImpressionPer"));
		} catch (Exception e) {
			logger.error("===do trigger get qconf error,has no [req2ImpressionPer] config", e);
			req2ImpressionPer = 1.4;
		}
		// impression 阀值
		int needImpressionCount = (int)configMap.get("needImpCntMeme");

		add2ExpIngList(configMap, expinfo_key, flowing_key, eagerly_key, impressionOfInfoIdKey, expRange, context, needImpressionCount, req2ImpressionPer);

		if( !checkExpInfoLength(context, lengthOfFlowingKey ,useImpression,configMap) ){
			return;
		}

		if( flowExpImpLog.isTraceEnabled() ){
			flowExpImpLog.trace("uid is :"+ context.getUid()+" lengthOfFlowingKey is "+ lengthOfFlowingKey +", expinfo_key is " + expinfo_key);
		}

		if( logger.isDebugEnabled() ){
			logger.debug("uid is " +  context.getUid()+ " flowExpPer is " + flowExpPer );
		}

		doGetExpInfo(context, indexListForNoCache, flowExpPer, flowing_key, eagerly_key, resultSet, resultList, indexList, useImpression, configMap, lengthOfFlowingKey, flowingExpInfo);
	}
}
