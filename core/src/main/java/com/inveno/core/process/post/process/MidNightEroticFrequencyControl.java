package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.thrift.ResponParam;

@Component("midNightEroticFrequencyControl")
public class MidNightEroticFrequencyControl implements IPostPolicy<List<ResponParam>>{
	
	private Log logger = LogFactory.getLog(this.getClass());
	public static String CONFIG_CORE_ADULTCONTROL = "adultControl";
	@Override
	public List<ResponParam> process(Context context) {

		long cur = System.currentTimeMillis();
		String app = context.getApp();
		String lan = context.getLanguage();
		String abtest = context.getAbtestVersion();
		String uid = context.getUid();

		if(logger.isDebugEnabled()){
			logger.debug(" uid: "+ uid  + " ,app "+  app +" begin midNightEroticFrequencyControl ,time is " +(System.currentTimeMillis()) +",size is " + context.getResponseParamList().size() +" ,and cur = " + System.currentTimeMillis()  );
		}

		List<ResponParam> responseList =  context.getResponseParamList();
		List<ResponParam> resultList = new ArrayList<ResponParam>();
		List<ResponParam> adultList = new ArrayList<ResponParam>();

		String strDefaultProduct2TimeZone = "{\"hotodayHindi\":6,\"mata\":6,\"noticiasSpanish\":-6}";
		String strProduct2TimeZone = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "product2TimeZone");
		if (StringUtils.isEmpty(strProduct2TimeZone)) {
			strProduct2TimeZone = strDefaultProduct2TimeZone;
		}

		Date date = new Date(System.currentTimeMillis()); // 2014-1-31 21:20:50
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"+JSON.parseObject(strProduct2TimeZone, Map.class).get(app+lan)));
		// 或者可以 Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);

		double adultScoreIntervalLower = 0.6;
		double adultScoreIntervalUpper = 0.8;
		int adultScoreSpaceDistance = 3;
		int adultScoreStartPosition = 10;
		boolean isMidNight = false;
		boolean isVideoChannel = ContextUtils.isVideoChannel(context);

		int midNightBeginTime = NumberUtils.toInt(context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNightBeginTime"), 21);
		int midNightEndTime   = NumberUtils.toInt(context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNightEndTime"), 6);

		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (hourOfDay >= midNightBeginTime || hourOfDay <= midNightEndTime) {
			adultScoreIntervalLower = NumberUtils.toDouble(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNighadultScoreIntervalLower"),0.6d);
			adultScoreIntervalUpper = NumberUtils.toDouble(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNighadultScoreIntervalUpper"),0.8d);
			adultScoreStartPosition = NumberUtils.toInt(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNighadultScoreStartPosition"), 4);
			adultScoreSpaceDistance = NumberUtils.toInt(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "midNighadultScoreSpaceDistance"), 5);
			isMidNight = true;
		} else {
			adultScoreIntervalLower = NumberUtils.toDouble(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "adultScoreIntervalLower"), 0.6d);
			adultScoreIntervalUpper = NumberUtils.toDouble(
					context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "adultScoreIntervalUpper"), 0.8d);
			adultScoreStartPosition = NumberUtils
					.toInt(context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "adultScoreStartPosition"), 4);
			adultScoreSpaceDistance = NumberUtils
					.toInt(context.getComponentConfiguration(CONFIG_CORE_ADULTCONTROL, "adultScoreSpaceDistance"), 9);
			isMidNight = false;
		}
		if (logger.isInfoEnabled()) {
			logger.info("adultScoreIntervalLower = " + adultScoreIntervalLower);
			logger.info("adultScoreIntervalUpper = " + adultScoreIntervalUpper);
			logger.info("adultScoreStartPosition = " + adultScoreStartPosition);
			logger.info("adultScoreSpaceDistance = " + adultScoreSpaceDistance);
		}
		for (ResponParam resp : responseList) {
			if( null != resp ){
				if( isMidNight ){
					if( resp.getAdultScore() >= adultScoreIntervalLower ){
						if( resp.getAdultScore() < adultScoreIntervalUpper ){
							resp.setStrategy(Strategy.DOWNGRADE_SCORE_061.getCode());
						}else{
							resp.setStrategy(Strategy.DOWNGRADE_SCORE_081.getCode());
						}
						adultList.add(resp);
					}else{
						resultList.add(resp);
					}
				}else{
					if( resp.getAdultScore() >= adultScoreIntervalLower  && resp.getAdultScore() < adultScoreIntervalUpper ){
						resp.setStrategy(Strategy.DOWNGRADE_SCORE_061.getCode());
						adultList.add(resp);
					}else if ( resp.getAdultScore() < adultScoreIntervalLower ){
						resultList.add(resp);
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(" uid :"+ uid  + " ,app "+  app +"  midNightEroticFrequencyControl ,time is " +(System.currentTimeMillis()-cur) +",adultList size is " + adultList.size() +" isMidNight " + isMidNight +" hourOfDay is "+ hourOfDay +" ,and cur = " + System.currentTimeMillis()  );
		}
		int pos = adultScoreStartPosition ;
		for ( ; adultList.size() > 0 &&   resultList.size() > pos ; ) {
			resultList.add(pos, adultList.get(0));
			adultList.remove(0);
			//pos += adultScore2EroticFre +  i ;
			pos += adultScoreSpaceDistance  ;
		}

		resultList.addAll(adultList);

		long end  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end midNightEroticFrequencyControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(resultList);
		//System.out.println("===========" + context.getResponseParamList() );

		return null;
	}
}
