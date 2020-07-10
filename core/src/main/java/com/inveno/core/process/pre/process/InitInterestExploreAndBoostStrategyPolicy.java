package com.inveno.core.process.pre.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.pre.IPrePolicy;
import com.inveno.thrift.ResponParam;

@Component("initInterestExploreAndBoostStrategyPolicy")
public class InitInterestExploreAndBoostStrategyPolicy implements IPrePolicy<List<ResponParam>> {

	private Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private MonitorLog monitorLog;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Override
	public List<ResponParam> process(Context context) throws TException {
		long begin = System.currentTimeMillis();
		int mixedStrategyListCount = 0;		
		int interestBoostListCount = 0;		
		int interestExploreListCount = 0;		
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		if (CollectionUtils.isNotEmpty(context.getResponseParamList()))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("[initInterestExploreAndBoostStrategyPolicy]" + strRequestInfo + ", start handling w/ responseParamList size is " + context.getResponseParamList().size());
			}

			Map<String, String> mapInterestBoostContent = new HashMap<String, String>();
			Map<Integer, List<ResponParam>> mapContenttypeInterestBoostList = context.getRecommendInfoData().getContenttypeInterestBoostList();
			if (MapUtils.isNotEmpty(mapContenttypeInterestBoostList)) {				
				for (Integer contentType : mapContenttypeInterestBoostList.keySet()) {
					List<ResponParam> interestBoostList = mapContenttypeInterestBoostList.get(contentType);
					for (ResponParam param : interestBoostList) {
						String contentId = param.getInfoid();
						mapInterestBoostContent.put(contentId, Strategy.INTEREST_BOOST.getCode());
					}
					logger.debug(strRequestInfo + ", initInterestExploreAndBoostStrategyPolicy contentType= "+contentType+", interestBoostList size=" + interestBoostList.size());
				}
			}

			Map<String, String> mapInterestExploreContent = new HashMap<String, String>();
			Map<Integer, List<ResponParam>> mapContenttypeInterestExploreList = context.getRecommendInfoData().getContenttypeInterestExploreList();
			if (MapUtils.isNotEmpty(mapContenttypeInterestExploreList)) {
				
				for (Integer contentType : mapContenttypeInterestExploreList.keySet()) {
					List<ResponParam> interestExploreList = mapContenttypeInterestExploreList.get(contentType);
					for (ResponParam param : interestExploreList) {
						String contentId = param.getInfoid();
						mapInterestExploreContent.put(contentId, Strategy.INTEREST_EXPLORING.getCode());
					}
					logger.debug(strRequestInfo + ", initInterestExploreAndBoostStrategyPolicy contentType= "+contentType+", interestExploreList size=" + interestExploreList.size());
				}
				
			}

			
			
			if(MapUtils.isNotEmpty(mapInterestBoostContent) && MapUtils.isNotEmpty(mapInterestBoostContent)){
				
				List<ResponParam> recommendationList = context.getResponseParamList();
				for (ResponParam param : recommendationList)
				{
					String contentId = param.getInfoid();
					if (mapInterestBoostContent.containsKey(contentId)) {
						param.setStrategy((String)mapInterestBoostContent.get(contentId));
						interestBoostListCount ++;
					} else if (mapInterestExploreContent.containsKey(contentId)) {
						param.setStrategy((String)mapInterestExploreContent.get(contentId));
						interestExploreListCount ++;
					}
				}
				
				context.setResponseParamList(recommendationList);
				
			}
			
			if(context.getRecommendInfoData().getContenttypeStrategyDocumentList() != null ){
				 Map<Integer, Map<String, List<ResponParam>>>  mapContenttypeStartegyDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList();
				
				for (Integer contentType : mapContenttypeStartegyDocumentList.keySet()) {
					if(MapUtils.isNotEmpty(mapContenttypeStartegyDocumentList.get(contentType)) ){
						for (Map.Entry<String, List<ResponParam>> entry : mapContenttypeStartegyDocumentList.get(contentType).entrySet()) {
							mixedStrategyListCount += entry.getValue().size();							
						}
					}
					
				}
			}
			
		
		}
		monitorLog.addResTimeLogByProduct(context, MonitorType.INIT_INTEREST_EXPLORE_AND_BOOST_STRATEGY_RESPONSE_TIME,(System.currentTimeMillis() - begin));
		logger.info("[initInterestExploreAndBoostStrategyPolicy]" + strRequestInfo + ", end handling w/ responseParamList spent time is " + (System.currentTimeMillis() - begin) 
				+ " responseParamList size is " + context.getResponseParamList().size() + " allContentTypeMixedStrategyList articles is "
				+ mixedStrategyListCount  + ", interestBoostList size= " + interestBoostListCount + ", interestExploreListCount size= " + interestExploreListCount);
		return null;
	}


	/**
	 * @throws Exception
	 *
	 * readedFilter(已读过滤)
	 * @Title: readedFilter
	 * @param @param list
	 * @param @return    设定文件
	 * @return List<String>    返回类型
	 * @throws
	 */
	private void readedFilter(Context context) throws Exception{
		String uid = context.getUid();
		long cur = System.currentTimeMillis();

		List<ResponParam> responseParamList = context.responseParamList;

		List<ResponParam> reList = new ArrayList<ResponParam>();

		if (responseParamList.size() >= 500) {
			if (logger.isDebugEnabled()) {
				logger.debug("uid is: "+ uid +" ,begin readFile ReadedFileterPolicy,and time is " + (System.currentTimeMillis()-cur) +",and size is " + 500 +",and cur: " + System.currentTimeMillis() );
			}
			reList.addAll(new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(0,499 ))));
			if (reList.size() < context.getNum()) {
				if (logger.isDebugEnabled()) {
					logger.debug("uid is: "+ uid +" ,begin readFile ReadedFileterPolicy,and time is " + (System.currentTimeMillis()-cur) +",and size is " + reList.size() +",and cur: " + System.currentTimeMillis() );
				}

				if (responseParamList.size() > 1500) {
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(500,1500 ))));
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(1500,responseParamList.size() ))));
				} else {
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(500,responseParamList.size() ))));
				}
			}
		} else {
			reList = readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList);
		}

		logger.info("uid is: "+ uid +" ,end readFile ReadedFileterPolicy, moudle=ReadedFileterPolicy,filterList size= = "+responseParamList.size()+",and time is " + (System.currentTimeMillis()-cur) +",and size is " + reList.size() +",and cur: " + System.currentTimeMillis() );

		if (reList.size() < context.getNum()) {
			monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT_RECREADED);
		}

		//logger.info(" end ================PreProcessImpl readedFilter====================time is " + (System.currentTimeMillis() - cur) +",and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(reList);
	}
}
