package com.inveno.core.process.expinfo.face;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
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
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component
public abstract class AbstractExpInfoOfImpression extends AbstractExpInfo {

	public static Log logger = LogFactory.getLog(AbstractExpInfo.class);

	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHH");

	public static final Log flowExpImpLog = LogFactory.getLog("flowExpPVLog");

	public static final Log imp_flow_explore_logger = LogFactory.getLog("impression-flow-explore-log");

	public static final String LOG_TAG = "&";
	public static final String EVENT_FIRST_INTO_FLOW_EXPLORING        = "first-into-flow-exploring";
	public static final String EVENT_INTO_FLOW_EXPLORING_AGAIN        = "into-flow-exploring-again";
	public static final String EVENT_FINISH_FLOW_EXPLORE              = "finish-flow-explore";
	public static final String EVENT_ENOUGH_IMPRESSION_BEFORE_EXPLORE = "enough-impression-before-explore";
	public static final String EVENT_STOP_EXPLORE_IN_EAGERLY_TOO_LONG = "stop-explore-because-in-eagerly-too-long";
	public static final String EVENT_STOP_EXPLORE_PUBLISHTIME_TOO_OLD = "stop-explore-because-publishTime-too-old";
	public static final String EVENT_STOP_EXPLORE_NO_NEWS_INFO        = "stop-explore-because-no-news-info";
	public static final String EVENT_WASTED_EXPLORE_COUNT             = "wasted-explore-count";

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	public MonitorLog monitorLog;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Override
	public void getExpInfo(Context context, List<Double> indexListForNoCache, List<ResponParam> resultList, List<Double> indexList,
			Set<ResponParam> resultSet, boolean useImpression) {
	}


