package com.inveno.core.process.explore;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

public abstract class AbstractExplore {

	public static Log checkExploringLogger = LogFactory.getLog("flow-explore-check-exploring-detail");
	public static Log exploreProcessLogger = LogFactory.getLog("flow-explore-process-detail");
	public static Log impFlowExploreLogger = LogFactory.getLog("impression-flow-explore-log");

	private static final String LOG_TAG = "&";
	private static final String EVENT_FIRST_INTO_FLOW_EXPLORING = "first-into-flow-exploring";
	private static final String EVENT_INTO_FLOW_EXPLORING_AGAIN = "into-flow-exploring-again";
	private static final String EVENT_FINISH_FLOW_EXPLORE = "finish-flow-explore";
	private static final String EVENT_ENOUGH_IMPRESSION_BEFORE_EXPLORE = "enough-impression-before-explore";
	private static final String EVENT_STOP_EXPLORE_IN_WAITING_TOO_LONG = "stop-explore-because-in-waiting-too-long";
	private static final String EVENT_STOP_EXPLORE_NO_NEWS_INFO = "stop-explore-because-no-news-info";
	private static final String EVENT_WASTED_EXPLORE_COUNT = "wasted-explore-count";

	private static final int MIN_REQUEST_NUM = 10;

	private Object[] logParam = null;
	private String strLogData = "";

	private HashMap<String, Boolean> mapDoCheckingExploringPool = new HashMap<String, Boolean>();

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	/**
	 * 流量探索
	 * 
	 * @param exploreParameter
	 *            流量探索配置文件实例
	 * @param mRequestIndexResponse
	 *            需要补充探索位的索引集合 比如，在foryou里同时进行news、short_video探索，则需要请求方法两次
	 *            第一次传news的探索位 第二次传short_video探索位
	 * @param hsAllResponse
	 *            所有的资讯返回集合，主要用来方式重复下发
	 */
	public abstract void getExploreData(Context context,ExploreParameter exploreParameter, Map<Integer, ResponParam> mRequestIndexResponse,
			Set<String> hsAllResponse);

	/**
	 * 确保每次至少会下发一定数量的request
	 */
	private int getBoundedRequestNum(int request_num) {
		try {
			if (request_num > MIN_REQUEST_NUM) {
				return request_num;
			} else {
				return MIN_REQUEST_NUM;
			}
		} catch (Exception e) {
			return MIN_REQUEST_NUM;
		}
	}

	private int getBoundedRequestNum(double request_num) {
		try {
			if (request_num > MIN_REQUEST_NUM) {
				return (int) Math.ceil(request_num);
			} else {
				return MIN_REQUEST_NUM;
			}
		} catch (Exception e) {
			return MIN_REQUEST_NUM;
		}
	}

