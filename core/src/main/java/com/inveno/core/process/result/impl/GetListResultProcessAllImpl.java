package com.inveno.core.process.result.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.process.PicSimHashControl;
import com.inveno.core.process.post.process.SimHashControl;
import com.inveno.core.process.result.ResultAbstract;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.util.TimeUtil;
import redis.clients.jedis.JedisCluster;

@Component("getListResultProcessAllImpl")
public class GetListResultProcessAllImpl extends ResultAbstract{
	
	
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
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		
		final int channelId = ContextUtils.getChannelId(context);
		final String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);
		final String offsetKey = (channelId > 0) ? KeyUtils.subChannelOffsetKey(context, channelId) : KeyUtils.getMainOffset(context);
		
		Set<ResponParam> resultSet = new HashSet<ResponParam>();
		//传入cache标志为true,非cache时候需要直接从list中进行拼接,cache则需要从redis获取
		List<ResponParam> resultList = infoToReturn(context, true, resultSet);
		
		List <Double> indexList = context.getIndexList();
		int neededCnt = indexList.size();
		if (neededCnt > context.getFetchNum()) {
			neededCnt = context.getFetchNum() ;
		}
		Set<byte[]> set = null;
		byte[][] fields = null;
		Set<byte[]> contentIDset = new HashSet<byte[]>();
		//进行反序列化
		if (indexList.size() > 0)
		{
			List<ResponParam> cachelist = getCacheFromEhcache(rankingCacheKey, neededCnt, context);
			if (logger.isDebugEnabled()) {
				logger.debug("uid " + context.getUid() + ", getCacheFromEhcache" + cachelist + " time is "
						+ (System.currentTimeMillis() - cur));
			}
			if (CollectionUtils.isEmpty(cachelist) ) {
				TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
				set = jedisCluster.zrange(rankingCacheKey.getBytes(), 0, neededCnt - 1 );
				fields = set.toArray(new byte[0][0]);
				
				List<byte[]> list = jedisCluster.hmget( KeyUtils.getDetailKeyByUid(context).getBytes(), fields);
				
				for (byte[] bs : list)
				{
					ResponParam res = new ResponParam();
					try
					{
						if (bs != null)
						{
							deserializer.deserialize(res, bs);
						}
					}
					catch (TException e)
					{
						res = null;
						logger.error(" uid is :"+ uid + ",  deserializer.deserialize time is " + (System.currentTimeMillis()-cur) 
								+" ,and size is " + resultList.size() +" ,and cur = " + System.currentTimeMillis(),e);
					}
					catch (Exception e)
					{
						logger.error(" uid is :"+ uid + ",  deserializer.deserialize time is " + (System.currentTimeMillis()-cur) 
								+" ,and size is " + resultList.size() +" ,and cur = " + System.currentTimeMillis() + e,  e.getCause());
					}
					
					if (resultSet.add(res) && indexList.size() >0)
					{
						resultList.set(indexList.get(0).intValue(), res);
						indexList.remove( 0 ) ;
						contentIDset.add(res.getInfoid().getBytes());
					}
				}
			}
			else
			{
				set = new HashSet<byte[]>();
				for (ResponParam res : cachelist)
				{
					TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
					try
					{
						if ( resultSet.add(res) && indexList.size() >0 )
						{
							set.add(serializer.serialize(res));
							contentIDset.add(res.getInfoid().getBytes());
							resultList.set(indexList.get(0).intValue(), res);
							indexList.remove( 0 ) ;
						}
					}
					catch (TException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
			
		
		final Set<byte[]> setfinal = set;
		threadPoolTaskExecutor.submit(() -> {
			long ttlTime = jedisCluster.ttl(rankingCacheKey);
			if (ttlTime == -1) {
				jedisCluster.expire(rankingCacheKey, 1);
				jedisCluster.expire(offsetKey, 1);
				logger.warn("redis key ttl -1 key is "+ rankingCacheKey +" , uid is "+ uid  +" , and abtestVersion is "+ context.getAbtestVersion() +" " + rankingCacheKey);
			}
			
			if (setfinal != null && setfinal.size() > 0) {
				//删除本次请求channel的缓存
				jedisCluster.zremrangeByRank(rankingCacheKey.getBytes(), 0, setfinal.size() >= 1 ? setfinal.size() - 1 : 0);
			}
			//如果本次请求的为 频道,则删除for u中缓存
			if (channelId > 0) {
				jedisCluster.zrem(  KeyUtils.getMainKey(context).getBytes(), contentIDset.toArray(new byte[0][0]));
			}
			//incrBy offset
			jedisCluster.incrBy(offsetKey, resultList.size());
			addOffset2Ehcache(context, offsetKey, context.getOffset(), resultList.size());
		});
		
		//获取指定资讯
		getTopInfo(resultList, context);
		
		//删除返回值中的 null 值,保证不报错
		Iterator<ResponParam> it = resultList.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}
		context.setResponseParamList(resultList);
		//对同一屏的资讯去重
		//checkDulInfo(context);
		logger.info("uid is :"+ uid +" ,ResultProcessAllImpl return through cache,size is " + context.getResponseParamList().size());
	}

	@Override
	public void getInfoByNoCache(Context context) {
		
		//get uid,app 用户id和渠道
		String uid =  context.getUid();
		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();
		
		if (logger.isDebugEnabled())
		{
			logger.debug("uid is :"+ uid +" ,begin ResultProcessAllImpl  noCache , " + "and result size is " + context.getResponseParamList().size() + " and time is  " + cur );
		}

		int rediscacheExpire = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "rediscacheExpire"), 600);
		if (logger.isDebugEnabled())
		{
			end  = System.currentTimeMillis();
			logger.debug("uid is :"+ uid +" ,begin  info2Return ,time is " + (end-cur) +",and cur: " + System.currentTimeMillis()  );
		}
		//拼装返回结果
		Set<ResponParam> resultSet = new HashSet<ResponParam>();
		List<ResponParam> resultList = infoToReturn(context, false, resultSet);
		
		if (logger.isDebugEnabled())
		{
			end  = System.currentTimeMillis();
			logger.debug("uid is :"+ uid +",end infoToReturn time is " + (end-cur) +",begin add2Cache," + "and remain size is "+ context.getIndexList().size() + " size is " + resultList.size() + ",and cur = " + System.currentTimeMillis()  );
		}
		
		//需要资讯的长度为0,即对应位置都进行了填充
		if (context.getIndexList().size() == 0)
		{
			addToCache(context, resultList, rediscacheExpire);
			if (logger.isDebugEnabled()) {
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",end addToCache time is " + (end-cur) +","
						+ "and add to cache size is " +  context.getResponseParamList().size()  +",and cur = " + System.currentTimeMillis()  ) ;
			}
		}
		
		if (context.getIndexList().size() != 0) {
			List<ResponParam> lastestInfoList = context.getNewsQList();
			monitorLog.addCntMonitorLogByConfigId(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProductAndScenario(context, MonitorType.FALLBACK_REQUEST_COUNT);
			addNewInfo(resultList,context.getNum(),lastestInfoList,context.getIndexList(),resultSet);
			end  = System.currentTimeMillis();
			logger.info("get news through fallback uid is :"+ uid + " ,addNewInfo time is " + (end-cur) 
					+",and remain size is " + context.getIndexList().size() +",and cur = " + System.currentTimeMillis()  );
		}  
		
		//get topInfo
		getTopInfo(resultList,context);
		context.setResponseParamList(resultList);
		
		int channelId = ContextUtils.getChannelId(context);
		final String offsetKey = (channelId > 0) ? KeyUtils.subChannelOffsetKey(context, channelId) : KeyUtils.getMainOffset(context);
		int finalRediscacheExpire = rediscacheExpire;
		threadPoolTaskExecutor.submit(() -> {
			addOffset2Ehcache(context, offsetKey, 0, resultList.size());
			jedisCluster.incrBy(offsetKey, resultList.size());
			jedisCluster.expire(offsetKey, finalRediscacheExpire);
			if (jedisCluster.ttl(offsetKey.getBytes()) <= 0) {
				jedisCluster.expire(offsetKey.getBytes(), finalRediscacheExpire);
			}
		});
		
		if (logger.isDebugEnabled()) {
			logger.debug("uid is :"+ uid + " ResultProcessAllImpl result return ,through noCache time is " + (System.currentTimeMillis()-cur) +",and remain is " + context.getIndexList() +",and cur = " + System.currentTimeMillis()  );
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
	@Override
	public void addInfo(Context context, List<ResponParam> relist, List<ResponParam> resultList, List<Double> indexList, Set<ResponParam> resultSet)
	{
		long begin = System.currentTimeMillis();
		List<Integer> categoryIdList = context.getCategoryids();

		for (int i = 0; i < relist.size() && indexList.size() > 0; i++)
		{
			ResponParam param = (ResponParam)relist.get(i);
			int channelId = ContextUtils.getChannelId(context);
			if (channelId == 0 || CollectionUtils.isEmpty(categoryIdList))
			{
				if (resultSet.add(param))
				{
					resultList.set(indexList.get(0).intValue(), param);
					indexList.remove(indexList.get(0));
				}
			}
			else if (categoryIdList.contains(param.getCategoryId()))
			{
				if (resultSet.add(param))
				{
					resultList.set(indexList.get(0).intValue(), param);
					indexList.remove(indexList.get(0));
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug(" uid is :"+ context.getUid() + ",end addInfo time is " + (System.currentTimeMillis()-begin) +" ,and result is " + resultList +" ,and  indexList is "+ indexList +" ,and cur = " + System.currentTimeMillis());
		}
	}
	

	/**
	* 增加到缓存,增加到redis,同时将结果从context中移除
	* @param context
	* @param resultList
	*/
	private void addToCache(Context context, List<ResponParam> resultList, int rediscacheExpire) {
		
		//需要的num长度大于推荐长度:其实此处会有误杀:例如 getNum==8,推荐过已读之后长度为6,但是有强插和探索资讯4条,其实会有2条剩余
		if (context.getResponseParamList().size() <= context.getNum() )
		{
			return;
		}
		//删除本次下发的资讯
		context.getResponseParamList().removeAll(resultList);
		
		//拼接key
		int channelId = ContextUtils.getChannelId(context);
		final String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);
		List<ResponParam> cacheList = context.getResponseParamList();
		//使用线程池进行处理:后台处理
		threadPoolTaskExecutor.submit(() -> {
			long begin = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("threadPoolTaskExecutor msg uid is "  + context.getUid() + " abtest "+  context.getAbtestVersion() 
						+" , ActiveCount ="+ threadPoolTaskExecutor.getActiveCount()
						+" , CorePoolSize =" + threadPoolTaskExecutor.getThreadPoolExecutor().getCorePoolSize() 
						+" , CompletedTaskCount =" + threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount()
						+" , TaskCount =" + threadPoolTaskExecutor.getThreadPoolExecutor().getTaskCount());
			}
			int cacheMaxCnt = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "cacheMaxCnt"), 300);
			String abtest = context.getAbtestVersion();
			String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "postclass");

			//如果有simhash重排序,则需要进行排序
			List<ResponParam> simHashList = null;
			if (strClass.contains("simHashControl")) {
				int simHashControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashControlwindowSize"), 0);
				int simHashDistance          = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashDistance"), 28);
				int simHashEnd               = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashend"), 90);

				if (cacheMaxCnt > simHashEnd) {
					simHashList = SimHashControl.reRank(cacheList, (simHashEnd-simHashControlwindowSize>0 ? simHashEnd-simHashControlwindowSize : 0 )
							, cacheMaxCnt, simHashControlwindowSize, simHashDistance,context);
				}
			} else {
				simHashList = cacheList;
			}
			
			if (strClass.contains("picSimHashControl")) {
				int simHashControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashControlwindowSize"), 8);
				int simHashDistance          = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashDistance"), 28);
				simHashList = PicSimHashControl.reRank(cacheList, 90, cacheList.size(), simHashControlwindowSize, simHashDistance,context);
			} else {
				simHashList = cacheList;
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
						if (context.getZhiziListReq() != null)
						{
							boolean bForYouChannel = ContextUtils.isForYouChannel(context);
							if (bForYouChannel) {
								contentMsg.append(responParam.getInfoid());
							} else {
								contentMsg.append(responParam.getInfoid()).append("#").append(context.getScenario());
							}
						}
						else
						{
							contentMsg.append(responParam.getInfoid());
						}
						
						map.put( contentMsg.toString().getBytes(), i);
						detailMap.put(contentMsg.toString().getBytes(), serializer.serialize(responParam));
						list.add(responParam);
					}
				}
				
				add2EhcheCache(context, list, rankingCacheKey);
				if (logger.isDebugEnabled()) {
					logger.info("uid "+ context.getUid() +", add2EhcheCache" + list.size() + " , time is " + ( System.currentTimeMillis() - begin ));
				}
				
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(rankingCacheKey.getBytes(), map);
				jedisCluster.hmset(KeyUtils.getDetailKeyByUid(context).getBytes(), detailMap);
			} catch (Exception e) {
				logger.error("=== uid is "  + context.getUid() + " abtest "+  context.getAbtestVersion() +"=== addToCache error " + e ,e.getCause());
				// 但是如果没有配置分类信息 则需要放入到cache中
				jedisCluster.zadd(rankingCacheKey.getBytes(), map);
				jedisCluster.hmset(KeyUtils.getDetailKeyByUid(context).getBytes(), detailMap);
			} finally {
				jedisCluster.expire(rankingCacheKey.getBytes(), rediscacheExpire);
				jedisCluster.expire(KeyUtils.getDetailKeyByUid(context).getBytes(), rediscacheExpire);
			}
		});
	}
	
	private void add2EhcheCache(Context context, List<ResponParam> list, String redisKey) {
		String abtestVersion = context.getAbtestVersion();
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));
		
		if (!ifL1Cache) {
			return ;
		}
		
		long begin = System.currentTimeMillis();
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
		cache.put(new Element(redisKey, list));
		if (logger.isDebugEnabled()) {
			logger.info("uid "+ context +", add2EhcheCache" + list.size() + " , time is " + ( System.currentTimeMillis() - begin ));
		}
	}

	private void addOffset2Ehcache(Context context, String offsetKeyInner, int begin,int size ) {
		String abtestVersion = context.getAbtestVersion();
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));

		if (!ifL1Cache) {
			return ;
		}
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
		int channelId = ContextUtils.getChannelId(context);
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
		
		String abtestVersion = context.getAbtestVersion();
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));

		if (!ifL1Cache) {
			return Collections.emptyList();
		}
		
		List<ResponParam> resultList;
		Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
//		Cache cache = cacheManager.getCache("userCache");
		/*
		@SuppressWarnings("unchecked")
		List<ResponParam> list = cache.get(key,ArrayList.class);*/
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
}
