package com.inveno.core.process.expinfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.expinfo.face.AbstractExpInfo;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

import redis.clients.jedis.JedisCluster;

@Component("expInfoProcess")
public class ExpInfoProcess extends AbstractExpInfo{

	public static  Log logger = LogFactory.getLog(ExpInfoProcess.class);

	public static  Log flowExpReqLog = LogFactory.getLog("flowExpLog");

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private MonitorLog monitorLog;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	/**
	 * 流量探索功能
	 * @param indexList
	 * @param resultList
	 * @param resultSet
	 * @param size
	 * @param req
	 * @return
	 */
	@Override
	public void getExpInfo(Context context, List<Double> indexListForNoCahce, List<ResponParam> resultList, List<Double> indexList, Set<ResponParam> resultSet, boolean useImpression) {

		Map<String,Object> configMap = initConfigMap(context,useImpression);
		String app =  context.getApp();

		if( !ifDoExp(context,configMap)){
			return ;
		}

		boolean ifVideo = ContextUtils.isVideoChannel(context);

		Map<String, String> mExpInfoKey = getExpInfoKey(app, context.getLanguage(), ifVideo, false);
		String expinfo_key = mExpInfoKey.get("origin");
		logger.debug("uid is :"+ context.getUid() + " mExpInfoKey is " + mExpInfoKey);

		int expPVCnt = (int) configMap.get("expPVCnt");
		int expRange = (int) configMap.get("expRange") ;//探索的范围
		double flowExpPer = (double) configMap.get("flowExpPer");

 		if (ifVideo) {
 			flowExpPer = (double) configMap.get("flowExpPer2Video");
 			expRange = (int)configMap.get("expRangeOfVideo");
 		}

 		Set<String> expInfoSet = jedisCluster.zrange(expinfo_key, 0, expRange);
 		int expInfoLength = ( null ==  expInfoSet ? 0 :expInfoSet.size() );
		if( logger.isDebugEnabled() ){
			logger.debug("uid is :"+ context.getUid() +" expInfoLength is " + expInfoLength +" , expinfo_key is " + expinfo_key);
		}


		if( !checkExpInfoLength(context,expInfoLength,useImpression,configMap) ){
			return;
		}

		if( logger.isDebugEnabled() ){
			logger.debug("uid is " +  context.getUid()+  " , expInfoLength  is " + expInfoLength  );
		}

		doGetExpInfo(context, indexListForNoCahce, flowExpPer, expinfo_key, resultSet, resultList, indexList, Math.min(expRange, expInfoLength), expPVCnt,configMap,expInfoSet);
	}


	private void doGetExpInfo(Context context, List<Double> indexListForNoCahce, double flowExpPer, String key, Set<ResponParam> resultSet, List<ResponParam> resultList, List<Double> indexList,int expRange,int expPVCnt, Map<String, Object> configMap, Set<String> expInfoSet){

		int cnt = 0;
		String app = context.getApp();

		//get info
		Set<String> idSet = expInfoSet;

		String[] ids = idSet.toArray(new String[0]);
		if( ids == null ){
			return ;
		}

		HashSet<String> expinfoSet = new HashSet<String>();
		try {

			for (Double resultIndex : indexListForNoCahce)
			{

				ResponParam re = null ;
				try {
					double value = Math.random();
					if( value > flowExpPer ){
						continue;
					}
					if( flowExpReqLog.isTraceEnabled() ){
						flowExpReqLog.trace(" uid is " +  context.getUid() +  " values is " + value +", flowExpPer is " + flowExpPer + " ,resultIndex is " + resultIndex);
					}

					int index = new Random().nextInt(ids.length) ;
					String contentMsg = ids[index];
					String contentId = ids[index].split("#")[0];

					re = new ResponParam(Strategy.FLOW_EXPLORATION.getCode(), contentId) ;
					int count=0;

					while ( (!expinfoSet.add(contentMsg) || !resultSet.add(re) || !filterIds(context, contentMsg, flowExpReqLog,configMap,key) ) ) {
						index = new Random().nextInt(ids.length) ;
						re = new ResponParam(Strategy.FLOW_EXPLORATION.getCode(), contentId) ;
						if( flowExpReqLog.isDebugEnabled() ){
							flowExpReqLog.debug(" contain uid is " +  context.getUid()+  " resultIndex is "+ cnt +" expinfo index is " + index);
						}
						count ++;
						if( count >= 5 ){
							break;
						}
					}
					if(count >= 5 ){
						continue;
					}

					cnt ++;
					if( flowExpReqLog.isDebugEnabled() ){
						flowExpReqLog.debug("uid is " +  context.getUid()+  " cnt is "+ cnt +" expinfo index is " + index);
					}

					//add to the return list
					resultList.set( resultIndex.intValue() , re);
					indexList.remove(resultIndex);
					//incrBy
					long pv = jedisCluster.incrBy(re.getInfoid()+"_"+app, 1);

					if( logger.isDebugEnabled() ){
						logger.debug("expinfo uid is " +  context.getUid()+  " and app is "+ app +" ,and language is "+ context.getLanguage() +" pv  is " + pv +" ,and infoid is " + re.getInfoid()+" , and abtest is " + context.getAbtestVersion()  );
					}
					if(flowExpReqLog.isDebugEnabled()){
						flowExpReqLog.debug("expinfo uid is " +  context.getUid()+  " and app is "+ app +" ,and language is "+ context.getLanguage() +" pv  is " + pv +" ,and infoid is " + re.getInfoid()+" , and abtest is " + context.getAbtestVersion()  );
					}
					//if pv > 50 ,then remove from the zset
					if( pv >= expPVCnt ){

						threadPoolTaskExecutor.submit(()->{
							long returnValue = jedisCluster.zrem( key , contentMsg );
							if( returnValue == 0 ){
								flowExpReqLog.error("jedisCluster.zrem( key , re.getInfoid()) error , and abtest is " + context.getAbtestVersion() +" ,and uid is " +  context.getUid() +" and infoid is " + contentId );
							}
							//set redis expire time
							jedisCluster.expire(contentId+"_"+app,  2*24*60*60 );
							try {
								List<String> list = new ArrayList<String>();
								list.add(contentId);
								readFilterInfo.addIdToBloomByExp(app, context.getLanguage(), list);
							} catch (Exception e) {
							}
						});

						monitorLog.addCntLogByProduct(context, MonitorType.EXP_INFO_CNT);
						logger.info("getExpInfo get 1 ,and app is "+ app +" ,and language is "+ context.getLanguage() +" ,"
								+ "and uid is " +  context.getUid() + " , and infoid is " + re.getInfoid()
								+" , and abtest is " + context.getAbtestVersion());

						flowExpReqLog.info("getExpInfo get 1 ,and app is "+ app +" ,and language is "+ context.getLanguage()
								+" ,and uid is " +  context.getUid() + " , and infoid is " + re.getInfoid() +" , and abtest is " + context.getAbtestVersion());
					}

				} catch (Exception e) {
					resultList.set( resultIndex.intValue(),null );
					indexList.add( resultIndex );
					if( re != null ){
						jedisCluster.incrBy( re.getInfoid()+"_"+app, -1 );
					}
					flowExpReqLog.error("getExpInfo error get "+ cnt +" ,and app is "+ app +" ,and uid is " +  context.getUid() ,e);
				}
			}
		} catch (Exception e) {
			flowExpReqLog.error("getExpInfo error get "+ cnt +" ,and app is "+ app +" ,and uid is " +  context.getUid() ,e);
			return ;
		}
	}

}
