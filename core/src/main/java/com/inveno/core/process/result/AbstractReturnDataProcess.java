package com.inveno.core.process.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.explore.AbstractExplore;
import com.inveno.core.process.explore.ExploreParameter;
import com.inveno.core.process.newsQ.FallbackNewsQ;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.util.RequestTrackingHelper;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component
public abstract class AbstractReturnDataProcess implements IResultProcess
{
	public static final String SUFFIX_IMPRESSION_ACS_KEY = "impression";

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private AbstractExplore exploreProcess;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Autowired
	private MonitorLog monitorLog;

	private Log logger = LogFactory.getLog(ResultAbstract.class);

	private static Log cmslogger = LogFactory.getLog("cmslog");

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	public Map<String,Map<Double, List<String>>> forceInsertCache = new ConcurrentHashMap<String, Map<Double, List<String>>>();

//	private Map<String,Set<String>> topInfoCache = new ConcurrentHashMap<String, Set<String>>();
	private Map<String,Map<Double, List<String>>> topInfoCache =  new ConcurrentHashMap<String, Map<Double, List<String>>>();


	private long tsStart = 0;
	private long tsEnd = 0;
	private long tsSpent = 0;
	
	public static final int OFFLINEDOWNLOADNUM = 20;
	
	@Override
	public void cache(Context context) {
		beforCache(context);
		getInfoByCache(context);
		afterCache(context);
	}

	public void beforCache(Context context) {}

	public void getInfoByCache(Context context) {}

	public void afterCache(Context context) {
		displayTypeStg(context);
	}

	@Override
	public void noCache(Context context) {
		beforNoCache(context);
		getInfoByNoCache(context);
		afterNoCache(context);
	}

	public void beforNoCache(Context context) {}

	public void getInfoByNoCache(Context context) {}

	public void afterNoCache(Context context) {
		displayTypeStg(context);
	}


	/**
	 * display type 重排
	 * @param context
	 * 1、Display_type处理规则放在core里处理；
	 * 2、Display_type现行处理逻辑为，在同一次request下发中：
	 *  a）取用户上报的display_type能力集合 交 文章的display_type能力集合，取最高位
	 *  b）通栏或banner须<=1
	 *  c）对于支持多图和三图的文章：
	 *       针对新加坡，需要判断app_ver
	 *              若app_ver < Hotoday_v2.2.8 或 Mata_v1.1.0，则以多图形式下发；
	 *              若app_ver >= Hotoday_v2.2.8 或 Mata_v1.1.0，则以三图形式下发；
	 *       针对美东，不需判断app_ver，一律以三图形式下发
	 *  d）对其它display_type不做限制
	 *  e）当forU频道下发memes news时 displayType 包含大图时，news的大图去掉 其他正常下发--2018年3月23日 11:51:07 
	 *  这段代码是关闭的！！
	 */
	private void displayTypeStg(Context context) {
		boolean ifDisplayType = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifDisplayType"));

		if (!ifDisplayType) {
			return ;
		}
		//国内不做控制
		if( context.getLanguage().equalsIgnoreCase("zh_CN") ){
			return ;
		}
		//需要为infoList 接口
		if( null == context.getZhiziListReq()  ){
			return ;
		}

		List<ResponParam> resultList = context.getResponseParamList();
		int sizeOfTongLan = 0;
		if( CollectionUtils.isNotEmpty(resultList) ){
			for (ResponParam re : resultList) {
				if( re.getStrategy().equals(Strategy.TOP) )  {
					sizeOfTongLan++;
					re.setDisplayType(0x80);
				}else{

					if( re.getDisplayType() != 0 ){
						
						

						int dispaly = (int) ((re.getDisplayType())&(context.getDisplay()));
//						logger.debug("displayTypeStg---展现信息：contentId="+re.getInfoid()+",dispaly="+dispaly);
						
						//当forU频道下发memes news时 displayType 包含大图时，news的大图去掉 其他正常下发--2018年3月23日 11:48:21 yezi
						if(re.contentType == ContentType.NEWS.getValue() && (dispaly & 0x400)!=0 ){
							dispaly = dispaly - 0x400;
						}
						
						//获取display的最高位
						int highestOneBit = Integer.highestOneBit(dispaly);
						
						//判断是否为通栏,最多有一条
						if( sizeOfTongLan <= 0 ){
							if( (re.getDisplayType()& 0x08) != 0 ||  (dispaly& 0x80) != 0){
								sizeOfTongLan++;
								re.setDisplayType(0x08);
							}
							//已经有banner则需要去除通栏位
						}else{
							if( 0x08 == highestOneBit ){
								highestOneBit = Integer.highestOneBit(dispaly-0x08);
							}
							
						}
						/*对于支持多图和三图的文章:针对新加坡,需要判断app_ver
							若app_ver < Hotoday_v2.2.8 或 Mata_v1.1.0，则以多图形式下发;
							若app_ver >= Hotoday_v2.2.8 或 Mata_v1.1.0，则以三图形式下发针对美东,不需判断app_ver,一律以三图形式下发*/
						String appVersion = context.getZhiziListReq().getAppVersion();
						if( (appVersion.contains("Hotoday") || appVersion.contains("Mata")) &&  (re.getDisplayType()& 0x04) != 0 && (re.getDisplayType()& 0x10) != 0 ){
							if( appVersion.compareToIgnoreCase("Hotoday_v2.2.8") < 0 || appVersion.compareToIgnoreCase("Mata_v1.1.0") < 0 ){
								re.setDisplayType(0x10);//多图文章
							}else{
								re.setDisplayType(0x04);//三图文章
							}
						}else{
							re.setDisplayType(highestOneBit);
						}
					}
					
					logger.debug("displayTypeStg--- real---展现信息：contentId="+re.getInfoid()+",dispaly="+re.getDisplayType());
				}
			}
		}
	}

