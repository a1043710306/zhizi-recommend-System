package com.inveno.core.process.result.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.process.MultiStrategyMixControl;
import com.inveno.core.process.post.process.PicSimHashControl;
import com.inveno.core.process.post.process.SimHashControl;
import com.inveno.core.process.post.process.VideoMixControl;
import com.inveno.core.process.result.AbstractReturnDataProcess;
import com.inveno.core.util.KeyUtils;

import com.inveno.thrift.ResponParam;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.util.TimeUtil;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component("testAllReturnDataProcessImpl")
public class TestAllReturnDataProcessImpl extends AbstractReturnDataProcess {
	private Log logger = LogFactory.getLog(GetListResultProcessAllImpl.class);
	
	@Autowired
	private JedisCluster jedisCluster;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Autowired
	private MonitorLog monitorLog;
	
	@Autowired
	EhCacheCacheManager cacheManager;
	
	@Override
	public void getInfoByCache(Context context)
	{
		String uid = context.getUid();
		
		List<ResponParam> resultList = getReturnData(context, true);

		context.setEndResponseParamList(resultList);
		logger.info("uid is "+ uid +" ,ResultProcessAllImpl return through cache,size is " + context.getResponseParamList().size());
	}
	
	@Override
	public void getInfoByNoCache(Context context) {
		//get uid,app 用户id和渠道
		String uid =  context.getUid();
		long cur = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		
		if (logger.isDebugEnabled())
		{
			logger.debug("uid is "+ uid +" ,begin ResultProcessAllImpl  noCache , " + "and result size is " + context.getResponseParamList().size() + " and time is  " + cur );
		}
		if (logger.isDebugEnabled())
		{
			end  = System.currentTimeMillis();
			logger.debug("uid is "+ uid +" ,begin getReturnData time is " + (end-cur) +",and cur: " + System.currentTimeMillis()  );
		}
		//拼装返回结果
		final List<ResponParam> resultList = getReturnData(context, false);
		
		if (logger.isDebugEnabled())
		{
			end  = System.currentTimeMillis();
			logger.debug("uid is "+ uid +",end getReturnData time is " + (end-cur) +",begin add2Cache," + "and remain size is "+ context.getIndexList().size() + " size is " + resultList.size() + ",and cur = " + System.currentTimeMillis()  );
		}
		
		context.setEndResponseParamList(resultList);
		
		if (logger.isDebugEnabled()) {
			logger.debug("uid is "+ uid + " ResultProcessAllImpl result return ,through noCache time is " + (System.currentTimeMillis()-cur) +",and remain is " + context.getIndexList() +",and cur = " + System.currentTimeMillis()  );
		}
	}
	
	/**
	 * 用于查询单个uid下发cache的数据信息
	 * @param uid
	 * @author yezi
	 */
	public  void getOneUidForUCoreCacheList(String uid){
		final String rankingCacheKey = uid+"::noticias::q";
		final String detailCacheKey  =  uid+"::noticias::detail";
		long tsStart = System.currentTimeMillis();
		
		Set<byte[]> set = new HashSet<byte[]>();
		Set<byte[]> contentIDset = new HashSet<byte[]>();
		
			

		
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			set = jedisCluster.zrange(rankingCacheKey.getBytes(), 0,  - 1);
			List<byte[]> alData = jedisCluster.hmget( detailCacheKey.getBytes(), set.toArray(new byte[0][0]));

			int i = 0;
			
				for ( ; i < alData.size(); i++) {
					byte[] bs = (byte[])alData.get(i);
					try {
						ResponParam data = new ResponParam();
						deserializer.deserialize(data, bs);
						if (data != null ) {
							contentIDset.add(data.getInfoid().getBytes());
							System.out.println("cacheIndex:"+i+"contentId:"+data.getInfoid()+",strategy:"+data.getStrategy()+",contentType:"+data.getContentType());
							break;
						}
					}
					catch (TException e) {
						logger.error( " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
					}
			
				}

	}
	/**
	* 补足推荐的资讯
	*  Description:
	*  @author liyuanyi  DateTime 2016年6月7日 下午2:38:21
	*  @param context
	*  @param relist
	*  @param resultList
	*  @param indexList
	*/
	public void addRecommendationData(Context context, Map<Integer, ResponParam> mRequestIndexResponse, Set<String> hsAllResponse, boolean fUseRankingCache) {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		long tsStart = System.currentTimeMillis();
		Map<Integer, ResponParam> mLocalRequestIndexData = prepareRequestIndexDataToBeFilled(mRequestIndexResponse);
		final int channelId          = ContextUtils.getChannelId(context);
		final String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);
		final String detailCacheKey  = KeyUtils.getDetailKeyByUid(context);
		final String offsetKey       = (channelId > 0) ? KeyUtils.subChannelOffsetKey(context, channelId) : KeyUtils.getMainOffset(context);
		if (fUseRankingCache) {
			Set<byte[]> set = new HashSet<byte[]>();
			Set<byte[]> contentIDset = new HashSet<byte[]>();
			if (MapUtils.isNotEmpty(mLocalRequestIndexData)) {
				int neededCount = mLocalRequestIndexData.size();

				if (logger.isDebugEnabled()) {
					logger.debug(strRequestInfo + ", start addRecommendationData with ranking cache");
//					logger.debug(strRequestInfo + ", start addRecommendationData with ranking cache,rankingCacheKey="+rankingCacheKey);
				}
				TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
				set = jedisCluster.zrange(rankingCacheKey.getBytes(), 0, neededCount - 1);
				List<byte[]> alData = jedisCluster.hmget( detailCacheKey.getBytes(), set.toArray(new byte[0][0]));

				int i = 0;
				for (Integer index : mLocalRequestIndexData.keySet()) {
					for ( ; i < alData.size(); i++) {
						byte[] bs = (byte[])alData.get(i);
						try {
							ResponParam data = new ResponParam();
							deserializer.deserialize(data, bs);
							if (data != null && hsAllResponse.add(data.getInfoid())) {
								contentIDset.add(data.getInfoid().getBytes());
								mRequestIndexResponse.put(index, data);
								break;
							}
						}
						catch (TException e) {
							logger.error(strRequestInfo + " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
						}
					}
				}
			}

			prepareReturnData(mRequestIndexResponse);

			final Set<byte[]> setfinal = set;
			threadPoolTaskExecutor.submit(() -> {
				long ttlTime = jedisCluster.ttl(rankingCacheKey);
				if (ttlTime == -1) {
					jedisCluster.expire(rankingCacheKey, 1);
					jedisCluster.expire(offsetKey, 1);
					logger.warn("redis key ttl -1 key is "+ rankingCacheKey +" , uid is "+ context.getUid() + " , and abtestVersion is "+ context.getAbtestVersion() +" " + rankingCacheKey);
				}
				
				if (setfinal != null && setfinal.size() > 0) {
					//删除本次请求channel的缓存
					jedisCluster.zremrangeByRank(rankingCacheKey.getBytes(), 0, setfinal.size() >= 1 ? setfinal.size() - 1 : 0);
				}
				//如果本次请求的为 频道,则删除for u中缓存
				if (channelId > 0) {
					jedisCluster.zrem(KeyUtils.getMainKey(context).getBytes(), contentIDset.toArray(new byte[0][0]));
				}
				//incrBy offset
				jedisCluster.incrBy(offsetKey, mRequestIndexResponse.size());
			});
		} else {
			List<ResponParam> alRanking = context.getResponseParamList();
			List<Integer> categoryIdList = context.getCategoryids();
			int offset = 0;
			if (logger.isDebugEnabled()) {
				logger.debug(strRequestInfo + ", start addRecommendationData without ranking cache alRanking.size=" + alRanking.size() + "\toffset=" + offset);
			}
			for (Integer index : mLocalRequestIndexData.keySet())
			{
				for (; offset < alRanking.size(); offset++)
				{
					ResponParam data = (ResponParam)alRanking.get(offset);
					if (channelId == 0 || CollectionUtils.isEmpty(categoryIdList))
					{
						if (hsAllResponse.add(data.getInfoid()))
						{
							mRequestIndexResponse.put(index, data);
							break;
						}
					}
					else if (categoryIdList.contains(data.getCategoryId()))
					{
						if (hsAllResponse.add(data.getInfoid()))
						{
							mRequestIndexResponse.put(index, data);
							break;
						}
					}
				}
			}
		}
		if (logger.isDebugEnabled()) {
			long tsSpent = System.currentTimeMillis() - tsStart;
			logger.debug("uid is "+ context.getUid() + ", end addRecommendationData time is " + tsSpent +", and result is " + mRequestIndexResponse);
		}
	}

	/**
	* 增加到缓存,增加到redis,同时将结果从context中移除
	* @param context
	* @param resultList
	*/
	public void addToCache(Context context, Collection<ResponParam> resultList) {
		String addToCacheLogTag = "addToCache:" + context.getRequestId();
		int channelId = ContextUtils.getChannelId(context);
		logger.debug(addToCacheLogTag + ", channelId=" + channelId + ", op=" + context.getZhiziListReq().getOperation() + ", context.getResponseParamList.size=" + context.getResponseParamList().size() + ", context.getNum="+context.getNum());
		//缓存的资讯条数 一定要大于 当前需要的资讯条数
		//其实此处会有误杀:例如 getNum==8,推荐过已读之后长度为6,但是有强插和探索资讯4条,其实最终只需要2条，但是我们还是用<=8
		//但实际上，只要不发生reRank context.getResponseParamList().size() 一直是0
		if (context.getResponseParamList().size() <= context.getNum()){
			return;
		}
		//删除本次下发的资讯
		context.getResponseParamList().removeAll(resultList);
		//缓存生效事件，默认600s
		final int rediscacheExpire = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "rediscacheExpire"), 600);
		//拼接key
		final String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);
		final String detailCacheKey = KeyUtils.getDetailKeyByUid(context);
		List<ResponParam> cacheList = context.getResponseParamList();
		logger.debug(addToCacheLogTag + ", redis key: rankingCacheKey=" + rankingCacheKey + ", detailCacheKey=" + detailCacheKey);		
		//使用线程池进行处理:后台处理
		threadPoolTaskExecutor.submit(() -> {
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
			String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "postclass");//open
			String strPipeline = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "pipeLine");
