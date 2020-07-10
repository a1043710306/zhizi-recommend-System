package com.inveno.core.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.core.Constants;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.cms.CMSInterface;
import com.inveno.core.process.cms.MixedRule;
import com.inveno.core.process.PipleLine;
import com.inveno.core.process.PipleLineFactory;
import com.inveno.core.process.newsQ.FallbackNewsQ;
import com.inveno.core.process.relative.RelativeRecommendationProcess;
import com.inveno.core.process.result.IResultProcess;
import com.inveno.core.service.AbstractCoreService;
import com.inveno.core.service.UserService;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.util.RequestTrackingHelper;
import com.inveno.thrift.NewsListResp;
import com.inveno.thrift.NewsListRespzhizi;
import com.inveno.thrift.RecNewsListReq;
import com.inveno.thrift.ResponParam;
import com.inveno.thrift.ResponseCode;
import com.inveno.thrift.UserInfo;
import com.inveno.thrift.TimelineNewsListReq;
import com.inveno.thrift.ZhiziListReq;
import redis.clients.jedis.JedisCluster;

@Component("coreServiceImpl")
public class CoreServiceImpl extends AbstractCoreService{

	public Log logger = LogFactory.getLog(CoreServiceImpl.class);

	public Log errlogger = LogFactory.getLog("toErrFile");

	public Log allInfoReadedLog = LogFactory.getLog("allInfoReadedLog");

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private MonitorLog monitorLog;

	@Autowired
	private IResultProcess resultProcessAllImpl;

	@Autowired
	private IResultProcess getListResultProcessAllImpl;

	@Autowired
	private IResultProcess getAllReturnDataProcessImpl;

	@Autowired
	private FallbackNewsQ fallbackNewsQ;

	@Autowired
	private CMSInterface cmsInterface;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	/*@Autowired
	private  ObjectPool<TServiceClient> ufsThriftPool;*/

	@Autowired
	EhCacheCacheManager cacheManager;

	@Autowired
	UserService userService;
	
	@Autowired
	RelativeRecommendationProcess relativeProcess;

	@Override
	public NewsListRespzhizi infoList(Context context) {

		long begin = System.currentTimeMillis(); 
		ZhiziListReq req = context.getZhiziListReq();

		String uid = req.getUid();
		String app = req.getApp();

		int operation =  req.getOperation();

		int ttl = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ttl"), 300);

		int channelId = ContextUtils.getChannelId(context);
		String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);
		String offsetKey = (channelId > 0) ? KeyUtils.subChannelOffsetKey(context, channelId) : KeyUtils.getMainOffset(context);

		NewsListRespzhizi response = null;
		
