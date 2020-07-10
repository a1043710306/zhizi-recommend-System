package com.inveno.core.process.result.impl;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.process.SimHashControl;
import com.inveno.core.process.result.ResultAbstract;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;

@Component("resultProcessAllImpl")
public class ResultProcessAllImpl extends ResultAbstract{

	//public Logger recordLog = Logger.getLogger("recordLog");

	private Log logger = LogFactory.getLog(ResultProcessAllImpl.class);

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	private MonitorLog monitorLog;

	@Override
	public void getInfoByCache(Context context) {

		long cur = System.currentTimeMillis();
		String uid = context.getUid();

		String key = KeyUtils.getMainKey(context);
		String offsetKey = KeyUtils.getMainOffset(context);

		if (null != context.getRecNewsListReq() && context.getRecNewsListReq().isNeedBanner()) {
			key = KeyUtils.getQBKey(context);
			offsetKey = KeyUtils.getQBOffsetKey(context);
		}
		if (null != context.getTimelineNewsListReq()) {
			int channelId = context.getTimelineNewsListReq().getChannelId();
			key = KeyUtils.getQCNKey(context, channelId);
			offsetKey = KeyUtils.getQCNOffsetKey(context, channelId);
		}

		Set<ResponParam> resultSet = new HashSet<ResponParam>();
		List<ResponParam> resultList = infoToReturn(context, true,resultSet);


		List <Double> indexList = context.getIndexList();
		int neededCnt = indexList.size();
		Set<byte[]> set = null;
		byte[][] fields = null;
		if (neededCnt > 0) {
			set = jedisCluster.zrange(key.getBytes(), 0, neededCnt - 1 );
			fields = set.toArray(new byte[0][0]);
			List<byte[]> list = jedisCluster.hmget( KeyUtils.getDetailKeyByUid(context).getBytes(), fields);
//			TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			for (byte[] bs : list) {
				ResponParam res = new ResponParam();
				try {
					if (bs != null) {
						deserializer.deserialize(res, bs);
						resultList.set( indexList.get(0).intValue() , res );
					}
				} catch (TException e) {
					logger.error(" uid is :"+ uid + ",  deserializer.deserialize time is " + (System.currentTimeMillis()-cur)
							+" ,and size is " + resultList.size() +" ,and cur = " + System.currentTimeMillis(),e);
				}catch (Exception e) {
					logger.error(" uid is :"+ uid + ",  deserializer.deserialize time is " + (System.currentTimeMillis()-cur)
							+" ,and size is " + resultList.size() +" ,and cur = " + System.currentTimeMillis() + e,  e.getCause());
				}
				finally{
					indexList.remove( 0 ) ;
				}

			}

		}

		final Set<byte[]> setfinal = set ;
		final String rediskey = key;
		final byte[][] fields_f = fields;
		final String offsetKeyFinal = offsetKey;
		threadPoolTaskExecutor.submit(() -> {
			long ttlTime = jedisCluster.ttl(rediskey);
			if (ttlTime == -1 ) {
				jedisCluster.expire(rediskey, 1);
				jedisCluster.expire(offsetKeyFinal, 1);
				logger.warn("redis key ttl -1 key is  , uid is "+ uid  +" , and abtestVersion is "+ context.getAbtestVersion() +" " + rediskey);
			}

			//incrBy offset
			jedisCluster.incrBy( offsetKeyFinal, resultList.size() );
			// 统一删除
			if (setfinal.size() > 0 && setfinal != null) {
				// jedisCluster.zrem(rediskey.getBytes(), setfinal.toArray(new  byte[0][0]));
				jedisCluster.zremrangeByRank(rediskey.getBytes(), 0, setfinal.size() >= 1 ? setfinal.size() - 1 : 0);
			}
			// 如果都进行删除?则会出现.qcn,q接口同时出现时候,由于删除了,则会导致没有详情的情况
			// 例如用户请求q接口和qcn接口,有同一条资讯
			if ( ("coolpad".equals(context.getApp()) || "emui".equals(context.getApp())) && fields_f != null ) {
				jedisCluster.hdel(KeyUtils.getDetailKeyByUid(context).getBytes(), fields_f);
			}

			if ("fuyiping-gionee".equals(context.getApp()) && setfinal.size() >0 && setfinal != null) {
				if (context.getRecNewsListReq().isNeedBanner()) {
					jedisCluster.zrem( KeyUtils.getMainKey(context).getBytes(), setfinal.toArray(new byte[0][0]));
				} else {
					jedisCluster.zrem(KeyUtils.getQBKey(context).getBytes(), setfinal.toArray(new byte[0][0]));
				}
			}
		});