//			if(logger.isDebugEnabled()){ //yezi remove debug
//				strClass="midNightEroticFrequencyControl;sourceRankMixedControl;simHashControl;multiStrategyMixControl";
//			}
			logger.debug(addToCacheLogTag + ", postclass=" + strClass+",strPipeline="+strPipeline);
			logger.debug(addToCacheLogTag +", add cache into redis, input size=" + cacheList.size() + " , spend time=" + (System.currentTimeMillis() - current));
			//如果有simhash重排序,则需要进行排序
			List<ResponParam> simHashList = cacheList;
			if(context.isPipeLineInvokeTimeout()){//是否超时
				logger.debug(addToCacheLogTag +", PipeLine Invoke Timeout !");
			
				if (strClass.contains("simHashControl")) {
					current = System.currentTimeMillis();
	
					int simHashStart = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "simHashStart"), 100);
					int simHashEnd = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "simHashEnd"), 300);
					int simHashDistance = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "simHashDistance"), 28);
					int simHashControlwindowSize = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "simHashControlwindowSize"), 8);
					
					// List <ResponParam> inlist, int start, int end, int windowSize, int simHashDistance, Context context
					simHashList = SimHashControl.reRank(simHashList, simHashStart, simHashEnd, simHashControlwindowSize, simHashDistance, context);
					logger.debug(addToCacheLogTag +", simHash spend time=" + (System.currentTimeMillis() - current) + ", size=" + simHashList.size());
				}
				
				if (strClass.contains("picSimHashControl")) {
					current = System.currentTimeMillis();
					
					int simHashStart = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "pic", "simHashStart"), 100);
					int simHashEnd = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "pic", "simHashEnd"), 300);
					int simHashDistance = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "pic", "simHashDistance"), 28);
					int simHashControlwindowSize = NumberUtils.toInt(context.getComponentConfiguration("rankingCache", "pic", "simHashControlwindowSize"), 8);
					
					// List <ResponParam> inlist, int start, int end, int windowSize, int simHashDistance, Context context
					simHashList = PicSimHashControl.reRank(simHashList, simHashStart, simHashEnd, simHashControlwindowSize, simHashDistance, context);
					logger.debug(addToCacheLogTag +", picSimHash spend time=" + (System.currentTimeMillis() - current) + ", size=" + simHashList.size());
				}
				if(ContextUtils.isForYouChannel(context)){//只针对forU来说
					if(strClass.contains("videoMixControl")){//2018年3月11日 15:12:44  增加视频混插
						current = System.currentTimeMillis();
						
						simHashList = VideoMixControl.reRank(context, simHashList);
						logger.debug(addToCacheLogTag +", TIMEOUT,VideoMixControl spend time=" + (System.currentTimeMillis() - current) + ", size=" + simHashList.size());
					}
					
					if(strClass.contains("multiStrategyMixControl")) {//2018年3月11日 15:12:44  增加gif/memes/...混插
						current = System.currentTimeMillis();
					
						simHashList = MultiStrategyMixControl.reRank(context, simHashList);
						logger.debug(addToCacheLogTag +",TIMEOUT, MultiStrategyMixControl spend time=" + (System.currentTimeMillis() - current) + ", size=" + simHashList.size());
					}
			  }

			}
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
				for (ResponParam responParam : simHashList) {
					i++;
					if (i <= cacheMaxCnt) {
						StringBuffer contentMsg = new StringBuffer();
						contentMsg.append(responParam.getInfoid());
						
						if (context.getZhiziListReq() != null) {
							if (!ContextUtils.isForYouChannel(context)) {
								contentMsg.append("#").append(context.getScenario());
							}
						}
						
						map.put( contentMsg.toString().getBytes(), i);
						detailMap.put(contentMsg.toString().getBytes(), serializer.serialize(responParam));
						list.add(responParam);
					}
				}
				
				//add2EhcheCache(context, list, rankingCacheKey);
				current = System.currentTimeMillis();
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(rankingCacheKey.getBytes(), map);
				jedisCluster.hmset(detailCacheKey.getBytes(), detailMap);
				logger.debug(addToCacheLogTag +", add cache into redis, write size=" + list.size() + " , spend time=" + (System.currentTimeMillis() - current));
			} catch (Exception e) {
				logger.error(addToCacheLogTag + " abtest "+  context.getAbtestVersion() +"=== addToCache error " + e ,e.getCause());
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(rankingCacheKey.getBytes(), map);
				jedisCluster.hmset(detailCacheKey.getBytes(), detailMap);
			} finally {
				current = System.currentTimeMillis();
				jedisCluster.expire(rankingCacheKey.getBytes(), rediscacheExpire);
				jedisCluster.expire(detailCacheKey.getBytes(), rediscacheExpire);
				if (channelId == 0) {
					String rankingCountKey    = StringUtils.join(new String[]{"rankingCount", context.getApp(), String.valueOf(context.getScenario())}, Constants.STR_SPLIT_PRE);
					String rankingCountMember = context.getUid();
					jedisCluster.zadd(rankingCountKey, (double)(current+rediscacheExpire*1000), rankingCountMember);
				}
				logger.debug(addToCacheLogTag +", rankingCacheKey=" + rankingCacheKey + " detailCacheKey=" + detailCacheKey + " finish setExpire " + rediscacheExpire + " sec, spend time is " + (System.currentTimeMillis() - current));
				logger.debug(addToCacheLogTag +", finish all tasks, spend time=" + (System.currentTimeMillis() - begin));
			}
		});
	}
	
	private void add2EhcheCache(Context context, List<ResponParam> list, String redisKey) {
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));
	
		if (!ifL1Cache) {
			return ;
		}
		
		long begin = System.currentTimeMillis();
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
		cache.put(new Element(redisKey, list));
		if (logger.isDebugEnabled()) {
			logger.info("requestId="+ context.getRequestId() +", add2EhcheCache" + list.size() + " , time is " + ( System.currentTimeMillis() - begin ));
		}
	}

	private void addOffset2Ehcache(Context context, String offsetKeyInner, int begin,int size ) {
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));

		if (!ifL1Cache) {
			return ;
		}
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
		int channelId = ContextUtils.getChannelId(context);;
		String offsetKey = (channelId > 0) ? KeyUtils.subChannelOffsetKey(context, channelId) : KeyUtils.getMainOffset(context);
		Element element = new Element(offsetKey, Integer.valueOf(size+begin));
		if (context.getCacheElement() !=null) {
			element.setTimeToLive(TimeUtil.toSecs((context.getCacheElement().getExpirationTime() - System.currentTimeMillis())));
		}
		cache.put(element);
		if (logger.isDebugEnabled()) {
			logger.debug("uid "+ context.getUid() +", addOffset2Ehcache" );
		}
	}
	
	private List<ResponParam> getCacheFromEhcache(String key, int neededCnt, Context context) {		
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));
		if (!ifL1Cache) {
			return Collections.emptyList();
		}
		
		List<ResponParam> resultList;
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
		List<ResponParam> list = context.getCacheResponseParamList();
		
		if (CollectionUtils.isNotEmpty(list)) {
			resultList = new ArrayList<ResponParam>(list.subList(0, neededCnt));
			list.removeAll(resultList);
			Element newElement = new Element(key, list);
			//newElement.setTimeToIdle(oldElement.getTimeToIdle());
			newElement.setTimeToLive(TimeUtil.toSecs((context.getCacheElement().getExpirationTime() - System.currentTimeMillis())));
			cache.put(newElement);
		}else{
			resultList = Collections.emptyList();
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("uid "+ context.getUid() +", getCacheFromEhcache size is " + resultList.size());
		}
		return resultList;
	}
	public static void main(String[] args) {
		try {
		TestAllReturnDataProcessImpl ga = new TestAllReturnDataProcessImpl();
//		getOneUidForUCoreCacheList(args[0]);
		String uid =args[0];
		final String rankingCacheKey = uid+"::noticias::q";
		final String detailCacheKey  =  uid+"::noticias::detail";
		long tsStart = System.currentTimeMillis();
		
		Set<byte[]> set = new HashSet<byte[]>();
		Set<byte[]> contentIDset = new HashSet<byte[]>();
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jj = new JedisCluster(redisSet);
		
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			set = jj.zrange(rankingCacheKey.getBytes(), 0,  - 1);
			List<byte[]> alData = jj.hmget( detailCacheKey.getBytes(), set.toArray(new byte[0][0]));

			int i = 0;
			
				for ( ; i < alData.size(); i++) {
					byte[] bs = (byte[])alData.get(i);
					try {
						ResponParam data = new ResponParam();
						deserializer.deserialize(data, bs);
						if (data != null ) {
							
							System.out.println("cacheIndex:"+i+",contentId:"+data.getInfoid()+",strategy:"+data.getStrategy()+",contentType:"+data.getContentType());
						
						}
					}
					catch (TException e) {
						ga.logger.error( " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
					}
			
				}
		}catch (Exception e) {
			System.out.println( " TException is " + e);
		}
	}
	
	
	
	public static void getForUMixInsertRatio(String[] args) {
		TestAllReturnDataProcessImpl ga = new TestAllReturnDataProcessImpl();
		String uid =args[0];
		if(uid == null){
			return;
		}
		
		Set<byte[]> set = new HashSet<byte[]>();
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);

		JedisCluster jj = new JedisCluster(redisSet);
		String[] uidList = uid.split(",");
		for (String uidd : uidList) {
			long tsStart = System.currentTimeMillis();
			
		
			try{
			
			
				TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
				set = jj.zrange(( uidd+"::noticias::q").getBytes(), 0,  - 1);
				List<byte[]> alData = jj.hmget( ( uidd+"::noticias::detail").getBytes(), set.toArray(new byte[0][0]));

				int i = 0;
				HashMap<Integer,Integer> contentTypeCount  = new HashMap<Integer,Integer>();
				HashMap<Integer,Integer> lastContentTypeIndex  = new HashMap<Integer,Integer>();
				HashMap<Integer,Integer> contentTypePositions  = new HashMap<Integer,Integer>();
				List<Integer> contentTypeNotNewsToPositions  = new ArrayList<Integer>();
				
				List<ResponParam> responParamList  = new ArrayList<ResponParam>();
					for ( ; i < alData.size(); i++) {
						byte[] bs = (byte[])alData.get(i);
						try {
							ResponParam data = new ResponParam();
							deserializer.deserialize(data, bs);
							
							responParamList.add(data);
							if (data != null ) {
								
								if(contentTypeCount.get(data.getContentType()) == null){
									contentTypeCount.put(data.getContentType(),0);
									
								}else{
									contentTypeCount.put(data.getContentType(),contentTypeCount.get(data.getContentType())+1);
								}
								//非news的position list
								if(data.getContentType() != 1){									
									contentTypeNotNewsToPositions.add(i);									
								}
								
								lastContentTypeIndex.put(data.getContentType(),i);
								contentTypePositions.put(i,data.getContentType());
								
								//System.out.println("cacheIndex:"+i+",contentId:"+data.getInfoid()+",strategy:"+data.getStrategy()+",contentType:"+data.getContentType());
							
							}
						}
						catch (TException e) {
							ga.logger.error( " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
						}
				
					}
					StringBuffer uidContentTypeRationInfo  = new StringBuffer();
					uidContentTypeRationInfo.append("uidd:"+uidd);
					int minIndex = 1000;
					for (Entry<Integer, Integer> rate : contentTypeCount.entrySet()) {
						uidContentTypeRationInfo.append("|contentType:"+rate.getKey()+",count:"+rate.getValue()+",lastIndex:"+lastContentTypeIndex.get(rate.getKey()));
						if(minIndex>lastContentTypeIndex.get(rate.getKey())){
							minIndex = lastContentTypeIndex.get(rate.getKey());
						}
						
					}
					
					System.out.println(uidContentTypeRationInfo);
					//contentType :count
					HashMap<Integer,Integer> tempContentTypeCount  = new HashMap<Integer,Integer>();
					//position---》contentType
					for(int k = 0 ; k < contentTypePositions.size(); k++){
//						contentTypePositions.put(i,data.getContentType());	
						if(k <= minIndex){
							if(tempContentTypeCount.get(contentTypePositions.get(k)) == null){
								tempContentTypeCount.put(contentTypePositions.get(k),0);
								
							}else{
								tempContentTypeCount.put(contentTypePositions.get(k),tempContentTypeCount.get(contentTypePositions.get(k))+1);
							}
						}
					}
//					for(int j=0 ;j<minIndex;j++){
//						responParamList.get(j).getContentType();
//						
//					}
					StringBuffer tempUidContentTypeRationInfo  = new StringBuffer();
					tempUidContentTypeRationInfo.append("----||||");
					StringBuffer tempUidContentType  = new StringBuffer();
					StringBuffer tempUidContentTypeCount  = new StringBuffer();
					tempUidContentType.append("contentType:(");
					tempUidContentTypeCount.append("(");
					for (Entry<Integer, Integer> rate : tempContentTypeCount.entrySet()) {
						tempUidContentType.append(rate.getKey()+"/");
						tempUidContentTypeCount.append(rate.getValue()+"/");
						
					}
					tempUidContentTypeCount.append(")");
					tempUidContentType.append(")");
					
					tempUidContentTypeRationInfo.append(tempUidContentType+"="+tempUidContentTypeCount);
					
					
					int tempRatioWrong  = 0;
					//获取缺失比例的问题
					 for (int j = contentTypeNotNewsToPositions.size(); j >1; j--) {
						 if(contentTypeNotNewsToPositions.get(j-1) - contentTypeNotNewsToPositions.get(j-2) <2){//后面的一个 减 前一个的index差值 <2 说明不是按照比例来处的
							 tempRatioWrong ++;
						 }
					}
					 System.out.println(tempUidContentTypeRationInfo+",tempRatioWrongCount="+tempRatioWrong);
				
			}catch(Exception  e){
				System.out.println(e);
				continue;
			}
			
		}
		
		
	}
	public static void getUserDetailRedisInfoByScenario(String[] args) {
		TestAllReturnDataProcessImpl ga = new TestAllReturnDataProcessImpl();
//		getOneUidForUCoreCacheList(args[0]);
		
		String uid =args[0];
		String scenario =args[1];
		System.out.println("args~uid="+uid+",scenario="+scenario);
		String rankingCacheKeyDefault = uid+"::noticias::q";
		boolean isVideoChannel = ContextUtils.isVideoChannel(Long.parseLong(scenario));
		boolean isForUScenario = true;
		
		if(StringUtils.isNotEmpty(scenario)){
			long  keyTempInfo = Long.parseLong(scenario);
			if(isVideoChannel){
				keyTempInfo = ContextUtils.getChannelId(Long.parseLong(scenario));
			}
			//01011802120553335201000177070507::noticias::Spanish::65829::q --- memes scen		
			rankingCacheKeyDefault = uid+"::noticias::Spanish::"+keyTempInfo+"::q";
//			01011805161936515201000261617501::noticias::Spanish::11::q ---video only
			if(scenario != "65792"){
				isForUScenario = false;
			}
		}
		
		
		final String rankingCacheKey = rankingCacheKeyDefault;
		final String detailCacheKey  =  uid+"::noticias::detail";//01011805161936515201000261617501::noticias::detail
		long tsStart = System.currentTimeMillis();
		
		Set<byte[]> set = new HashSet<byte[]>();
		Set<byte[]> contentIDset = new HashSet<byte[]>();
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jj = new JedisCluster(redisSet);
		
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			set = jj.zrange(rankingCacheKey.getBytes(), 0,  - 1);
			List<byte[]> alData = jj.hmget( detailCacheKey.getBytes(), set.toArray(new byte[0][0]));

			int i = 0;
			
				for ( ; i < alData.size(); i++) {
					byte[] bs = (byte[])alData.get(i);
					try {
						ResponParam data = new ResponParam();
						deserializer.deserialize(data, bs);
						if (data != null ) {
							
							System.out.println("cacheIndex:"+i+",contentId:"+data.getInfoid()+",strategy:"+data.getStrategy()+",contentType:"+data.getContentType());
						
						}
					}
					catch (TException e) {
						ga.logger.error( " deserializer.deserialize time is " + (System.currentTimeMillis() - tsStart));
					}
			
				}
	}
	
	
	/***
	 * @date 2018年6月27日 20:29:01
	 * 针对等待池,移除满足探索阈值不需要在探索的有效文章 以及删除在等待池里无效文章
	 * a 验证文章的是否在等待池超时（大于最大等待池时间）
	 * b 验证文章的impression是否已经满足探索阈值移除处理
	 * @author yezi
	 */
	public static void removeExploreWaitingPoolArticles(){
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		//探索-等待池文章的最大等待时间 暂时只考虑news video
//		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;
		
		//完成探索所需要的下发或展示次数
		 int EXPINFO_NEWS_THRESHOLD = 50;
		 int EXPINFO_VIDEO_THRESHOLD = 30;
//		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish";
		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
				        + ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video";
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		
		 String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
		long tsCurrentTime = System.nanoTime();
		
		
		
		try
		{			
			String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(":");//FeederConstants.SEPARATOR_APP_LANGUAGE
				for (String mainLanguage : arrLanguages)
				{
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					
					long startTime = System.nanoTime();

					String expinfoWaitingkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"eagerly") : getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
					String expinfoImpressionInfokey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") : getExpInfoKey(firmApp, mainLanguage, strContentType,"impressionOfInfoIdKey");
				
					
					
//
					Long redisSize = jedisCache.zcard(expinfoWaitingkey);
					System.out.println("\t\t start waiting key=" + expinfoWaitingkey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey);
					//get waiting max time
					long maxWaitingTime = 48*60*60*1000;
					int threshold = EXPINFO_NEWS_THRESHOLD;
					int contentTypeInt = NumberUtils.toInt(strContentType);
					
					if(ContentType.VIDEO.getValue() == contentTypeInt){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_VIDEO;
						threshold = EXPINFO_VIDEO_THRESHOLD;
					}else if(contentTypeInt == 0){//空-->0
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_NEWS;
						threshold = EXPINFO_NEWS_THRESHOLD;
					}
					long currentTime = new Date().getTime();
					long maxwaitingDate = currentTime - maxWaitingTime;
					Set<String> listNeedToDeletedContentData = new HashSet<String>();
					Set<String> listNeedToDeletedContentId = new HashSet<String>();
					//1 get out of date waiting time articles
					System.out.println("\t" + expinfoWaitingkey +" maxwaitingDate = "+ maxwaitingDate);
					Set<Tuple> removeOutOfMaxWaitingTimeTupleSet = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, 0, maxwaitingDate);
					Iterator<Tuple> outOfMaxWaitingTimeIts = removeOutOfMaxWaitingTimeTupleSet.iterator();
					while (outOfMaxWaitingTimeIts.hasNext())
					{
						Tuple tuple = outOfMaxWaitingTimeIts.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						listNeedToDeletedContentId.add(contentId);
						listNeedToDeletedContentData.add(infoid_with_type);	
					}
										
					System.out.println("\t" + expinfoWaitingkey +" listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
					System.out.println("\t" + expinfoWaitingkey + " get OutOfMaxWaitingTimeSet, listNeedToDeletedContentId:" + listNeedToDeletedContentId.size() + " cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					
					
					//2 check article impression remove bigger threshold article	
					System.out.println("\t" + expinfoWaitingkey +" begin check article impression remove bigger threshold article, maxwaitingDate = "+maxwaitingDate+ " currentTime=" + currentTime);
					Set<Tuple> effectiveWaitingtuples = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, maxwaitingDate, currentTime);
					Iterator<Tuple> its = effectiveWaitingtuples.iterator();
					List<String> checkImpressionArticleArr = new ArrayList<String>();
					Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
					Map<String, String> hmContentIdsData = new HashMap<String, String>();
				
					while (its.hasNext())
					{
						Tuple tuple = its.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						checkImpressionArticleArr.add(contentId);
						hmContentIdsData.put(contentId, infoid_with_type);
					}
					
					
					if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){	
						System.out.println("\t" + expinfoWaitingkey +" hmContentIdsData size= "+hmContentIdsData.size() +" checkImpressionArticleArr size= "+checkImpressionArticleArr.size());
						List<String> tempData = jedisCache.hmget(expinfoImpressionInfokey, checkImpressionArticleArr.toArray(new String[0]));
						System.out.println("tempData ="+tempData.toString());
						
						if(CollectionUtils.isNotEmpty(tempData)){//add
							String[] arrContentIdImpression = tempData.toArray(new String[0]);
							System.out.println("arrContentIdImpression size= "+CollectionUtils.size(arrContentIdImpression)+ " arrContentIdImpression=" + StringUtils.join(arrContentIdImpression,","));
							int i = -1;
							for (String article : checkImpressionArticleArr) {
								i++;
								try {
									if(arrContentIdImpression[i] == null ||NumberUtils.toInt(arrContentIdImpression[i]) < threshold){
										System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdImpression[i]);
										continue;
									}else{
										listNeedToDeletedContentData.add(hmContentIdsData.get(article));
										listNeedToDeletedContentId.add(article);
										logParam = new Object[] { EVENT_REMOVE_WAITING_EXPLORE, expinfoWaitingkey, article,
												arrContentIdImpression[i], System.currentTimeMillis() };
										System.out.println("\t  "+expinfoWaitingkey + " remove bigger threshold article="+ StringUtils.join(logParam, LOG_TAG));
									}
									
									hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdImpression[i]));
								} catch (Exception e) {
									hmContentIdsImpression.put(hmContentIdsData.get(article),0);
									System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
								}
							}
							System.out.println("\t  END");	
							
						}
		
					  
					}
					
					if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
						
						System.out.println("\t listNeedToDeletedContentData size="+listNeedToDeletedContentData.size() + " listNeedToDeletedContentData="+listNeedToDeletedContentData.size());
						System.out.println("\t" + expinfoWaitingkey +"  removeBatchArticlesFromWaitingPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentData, LOG_TAG));
						System.out.println("\t" + expinfoImpressionInfokey +"  removeBatchArticlesFromImpressionInfoPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
						
						System.out.println("\t\t removeBatchArticlesFromWaitingPool need to remove contentId size=" + listNeedToDeletedContentData.size() + "\t exploreKey=" + expinfoWaitingkey );
//						StringUtils.join(listNeedToDeletedContentData , " ");
						System.out.println("\t\t hdel  "+expinfoWaitingkey+" "+StringUtils.join(listNeedToDeletedContentData , " "));
						jedisCache.zrem(expinfoWaitingkey,listNeedToDeletedContentData.toArray(new String[0]) );//
						System.out.println("\t\tremoveBatchArticlesFromImpressionInfoPool need to remove contentId=" + listNeedToDeletedContentId.size()   + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey);
						jedisCache.hdel(expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));//
						
//						removeBatchArticlesFromWaitingPool( expinfoWaitingkey, listNeedToDeletedContentData.toArray(new String[0]));
//						removeBatchArticlesFromImpressionInfoPool( expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));								
					}
					
					
					System.out.println("\t "+ expinfoWaitingkey + " = get OutOfMaxWaitingTimeTupleSet cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					
					System.out.println("\t");
					System.out.println("\t");
					System.out.println("\t");
					System.out.println("\t  redis-cli -h 10.10.100.100 -p 6300 -c zrangebyscore " + expinfoWaitingkey +" 0 "+ maxwaitingDate);
					System.out.println("\t  redis-cli -h 10.10.100.100 -p 6300 -c zrangebyscore " + expinfoWaitingkey  +" "+  maxwaitingDate +" "+ currentTime);
					System.out.println("\t-----End---"+expinfoWaitingkey);
				
				}
			}
				
		} catch (Exception e) {
			System.out.println("\t\t removeExploreWaitingPoolArticles: " + e);
		}
		
		System.out.println("\t\t finish removeExploreWaitingPoolArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	}
	
	public static void removeTest(){
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		//探索-等待池文章的最大等待时间 暂时只考虑news video
//		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		long EXPINFO_WAITING_MAX_TIME_NEWS  = 60*60*1000;
		  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;
		
		//完成探索所需要的下发或展示次数
		 int EXPINFO_NEWS_THRESHOLD = 50;
		 int EXPINFO_VIDEO_THRESHOLD = 30;
		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish";
//		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
//				        + ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video";
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		
		 String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
		long tsCurrentTime = System.nanoTime();
		
		
		try
		{			
			String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(":");
				for (String mainLanguage : arrLanguages)
				{
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					
					long startTime = System.nanoTime();

					String expinfoWaitingkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"eagerly") : getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
					String expinfoImpressionInfokey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") : getExpInfoKey(firmApp, mainLanguage, strContentType,"impressionOfInfoIdKey");
				
//
					Long redisSize = jedisCache.zcard(expinfoWaitingkey);
					System.out.println("\t\t start waiting key=" + expinfoWaitingkey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey);
					//get waiting max time
					long maxWaitingTime = 48*60*60*1000;
					int threshold = EXPINFO_NEWS_THRESHOLD;
					int contentTypeInt = NumberUtils.toInt(strContentType);
					
					if(ContentType.VIDEO.getValue() == contentTypeInt){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_VIDEO;
						threshold = EXPINFO_VIDEO_THRESHOLD;
					}else if(contentTypeInt == 0){//空-->0
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_NEWS;
						threshold = EXPINFO_NEWS_THRESHOLD;
					}
					long currentTime = new Date().getTime();
					long maxwaitingDate = currentTime - maxWaitingTime;
					Set<String> listNeedToDeletedContentData = new HashSet<String>();
					Set<String> listNeedToDeletedContentId = new HashSet<String>();
					//1 get out of date waiting time articles
					Set<Tuple> removeOutOfMaxWaitingTimeTupleSet = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, 0, maxwaitingDate);
					Iterator<Tuple> utOfMaxWaitingTimeIts = removeOutOfMaxWaitingTimeTupleSet.iterator();
					while (utOfMaxWaitingTimeIts.hasNext())
					{
						Tuple tuple = utOfMaxWaitingTimeIts.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						listNeedToDeletedContentId.add(contentId);
						listNeedToDeletedContentData.add(infoid_with_type);	
					}
										
					System.out.println("\t" + expinfoWaitingkey + "get OutOfMaxWaitingTimeSet, listNeedToDeletedContentId:" + listNeedToDeletedContentId.size() + " cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					
					
					//2 check article impression remove bigger threshold article					
					Set<Tuple> effectiveWaitingtuples = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, maxwaitingDate, currentTime);
					Iterator<Tuple> its = effectiveWaitingtuples.iterator();
					List<String> checkImpressionArticleArr = new ArrayList<String>();
					Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
					Map<String, String> hmContentIdsData = new HashMap<String, String>();
				
					while (its.hasNext())
					{
						Tuple tuple = its.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						checkImpressionArticleArr.add(contentId);
						hmContentIdsData.put(contentId, infoid_with_type);
					}
					
					if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){						
						String[] arrContentIdImpression = jedisCache.hmget(expinfoImpressionInfokey, checkImpressionArticleArr.toArray(new String[0])).toArray(new String[0]);
						int i = -1;
						for (String article : checkImpressionArticleArr) {
							i++;
							try {
								if(arrContentIdImpression[i] == null || NumberUtils.toInt(arrContentIdImpression[i]) < threshold){
									System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdImpression[i]);
									continue;
								}else{
									listNeedToDeletedContentData.add(hmContentIdsData.get(article));
									listNeedToDeletedContentId.add(article);
									logParam = new Object[] { EVENT_REMOVE_WAITING_EXPLORE, expinfoWaitingkey, article,
											arrContentIdImpression[i], System.currentTimeMillis() };
									System.out.println("\t  "+expinfoWaitingkey + " remove bigger threshold article="+ StringUtils.join(logParam, LOG_TAG));
								}
								
								hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdImpression[i]));
							} catch (Exception e) {
								hmContentIdsImpression.put(hmContentIdsData.get(article),0);
								System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
							}
						}
					  
					}
					
					if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
						System.out.println("\t listNeedToDeletedContentData size="+listNeedToDeletedContentData.size() + " listNeedToDeletedContentData="+listNeedToDeletedContentData.size());
						System.out.println("\t" + expinfoWaitingkey +"  removeBatchArticlesFromWaitingPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentData, LOG_TAG));
						System.out.println("\t" + expinfoImpressionInfokey +"  removeBatchArticlesFromImpressionInfoPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
						
						System.out.println("\t\t removeBatchArticlesFromWaitingPool need to remove contentId size=" + listNeedToDeletedContentData.size() + "\t exploreKey=" + expinfoWaitingkey );
//						StringUtils.join(listNeedToDeletedContentData , " ");
						System.out.println("\t\t hdel  "+expinfoWaitingkey+" "+StringUtils.join(listNeedToDeletedContentData , " "));
						jedisCache.zrem(expinfoWaitingkey,listNeedToDeletedContentData.toArray(new String[0]) );//
						System.out.println("\t\tremoveBatchArticlesFromImpressionInfoPool need to remove contentId=" + listNeedToDeletedContentId.size()   + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey);
						jedisCache.hdel(expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));//
						
					}
					
					
					System.out.println("\t "+ expinfoWaitingkey + " = get OutOfMaxWaitingTimeTupleSet cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					
				
				}
			}
				
		} catch (Exception e) {
			System.out.println("\t\t removeExploreWaitingPoolArticles: " + e);
		}
		
		System.out.println("\t\t finish removeExploreWaitingPoolArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	
	}
	
	
	public static void removeExplorePoolVaildArticles(){
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		//探索-等待池文章的最大等待时间 暂时只考虑news video
//		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		long EXPINFO_WAITING_MAX_TIME_NEWS  = 60*60*1000;
		  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;
		
		//完成探索所需要的下发或展示次数
		 int EXPINFO_NEWS_THRESHOLD = 50;
		 int EXPINFO_VIDEO_THRESHOLD = 30;
		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticias=Spanish_video";
//		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
//				        + ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video";
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		
		 String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
		long tsCurrentTime = System.nanoTime();
		try
		{
		String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
		for (String strAppLanguage : arrAppLanaguage)
		{
			String[] tmp = strAppLanguage.split("=");
			if (tmp.length < 2)
				continue;
			String firmApp = tmp[0];
			String[] arrLanguages = tmp[1].split(":");
			for (String mainLanguage : arrLanguages)
			{
				String strContentType = null;
				if (mainLanguage.indexOf("_") >= 0) {
					String[] s = mainLanguage.split("_");
					mainLanguage = s[0];
					strContentType = s[1];
				}
				
				long startTime = System.nanoTime();

					String expinfokey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage) : getExpInfoKey(firmApp, mainLanguage, strContentType);
					String expinfoImpressionInfokey = getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					
					Long redisSize = jedisCache.zcard(expinfokey);
					System.out.println("\t ---start expinfo origin key=" + expinfokey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey+" strContentType="+strContentType);
					//get waiting max time
					int threshold = EXPINFO_NEWS_THRESHOLD;
					int contentTypeInt = NumberUtils.toInt(strContentType);
					System.out.println("\t ---contentTypeInt=" +contentTypeInt+ " ContentType.VIDEO.getValue()="+ContentType.VIDEO.getValue());
					
					if(ContentType.VIDEO.getValue() == contentTypeInt){
						threshold = EXPINFO_VIDEO_THRESHOLD;
					}else if(contentTypeInt == 0){//空-->0
						threshold = EXPINFO_NEWS_THRESHOLD;
					}
					System.out.println("\t ---contentTypeInt=" +contentTypeInt+ " ContentType.VIDEO.getValue()="+ContentType.VIDEO.getValue()+ " threshold="+threshold);
					removeBiggerThresholdVaildArticles(expinfokey,expinfoImpressionInfokey,threshold,null);
					System.out.println("\t\t ");
				}
			}
		} catch (Exception e) {
			System.out.println("\t\t removeExplorePoolVaildArticles: " + e);
		}
		
		System.out.println("\t\t finish removeExplorePoolVaildArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	}
	
	
	private static void removeBiggerThresholdVaildArticles(String exploreKey,String expinfoImpressionInfokey,int threshold,Set<Tuple> effectiveExpTuples){
		long startTime = System.nanoTime();
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		if(CollectionUtils.isEmpty(effectiveExpTuples)){
			
			effectiveExpTuples = jedisCache.zrangeWithScores(exploreKey, 0, -1);
		}
		Iterator<Tuple> its = effectiveExpTuples.iterator();
		List<String> checkImpressionArticleArr = new ArrayList<String>();
		Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
		Map<String, String> hmContentIdsData = new HashMap<String, String>();
		Set<String> listNeedToDeletedContentData = new HashSet<String>();
		Set<String> listNeedToDeletedContentId = new HashSet<String>();
		
		while (its.hasNext())
		{
			Tuple tuple = its.next();
			String infoid_with_type = tuple.getElement();
			String[] s = infoid_with_type.split("#");
			String contentId = s[0];
			checkImpressionArticleArr.add(contentId);
			hmContentIdsData.put(contentId, infoid_with_type);
		}
		
		if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){						
			String[] arrContentIdImpression = jedisCache.hmget(expinfoImpressionInfokey, checkImpressionArticleArr.toArray(new String[0])).toArray(new String[0]);
			int i = -1;
			for (String article : checkImpressionArticleArr) {
				i++;
				try {
					if(arrContentIdImpression[i] == null || NumberUtils.toInt(arrContentIdImpression[i]) < threshold){
						System.out.println("\t "+exploreKey+", get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdImpression[i]);
						continue;
					}else{
						listNeedToDeletedContentData.add(hmContentIdsData.get(article));
						listNeedToDeletedContentId.add(article);
						logParam = new Object[] { "removeBiggerThresholdVaildArticles", exploreKey, article,
								arrContentIdImpression[i], System.currentTimeMillis() };
						System.out.println("\t  "+exploreKey + ", remove bigger threshold article,"+ StringUtils.join(logParam, LOG_TAG));
					}
					
					hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdImpression[i]));
				} catch (Exception e) {
					hmContentIdsImpression.put(hmContentIdsData.get(article),0);
					System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
				}
			}
		  
		}
		
		if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
			System.out.println("\t listNeedToDeletedContentData size="+listNeedToDeletedContentData.size() + " listNeedToDeletedContentData="+listNeedToDeletedContentData.size());
			System.out.println("\t" + exploreKey +"  removeBatchArticlesFromWaitingPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentData, LOG_TAG));
			System.out.println("\t" + expinfoImpressionInfokey +"  removeBatchArticlesFromImpressionInfoPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
			
			System.out.println("\t\t removeBatchArticlesFromWaitingPool need to remove contentId size=" + listNeedToDeletedContentData.size() + "\t exploreKey=" + exploreKey );			
			long remexploreKey = jedisCache.zrem(exploreKey,listNeedToDeletedContentData.toArray(new String[0]) );//
			System.out.println("\t\t zrem  "+exploreKey+" "+StringUtils.join(listNeedToDeletedContentData , " ")+" ,remexplore size ="+remexploreKey);
			System.out.println("\t\tremoveBatchArticlesFromImpressionInfoPool need to remove contentId=" + listNeedToDeletedContentId.size()   + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey);
			remexploreKey=jedisCache.hdel(expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));////			removeBatchArticlesFromExpPool(exploreKey, listNeedToDeletedContentData.toArray(new String[0]));
			System.out.println("\t\t hdel  "+expinfoImpressionInfokey+" "+StringUtils.join(listNeedToDeletedContentId , " ")+" ,remexplore size ="+remexploreKey);
		}
		
		
		System.out.println("\t "+ exploreKey + " ,removeBiggerThresholdVaildArticles listNeedToDeletedContentId.size:"+listNeedToDeletedContentId.size()+", cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
		
	}
	
	
	
	public static void removeExploringPoolVaildArticles(){
		
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		//探索-等待池文章的最大等待时间 暂时只考虑news video
//		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		long EXPINFO_WAITING_MAX_TIME_NEWS  = 60*60*1000;
		  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;
		
		//完成探索所需要的下发或展示次数
		 int EXPINFO_NEWS_THRESHOLD = 50;
		 int EXPINFO_VIDEO_THRESHOLD = 30;
		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish";
//		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
//				        + ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video";
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		
		 String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
		long tsCurrentTime = System.nanoTime();
		try
		{
		String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
		for (String strAppLanguage : arrAppLanaguage)
		{
			String[] tmp = strAppLanguage.split("=");
			if (tmp.length < 2)
				continue;
			String firmApp = tmp[0];
			String[] arrLanguages = tmp[1].split(":");
			for (String mainLanguage : arrLanguages)
			{
				String strContentType = null;
				if (mainLanguage.indexOf("_") >= 0) {
					String[] s = mainLanguage.split("_");
					mainLanguage = s[0];
					strContentType = s[1];
				}
				long startTime = System.nanoTime();
				
				String expinfoExploringkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"ing") : getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
				String expinfoImpressionInfokey = getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;

				String expinfokey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage) : getExpInfoKey(firmApp, mainLanguage, strContentType);
				
				Long redisSize = jedisCache.zcard(expinfokey);
				System.out.println("\t ---start expinfo origin key=" + expinfokey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey);
				//get waiting max time
				int threshold = EXPINFO_NEWS_THRESHOLD;
				int contentTypeInt = NumberUtils.toInt(strContentType);
				
				if(ContentType.VIDEO.getValue() == contentTypeInt){
					threshold = EXPINFO_VIDEO_THRESHOLD;
				}else if(contentTypeInt == 0){//空-->0
					threshold = EXPINFO_NEWS_THRESHOLD;
				}
					
	
					removeBiggerThresholdVaildArticles(expinfoExploringkey,expinfoImpressionInfokey,threshold,null);
					System.out.println("\t\t ");
				}
			}
		} catch (Exception e) {
			System.out.println("\t\t removeExploringPoolVaildArticles: " + e);
		}
		
		System.out.println("\t\t finish removeExploringPoolVaildArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	}
	
	
	
	public static void getWaitingArticles(){
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		//探索-等待池文章的最大等待时间 暂时只考虑news video
//		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
		  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;
		
		//完成探索所需要的下发或展示次数
		 int EXPINFO_NEWS_THRESHOLD = 50;
		 int EXPINFO_VIDEO_THRESHOLD = 30;
		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticias=Spanish_video";
//		 String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
//				        + ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video";
		 Object[] logParam = null;
		 String LOG_TAG = "&";
		
		 String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
		long tsCurrentTime = System.nanoTime();
		
		
		
		try
		{			
			String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(":");//FeederConstants.SEPARATOR_APP_LANGUAGE
				for (String mainLanguage : arrLanguages)
				{
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					
					long startTime = System.nanoTime();

					String expinfoWaitingkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"eagerly") : getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
					String expinfoImpressionInfokey = //(strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") : getExpInfoKey(firmApp, mainLanguage, strContentType,"impressionOfInfoIdKey");
					getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					
					
//
					Long redisSize = jedisCache.zcard(expinfoWaitingkey);
					System.out.println("\t\t start waiting key=" + expinfoWaitingkey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey);
					//get waiting max time
					long maxWaitingTime = 48*60*60*1000;
					int threshold = EXPINFO_NEWS_THRESHOLD;
					int contentTypeInt = NumberUtils.toInt(strContentType);
					
//					if(ContentType.VIDEO.getValue() == contentTypeInt){
//						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_VIDEO;
//						threshold = EXPINFO_VIDEO_THRESHOLD;
//					}else if(contentTypeInt == 0){//空-->0
//						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_NEWS;
//						threshold = EXPINFO_NEWS_THRESHOLD;
//					}
					
					if(strContentType!=null && strContentType.equalsIgnoreCase("video")){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_VIDEO;
						threshold = EXPINFO_VIDEO_THRESHOLD;
					}else{
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_NEWS;
						threshold = EXPINFO_NEWS_THRESHOLD;
					}
					
					System.out.println("\t ---contentTypeInt=" +contentTypeInt+" strContentType="+strContentType+ " ContentType.VIDEO.getValue()="+ContentType.VIDEO.getValue()+ " threshold="+threshold);
					long currentTime = new Date().getTime();
					long maxwaitingDate = currentTime - maxWaitingTime;
					Set<String> listNeedToDeletedContentData = new HashSet<String>();
					Set<String> listNeedToDeletedContentId = new HashSet<String>();
//					//1 get out of date waiting time articles
//					System.out.println("\t" + expinfoWaitingkey +" maxwaitingDate = "+ maxwaitingDate);
//					Set<Tuple> removeOutOfMaxWaitingTimeTupleSet = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, 0, maxwaitingDate);
//					Iterator<Tuple> outOfMaxWaitingTimeIts = removeOutOfMaxWaitingTimeTupleSet.iterator();
//					while (outOfMaxWaitingTimeIts.hasNext())
//					{
//						Tuple tuple = outOfMaxWaitingTimeIts.next();
//						String infoid_with_type = tuple.getElement();
//						String[] s = infoid_with_type.split("#");
//						String contentId = s[0];
//						listNeedToDeletedContentId.add(contentId);
//						listNeedToDeletedContentData.add(infoid_with_type);	
//					}
//										
//					System.out.println("\t" + expinfoWaitingkey +" listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
//					System.out.println("\t" + expinfoWaitingkey + " get OutOfMaxWaitingTimeSet, listNeedToDeletedContentId:" + listNeedToDeletedContentId.size() + " cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
//					
					
					//2 check article impression remove bigger threshold article	
					System.out.println("\t" + expinfoWaitingkey +" begin check article impression remove bigger threshold article, maxwaitingDate = "+maxwaitingDate+ " currentTime=" + currentTime);
					Set<Tuple> effectiveWaitingtuples = jedisCache.zrangeWithScores(expinfoWaitingkey, 0, 400- 1);//jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, maxwaitingDate, currentTime);
					Iterator<Tuple> its = effectiveWaitingtuples.iterator();
					List<String> checkImpressionArticleArr = new ArrayList<String>();
					Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
					Map<String, String> hmContentIdsData = new HashMap<String, String>();
					Map<String, Double> hmContentIdsWaitData = new HashMap<String, Double>();
				
					while (its.hasNext())
					{
						Tuple tuple = its.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						checkImpressionArticleArr.add(contentId);
						hmContentIdsData.put(contentId, infoid_with_type);
						hmContentIdsWaitData.put(contentId, tuple.getScore());
					}
					
					
					if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){	
						System.out.println("\t" + expinfoWaitingkey +" hmContentIdsData size= "+hmContentIdsData.size() +" checkImpressionArticleArr size= "+checkImpressionArticleArr.size());
						List<String> tempData = jedisCache.hmget(expinfoImpressionInfokey, checkImpressionArticleArr.toArray(new String[0]));
						System.out.println("tempData ="+tempData.toString());
						
						if(CollectionUtils.isNotEmpty(tempData)){//add
							String[] arrContentIdImpression = tempData.toArray(new String[0]);
							System.out.println("arrContentIdImpression size= "+CollectionUtils.size(arrContentIdImpression)+ " arrContentIdImpression=" + StringUtils.join(arrContentIdImpression,","));
							int i = -1;
							for (String article : checkImpressionArticleArr) {
								i++;
								try {
									SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
									 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
									if(arrContentIdImpression[i] == null ||NumberUtils.toInt(arrContentIdImpression[i]) < threshold){
										System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdImpression[i]+" time "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
										continue;
									}else{
										listNeedToDeletedContentData.add(hmContentIdsData.get(article));
										listNeedToDeletedContentId.add(article);
										logParam = new Object[] { EVENT_REMOVE_WAITING_EXPLORE, expinfoWaitingkey, article,bjTime,
												arrContentIdImpression[i], System.currentTimeMillis() };
										System.out.println("\t  "+expinfoWaitingkey + " remove bigger threshold article="+ StringUtils.join(logParam, LOG_TAG));
									}
									
									hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdImpression[i]));
								} catch (Exception e) {
									hmContentIdsImpression.put(hmContentIdsData.get(article),0);
									System.out.println("\t  get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
								}
							}
							System.out.println("\t  END");	
							
						}
		
					  
					}
					
					if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
						
						System.out.println("\t listNeedToDeletedContentData size="+listNeedToDeletedContentData.size() + " listNeedToDeletedContentData="+listNeedToDeletedContentData.size());
						System.out.println("\t" + expinfoWaitingkey +"  removeBatchArticlesFromWaitingPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentData, LOG_TAG));
						System.out.println("\t" + expinfoImpressionInfokey +"  removeBatchArticlesFromImpressionInfoPool listNeedToDeletedContent ="+StringUtils.join(listNeedToDeletedContentId, LOG_TAG));
						
						System.out.println("\t\t removeBatchArticlesFromWaitingPool need to remove contentId size=" + listNeedToDeletedContentData.size() + "\t exploreKey=" + expinfoWaitingkey );
//						System.out.println("\t\t hdel  "+expinfoWaitingkey+" "+StringUtils.join(listNeedToDeletedContentData , " "));
//						jedisCache.zrem(expinfoWaitingkey,listNeedToDeletedContentData.toArray(new String[0]) );//
//						System.out.println("\t\tremoveBatchArticlesFromImpressionInfoPool need to remove contentId=" + listNeedToDeletedContentId.size()   + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey);
//						jedisCache.hdel(expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));//
//						
					}
					
					System.out.println("\t "+ expinfoWaitingkey + " = get OutOfMaxWaitingTimeTupleSet cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					
					System.out.println("\t");
					System.out.println("\t");
					System.out.println("\t");
					System.out.println("\t  redis-cli -h 10.10.100.100 -p 6300 -c zrangebyscore " + expinfoWaitingkey +" 0 "+ maxwaitingDate);
					System.out.println("\t  redis-cli -h 10.10.100.100 -p 6300 -c zrangebyscore " + expinfoWaitingkey  +" "+  maxwaitingDate +" "+ currentTime);
					System.out.println("\t-----End---"+expinfoWaitingkey);
				
				}
			}
				
		} catch (Exception e) {
			System.out.println("\t\t removeExploreWaitingPoolArticles: " + e);
		}
		
		System.out.println("\t\t finish removeExploreWaitingPoolArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	}
	
	
	
	
//	private static void removeBatchArticlesFromWaitingPool(String exploreInfoWaitingKey,String[] infoWithTypeList) {
//		
//		System.out.println("\t\t removeBatchArticlesFromWaitingPool need to remove contentId size=" + infoWithTypeList.length + "\texploreKey=" + exploreInfoWaitingKey );
//		jedisCache.hdel(exploreInfoWaitingKey, infoWithTypeList);
////		logProcessedArticle(FeederConstants.LOG_EVENT_OUT_OF_DATE_WAITING_EXPINFO, infoWithTypeList.toString());
//	}
//	
//	private static void removeBatchArticlesFromImpressionInfoPool(String expinfoImpressionInfokey,String[] contentIds) {
//		
//		System.out.println("\t\tremoveBatchArticlesFromImpressionInfoPool need to remove contentId=" + contentIds.length  + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey);
//		jedisCache.zrem(expinfoImpressionInfokey, contentIds);
////		logProcessedArticle(FeederConstants.LOG_EVENT_OUT_OF_DATE_WAITING_EXPINFO, contentIds.toString());
//	}
	
	public static String getExpInfoKey(String productId, String language, Object... args)
	{
		Object[] values = new Object[]{};
		values = ArrayUtils.add(values, "expinfo");
		values = ArrayUtils.add(values, productId);
		values = ArrayUtils.add(values, language);
		if (ArrayUtils.isNotEmpty(args))
		{
			for (int i = 0; i < args.length; i++)
				values = ArrayUtils.add(values, args[i]);
		}
		return StringUtils.join(values, "_");
	}
	
	
	
	
    /**
     * @date 2018年7月4日 18:37:52
     * 针对操作的探索key进行阈值判断 超过阈值的直接移除处理
     * @param exploreKey 要操作的探索key
     * @param expinfoImpressionInfokey 探索impression key
     * @param expinfoAllImpressionInfokey -1的impression 暂时不做处理 只是跟现有的阈值判断逻辑做一并的移除操作
     * @param threshold 阈值
     * @param effectiveExpTuples 根据探索key得到的数据
     * @author yezi
     */
	private static void removeBiggerThresholdVaildArticles(String exploreKey,String expinfoImpressionInfokey,String expinfoAllImpressionInfokey,int threshold,Set<Tuple> effectiveExpTuples){
		long startTime = System.nanoTime();
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticias=Spanish_video";
		if(CollectionUtils.isEmpty(effectiveExpTuples)){
			effectiveExpTuples = jedisCache.zrangeWithScores(exploreKey, 0, -1);
		}
		Iterator<Tuple> its = effectiveExpTuples.iterator();
		List<String> checkImpressionArticleArr = new ArrayList<String>();
		Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
		Map<String, String> hmContentIdsData = new HashMap<String, String>();
		Set<String> listNeedToDeletedContentData = new HashSet<String>();
		Set<String> listNeedToDeletedContentId = new HashSet<String>();
		Map<String, Double> hmContentIdsWaitData = new HashMap<String, Double>();
		
		while (its.hasNext())
		{
			Tuple tuple = its.next();
			String infoid_with_type = tuple.getElement();
			String[] s = infoid_with_type.split("#");
			String contentId = s[0];
			checkImpressionArticleArr.add(contentId);
			hmContentIdsData.put(contentId, infoid_with_type);
			hmContentIdsWaitData.put(contentId, tuple.getScore());
		}
		//获取文章在-1impression中的impression count 如果达到阈值 添加到删除队列中
		if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){	
			String[] arrContentIdAllImpression = jedisCache.hmget(expinfoAllImpressionInfokey, checkImpressionArticleArr.toArray(new String[0])).toArray(new String[0]);			
			
			int i = -1;
			for (String article : checkImpressionArticleArr) {
				i++;
				try {
					
					 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
					 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
					if(arrContentIdAllImpression[i] == null || NumberUtils.toInt(arrContentIdAllImpression[i]) < threshold){
						System.out.println("\t "+exploreKey+",  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdAllImpression[i]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
						continue;
					}else{
						listNeedToDeletedContentData.add(hmContentIdsData.get(article));
						listNeedToDeletedContentId.add(article);
						Object logParam = new Object[] { "remove--new", exploreKey, article,bjTime,
								arrContentIdAllImpression[i], System.currentTimeMillis() };
						System.out.println("\t  "+exploreKey + ", remove bigger threshold article,"+ StringUtils.join(logParam, "#"));
					}
					
					hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdAllImpression[i]));
				} catch (Exception e) {
					hmContentIdsImpression.put(hmContentIdsData.get(article),0);
					System.out.println("\t "+exploreKey + ", get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
				}
			}
		}
		//将删除队列的文章 移除expinfokey 、移除在每个产品中的impression、-1impression
		if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
			//a 将删除队列的文章 在 expinfokey移除
//			removeBatchArticlesFromExpPool(exploreKey, listNeedToDeletedContentData.toArray(new String[0]));
			System.out.println("jedisCache.hdel(exploreKey, listNeedToDeletedContentData.toArray(new String[0]));");
			//jedisCache.hdel(exploreKey, listNeedToDeletedContentData.toArray(new String[0]));//
			
			// listNeedToDeletedContentData in apps impression count:
			//b 将删除队列的文章 在每个产品中的impression移除
			System.out.println("\t remove  listNeedToDeletedContentData in apps");
			
			String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(":");
				for (String mainLanguage : arrLanguages)
				{				
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
					
					}
					String expinfoAppImpressionInfokey =getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;// FeederConstants.getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					//
					String[] arrContentIdAppImpression = jedisCache.hmget(expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0])).toArray(new String[0]);
					int j = -1;
					for (String article : listNeedToDeletedContentId) {
						j++;
						try {
							
							 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
							 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
							 System.out.println("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey+",  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdAppImpression[j]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
							
						} catch (Exception e) {							
							System.out.println("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey + ", get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
						}
					}
					
					//
					
					//removeBatchArticlesFromImpressionInfoPool( expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));
					System.out.println( "expinfoAppImpressionInfokey="+expinfoAppImpressionInfokey+",listNeedToDeletedContentId="+ listNeedToDeletedContentId.toArray(new String[0]));
//					jedisCache.hdel(expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));
				}
			}
			//b 将删除队列的文章 在-1impression移除
