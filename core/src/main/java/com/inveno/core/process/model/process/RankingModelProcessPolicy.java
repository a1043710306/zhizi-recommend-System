package com.inveno.core.process.model.process;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.model.IModelPolicy;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.thrift.DocInfo;
import com.inveno.thrift.DocInfoReq;
import com.inveno.thrift.DocScore;
import com.inveno.thrift.PredictService;
import com.inveno.thrift.ResponParam;

@Component("rankingModelProcessPolicy")
public class RankingModelProcessPolicy implements IModelPolicy<Boolean>
{
	private Log logger = LogFactory.getLog(RankingModelProcessPolicy.class);

	private static Log poolLogger = LogFactory.getLog("BasePooledObjectFactory");

	private Log logELK = LogFactory.getLog("elklog");

	private Log errLogger = LogFactory.getLog("toErrFile");

	@Resource
	private ObjectPool<TSocket> modelThriftPool;

	@Autowired
	private MonitorLog monitorLog;

	private DocInfo buildDocInfo(ResponParam param) {
		DocInfo docInfo = new DocInfo();
		docInfo.setId(param.getInfoid());
		docInfo.setKeywordRelevance(param.getKeywordScores());
		docInfo.setDocGmp(param.getGmp());
		docInfo.setPublishTime(param.getPublishTime());
		docInfo.setNegativeTitleTagsScore(param.getNegativeTitleTagsScore());
		docInfo.setSource(param.getSource());
		docInfo.setDocCategories(param.getCategories());
		docInfo.setWordCount(param.getWordCount());
		docInfo.setSourceFeedsUrl(param.getSourceFeedsUrl());
		docInfo.setLockscreenGmp(param.getLockscreenGmp());
		docInfo.setContentType(param.getContentType());
		docInfo.setStrategy(param.getStrategy());
		return docInfo;
	}

	private Map<String, List<ResponParam>> prepareStrategyDocumentListMapForContentType(Map<Integer, Map<String, List<ResponParam>>> contenttypeStrategyDocumentListMap, int requestedContentType) {
		Map<String, List<ResponParam>> strategyMap = contenttypeStrategyDocumentListMap.get(requestedContentType);
		if (strategyMap == null) {
			strategyMap = new HashMap<String, List<ResponParam>>();
			contenttypeStrategyDocumentListMap.put(requestedContentType, strategyMap);
		}
		return strategyMap;
	}

	@Override
	public Boolean process(Context context)
	{
		boolean bInvokeGDBTSuccess = true;
		if (CollectionUtils.isNotEmpty(context.getResponseParamList()))
		{
			String strRequestInfo = ContextUtils.toRequestInfoString(context);
			String abtestVersion = context.getAbtestVersion();
			String uid = context.getUid();
			String app = context.getApp();		
			
			long begin = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			{
				logger.debug("[rankingModelProcessPolicy]" + strRequestInfo + ", start handling w/ responseParamList size is " + context.getResponseParamList().size());
			}

			//获取不过主模型的ContentType
			String notRankModelContentTypeStr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_GBDT, "notRankModelContentTypes");
			List<String> notRankModelContentTypes = new ArrayList<String>();
			if (StringUtils.isNotEmpty(notRankModelContentTypeStr) ){
				notRankModelContentTypes = Arrays.asList(notRankModelContentTypeStr.split(";"));
				logger.debug("[rankingModelProcessPolicy]" + strRequestInfo + ", notRankModelContentTypes= " + notRankModelContentTypes);
			}
			//new  DocInfoReq 
			DocInfoReq req = new DocInfoReq();
			req.setConfigid(abtestVersion);
			req.setUid(uid);
			req.setRequest_id(context.getRequestId());

			//网络类型
			String net = null;
			if (context.getZhiziListReq() != null) {
				net = context.getZhiziListReq().getNetwork();
			}
			if (StringUtils.isEmpty(net)) {
				net = "7";
			}

			req.setNetwork(net);

			String positionDesc = ContextUtils.getPositionDesc(context);
			req.setPositionDesc(positionDesc);

			boolean bOutputResultData = Boolean.parseBoolean(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "ifOutputResult"));
			ArrayList<HashMap<String, Object>> alDataInput  = (!bOutputResultData) ? null : new ArrayList<HashMap<String, Object>>();
			ArrayList<HashMap<String, Object>> alDataOutput = (!bOutputResultData) ? null : new ArrayList<HashMap<String, Object>>();
			Map<String, ResponParam> mapContentToData = new HashMap<String, ResponParam>();			
			HashSet<DocInfo> docSetList =  new HashSet<DocInfo>();
			
			
			boolean isRecommendationContentTypeNotRank = false;
			
