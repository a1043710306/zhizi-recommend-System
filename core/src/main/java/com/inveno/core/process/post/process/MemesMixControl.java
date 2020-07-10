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

@Component("memesMixControl")
public class MemesMixControl implements IPostPolicy<List<ResponParam>>{
	 
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
		
		if (!ContextUtils.isLockscreen(context)) {
			return null;
		}

		List<ResponParam> mixedList = new ArrayList<ResponParam>();
		if (null != context.getRecommendInfoData()) {
			Map<String, List<ResponParam>> mapStrategyToDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.MEME.getValue());
			if (MapUtils.isNotEmpty(mapStrategyToDocumentList)) {
				for (Map.Entry<String, List<ResponParam>> entry : mapStrategyToDocumentList.entrySet()) {
					mixedList.addAll( entry.getValue() );
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(strRequestInfo + " | begin MemesMixControl ,time is " +(System.currentTimeMillis()) +",size is " + context.getResponseParamList().size() +"  ,and memes size is "+ mixedList.size() +" ,and cur = " + System.currentTimeMillis()  );
		}
 		
 		List<ResponParam> responseList =  context.getResponseParamList();
		
		if (CollectionUtils.isEmpty(responseList) ||  CollectionUtils.isEmpty(mixedList)) {
			return null;
		}
		
		
		int memesMixFrequence   = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "memesMixFrequence"), 5);
		int beginOffsetForMemes = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "beginOffsetForMemes"), 0);

		int pos = beginOffsetForMemes;
		while (mixedList.size() > 0 && responseList.size() > pos) {
			ResponParam param = mixedList.get(0);
			if (param == null) {
				logger.fatal(strRequestInfo + " | contentType=" + ContentType.MEME.getValue() + " has null entry in response.");
			}
			param.setStrategy(Strategy.MIXED_INSERT_MEMESBOOST.getCode());
			responseList.add(pos, param);
			mixedList.remove(0);
			pos += memesMixFrequence;
		}

		long end  = System.currentTimeMillis();
		monitorLog.addResTimeLogByProduct(context, MonitorType.MEMESMIX_RESPONSE_TIME, (end - cur));
		logger.info(strRequestInfo + " | end MemesMixControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(responseList);
		return null;
	}
}