//			removeBatchArticlesFromImpressionInfoPool( expinfoAllImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));	
			System.out.println( "expinfoAllImpressionInfokey="+expinfoAllImpressionInfokey+",listNeedToDeletedContentId="+ listNeedToDeletedContentId.toArray(new String[0]));
		}
		
		
		System.out.println("\t "+ exploreKey + " ,removeBiggerThresholdVaildArticles listNeedToDeletedContentId.size:"+listNeedToDeletedContentId.size()+", cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
		
	}
	
	
	
	private static void removeBatchArticlesFromImpressionInfoPool(String  expinfoImpressionInfokey,String[] contentIds) {		
//		long result = jedisCache.hdel(expinfoImpressionInfokey, contentIds);
//		expinfo_remove_logger.info("\t\t need to remove contentId length=" + contentIds.length  + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey+"\tresult="+result);
//		logProcessedArticle("remove-vaild-article-from-"+expinfoImpressionInfokey, StringUtils.join(contentIds,","));
	}
	
	
	
	public  static void removeBiggerThresholdVaildArticles(){
		long startTime = System.nanoTime();
		Map<String, Double> hmContentIdsWaitData = new HashMap<String, Double>();
		Map<String, String> hmContentIdsData = new HashMap<String, String>();
		Map<String, String> hmContentIdsForExplorePoolData = new HashMap<String, String>();
		Set<String> listNeedToDeletedContentData = new HashSet<String>();
		Set<String> listNeedToDeletedContentId = new HashSet<String>();
//		Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
		Object[] logParam = null;
		 
		Set<HostAndPort> redisSet= new HashSet<HostAndPort>();
		HostAndPort redisHost = new	HostAndPort("10.10.100.100",6300);
		redisSet.add(redisHost);
		JedisCluster jedisCache = new JedisCluster(redisSet);
		String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = "noticias=Spanish,noticias=Spanish_video";
		String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
		for (String strAppLanguage : arrAppLanaguage)
		{
			String[] tmp = strAppLanguage.split("=");
			if (tmp.length < 2)
				continue;
			String firmApp = tmp[0];
			String[] arrLanguages = tmp[1].split(":");
			for (String mainLanguage : arrLanguages)
			{		String strContentType = null;		
				if (mainLanguage.indexOf("_") >= 0) {
					String[] s = mainLanguage.split("_");
					mainLanguage = s[0];				
			
					strContentType = s[1];
				}
				String expinfoAllImpressionInfokey = getExpInfoKey("-1", mainLanguage,"impressionOfInfoIdKey") ;
				
				// 其他各个产品 等待池 探索池 进行池					
//				String exploreKey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage) : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType);
//				String expinfoExploringkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"ing") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
//				String expinfoWaitingkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"eagerly") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
//				String expinfoImpressionInfokey = getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
				String expinfoExploringkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"ing") : getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
				String exploreKey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage) : getExpInfoKey(firmApp, mainLanguage, strContentType);
				String expinfoWaitingkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"eagerly") : getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
			
				int threshold = 50;
			
				Set<Tuple> effectiveExpTuples = jedisCache.zrangeWithScores(exploreKey, 0, -1);
				Set<Tuple> effectiveExploringTuples = jedisCache.zrangeWithScores(expinfoExploringkey, 0, -1);
				Set<Tuple> effectiveWaitingTuples = jedisCache.zrangeWithScores(expinfoWaitingkey, 0, -1);
				
				if(strContentType != null && strContentType.equalsIgnoreCase("video")){
					threshold = 30;
				}else if(strContentType != null && strContentType.equalsIgnoreCase("gif")){
					threshold = 100;
				}else if(strContentType != null && strContentType.equalsIgnoreCase("meme")){
					threshold = 100;
				}else{
					threshold = 50;
				}
				
				
				Set<Tuple> effectiveTuples = new HashSet<Tuple> ();
				effectiveTuples.addAll(effectiveExpTuples);
				effectiveTuples.addAll(effectiveExploringTuples);
				effectiveTuples.addAll(effectiveWaitingTuples);
				
				
				Iterator<Tuple> its = effectiveTuples.iterator();
				List<String> checkImpressionArticleArr = new ArrayList<String>();
