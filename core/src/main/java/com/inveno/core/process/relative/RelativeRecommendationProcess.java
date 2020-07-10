package com.inveno.core.process.relative;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.gmp.impl.PrimarySelectionRelateNewsImpl;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.util.RequestTrackingHelper;
import com.inveno.core.util.SysUtil;
import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.thrift.NewsListRespzhizi;
import com.inveno.thrift.ResponParam;
import com.inveno.thrift.ResponseCode;

import redis.clients.jedis.JedisCluster;

@Component("RelativeRecommendationProcess")
public class RelativeRecommendationProcess {
	public static final String SUFFIX_IMPRESSION_ACS_KEY = "impression";
	public static Log relativeLogger = LogFactory.getLog("relative");

	private Log logger = LogFactory.getLog(RelativeRecommendationProcess.class);
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Autowired
	private MonitorLog monitorLog;
	@Autowired
	private ReadedInfoHandler readFilterInfo;

	@Autowired
	private JedisCluster jedisCluster;

	/***
	 * @date 2018-9-29 16:06:13
	 * 获取相关文章信息列表入口
	 * @param context 上下文
	 * @return NewsListRespzhizi
	 * @author yezi
	 */
	public NewsListRespzhizi process(Context context) throws TException {
	
			long begin = System.currentTimeMillis();
			int fetchNum = context.getNum();
			String relatedRankingCacheKey = "";
			String relatedDetailKey = "";
			NewsListRespzhizi response = null;
			
			int ttl = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ttl"), 300);
			int relatedCallType = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "relatedCallType"), Constants.CALLRELATEDTYPE_PUBLISHER);
			String relateCallTypeKey = "publisher";
			String relateCallTypeValue = "";
			if(relatedCallType == Constants.CALLRELATEDTYPE_PUBLISHER){
				relateCallTypeKey = "publisher";			
				relateCallTypeValue = getPublisher(context.getContentId());//getPublisher(context.getContentId());//1093800653     
				if(StringUtils.isEmpty(relateCallTypeValue)){
					logger.error(context.toString() + ",getPublisher is null " );
					monitorLog.addCntLogByProduct(context,MonitorType.RELATED_EMPTY_PUBLISHER_COUNT);
					return response;
				}
				context.setPublisher(relateCallTypeValue);
			}
			//context.getApp() + "#" + context.getLanguage() + "#" + contentType + "#" + relateCallTypeKey + "#" +relateCallTypeValue+"#relate-rank";
			relatedRankingCacheKey = KeyUtils.getRelatedRankingCacheKey(context, relateCallTypeKey, relateCallTypeValue);
			relatedDetailKey = KeyUtils.getRelatedNewsDetailCacheKey(context, relateCallTypeKey, relateCallTypeValue);//context.getApp() + "#" + context.getLanguage() + "#" + contentType + "#" + relateCallTypeKey + "#" +relateCallTypeValue+"#relate-detail";;
			try {
				logger.info(context.toString() + ",origin relatedRankingCacheKey= " + relatedRankingCacheKey + ",origin relatedDetailKey= " + relatedRankingCacheKey);
				relatedRankingCacheKey = Base64.getEncoder().encodeToString(relatedRankingCacheKey.getBytes("utf-8"));
				relatedDetailKey = Base64.getEncoder().encodeToString(relatedDetailKey.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error(context.toString() + ",base64 encoder  error! ");
			}
			long cacheNum =  jedisCluster.zcard(relatedRankingCacheKey);
			logger.info(context.toString() +  ",base64 encoder relatedRankingCacheKey= " + relatedRankingCacheKey+ ",base64 encoder relatedDetailKey="+relatedDetailKey+",cacheNum = "+cacheNum);
			
			if (cacheNum >= fetchNum) {				
				response = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, getRelatedRecommentNewsList(context,relatedRankingCacheKey,relatedDetailKey,true));			
				response.setReset(false);
				response.setTtl(ttl);
				
				monitorLog.addCntLogByProduct(context, MonitorType.RELATED_REQUEST_CACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.RELATED_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.RELATED_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
				logger.info(context.toString() + ",from cache size= " + response.getData().size()  + " spent time=" + (System.currentTimeMillis() - begin));
			}
			else
			{
				response = initRelatedDataAndAddCache(context,relatedRankingCacheKey,relatedDetailKey);
				response.setOffset(context.getOffset() + context.getResponseParamList().size());
				
				monitorLog.addCntLogByProduct(context, MonitorType.RELATED_REQUEST_NOCACHE_COUNT);
				monitorLog.addResTimeLogByConfigId(context, MonitorType.RELATED_NO_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
				monitorLog.addResTimeLogByProduct(context, MonitorType.RELATED_NO_CACHE_RESPONSE_TIME,(System.currentTimeMillis() - begin));
				logger.info(context.toString() + ",from no cache size= " + response.getData().size()  + " spent time=" + (System.currentTimeMillis() - begin));
			}
		
			
			return response;
		}
		
	
	
	
	/**
	 * @date 2018-9-29 16:05:21
	 * 根据文章id获取publisher信息
	 * @param contentId 文章id
	 * @return publisher
	 * @author yezi
	 */
	protected String getPublisher(String contentId){
		// get FeederInfo from redis
		byte[] value = jedisCluster.hget(("news_" + contentId).getBytes(), "API".getBytes());
		if (value == null) {
			return null;
		}
		// deserialize into feederInfo
		FeederInfo feederInfo = new FeederInfo();
		try {
			new TDeserializer().deserialize(feederInfo, value);
		} catch (TException e) {
			logger.error("TDeserializer TException:" + e);
		}
		
		return feederInfo.getPublisher();		
	}
	
	/**
	 * @date 2018-9-29 16:03:27
	 * 初始化相关新闻列表，并将结果集写入redis缓存中
	 * @param context
	 * @param relatedRankingCacheKey
	 * @return NewsListRespzhizi
	 * @author yezi
	 */
	public NewsListRespzhizi initRelatedDataAndAddCache(Context context,String relatedRankingCacheKey,String relatedDetailKey){
		long tsStart = System.currentTimeMillis();
		int ttl = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ttl"), 300);

		List<ResponParam> list = null;
		NewsListRespzhizi res = null;
		try
		{
			list = asynGetRelatedData(context,relatedRankingCacheKey,relatedDetailKey);
			res = new NewsListRespzhizi(ResponseCode.RC_SUCCESS, list);
			res.setReset(true);
			//如果全部都为null,即 位置信息都在,则reset ==false;下发条数为 0
			if (context.getIndexList().size() == context.getNum())
			{
				res.setReset(false);
			}
			
			res.setTtl(ttl);
			res.setOffset(list.size()) ;
		}
		catch (Exception e)
		{
			logger.error(context.toString() + " e is  " + e);
			list = Collections.emptyList();
			res = new NewsListRespzhizi(ResponseCode.RC_ERROR, list);
			res.setTtl(ttl);
			res.setOffset(0) ;
			res.setReset(true);
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
	
	
	
	/**
	 * @date 2018-9-29 16:04:34
	 *  异步从初选中获取相关新闻列表
	 * @param context 上下文
	 * @return List<ResponParam> 推荐文章列表
	 * @author yezi
	 */
	private List<ResponParam> asynGetRelatedData(Context context,String relatedRankingCacheKey,String relatedDetailKey) {
		long begin = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		List<ResponParam> res = null;

//		UserInfo userInfo = userService.getUserInfo(context);
//		if (userInfo != null) {
//			context.setUserInfo(userInfo);
//		}
//		RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_ufs");

		//异步事件调用
		asynchronousRelativeTask(context);

		long elaTime  = System.currentTimeMillis() - begin;
		logger.info(context.toString() + " end  asynchronousTask(context) task and return result size is :" 
				+ context.getResponseParamList().size() +",and time is " 
				+ elaTime +",and cur = " + System.currentTimeMillis()   );
		
		if (context.getZhiziListReq() != null) {
			logger.info(context.toString() + " add To Cache" );
			//get data
			res = getRelatedRecommentNewsList(context,relatedRankingCacheKey,relatedDetailKey,false);
			//add to cache
			try {
				addToCache(context, relatedRankingCacheKey,relatedDetailKey, res);
			} catch (Exception e) {
				logger.error(context.toString() + " add To Cache: e=" + e);
			}
		}
		
		elaTime  = System.currentTimeMillis() - begin;
		logger.info(context.toString() + " end getRelatedRecommentNewsList.noCache(context) result size is :" 
				+ context.getResponseParamList().size() +",and time is " + elaTime  +",and cur = " + System.currentTimeMillis()  );

		//删除返回值中的 null 值,保证不报错
		Iterator<ResponParam> it = res.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}

		if (CollectionUtils.isEmpty(res)) {
			elaTime  = System.currentTimeMillis() - begin;
			logger.warn(" asynGetRelatedData uid: "+uid +" ,app: "+ app +" ,abtest " 
					+ context.getAbtestVersion() +  " and scenario is "+ context.getScenario() 
					+" ,and lan is "+ context.getLanguage() +"  and return result is : " + res 
					+" , and  result size is "+ res.size() +" ,time is " + elaTime +",and cur: " + System.currentTimeMillis()  );
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
	public void asynchronousRelativeTask(Context context) {
		long cur = System.currentTimeMillis();
		if( logger.isDebugEnabled() ){
			logger.debug(context.toString() + " begin AsynchronousTask time is " + (cur) +" ,and cur: " + System.currentTimeMillis()  );
		}

		CountDownLatch latch = new CountDownLatch(1);
		List<FutureTask<List<ResponParam>>> taskList = new ArrayList<FutureTask<List<ResponParam>>>();  
		//异步调用


		//事件流程调用
		FutureTask<List<ResponParam>> pipleLineTask = new FutureTask<List<ResponParam>>(new Callable<List<ResponParam>>() {
			@Override
			public List<ResponParam> call() throws Exception {
				String requestId = context.getRequestId();
				long tsStart, tsEnd;

				tsStart = System.currentTimeMillis();
				List<ResponParam> list = new ArrayList<ResponParam>();
				try {
					PrimarySelectionRelateNewsImpl relatedNews = SysUtil.getBean("PrimarySelectionRelateNewsImpl");
					list = relatedNews.process(context);
				} catch (Exception e) {
					logger.warn(context.toString() +",get relatedNews from PrimarySelectionRelateNewsImpl error ！" );
					logger.error(" process Exception", e);
				} finally {
					latch.countDown();					
				}
				tsEnd = System.currentTimeMillis();
				RequestTrackingHelper.logCheckPoint(tsStart, tsEnd, requestId, "core.invoke_relatedNews");
				return list;
			}
		});


		taskList.add(pipleLineTask);
		threadPoolTaskExecutor.submit(pipleLineTask);

		//获取异步调用的结果
		int timeout =  NumberUtils.toInt(context.getComponentConfiguration("timeOut/coreTimeOut"), 250);		

		boolean bExecuteFinished = false;
		try {
			bExecuteFinished = latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			logger.error(context.toString() + " latch.await.InterruptedException pipeLine===" +e1,e1.getCause());
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
					context.setResponseParamList(list);
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

		if( logger.isDebugEnabled() ){
			logger.debug(context.toString() + " end AsynchronousTask time is " + (System.currentTimeMillis() - cur) +" ,and cur:" + System.currentTimeMillis()  );
		}
	}
	
	/**
	 * @date 2018-9-29 16:06:13
	 * 获取相关文章信息列表
	 * @param context 上下文
	 * @param relatedRankingCacheKey 
	 * @param isByCache 是否rankingcache
	 * @return List<ResponParam>
	 * @author yezi
	 */
	public List<ResponParam> getRelatedRecommentNewsList(Context context,String relatedRankingCacheKey,String relatedDetailKey ,boolean isByCache){
		logger.info(context.toString() + ", getRelatedRecommentNewsList,isByCache = "+isByCache);
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		long tsStart = System.currentTimeMillis();
		Map<Integer, ResponParam> mRequestIndexResponse = new LinkedHashMap<Integer, ResponParam>();
		int reqFetchCnt = context.getNum();
		for (int i = 0; i < reqFetchCnt; i++) {
			mRequestIndexResponse.put(i, null);
		}
		Set<String> contentIdInfoSet = new HashSet<String>();
		contentIdInfoSet.add(context.getContentId());//移除本身contentId
		Random random = new Random();
		
		if(isByCache){
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			Set<byte[]> set = new HashSet<byte[]>();
			set = jedisCluster.zrange(relatedRankingCacheKey.getBytes(), 0,  - 1);
			List<byte[]> alData = jedisCluster.hmget(relatedDetailKey.getBytes(), set.toArray(new byte[0][0]));
			int i = 0;
			for (Integer index : mRequestIndexResponse.keySet()) {
				
				for ( ; i < alData.size(); i++) {
					int randomIndex = random.nextInt(alData.size()); // 生成一个随机下标
					byte[] bs = (byte[])alData.get(randomIndex);
					try {
						ResponParam data = new ResponParam();
						deserializer.deserialize(data, bs);
						if (data != null && contentIdInfoSet.add(data.getInfoid())) {						
							mRequestIndexResponse.put(index, data);
							break;
						}
					}
					catch (TException e) {
						logger.error(strRequestInfo + " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
					}
				}
			}
	    }else{
			List<ResponParam> alRanking = context.getResponseParamList();
			int offset = 0;
			if (logger.isDebugEnabled()){
				logger.debug(strRequestInfo + ", start addRecommendationData without ranking cache alRanking.size=" + alRanking.size() + "\toffset=" + offset);
			}
			
			for (Integer index : mRequestIndexResponse.keySet()){			
				for (; offset < alRanking.size(); offset++){
					int randomIndex = random.nextInt(alRanking.size()); // 生成一个随机下标
					ResponParam data = (ResponParam)alRanking.get(randomIndex);					
						if (contentIdInfoSet.add(data.getInfoid())){
							mRequestIndexResponse.put(index, data);							
							break;
						}					
				}
			}
		}
		logger.debug(strRequestInfo + ", end getRelatedRecommentNewsList size=" + mRequestIndexResponse.size() );
		return new java.util.ArrayList<ResponParam>(mRequestIndexResponse.values());
	}
	
	

	
	/**
	* @date 2018-9-29 16:06:13
	* 增加到缓存,增加到redis,同时将结果从context中移除
	* @param context
	* @param resultList
	* @author yezi
	*/
	public void addToCache(Context context,String redisKey,String relatedDetailKey, Collection<ResponParam> resultList) {
		String addToCacheLogTag = "addToCache:" + context.getRequestId();
		int channelId = ContextUtils.getChannelId(context);
		logger.debug(addToCacheLogTag + ", channelId=" + channelId  + ", context.getResponseParamList.size=" + context.getResponseParamList().size() + ", context.getNum="+context.getNum());
		//缓存的资讯条数 一定要大于 当前需要的资讯条数
		//其实此处会有误杀:例如 getNum==8,推荐过已读之后长度为6,但是有强插和探索资讯4条,其实最终只需要2条，但是我们还是用<=8
		//但实际上，只要不发生reRank context.getResponseParamList().size() 一直是0
		if (context.getResponseParamList().size() <= context.getNum()){
			return;
		}	
		//缓存生效事件，默认600s
		final int rediscacheExpire = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "rediscacheExpire"), 600);
		List<ResponParam> cacheList = context.getResponseParamList();
		logger.debug(addToCacheLogTag + ", redis key: redisKey=" + redisKey+",context.getResponseParamList().size=" + cacheList.size() + ",resultList.size=" + resultList.size());		
		//使用线程池进行处理:后台处理
		threadPoolTaskExecutor.submit(() -> {
			logger.debug(addToCacheLogTag + ",threadPoolTaskExecutor begin");
			long begin = System.currentTimeMillis();
			long current = 0;
			if (logger.isDebugEnabled()) {
				logger.debug(addToCacheLogTag + ", threadPoolTaskExecutor msg: " + " abtest="+  context.getAbtestVersion() 
						+" , ActiveCount="+ threadPoolTaskExecutor.getActiveCount()
						+" , CorePoolSize=" + threadPoolTaskExecutor.getThreadPoolExecutor().getCorePoolSize() 
						+" , CompletedTaskCount=" + threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount()
						+" , TaskCount=" + threadPoolTaskExecutor.getThreadPoolExecutor().getTaskCount());
			}
			int cacheMaxCnt = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "cacheMaxCnt"), 1000);

			Map<byte[], Double> map = null;
			Map<byte[], byte[]> detailMap = null;
			List<ResponParam> list = null;
			try {
				//map为zset存放,detaiMap为hash存放--批量操作
				map = new HashMap<byte[], Double>();
				detailMap = new HashMap<byte[], byte[]>();
				list = new ArrayList<ResponParam>();

				double i = 0;				
				TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
				/**
				* 存放缓存逻辑:分开频道存放,统一个detail
				* 但是如果一个contentID,在多个频道都进行了下发,但是推荐策略不同,
				* 因此会导致后面hset的detail会覆盖前面的值,因此增加了频道 #scenario的进行区分
				*/
				for (ResponParam responParam : cacheList) {
					i++;
					if (i <= cacheMaxCnt) {
						StringBuffer contentMsg = new StringBuffer();
						contentMsg.append(responParam.getInfoid());
						
						map.put( contentMsg.toString().getBytes(), i);
						detailMap.put(contentMsg.toString().getBytes(), serializer.serialize(responParam));
						list.add(responParam);
					}
				}
				
				current = System.currentTimeMillis();
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(redisKey.getBytes(), map);
				jedisCluster.hmset(relatedDetailKey.getBytes(), detailMap);
				logger.debug(addToCacheLogTag +", add cache into redis, write size=" + list.size() + " , spend time=" + (System.currentTimeMillis() - current));
			} catch (Exception e) {
				logger.error(addToCacheLogTag + " abtest "+  context.getAbtestVersion() +"=== addToCache error " + e ,e.getCause());
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(redisKey.getBytes(), map);
				jedisCluster.hmset(relatedDetailKey.getBytes(), detailMap);
			} finally {
				current = System.currentTimeMillis();
				jedisCluster.expire(redisKey.getBytes(), rediscacheExpire);
				jedisCluster.expire(relatedDetailKey.getBytes(), rediscacheExpire);
			
				logger.debug(addToCacheLogTag +", rankingCacheKey=" + redisKey  + " finish setExpire " + rediscacheExpire + " sec, spend time is " + (System.currentTimeMillis() - current));
				logger.debug(addToCacheLogTag +", finish all tasks, spend time=" + (System.currentTimeMillis() - begin));
			}
		});
		
	}
	
	
	

	
	
}