	protected void prepareReturnData(Map<Integer, ResponParam> mRequestIndexData) {
		Iterator<Integer> it = mRequestIndexData.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			if (mRequestIndexData.get(key) == null)
				it.remove();
		}
	}
	protected Map<Integer, ResponParam> prepareRequestIndexDataToBeFilled(Map<Integer, ResponParam> mRequestIndexData) {
		return prepareRequestIndexDataToBeFilled(mRequestIndexData, -1);
	}
	protected Map<Integer, ResponParam> prepareRequestIndexDataToBeFilled(Map<Integer, ResponParam> mRequestIndexData, int indexToIgnoreExploration) {
		Map<Integer, ResponParam> mLocalRequestIndexData = new HashMap<Integer, ResponParam>();
		for (Integer index : mRequestIndexData.keySet()) {
			if (indexToIgnoreExploration >= 0 && index < indexToIgnoreExploration) {
				continue;
			}
			else if (mRequestIndexData.get(index) == null) {
				mLocalRequestIndexData.put(index, mRequestIndexData.get(index));
			}
		}
		return mLocalRequestIndexData;
	}
	/**
	 * 2018年5月18日
	 * 按照规则返回的资讯列表
	 * 改变按照 置顶-》强插 -》流量探索 -》推荐资讯 -->(fallback补文章)--->(AddToCache)顺序执行
	 * @param context
	 * @param list
	 * @autor yezi 
	 * @return
	 */
	public List<ResponParam> getReturnData(Context context, boolean bUseRankingCache) {
		long tsRequestStart = System.currentTimeMillis();
		
		Set<String> hsAllResponse = new HashSet<String>();
		Map<Integer, ResponParam> mRequestIndexResponse = new LinkedHashMap<Integer, ResponParam>();
		int reqFetchCnt = context.getNum();
		String requestId = context.getRequestId();
		
		try {
	
				for (int i = 0; i < reqFetchCnt; i++) {
					mRequestIndexResponse.put(i, null);
				}
				String strRequestInfo = ContextUtils.toRequestInfoString(context);
				
				
				//1 置顶资讯
				 tsStart = System.currentTimeMillis();
				 addTopData(context, mRequestIndexResponse, hsAllResponse);
				 tsEnd = System.currentTimeMillis();
				 RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.top_info");
				 
//				if (logger.isDebugEnabled()) {
//					// LOG PRINT----
//					String uid = context.getUid();
//					logger.info("1 end PostProcessImpl.process---addTopData --after----");
//					ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//					if (!MapUtils.isEmpty(mRequestIndexResponse) && mRequestIndexResponse.entrySet() != null) {
//						logger.info("1 置顶资讯 ");
//						for (Entry<Integer, ResponParam> entry : mRequestIndexResponse.entrySet()) {
//							if (entry != null && entry.getValue() != null) {
//								HashMap<String, Object> mData = new HashMap<String, Object>();
//								mData.put("index", entry.getKey());
//								mData.put("contentId", entry.getValue().getInfoid());
//								mData.put("strategy", entry.getValue().getStrategy());
//								alDataInput.add(mData);
//							}
//						}
//						HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//						mDumpLog.put("module", "addTopData --after");
//						mDumpLog.put("event_time", System.currentTimeMillis());
//						mDumpLog.put("uid", uid);
//						mDumpLog.put("app", context.getApp());
//						mDumpLog.put("scenario", context.getScenario());
//						mDumpLog.put("iostream", "addTopData --after");
//						mDumpLog.put("data", alDataInput);
//						String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] { SerializerFeature.DisableCircularReferenceDetect });
//						logger.info("1 end PostProcessImpl.process---addTopData --after----" + strLogMsg);
//					}
//				}
		
				//2 强插资讯
				tsStart = System.currentTimeMillis();
				addForceInsertData(context, mRequestIndexResponse, hsAllResponse);
				tsEnd = System.currentTimeMillis();
				tsSpent = tsEnd - tsStart;
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.force_insert");
				logger.info(strRequestInfo + " end addForceInsertData time is " + tsSpent + " ms.");
				
//				if(logger.isDebugEnabled()){
//				//LOG PRINT----
//					String uid = context.getUid();
//					logger.info("2 end PostProcessImpl.process---addForceInsertData --after----");
//					ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//					if(!MapUtils.isEmpty(mRequestIndexResponse) && mRequestIndexResponse.entrySet() != null){
//						
//					
//					for (Entry<Integer, ResponParam> entry : mRequestIndexResponse.entrySet()) {  
//						if(entry != null && entry.getValue() != null){
//							HashMap<String, Object> mData = new HashMap<String, Object>();
//							mData.put("index", entry.getKey());
//							mData.put("contentId", entry.getValue().getInfoid());
//							mData.put("strategy", entry.getValue().getStrategy());
//							alDataInput.add(mData);
//						}
//					}
//					HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//					mDumpLog.put("module", "addForceInsertData --after");
//					mDumpLog.put("event_time", System.currentTimeMillis());
//					mDumpLog.put("uid", uid);
//					mDumpLog.put("app", context.getApp());
//					mDumpLog.put("scenario", context.getScenario());
//					mDumpLog.put("iostream", "addForceInsertData --after");
//					mDumpLog.put("data", alDataInput);
//					String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
//					logger.info("2 end PostProcessImpl.process---addForceInsertData --after----"+strLogMsg);
//				 }
//				}
		
				//3 流量探索
				tsStart = System.currentTimeMillis();
				addFlowExplorationData(context, mRequestIndexResponse, hsAllResponse, bUseRankingCache);
				tsEnd = System.currentTimeMillis();
				tsSpent = tsEnd - tsStart;
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.flow_explore");
				logger.info(strRequestInfo + " end addFlowExplorationData time is " + tsSpent + " ms.");
				
//				if(logger.isDebugEnabled()){
//				//LOG PRINT----
//					String uid = context.getUid();
//					logger.info("3 addFlowExplorationData --after----");
//					ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//					if(!MapUtils.isEmpty(mRequestIndexResponse) && mRequestIndexResponse.entrySet() != null){
//						
//					
//					for (Entry<Integer, ResponParam> entry : mRequestIndexResponse.entrySet()) {  
//						if(entry != null && entry.getValue() != null){
//							HashMap<String, Object> mData = new HashMap<String, Object>();
//							mData.put("index", entry.getKey());
//							mData.put("contentId", entry.getValue().getInfoid());
//							mData.put("strategy", entry.getValue().getStrategy());
//							alDataInput.add(mData);
//						}
//					}
//					HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//					mDumpLog.put("module", "addFlowExplorationData --after");
//					mDumpLog.put("event_time", System.currentTimeMillis());
//					mDumpLog.put("uid", uid);
//					mDumpLog.put("app", context.getApp());
//					mDumpLog.put("scenario", context.getScenario());
//					mDumpLog.put("iostream", "addFlowExplorationData --after");
//					mDumpLog.put("data", alDataInput);
//					String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
//					logger.info("3 end PostProcessImpl.process---addFlowExplorationData --after----"+strLogMsg);
//				 }
//				}
				
				//4 推荐资讯
				tsStart = System.currentTimeMillis();
				addRecommendationData(context, mRequestIndexResponse, hsAllResponse, bUseRankingCache);
				tsEnd = System.currentTimeMillis();
				tsSpent = tsEnd - tsStart;
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.recommendation_info");
				logger.info(strRequestInfo + " end addRecommendationData time is " + tsSpent + " ms.");
		
		
//				if(logger.isDebugEnabled()){
//					//LOG PRINT----
//						String uid = context.getUid();
//						logger.info("4 end PostProcessImpl.process---addFlowExplorationData --after----");
//						ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//						if(!MapUtils.isEmpty(mRequestIndexResponse) && mRequestIndexResponse.entrySet() != null){						
//						
//						for (Entry<Integer, ResponParam> entry : mRequestIndexResponse.entrySet()) {  
//							if(entry != null && entry.getValue() != null){
//								HashMap<String, Object> mData = new HashMap<String, Object>();
//								mData.put("index", entry.getKey());
//								mData.put("contentId", entry.getValue().getInfoid());
//								mData.put("strategy", entry.getValue().getStrategy());
//								alDataInput.add(mData);
//							}
//						}
//						HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//						mDumpLog.put("module", "addFlowExplorationData --after");
//						mDumpLog.put("event_time", System.currentTimeMillis());
//						mDumpLog.put("uid", uid);
//						mDumpLog.put("app", context.getApp());
//						mDumpLog.put("scenario", context.getScenario());
//						mDumpLog.put("iostream", "addFlowExplorationData --after");
//						mDumpLog.put("data", alDataInput);
//						String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
//						logger.info("4 end PostProcessImpl.process---addFlowExplorationData --after----"+strLogMsg);
//					 }
//					}
				
		
				//5 fallback
				boolean bAddFromFallback = addFallbackData(context, mRequestIndexResponse, hsAllResponse);
				//6 addToCache		
				if (!bAddFromFallback) {
					//如果当前补充资讯正常，不需要fallback填充下发位置，则将用户的ranking数据添加到cache中
					tsStart = System.currentTimeMillis();
					addToCache(context, mRequestIndexResponse.values());
					tsEnd = System.currentTimeMillis();
					tsSpent = tsEnd - tsStart;
					RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.add_cache");
					if (logger.isDebugEnabled()) {
						logger.debug(strRequestInfo + " end addToCache time is " + tsSpent + " ms.");
					}
				}
		
	
				
				
//				if(logger.isDebugEnabled()){
//				//LOG PRINT----
//					String uid = context.getUid();
//					
//					ArrayList<HashMap<String, Object>> alDataInput = new ArrayList<HashMap<String, Object>>();
//					for (Entry<Integer, ResponParam> entry : mRequestIndexResponse.entrySet()) {  
//						if(entry != null && entry.getValue() != null){
//							HashMap<String, Object> mData = new HashMap<String, Object>();
//							mData.put("index", entry.getKey());
//							mData.put("contentId", entry.getValue().getInfoid());
//							mData.put("strategy", entry.getValue().getStrategy());
//							alDataInput.add(mData);
//						}
//					}
//					HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
//					mDumpLog.put("module", "addCache --after-");
//					mDumpLog.put("event_time", System.currentTimeMillis());
//					mDumpLog.put("uid", uid);
//					mDumpLog.put("app", context.getApp());
//					mDumpLog.put("scenario", context.getScenario());
//					mDumpLog.put("iostream", "addCache --after-");
//					mDumpLog.put("data", alDataInput);
//					String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
//					logger.info("end---  end PostProcessImpl.process---addCache --after----"+strLogMsg);
//				}
				
		
		   RequestTrackingHelper.logCheckPoint(tsRequestStart, tsEnd, requestId, "core.info_filled");
		} catch (Exception e) {
			logger.info("Exception----"+e.toString());
        }
		return new java.util.ArrayList<ResponParam>(mRequestIndexResponse.values());
	}
	
	
	

	public abstract void addRecommendationData(Context context, Map<Integer, ResponParam> mRequestIndexResponse, Set<String> hsAllResponse, boolean fUseRankingCache);
	public abstract void addToCache(Context context, Collection<ResponParam> resultList);

	/**
	 * addFlowExplorationData
	 * @param context
	 * @param resultList
	 * @param indexList
	 * @param resultSet
	 * @param ifCache
	 */
	private void addFlowExplorationData(Context context, Map<Integer, ResponParam> mRequestIndexData, Set<String> hsAllResponse, boolean bUseRankingCache) {
		if (context.getZhiziListReq() == null) {
			return ;
		}
		
		//skip position : quickread
		if (ContextUtils.isQuickread(context)) {
			return;
		}

		//读取qconf,根据exlore-product-bRemoveFlowExploreInOfflineDownload配置判断是否离线下载,当请求条数大于20为离线下载的数据;离线下载处理时不做流量探索		
		String bRemoveFlowExploreInOfflineDownloadStr = context.getComponentConfiguration("explore", context.getApp(),  "bRemoveFlowExploreInOfflineDownload");		
		boolean bRemoveFlowExploreInOfflineDownload = StringUtils.isEmpty(bRemoveFlowExploreInOfflineDownloadStr) ? true : Boolean.valueOf(bRemoveFlowExploreInOfflineDownloadStr) ;
		logger.debug(ContextUtils.toRequestInfoString(context) 	+ ", context.getNum() is " + context.getNum() 
				+ " ,bRemoveFlowExploreInOfflineDownloadStr="+bRemoveFlowExploreInOfflineDownloadStr + " ,bRemoveFlowExploreInOfflineDownload="+bRemoveFlowExploreInOfflineDownload);
		if(bRemoveFlowExploreInOfflineDownload && (context.getNum() >= OFFLINEDOWNLOADNUM)){
			logger.debug(ContextUtils.toRequestInfoString(context) + " |offline download news stop add flow exploration data:"
					+ ", context.getNum() is " + context.getNum());
			return;
		}
		long tsStart = System.currentTimeMillis();
		boolean bUseImpression = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "useImpression"));
		String strExploreStrategy = (bUseImpression) ? ExploreParameter.EXPLORE_STRATEGY_IMPRESSION_BASED : ExploreParameter.EXPLORE_STRATEGY_REQUEST_BASED;
		String strExploreType = "";
		Map<Integer, ResponParam> mLocalRequestIndexData = null;
		Map<Integer, ResponParam> mLocalRequestIndexDataNews = null;
		Map<Integer, ResponParam> mLocalRequestIndexDataVideo = null;
		if (ContextUtils.isLockscreen(context)) {
			//首屏前一个位置不做探索
			int indexToIgnoreExploration = (bUseRankingCache) ? -1 : 1;
			mLocalRequestIndexData = prepareRequestIndexDataToBeFilled(mRequestIndexData, indexToIgnoreExploration);
			strExploreType = ExploreParameter.EXPLORE_TYPE_LOCKSCREEN_NEWS;
		} else {
			// skip channel : foryou、video
			boolean bForYouChannel = ContextUtils.isForYouChannel(context);
			boolean bVideoChannel = ContextUtils.isVideoChannel(context);
			boolean bMemesChannel = ContextUtils.isMemesChannel(context);
			boolean bComicChannel = ContextUtils.isComicChannel(context);
			boolean bGifChannel = ContextUtils.isGifChannel(context);
			if (bForYouChannel || bVideoChannel || bMemesChannel || bComicChannel || bGifChannel) {
				//首屏前三个位置不做探索
				int indexToIgnoreExploration = (bUseRankingCache) ? -1 : 3;
				mLocalRequestIndexData = prepareRequestIndexDataToBeFilled(mRequestIndexData, indexToIgnoreExploration);		
				if (bVideoChannel) {
					strExploreType = ExploreParameter.EXPLORE_TYPE_VIDEO;
				} else if (bMemesChannel) {
					strExploreType = ExploreParameter.EXPLORE_TYPE_MEME;
				} else if (bComicChannel) {
					strExploreType = ExploreParameter.EXPLORE_TYPE_COMIC;
				} else if (bGifChannel) {
                    strExploreType = ExploreParameter.EXPLORE_TYPE_GIF;
                } else {
					if (MapUtils.isNotEmpty(mLocalRequestIndexData)) {
						for (int index : mLocalRequestIndexData.keySet()){
							if (((context.getOffset()+index) % context.getIndex_offset_delivery()) == context.getVideo_index_offset_delivery_remainder()) {
								//ExploreParameter.EXPLORE_TYPE_VIDEO;
								if(mLocalRequestIndexDataVideo == null){
									mLocalRequestIndexDataVideo = new HashMap<Integer, ResponParam>();
								}
								mLocalRequestIndexDataVideo.put(index, mLocalRequestIndexData.get(index));
							} else {
								//ExploreParameter.EXPLORE_TYPE_NEWS;
								if(mLocalRequestIndexDataNews == null){
									mLocalRequestIndexDataNews = new HashMap<Integer, ResponParam>();
								}
								mLocalRequestIndexDataNews.put(index, mLocalRequestIndexData.get(index));
							}
						}
						mLocalRequestIndexData.clear();
					}
				}
			}
		}
		
		try {
			if (MapUtils.isNotEmpty(mLocalRequestIndexData)) {
				ExploreParameter exploreParameter = new ExploreParameter(context, strExploreType, strExploreStrategy);
				exploreProcess.getExploreData(context,exploreParameter, mLocalRequestIndexData, hsAllResponse);
				mRequestIndexData.putAll(mLocalRequestIndexData);
				logger.debug(ContextUtils.toRequestInfoString(context) + " | " + strExploreType + " getExpInfo resultList is " + mLocalRequestIndexData.values());
			} else {
				if(MapUtils.isNotEmpty(mLocalRequestIndexDataNews)){
					strExploreType = ExploreParameter.EXPLORE_TYPE_NEWS;
					ExploreParameter exploreParameter = new ExploreParameter(context, strExploreType, strExploreStrategy);
					exploreParameter.setOffset(context.getOffset());
					exploreProcess.getExploreData(context, exploreParameter, mLocalRequestIndexDataNews, hsAllResponse);
					mRequestIndexData.putAll(mLocalRequestIndexDataNews);
					logger.debug(ContextUtils.toRequestInfoString(context) + " | " + strExploreType + " getExpInfo resultList is " + mLocalRequestIndexDataNews.values());
				}
				
				if(MapUtils.isNotEmpty(mLocalRequestIndexDataVideo)){
					strExploreType = ExploreParameter.EXPLORE_TYPE_VIDEO;
					ExploreParameter exploreParameter = new ExploreParameter(context, strExploreType, strExploreStrategy);
					exploreParameter.setOffset(context.getOffset());
					exploreProcess.getExploreData(context, exploreParameter, mLocalRequestIndexDataVideo, hsAllResponse);
					mRequestIndexData.putAll(mLocalRequestIndexDataVideo);
					logger.debug(ContextUtils.toRequestInfoString(context) + " | " + strExploreType + " getExpInfo resultList is " + mLocalRequestIndexDataVideo.values());
				}
			}
		} catch (Exception e) {
			logger.error(ContextUtils.toRequestInfoString(context) + " | addFlowExplorationInfo getExpInfo error", e);
		}
		
		long tsSpent = System.currentTimeMillis() - tsStart;
		monitorLog.addResTimeLogByProduct(context, MonitorType.EXP_INFO_RESPONSE_TIME, tsSpent);
		logger.debug(ContextUtils.toRequestInfoString(context) + " | end getExpInfo time is " + tsSpent + " ms");
	}

	public void addForceInsertData(Context context, Map<Integer, ResponParam> mRequestIndexResponse, Set<String> hsAllResponse) {
		int reqFetchCnt = context.getNum();
		int offset = context.getOffset();
		String uid = context.getUid();
		String app = context.getApp();
		String abTestVersion =  context.getAbtestVersion();
		long scenario = context.getScenario();

		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();
		int cmsMaxCnt = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "cmsMaxCnt"), 30);
		boolean bForceInsert = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifForceInsert"));

		if (!bForceInsert) {
			return ;
		}

		String interfaceName = "q";
		String semantic = "channel";
		if (context.getZhiziListReq() != null) {
			interfaceName = "list";
			semantic = "scenario";
		} else if (context.getRecNewsListReq() != null) {
			if (!context.getRecNewsListReq().isNeedBanner()) {
				interfaceName = "q";
			} else {
				interfaceName = "qb";
			}
		} else if (context.getScenario() > 0) {
			interfaceName = "qcn";
		}

		StringBuffer timeliness_high = new StringBuffer("stginfo:timeliness:high:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);

		StringBuffer timeliness_low = new StringBuffer("stginfo:timeliness:low:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);


		//设置此两个 pos2Resp,和list位置,是由于需要过已读,可能存在强插资讯已读的情况,因此设置此两个变量,
		//如果直接拼接resultList,由于存在null占位符,直接过已读可能存在问题;
		HashMap<ResponParam, Double> resp2Pos = new HashMap<ResponParam, Double>();
		List<ResponParam> list2Acs = new ArrayList<ResponParam>();

		Map<Double, List<String>> highMap = forceInsertCache.get(timeliness_high.toString());
		Map<Double, List<String>> lowMap = forceInsertCache.get(timeliness_low.toString());

		if (lowMap == null || lowMap.isEmpty() )
		{
			this.forceInsertCache.put(timeliness_low.toString(),  Collections.emptyMap());
		}
		if (highMap == null || highMap.isEmpty())
		{
			this.forceInsertCache.put(timeliness_high.toString(), Collections.emptyMap());
		}

		if (lowMap != null)
		{
			if (offset >= cmsMaxCnt)//前30才做强插
			{
				//do nothing
			}
			else
			{
				//遍历
				for (Map.Entry<Double, List<String>> entry : lowMap.entrySet())
				{
					double key = entry.getKey();
					if (key >= offset + 1 && key <= offset + reqFetchCnt)
					{
						List<String> alCandidate = entry.getValue();
						String strategyCode = "";
						String contentId = null;
						//only through acs w/ force_insert candidates for lock screen
						if (alCandidate.size() > 1 && ContextUtils.isLockscreen(context)){
							strategyCode = Strategy.FIRSTSCREEN_FORCE_INSERT.getCode();
							ArrayList<String> filteredListTmp = new ArrayList<String>();
							//candidateId pattern : ${infoid}#${contentType}#${linkType}#${displayType}
							HashMap<String, String> mInfoIdWithType = new HashMap<String, String>();
							for (String infoIdWithType : alCandidate)
							{
								StringBuffer sb = new StringBuffer();
								String infoId = infoIdWithType.split("#")[0];
								mInfoIdWithType.put(infoId, infoIdWithType);
								sb.append(infoId);
								sb.append("#");
								sb.append(String.valueOf(scenario));
								sb.append("#");
								sb.append(SUFFIX_IMPRESSION_ACS_KEY);
								filteredListTmp.add(sb.toString());
							}
							List<String> filteredList = (ArrayList<String>)filteredListTmp.clone();
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " starting acs filter for FIRSTSCREEN_FORCE_INSERT result_list.size=" + filteredList.size() + "\tresult_list=" + filteredList);
							try
							{
								filteredList = readFilterInfo.filterIdList(context,uid, app, filteredList);
							}
							catch (Exception e)
							{
								logger.error("", e);
							}
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " finishing acs filter for FIRSTSCREEN_FORCE_INSERT result_list.size=" + filteredList.size() + "\tresult_list=" + filteredList);
							for (String infoId : filteredList)
							{
								infoId = infoId.split("#")[0];
								String infoIdWithType = (String)mInfoIdWithType.get(infoId);
								infoId = filterTypes(infoIdWithType, context);
								if (StringUtils.isNotEmpty(infoId))
								{
									contentId = infoId;
									break;
								}
							}
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " apply content for FIRSTSCREEN_FORCE_INSERT content_id=" + contentId);
						}
						else if (alCandidate.size() > 0)
						{
							String value = (String)alCandidate.get(0);
							contentId = filterTypes(value, context);
							strategyCode = Strategy.FORCE_INSERT.getCode();
						}
						if (StringUtils.isNotEmpty(contentId))
						{
							ResponParam re = new ResponParam(strategyCode, contentId);
							resp2Pos.put(re, key - offset - 1);
							list2Acs.add(re);
						}
					}
				}
			}
		}

		if (highMap != null)
		{
			for (Map.Entry<Double, List<String>> entry : highMap.entrySet())
			{
				String value = entry.getValue().get(0);
				String contentId = filterTypes(value, context);
				if (StringUtils.isNotEmpty(contentId))
				{
					ResponParam re = new ResponParam(Strategy.FORCE_INSERT.getCode(), contentId);
					resp2Pos.put(re, 0.0);
					list2Acs.add(re);
				}
			}
		}

		List<ResponParam> list2AcsTmp = list2Acs;
		//过已读
		if (CollectionUtils.isNotEmpty(list2AcsTmp))
		{
			if (logger.isDebugEnabled())
			{
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",app is "+ app +" begin cmsStg readFilter time is " + (end-cur)  +",and size is " + list2AcsTmp.size() +",and cur = " + System.currentTimeMillis());
			}
			list2AcsTmp = readedFilter(context,uid, app, list2AcsTmp);
			if (logger.isDebugEnabled())
			{
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",app is "+ app +" end cmsStg readFilter time is " + (end-cur)  +",and size is " + list2AcsTmp.size() +",and cur = " + System.currentTimeMillis());
			}
		}

		Map<Integer, ResponParam> mLocalRequestIndexData = prepareRequestIndexDataToBeFilled(mRequestIndexResponse);
		//结果拼接
		for (Integer index : mLocalRequestIndexData.keySet()) {
			for (ResponParam data : list2AcsTmp)
			{
				if (!hsAllResponse.contains(data.getInfoid()))
				{
					int indexKey = resp2Pos.get(data).intValue();
					if(index == indexKey){//排除置顶位置,预留的位置给强插位置
						mRequestIndexResponse.put(index, data);
						hsAllResponse.add(data.getInfoid());
						break;
					}
					
				}
	
				threadPoolTaskExecutor.submit(()->{
					//判断是否和cache中重复,如果重复则需要将q中进行删除
					if (jedisCluster.hexists( KeyUtils.getDetailKeyByUid(context) , data.getInfoid())) {
						jedisCluster.zrem( KeyUtils.getMainKey(context).getBytes(),  data.getInfoid().getBytes() );
						if (logger.isDebugEnabled()) {
							logger.debug("uid is :"+ uid + ",app is "+ app +" cmsStg removeDuplicate time is " + ( System.currentTimeMillis()-cur) +",and infoid is " + data.getInfoid() +",and cur = " + System.currentTimeMillis());
						}
					}
				});
		   }
		}
	}

	@Scheduled(cron="0/30 * * * * ? ")
	public void loadForceInsert() {
		cmslogger.info("loadForceInsert key :" + this.forceInsertCache.keySet()) ;
		Set<String> keyset = forceInsertCache.keySet();
		for (String key : keyset)
		{
			try {
					if (key.contains("stginfo:timeliness:high:"))
					{
						Set<byte[]> timeliness_high_info = jedisCluster.zrange(key.getBytes(), 0, 0);
						Map<Double, List<String>> highMap = new HashMap<Double, List<String>>();
						for (byte[] string : timeliness_high_info)
						{
							ArrayList<String> alCandidate = new ArrayList<String>();
							alCandidate.add(new String(string));
							highMap.put(0.0D, alCandidate);
						}
						if (highMap.isEmpty())
						{
							this.forceInsertCache.remove(key);
						}
						else
						{
							this.forceInsertCache.put(key, highMap);
						}
					}
					else
					{
						Set<Tuple> timeliness_lower_info = jedisCluster.zrangeWithScores(key.getBytes(), 0, -1);
						Map<Double, List<String>> lowMap = new HashMap<Double, List<String>>();
						for (Tuple tuple : timeliness_lower_info)
						{
							if (StringUtils.isNotEmpty(tuple.getElement()))
							{
								double pos = tuple.getScore();
								ArrayList<String> alCandidate = (ArrayList<String>)lowMap.get(pos);
								if (alCandidate == null)
								{
									alCandidate = new ArrayList<String>();
									lowMap.put(pos, alCandidate);
								}
								alCandidate.add( tuple.getElement() );
							}
						}
						if (lowMap.isEmpty())
						{
							this.forceInsertCache.remove(key);
						}
						else
						{
							this.forceInsertCache.put(key, lowMap);
						}
					}
				} catch (Exception e) {
					logger.error("initForceInsert load key " + key, e);
					continue;
				}
		}

		cmslogger.info("loadForceInsert load cache form redis :" + forceInsertCache) ;
	}

	static private String filterTypes(String contentInfo, Context context) {
		if( context.getZhiziListReq() != null &&  contentInfo.split("#").length == 4 )
		{
			ArticleProperty article_property = context.getArticleProperty();
			ArticleFilterImpl articleFilter = context.getArticleFilter();

			String contentInfoArr[] = contentInfo.split("#");
			article_property.setType(contentInfoArr[1]); //content_type
			article_property.setLink_type(contentInfoArr[2]);
			article_property.setDisplay_type(contentInfoArr[3]);
			article_property.setContent_id(contentInfoArr[0]);

			if ( !articleFilter.isValidArticle(article_property) )
			{
				return null;
			}
			else
			{
				return contentInfo.split("#")[0];
			}
		}
		else
		{
			return contentInfo;
		}
	}

	/**
	 * 获取置顶资讯;
	 * @param resultList
	 * @param context
	 */
	public void addTopData(Context context, Map<Integer, ResponParam> mRequestIndexResponse, Set<String> hsAllResponse) {
		boolean bTopInfo = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifTopInfo"));
		if (!bTopInfo) {
			return ;
		}		
		if (context.getZhiziListReq() != null && (context.getAbilityType() & Constants.TOPFORCEINSERT) != 0) {//是否包含置顶能力集合
			if (context.getZhiziListReq().getOperation() == 2 ) {
				return;
			}
		} else {
			return;
		}

		String interfaceName = "q";
		String semantic = "channel";
		String abTestVersion = context.getAbtestVersion();
		String uid = context.getUid();
		long cur = System.currentTimeMillis();

		if (context.getZhiziListReq() != null) {
			interfaceName = "list";
			semantic = "scenario";
		} else if (context.getRecNewsListReq() != null) {
			if (!context.getRecNewsListReq().isNeedBanner()) {
				interfaceName = "q";
			} else {
				interfaceName = "qb";
			}
		} else if (context.getScenario() > 0) {
			interfaceName = "qcn";
		}

		StringBuffer topkey = new StringBuffer("topinfo:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);


		
		Map<Double, List<String>> topInfoMap = topInfoCache.get(topkey.toString());
		
		if (MapUtils.isEmpty(topInfoMap)) {
			this.topInfoCache.put(topkey.toString(),  Collections.emptyMap());
			topInfoMap = Collections.emptyMap();
		}
		if(MapUtils.isNotEmpty(topInfoMap)){
			for (Map.Entry<Double, List<String>> entry : topInfoMap.entrySet())
			{
				int key = entry.getKey().intValue() - 1;
				List<String> alCandidate = entry.getValue();
				for (String infoId : alCandidate) {	
					if (hsAllResponse.add(infoId)) {
						mRequestIndexResponse.put(key, new ResponParam( Strategy.TOP.getCode() , infoId));
					}
					
				}
			}
		}

		
		if( logger.isDebugEnabled()  ){
			logger.debug("uid is :"+ uid + ",end getTopInfo time is " + (System.currentTimeMillis()-cur)
					+" ,and info is " + mRequestIndexResponse +" ,and cur = " + System.currentTimeMillis() + " topInfoMap " + topInfoMap);
		}
	}

	@Scheduled(cron="0/30 * * * * ? ")
	public void loadTopCache() {
		cmslogger.info("loadTopCache key :" + this.topInfoCache) ;
		Set<String> keyset =  topInfoCache.keySet();
		for (String key : keyset) {
			try {
					Set<Tuple> topInfo = jedisCluster.zrangeWithScores(key.getBytes(), 0, -1);
					Map<Double, List<String>> topMap = new HashMap<Double, List<String>>();
					for (Tuple tuple : topInfo)
					{
						if (StringUtils.isNotEmpty(tuple.getElement()))
						{
							double pos = tuple.getScore();
							ArrayList<String> alCandidate = (ArrayList<String>)topMap.get(pos);
							if (alCandidate == null)
							{
								alCandidate = new ArrayList<String>();
								topMap.put(pos, alCandidate);
							}
							alCandidate.add( tuple.getElement() );
						}
					}
					
					if (topMap.isEmpty())
					{
						this.topInfoCache.remove(key);
					}
					else
					{
						this.topInfoCache.put(key, topMap);
					}

				} catch (Exception e) {
					logger.error("initForceInsert load key " + key, e);
					continue;
				}
		}

		cmslogger.info("loadTopCache load cache form redis :" + topInfoCache) ;
	}


	/**
	 * 增加最新资讯
	 * @param resultList
	 * @param i
	 * @param lastestInfoList
	 * @param indexlist
	 * @param resultSet
	 */
	public boolean addFallbackData(Context context, Map<Integer, ResponParam> mRequestIndexResponse, Set<String> hsAllResponse) {
		// 如果不需要fallback 返回false
		boolean bAddFromFallback = false;
		
		// fallback填充资讯空缺位置
		Map<Integer, ResponParam> mLocalRequestIndexData = prepareRequestIndexDataToBeFilled(mRequestIndexResponse);
		if (MapUtils.isNotEmpty(mLocalRequestIndexData))
		{
			bAddFromFallback = true;

			tsStart = System.currentTimeMillis();
			List<ResponParam> allFallbackData = context.getNewsQList();
			monitorLog.addCntMonitorLogByConfigId(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProductAndScenario(context, MonitorType.FALLBACK_REQUEST_COUNT);

			int i = 0;
			for (Integer index : mLocalRequestIndexData.keySet())
			{
				for ( ; i < allFallbackData.size(); i++) {
					ResponParam data = allFallbackData.get(i);
					if (hsAllResponse.add(data.getInfoid())) {
						mRequestIndexResponse.put(index, data);
						FallbackNewsQ.fallbackLogger.info("add fallback in index:"+index+", id:"+data.getInfoid()+", gmp:"+String.format("%.6f", data.getGmp()));
						break;
					}
				}
			}
			
			tsEnd = System.currentTimeMillis();
			long tsSpent = tsEnd - tsStart;
			RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, context.getRequestId(), "core.fallback");
			logger.info(ContextUtils.toRequestInfoString(context) + " | get news through fallback addFallbackData time is " + tsSpent + " ms.");
		}

		return bAddFromFallback;
	}

	/**
	 * @throws Exception
	 *
	 * readedFilter(已读过滤)
	 * @Title: readedFilter
	 * @param @param list
	 * @param @return    设定文件
	 * @return List<String>    返回类型
	 * @throws
	 */
	private List<ResponParam> readedFilter(Context context, String uid,String app, List<ResponParam> list){
		//long cur = System.currentTimeMillis();
		//logger.info(" begin  ================ ResultProcessAllImpl readedFilter====================time is " + (System.currentTimeMillis() - cur));
		//ReadedInfoHandler readFilterInfo =  SysUtil.getBean("readFilterInfo");
		try {
			list = readFilterInfo.filterIds(context,uid, app, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//logger.info(" end ================ResultProcessAllImpl readedFilter====================time is " + (System.currentTimeMillis() - cur));
		return list;
	}
}