//				Map<String, Double> hmContentIdsWaitData = new HashMap<String, Double>();
			
	
				while (its.hasNext())
				{
					Tuple tuple = its.next();
				
					String infoid_with_type = tuple.getElement();
					String[] s = infoid_with_type.split("#");
					String contentId = s[0];
					checkImpressionArticleArr.add(contentId);
					hmContentIdsData.put(contentId, infoid_with_type);
					hmContentIdsWaitData.put(contentId, tuple.getScore());
					if(effectiveExpTuples.contains(tuple)){
						hmContentIdsForExplorePoolData.put(contentId,exploreKey);
					}else if(effectiveExploringTuples.contains(tuple)){
						hmContentIdsForExplorePoolData.put(contentId,expinfoExploringkey);
					}else{
						hmContentIdsForExplorePoolData.put(contentId,expinfoWaitingkey);
					}
				}
				
				//获取文章在-1impression中的impression count 如果达到阈值 添加到删除队列中
				if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){	
					String[] arrContentIdAllImpression = jedisCache.hmget(expinfoAllImpressionInfokey, checkImpressionArticleArr.toArray(new String[0])).toArray(new String[0]);			
					
					int i = -1;
					for (String article : checkImpressionArticleArr) {
						i++;
						String keyInfo = "";
						try {
							 keyInfo =  hmContentIdsForExplorePoolData.get(article);
							 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
							 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
							if(arrContentIdAllImpression[i] == null ){//如果解析impression count失败，则可以认为该资讯数据为无效数据，从各个池中删掉
								listNeedToDeletedContentData.add(hmContentIdsData.get(article));
								listNeedToDeletedContentId.add(article);
								 logParam = new Object[] { "remove_explore_info", keyInfo, article,bjTime,hmContentIdsData.get(article),
										 System.currentTimeMillis() };
								System.out.println("\t  "+keyInfo + ",to remove all impression is null ,"+ StringUtils.join(logParam, "&"));
							}else if( NumberUtils.toInt(arrContentIdAllImpression[i]) < threshold){
								System.out.println("\t "+keyInfo+",  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdAllImpression[i]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
								continue;
							}else{
								listNeedToDeletedContentData.add(hmContentIdsData.get(article));
								listNeedToDeletedContentId.add(article);	
								 logParam = new Object[] { "remove_explore_info",  hmContentIdsForExplorePoolData.get(article), article,bjTime,hmContentIdsData.get(article),
										arrContentIdAllImpression[i], System.currentTimeMillis() };
								System.out.println("\t  "+keyInfo + ", to remove bigger threshold article,"+ StringUtils.join(logParam, "&"));
							}
							
						
							
//							hmContentIdsImpression.put(hmContentIdsData.get(article),NumberUtils.toInt(arrContentIdAllImpression[i]));
						} catch (Exception e) {
//							hmContentIdsImpression.put(hmContentIdsData.get(article),0);
							System.out.println("\t "+keyInfo + ", get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
						}
					}
				}
				
			}
		}
		
		
		// need to delete
		//将删除队列的文章  在每个产品中的等待池 探索池 进行池 impression移除、-1impression
		if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
			//a 将删除队列的文章 在每个产品中的等待池 探索池 进行池 impression移除
			System.out.println("\t remove  listNeedToDeletedContentData in apps");
			
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(":");
				for (String mainLanguage : arrLanguages)
				{			
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					String expinfoAppImpressionInfokey = getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					// 其他各个产品 等待池 探索池 进行池						
					String expinfoOtherExploringkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"ing") : getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
					String expinfoOtherkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage) : getExpInfoKey(firmApp, mainLanguage, strContentType);
					String expinfoOtherWaitingkey = (strContentType == null) ? getExpInfoKey(firmApp, mainLanguage,"eagerly") : getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
				
					String[] arrContentIdAppImpression = jedisCache.hmget(expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0])).toArray(new String[0]);
					
					
					int j = -1;					
					for (String article : listNeedToDeletedContentId) {
						j++;
						try {							
							 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
							 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
							 System.out.println("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey+",  get app impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdAppImpression[j]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
							
						} catch (Exception e) {							
							System.out.println("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey + ", get app impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
						}
					}
					
					//移除各个产品的探索池 等待池 进行池信息 		
					System.out.println("listNeedToDeletedContentData:"+listNeedToDeletedContentData);
					System.out.println("removeBatchArticlesFromExpPool_expinfoOtherkey:"+expinfoOtherkey);
//					jedisCache.zrem(expinfoOtherkey,listNeedToDeletedContentData.toArray(new String[0]) );//

					//removeBatchArticlesFromExpPool(expinfoOtherkey, listNeedToDeletedContentId.toArray(new String[0]));
					System.out.println("removeBatchArticlesFromExpPool_expinfoOtherkey:"+expinfoOtherExploringkey);
//					removeBatchArticlesFromExpPool(expinfoOtherExploringkey, listNeedToDeletedContentId.toArray(new String[0]));
//					jedisCache.zrem(expinfoOtherExploringkey,listNeedToDeletedContentData.toArray(new String[0]) );
					System.out.println("removeBatchArticlesFromExpPool_expinfoOtherWaitingkey:"+expinfoOtherWaitingkey);
//					jedisCache.zrem(expinfoOtherWaitingkey,listNeedToDeletedContentData.toArray(new String[0]) );
//					removeBatchArticlesFromExpPool(expinfoOtherWaitingkey, listNeedToDeletedContentId.toArray(new String[0]));
					//移除各国impression
					System.out.println("removeBatchArticlesFromExpPool_expinfoOtherWaitingkey:"+expinfoAppImpressionInfokey);
//					jedisCache.hdel(expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));//
//					removeBatchArticlesFromImpressionInfoPool( expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));
				}
			}
			String expinfoAllImpressionInfokey = getExpInfoKey("-1", "Spanish","impressionOfInfoIdKey") ;
			//b 将删除队列的文章 在-1impression移除
//			removeBatchArticlesFromImpressionInfoPool( expinfoAllImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));	
//			jedisCache.hdel(expinfoAllImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));//
		}
		
		
		System.out.println("\t all ,removeBiggerThresholdVaildArticles listNeedToDeletedContentId.size:"+listNeedToDeletedContentId.size()+", cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
	
		

		
	}
	
	

}
