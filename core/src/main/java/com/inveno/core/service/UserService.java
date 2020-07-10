package com.inveno.core.service;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.service.impl.CoreServiceImpl;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.UfsService;
import com.inveno.thrift.UserInfo;

@Component("userService")
public class UserService {
	
	private static Log logger = LogFactory.getLog(CoreServiceImpl.class);
    private static Log poolLogger = LogFactory.getLog("BasePooledObjectFactory");

	@Autowired
	private ObjectPool<TSocket> ufsThriftPool;
	
	@Autowired
	private MonitorLog monitorLog;
	
	public Log errLogger = LogFactory.getLog("toErrFile");

	/**
	 * 
	 * @param context
	 * @return
	 */
	public UserInfo getUserInfo(Context context) {
		
		long tsStart = System.currentTimeMillis();
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		String uid = context.getUid();
		String app = context.getApp();
		String abtestVersion =  context.getAbtestVersion();

		boolean callUfs = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "callUfs"));
		if (!callUfs)
		{
			return null;
		}

		String featureDefaultVersion = "v2";
		String tagsVersion      = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "tagVersionsWithCoefList");
		String catVersion       = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "categoryVersionsWithCoefList");
		String titleTagsVersion = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "titleTagVersionsWithCoefList");
		String ldaTopicVersion  = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "ldaTopicVersionsWithCoefList");
		try {
			if (StringUtils.isNotEmpty(tagsVersion))
			{
				int idx = tagsVersion.indexOf("=");
				if (idx >= 0)
					tagsVersion = tagsVersion.substring(0, idx);
			}
			else
			{
				tagsVersion = featureDefaultVersion;
			}
			if (StringUtils.isNotEmpty(catVersion))
			{
				int idx = catVersion.indexOf("=");
				if (idx >= 0)
					catVersion = catVersion.substring(0, idx);
			}
			else
			{
				catVersion = "";
			}
			if (StringUtils.isNotEmpty(titleTagsVersion))
			{
				int idx = titleTagsVersion.indexOf("=");
				if (idx >= 0)
					titleTagsVersion = titleTagsVersion.substring(0, idx);
			}
			else
			{
				titleTagsVersion = "";
			}
			if (StringUtils.isNotEmpty(ldaTopicVersion)) {
				int idx = ldaTopicVersion.indexOf("=");
				if (idx >= 0)
					ldaTopicVersion = ldaTopicVersion.substring(0, idx);
			}
			else
			{
				ldaTopicVersion = "";
			}
		}
		catch (Exception e2)
		{
			logger.error(strRequestInfo + ", parse feature version error", e2);
		}
		finally
		{
			logger.info(strRequestInfo + ", tagsVersion is "+ tagsVersion + ", catVersion is " + catVersion + ", titleTagsVersion is "+ titleTagsVersion + ", ldaTopicVersion is "+ ldaTopicVersion);
		}

		boolean bFirstInvokeUfs = true;
		int nGetUserInfoRetryCount = 2;
		monitorLog.addCntLogByProduct(context, MonitorType.UFS_INVOKE_COUNT);
		UserInfo userInfo = null;
		while (true) {
			if (!bFirstInvokeUfs) {
				logger.debug(strRequestInfo + ", retry to invoke ufsService");
			}
			if (nGetUserInfoRetryCount <= 0 || userInfo != null)
				break;
			nGetUserInfoRetryCount--;
			userInfo = invokeGetUserInfo(context, tsStart, uid, catVersion, tagsVersion, titleTagsVersion, ldaTopicVersion);
			bFirstInvokeUfs = false;
		}

		return userInfo;
	}

	private UserInfo invokeGetUserInfo(Context context, long tsStart, String uid, String catVersion, String tagsVersion, String titleTagsVersion, String ldaTopicVersion) {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);

		UserInfo userInfo = null;
		TSocket tSocket = null;
		try {
			poolLogger.debug(strRequestInfo + ", getUserInfo ufsThriftPool.borrowObject");
			tSocket = ufsThriftPool.borrowObject();
			try {
				poolLogger.debug(strRequestInfo + ", getUserInfo new UfsService.Client");
				UfsService.Client ufsService = new UfsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));
				if (logger.isDebugEnabled()) {
					logger.debug(strRequestInfo + ", getUserInfo ufsThriftPool.borrowObject spent time: " + (System.currentTimeMillis() - tsStart));
					logger.debug(strRequestInfo + ", getUserInfo ufsThriftPool msg NumActive=" + ufsThriftPool.getNumActive() + ", NumIdle=" + ufsThriftPool.getNumIdle());
				}

				userInfo = ufsService.GetUserInfo(uid, catVersion, tagsVersion, titleTagsVersion, ldaTopicVersion);

				monitorLog.addResTimeLogByProduct(context, MonitorType.UFS_RESPONSE_TIME,(System.currentTimeMillis() - tsStart));
			} catch (Exception e) {
				logger.error(strRequestInfo + ", getUserInfo ufsService.GetUserInfo failed", e);
				errLogger.error(strRequestInfo + ", getUserInfo ufsService.GetUserInfo failed", e);
				monitorLog.addCntLogByProduct(context, MonitorType.UFS_FAIL_COUNT);
				poolLogger.debug(strRequestInfo + ", getUserInfo ufsThriftPool.invalidateObject");
				try {
					ufsThriftPool.invalidateObject(tSocket);
					tSocket = null;
				} catch (Exception e1) {
					logger.error(strRequestInfo + ", getUserInfo ufsThriftPool.invalidateObject failed", e1);
				}
			} finally {
				if (tSocket != null) {
					try {
						poolLogger.debug(strRequestInfo + ", getUserInfo ufsThriftPool.returnObject");
						ufsThriftPool.returnObject(tSocket);
					} catch (Exception e) {
						logger.error(strRequestInfo + ", getUserInfo ufsThriftPool.returnObject failed", e);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(strRequestInfo + ", getUserInfo ufsService.getUserInfo spent time: " + (System.currentTimeMillis() - tsStart));
				}
			}
		} catch (Exception e) {
			logger.error(strRequestInfo + ", getUserInfo ufsThriftPool.borrowObject failed", e);
		}
		return userInfo;
	}

	public void initInterestCat(Context context) {
		if( !"emui".equals(context.getApp()) ){
			return ;
		}
		
		long begin = System.currentTimeMillis();
		String app = context.getApp();
		
		String uid = context.getUid();
		String abtestVersion = context.getAbtestVersion();
		
		boolean bNeedInterestCat = false;
		try {
			bNeedInterestCat = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifNeedInterestCat"));
		} catch (Exception e) {
			logger.error("=== get qconf error,has no [ifNeedInterestCat] config ,and uid is "  + context.getUid() + " abtest "+  abtestVersion +"===",e);
		}
		//需要设置兴趣
		if (!bNeedInterestCat) {
			return ;
		}

		TSocket tSocket = null;
		List<Integer>  catList = new ArrayList<Integer>();
		try {
			tSocket = ufsThriftPool.borrowObject();
			UfsService.Client ufsService = new UfsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));
			if( logger.isDebugEnabled() ){
				logger.debug(" uid: "+ uid  + " ,app "+  app +"  getInterestCat ,end ufsService.borrowObject, "
						+ "time is " +(System.currentTimeMillis()-begin) +",and cur: " + System.currentTimeMillis()  );
			}
			String interestStr = ufsService.GetInterest(uid);
			
			if( logger.isDebugEnabled() ){
				logger.debug(" uid: "+ uid  + " ,app "+  app +"  getInterestCat ,end ufsService.GetInterest ,"
						+ " time is " +(System.currentTimeMillis() -begin) + ",and cur: " + System.currentTimeMillis()  );
			}
			if( !StringUtils.isEmpty(interestStr) ){
				catList = JSON.parseArray(interestStr,Integer.class);
				if( CollectionUtils.isEmpty(catList) ){
					catList = Collections.emptyList();
				}
			} 
		} catch (Exception e) {
			logger.error("=== getInterestCat process Exception ,and uid is "  + uid + " abtest "+  abtestVersion +"===",e);
			try {
				//ufsThriftPool.clear();
			} catch (Exception e1) {
			}
		} finally {
			try {
				ufsThriftPool.returnObject(tSocket);
			} catch (Exception e) {
				logger.error("=== getInterestCat process Exception ,and uid is "  + uid + " abtest "+  abtestVersion +"===",e);
			}
		}
		 
		if( logger.isDebugEnabled() ){
			logger.debug(" uid: "+ uid  + " ,app "+  app +",end ufsService , time is " +(System.currentTimeMillis()-begin) 
					+",list is " + catList +",and cur: " + System.currentTimeMillis()  );
		}
		context.setCategoryids(catList);
	}
}

