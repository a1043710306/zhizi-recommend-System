package com.inveno.core.process.cms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inveno.common.bean.Context;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanResult;
/**
 * CMS交互的接口
 *  Class Name: CMSInterface.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年6月6日 下午6:33:02 
 *  @company inveno 
 *  @version 1.0
 */
@Component("cMSInterface")
public class CMSInterface {
	
	private Log logger = LogFactory.getLog("cmslog");
	
	@Autowired
	private JedisCluster jedisCluster;
	
	//保存channel对应的categoryId
	//key zh_cn::fuyiping-gionee::
	private static Map<String,List<Integer>> channel_category_Map = new ConcurrentHashMap<String,List<Integer>>();
	
	private static Map<String,Integer> channel_contentQuality_Map = new ConcurrentHashMap<String,Integer>();
	
	public static final String CONFIG_MIXED_RULE_MAP_KEY = "config_mixed_rule_map";
	
	private static Map<String, Map<Integer, MixedRule>> highMixRuleMap = new ConcurrentHashMap<String, Map<Integer, MixedRule>>();
	
	private static Map<String,Integer> maxPosOfConfig = new ConcurrentHashMap<String,Integer>();
	
	
	// {categoryId:[1,2],rank:10}
	public synchronized void initScenarioCagetory() throws Exception {

		Map<String, List<Integer>> _channel_category_Map = new ConcurrentHashMap<String, List<Integer>>();
		
		Map<String, Integer> _channel_contentQuality_Map = new ConcurrentHashMap<String, Integer>();

		String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
		boolean cycleIsFinished = false;

		String json = "";
		List<Integer> categoryIds = null;
		JSONObject jsonObject = null;
		//int rank = 0;
		while (!cycleIsFinished) {
			ScanResult<Entry<String, String>> scanResult = jedisCluster.hscan(Constants.SCENARIO_CATEGORYID_MAP, cur);
			List<Entry<String, String>> result = scanResult.getResult();

			// do whatever with the key-value pairs in result
			for (Entry<String, String> entry : result) {

				try {
					categoryIds = new ArrayList<Integer>();
					json = entry.getValue();
					if (StringUtils.isEmpty(json)) {
						logger.warn("init scenario happen warn,the key " + entry.getKey() + " value is null");
						continue;
					}
					logger.info("init scenario key " + entry.getKey() + " value is " + json);
					jsonObject = JSONObject.parseObject(json);
					categoryIds = JSON.parseArray(jsonObject.getJSONArray("categoryId").toString(), Integer.class);
					_channel_category_Map.put(entry.getKey(), categoryIds);
					
					Object contentQuality = jsonObject.get("contentQuality");
					if( null != contentQuality ){
						_channel_contentQuality_Map.put(entry.getKey(), Integer.parseInt(contentQuality.toString()));
					}else{
						_channel_contentQuality_Map.put(entry.getKey(), 0);
					}
				} catch (Exception e) {
					logger.error(" initScenarioCagetory happen error ,and  value is" + json , e);
				}

			}

			cur = scanResult.getStringCursor();
			if (cur.equals("0")) {
				cycleIsFinished = true;
			}
		}
		
		channel_category_Map = _channel_category_Map;
		channel_contentQuality_Map = _channel_contentQuality_Map;
	}

	public void initConfigMixedRuleMap() {
		Map<String, Map<Integer, MixedRule>> map = new ConcurrentHashMap<String, Map<Integer, MixedRule>>();
		String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
		boolean cycleIsFinished = false;

		while (!cycleIsFinished) {
			// key='config_mixed_rule_map'; field = "noticias:Spanish:list:scenario:65792:254"; value = $ruleJson
//			 "[{\"rule_id\":0,\"source\":[[0],[213]],\"ratio\":[0,1],\"range\":[0]},
//				{\"rule_id\":1,\"source\":[[0],[212]],\"ratio\":[0.5,0.5],\"range\":[1,2,3,4,5,6,7,8,9]},"
//			  + "{\"rule_id\":2,\"source\":[[0],[211],[19],[203],[204]],\"ratio\":[0.4,0.2,0.2,0.1,0.1],\"range\":[10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29]}]"
			//
			ScanResult<Entry<String, String>> scanResult = jedisCluster.hscan(CONFIG_MIXED_RULE_MAP_KEY, cur);
			List<Entry<String, String>> result = scanResult.getResult();
			for (Entry<String, String> entry : result) {
				
				try {
					String configId = entry.getKey();
					String json = entry.getValue();
					List<String> list = JSON.parseArray(json, String.class);
					
					Map<Integer, MixedRule> posToRuleMap = new HashMap<Integer, MixedRule>();
					for (int i = 0; i < list.size(); i++) {
						MixedRule mixRule = JSON.parseObject(list.get(i),MixedRule.class);
						List<Double> ratioList = mixRule.getRatio();
						for (int j = 0; j < ratioList.size(); j++) {
							if( ratioList.size()>=2 ){
								if(j>=1){
									ratioList.set(j, ratioList.get(j-1) + ratioList.get(j));
								}
							}
						}
						mixRule.setRatio(ratioList);
						for (int j = 0; j < mixRule.getRange().size(); j++) {
							posToRuleMap.put(mixRule.getRange().get(j), mixRule);
						}
					}
					
					map.put(configId, posToRuleMap);
				} catch (Exception e) {
					logger.error(" initConfigMixedRuleMap Exception  ",e);
				}
				
			}
			cur = scanResult.getStringCursor();
			if (cur.equals("0")) {
				cycleIsFinished = true;
			}
		}
		highMixRuleMap = map;
		logger.info("cms get advanced mix rule :"+highMixRuleMap.size());
		
		initMaxPosOfConfig(highMixRuleMap);
	}
	
