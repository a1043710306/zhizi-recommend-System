package com.inveno.common.filter.impl;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.MonitorType;
import com.inveno.common.exception.ServerException;
import com.inveno.common.util.CommonUtils;
import com.inveno.common.util.SysUtil;
import com.inveno.common.monitor.MonitorCommLog;
import com.inveno.thrift.AcsService;
import com.inveno.thrift.ResponParam;
import com.inveno.thrift.Status;
import com.inveno.thrift.SysType;

/**
 * 
 *  Class Name: ReadedInfoHandler.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年1月20日 下午3:32:13 
 *  @company inveno 
 *  @version 1.0
 */
public class ReadedInfoHandler {
	
	public static Log logger = LogFactory.getLog(ReadedInfoHandler.class);
	
	public static Log toErrFile = LogFactory.getLog("toErrFile");
	
	private Lock lock = new ReentrantLock();
	
	private ObjectPool<TSocket> thriftPool;
	
	public ObjectPool<TSocket> getThriftPool() {
		return thriftPool;
	}
	
	public void setThriftPool(ObjectPool<TSocket> thriftPool) {
		this.thriftPool = thriftPool;
	}
	

	@Autowired
	private MonitorCommLog monitorLog;
	
	/**
	 * 
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年1月20日 下午8:59:09
	 *  @param thriftPool
	 *  @param uid
	 *  @param app
	 *  @param idList
	 *  @return
	 *  @throws Exception
	 */
	public List<String> filterIdList(Context context,String uid, String app, List<String> idList) throws Exception
	{
		List<String> resultList = new ArrayList<String>();
		List<String> returnList = new ArrayList<String>();
//		int noResponseCount = 0;
//		int unKnowCount = 0;		
//		int GT1000Count = 0;
//		boolean existAcsException = false; //是否存在acs异常；---add by yezi  2018年4月3日15:19:37

		Long begin = System.currentTimeMillis();
		if (CollectionUtils.isEmpty(idList))
		{
			throw new ServerException("list is null");
		}

		for (String id : idList)
		{
			resultList.add(CommonUtils.spliceStr(uid, app, id));
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("uid: " + uid + " filterIdList cost time " + (System.currentTimeMillis() - begin) + " begin =1 to check read");
		}
		
		monitorLog.addCntLogByProduct(context, MonitorType.ACS_REQUEST_COUNT);//acs 调用次数
		
		TSocket tSocket = null;
		try {
			tSocket = thriftPool.borrowObject();
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("uid: " + uid + ",filterList.size ="+resultList.size() + " filterIdList cost time " + (System.currentTimeMillis() - begin) + " end borrowObject=2 to check read");
				}

				AcsService.Client acsService = new AcsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));
				List<Status> list = acsService.existMul(SysType.USER_READ, resultList, 1000);

				if (logger.isDebugEnabled()) {
					logger.debug("uid: " + uid + " filterIdList cost time " + (System.currentTimeMillis() - begin) + " end existMul=3 to check read");
				}
				if (CollectionUtils.isNotEmpty(list)) {
					for (int i = 0; i < list.size(); i++) {
						if (Status.YES.getValue() == list.get(i).getValue()) {
							returnList.add(idList.get(i));
						}
//						if (Status.YES.getValue() == list.get(i).getValue()) {
//							returnList.add(idList.get(i));
//						}else if(Status.NO_RESPONSE.getValue() == list.get(i).getValue()){
//							noResponseCount ++;
//						}else if(Status.UNKNOW.getValue() == list.get(i).getValue()){
//							unKnowCount ++;
//						}else if(Status.GT1000.getValue() == list.get(i).getValue()){
//							GT1000Count ++;
//						}
						//调用check acs 除了1 存在、0 不存在的情况 还包括错误的返回状态（2 3 4）
					}
				
				
					monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_COUNT ,list.size());
//					if(noResponseCount > 0){
//						monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_NO_RESPONSE_ERROR_COUNT ,noResponseCount);
//					}
//					if(unKnowCount > 0){
//						monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_UNKNOW_ERROR_COUNT ,unKnowCount);
//					}
//					
//					if(GT1000Count > 0){
//						monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_GT1000_ERROR_COUNT ,GT1000Count);
//					}
				}
				
			} catch (Exception e) {
				logger.error("uid: " + uid + " acsService.existMul failed", e);
//				existAcsException = true;
//				logProcessCost.info(InitDocumentCollection.getHostIPAddress() + "&&acs_fail_count&&" + (System.currentTimeMillis() - begin) + "&&1&&AVG&&60&&mode=" + strStrategyCode);
				monitorLog.addCntLogByProduct(context, MonitorType.ACS_RESPONSE_FAIL_COUNT);
				try {
					thriftPool.invalidateObject(tSocket);
					tSocket = null;
				} catch (Exception e1) {
					logger.error("uid: " + uid + " filterIdList thriftPool.invalidateObject failed", e1);
				}
			} finally {
				try {
					if (tSocket != null) {
						thriftPool.returnObject(tSocket);
					}
				} catch (Exception e) {
					logger.error("uid: " + uid + " filterIdList thriftPool.returnObject failed", e);
				}
			}
		} catch (Exception e) {
			logger.error("uid: " + uid + " filterIdList thriftPool.borrowObject failed", e);
		}
			
