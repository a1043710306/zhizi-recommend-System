package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

@Component("eroticFrequencyControl")
public class EroticFrequencyControl implements IPostPolicy<List<ResponParam>>{
	private Log logger = LogFactory.getLog(this.getClass());

	@Override
	public List<ResponParam> process(Context context) {

		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();
		if(logger.isDebugEnabled()){
			logger.debug(" uid: "+ uid  + " ,app "+  app +" begin EroticFrequencyControl ,time is " +(System.currentTimeMillis()) +",size is " + context.getResponseParamList().size() +" ,and cur = " + System.currentTimeMillis()  );
		}
 
		List<ResponParam> responseList =  context.getResponseParamList();
		List<ResponParam> resultList = new ArrayList<ResponParam>();
		List<ResponParam> adultList = new ArrayList<ResponParam>();

		if (CollectionUtils.isEmpty(responseList) ){
			return null;
		}

		double adultScore2Erotic = NumberUtils.toDouble(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "adultScore2Erotic"), 0.6d);
		int adultScore2EroticFre = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "adultScore2EroticFre"), 3);
		int begingOffsetToErotic = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "begingOffsetToErotic"), 10);

		for (ResponParam resp : responseList) {
			if (null != resp) {
				if (resp.getAdultScore() > adultScore2Erotic) {
					adultList.add(resp);
				} else {
					resultList.add(resp);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug(" uid :"+ uid  + " ,app "+  app +"  EroticFrequencyControl ,time is " +(System.currentTimeMillis()-cur) +",adultList size is " + adultList.size() +" ,and cur = " + System.currentTimeMillis()  );
		}
		int pos = begingOffsetToErotic ;
		for ( ; adultList.size() > 0 &&   resultList.size() > pos ; ) {
			resultList.add(pos, adultList.get(0));
			adultList.remove(0);
			//pos += adultScore2EroticFre +  i ; 
			pos += adultScore2EroticFre  ; 
		}

		resultList.addAll(adultList);

		long end  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end EroticFrequencyControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(resultList);
		//System.out.println("===========" + context.getResponseParamList() );
		 
		return null;
	}


}
