package com.inveno.core.process.expinfo.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.expinfo.ExpInfoOfImpressionProcess;
import com.inveno.core.process.expinfo.face.AbstractExpInfo;
import com.inveno.core.process.expinfo.face.AbstractExpInfoOfImpression;

import net.qihoo.qconf.Qconf;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component("removeIdForFlow")
public class RemoveIdForFlow {

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private JedisCluster jedisClusterDetail;

	public static Log logger = LogFactory.getLog(RemoveIdForFlow.class);

	public static Log flowExpImpLog = LogFactory.getLog("flowExpPVLog");

	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHH");

	public static Log imp_flow_explore_logger = ExpInfoOfImpressionProcess.imp_flow_explore_logger;

	public static final String LOG_TAG = ExpInfoOfImpressionProcess.LOG_TAG;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	/**	
	 * 将等待池里的资讯移到进行池
	 */
	public synchronized void remove() {
		//noticias=Spanish;noticias=Spanish=video;noticiasboom=Spanish;noticiasboom=Spanish=video;noticiasboomcolombia=Spanish;
		//noticiasboomcolombia=Spanish=video;noticiasboomchile=Spanish;noticiasboomchile=Spanish=video;noticias=Spanish=meme
		// 10.10.20.113
		String taskApp = "ali=zh_CN=video;ali=zh_CN";
		String taskIp = "";
		try {
			taskApp = Qconf.getConf("/zhizi/core/task/taskApp");
			taskIp = Qconf.getConf("/zhizi/core/task/taskIp");
		} catch (Exception e) {
			logger.error("=== get qconf error,has no [task] config", e);
		}

		// 每一个进入等待池的资讯都必须待够一段时间， 配置是10Min
		int timeDelay = 10 * 60 * 1000; // 10分钟
		// 每一个待在等待池的资讯都不能待太久了， 配置是48H
		long timeDelay2Del = 60 * 1000 * 60 * 2;// 两个小时

		// int expPVCnt = 50;
		// impression阈值， 配置是30
		int needImpCnt = 50;
		// 例如:request 下发50次可能只有40次的impression ,则值为:50/40
		// 第二次进入进行池的下发比例，配置是1.4
		double req2ImpressionPer = 1.2;
		// int videoScenario = 65799;

		// 分产品遍历redis key
		flowExpImpLog.info("do trigger " + System.currentTimeMillis() + " taskApp " + taskApp);
		String[] arr_task = taskApp.split(";");
		for (String task : arr_task) {
			String app = task.split("=")[0];
			String language = task.split("=")[1];
			boolean ifVideo = task.contains("=video");
			boolean ifMEME = task.contains("=meme");
			flowExpImpLog.info("do trigger task=" + task + "\tapp=" + app + "\tlanguage=" + language);
			// 如果下发的和现在超过10分钟,则需要加入到正在下发列表中
			try {
				if (taskIp.contains(MonitorLog.hostname)) {
					if (!ifMEME) {
						if (!System.getProperties().getProperty("os.name").startsWith("Windows")) {
							try {
								// 上报延迟时间,下发次数
								timeDelay = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/timeDelay"));
								timeDelay2Del = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/timeDelay2Del"));
								needImpCnt = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/needImpCnt"));
								req2ImpressionPer = Double.parseDouble(Qconf.getConf("/zhizi/core/task/" + app + "/req2ImpressionPer"));
							} catch (Exception e) {
								logger.error(
										"===do trigger get qconf error,has no [timeDelay,timeDelay2Del,needImpCnt,req2ImpressionPer] config",
										e);
							}
						}
					} else {
						if (!System.getProperties().getProperty("os.name").startsWith("Windows")) {
							try {
								// 上报延迟时间,下发次数
								timeDelay = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/meme/timeDelay"));
								timeDelay2Del = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/meme/timeDelay2Del"));
								needImpCnt = Integer.parseInt(Qconf.getConf("/zhizi/core/task/" + app + "/meme/needImpCnt"));
								req2ImpressionPer = Double
										.parseDouble(Qconf.getConf("/zhizi/core/task/" + app + "/meme/req2ImpressionPer"));
							} catch (Exception e) {
								logger.error(
										"===do trigger meme get qconf error,has no [timeDelay,timeDelay2Del,needImpCnt,req2ImpressionPer] config",
										e);
							}
						}
					}

					Map<String, String> mExpInfoKey = AbstractExpInfo.getExpInfoKey(app, language, ifVideo, ifMEME);
					String expinfo_key = mExpInfoKey.get("origin");
					String flowing_key = mExpInfoKey.get("flowing");
					String eagerly_key = mExpInfoKey.get("eagerly");
					String impressionOfInfoIdKey = mExpInfoKey.get("impression");

					// 获取已经下发,但是没有达到impression或者等待上报 次数的资讯id
					Set<Tuple> idsWithScores = jedisCluster.zrangeWithScores(eagerly_key, 0, -1);

					List<String> add2Flowing = new ArrayList<String>();
					Map<String, Double> add2FlowingMap = new HashMap<String, Double>();

					// 完成流量探索的资讯
					List<String> enoughList = new ArrayList<String>();
					List<String> enoughIDList = new ArrayList<String>();

					// 过期了不需要再等待的资讯
					List<String> toRemoveList = new ArrayList<String>();
					List<String> toRemoveIDList = new ArrayList<String>();

					for (Tuple t : idsWithScores) {
						// 大于延迟上报时间
						if (new Date().getTime() - t.getScore() > timeDelay) {
							String infoId = t.getElement().split("#")[0];
							flowExpImpLog.info("do trigger for impressionOfInfoIdKey=" + impressionOfInfoIdKey + "\tid=" + t.getElement());
							// 如果再0记录次数中不存在,则有可能是由于没有进行上报
							String strImpressionCount = "";
							int impressionCount = 0;
							//added by genix@2017/04/22, if hexists is false, jedisCluster may throw exception to interrupt the process of whole process for the app.
							try {
								strImpressionCount = jedisCluster.hget(impressionOfInfoIdKey, infoId);
								impressionCount = Integer.parseInt(strImpressionCount);
							} catch (Exception e) {
								flowExpImpLog.error("do trigger doWithImpressionCnt for hget " + impressionOfInfoIdKey + " " + infoId);
							}
							// 没有任何impression回来的时候继续等待，不做任何处理
							if (StringUtils.isNotEmpty(strImpressionCount) && !strImpressionCount.equals("0")) {
								// 小于需要的impression次数
								if (impressionCount < needImpCnt) {
									// 判断是否已经在进行池了
									// 如果是，说明当前正在处理的时候这篇资讯已经因为其他逻辑处理了（如，进行池，探索池资讯不足，主程序会拉取等待池数据进进行池），这条数据不处理
									// 如果不是，则将数据插入进行池，几下log
									// type flowing_key is a zset, should apply zrank to check member exists or not.
									boolean isExists = false;
									try {
										Long rank = jedisCluster.zrank(flowing_key, t.getElement());
										if (rank != null)
											isExists = true;
									} catch (Exception e) {
										flowExpImpLog
												.error("do trigger doWithImpressionCnt for zrank " + flowing_key + " " + t.getElement());
									}
									if (!isExists) {
										flowExpImpLog.info("do trigger add to flowing_key id is " + infoId + " , and score is "
												+ t.getScore() + ",impression " + impressionCount);

										add2Flowing.add(t.getElement());
										int need2pv = (int) Math.ceil((needImpCnt - impressionCount) * req2ImpressionPer);
										// 确保最小下发次数 10
										need2pv = AbstractExpInfo.getBoundedRequestNum(need2pv);

										add2FlowingMap.put(t.getElement(), Double
												.valueOf(sf.format(new Date()) + StringUtils.leftPad(String.valueOf(need2pv), 3, "0")));

										String need_request_key = AbstractExpInfoOfImpression.getRedisKeyForNeedRequest(infoId, app);
										jedisCluster.set(need_request_key, Integer.toString(need2pv));

										flowExpImpLog.info("do trigger info id " + infoId + " , need to pv " + need2pv);

										Object[] param = new Object[] { AbstractExpInfoOfImpression.EVENT_INTO_FLOW_EXPLORING_AGAIN, task,
												t.getElement(), need2pv, System.currentTimeMillis() };
										String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
										imp_flow_explore_logger.info(str);
									}
								} else if (impressionCount >= needImpCnt) {
									enoughList.add(t.getElement());
									enoughIDList.add(infoId);
									flowExpImpLog.info("do trigger enough request cnt ,impression " + impressionCount + " info id " + infoId
											+ " , need to remove from  " + impressionOfInfoIdKey + ", ifVideo " + ifVideo);

									Object[] param = new Object[] { AbstractExpInfoOfImpression.EVENT_FINISH_FLOW_EXPLORE, task,
											t.getElement(), impressionCount, System.currentTimeMillis() };
									String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
									imp_flow_explore_logger.info(str);
								}
							}

							// 上报延迟个小时,太大则需要进行移除
							if (new Date().getTime() - t.getScore() >= timeDelay2Del) {
								flowExpImpLog.info(
										"do trigger timeDelay2Del delete ,impression " + impressionCount + " info id "
												+ infoId + " , need to remove from  " + impressionOfInfoIdKey + ", ifVideo " + ifVideo);
								toRemoveList.add(t.getElement());
								toRemoveIDList.add(infoId);
								Object[] param = new Object[] { AbstractExpInfoOfImpression.EVENT_STOP_EXPLORE_IN_EAGERLY_TOO_LONG, task,
										t.getElement(), System.currentTimeMillis() };
								String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
								imp_flow_explore_logger.info(str);
							}

							/**
							 * 找出过期的资讯
							 *  1054192703#
							 *  1#          Type
							 *  4098#       Link_type
							 *  1063#       Display_type
							 *  1494968700  publishTime
							 */
							if (t.getElement().split("#").length == 5) {
								long publishTime = Long.valueOf(t.getElement().split("#")[4]);
								if (System.currentTimeMillis() / 1000 - publishTime > timeDelay2Del && !ifVideo) {
									flowExpImpLog.info("do trigger delete content before 48 hour ,impression "
											+ impressionCount + " info id " + infoId + " , need to remove from  "
											+ impressionOfInfoIdKey + ", ifVideo " + ifVideo);
									toRemoveList.add(t.getElement());
									toRemoveIDList.add(infoId);
									Object[] param = new Object[] { AbstractExpInfoOfImpression.EVENT_STOP_EXPLORE_PUBLISHTIME_TOO_OLD,
											task, t.getElement(), System.currentTimeMillis() };
									String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
									imp_flow_explore_logger.info(str);
								}
							}

							if (!jedisClusterDetail.hexists("news_" + infoId, "INFO")) {
								flowExpImpLog.info(
										"do trigger no feeder detail ,impression " + impressionCount + " info id "
												+ infoId + " , need to remove from  " + impressionOfInfoIdKey + ", ifVideo " + ifVideo);
								toRemoveList.add(t.getElement());
								toRemoveIDList.add(infoId);
								Object[] param = new Object[] { AbstractExpInfoOfImpression.EVENT_STOP_EXPLORE_NO_NEWS_INFO, task,
										t.getElement(), System.currentTimeMillis() };
								String str = LOG_TAG + StringUtils.join(param, LOG_TAG);
								imp_flow_explore_logger.info(str);
							}
						}
					}

					try {
						// 过滤掉一些无效的数据
						if (CollectionUtils.isNotEmpty(toRemoveList) && CollectionUtils.isNotEmpty(add2Flowing)) {
							add2Flowing.removeAll(toRemoveList);
						}

						if (CollectionUtils.isNotEmpty(add2Flowing)) {
							flowExpImpLog.info("do trigger add to flowing_key , add2Flowing " + add2Flowing + " ," + impressionOfInfoIdKey
									+ ", ifVideo " + ifVideo);
							jedisCluster.zrem(eagerly_key, add2Flowing.toArray(new String[0]));
							jedisCluster.zadd(flowing_key, add2FlowingMap);
						}

						if (CollectionUtils.isNotEmpty(enoughList)) {
							flowExpImpLog.info("do trigger  enough request cnt , enoughList " + enoughList + " ," + impressionOfInfoIdKey
									+ ", ifVideo " + ifVideo);
							/**
							 * 可以考虑删除探索池里面的数据							
							 */
							jedisCluster.zrem(eagerly_key, enoughList.toArray(new String[0]));
							jedisCluster.zrem(flowing_key, enoughList.toArray(new String[0]));
							jedisCluster.hdel(impressionOfInfoIdKey, enoughIDList.toArray(new String[0]));
							try {
								readFilterInfo.addIdToBloomByExp(app, language, enoughIDList);
							} catch (Exception e) {
							}
						}

						if (CollectionUtils.isNotEmpty(toRemoveList)) {
							flowExpImpLog.info("do trigger no feeder detail , toRemoveList " + toRemoveList + " ," + impressionOfInfoIdKey
									+ ", ifVideo " + ifVideo);
							/**
							 * 可以考虑删除探索池里面的数据							
							 */
							jedisCluster.zrem(eagerly_key, toRemoveList.toArray(new String[0]));
							jedisCluster.zrem(flowing_key, toRemoveList.toArray(new String[0]));
							jedisCluster.hdel(impressionOfInfoIdKey, toRemoveIDList.toArray(new String[0]));
						}
					} catch (Exception e) {
						flowExpImpLog.error("do trigger do add2Flowing and enoughList error ,and add2Flowing size " + add2Flowing
								+ ", enoughList enoughList " + enoughList, e);
						if (CollectionUtils.isNotEmpty(add2Flowing)) {
							jedisCluster.zrem(eagerly_key, add2Flowing.toArray(new String[0]));
							jedisCluster.zadd(flowing_key, add2FlowingMap);
						}

						if (CollectionUtils.isNotEmpty(enoughList)) {
							jedisCluster.zrem(eagerly_key, enoughList.toArray(new String[0]));
							jedisCluster.zrem(flowing_key, enoughList.toArray(new String[0]));
							jedisCluster.hdel(impressionOfInfoIdKey, enoughList.toArray(new String[0]));
						}
					}
				}
			} catch (Exception e) {
				flowExpImpLog.error("do trigger doWithImpressionCnt Exception key is " + task, e);
			}
		}
	}
}