		//删除返回值中的 null 值,保证不报错
		Iterator<ResponParam> it = resultList.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}
		//getTopInfo(resultList,context);
		context.setResponseParamList(resultList);
		logger.info("uid is :"+ uid +" ,ResultProcessAllImpl return through cache,size is " + context.getResponseParamList().size());
	}

	@Override
	public void getInfoByNoCache(Context context) {

		//get uid,app 用户id和渠道
		String uid =  context.getUid();
		//String app = context.getApp();
		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("uid is :"+ uid +" ,begin ResultProcessAllImpl  noCache ,"
					+ " and result size is " + context.getResponseParamList().size() + " and time is  " + cur );
		}

		int rediscacheExpire = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "rediscacheExpire"), 600);
		if (logger.isDebugEnabled()) {
			end  = System.currentTimeMillis();
			logger.debug("uid is :"+ uid +" ,begin  info2Return ,time is " + (end-cur) +",and cur: " + System.currentTimeMillis()  );
		}

		//拼装返回结果
		Set<ResponParam> resultSet = new HashSet<ResponParam>();
		List<ResponParam> resultList = infoToReturn(context, false, resultSet);
		if (logger.isDebugEnabled()) {
			end  = System.currentTimeMillis();
			logger.debug("uid is :"+ uid +",end infoToReturn time is " + (end-cur) +",begin add2Cache,"
					+ "and size is " + resultList.size() +",and cur = " + System.currentTimeMillis()  );
		}

		//如果所有都拼接完成,则表示剩余>=0
		if (context.getIndexList().size() == 0) {
			addToCache(context,resultList,rediscacheExpire);
			if (logger.isDebugEnabled()) {
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",end addToCache time is " + (end-cur) +","
						+ "and size is " + resultList.size() +",and cur = " + System.currentTimeMillis()  ) ;
			}
		}

		//fallback 资讯
		if (context.getIndexList().size() != 0) {
			List<ResponParam> lastestInfoList = context.getNewsQList();
			monitorLog.addCntMonitorLogByConfigId(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT);
			monitorLog.addCntLogByProductAndScenario(context, MonitorType.FALLBACK_REQUEST_COUNT);
			addNewInfo(resultList,context.getNum(),lastestInfoList,context.getIndexList(),resultSet);
			end  = System.currentTimeMillis();
			logger.info("get news through fallback uid is :"+ uid + " ,addNewInfo time is " + (end-cur) + "," + "and size is " + resultList.size() +",and cur = " + System.currentTimeMillis()  );
		}

		//get topInfo
		//getTopInfo(resultList,context);
		context.setResponseParamList(resultList);

		//处理offset,需要将offset加上,offset为本次用户读取的位置
		String offsetKey = KeyUtils.getMainOffset(context);
		if (null != context.getRecNewsListReq() && context.getRecNewsListReq().isNeedBanner()) {
			offsetKey = KeyUtils.getQBOffsetKey(context);
		}
		if (null != context.getTimelineNewsListReq()) {
			int channelId = context.getTimelineNewsListReq().getChannelId();
			offsetKey = KeyUtils.getQCNOffsetKey(context, channelId);
		}

		final String offsetKeyInner = offsetKey;
		int finalRediscacheExpire = rediscacheExpire ;

		threadPoolTaskExecutor.submit(() -> {
			try {
				jedisCluster.incrBy(offsetKeyInner, resultList.size());
			} catch (Exception e) {
			} finally {
				jedisCluster.expire(offsetKeyInner, finalRediscacheExpire);
				if (jedisCluster.ttl(offsetKeyInner.getBytes()) <= 0) {
					jedisCluster.expire(offsetKeyInner.getBytes(), finalRediscacheExpire);
				}
			}

			// add by liubin :2016-7-6 12:05:20 ,as 需要进行相互删除;
			if ("fuyiping-gionee".equals(context.getApp())) {
				Set<byte[]> set = new HashSet<byte[]>();
				for (ResponParam responParam : resultList) {
					set.add(responParam.getInfoid().getBytes());
				}
				if (context.getRecNewsListReq().isNeedBanner()) {
					jedisCluster.zrem(KeyUtils.getMainKey(context).getBytes(), set.toArray(new byte[0][0]));
				} else {
					jedisCluster.zrem(KeyUtils.getQBKey(context).getBytes(), set.toArray(new byte[0][0]));
				}
			}
		});

		if (logger.isDebugEnabled()) {
			end  = System.currentTimeMillis();
			logger.debug("uid is :"+ uid + " ResultProcessAllImpl result return ,through noCache "
					+ "time is " + (end-cur) +",and size is " + resultList.size() +",and cur = " + System.currentTimeMillis()  );
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
	public void addInfo(Context context,List<ResponParam> relist, List<ResponParam> resultList, List<Double> indexList,Set<ResponParam> resultSet){

		long begin = System.currentTimeMillis();
		List<Integer> categoryIdList = context.getCategoryids();

		Iterator<ResponParam> it = relist.iterator();
		while (it.hasNext() && indexList.size() > 0 ) {

			ResponParam re = it.next();
			if (!CollectionUtils.isEmpty(categoryIdList)) {
				if (!categoryIdList.contains(re.getCategoryId())) {
					continue;
				}
			}

			if (resultSet.add(re)) {
				resultList.set( indexList.get(0).intValue(),  re );
				indexList.remove( indexList.get(0) );
			} else {
				it.remove();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(" uid is :"+ context.getUid() + ",end addInfo time is " + (System.currentTimeMillis()-begin)
					+" ,and result is " + resultList +" ,and  indexList is "+ indexList +" ,and cur = " + System.currentTimeMillis());
		}
	}


	/**
	 * 增加到缓存,增加到redis,同时将结果从context中移除
	 * @param context
	 * @param resultList
	 */
	private void addToCache(Context context, List<ResponParam> resultList,int rediscacheExpire) {

		//需要的num长度大于推荐长度:其实此处会有误杀:例如 getNum==8,推荐过已读之后长度为6,但是有强插和探索资讯4条,其实会有2条剩余
		if (context.getResponseParamList().size() <= context.getNum()) {
			return;
		}

		//将下发出去的资讯从 list中删除;此处可以考虑使用 sublist;
		context.getResponseParamList().removeAll(resultList);
		String key = KeyUtils.getMainKey(context);

		if (null != context.getRecNewsListReq() && context.getRecNewsListReq().isNeedBanner()) {
			key  = KeyUtils.getQBKey(context);
		}
		if (null != context.getTimelineNewsListReq()) {
			int channelId = context.getTimelineNewsListReq().getChannelId();
			key  = KeyUtils.getQCNKey(context, channelId);
		}

		//String offsetKey = new String(context.getRecNewsListReq().getBase().getUid()+"::"+context.getRecNewsListReq().getBase().getApp()+"::offset");
		final String redisKey = key;
		List<ResponParam> cacheList = context.getResponseParamList();

		threadPoolTaskExecutor.submit(() -> {
			String abtest = context.getAbtestVersion();
			String strClass = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "postclass");
			int cacheMaxCnt = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "cacheMaxCnt"), 300);

			List<ResponParam> simHashList = null;
			if (strClass.contains("simHashControl")) {
				int simHashControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashControlwindowSize"), 8);
				int simHashDistance          = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashDistance"), 28);
				int simHashEnd               = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "simHashend"), 100);
				if (cacheMaxCnt > simHashEnd) {
					simHashList = SimHashControl.reRank(cacheList, (simHashEnd-simHashControlwindowSize>0 ? simHashEnd-simHashControlwindowSize : 0 )
							, cacheMaxCnt, simHashControlwindowSize, simHashDistance,context);
				}
			} else {
				simHashList = cacheList;
			}

			Map<byte[], Double> map = null;
			Map<byte[], byte[]> detailMap = null;
			try {
				map = new HashMap<byte[], Double>();
				detailMap = new HashMap<byte[], byte[]>();
				TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
				double i = 0;
				for (ResponParam responParam : simHashList) {

					map.put(responParam.getInfoid().getBytes(), i++);
					detailMap.put(responParam.getInfoid().getBytes(), serializer.serialize(responParam));

					if (i > cacheMaxCnt) {
						break;
					}
					// jedisCluster.zadd(redisKey.getBytes(),i++,
					// serializable(responParam));
				}

				jedisCluster.zadd(redisKey.getBytes(), map);
				jedisCluster.hmset(KeyUtils.getDetailKeyByUid(context).getBytes(), detailMap);

				// fuyiping-gionee q 接口的时候,需要将 qb中的内容删除
				if ("fuyiping-gionee".equals(context.getApp()) && !context.getRecNewsListReq().isNeedBanner()) {
					Set<byte[]> qbcache = jedisCluster.zrange(KeyUtils.getQBKey(context).getBytes(), 0, -1);
					jedisCluster.zrem(redisKey.getBytes(), qbcache.toArray(new byte[0][0]));
				}
			} catch (Exception e) {
				logger.error("=== uid is "  + context.getUid() + " abtest "+  context.getAbtestVersion() +"=== addToCache error " + e ,e.getCause());

				jedisCluster.zadd(redisKey.getBytes(), map);
				jedisCluster.hmset(KeyUtils.getDetailKeyByUid(context).getBytes(), detailMap);

				// fuyiping-gionee q 接口的时候,需要将 qb中的内容删除
				if ("fuyiping-gionee".equals(context.getApp()) && !context.getRecNewsListReq().isNeedBanner()) {
					Set<byte[]> qbcache = jedisCluster.zrange(KeyUtils.getQBKey(context).getBytes(), 0, -1);
					jedisCluster.zrem(redisKey.getBytes(), qbcache.toArray(new byte[0][0]));
				}
			} finally {
				jedisCluster.expire(redisKey.getBytes(), rediscacheExpire);
				jedisCluster.expire(KeyUtils.getDetailKeyByUid(context).getBytes(), rediscacheExpire);
			}
		});
	}
}