	/**
	 * 检查进行池是否处于安全水位 如果不是，则从等待池、探索池补充资讯 step 1. 优先从等待池出一定比例的资讯 step 1.1
	 * 如果从等待池中取得的数据足够 则取多一点的数据然后按照impression累计数降序取top step 1.2 如果从等待池中取得的数据不够多
	 * 则全加入进行池 step 2. 剩下的资讯全都从探索池里面出
	 * 
	 * 补充：
	 * 2018-7-11 16:32:31
	 *  等待池进入等待池 处理策略
	 * 补充进行池策略：等待池大小=S ，等待池的量进入进行池 的比值=R， 等待池文章量/(探索池文章量 + 等待池文章量)=r
	 * S=[2000,+] --> R=0.95 
	 * S=[100,2000] --> R=[0.6,r] 
	 * S=[0,100] --> R=[0.1,r]
	 * @author yezi
	 */
	public void checkExploringPool(ExploreParameter exploreParameter) {
		threadPoolTaskExecutor.submit(() -> {
			// 如果正在探索资讯池长度小于 safeLengthOfExploringPool 则补充
			// 过滤探索池中不需要探索的资讯
			String loggerPrefix = exploreParameter.getApp() + "_" + exploreParameter.getScenario() + "_" + exploreParameter.getType();
			boolean fDoTask = false;
			synchronized (mapDoCheckingExploringPool) {
				String exploreKey = exploreParameter.getRedisKeyForExplore();
				boolean bDoCheckingExploringPool = Boolean.valueOf(String.valueOf(mapDoCheckingExploringPool.get(exploreKey)));
				if (!bDoCheckingExploringPool) {
					fDoTask = true;
					mapDoCheckingExploringPool.put(exploreKey, true);
					checkExploringLogger.info(loggerPrefix + " " + "ready to checkExploringPool");
				} else {
					checkExploringLogger.info(loggerPrefix + " " + "reject to checkExploringPool due to someone is doing");
				}
			}
			if (fDoTask) {
				String exploreKey = exploreParameter.getRedisKeyForExplore();
				String exploringKey = exploreParameter.getRedisKeyForExploring();
				String waitingKey = exploreParameter.getRedisKeyForWaiting();

				checkExploringLogger.info(loggerPrefix + " " + "start checkExploringPool " + System.currentTimeMillis());

				// 补充探索进行池水位
				try {
					long lengthOfExploringPool = jedisCluster.zcard(exploringKey);
					checkExploringLogger.info(loggerPrefix + " " + "lengthOfExploringPool -> " + lengthOfExploringPool);

					if (lengthOfExploringPool < exploreParameter.getEffectiveRange()) {
						try {
							checkExploringLogger.debug(loggerPrefix + " " + exploreParameter.getRedisKeyForExplore() + " "
									+ exploreParameter.getRedisKeyForExploring() + " " + exploreParameter.getRedisKeyForWaiting() + " "
									+ exploreParameter.getRedisKeyForImpressionCount()+" -1="+ exploreParameter.getRedisKeyForAllImpressionCount());

							int safeLengthOfExploringPool = exploreParameter.getSafeRange();
							checkExploringLogger.info(loggerPrefix + " " + "safeLengthOfExploringPool -> " + safeLengthOfExploringPool);

							// 需要补充的长度
							int needSupplementLength = (int) (safeLengthOfExploringPool - lengthOfExploringPool);
							checkExploringLogger.info(loggerPrefix + " " + "needSupplementLength -> " + needSupplementLength);

							if (ExploreParameter.EXPLORE_STRATEGY_IMPRESSION_BASED.equalsIgnoreCase(exploreParameter.getStrategy())) {
								checkExploringLogger.info(loggerPrefix + " " + "impression explore base");
								// 开启策略
								// step 1. 优先从等待池出一定比例的资讯
								float waitingSupplementRatio = exploreParameter.getWaitingSupplementExploringRatio();
								long waitingSize = jedisCluster.zcard(waitingKey);
								//增加动态算放出比例 给出一定范围处理判断
								long  waitingPoolMaxRangSize = exploreParameter.getWaitingSupplementExploringThreshold();								
								float exploreSupplementExploringAttenuationRatio = exploreParameter.getExploreSupplementExploringAttenuationRatio();
								float defaultMaxWaitingSupplementRatio = exploreParameter.getWaitingSupplementExploringMaxRatio();
								long  exploreSize = jedisCluster.zcard(exploreKey);								
								long  waitingPoolMinRangeSize = exploreParameter.getWaitingSupplementExploringMinRange();								
								float waitingPoolInRangeMinRatio = exploreParameter.getWaitingSupplementExploringInRangeMinRatio();
								float waitingPoolLessRangeMinRatio = exploreParameter.getWaitingSupplementExploringLessRangeMinRatio();
								
								
								if(exploreSize <= 0){
									waitingSupplementRatio = 1;
									checkExploringLogger.info(loggerPrefix + " " + "exploreSize <= 0 " );
								}else if( waitingSize >= waitingPoolMaxRangSize){ //等待池量=[2000,+] --> 等待池放出比例R=0.95 
									waitingSupplementRatio = defaultMaxWaitingSupplementRatio;
									checkExploringLogger.info(loggerPrefix + " " + " waitingSize >= waitingPoolMaxRangSize" );
								}else if( waitingSize < waitingPoolMaxRangSize && waitingSize >= waitingPoolMinRangeSize){//等待池量=[100,2000] --> 等待池放出比例R=[0.6,r] 										
									waitingSupplementRatio = 1 - (exploreSize * exploreSupplementExploringAttenuationRatio/(exploreSize + waitingSize)) ;
									checkExploringLogger.info(loggerPrefix + " " + " waitingSize < waitingPoolMaxRangSize && waitingSize >= waitingPoolMinRangeSize, r="+waitingSupplementRatio );
									waitingSupplementRatio = waitingSupplementRatio < waitingPoolInRangeMinRatio ? waitingPoolInRangeMinRatio : waitingSupplementRatio;
								}else if( waitingSize < waitingPoolMinRangeSize){//等待池量=[0,100] --> 等待池放出比例 R=[0.1,r]
									waitingSupplementRatio = 1 - (exploreSize * exploreSupplementExploringAttenuationRatio/(exploreSize + waitingSize)) ;
									checkExploringLogger.info(loggerPrefix + " " + " waitingSize < waitingPoolMinRangeSize, r="+waitingSupplementRatio);
									waitingSupplementRatio =  waitingSupplementRatio < waitingPoolLessRangeMinRatio ? waitingPoolLessRangeMinRatio : waitingSupplementRatio;
								}

								
								int waitingSupplementNum = (int) Math.ceil(waitingSupplementRatio * needSupplementLength);							
								
								int optimizeMinSize = (int) Math
										.ceil(exploreParameter.getWaitingIntoExploringOptimizeMinRatio() * waitingSupplementNum);

								checkExploringLogger.info(loggerPrefix + " " + "waitingSupplementRatio -> " + waitingSupplementRatio);
								checkExploringLogger.info(loggerPrefix + " " + "waitingSupplementNum -> " + waitingSupplementNum);
								checkExploringLogger.info(loggerPrefix + " " + "optimizeMinSize -> " + optimizeMinSize);
								checkExploringLogger.info(loggerPrefix + " " + "waitingSize -> " + waitingSize);
								checkExploringLogger.info(loggerPrefix + " " + "exploreSize -> " + exploreSize);

								if (waitingSize > optimizeMinSize) {
									checkExploringLogger.info(loggerPrefix + " " + "start optimizes");
									// step 1.1 如果从等待池中取得的数据足够
									// 则取多一点的数据然后按照impression累计数降序取top
									Set<Tuple> hsWaitingSupplementData = jedisCluster.zrangeWithScores(waitingKey, 0, optimizeMinSize - 1);
									List<String> listContentIds = new ArrayList<>();
									Map<String, String> hmContentIdsData = new HashMap<String, String>();
									String key_tag = "__";

									// result key
									for (Tuple t : hsWaitingSupplementData) {
										String contentId = t.getElement().split("#")[0];
										listContentIds.add(contentId);
										hmContentIdsData.put(contentId, t.getElement() + key_tag + t.getScore());
										checkExploringLogger.info(loggerPrefix + " " + t.getElement() + " : " + t.getScore());
									}
									checkExploringLogger
											.info("hsWaitingSupplementData.size() -> " + CollectionUtils.size(hsWaitingSupplementData));

									// result value
//									String[] arrContentIdImpression = jedisCluster
//											.hmget(exploreParameter.getRedisKeyForImpressionCount(), listContentIds.toArray(new String[0]))
//											.toArray(new String[0]);
									
									String[] arrContentIdImpression = jedisCluster
											.hmget(exploreParameter.getRedisKeyForAllImpressionCount(), listContentIds.toArray(new String[0]))
											.toArray(new String[0]);
									checkExploringLogger.info("arrContentIdImpression -> " + Arrays.toString(arrContentIdImpression)
											+ " arrContentIdImpression size -> " + arrContentIdImpression.length);

									// merge result hash
									Map<String, Integer> hmContentIdsImpression = new HashMap<String, Integer>();
									List<String> listNeedToDeletedContent = new ArrayList<String>();
									int i = 0;
									for (String tmp : listContentIds) {
										try {
											hmContentIdsImpression.put(hmContentIdsData.get(tmp),
													Integer.valueOf(arrContentIdImpression[i++]));
										} catch (Exception e) {
											// ignore
											checkExploringLogger.error(e);
											//如果解析impression count失败，则可以认为该资讯数据为无效数据，从各个池中删掉
											try {
												listNeedToDeletedContent.add(hmContentIdsData.get(tmp).split(key_tag)[0]);
											} catch (Exception e1) {
												checkExploringLogger.error(e1);
											}
										}
									}
									checkExploringLogger
											.info("hmContentIdsImpression size -> " + CollectionUtils.size(hmContentIdsImpression));

									List<Map.Entry<String, Integer>> listImpressionSort = new ArrayList<Map.Entry<String, Integer>>(
											hmContentIdsImpression.entrySet());

									// 排序
									Collections.sort(listImpressionSort, new Comparator<Map.Entry<String, Integer>>() {
										public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
											return (o2.getValue() - o1.getValue());
										}
									});

									Set<Tuple> hsWaitingSupplementDataSorted = new HashSet<Tuple>();
									int threshold = exploreParameter.getThreshold();
									// 排序后
									for (Map.Entry<String, Integer> tmp : listImpressionSort) {
										String[] arrcontentIdData = tmp.getKey().split(key_tag);
										if (arrcontentIdData != null && arrcontentIdData.length > 1) {
											int impressionCount = tmp.getValue();
											// 完成探索
											if (impressionCount >= threshold) {
												checkExploringLogger.info(loggerPrefix + " " + "impressionCount >= requiredFeedbackCount");
												listNeedToDeletedContent.add(arrcontentIdData[0]);
												logParam = new Object[] { EVENT_FINISH_FLOW_EXPLORE, waitingKey, arrcontentIdData[0],
														impressionCount, System.currentTimeMillis() };
												strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
												impFlowExploreLogger.info(strLogData);
												checkExploringLogger.info(loggerPrefix + " " + strLogData);
												continue;
											} else {
												hsWaitingSupplementDataSorted
														.add(new Tuple(arrcontentIdData[0], Double.valueOf(arrcontentIdData[1])));
												checkExploringLogger
														.info("arrcontentIdData add a entry-> " + tmp.getKey() + " : " + tmp.getValue());
												if (CollectionUtils.size(hsWaitingSupplementDataSorted) >= waitingSupplementNum) {
													break;
												}
											}
										}
									}
									checkExploringLogger
											.info("listNeedToDeletedContent size -> " + CollectionUtils.size(listNeedToDeletedContent));
									if (CollectionUtils.size(listNeedToDeletedContent) > 0) {
										checkAllResultList(exploreParameter, listNeedToDeletedContent, null, null);
									}
									checkExploringLogger.info(loggerPrefix + " " + "end optimizes");

									doAddExploringProcess(exploreParameter, hsWaitingSupplementDataSorted, true);
								} else if (waitingSize > 0) {
									checkExploringLogger.info(loggerPrefix + " " + "not need optimizes");
									Set<Tuple> hsWaitingSupplementData = jedisCluster.zrangeWithScores(waitingKey, 0,
											waitingSupplementNum - 1);
									if (CollectionUtils.size(hsWaitingSupplementData) > 0) {
										// step 1.2 如果从等待池中取得的数据不够多 则全加入进行池
										doAddExploringProcess(exploreParameter, hsWaitingSupplementData, true);
									}
								}

								needSupplementLength = (int) (safeLengthOfExploringPool - jedisCluster.zcard(exploringKey));
								checkExploringLogger.info(
										loggerPrefix + " " + "after waiting supplement needSupplementLength -> " + needSupplementLength);
							}

							// step 2. 剩下的资讯全都从探索池里面出
							long explore_size = jedisCluster.zcard(exploreKey);
							checkExploringLogger.info(loggerPrefix + " " + "explore size -> " + explore_size);
							if ((needSupplementLength > 0) && (explore_size > 0)) {
								doAddExploringProcess(exploreParameter,
										jedisCluster.zrangeWithScores(exploreKey, 0, needSupplementLength - 1), false);
							}
						} catch (Exception e) {
							checkExploringLogger.error("", e);
						}
					}
				} catch (Exception e) {
					checkExploringLogger.error(loggerPrefix, e);
				}
				checkExploringLogger.info(loggerPrefix + " " + "end checkExploringPool " + System.currentTimeMillis());
				synchronized (mapDoCheckingExploringPool) {
					mapDoCheckingExploringPool.put(exploreKey, false);
				}
				checkExploringLogger.info(loggerPrefix + " " + "mapDoCheckingExploringPool :" + mapDoCheckingExploringPool);
			}
		});
	}

	private void doAddExploringProcess(ExploreParameter exploreParameter, Set<Tuple> hsSupplementData, boolean fFromWaiting) {
		String loggerPrefix = exploreParameter.getApp() + "_" + exploreParameter.getScenario() + "_" + exploreParameter.getType();
		String exploreKey = exploreParameter.getRedisKeyForExplore();
		String waitingKey = exploreParameter.getRedisKeyForWaiting();

		checkExploringLogger
				.info(loggerPrefix + " " + "doAddExploringProcess hsSupplementData size -> " + CollectionUtils.size(hsSupplementData));
		checkExploringLogger.info(loggerPrefix + " " + "doAddExploringProcess fFromWaiting -> " + fFromWaiting);

		// 准备将新资讯写入进行池
		// score是当前的时间+下发次数 - 2017051719045
		if (CollectionUtils.isNotEmpty(hsSupplementData)) {
//			String impressionOfcontentIdKey = exploreParameter.getRedisKeyForImpressionCount();
			String impressionOfcontentIdKey = exploreParameter.getRedisKeyForAllImpressionCount();
			int requiredFeedbackCount = exploreParameter.getThreshold();

			// 非法异常数据、已经达到阈值，从所有的池中清理
			// 情景 1.发布时间太长
			// 情景 2.没有资讯详情
			// 情景 3.没有impression数据统计key
			// 情景 4.在等待池等待时间太长
			// 情景 5.impression反馈统计达到阈值
			List<String> listNeedToDeletedContent = new ArrayList<String>();
			// 从探索池补进行池
			List<String> listExploreIntoExploring = new ArrayList<String>();
			// 从等待池补进行池
			List<String> listWaitingIntoExploring = new ArrayList<String>();

			for (Tuple tupleContentData : hsSupplementData) {
				checkExploringLogger.info(
						loggerPrefix + " " + "current process data " + tupleContentData.getElement() + " : " + tupleContentData.getScore());

				String contentData = tupleContentData.getElement();
				String[] arrContentData = contentData.split("#");
				String contentId = arrContentData[0];

				/**
				 * 待V2.0版本的最后有效时间上线在开启这段code 过滤过期的资讯 1054192703# 1# Type 4098#
				 * Link_type 1063# Display_type 1494968700 publishTime
				 */
				/**
				 * if (arrContentData.length == 5) { long publishTime =
				 * Long.valueOf(arrContentData[4]); if ((new Date()).getTime() /
				 * 1000 - publishTime >
				 * exploreParameter.getAvailablePublishTimeSecond()) {
				 * listNeedToDeletedContent.add(contentId);
				 * 
				 * logParam = new Object[] {
				 * EVENT_STOP_EXPLORE_PUBLISHTIME_TOO_OLD,
				 * exploreParameter.getApp(), contentData,
				 * System.currentTimeMillis() }; strLogData = LOG_TAG +
				 * StringUtils.join(logParam, LOG_TAG);
				 * impFlowExploreLogger.info(strLogData);
				 * checkExploringLogger.info(exploreKey+" "+strLogData);
				 * continue; } }
				 */

				// 判断是否有详情
				if (!jedisCluster.hexists(exploreParameter.getRedisKeyForContentInfo(contentId), "INFO")) {
					listNeedToDeletedContent.add(contentData);

					logParam = new Object[] { EVENT_STOP_EXPLORE_NO_NEWS_INFO, exploreParameter.getApp(), contentData,
							System.currentTimeMillis() };
					strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
					impFlowExploreLogger.info(strLogData);
					checkExploringLogger.info(loggerPrefix + " " + strLogData);
					continue;
				}

				// 获取反馈数据中的反馈次数 feeder初始化-1
				String strImpressionCount = jedisCluster.hget(impressionOfcontentIdKey, contentId);
				// 每篇资讯当前已经累积的impression数 初始化为一个超过阈值的数 以免产生不必要的下发处理
				int impressionCount = requiredFeedbackCount + 1;
				try {
					impressionCount = Integer.valueOf(strImpressionCount);
				} catch (Exception e) {
					// [重要] ***非法数据，将其从所有的池中删除***
					// 情景. A线程将已经完成探索的资讯删除了，B线程正在便利该ID，会出现无impression的情况
					listNeedToDeletedContent.add(contentData);
					checkExploringLogger.error(contentId + " Integer.valueOf(strImpressionCount) in " + impressionOfcontentIdKey + " exception " + e);
					continue;
				}

				/**
				 * 反馈次数进探索池之前会初始化成 -1， 进入进入池之后会从-1设置为 0 进入进行池之前反馈次数分析： a. 反馈次数等于
				 * -1, 为新探索文章 b. 反馈次数不等于 -1 且不满阀值 - 反馈展示量等于 0, 表示已完成一轮下发在等待反馈 -
				 * 反馈展示量不等于 0, 表示不是第一次来，或是第一次从探索池来之前就有混插, keyword
				 * 相关性或在其它同语系产品上的下发 c. 反馈次数超过阀值，直接从所有池子中去除，不再需要进行探索
				 */
				checkExploringLogger.info(loggerPrefix + " " + "impressionCount -> " + impressionCount + " requiredFeedbackCount -> "
						+ requiredFeedbackCount);
				if (impressionCount < 0) {
					// 第一次进入进行池，直接从探索池来的资讯
					listExploreIntoExploring.add(contentData);
				} else if (impressionCount < requiredFeedbackCount) {
					if (fFromWaiting) {
						// 每一篇资讯在等待池必须等待一定的时间
						int mixWaitingTimeSecond = (int) Math.ceil(exploreParameter.getWaitingFeedbackTimeSecond()
								* (requiredFeedbackCount - impressionCount) * 1.0 / requiredFeedbackCount);
						mixWaitingTimeSecond = Math.max(exploreParameter.getMinWaitingTimeSecond(), mixWaitingTimeSecond);

						// 如果该资讯已经在等待池中待到了一定时间 milliseconds
						int waitedTimeSecond = (int) (new Date().getTime() - tupleContentData.getScore()) / 1000;
						checkExploringLogger.info(loggerPrefix + " " + "mixWaitingTimeSecond -> " + mixWaitingTimeSecond
								+ " waitedTimeSecond -> " + waitedTimeSecond);

						if (waitedTimeSecond > exploreParameter.getMaxWaitingTimeSecond()) {
							// 等待时间太长，清掉这条数据
							listNeedToDeletedContent.add(contentData);

							logParam = new Object[] { EVENT_STOP_EXPLORE_IN_WAITING_TOO_LONG, exploreParameter.getApp(), contentData,
									System.currentTimeMillis() };
							strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
							impFlowExploreLogger.info(strLogData);
							checkExploringLogger.info(loggerPrefix + " " + strLogData);
						} else if (waitedTimeSecond >= mixWaitingTimeSecond) {
							checkExploringLogger.info(loggerPrefix + " " + "add " + contentData + " from waiting into exploring");
							// 等待时间合理，不太长也不太短
							listWaitingIntoExploring.add(contentData);
						}else{
							checkExploringLogger.info(loggerPrefix + " " + " remove " + contentData + " from waiting into exploring ,because waitedTimeSecond < mixWaitingTimeSecond");
						}

					} else {
						// 从探索池来的资讯直接补充到进行池
						listExploreIntoExploring.add(contentData);
					}
				} else {
					checkExploringLogger.info(loggerPrefix + " " + "impressionCount >= requiredFeedbackCount");
					listNeedToDeletedContent.add(contentData);

					if (fFromWaiting) {
						logParam = new Object[] { EVENT_FINISH_FLOW_EXPLORE, waitingKey, contentData, impressionCount,
								System.currentTimeMillis() };
					} else {
						logParam = new Object[] { EVENT_ENOUGH_IMPRESSION_BEFORE_EXPLORE, exploreKey, contentData, impressionCount,
								System.currentTimeMillis() };
					}
					strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
					impFlowExploreLogger.info(strLogData);
					checkExploringLogger.info(loggerPrefix + " " + strLogData);
				}
			}

			checkAllResultList(exploreParameter, listNeedToDeletedContent, listExploreIntoExploring, listWaitingIntoExploring);
		}
	}

	private void checkAllResultList(ExploreParameter exploreParameter, List<String> listNeedToDeletedContent,
			List<String> listExploreIntoExploring, List<String> listWaitingIntoExploring) {
		String loggerPrefix = exploreParameter.getApp() + "_" + exploreParameter.getScenario() + "_" + exploreParameter.getType();
		String exploreKey = exploreParameter.getRedisKeyForExplore();
		String exploringKey = exploreParameter.getRedisKeyForExploring();
		String waitingKey = exploreParameter.getRedisKeyForWaiting();
//		String impressionOfcontentIdKey = exploreParameter.getRedisKeyForImpressionCount();
		String impressionOfcontentIdKey = exploreParameter.getRedisKeyForAllImpressionCount();
		String impressionAppOfContentIdKey = exploreParameter.getRedisKeyForImpressionCount();

		double multipleOfFirstRound = exploreParameter.getMultipleOfFirstRound();
		double multipleOfOtherRound = exploreParameter.getMultipleOfOtherRound();
		int requiredFeedbackCount = exploreParameter.getThreshold();

		// 探索池补进行池
		if (CollectionUtils.isNotEmpty(listExploreIntoExploring)) {
			checkExploringLogger
					.info(loggerPrefix + " " + "listExploreIntoExploring size -> " + CollectionUtils.size(listExploreIntoExploring));

			for (String contentData : listExploreIntoExploring) {
				checkExploringLogger.info(loggerPrefix + " " + "contentData -> " + contentData);

				try {
					String contentId = contentData.split("#")[0];

					// 初始化impression统计数据
					int impressionCount = Integer.parseInt(jedisCluster.hget(impressionOfcontentIdKey, contentId));
					checkExploringLogger.info(loggerPrefix + " " + "impressionCount -> " + impressionCount);
					if (impressionCount < 0) {
						jedisCluster.hset(impressionOfcontentIdKey, contentId, "0");
						impressionCount = 0;
					}
					checkExploringLogger.info(loggerPrefix + " " + "init impressionCount -> " + impressionCount);

					// 初始化下发次数
					int needRequestCount = getBoundedRequestNum(multipleOfFirstRound * (requiredFeedbackCount - impressionCount));
					String needRequestCountKey = exploreParameter.getRedisKeyForNeedRequestCount(contentId);
					jedisCluster.set(needRequestCountKey, Integer.toString(needRequestCount));
					jedisCluster.expire(needRequestCountKey, 72000);
					checkExploringLogger.info(loggerPrefix + " " + "needRequestCount -> " + needRequestCount);

					// 从探索池移入进行池
					jedisCluster.zrem(exploreKey, contentData);
					jedisCluster.zadd(exploringKey, exploreParameter.getExploringScore(needRequestCount), contentData);

					logParam = new Object[] { EVENT_FIRST_INTO_FLOW_EXPLORING, exploreKey, contentData, needRequestCount,
							System.currentTimeMillis() };
					strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
					impFlowExploreLogger.info(strLogData);
					checkExploringLogger.info(loggerPrefix + " " + strLogData);
				} catch (Exception e) {
					checkExploringLogger.error(e);
				}
			}
		}

		// 等待池补进行池
		if (CollectionUtils.isNotEmpty(listWaitingIntoExploring)) {
			checkExploringLogger
					.info(loggerPrefix + " " + "listWaitingIntoExploring size -> " + CollectionUtils.size(listWaitingIntoExploring));

			for (String contentData : listWaitingIntoExploring) {
				checkExploringLogger.info(loggerPrefix + " " + "contentData -> " + contentData);

				try {
					String contentId = contentData.split("#")[0];

					// 初始化下发次数
					int impressionCount = Integer.parseInt(jedisCluster.hget(impressionOfcontentIdKey, contentId));
					int needRequestCount = getBoundedRequestNum(multipleOfOtherRound * (requiredFeedbackCount - impressionCount));
					String needRequestCountKey = exploreParameter.getRedisKeyForNeedRequestCount(contentId);
					jedisCluster.set(needRequestCountKey, Integer.toString(needRequestCount));
					jedisCluster.expire(needRequestCountKey, 72000);

					checkExploringLogger.info(loggerPrefix + " " + "impressionCount -> " + impressionCount);
					checkExploringLogger.info(loggerPrefix + " " + "needRequestCount -> " + needRequestCount);

					// 从等待池移入进行池
					jedisCluster.zrem(waitingKey, contentData);
					jedisCluster.zadd(exploringKey, exploreParameter.getExploringScore(needRequestCount), contentData);

					logParam = new Object[] { EVENT_INTO_FLOW_EXPLORING_AGAIN, exploreKey, contentData,
							getBoundedRequestNum(needRequestCount), System.currentTimeMillis() };
					strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
					impFlowExploreLogger.info(strLogData);
					checkExploringLogger.info(loggerPrefix + " " + strLogData);
				} catch (Exception e) {
					checkExploringLogger.error(e);
				}
			}
		}

		/**
		 * 将完成探索资讯 a. 从探索池中删除 b. 从等待池删除 c. 从进行池删除 d. 从探索累计key中删除
		 */
		if (CollectionUtils.isNotEmpty(listNeedToDeletedContent)) {
			checkExploringLogger
					.info(loggerPrefix + " " + "listNeedToDeletedContent size -> " + CollectionUtils.size(listNeedToDeletedContent));
			checkExploringLogger.info(
					loggerPrefix + " " + "listNeedToDeletedContent -> " + Arrays.toString(listNeedToDeletedContent.toArray(new String[0])));

			jedisCluster.zrem(exploreKey, listNeedToDeletedContent.toArray(new String[0]));
			jedisCluster.zrem(waitingKey, listNeedToDeletedContent.toArray(new String[0]));
			jedisCluster.zrem(exploringKey, listNeedToDeletedContent.toArray(new String[0]));

			List<String> listContentId = getIdsFromContentData(listNeedToDeletedContent);
			if (CollectionUtils.isNotEmpty(listContentId)) {
				jedisCluster.hdel(impressionAppOfContentIdKey, listContentId.toArray(new String[0]));//app-impression-key
			}
		}
	}

	private List<String> getIdsFromContentData(Collection<String> collectionContentData) {
		List<String> listContentId = new ArrayList<String>();
		for (String contentData : collectionContentData) {
			try {
				listContentId.add(contentData.split("#")[0]);
			} catch (Exception e) {
				checkExploringLogger.error(e);
			}
		}
		return listContentId;
	}

	/**
	 * 获取当前有效的和 注意两种情况 1、返回值为0
	 * 2、取得返回值，获取数据的这个阶段进行池的数据已经发生了改动，所以需要zrange去数据后确认返回结果
	 * 
	 * @param exploreParameter
	 * @return
	 */
	private int getEffectiveRange(ExploreParameter exploreParameter) {
		String exploringKey = exploreParameter.getRedisKeyForExploring();
		return (int) Math.min(exploreParameter.getEffectiveRange(), jedisCluster.zcard(exploringKey));
	}

	/**
	 * 流量探索位填充
	 * 
	 * @param mRequestIndexResponse
	 *            需要进行探索的位置， 不需要care探索类型
	 * @param hsAllResponse
	 *            所有下发的content集合，不能在一屏中出现两个相同的资讯
	 */
	public void doExploreProcess(Context context,ExploreParameter exploreParameter, Map<Integer, ResponParam> mRequestIndexResponse,
			Set<String> hsAllResponse) {
		String scenario = exploreParameter.getScenario();
		String loggerPrefix = exploreParameter.getApp() + "_" + scenario + "_" + exploreParameter.getType();
		String exploringKey = exploreParameter.getRedisKeyForExploring();
		String waitingKey = exploreParameter.getRedisKeyForWaiting();

		exploreProcessLogger.info(loggerPrefix + " " + "start doExploreProcess " + System.currentTimeMillis());
		exploreProcessLogger.info(loggerPrefix + " " + "mRequestIndexResponse size ->  " + CollectionUtils.size(mRequestIndexResponse));

		int maxRetryCount = exploreParameter.getMaxRetryCount();
		long stillNeedRequestCount = -1;// 下发完当前一轮后，还需要下发的次数

		for (int index : mRequestIndexResponse.keySet()) {
			ResponParam respon_param = null;
			try {
				double value = Math.random();
				exploreProcessLogger.info(loggerPrefix + " offset -> " + exploreParameter.getOffset() + "; index ->  " + index + " ; random -> " + value);
				if (value > exploreParameter.getProbability()) {
					continue;
				}

				String contentId = "";
				String contentMsg = "";
				int tryFillCount = 0;
				do {
					if (tryFillCount >= maxRetryCount) {
						break;
					}
					tryFillCount++;

					int range = getEffectiveRange(exploreParameter);
					if (range <= 0) {
						respon_param = null;
						break;
					}
					int randomExploringIndex = new Random().nextInt(range);
					contentMsg = jedisCluster.zrange(exploringKey, randomExploringIndex, randomExploringIndex).toArray(new String[0])[0];
					contentId = contentMsg.split("#")[0];
					respon_param = new ResponParam(Strategy.FLOW_EXPLORATION.getCode(), contentId);
					exploreProcessLogger.info(loggerPrefix + " " + "fill index tryFillCount -> " + tryFillCount + " getEffectiveRange ->  "
							+ range + " ; contentMsg -> " + contentMsg);

				} while (!hsAllResponse.add(contentId) || !filterIds(context,exploreParameter, contentMsg));

				// log
				if (tryFillCount >= maxRetryCount) {
					logParam = new Object[] { EVENT_WASTED_EXPLORE_COUNT, exploringKey, System.currentTimeMillis() };
					strLogData = LOG_TAG + StringUtils.join(logParam, LOG_TAG);
					impFlowExploreLogger.info(strLogData);
					exploreProcessLogger.info(loggerPrefix + " " + strLogData);
					continue;
				}

				if (respon_param == null) {
					exploreProcessLogger.info(loggerPrefix + " " + "no data in exploring , fill index " + index + " faild.");
					continue;
				}

				// 数据结果产出
				mRequestIndexResponse.put(index, respon_param);
				exploreProcessLogger.info("fill index ->  " + index + " respon_param_infoId ->  " + respon_param.getInfoid());

				// 还需要下发的次数
				stillNeedRequestCount = jedisCluster.incrBy(exploreParameter.getRedisKeyForNeedRequestCount(contentId), -1);
				exploreProcessLogger.info(loggerPrefix + " " + exploreParameter.getRedisKeyForNeedRequestCount(contentId)
						+ " stillNeedRequestcount -> " + stillNeedRequestCount);

				// 如果不需要再下发了 将其从进行池中移入等待池
				if (stillNeedRequestCount <= 0) {
					exploreProcessLogger.info(loggerPrefix + " " + "move from explore into waiting ");

					final String contentIdFinal = contentId;
					final String contentMsgFinal = contentMsg;

					threadPoolTaskExecutor.submit(() -> {
						jedisCluster.del(exploreParameter.getRedisKeyForNeedRequestCount(contentIdFinal));

						jedisCluster.zrem(exploringKey, contentMsgFinal);

						if (ExploreParameter.EXPLORE_STRATEGY_IMPRESSION_BASED.equalsIgnoreCase(exploreParameter.getStrategy())) {
							jedisCluster.zadd(waitingKey.getBytes(), exploreParameter.getWaitingScore(), contentMsgFinal.getBytes());
						}
					});
				}
			} catch (Exception e) {
				exploreProcessLogger.info(
						loggerPrefix + " " + "file index ->  " + index + " failed" + " stillNeedRequestCount -> " + stillNeedRequestCount);

				// 本次流量探索位置补充失败了
				mRequestIndexResponse.put(index, null);

				// 如果已经将需要下发数减1了，需要重新恢复
				if (respon_param != null && stillNeedRequestCount >= 0) {
					jedisCluster.incrBy(exploreParameter.getRedisKeyForNeedRequestCount(respon_param.getInfoid()), 1);
				}
				exploreProcessLogger.error(e.getMessage());
			}
		}
		exploreProcessLogger.info(loggerPrefix + " " + "end doExploreProcess " + System.currentTimeMillis());
	}

	/**
	 * @param contentInfo
	 * @return
	 */
	public boolean filterIds(Context context,ExploreParameter exploreParameter, String contentInfo) {
		String loggerPrefix = exploreParameter.getApp() + "_" + exploreParameter.getScenario() + "_" + exploreParameter.getType();

		String uid = exploreParameter.getUid();
		String app = exploreParameter.getApp();
		boolean bFilterTypes = exploreParameter.isFilterByTypes();

		String contentId = contentInfo.split("#")[0];
		if (bFilterTypes) {
			ArticleFilterImpl articleFilter = exploreParameter.getArticleFilterImpl();
			ArticleProperty articleProperty = exploreParameter.getArticleProperty();

			String[] contentInfoArr = contentInfo.split("#");
			if (contentInfoArr.length == 5) {
				articleProperty.setType(contentInfoArr[1]);
				articleProperty.setLink_type(contentInfoArr[2]);
				articleProperty.setDisplay_type(contentInfoArr[3]);
				articleProperty.setContent_id(contentInfoArr[0]);

				if (exploreProcessLogger.isDebugEnabled()) {
					exploreProcessLogger.debug("uid is " + uid + " articleProperty  is " + JSON.toJSONString(articleProperty)
							+ " articleFilter is " + JSON.toJSONString(articleFilter));
				}

				if (!articleFilter.isValidArticle(articleProperty)) {
					exploreProcessLogger.info("articleFilter.isValidArticle(articleProperty) filter " + contentInfo);

					final String exploringKey = exploreParameter.getRedisKeyForExploring();
					final String failCountKey = exploreParameter.getRedisKeyForFilterTypeFailCount(contentInfo);
					threadPoolTaskExecutor.submit(() -> {
						int nFilterTypeFailCountToRemove = exploreParameter.getFilterTypeFailCountToRemove();
						long value = jedisCluster.incrBy(failCountKey, 1);
						if (value >= nFilterTypeFailCountToRemove) {
							exploreProcessLogger.error("nFilterTypeFailCountToRemove than " + nFilterTypeFailCountToRemove + " uid is "
									+ uid + " , and contentID " + contentId + " stay list too long , need to delete and info is "
									+ contentInfo);
							jedisCluster.zrem(exploringKey, contentInfo);
							jedisCluster.expire(failCountKey, 10);
						}
					});

					return false;
				}
			}
		}

		// 用户已读列表
		try {
			List<String> ids = readFilterInfo.filterIdList(context,uid, app, Arrays.asList(contentId));
			if (CollectionUtils.isEmpty(ids)) {
				exploreProcessLogger.info(loggerPrefix + " " + "readFilterInfo filter " + contentInfo);
				return false;
			}
			exploreProcessLogger.info(loggerPrefix + " " + "readFilterInfo will not filter " + contentInfo);
		} catch (Exception e) {
			exploreProcessLogger.error("read filter Exception is " + uid + " expinfo id is " + contentId + ", length is " + 1, e);
		}finally{
			exploreProcessLogger.info(ContextUtils.toRequestInfoString(context)+ " | moudle=AbstractExplore,filterList length= "+1);
		}

		return true;
	}
}