//		if(existAcsException){
//			throw new Exception("Acs exception!");
//		}
		return returnList;
	}
	
	 /**
	  * 
	   * filterIdList(已读过滤)
 	   * @Title: filterIdList
	   * @author huangyiming
	   * @param @param uid
	   * @param @param app
	   * @param @param idList
	   * @param @return
	   * @param @throws Exception    设定文件
	   * @return List<String>    返回类型
	   * @throws
	  */
	public List<ResponParam> filterIds(Context context,String uid, String app, List<ResponParam> responParamList) throws Exception {

		Long begin = System.currentTimeMillis();
		List<ResponParam> resultRespList = new ArrayList<ResponParam>();
//		boolean existAcsException = false; //是否存在acs异常；---add by yezi  2018年4月3日15:19:37
//		int noResponseCount = 0;
//		int unKnowCount = 0;		
//		int GT1000Count = 0;
		
		if (CollectionUtils.isNotEmpty(responParamList)) {
			Map<String, ResponParam> id2Resp = new HashMap<String, ResponParam>();
			List<String> resultList = new ArrayList<String>();

			for (ResponParam resp : responParamList) {
				resultList.add(CommonUtils.spliceStr(uid, app, resp.getInfoid()));
				id2Resp.put(resp.getInfoid(), resp);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("uid: " + uid + " filterIds cost time " + (System.currentTimeMillis() - begin) + " begin=1 to check read");
			}
			monitorLog.addCntLogByProduct(context, MonitorType.ACS_REQUEST_COUNT);//acs 调用 次数
			TSocket tSocket = null;
			try {
				tSocket = thriftPool.borrowObject();
				try {
					AcsService.Client acsService = new AcsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));

					if (logger.isDebugEnabled()) {
						logger.debug("uid: " + uid + ",filterIds.size ="+resultList.size() + " filterIds cost time " + (System.currentTimeMillis() - begin) + " end borrowObject=2 to check read");
					}
				
					Long begin1 = System.currentTimeMillis();
					List<Status> list = acsService.existMul(SysType.USER_READ, resultList, 1000);
				
					if (logger.isDebugEnabled()) {
						logger.debug("uid: " + uid + " filterIds cost time " + (System.currentTimeMillis() - begin1) + " end existMul=3 to check read");
					}

					if (CollectionUtils.isNotEmpty(list)) {
						for (int i = 0; i < list.size(); i++) {
							if (Status.YES.getValue() == list.get(i).getValue()) {
								resultRespList.add(id2Resp.get(responParamList.get(i).getInfoid()));
							}
//							if (Status.YES.getValue() == list.get(i).getValue()) {
//								resultRespList.add(id2Resp.get(responParamList.get(i).getInfoid()));
//							}else if(Status.NO_RESPONSE.getValue() == list.get(i).getValue()){
//								noResponseCount ++;
//							}else if(Status.UNKNOW.getValue() == list.get(i).getValue()){
//								unKnowCount ++;
//							}else if(Status.GT1000.getValue() == list.get(i).getValue()){
//								GT1000Count ++;
//							}
//							//调用check acs 除了1 存在、0 不存在的情况 还包括错误的返回状态（2 3 4）
						}
						
						
						monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_COUNT ,list.size());
//						if(noResponseCount > 0){
//							monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_NO_RESPONSE_ERROR_COUNT ,noResponseCount);
//						}
//						if(unKnowCount > 0){
//							monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_UNKNOW_ERROR_COUNT ,unKnowCount);
//						}
//						
//						if(GT1000Count > 0){
//							monitorLog.addCntCountLogByProduct(context, MonitorType.ACS_CHECK_GT1000_ERROR_COUNT ,GT1000Count);
//						}
					}
					
				} catch (Exception e) {
					logger.error("uid: " + uid + " acsService.existMul failed", e);
//					existAcsException = true;
					monitorLog.addCntLogByProduct(context, MonitorType.ACS_RESPONSE_FAIL_COUNT);
					try {
						thriftPool.invalidateObject(tSocket);
						tSocket = null;
					} catch (Exception e1) {
						logger.error("uid: " + uid + " filterIds thriftPool.invalidateObject failed", e1);
					}
				} finally {
					try {
						if (tSocket != null) {
							thriftPool.returnObject(tSocket);
						}
					} catch (Exception e) {
						logger.error("uid: " + uid + " filterIds thriftPool.returnObject failed", e);
					}
				}
			} catch (Exception e) {
				logger.error("uid: " + uid + " filterIds thriftPool.borrowObject failed", e);
			}
				
			if (logger.isDebugEnabled()) {
				logger.debug("uid: " + uid + " filterIds cost time " + (System.currentTimeMillis() - begin) + " end returnObject=4 to check read");
			}
		}