	private void initMaxPosOfConfig(Map<String, Map<Integer, MixedRule>> highMixRuleMap2) {
		Set<String> configSet = highMixRuleMap2.keySet();
		for (String config : configSet) {
			try {
				List<Integer> posList = new ArrayList<Integer>(highMixRuleMap2.get(config).keySet());
	 			if( CollectionUtils.isNotEmpty(posList) ){
	 				Collections.sort(posList);
	 				maxPosOfConfig.put(config ,posList.get(posList.size()-1));
	 			}
			} catch (Exception e) {
				logger.error("maxPosOfConfig :" ,e);
			}
		}
		
		logger.info("maxPosOfConfig :" +maxPosOfConfig);
	}


	/**
	 * 初始化需要缓存的scenarioKey
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月11日 下午4:12:22
	 *  @param scenariokey
	 */
	@SuppressWarnings("unused")
	private void initCacheScenario(String scenariokey, Map<String, List<String>> _cache_scenario_map) {
		String dealScenario = scenariokey.substring(0, scenariokey.lastIndexOf("_"));
		String productKey = dealScenario.substring(0, dealScenario.lastIndexOf("_"));
		
		if (_cache_scenario_map.containsKey(productKey)) {
			List<String> cacheScenarioKeyList = _cache_scenario_map.get(productKey);
			cacheScenarioKeyList.add(scenariokey);
		} else {
			List<String> cacheScenarioKeyList = new ArrayList<String>();
			cacheScenarioKeyList.add(scenariokey);
			_cache_scenario_map.put(productKey, cacheScenarioKeyList);
		}
	}
	
	public List<Integer> getCategoryIdListFromCMS(Context context) {
		String interfaceName = ContextUtils.getInterfaceName(context);
		String semantic = ContextUtils.getSemantic(context);
		
		String key = context.getApp() + ":" + context.getLanguage() + ":"+ interfaceName +":"+ semantic + ":" + context.getScenario();
		List<Integer> list = channel_category_Map.get(key);

		return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
	}
	
	/**
	 * 获取ContentQuality
	 * @param context
	 * @return
	 */
	public Integer getContentQualityFromCMS(Context context) {
		String interfaceName = ContextUtils.getInterfaceName(context);
		String semantic = ContextUtils.getSemantic(context);
		
		String key = context.getApp() + ":" + context.getLanguage() + ":"+ interfaceName +":"+ semantic + ":" + context.getScenario();
		return channel_contentQuality_Map.get(key);
	}

	public Map<Integer, MixedRule> getHighMixRule(Context context){
		Map<Integer, MixedRule> mixRule =  highMixRuleMap.get(context.getAbtestVersion());
		if( MapUtils.isEmpty(mixRule) ){
			String interfaceName = ContextUtils.getInterfaceName(context);
			String semantic = ContextUtils.getSemantic(context);
			
			String key = new StringBuffer(context.getApp()).append(":").append( context.getLanguage()).append(":").append(interfaceName)
			.append(":").append(semantic).append(":").append(context.getScenario()).append(":").append(context.getAbtestVersion()).toString();
			mixRule = highMixRuleMap.get(key);
		}
		return mixRule;
	}
	
	public int getMaxPosOfHighMix(Context context){
		Integer maxPos = maxPosOfConfig.get(context.getAbtestVersion());
		
		if (maxPos == null || maxPos == 0) {
			String interfaceName = ContextUtils.getInterfaceName(context);
			String semantic = ContextUtils.getSemantic(context);

			String key = new StringBuffer(context.getApp()).append(":").append( context.getLanguage()).append(":").append(interfaceName)
			.append(":").append(semantic).append(":").append(context.getScenario()).append(":").append(context.getAbtestVersion()).toString();
			maxPos = maxPosOfConfig.get(key);
		}
		return maxPos;
	}
}