			List<ResponParam> recommendationList = context.getResponseParamList();
			if(CollectionUtils.isNotEmpty(recommendationList)){
				//逻辑：推荐1000篇的文章来自初选，这些文章是指定一种contentType的;取一条数据的ContentType就知道在不在~【不过主模型的contentType】,如果在则移除
				if(CollectionUtils.isNotEmpty(notRankModelContentTypes) && notRankModelContentTypes.contains(String.valueOf(recommendationList.get(0).getContentType()))){
					logger.debug("[rankingModelProcessPolicy]" + strRequestInfo +",notRankModelContentTypes="+recommendationList.get(0).getContentType());
					isRecommendationContentTypeNotRank = true;
				}else{
					for (ResponParam param : recommendationList)
					{				
						DocInfo docInfo = buildDocInfo(param);
						docInfo.setAppname(app);
						docSetList.add(docInfo);					
						mapContentToData.put(param.getInfoid(), param);
						if (bOutputResultData) {
							HashMap<String, Object> mData = new HashMap<String, Object>();
							mData.put("contentId", param.getInfoid());
							alDataInput.add(mData);
						}
					}
				}
			}
			

			int nMixedStrategyArticleCount = 0;
			Map<String, List<String>> mapContentToTypeWithStrategy = new HashMap<String, List<String>>();
			Map<String, ResponParam> mapMixedStrategyContentToData =  new HashMap<String, ResponParam>();
			Map<Integer, Map<String, List<ResponParam>>> mapContenttypeStartegyDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList();
			
			Map<Integer, Map<String, List<ResponParam>>> mapContenttypeStartegyDocumentListNotRank = new HashMap<Integer, Map<String, List<ResponParam>>>();
			if (MapUtils.isNotEmpty(mapContenttypeStartegyDocumentList)) {
				for (Integer contentType : mapContenttypeStartegyDocumentList.keySet()) {
					
					Map<String, List<ResponParam>> mapStrategyToDocumentList = mapContenttypeStartegyDocumentList.get(contentType);
					//逻辑：混插的文章的contentType在不在~【不过主模型的contentType】,如果在增加不排序的推荐map中
					if(CollectionUtils.isNotEmpty(notRankModelContentTypes) && notRankModelContentTypes.contains(String.valueOf(contentType)) ){
						logger.debug("[rankingModelProcessPolicy]---contentType="+contentType+",notRankModelContentTypes="+notRankModelContentTypes.contains(String.valueOf(contentType))   );
						mapContenttypeStartegyDocumentListNotRank.put(contentType,mapStrategyToDocumentList);
						if(MapUtils.isNotEmpty(mapStrategyToDocumentList)){
							for (Map.Entry<String, List<ResponParam>> entry : mapStrategyToDocumentList.entrySet()) {
								if(CollectionUtils.isNotEmpty(entry.getValue())){
									nMixedStrategyArticleCount += entry.getValue().size();
									logger.debug("[rankingModelProcessPolicy]---contentType="+contentType+",notRankModelContentTypes="+notRankModelContentTypes.contains(String.valueOf(contentType))+" strategy="+entry.getKey()  +", size="+entry.getValue().size()   );
								}
							}
						}						
						continue;
					}
					
//					logger.debug(strRequestInfo + ", contentType=" + contentType + ", mapStrategyToDocumentList.size=" + mapStrategyToDocumentList.size());
					for (Map.Entry<String, List<ResponParam>> entry : mapStrategyToDocumentList.entrySet()) {
						String strategy = entry.getKey();
						for (ResponParam param : entry.getValue()) {
							if (param == null) {
								logger.fatal(strRequestInfo + ", contentType=" + contentType + " strategy=" + strategy + " has null entry in response.");
							}
							String contentId = param.getInfoid();
							DocInfo docInfo = buildDocInfo(param);
							docInfo.setAppname(app);
							docSetList.add(docInfo);
							nMixedStrategyArticleCount++;

							List<String> alTypeWithStrategy = (List<String>)mapContentToTypeWithStrategy.get(contentId);
							if (alTypeWithStrategy == null) {
								alTypeWithStrategy = new ArrayList<String>();
								mapContentToTypeWithStrategy.put(contentId, alTypeWithStrategy);
							}
							alTypeWithStrategy.add(contentType + ":" + strategy);

							String contentIdWithStrategy = contentId + ":" + strategy;
							if (!mapMixedStrategyContentToData.containsKey(contentIdWithStrategy)) {
								mapMixedStrategyContentToData.put(contentIdWithStrategy, param);
							}
						}
					}
				}
			}

			
			req.setDocList(new ArrayList<DocInfo>(docSetList));
			

