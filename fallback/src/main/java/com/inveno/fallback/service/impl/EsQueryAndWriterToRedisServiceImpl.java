package com.inveno.fallback.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.calc.ScoreCalc;
import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.es.FallbackES;
import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.qconf.FallbackQconf;
import com.inveno.fallback.redis.FallbackRedis;
import com.inveno.fallback.server.DataBaseContentLoader;
import com.inveno.fallback.server.PropertyContentLoader;
import com.inveno.fallback.service.FallbackService;

@Component
public class EsQueryAndWriterToRedisServiceImpl implements FallbackService {

	private static final Logger logger = LoggerFactory.getLogger(EsQueryAndWriterToRedisServiceImpl.class);
	
	@Autowired
	private ScoreCalc scoreCalcImpl;
	
	@Autowired
	private FallbackQconf fallbackQconfImpl;
	
	@Autowired
	private FallbackES fallbackESImpl;
	
	@Autowired
	private FallbackRedis scoreFallbackRedisImpl;
	
	List<MessageContentEntry> allCategoryidList = new ArrayList<MessageContentEntry>();
	
	public String handler() {
		return null;/*
		// TODO Auto-generated method stub
		logger.debug("start search es system and write redis process...");
		
		try{
			
			List<Integer> categoryIds = DataBaseContentLoader.getValues(FallbackConstant.T_CATEGORYID_VALUES);
			logger.debug("categoryids :"+categoryIds);
			
			//Qconf API调用
			Double s1 = fallbackQconfImpl.getValue(PropertyContentLoader.getValues(FallbackConstant.QCONF_S1_VALUE));
			Double s2 = fallbackQconfImpl.getValue(PropertyContentLoader.getValues(FallbackConstant.QCONF_S2_VALUE));

			//渠道FIRM_APP和多语言版本
			String[] channelLanguages =  PropertyContentLoader.getValues(FallbackConstant.FIRM_APP_LANGUAGES).split(",");
			
			//多语言
			//String[] languages =  PropertyContentLoader.getValues(FallbackConstant.LANGUAGE).split(",");
				
				for(String channelLanguage : channelLanguages){
					String[] realChlLangs = channelLanguage.split("="); 
					String channel = realChlLangs[0];
					String[] languages = realChlLangs[1].split(":");
					for(String language : languages){
					
					String keyAll = FallbackConstant.REDIS_FALLBACK_VALUE+FallbackConstant.BLANK_VALUE+channel+FallbackConstant.BLANK_VALUE+
							language+FallbackConstant.BLANK_VALUE+FallbackConstant.REDIS_ALL_VALUE;
					for(Integer categoryId : categoryIds){
						
						String keyCategoryid = FallbackConstant.REDIS_FALLBACK_VALUE+FallbackConstant.BLANK_VALUE+channel+FallbackConstant.BLANK_VALUE+categoryId;
						
						List<MessageContentEntry> messageContentModels = fallbackESImpl.getEsContents(channel,categoryId,language,
								Double.valueOf(PropertyContentLoader.getValues(FallbackConstant.GMP_DOUBLE_VALUE_UP)),
								Double.valueOf(PropertyContentLoader.getValues(FallbackConstant.GMP_DOUBLE_VALUE_DOWN)),
								Double.valueOf(PropertyContentLoader.getValues(FallbackConstant.DWELLTIME_DOUBLE_VALUE_UP)),
								Double.valueOf(PropertyContentLoader.getValues(FallbackConstant.DWELLTIME_DOUBLE_VALUE_DOWN)));
						
						messageContentModels = processHandler(messageContentModels,s1,s2);//处理GMP和DWELLTIME
						
						allCategoryidList.addAll(messageContentModels);
						
						scoreFallbackRedisImpl.process(messageContentModels,keyCategoryid,
								Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.REDIS_STORE_CATEGORYID_COUNT)),categoryId.toString());
						
					}
					logger.debug(keyAll+"list:"+allCategoryidList.toString());
					scoreFallbackRedisImpl.process(allCategoryidList,keyAll,Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.REDIS_STORE_ALL_COUNT)),"all");
					allCategoryidList.clear();
				}
			}
			
		}catch(Exception e){
			logger.error("EsQueryAndWriterToRedisServiceImpl exception:",e);
		}
		
		return "success";
	*/}
	
	public List<MessageContentEntry> processHandler(List<MessageContentEntry> messageContentEntryList,Double s1,Double s2){
		if(messageContentEntryList != null && messageContentEntryList.size() > 0){
			for(MessageContentEntry messageContentModel : messageContentEntryList){
				Double gmpValue = messageContentModel.getGmpValue();
				Double dwelltimeValue = messageContentModel.getDwelltimeValue();
				Double score = scoreCalcImpl.caclScorce(s1, s2, dwelltimeValue, gmpValue);
				messageContentModel.setScore(score);
			}
		}
		return messageContentEntryList;
	}
    
}
