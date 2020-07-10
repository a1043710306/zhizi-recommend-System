package com.inveno.core.process.expinfo.face;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

import redis.clients.jedis.JedisCluster;

@Component
public class AbstractExpInfo implements ExpInfo {

	public static Log logger = LogFactory.getLog(AbstractExpInfo.class);

	private static final int MIN_REQUEST_NUM = 10;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Override
	public void getExpInfo(Context context, List<Double> indexListForNoCache, List<ResponParam> resultList, List<Double> indexList,
			Set<ResponParam> resultSet, boolean useImpression) {
	}

	/**
	 * 确保每次至少会下发一定数量的request
	 */
	public static int getBoundedRequestNum(int request_num) {
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

	public static Map<String, String> getExpInfoKey(String app, String language, boolean ifVideo, boolean ifMEME)
	{
		String prefix    = Constants.REDIS_KEY_PREFIX_EXPINFO;
		String separator = Constants.SEPARATOR_REDIS_KEY;

 		StringBuffer sb = new StringBuffer();
 		sb.append(prefix).append(separator).append(app);
 		if (!language.equalsIgnoreCase(Constants.ZH_CN))
 			sb.append(separator).append(language);
 		String key_base = sb.toString();
 		if (ifVideo)
 			sb.append(separator).append("video");
 		else if (ifMEME)
 			sb.append(separator).append("meme");
 		String expinfo_key = sb.toString();

		String eagerly_key = expinfo_key + separator + "eagerly";
		String flowing_key = expinfo_key + separator + "ing";
		String impressionOfInfoIdKey = key_base + separator + "impressionOfInfoIdKey";

		Map<String, String> mResult = new HashMap<String, String>();
		mResult.put("origin", expinfo_key);
		mResult.put("eagerly", eagerly_key);
		mResult.put("flowing", flowing_key);
		mResult.put("impression", impressionOfInfoIdKey);
		return mResult;
	}
	/**
	 *
	 * @param context
	 * @param contentInfo
	 * @param expRange
	 * @param flowExpLog
	 * @param key
	 * @return
	 */
	public boolean filterIds(Context context, String contentInfo, Log flowExpLog, Map<String, Object> configMap, String key) {

		boolean filterTypes = (boolean) configMap.get("filterTypes");
		String app = context.getApp();

		String infoId = contentInfo.split("#")[0];
		if (filterTypes) {
			ArticleFilterImpl articleFilter = context.getArticleFilter();
			ArticleProperty article_property = context.getArticleProperty();

			if (context.getZhiziListReq() != null) {

				String[] contentInfoArr = contentInfo.split("#");
				if (context.getZhiziListReq() != null && contentInfoArr.length == 5) {
					article_property.setType(contentInfoArr[1]);
					article_property.setLink_type(contentInfoArr[2]);
					article_property.setDisplay_type(contentInfoArr[3]);
					article_property.setContent_id(contentInfoArr[0]);

					if (logger.isDebugEnabled()) {
						logger.debug("uid is " + context.getUid() + " expinfo ids is " + contentInfo + ", " + "article_property  is "
								+ JSON.toJSONString(article_property) + " , and articleFilter + " + JSON.toJSONString(articleFilter));
					}

					if (!articleFilter.isValidArticle(article_property)) {
						long publishTime = Long.valueOf(contentInfoArr[4]);

						threadPoolTaskExecutor.submit(() -> {
							int flowfilterTypeFailCnt2Remove = (int) configMap.get("flowfilterTypeFailCnt2Remove");
							long value = jedisCluster.incrBy(contentInfo + "_filterTypeFailCnt", 1);
							if (value >= flowfilterTypeFailCnt2Remove) {
								flowExpLog.error("flowfilterTypeFailCnt2Remove than 100 uid is " + context.getUid() + " , and contentID "
										+ infoId + " stay list too long , need to delete and info is " + contentInfo);
								jedisCluster.zrem(key, contentInfo);
								jedisCluster.expire(contentInfo + "_filterTypeFailCnt", 10);
							}
						});

						if (System.currentTimeMillis() - publishTime > 7200000) {
							//jedisCluster.zrem(key, contentInfoArr[0] );
							flowExpLog.warn("filterIds uid is " + context.getUid() + " , and contentID " + infoId + " "
									+ "stay list too long , need to delete and info is " + contentInfo);
						}
						return false;
					}
				}
			}
		}

		try {
			List<String> ids = readFilterInfo.filterIdList(context,context.getUid(), app, Arrays.asList(infoId));
			if (CollectionUtils.isEmpty(ids)) {
				return false;
			}
		} catch (Exception e) {
			logger.error("read filter Exception is " + context.getUid() + " expinfo id is " + infoId + ", length is " + 1, e);
		}

		if (flowExpLog.isTraceEnabled()) {
			flowExpLog.trace("uid is " + context.getUid() + " expinfo ids is " + infoId + ", length is " + 1 + " , and contentInfo is "
					+ contentInfo);
		}
		return true;
	}

	/**
	 *
	 * @param context
	 * @param idSet
	 * @param expRange
	 * @param flowExpLog
	 * @param key
	 * @param idInfoMap
	 * @return
	 */
	public String[] filterIds(Context context, Set<String> idSet, int expRange, Log flowExpLog, String key, Map<String, String> idInfoMap,
			Map<String, Object> configMap) {

		//Set<String> idSetTmp = new HashSet<String>();
		String app = context.getApp();

		boolean filterTypes = (boolean) configMap.get("filterTypes");

		if (filterTypes) {
			ArticleFilterImpl articleFilter = null;
			ArticleProperty article_property = null;

			if (context.getZhiziListReq() != null) {
				articleFilter = new ArticleFilterImpl();
				articleFilter.getQueryProperty().setZhizi_request(context.getZhiziListReq());

				article_property = new ArticleProperty();
				article_property.setProduct_id(app);
				article_property.setPublish_timestamp(new Date().getTime()); //ensure within 48hours..
				article_property.setAdult_score(0.0D); //ensure pass check..
				article_property.setFirm_app(null);
			}

			for (String contentInfo : idSet) {
				String[] contentInfoArr = contentInfo.split("#");

				if (context.getZhiziListReq() != null && contentInfoArr.length == 5) {
					article_property.setType(contentInfoArr[1]);
					article_property.setLink_type(contentInfoArr[2]);
					article_property.setDisplay_type(contentInfoArr[3]);
					article_property.setContent_id(contentInfoArr[0]);

					if (articleFilter.isValidArticle(article_property)) {
						idInfoMap.put(contentInfoArr[0], contentInfo);
					} else {
						long publishTime = Long.valueOf(contentInfoArr[4]);
						if (System.currentTimeMillis() - publishTime > 7200000) {
							//jedisCluster.zrem(key, contentInfoArr[0] );
							flowExpLog.warn(" uid is " + context.getUid() + " , and contentID " + contentInfoArr[0] + " "
									+ "stay list too long , need to delete and info is " + contentInfo);
						}
					}
				} else {
					idInfoMap.put(contentInfoArr[0], contentInfo);
				}
			}
		} else {
			for (String contentInfo : idSet) {
				idInfoMap.put(contentInfo.split("#")[0], contentInfo);
			}
		}

		if (idInfoMap.keySet().size() <= 0) {
			return null;
		}
		String[] ids = idInfoMap.keySet().toArray(new String[0]);

		//ReadedInfoHandler readFilterInfo = SysUtil.getBean("readFilterInfo");
		try {
			List<String> readFilterIDs = readFilterInfo.filterIdList(context,context.getUid(), app, Arrays.asList(ids));
			if (CollectionUtils.isNotEmpty(readFilterIDs)) {
				if (expRange != 0 && expRange / 2 > readFilterIDs.size()) {
					return null;
				}
				ids = readFilterIDs.toArray(new String[0]);
			} else {
				return null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("read filter Exception is " + context.getUid() + " expinfo ids is " + ids + ", length is " + ids.length + " and  "
					+ Arrays.asList(ids), e1);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(
					"uid is " + context.getUid() + " expinfo ids is " + ids + ", length is " + ids.length + " and  " + Arrays.asList(ids));
		}

		return ids;
	}

	/**
	 *  if need do expinfo
	 * @param context
	 * @return
	 */
	public boolean ifDoExp(Context context, Map<String, Object> configMap) {
		int offset = context.getOffset();

		//获取流量探索开始位置,前几屏不做流量探索
		int offset2ExpInfo = (int) configMap.get("offset2ExpInfo");
		int offset2ExpInfo2Video = (int) configMap.get("offset2ExpInfo2Video");
		if ((Boolean) configMap.get("doExpVersionFilter") && context.getZhiziListReq() != null
				&& StringUtils.isNotEmpty(context.getZhiziListReq().getAppVersion())) {
			@SuppressWarnings("unchecked")
			List<String> doExpVersionArr = (List<String>) configMap.get("doExpVersionArr");
			if (!doExpVersionArr.contains(context.getZhiziListReq().getAppVersion())) {
				return false;
			}
		}

		boolean bForYouChannel = ContextUtils.isForYouChannel(context);
		if (logger.isDebugEnabled()) {
			logger.debug("uid is :" + context.getUid() + " offset is " + offset + ", for you  is "
					+ bForYouChannel + " , offset2ExpInfo is " + offset2ExpInfo + " , and scenanio is "
					+ context.getScenario());
		}

		//if video channel
		boolean bVideoChannel = ContextUtils.isVideoChannel(context);
		if (bVideoChannel && offset > offset2ExpInfo2Video) {
			return true;
		}

		//meme channel
		boolean doExpForMEME = false;
		if (null != configMap.get("doExpForMEME")) {
			doExpForMEME = ((Boolean) configMap.get("doExpForMEME")).booleanValue();
		}
		boolean bMemesChannel = ContextUtils.isMemesChannel(context);
		if (doExpForMEME && bMemesChannel) {
			return true;
		}

		// offset小于offset2ExpInfo 非for you 不进行 流量探索
		boolean bNoExploration = (offset < offset2ExpInfo || !bForYouChannel);
		if (bNoExploration && logger.isDebugEnabled()) {
			logger.debug("uid is :" + context.getUid() + " is no exp is " + bNoExploration);
			return false;
		}
		return true;
	}

	public boolean checkExpInfoLength(Context context, long expInfoLength, boolean useImpression, Map<String, Object> configMap) {
		//例如 50/2 > 20 不进行探索
		//int expRange = (int) configMap.get("expRange");
		//int videoScenario =  (int) configMap.get("videoScenario");
		int minExpLength = (int) configMap.get("minExpLength");
		if (minExpLength >= expInfoLength) {
			return false;
		}

		return true;
	}

	public Map<String, Object> initConfigMap(Context context, boolean useImpression) {
		Map<String, Object> configMap = new ConcurrentHashMap<String, Object>();

		String abTestVersion = context.getAbtestVersion();
		int reqFetchCnt = context.getNum();

		// 获取流量探索开始位置,前几屏不做流量探索

		if (!useImpression) {
			double expPer = 0.15;// 流量比例
			int expRange = 50;// 探索的范围
			int expRangeOfVideo = 10;
			String[] expPerArr = null;
			String[] expinfoCntArr = null;
			int expPVCnt = 50;
			double flowExpPer = 0.2;
			int offset2ExpInfo = 2 * reqFetchCnt;
			boolean filterTypes = false;
			int videoScenario = 65799;
			int offset2ExpInfo2Video = -1;
			String[] expPerArr4video = null;
			int flowfilterTypeFailCnt2Remove = 100;
			double flowExpPer2Video = 0.2;
			String[] expVideoCntArr = null;
			int minExpLength = 50;
			List<String> doExpVersionArr = null;
			boolean doExpVersionFilter = false;
			boolean doExpForMEME = false;

			try {
				expRange = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expRange"));
				expRangeOfVideo = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expRangeOfVideo"));
				expPerArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPerArr").split(";");
				expinfoCntArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expinfoCntArr").split(";");
				expPVCnt = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPVCnt"));
				flowExpPer = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowExpPerOfForYou"));
				offset2ExpInfo = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "offset2ExpInfo"));
				filterTypes = Boolean.parseBoolean(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "filterTypes"));
				videoScenario = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "videoScenario"));
				offset2ExpInfo2Video = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "offset2ExpInfo2Video"));
				expPerArr4video = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPerArr4video").split(";");
				flowfilterTypeFailCnt2Remove = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowfilterTypeFailCnt2Remove"));
				flowExpPer2Video = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowExpPer2Video"));
				expVideoCntArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expVideoCntArr").split(";");
				minExpLength = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "minExpLength"));
				doExpVersionFilter = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpVersionFilter"));
				doExpVersionArr = Arrays.asList(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpVersionArr").split(";"));
				doExpForMEME = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpForMEME"));

				configMap.put("expRange", expRange);
				configMap.put("expPerArr", expPerArr);
				configMap.put("expinfoCntArr", expinfoCntArr);
				configMap.put("expPVCnt", expPVCnt);
				configMap.put("flowExpPer", flowExpPer);
				configMap.put("expPer", expPer);
				configMap.put("offset2ExpInfo", offset2ExpInfo);
				configMap.put("filterTypes", filterTypes);
				configMap.put("videoScenario", videoScenario);
				configMap.put("offset2ExpInfo2Video", offset2ExpInfo2Video);
				configMap.put("expPerArr4video", expPerArr4video);
				configMap.put("flowfilterTypeFailCnt2Remove", flowfilterTypeFailCnt2Remove);
				configMap.put("flowExpPer2Video", flowExpPer2Video);
				configMap.put("expVideoCntArr", expVideoCntArr);
				configMap.put("minExpLength", minExpLength);
				configMap.put("doExpVersionArr", doExpVersionArr);
				configMap.put("doExpVersionFilter", doExpVersionFilter);
				configMap.put("expRangeOfVideo", expRangeOfVideo);
				configMap.put("doExpForMEME", doExpForMEME);
			} catch (Exception e) {
				logger.error(
						"=== get qconf error,has no {expRange or expPerArr or expinfoCntArrm or expPVCnt ,flowExpPerOfForYou}config ,and uid is "
								+ context.getUid() + " abtest " + abTestVersion + "===",
						e);
			}
		} else {
			double expPer = 0.15;// 流量比例
			int expRange = 50;// 探索的范围
			int expRangeOfVideo = 10;
			String[] expPerArr = null;
			String[] expinfoCntArr = null;
			int expPVCnt = 50;
			double flowExpPer = 0.2;
			int offset2ExpInfo = 2 * reqFetchCnt;
			boolean filterTypes = false;
			int videoScenario = 65799;
			int offset2ExpInfo2Video = -1;
			String[] expPerArr4video = null;
			double firstRequestPer = 3.0;
			int needImpCnt = 50;
			int flowfilterTypeFailCnt2Remove = 100;
			double flowExpPer2Video = 0.2;
			String[] expVideoCntArr = null;
			List<String> doExpVersionArr = null;
			int minExpLength;
			boolean doExpVersionFilter = false;
			double flowExpPer2MEME = 0.15;
			int expRangeOfMEME = 10;
			int needImpCntMeme = 10;
			boolean doExpForMEME = false;
			try {
				expRange = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expRange"));
				expRangeOfVideo = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expRangeOfVideo"));
				expPerArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPerArr").split(";");
				expinfoCntArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expinfoCntArr").split(";");
				expPVCnt = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPVCnt"));
				flowExpPer = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowExpPerOfForYou"));
				offset2ExpInfo = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "offset2ExpInfo"));
				filterTypes = Boolean.parseBoolean(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "filterTypes"));
				videoScenario = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "videoScenario"));
				offset2ExpInfo2Video = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "offset2ExpInfo2Video"));
				expPerArr4video = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expPerArr4video").split(";");
				firstRequestPer = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "firstRequestPer"));
				needImpCnt = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "needImpCnt"));
				flowfilterTypeFailCnt2Remove = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowfilterTypeFailCnt2Remove"));
				flowExpPer2Video = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowExpPer2Video"));
				expVideoCntArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expVideoCntArr").split(";");
				minExpLength = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "minExpLength"));
				doExpVersionFilter = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpVersionFilter"));
				doExpVersionArr = Arrays.asList(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpVersionArr").split(";"));
				doExpForMEME = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "doExpForMEME"));

				flowExpPer2MEME = Double.parseDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowExpPer2MEME"));
				expRangeOfMEME = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "expRangeOfMEME"));
				needImpCntMeme = Integer.parseInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "needImpCntMeme"));

				configMap.put("expRangeOfMEME", expRangeOfMEME);
				configMap.put("flowExpPer2MEME", flowExpPer2MEME);
				configMap.put("expRange", expRange);
				configMap.put("expPerArr", expPerArr);
				configMap.put("expinfoCntArr", expinfoCntArr);
				configMap.put("expPVCnt", expPVCnt);
				configMap.put("flowExpPer", flowExpPer);
				configMap.put("expPer", expPer);
				configMap.put("offset2ExpInfo", offset2ExpInfo);
				configMap.put("filterTypes", filterTypes);
				configMap.put("videoScenario", videoScenario);
				configMap.put("offset2ExpInfo2Video", offset2ExpInfo2Video);
				configMap.put("expPerArr4video", expPerArr4video);
				configMap.put("firstRequestPer", firstRequestPer);
				configMap.put("needImpCnt", needImpCnt);
				configMap.put("flowfilterTypeFailCnt2Remove", flowfilterTypeFailCnt2Remove);
				configMap.put("flowExpPer2Video", flowExpPer2Video);
				configMap.put("expVideoCntArr", expVideoCntArr);
				configMap.put("minExpLength", minExpLength);
				configMap.put("doExpVersionArr", doExpVersionArr);
				configMap.put("doExpVersionFilter", doExpVersionFilter);
				configMap.put("expRangeOfVideo", expRangeOfVideo);
				configMap.put("needImpCntMeme", needImpCntMeme);
				configMap.put("doExpForMEME", doExpForMEME);
			} catch (Throwable e) {
				logger.error(
						"=== get qconf error,has no {expRange or expPerArr or expinfoCntArrm or expPVCnt ,flowExpPerOfForYou}config ,and uid is "
								+ context.getUid() + " abtest " + abTestVersion + "===",
						e);
			}
		}

		return configMap;
	}
}