//		if(existAcsException){
//			throw new Exception("Acs exception!");
//		}

		return resultRespList;
	}
	
	/**
	 * 
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年1月20日 下午8:59:13
	 *  @param idList
	 *  @return
	 */
	public boolean addIdToBloom(String uid, String app, List<String> idList)throws Exception {
		long begin = System.currentTimeMillis();

		boolean bSuccess = true;
		if (CollectionUtils.isNotEmpty(idList)) {
			TSocket tSocket = null;
			try {
				tSocket = thriftPool.borrowObject();
				try {
					AcsService.Client acsService = new AcsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));

					if (logger.isDebugEnabled()) {
						logger.debug("uid: " + uid + " addIdToBloom cost time " + (System.currentTimeMillis() - begin) + " begin=1 to check read");
					}

					List<String> resultList = new ArrayList<String>();
					for (String id : idList) {
						resultList.add(CommonUtils.spliceStr(uid, app, id));
					}

					if (logger.isDebugEnabled()) {
						logger.debug("uid : " + uid + " addIdToBloom cost time " + (System.currentTimeMillis() - begin) + " end borrowObject=2 to check read");
					}

					acsService.checkAndInsertMul(SysType.USER_READ, resultList, 1000);

					if (logger.isDebugEnabled()) {
						logger.debug("uid: " + uid + " addIdToBloom cost time " + (System.currentTimeMillis() - begin) + " end checkAndInsertMul=3 to check read");
					}

					bSuccess = true;
				} catch (Exception e) {
					logger.error("uid: " + uid + " acsService.existMul failed", e);
					try {
						thriftPool.invalidateObject(tSocket);
						tSocket = null;
					} catch (Exception e1) {
						logger.error("uid: " + uid + " addIdToBloom thriftPool.invalidateObject failed", e1);
					}
				} finally {
					try {
						if (tSocket != null) {
							thriftPool.returnObject(tSocket);
						}
					} catch (Exception e) {
						logger.error("uid: " + uid + " addIdToBloom thriftPool.returnObject failed", e);
					}
				}
			} catch (Exception e) {
				logger.error("uid: " + uid + " addIdToBloom thriftPool.borrowObject failed", e);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("addIdToBloom cost time " + (System.currentTimeMillis() - begin) + " end returnObject=4 to check read");
			}
		} else {
			logger.info(uid + ":" + app + " is empty!");
		}

		return bSuccess;
	}
	
	
	/**
	 * 
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年1月20日 下午8:59:13
	 *  @param idList
	 *  @return
	 */
	public boolean addIdToBloomByExp(String app, String language, List<String> idList) throws Exception {
		long begin = System.currentTimeMillis();

		boolean bSuccess = true;
		if (CollectionUtils.isNotEmpty(idList)) {
			TSocket tSocket = null;
			try {
				tSocket = thriftPool.borrowObject();
				try {
					AcsService.Client acsService = new AcsService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));

					if (logger.isDebugEnabled()) {
						logger.debug("addIdToBloomByExp cost time " + (System.currentTimeMillis() - begin) + " begin=1 to check read");
					}

					List<String> resultList = new ArrayList<String>();
					for (String id : idList) {
						resultList.add(CommonUtils.spliceStr(app, language, id));
					}

					if (logger.isDebugEnabled()) {
						logger.debug("addIdToBloomByExp cost time " + (System.currentTimeMillis() - begin) + " end borrowObject=2 to check read");
					}

					acsService.checkAndInsertMul(SysType.USER_READ, resultList, 1000);

					if (logger.isDebugEnabled()) {
						logger.debug("addIdToBloomByExp cost time " + (System.currentTimeMillis() - begin) + " end checkAndInsertMul=3 to check read");
					}

					bSuccess = true;
				} catch (Exception e) {
					logger.error("acsService.existMul failed", e);
					try {
						thriftPool.invalidateObject(tSocket);
						tSocket = null;
					} catch (Exception e1) {
						logger.error("addIdToBloomByExp thriftPool.invalidateObject failed", e1);
					}
				} finally {
					try {
						if (tSocket != null) {
							thriftPool.returnObject(tSocket);
						}
					} catch (Exception e) {
						logger.error("addIdToBloomByExp thriftPool.returnObject failed", e);
					}
				}
			} catch (Exception e) {
				logger.error("addIdToBloomByExp thriftPool.borrowObject failed", e);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("addIdToBloomByExp cost time " + (System.currentTimeMillis() - begin) + " end returnObject=4 to check read");
			}
		} else {
			logger.info("id list is empty!");
		}

		return bSuccess;
	}
}