		if (operation == 1) {
			response = getRankingDataAndInit(context, true, ttl);
			monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
			monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
			monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
		} else if (operation == 2) {
			int fetchNum = req.getNum();
			long cacheNum = getCacheAndOffset(offsetKey, rankingCacheKey, context, fetchNum);
			if (cacheNum >= fetchNum) {
				if(logger.isDebugEnabled() ){
					logger.debug(context.toString() + ",get data from cache, operation= "+ operation +" cacheNum=" + cacheNum + " offsetkey="+  offsetKey + " offset=" + context.getOffset() );
				}
				
				getAllReturnDataProcessImpl.cache(context);
				response = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, context.getResponseParamList());
				response.setOffset( context.getOffset() + context.getResponseParamList().size()  );
				response.setReset(false);
				response.setTtl(ttl);

				long end = System.currentTimeMillis();
				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_CACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.CACHE_RESPONSE_TIME,(end - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.CACHE_RESPONSE_TIME,(end - begin));
				logger.info(context.toString() + " operation= "+ operation + " size= " + context.getResponseParamList().size()  + " spent time=" + (end -begin));
			}
			else
			{
				if (logger.isDebugEnabled())
				{
					logger.debug(context.toString() + " operation= "+ operation +"  cacheNum=" + cacheNum  + " offsetkey=" + offsetKey + " offset=" + context.getOffset());
				}
				response = getRankingDataAndInit(context, false, ttl);
				long end  = System.currentTimeMillis();
				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));
				response.setOffset(context.getOffset() + context.getResponseParamList().size());
			}
		} else if (operation == 3) {
			int T_TRIGGER = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "T_TRIGGER"), 300);
			int rediscacheExpire = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "rediscacheExpire"), 600);

			long cacheTTL = jedisCluster.ttl(rankingCacheKey);
			int fetchNum = req.getNum();

			long cacheNum = getCacheAndOffset(offsetKey, rankingCacheKey, context, fetchNum);

			if( rediscacheExpire - cacheTTL < T_TRIGGER && cacheNum >= fetchNum)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug(context.toString() + " ,get data from cache, operation= "+ operation +" cacheNum=" + cacheNum + " offsetkey="+  offsetKey + " offset=" + context.getOffset() );

				}
				
				getAllReturnDataProcessImpl.cache(context);
				cleanNullValue(context);
				response =new NewsListRespzhizi(ResponseCode.RC_SUCCESS,context.getResponseParamList());
				response.setOffset( context.getOffset() + context.getResponseParamList().size()  );
				response.setReset(false);
				response.setTtl(ttl);

				long end  = System.currentTimeMillis();
				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_CACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.CACHE_RESPONSE_TIME,(end - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.CACHE_RESPONSE_TIME,(end - begin));
				logger.info(context.toString() + " operation= "+ operation + " spent time=" + (end -begin));
			}
			else
			{
				if (logger.isDebugEnabled())
				{
					logger.debug(context.toString() + " ,get data from cache, operation== "+ operation +" cacheNum=" + cacheNum + " offsetkey="+  offsetKey + " offset=" + context.getOffset() );
					
				}
				response = getRankingDataAndInit(context, false, ttl);
				response.setOffset(context.getResponseParamList().size());


				long end  = System.currentTimeMillis();
				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));

				logger.info(context.toString() + " operation= "+ operation + " ranking rediscacheExpire - cacheTTL >= T_TRIGGER " + " spent time=" +(end -begin));
			}
		} else if (operation == 4) {
			long end  = System.currentTimeMillis();
			response = getRankingDataAndInit(context, true, ttl);
			monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
			monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));
			monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME,(end - begin));
		}

	
		return response;
	}


	@Override
	public NewsListResp oldRecNewsRq(Context context) {

		RecNewsListReq req = context.getRecNewsListReq();
		String uid = context.getUid();
		String app = context.getApp();

		String offsetKey = KeyUtils.getMainOffset(context);
		String key = KeyUtils.getMainKey(context);

		//if NeedBanner is true ,则为qb接口,设置 Display(0x80) Scenario(0x000100)
		if (req.isNeedBanner()) {
			//eg: 99000523605937::fuyiping-gionee::qb
			//context.setScenario(0x020101);
			key  = KeyUtils.getQBKey(context);
			offsetKey = KeyUtils.getQBOffsetKey(context);
			context.setDisplay(0x08);
		} 

		long begin  = System.currentTimeMillis();

		long cacheNum = jedisCluster.zcard(key);
		//if have cache ,then return the : stg + expinfo + cache
		//if( jedisCluster.exists(key) ){
		if( cacheNum >  req.getNum() ){
			NewsListResp res = null;
			try {

				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_CACHE_COUNT);
				String offsetvalue =  jedisCluster.get(offsetKey);
				int offset = NumberUtils.toInt(offsetvalue, 0);
				context.setOffset(offset);

				if( logger.isDebugEnabled() ){
					logger.debug(" uid: "+uid +" ,and after jedisCluster.get(offsetKey) " + context.getOffset());
				}

				if(logger.isDebugEnabled()){
					logger.debug("uid: "+uid +",app: " + app +" ,has men and men size is " + cacheNum +",and offset  is " + context.getOffset());
				}

				resultProcessAllImpl.cache(context);

				res= new NewsListResp(ResponseCode.RC_SUCCESS,context.getResponseParamList());

				long elaTime  = System.currentTimeMillis() - begin;
				logger.warn("uid: "+uid +" ,app: " + app + " ,abtest: "+ req.getAbTestVersion() 
						+" and return result is :" + context.getResponseParamList() +" ,and result size is " 
						+ context.getResponseParamList().size() +" time is " + elaTime);

				monitorLog.addResTimeLogByConfigId(context, MonitorType.CACHE_RESPONSE_TIME,elaTime);
				monitorLog.addResTimeLogByProduct(context, MonitorType.CACHE_RESPONSE_TIME, elaTime);
				return res;
			} catch (Exception e) {
				logger.error("q uid: "+uid +" ,app: " + app + " ,abtest: "+ req.getAbTestVersion() 
						+" and return result is : " + res +" ,and  result size is "+ context.getResponseParamList().size() 
						+" and time is " + (System.currentTimeMillis() - begin) + e,e.getCause());
				return new NewsListResp(ResponseCode.RC_ERROR,Collections.emptyList());
			}
		}else{
			monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
			jedisCluster.del(key);
			initCategoryIds(context);
			List<ResponParam> resultlist = getInfoByZhizi(context);

			long elaTime  = System.currentTimeMillis() - begin;
			logger.info("uid: "+uid +" ,app: "+ app +" ,abtest " + context.getAbtestVersion() + "  and return result is : " 
					+ resultlist +" , and  result size is "+ resultlist.size() +" ,time is " + elaTime +",and cur: " + System.currentTimeMillis()  );
			monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,elaTime);
			monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME, elaTime);
			return new NewsListResp(ResponseCode.RC_SUCCESS,resultlist);
		}
	}

	@Override
	public NewsListResp oldTimelineRq(Context context) {

		TimelineNewsListReq req = context.getTimelineNewsListReq();
		String uid = req.getBase().getUid();
		String app = req.getBase().getApp();
		String offsetKey = KeyUtils.getMainOffset(context);
		String key = KeyUtils.getMainKey(context);

		long begin = System.currentTimeMillis();

		int channelId =  req.getChannelId();
		if( channelId > 0 ){
			context.setScenario(channelId);
			initCategoryIds(context);

			key  = KeyUtils.getQCNKey(context, channelId) ;
			offsetKey = KeyUtils.getQCNOffsetKey(context, channelId) ;
		}

		long cacheNum = jedisCluster.zcard(key);

		//if have cache ,then return the : stg + expinfo + cache
		if( cacheNum  >=  req.getNum() ){
			NewsListResp res = null;
			try {
				monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_CACHE_COUNT);
				String offsetvalue =  jedisCluster.get(offsetKey);
				int offset = NumberUtils.toInt(offsetvalue, 0);
				context.setOffset(offset);

				logger.info("uid: "+uid +",app: " + app +" ,has men and men size is " + cacheNum +" ,and offset  is " + context.getOffset());

				resultProcessAllImpl.cache(context);

				res= new NewsListResp(ResponseCode.RC_SUCCESS,context.getResponseParamList());

				long elaTime  = System.currentTimeMillis() - begin;
				monitorLog.addResTimeLogByConfigId(context, MonitorType.CACHE_RESPONSE_TIME, elaTime);
				monitorLog.addResTimeLogByProduct(context, MonitorType.CACHE_RESPONSE_TIME,  elaTime);
				logger.info("uid: "+uid +" ,app: " + app + " ,abtest: "+ req.getAbTestVersion() +" and "
						+ "return result is :" + context.getResponseParamList() +" ,and time is " + elaTime);
				return res;
			} catch (Exception e) {
				errlogger.error("qcn uid: "+uid +" ,app: " + app + " ,abtest: "+ req.getAbTestVersion() 
						+" and return result is : " + res +" ,and  result size is "+ context.getResponseParamList().size()
						+" and time is " + (System.currentTimeMillis() - begin),e);
				return res;
			}
		}else{
			monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_NOCACHE_COUNT);
			List<ResponParam> resultlist = getInfoByZhizi(context);

			long elaTime  = System.currentTimeMillis() - begin;
			monitorLog.addResTimeLogByConfigId(context, MonitorType.NO_CACHE_RESPONSE_TIME,elaTime);
			monitorLog.addResTimeLogByProduct(context, MonitorType.NO_CACHE_RESPONSE_TIME, elaTime);
			logger.info("qcn uid: "+uid +" ,app: " + app +" ,abtest: "+ req.getAbTestVersion() +" and return result is : "
					+ resultlist +" ,and result size is "+ resultlist.size() +" and no cache time is " + elaTime);
			return new NewsListResp(ResponseCode.RC_SUCCESS,resultlist);
		}

	}

	private void determineStrategyParameter(Context context) {
		Map<Integer, Map<Integer, Integer>> mapContenttypeStrategyArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();

		Map<Integer, MixedRule> mapHighMixRule = cmsInterface.getHighMixRule(context);

		Map<Integer, Integer> mStrategyArticleAmount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mGifStrategyArticleAmount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mMemeStrategyArticleAmount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mBeautyStrategyArticleAmount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mVideoStrategyArticleAmount = new HashMap<Integer, Integer>();
		if (MapUtils.isNotEmpty(mapHighMixRule)) {
			for (Map.Entry<Integer, MixedRule> entry : mapHighMixRule.entrySet()) {
				MixedRule rule = entry.getValue();
				for (List<String> alSource : rule.getSource()) {
					for (String strStrategyCode : alSource) {
						int iStrategyCode = Integer.parseInt(strStrategyCode);
						if (iStrategyCode > 0) {
						    if(ContextUtils.isGifChannel(context)){
						        mGifStrategyArticleAmount.put(iStrategyCode, 200);
						    }else if (ContextUtils.isBeautyChannel(context)) {
						        mBeautyStrategyArticleAmount.put(iStrategyCode, 200);
                            }else if (ContextUtils.isMemesChannel(context)) {
                                mMemeStrategyArticleAmount.put(iStrategyCode, 200);
                            }else if (ContextUtils.isVideoChannel(context)) {
                                mVideoStrategyArticleAmount.put(iStrategyCode, 200);
                            }else{
                                mStrategyArticleAmount.put(iStrategyCode, 200);
                            }
						}
					}
				}
			}
		}
		mapContenttypeStrategyArticleAmount.put(ContentType.NEWS.getValue(), mStrategyArticleAmount);
		mapContenttypeStrategyArticleAmount.put(ContentType.GIF.getValue(), mGifStrategyArticleAmount);
		mapContenttypeStrategyArticleAmount.put(ContentType.BEAUTY.getValue(), mBeautyStrategyArticleAmount);
		mapContenttypeStrategyArticleAmount.put(ContentType.MEME.getValue(), mMemeStrategyArticleAmount);
		mapContenttypeStrategyArticleAmount.put(ContentType.VIDEO.getValue(), mVideoStrategyArticleAmount);

		//2018年4月26日17:48:46 yezi 增加判断是否FORU频道处理，是forU的话才加pipleline 的post的混插处理，否则非forU频道对应的contentType跟混插的contentType相同的话 初选就会做无用的混插准备 
		if(ContextUtils.isForYouChannel(context)){
			String strMixedInsertContentTypeStrategies = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "MixedInsertStrategies");
	//		if(logger.isDebugEnabled()){
	//			////2018年3月13日 15:22:43  yezi remove test add other contentType info!!!!!!---test strMixedInsertContentTypeStrategies
	//			strMixedInsertContentTypeStrategies = "{2:[\"2\"],128:[\"220\"],32:[\"221\"]}";
	//		}
	
			
			if (StringUtils.isNotEmpty(strMixedInsertContentTypeStrategies)) {
				try {
					Map<Integer, List<String>> mMixedInsertPoolMapping = JSON.parseObject(strMixedInsertContentTypeStrategies, HashMap.class);
					for (Map.Entry<Integer, List<String>> entry : mMixedInsertPoolMapping.entrySet()) {
						Integer contentType = entry.getKey();
						List<String> alStrategy = entry.getValue();
						mStrategyArticleAmount = mapContenttypeStrategyArticleAmount.get(contentType);
						if (mStrategyArticleAmount == null) {
							mStrategyArticleAmount = new HashMap<Integer, Integer>();
							mapContenttypeStrategyArticleAmount.put(contentType, mStrategyArticleAmount);
						}
						for (String strStrategyCode : alStrategy) {
							int iStrategyCode = Integer.parseInt(strStrategyCode);
							mStrategyArticleAmount.put(iStrategyCode, 200);
						}
					}
				} catch (Exception e) {
					logger.error(context.toString() + " determineStrategyParameter parsing error : ", e);
				}
			}
		
		}
	
		logger.info(context.toString() + " determineStrategyParameter mapContenttypeStrategyArticleAmount=" + mapContenttypeStrategyArticleAmount);
		context.setContenttypeStrategyArticleAmount(mapContenttypeStrategyArticleAmount);
	}

	private void determineInterestBoostAndExploreParameter(Context context) {

		String strCategoryClickAndImp = (context.getUserInfo() == null) ? "" : context.getUserInfo().getCategoryClickAndImp();
//		if(logger.isDebugEnabled()){//线下兴趣加强会超时打断处理----目前先移除处理方式 yezi test ---线上 remove，线下打开结构体if --2018年3月27日 16:03:24
//			logger.info("determineInterestBoostAndExploreParameter---ufs--categoryClick="+strCategoryClickAndImp);
//			strCategoryClickAndImp = null;
//		}
		
		/**
		 * json_format : { "ts": 1504751055, "data": [ {"category": "103", "click": 58.609, "imp": 423.67}, {"category": "107", "click": 58.609, "imp": 323.67}] }
		 */
		int impressionThresholdToStopInterestExplore = NumberUtils.toInt(context.getComponentConfiguration("impressionThresholdToStopInterestExplore"), 10);
		HashSet<String> hsExploreCategory = new HashSet<String>();
		HashSet<String> hsBoostCategory = new HashSet<String>();
		if (StringUtils.isNotEmpty(strCategoryClickAndImp)) {
			try {
				JSONObject jsonObject = JSONObject.parseObject(strCategoryClickAndImp);
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					String strCategory = obj.getString("category");
					double click      = obj.getDoubleValue("click");
					double impression = obj.getDoubleValue("imp");
					if (click >= 1.0d) {
						hsBoostCategory.add(strCategory);
					} else if (impression < impressionThresholdToStopInterestExplore) {
						hsExploreCategory.add(strCategory);
					}
				}
			} catch (Exception e) {
				logger.error("[determineInterestBoostAndExploreParameter]", e);
			}
		}
		int boostArticleAmount = NumberUtils.toInt(context.getComponentConfiguration("boostArticleAmount"), 200);
		Map<Integer, Map<Integer, Integer>> mapContenttypeBoostCategoryArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();
		HashMap<Integer, Integer> mBoostCategoryArticleAmount = new HashMap<Integer, Integer>();

		int exploreArticleAmount = NumberUtils.toInt(context.getComponentConfiguration("exploreArticleAmount"), 100);
		Map<Integer, Map<Integer, Integer>> mapContenttypeExploreCategoryArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();
		HashMap<Integer, Integer> mExploreCategoryArticleAmount = new HashMap<Integer, Integer>();

		String strBoostCategoryList = context.getComponentConfiguration("boostCategoryList");
		if (StringUtils.isNotEmpty(strBoostCategoryList)) {
			for (String strCategory : strBoostCategoryList.split(";")) {
				if (hsBoostCategory.contains(strCategory)) {
					mBoostCategoryArticleAmount.put(Integer.parseInt(strCategory), boostArticleAmount);
				}
				//最多取三个分类做兴趣探索
				if (hsExploreCategory.contains(strCategory) && mExploreCategoryArticleAmount.size() < 3) {
					mExploreCategoryArticleAmount.put(Integer.parseInt(strCategory), exploreArticleAmount);
				}
			}
		}
		if (MapUtils.isNotEmpty(mBoostCategoryArticleAmount)) {
			mapContenttypeBoostCategoryArticleAmount.put(ContentType.NEWS.getValue(), mBoostCategoryArticleAmount);
		}
		Map<Integer, Integer> mapContenttypeInterestBoostArticleAmount = new HashMap<Integer, Integer>();
		if (MapUtils.isNotEmpty(mapContenttypeBoostCategoryArticleAmount)) {
			mapContenttypeInterestBoostArticleAmount.put(ContentType.NEWS.getValue(), boostArticleAmount);
		}

		logger.info(context.toString() + " determineInterestBoostParameter mapContenttypeBoostCategoryArticleAmount=" + mapContenttypeBoostCategoryArticleAmount + " mapContenttypeInterestBoostArticleAmount=" + mapContenttypeInterestBoostArticleAmount);
		context.setContenttypeBoostCategoryArticleAmount(mapContenttypeBoostCategoryArticleAmount);
		context.setContenttypeInterestBoostArticleAmount(mapContenttypeInterestBoostArticleAmount);

		//
		if (MapUtils.isNotEmpty(mExploreCategoryArticleAmount)) {
			mapContenttypeExploreCategoryArticleAmount.put(ContentType.NEWS.getValue(), mExploreCategoryArticleAmount);	
		}
		Map<Integer, Integer> mapContenttypeInterestExploreArticleAmount = new HashMap<Integer, Integer>();
		if (MapUtils.isNotEmpty(mapContenttypeExploreCategoryArticleAmount)) {
			mapContenttypeInterestExploreArticleAmount.put(ContentType.NEWS.getValue(), exploreArticleAmount);
		}

		logger.info(context.toString() + " determineInterestExploreParameter mapContenttypeExploreCategoryArticleAmount=" + mapContenttypeExploreCategoryArticleAmount + " mapContenttypeInterestExploreArticleAmount=" + mapContenttypeInterestExploreArticleAmount);
		context.setContenttypeExploreCategoryArticleAmount(mapContenttypeExploreCategoryArticleAmount);
		context.setContenttypeInterestExploreArticleAmount(mapContenttypeInterestExploreArticleAmount);
	}
	/**
	 * 通过流程获取推荐资讯
	 * @param context
	 * @return
	 */
	private List<ResponParam> getInfoByZhizi(Context context) {

		String requestId = context.getRequestId();
		long tsStart, tsEnd;

		long begin = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();

		tsStart = System.currentTimeMillis();
		UserInfo userInfo = userService.getUserInfo(context);
		if (userInfo != null) {
			context.setUserInfo(userInfo);
			determineInterestBoostAndExploreParameter(context);
		}
		determineStrategyParameter(context);

		tsEnd = System.currentTimeMillis();
		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_ufs");

		//异步事件调用
		asynchronousTask(context);
		long elaTime  = System.currentTimeMillis() - begin;
		logger.warn(context.toString() + " end  asynchronousTask(context) task and return result size is :" 
				+ context.getResponseParamList().size()+" and fallback size is " + context.getNewsQList().size() +",and time is " 
				+ elaTime +",and cur = " + System.currentTimeMillis()   );

		if (context.getZhiziListReq() != null) {
			getAllReturnDataProcessImpl.noCache(context);
		} else {
			//拼装返回结果
			resultProcessAllImpl.noCache(context);
		}

		elaTime  = System.currentTimeMillis() - begin;
		logger.info(context.toString() + " end resultProcessAllImpl.noCache(context) result size is :" 
				+ context.getResponseParamList().size() +",and time is " + elaTime  +",and cur = " + System.currentTimeMillis()  );
		List<ResponParam> res = null;

		//此处表示:fallback也没有info;
		//则hotday不需要此逻辑,已经后续getList接口;并且金立也不需要;
		boolean bRoundRobinInfo = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "roundrobinInfo"));
		if (context.getIndexList().size() > 0 && bRoundRobinInfo)
		{
			logger.warn(context.toString() + " info has out ,should take care of this ...and  begin use readed infoList");

			context.setResponseParamList( context.getResponseParamListBak() );

			if (context.getZhiziListReq() != null)
			{
				getAllReturnDataProcessImpl.noCache(context);
			}
			else
			{
				//拼装返回结果
				resultProcessAllImpl.noCache(context);
			}
		}

		//删除返回值中的 null 值,保证不报错
		res = context.getResponseParamList();
		Iterator<ResponParam> it = res.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}

		if (CollectionUtils.isEmpty(res)) {
			elaTime  = System.currentTimeMillis() - begin;
			logger.warn(" zhizi_core_allinfo_readed uid: "+uid +" ,app: "+ app +" ,abtest " 
					+ context.getAbtestVersion() +  " and scenario is "+ context.getScenario() 
					+" ,and lan is "+ context.getLanguage() +"  and return result is : " + res 
					+" , and  result size is "+ res.size() +" ,time is " + elaTime +",and cur: " + System.currentTimeMillis()  );

			allInfoReadedLog.error(" zhizi_core_allinfo_readed uid: "+uid +" ,app: "+ app +" ,abtest " + context.getAbtestVersion() 
					+  " and scenario is "+ context.getScenario() +" ,and lan is "+ context.getLanguage() 
					+"  and return result is : " + res +" , and  result size is "+ res.size() 
					+" ,time is " + elaTime +",and cur: " + System.currentTimeMillis()  );
			monitorLog.addCntLogByProductAndScenario(context, MonitorType.ALLINFO_READED);
		}

		return res;
	}


	/**
	 * 
	  * AsynchronousTask(异步执行事件流和最新资讯)
	  * @Title: AsynchronousTask
	  * @param @param context    设定文件
	  * @return void    返回类型
	  * @throws
	 */
	public void asynchronousTask(Context context) {
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		if( logger.isDebugEnabled() ){
			logger.debug(context.toString() + " begin AsynchronousTask time is " + (cur) +" ,and cur: " + System.currentTimeMillis()  );
		}

		CountDownLatch latch = new CountDownLatch(2);
		List<FutureTask<List<ResponParam>>> taskList = new ArrayList<FutureTask<List<ResponParam>>>();  
		//异步调用
		asynchronousInvoke(context, latch, taskList);

		//获取异步调用的结果
		getAsynchronousResult(context, latch, taskList);

		if( logger.isDebugEnabled() ){
			logger.debug(context.toString() + " end AsynchronousTask time is " + (System.currentTimeMillis() - cur) +" ,and cur:" + System.currentTimeMillis()  );
		}
	}


	/**
	 * 
	  * AsynchronousInvoke(异步调用事件流程和最新资讯)
	  * @Title: AsynchronousInvoke
	  * @param @param context
	  * @param @param latch
	  * @param @param taskList    设定文件
	  * @return void    返回类型
	  * @throws
	 */
	private void asynchronousInvoke(Context context, CountDownLatch latch, List<FutureTask<List<ResponParam>>> taskList) {
		//事件流程调用
		FutureTask<List<ResponParam>> pipleLineTask = new FutureTask<List<ResponParam>>(new Callable<List<ResponParam>>() {
			@Override
			public List<ResponParam> call() throws Exception {
				String requestId = context.getRequestId();
				long tsStart, tsEnd;

				tsStart = System.currentTimeMillis();
				List<ResponParam> list = new ArrayList<ResponParam>();
				try {
					list = pipleLineInvoke(context);
				} catch (Exception e) {
					logger.warn("===pipleLineInvoke  Exception===", e);
					errlogger.error(" process Exception", e);
				} finally {
					latch.countDown();
					if (list.size() >= context.getNum()) {
						//if the flow is faster then newsQTask ,then do not wait newsQTask
						latch.countDown();
					}
				}
				tsEnd = System.currentTimeMillis();
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_pipeline");
				return list;
			}
		});

		FutureTask<List<ResponParam>> newsQTask = new FutureTask<List<ResponParam>>(new Callable<List<ResponParam>>() {
			@Override
			public List<ResponParam> call() throws Exception {
				String requestId = context.getRequestId();
				long tsStart, tsEnd;

				tsStart = System.currentTimeMillis();
				List<ResponParam> list = new ArrayList<ResponParam>();
				try {
					list = getNewsQs(context);
				} catch (Exception e) {
					logger.warn("===pipleLineInvoke fallback Exception===", e);
					errlogger.error(" process fallback Exception," + e);
				} finally {
					latch.countDown();
				}
				tsEnd = System.currentTimeMillis();
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_fallback");
				return list;
			}
		});

		taskList.add(pipleLineTask);
		taskList.add(newsQTask);
		threadPoolTaskExecutor.submit(pipleLineTask);
		threadPoolTaskExecutor.submit(newsQTask);
	}

	/**
	 * 获取异步调用的结果
	 * @param context
	 * @param latch
	 * @param taskList
	 */
	private void getAsynchronousResult(Context context, CountDownLatch latch, List<FutureTask<List<ResponParam>>> taskList) {

		
		int iCoreTimeout =  NumberUtils.toInt(context.getComponentConfiguration("timeOut/coreTimeOut"), 250);		
		
		String uid = context.getUid();

		boolean bExecuteFinished = false;
		try {
			bExecuteFinished = latch.await(iCoreTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			logger.error(context.toString() + " latch.await.InterruptedException wait for fallback info and pipeLine===" +e1,e1.getCause());
		}

		if (!bExecuteFinished) {
			context.setAsynPipeLineTimeout(true);
			logger.info(context.toString() + " getAsynchronousResult timeout!");
		}

		int i =0;
		for (FutureTask<List<ResponParam>> futureTask : taskList) {
			i++;
			try {
				List<ResponParam> list = futureTask.get(1, TimeUnit.MILLISECONDS);
				if (bExecuteFinished && CollectionUtils.isNotEmpty(list) && i ==1) {
					//resultList.addAll(list);
					context.setResponseParamList(list);
				}
				if (CollectionUtils.isNotEmpty(list) && i ==2) {
					//resultList.addAll(list);
					context.setNewsQList(list);
					 
				}
			} catch (InterruptedException e) {
				logger.warn(context.toString() + " futureTask.get InterruptedException");
			} catch (ExecutionException e) {
				logger.warn(context.toString() + " futureTask.get ExecutionException");
			} catch (TimeoutException e) {
				logger.error(context.toString() + " futureTask.get TimeoutException");
				futureTask.cancel(true);
			}
		}
	}

	/**
	 * 
	  * getNewsQs(获取最新资讯)
	  * @Title: getNewsQs
	  * @param @param context
	  * @param @return    设定文件
	  * @return List<ResponParam>    返回类型
	  * @throws
	 */
	public List<ResponParam> getNewsQs(Context context){

		long beginTime = System.nanoTime();
		List<ResponParam> returnList = new ArrayList<ResponParam>();
		if(ContextUtils.isSubscriptionChannel(context)){
			return returnList;
		}
		try {
			returnList = fallbackNewsQ.process(context);
			if (logger.isDebugEnabled()) {
				logger.debug(context.toString() + " getNewsQ from fallback, list size: "+returnList.size()+" cost: "+((System.nanoTime() - beginTime)/1000000)+"ms");
			}
		} catch (TException e) {
			logger.warn(context.toString() + " getNewsQs error===",e );
		}
		return returnList;
	}

	/**
	 * 
	  * pipleLineInvoke(事件流处理)
	  * @Title: pipleLineInvoke
	  * @param @param context    设定文件
	  * @return void    返回类型
	  * @throws
	 */
	public List<ResponParam> pipleLineInvoke(Context context){
		try {
			long cur = System.currentTimeMillis();
			PipleLine pipleLine = PipleLineFactory.getPipleLine(context);
			pipleLine.invoke(context);
			long end = System.currentTimeMillis();
			logger.debug(context.toString() + " end pipleLine.invokeFlow, time is " + (end - cur));
		} catch (Exception e) {
			logger.warn(context.toString() + " pipleLineInvoke error is==="+e,e.getCause() );
		}
		return context.getResponseParamList();
	}

	/**
	 * 新下发接口中 op == 1,为新开启客户端,缓存失效情况,即重新ranking 
	 * @param context
	 * @return
	 */
	private NewsListRespzhizi getRankingDataAndInit(Context context, boolean reset, int ttl)
	{
		long tsStart = System.currentTimeMillis();

		List<ResponParam> list = null;
		NewsListRespzhizi res = null;
		try
		{
			initCategoryIds(context);
			cleanCache(context);

			list = getInfoByZhizi(context);
			res = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, list);

			res.setReset(reset);
			//如果全部都为null,即 位置信息都在,则reset ==false;下发条数为 0
			if (context.getIndexList().size() == context.getNum())
			{
				res.setReset(false);
			}
			//added by genix.li@2017/08/10, always send reset=true for comic channel if any reranking occurs
			if (ContextUtils.isComicChannel(context)) {
				res.setReset(true);
			}
			res.setTtl(ttl);
			res.setOffset(list.size()) ;
		}
		catch (Exception e)
		{
			list = Collections.emptyList();
			res = new NewsListRespzhizi(ResponseCode.RC_ERROR, list);
			res.setTtl(ttl);
			res.setOffset(0) ;
			res.setReset(reset);
			//如果全部都为null,即 位置信息都在,则reset ==false;下发条数为 0
			if (context.getIndexList().size() == context.getNum())
			{
				res.setReset(false);
			}
		} finally {
			long tsEnd = System.currentTimeMillis();
			logger.info(context.toString() + " and time is  " + (tsEnd - tsStart)  +" ,size is " + list.size());
		}
		return res;
	}


	/***
	 * @date 2018-9-29 16:06:13
	 * 获取相关文章信息列表入口
	 * @param context 上下文
	 * @return NewsListRespzhizi
	 * @author yezi
	 */
	@Override
	public NewsListRespzhizi relatedRecommendList(Context context) {
		NewsListRespzhizi response = null;
		try {
			response = relativeProcess.process(context);
		} catch (TException e) {
			logger.info(context.toString() + ",get relatedRecommendList exception!! " );
			monitorLog.addCntLogByProduct(context,MonitorType.RELATED_EXCEPTION_COUNT);
		}
		return response;
	}


	
	
}
