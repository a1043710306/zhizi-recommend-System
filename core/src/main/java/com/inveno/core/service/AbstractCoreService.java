package com.inveno.core.service;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;
import com.inveno.common.bean.Context;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.cms.CMSInterface;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import redis.clients.jedis.JedisCluster;

@Component
public abstract class  AbstractCoreService implements ICoreService {
	
	@Autowired
	private JedisCluster jedisCluster;
	
	@Autowired
	private CMSInterface cMSInterface;
	
	public Log logger = LogFactory.getLog(AbstractCoreService.class);
	
	public Log errlogger = LogFactory.getLog("toErrFile");
	
	@Autowired
	UserService userService;
	
	@Autowired
	EhCacheCacheManager cacheManager;
	
	@Autowired
	private MonitorLog monitorLog;
	
	
	/**
	 * 清除缓存
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月6日 下午8:41:43
	 *  @param context
	 */
	protected void cleanCache(Context context) {
		int channelId = ContextUtils.getChannelId(context);
		final String rankingCacheKey = (channelId > 0) ? KeyUtils.subChannelKey(context) : KeyUtils.getMainKey(context);

		if (context.getZhiziListReq() != null && (context.getZhiziListReq().getOperation() ==1 || context.getZhiziListReq().getOperation() ==4)) {
			jedisCluster.del(rankingCacheKey);
		}
	}
	
	
	/**
	 * 清空为null的值
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月16日 下午8:54:54
	 *  @param context
	 */
	public void cleanNullValue(Context context) {

		List<ResponParam> responselist = context.getResponseParamList();
		if (CollectionUtils.isEmpty(responselist))
			return;
		Iterator<ResponParam> it = responselist.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}
	}
	
	/**
	 * initCategoryIds
	 * @param context
	 */
	protected void initCategoryIds(Context context) {
		
		if(  null != context.getRecNewsListReq() || null != context.getTimelineNewsListReq() ){
			initCategoryIdsOldInterface(context);
		}else{
			int channelId = ContextUtils.getChannelId(context);
			List<Integer> catList = cMSInterface.getCategoryIdListFromCMS(context);
//			if(logger.isDebugEnabled()){
//				//begin remove test yezi 补全要下发的scenario category  2018年3月16日16:49:48			
//				catList.add(133);
//				catList.add(123);
//				//begin remove test yezi 补全要下发的scenario category  2018年3月16日16:49:48
//			}
			
			Integer contentQuality = cMSInterface.getContentQualityFromCMS(context);
			if (channelId > 0 && CollectionUtils.isEmpty(catList)) {
				context.setCategoryids(Arrays.asList(0));
				logger.error("initCategoryIds error ,no categoryid list and uid " + context.getUid() + ",and scenario is " + context.getScenario());
				errlogger.error("initCategoryIds error ,no categoryid list and uid " + context.getUid() + ",and scenario is " + context.getScenario());
			} else {
				context.setCategoryids(catList);
			}
			
			if( null != contentQuality ){
				context.setContentQuality(contentQuality);
			}
			logger.info(" app " + context.getApp() + " ,initCategoryIds and catsId is " + catList + " , and uid " + context.getUid());
		}
	}

	/**
	 *
	 *  Description:
	 *  @author liubin  DateTime 2016年6月6日 下午8:18:25
	 *  @param context
	 */
	protected void initCategoryIdsOldInterface(Context context){
		int channelId = (int)context.getScenario();
		
		if( logger.isDebugEnabled() ){
			logger.debug(" initCategoryIdsOldInterface  uid " + context.getUid() );
		}
		List<Integer>  catList = cMSInterface.getCategoryIdListFromCMS(context);
		
		if(channelId > 0 && CollectionUtils.isEmpty(catList) ){
			context.setCategoryids(Arrays.asList(0));
			logger.error("initCategoryIdsOldInterface error ,no categoryid list uid " + context.getUid() + ",and channelId is " + context.getScenario());
			errlogger.error("initCategoryIds error ,no categoryid list and uid " + context.getUid() + ",and scenario is " + context.getScenario());
		}else{
			context.setCategoryids(catList);
		}
		
		if( logger.isDebugEnabled() ){
			logger.debug(" app "+ context.getApp() +" ,initCategoryIdsOldInterface and catsId is " + catList +" , and uid " + context.getUid());
		}
		
		userService.initInterestCat(context);
		
		logger.info(" app " + context.getApp() + " ,initCategoryIdsOldInterface and catsId is " + context.getCategoryids() + " , and uid " + context.getUid());
	}
	
	protected long getCacheAndOffset(String offsetKey, String rankingCacheKey, Context context, int fetchNum) {
		boolean ifL1Cache = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifL1Cache"));
		
 		long cacheNum = 0 ;
 		if (ifL1Cache) {
 			Ehcache cache = cacheManager.getCacheManager().getEhcache("userCache");
 			Element element = cache.get(rankingCacheKey);
 			Object v = (element != null ? element.getObjectValue() : null);
 			
 			@SuppressWarnings("unchecked")
			List<ResponParam> list = (List<ResponParam>) v;
 			
 	 		cacheNum = CollectionUtils.isEmpty(list) ? 0 : list.size();
 	 		if (cacheNum < fetchNum) {
 	 			cacheNum = jedisCluster.zcard(rankingCacheKey);
 	 		} else {
 	 			context.setCacheResponseParamList(list);
 	 			context.setCacheElement(element);
 	 			monitorLog.addCntLogByProduct(context, MonitorType.REQUEST_L1CACHE_COUNT);
 	 		}
 	 		
 	 		Integer offset = cacheManager.getCache("userCache").get(offsetKey, Integer.class);
 	 		if (offset == null || offset == 0) {
 	 			String strOffset = jedisCluster.get(offsetKey);
 	 			offset = NumberUtils.toInt(strOffset, 0);
 	 		}
 	 		context.setOffset(offset);
 		} else {
 			cacheNum = jedisCluster.zcard(rankingCacheKey);
 			String strOffset = jedisCluster.get(offsetKey);
 	 		int offset = NumberUtils.toInt(strOffset, 0);
 	 		context.setOffset(offset);
 		}
 		
 		return cacheNum;
	}
	

	
}
