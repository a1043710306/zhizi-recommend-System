package com.inveno.core.process.expinfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.process.expinfo.face.AbstractExpInfoOfImpression;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;

@Component("expInfoOfImpressionProcess")
public class ExpInfoOfImpressionProcess extends AbstractExpInfoOfImpression {

	public static Log logger = LogFactory.getLog(ExpInfoOfImpressionProcess.class);

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	/**
	 * 流量探索功能
	 * 
	 * @param indexList
	 * @param resultList
	 * @param resultSet
	 * @param size
	 * @param req
	 * @return
	 */
	@Override
	public void getExpInfo(Context context, List<Double> indexListForNoCache, List<ResponParam> resultList, List<Double> indexList,
			Set<ResponParam> resultSet, boolean useImpression) {
		String app = context.getApp();

		Map<String, Object> configMap = initConfigMap(context, useImpression);

		/**
		 * 【过滤】
		 */
		if (!ifDoExp(context, configMap)) {
			return;
		}

		boolean ifVideo = ContextUtils.isVideoChannel(context);

		Map<String, String> mExpInfoKey = getExpInfoKey(app, context.getLanguage(), ifVideo, false);
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
		int expRange = (int) configMap.get("expRange");
		if (ifVideo) {
			expRange = (int) configMap.get("expRangeOfVideo");
		}

		// 流量探索比例 配置0.15
		double flowExpPer = (double) configMap.get("flowExpPer");
		if (ifVideo) {
			flowExpPer = (double) configMap.get("flowExpPer2Video");
		}

		// 获取正在探索池中按时间排序的资讯数
		Set<String> flowingExpInfo = jedisCluster.zrange(flowing_key, 0, expRange);

		// 本轮，实际获得到的探索资讯数
		long lengthOfFlowingKey = (flowingExpInfo != null ? flowingExpInfo.size() : 0);

		// 非第一次，下发和展示的投放比
		double req2ImpressionPer = 1.4;
		try {
			req2ImpressionPer = Double.parseDouble(context.getComponentConfiguration("task/" + app + "/req2ImpressionPer"));
		} catch (Exception e) {
			logger.error("===do trigger get qconf error,has no [req2ImpressionPer] config", e);
			req2ImpressionPer = 1.4;
		}
		// impression 阀值
		int needImpressionCount = (int) configMap.get("needImpCnt");

		// 填补正在探索池、计算从探索池获取到的这批资讯需要的request数
		add2ExpIngList(configMap, expinfo_key, flowing_key, eagerly_key, impressionOfInfoIdKey, expRange, context, needImpressionCount,
				req2ImpressionPer);

		// 如果探索文章数小于最小可允许的探索文章数，则不下发
		// minExpLength 初始化 50
		// minExpLength 配置为 0
		if (!checkExpInfoLength(context, lengthOfFlowingKey, useImpression, configMap)) {
			return;
		}

		if (flowExpImpLog.isTraceEnabled()) {
			flowExpImpLog.trace("uid is :" + context.getUid() + " lengthOfFlowingKey is " + lengthOfFlowingKey + ",  expinfo_key is " + expinfo_key);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("uid is " + context.getUid() + " flowExpPer is " + flowExpPer);
		}

		// 给流量探索资讯分派request
		doGetExpInfo(context, indexListForNoCache, flowExpPer, flowing_key, eagerly_key, resultSet, resultList, indexList, useImpression,
				configMap, lengthOfFlowingKey, flowingExpInfo);
	}
}