	public static String getRedisKeyForNeedRequest(String content_id, String app)
	{
		return content_id + Constants.SEPARATOR_REDIS_KEY + app;
	}
	private void doAddingToExpIng(String expinfo_key, String flowing_key, String eagerly_key, String impressionOfInfoIdKey,
		Context context, int needImpressionCount, double firstRequestPer, double req2ImpressionPer, Set<Tuple> idsWithScores, boolean fFromEagerly)
	{
		String app = context.getApp();
		// 准备将新资讯写入正在探索池
		// score是当前的时间+下发次数 - 2017051719045
		if (CollectionUtils.isNotEmpty(idsWithScores)) {
			// 第一次开始进行探索的资讯
			List<String> startingIDList = new ArrayList<String>();
			// 等待反馈的资讯
			List<String> waitingList = new ArrayList<String>();
			// 反馈次数大于阀值的资讯, 已探索完成的资讯
			List<String> enoughList = new ArrayList<String>();
			List<String> enoughIDList = new ArrayList<String>();
			// 在等待池中，但没有反馈数据的资讯
			List<String> removeList   = new ArrayList<String>();
			List<String> removeIDList = new ArrayList<String>();
			// 需要移入进行池的资讯
			List<String> movingList = new ArrayList<String>();
			Map<byte[], Double> movingMap = new HashMap<byte[], Double>();

			for (Tuple t : idsWithScores) {
				// 可能已经在探索池中,由于feeder使用时间为udpateTime,因此会导致重新入探索池
				String infoid_with_type = t.getElement();
				String contentId = infoid_with_type.split("#")[0];
				// 获取反馈数据中的反馈次数
				String strImpressionCount = jedisCluster.hget(impressionOfInfoIdKey, contentId);

				//每篇资讯需要被下发的次数
				int needReqCnt = 0;
				int impressionCount = -1;
				try {
					impressionCount = Integer.valueOf(strImpressionCount);
				} catch (Exception e) {
					// 新资讯，无impression反馈，接受到的redis返回值为非法数字
					if (e instanceof NumberFormatException) {
						jedisCluster.hdel(impressionOfInfoIdKey, contentId);
					}
				}

				/**
				 * 反馈次数改成在进探索池之前会初始化成 -1，进入池之后设置为 0
				 * 如果存在反馈次数
				 *   a. 反馈次数等于 -1, 则为新探索文章
				 *   b. 反馈次数不等于 -1 且不满阀值
				 *     - 反馈展示量等于 0, 表示下发正在进行中或已完成一轮下发在等待反馈
				 *     - 反馈展示量不等于 0, 表示不是第一次来，或是第一次从探索池来之前就有混插, keyword 相关性或在其它同语系产品上的下发
				 */
				Object[] param = null;
				if (fFromEagerly)
				{
					if (impressionCount < 0) {
						//should not happen.
						removeList.add(infoid_with_type);
						removeIDList.add(contentId);
						needReqCnt = 0;
					}
					else if (impressionCount < needImpressionCount) {
						if (impressionCount == 0) {
							waitingList.add(infoid_with_type);
							needReqCnt = 0;
						} else {
							needReqCnt = (int) Math.ceil(req2ImpressionPer * (needImpressionCount - impressionCount));
							param = new Object[] {AbstractExpInfoOfImpression.EVENT_INTO_FLOW_EXPLORING_AGAIN, expinfo_key
								, infoid_with_type, getBoundedRequestNum(needReqCnt), System.currentTimeMillis() };
							String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
							imp_flow_explore_logger.info(str);
						}
					} else {
						needReqCnt = 0;
						enoughList.add(infoid_with_type);
						enoughIDList.add(contentId);
						param = new Object[] {AbstractExpInfoOfImpression.EVENT_FINISH_FLOW_EXPLORE, expinfo_key
							, infoid_with_type, impressionCount, System.currentTimeMillis() };
						String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
						imp_flow_explore_logger.info(str);
					}
				}
				else
				{
					if (impressionCount < 0) {
						needReqCnt = (int) Math.ceil(firstRequestPer * needImpressionCount);
						param = new Object[] {AbstractExpInfoOfImpression.EVENT_FIRST_INTO_FLOW_EXPLORING, expinfo_key
							, infoid_with_type, getBoundedRequestNum(needReqCnt), System.currentTimeMillis() };
						String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
						imp_flow_explore_logger.info(str);
						startingIDList.add(contentId);
					} else if (impressionCount < needImpressionCount) {
						if (impressionCount == 0) {
							waitingList.add(infoid_with_type);
							needReqCnt = 0;
						} else {
							needReqCnt = (int) Math.ceil(req2ImpressionPer * (needImpressionCount - impressionCount));
							param = new Object[] {AbstractExpInfoOfImpression.EVENT_FIRST_INTO_FLOW_EXPLORING, expinfo_key
								, infoid_with_type, getBoundedRequestNum(needReqCnt), System.currentTimeMillis() };
							String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
							imp_flow_explore_logger.info(str);
						}
					} else {
						needReqCnt = 0;
						enoughList.add(infoid_with_type);
						enoughIDList.add(contentId);
						param = new Object[] {AbstractExpInfoOfImpression.EVENT_ENOUGH_IMPRESSION_BEFORE_EXPLORE, expinfo_key
							, infoid_with_type, impressionCount, System.currentTimeMillis() };
						String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
						imp_flow_explore_logger.info(str);
					}
				}

				if (needReqCnt > 0) {
					// 此做法会优先下发yyyyMMddHH
					// 小时级别的数据==同一个小时则优先下发需要request 少的资讯;
					// 确保最小下发次数 10
					needReqCnt = getBoundedRequestNum(needReqCnt);
					movingMap.put(infoid_with_type.getBytes(), Double.valueOf(sf.format(new Date()) + StringUtils.leftPad(String.valueOf(needReqCnt), 3, "0")));
					movingList.add(infoid_with_type);
					String need_request_key = getRedisKeyForNeedRequest(contentId, app);
					jedisCluster.set(need_request_key, Integer.toString(needReqCnt));
					jedisCluster.expire(need_request_key, 72000);
				}
			}

			/**
			 * 将资讯加入正在探索池
			 * 从探索池中删除
			 * 从等待池删除
			 */
			if (!movingMap.isEmpty()) {
				jedisCluster.zadd(flowing_key.getBytes(), movingMap);
				jedisCluster.zrem(expinfo_key.getBytes(), movingMap.keySet().toArray(new byte[0][0]));
				jedisCluster.zrem(eagerly_key.getBytes(), movingMap.keySet().toArray(new byte[0][0]));
				if (flowExpImpLog.isDebugEnabled()) {
					flowExpImpLog.debug("from feeder key add to flowing key from key " + expinfo_key + " uid is " + context.getUid()
							+ " contentId is " + movingList);
				}
			}

			if (CollectionUtils.isNotEmpty(startingIDList)) {
				flowExpImpLog.info("from feeder key starting request cnt uid " + context.getUid() + " app=" + app
						+ " startingList " + startingIDList);
				for (String contentId : startingIDList)
				{
					try
					{
						String strImpressionCount = jedisCluster.hget(impressionOfInfoIdKey, contentId);
						if (Integer.parseInt(strImpressionCount) < 0)
							jedisCluster.hset(impressionOfInfoIdKey, contentId, "0");
					}
					catch (Exception e)
					{
					}
				}
			}
			/**
			 * 将完成探索资讯
			 * a. 从探索池中删除
			 * b. 从等待池删除
			 * c. 从进行池删除
			 * d. 从探索累计key中删除
			 */
			if (CollectionUtils.isNotEmpty(enoughList)) {
				flowExpImpLog.info("from feeder key enough request cnt uid " + context.getUid() + " app=" + app
						+ " enoughList " + enoughList);
				jedisCluster.zrem(expinfo_key, enoughList.toArray(new String[0]));
				jedisCluster.zrem(eagerly_key, enoughList.toArray(new String[0]));
				jedisCluster.zrem(flowing_key, enoughList.toArray(new String[0]));
				jedisCluster.hdel(impressionOfInfoIdKey, enoughIDList.toArray(new String[0]));
				try {
					readFilterInfo.addIdToBloomByExp(app, context.getLanguage(), enoughIDList);
				} catch (Exception e) {
				}
			}
			/**
			 * 将等待反馈的资讯从探索池中删除
			 */
			if (CollectionUtils.isNotEmpty(waitingList)) {
				flowExpImpLog.info("from feeder key waiting uid " + context.getUid() + " app=" + app
						+ " waitingList " + waitingList);
				jedisCluster.zrem(expinfo_key, waitingList.toArray(new String[0]));
			}
			if (CollectionUtils.isNotEmpty(removeList)) {
				flowExpImpLog.info("from feeder key removing uid " + context.getUid() + " app=" + app
						+ " removeList " + removeList);
				jedisCluster.zrem(expinfo_key, removeList.toArray(new String[0]));
				jedisCluster.zrem(eagerly_key, removeList.toArray(new String[0]));
				jedisCluster.zrem(flowing_key, removeList.toArray(new String[0]));
				jedisCluster.hdel(impressionOfInfoIdKey, removeIDList.toArray(new String[0]));
			}
		}
	}
	/**
	 * 此方法将探索池资讯加入到正在探索池: 1.需要判断探索池的资讯是否已经探索完成（避免部分重复探索） 2.是否已经进入了正在探索池,或者等待池
	 * 
	 * @param configMap 配置信息map
	 * @param key 探索池
	 * @param flowing_key 正在探索池
	 * @param eagerly_key 等待探索池
	 * @param impressionOfInfoIdKey
	 * @param expRange 单次最大探索文章数
	 * @param lengthOfFlowingKey 单次实际探索文章数
	 * @param context 当前上下文
	 * @param needImpressionCount impression 阀值
	 * @param req2ImpressionPer 非第一次下发的时候，request和impression的比例
	 */
	public void add2ExpIngList(Map<String, Object> configMap, String expinfo_key, String flowing_key, String eagerly_key,
			String impressionOfInfoIdKey, int expRange, Context context, int needImpressionCount, double req2ImpressionPer) {
		threadPoolTaskExecutor.submit(() -> {
			// 第一次流量探索下发比例
			double firstRequestPer = (double) configMap.get("firstRequestPer");

			// 如果正在探索资讯池长度小于 50 则补充
			// 过滤探索池的不需要进去正在探索池的资讯
			synchronized (this) {
				//补充探索进行池水位只需要一个线程进行补充
				long lengthOfFlowingKey = jedisCluster.zcard(flowing_key);
				if (lengthOfFlowingKey < expRange) {
					try {
						// 需要补充的长度
						long needLength = expRange * 3 - lengthOfFlowingKey;
						Set<Tuple> idsWithScores = jedisCluster.zrangeWithScores(expinfo_key, 0, needLength);

						doAddingToExpIng(expinfo_key, flowing_key, eagerly_key, impressionOfInfoIdKey, context, needImpressionCount,
							firstRequestPer, req2ImpressionPer, idsWithScores, false);

						// 如果探索池里面的资讯数很少，从等待池取资讯
						long need_length_eagerly = needLength - idsWithScores.size();
						if (need_length_eagerly > 0) {
							idsWithScores = jedisCluster.zrangeWithScores(eagerly_key, 0, need_length_eagerly);
							doAddingToExpIng(expinfo_key, flowing_key, eagerly_key, impressionOfInfoIdKey, context, needImpressionCount,
								firstRequestPer, req2ImpressionPer, idsWithScores, true);
						}
					} catch (Exception e) {
						logger.error("uid " + context.getUid() + " , get info from lengthOfAll", e);
					}
				}
			}
		});
	}

