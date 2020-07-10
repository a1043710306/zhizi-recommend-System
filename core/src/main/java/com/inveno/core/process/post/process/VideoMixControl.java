package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Strategy;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

@Component("videoMixControl")
public class VideoMixControl implements IPostPolicy<List<ResponParam>>{
	 
	private Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private MonitorLog monitorLog;

	@Override
	public List<ResponParam> process(Context context) {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();
		
		if (!ContextUtils.isForYouChannel(context)) {
			return null;
		}
		List<ResponParam> responseList = context.getResponseParamList();
		if (CollectionUtils.isEmpty(responseList) ){
			return null;
		}
		try {
			responseList = reRank(context,responseList);
			long end  = System.currentTimeMillis();
			monitorLog.addResTimeLogByProduct(context, MonitorType.VIDEOMIX_RESPONSE_TIME, (end - cur));
			logger.debug(strRequestInfo + " end VideoMixControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
			context.setResponseParamList(responseList);
		} catch (Exception e) {
			logger.error(" uid :"+ uid  + " ,app "+  app +"  SimHashControl Exception,time is,and cur = " + System.currentTimeMillis() +e.getCause(),e );
		}
//		//remove--bug tracking yezi 2018年3月30日18:15:26		
//			logger.debug(" uid: " + uid + " ,app " + app + " end VideoMixControl , abtestVersion is "	+ abtest + "  , time is " + (System.currentTimeMillis())
//					+ ",size is " + context.getResponseParamList().size()	+ " ,and cur = " + System.currentTimeMillis() + " , after list " + responseList);
		return null;
	}
//	@Override
	public List<ResponParam> processOld(Context context) {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();
		
		if (!ContextUtils.isForYouChannel(context)) {
			return null;
		}

		List<ResponParam> videoList = new ArrayList<ResponParam>();
		if (null != context.getRecommendInfoData()) {
			Map<String, List<ResponParam>> mapStrategyToDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.VIDEO.getValue());
			if (MapUtils.isNotEmpty(mapStrategyToDocumentList)) {
				for (Map.Entry<String, List<ResponParam>> entry : mapStrategyToDocumentList.entrySet()) {
					videoList.addAll( entry.getValue() );
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(strRequestInfo + " begin VideoMixControl ,time is " +(System.currentTimeMillis()) +",size is " + context.getResponseParamList().size() +"  ,and video size is "+ videoList.size() +" ,and cur = " + System.currentTimeMillis()  );
		}
 		
 		List<ResponParam> responseList = context.getResponseParamList();
		
		if (CollectionUtils.isEmpty(responseList) ||  CollectionUtils.isEmpty(videoList)){
			return null;
		}
		
		
		int videoMixFrequence   = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "videoMixFrequence"), 5);
		int beginOffsetForVideo = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "beginOffsetForVideo"), 0);

		int pos = beginOffsetForVideo;
		while (videoList.size() > 0 && responseList.size() > pos) {
			ResponParam param = videoList.get(0);
			if (param == null) {
				logger.fatal(strRequestInfo + ", contentType=" + ContentType.VIDEO.getValue() + " has null entry in response.");
			}
			param.setStrategy(Strategy.MIXED_INSERT_VIDEOBOOST.getCode());
			responseList.add(pos, param);
			videoList.remove(0);
//			//remove ---日志 yezi  2018年3月30日19:16:47
//			logger.debug(strRequestInfo +"add VideoMixControl--while-list:pos="+pos+"-list="+param.getContentType()+",Strategy=" +param.getStrategy()+",contentId=" +param.getInfoid());	
//			//remove ---日志 yezi  2018年3月30日19:16:47
			pos += videoMixFrequence; 
		}
		
		
	
		long end  = System.currentTimeMillis();
		monitorLog.addResTimeLogByProduct(context, MonitorType.VIDEOMIX_RESPONSE_TIME, (end - cur));
		logger.debug(strRequestInfo + " end VideoMixControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(responseList);
		
//		//remove ---日志 yezi  2018年3月30日19:16:47
		for(int i = 0;i< responseList.size();i++){
			ResponParam param = responseList.get(i);
			logger.debug(strRequestInfo +"add VideoMixControl---list:ContentType="+param.getContentType()+",Strategy=" +param.getStrategy()+",contentId=" +param.getInfoid());		
			if(i>100){
				break;
			}
		}
//		//remove ---日志 yezi  2018年3月30日19:16:47
		return null;
	}
	
	
	public static List<ResponParam> reRank(Context context,List<ResponParam> inlist) {
		
		
	    ArrayList<ResponParam> list = (ArrayList<ResponParam>)inlist;
	    @SuppressWarnings("unchecked")
		ArrayList<ResponParam> result = (ArrayList<ResponParam>) list.clone();
		
		List<ResponParam> videoList = new ArrayList<ResponParam>();
		if (null != context.getRecommendInfoData()) {
			Map<String, List<ResponParam>> mapStrategyToDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.VIDEO.getValue());
			if (MapUtils.isNotEmpty(mapStrategyToDocumentList)) {
				for (Map.Entry<String, List<ResponParam>> entry : mapStrategyToDocumentList.entrySet()) {
					videoList.addAll( entry.getValue() );
				}
			}
		}
	
		
		if (CollectionUtils.isEmpty(result) ||  CollectionUtils.isEmpty(videoList)){
			return result;
		}
		int videoMixFrequence   = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "videoMixFrequence"), 5);
		int beginOffsetForVideo = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "beginOffsetForVideo"), 0);

		int pos = beginOffsetForVideo;
		while (videoList.size() > 0 && result.size() > pos) {
			ResponParam param = videoList.get(0);
			
			param.setStrategy(Strategy.MIXED_INSERT_VIDEOBOOST.getCode());
			result.add(pos, param);
			videoList.remove(0);
			pos += videoMixFrequence; 
		}
		
		return result;
	}
}