			if (context.getUserInfo() != null)
			{
				req.setUserInfo(context.getUserInfo());
			}

			monitorLog.addCntLogByProduct(context, MonitorType.GBDT_INVOKE_COUNT);
			TSocket tSocket = null;
			try {
				poolLogger.debug(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.borrowObject");
				tSocket = modelThriftPool.borrowObject();
				try {
					poolLogger.debug(strRequestInfo + ", rankingModelProcessPolicy new PredictService.Client");
					PredictService.Client predictService = new PredictService.Client(new TBinaryProtocol(new TFramedTransport(tSocket)));

					if (logger.isDebugEnabled())
					{
						logger.debug(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.borrowObject spent time is " + (System.currentTimeMillis() - begin));
						logger.debug(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool msg NumActive=" + modelThriftPool.getNumActive() + ", NumIdle=" + modelThriftPool.getNumIdle());
					}

					logger.debug(strRequestInfo + ", rankingModelProcessPolicy gbdt submit size=" + docSetList.size() + " mapContentToData.size=" + mapContentToData.size() + " mapMixedStrategyContentToData.size=" + mapMixedStrategyContentToData.size());
					List<DocScore> resList  = predictService.GetPredictList(req).getScoreList();
					logger.debug(strRequestInfo + ", rankingModelProcessPolicy gbdt return size=" + resList.size());

					long ts_spent = System.currentTimeMillis() - begin;
					monitorLog.addResTimeLogByProduct(context, MonitorType.GBDT_RESPONSE_TIME, ts_spent);

					Map<Integer, Map<String, List<ResponParam>>> resultContenttypeStrategyDocumentListMap = new HashMap<Integer, Map<String, List<ResponParam>>>();
					if (CollectionUtils.isNotEmpty(resList))
					{
						ArrayList<ResponParam> resultList = new ArrayList<ResponParam>();
						for (DocScore docScore : resList)
						{
							String contentId = docScore.getId();
							if (StringUtils.isEmpty(contentId)) {
								continue;
							}
							if (bOutputResultData) {
								HashMap<String, Object> mData = new HashMap<String, Object>();
								mData.put("contentId", contentId);
								mData.put("gbdtScore", docScore.getScore());
								alDataOutput.add(mData);
							}
							if (mapContentToData.containsKey(contentId)) {
								ResponParam param = mapContentToData.get(docScore.getId());
								resultList.add(param);
							}
							if (mapContentToTypeWithStrategy.containsKey(contentId)) {
								List<String> alTypeWithStrategy = (List<String>)mapContentToTypeWithStrategy.get(contentId);
								for (String strTypeWithStrategy : alTypeWithStrategy) {
									String[] s = strTypeWithStrategy.split(":");
									int contentType = Integer.parseInt(s[0]);
									String strategy = s[1];
									String contentIdWithStrategy = contentId + ":" + strategy;
									ResponParam param = mapMixedStrategyContentToData.get(contentIdWithStrategy);
									Map<String, List<ResponParam>> resultMapStrategyToList = prepareStrategyDocumentListMapForContentType(resultContenttypeStrategyDocumentListMap, contentType);
									List<ResponParam> alList = (List<ResponParam>)resultMapStrategyToList.get(strategy);
									if (alList == null) {
										alList = new ArrayList<ResponParam>();
										resultMapStrategyToList.put(strategy, alList);
									}
									alList.add(param);
								}
							}
						}
						
						//如果推荐列表1000的文章类型在【不过主模型的contentType】,那么这些文章不过主模型，就不需要重新赋值；否则就是过完主模型重新赋值
						if(!isRecommendationContentTypeNotRank){ 
							logger.debug("[rankingModelProcessPolicy]" + strRequestInfo + ", isRecommendationContentTypeNotRank= " + false);
							context.setResponseParamList(resultList);	
						}
						
					}
					
					//混插、精品池等策略文章列表，这些文章的文章类型存在【不过主模型的contentType】，那么就需要把这些不过主模型的原有的文map追加在map后面,维持；
					if(MapUtils.isNotEmpty(mapContenttypeStartegyDocumentListNotRank)){
						logger.debug("[rankingModelProcessPolicy]" + strRequestInfo + ", mapContenttypeStartegyDocumentListNotRank size= " + mapContenttypeStartegyDocumentListNotRank.size());
						resultContenttypeStrategyDocumentListMap.putAll(mapContenttypeStartegyDocumentListNotRank);
					}
					context.getRecommendInfoData().setContenttypeStrategyDocumentList(resultContenttypeStrategyDocumentListMap);			
					
				} catch (Exception e) {
					bInvokeGDBTSuccess = false;
					logger.error(strRequestInfo + ", rankingModelProcessPolicy PredictService.GetPredictList failed", e);
					errLogger.error(strRequestInfo + ", rankingModelProcessPolicy PredictService.GetPredictList failed", e);
					monitorLog.addCntLogByProduct(context, MonitorType.GBDT_FAIL_COUNT);
					try {
						modelThriftPool.invalidateObject(tSocket);
						tSocket = null;
					} catch (Exception e1) {
						logger.error(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.invalidateObject failed", e1);
					}
				} finally {
					if (tSocket != null) {
						try {
							poolLogger.debug(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.returnObject");
							modelThriftPool.returnObject(tSocket);
						} catch (Exception e) {
							logger.error(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.returnObject failed", e);
						}
					}
					if (logger.isDebugEnabled())
					{
						logger.debug(strRequestInfo + ", rankingModelProcessPolicy predictService.GetPredictList spent time is " + (System.currentTimeMillis() - begin) + " responseParamList size is " + context.getResponseParamList().size());
					}
				}
			} catch (Exception e) {
				logger.error(strRequestInfo + ", rankingModelProcessPolicy modelThriftPool.borrowObject failed", e);
			}

			if (bOutputResultData) {
				HashMap<String, Object> mDumpLog = new HashMap<String, Object>();
				mDumpLog.put("module", "core");
				mDumpLog.put("event_time", System.currentTimeMillis());
				mDumpLog.put("uid", uid);
				mDumpLog.put("app", app);
				mDumpLog.put("configid", abtestVersion);
				mDumpLog.put("scenario", context.getScenario());
				mDumpLog.put("iostream", "gbdt_input");
				mDumpLog.put("data", alDataInput);
				String strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
				logELK.info(strLogMsg);
				mDumpLog.put("invokeGDBTFailed", (!bInvokeGDBTSuccess));
				mDumpLog.put("iostream", "gbdt_output");
				mDumpLog.put("data", (bInvokeGDBTSuccess) ? alDataOutput : alDataInput);
				strLogMsg = JSON.toJSONString(mDumpLog, new SerializerFeature[] {SerializerFeature.DisableCircularReferenceDetect});
				logELK.info(strLogMsg);
			}

			logger.info("[rankingModelProcessPolicy]" + strRequestInfo + ", end handling w/ responseParamList spent time is " + (System.currentTimeMillis() - begin) + " responseParamList size is " + context.getResponseParamList().size() + " mixedStrategyList articles is " + nMixedStrategyArticleCount);
		}

		return bInvokeGDBTSuccess;
	}
}