	/**
	 * 
	 * @param context 
	 * @param configMap 
	 * @param lengthOfFlowingKey 
	 * @param flowingExpInfo 
	 * @param indexListForNoCache:需要进行探索的位置
	 * @param flowExpPer:每个位置的比例
	 * @param flowing_key:正在探索
	 * @param eagerly_key:等待区
	 * @param resultSet:结果hashset,防止重复资讯
	 * @param resultList:结果集
	 * @param indexList:结果index list
	 */
	public void doGetExpInfo(Context context, List<Double> indexListForNoCache, double flowExpPer, String flowing_key, String eagerly_key,
			Set<ResponParam> resultSet, List<ResponParam> resultList, List<Double> indexList, boolean useImpression,
			Map<String, Object> configMap, long lengthOfFlowingKey, Set<String> flowingExpInfo) {
		String app = context.getApp();
		//get info 
		Set<String> idSet = new HashSet<String>();
		idSet = flowingExpInfo;

		String[] ids = idSet.toArray(new String[0]);
		if (ids == null) {
			return;
		}

		//redis map结构 infoid-> impression count (45980990,55)
		String impressionOfInfoIdKey = "expinfo_" + app;
		if (!context.getLanguage().equalsIgnoreCase("zh_CN")) {
			impressionOfInfoIdKey += "_" + context.getLanguage();
		}
		impressionOfInfoIdKey += "_impressionOfInfoIdKey";

		HashSet<String> expinfoSet = new HashSet<String>();
		int cnt = 0;
		for (Double resultIndex : indexListForNoCache) {
			ResponParam respon_param = null;
			try {
				double value = Math.random();
				if (value > flowExpPer) {
					continue;
				}
				if (flowExpImpLog.isTraceEnabled()) {
					flowExpImpLog.trace(" uid is " + context.getUid() + " values is " + value + ", flowExpPer is " + flowExpPer
							+ " resultIndex is " + resultIndex);
				}

				int index = new Random().nextInt(ids.length);
				String contentMsg = ids[index];
				String contentId = ids[index].split("#")[0];
				respon_param = new ResponParam(Strategy.FLOW_EXPLORATION.getCode(), contentId);

				int count = 0;
				while ((!expinfoSet.add(contentMsg) || !resultSet.add(respon_param)
						|| !filterIds(context, contentMsg, flowExpImpLog, configMap, flowing_key))) {
					index = new Random().nextInt(ids.length);
					contentMsg = ids[index];
					contentId = ids[index].split("#")[0];
					respon_param = new ResponParam(Strategy.FLOW_EXPLORATION.getCode(), contentId);
					if (flowExpImpLog.isDebugEnabled()) {
						flowExpImpLog.debug(" contain uid is " + context.getUid() + " resultIndex is " + cnt + " expinfo index is " + index);
					}
					count++;
					if (count >= 5) {
						break;
					}
				}

				if (count >= 5) {
					Object[] param = new Object[] {AbstractExpInfoOfImpression.EVENT_WASTED_EXPLORE_COUNT, flowing_key, System.currentTimeMillis() };
					String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
					imp_flow_explore_logger.info(str);
					continue;
				}

				cnt++;
				if (flowExpImpLog.isDebugEnabled()) {
					flowExpImpLog.debug("uid is " + context.getUid() + " cnt is " + cnt + " expinfo index is " + index);
				}

				//add to the return list
				resultList.set(resultIndex.intValue(), respon_param);
				indexList.remove(resultIndex);
				//incrBy
				long need2pv = jedisCluster.incrBy(getRedisKeyForNeedRequest(contentId, app), -1);

				if (logger.isDebugEnabled()) {
					logger.debug("expinfo uid is " + context.getUid() + " and app is " + app + " ,and language is "
							+ context.getLanguage() + " need2pv  is " + need2pv + " ,and infoid is " + respon_param.getInfoid()
							+ " , and abtest is " + context.getAbtestVersion());
				}
				if (flowExpImpLog.isDebugEnabled()) {
					flowExpImpLog.debug("expinfo uid is " + context.getUid() + " and app is " + app + " ,and language is "
							+ context.getLanguage() + " need2pv  is " + need2pv + " ,and infoid is " + respon_param.getInfoid()
							+ " , and abtest is " + context.getAbtestVersion());
				}

				if (need2pv <= 0) {
					String contentIdFinal = contentId;
					String contentMsgFinal = contentMsg;
					String impressionOfInfoIdKeyFinal = impressionOfInfoIdKey;

					threadPoolTaskExecutor.submit(() -> {
						jedisCluster.del(getRedisKeyForNeedRequest(contentIdFinal, app));
						long returnValue = jedisCluster.zrem(flowing_key, contentMsgFinal);
						jedisCluster.zadd(eagerly_key.getBytes(), new Date().getTime(), contentMsgFinal.getBytes());
						if (returnValue == 0) {
							flowExpImpLog.error("jedisCluster.zrem( key , respon_param.getInfoid()) error , and abtest is "
									+ context.getAbtestVersion() + " ,and uid is " + context.getUid() + " and infoid is "
									+ contentIdFinal + " contentMsg is " + contentMsgFinal);
						}
						monitorLog.addCntMonitorLogByConfigId(context, MonitorType.EXP_INFO_CNT);
						logger.info("getExpInfo get 1 ,and app is " + app + " ,and language is " + context.getLanguage()
								+ " ,and uid is " + context.getUid() + " , and infoid is " + contentIdFinal + " , and abtest is "
								+ context.getAbtestVersion());
						flowExpImpLog.info("getExpInfo get 1 ,and app is " + app + " ,and language is " + context.getLanguage()
								+ " ,and uid is " + context.getUid() + " , and infoid is " + contentIdFinal + " , and abtest is "
								+ context.getAbtestVersion());
					});
				}
			} catch (Exception e) {
				resultList.set(resultIndex.intValue(), null);
				indexList.add(resultIndex);
				if (respon_param != null) {
					jedisCluster.incrBy(getRedisKeyForNeedRequest(respon_param.getInfoid(), app), 1);
				}
				logger.error("getExpInfo error get " + cnt + " ,and app is " + app + " ,and uid is " + context.getUid(), e);
			}
		}
	}
}
