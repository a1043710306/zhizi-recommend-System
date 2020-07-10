package com.inveno.fallback.service.impl;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisCluster;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.es.FallbackES;
import com.inveno.fallback.model.GmpAndDwelltimeEntry;
import com.inveno.fallback.service.FallbackService;
import com.inveno.fallback.util.ElasticSearchUtil;
import com.inveno.fallback.util.FastJsonConverter;

@Component
public class EsUpdateServiceImpl implements FallbackService {

	private static final Logger logger = LoggerFactory.getLogger(EsUpdateServiceImpl.class);
	
	@Autowired
	private JedisCluster jedisCluster;
	
	@Autowired
	private FallbackES fallbackESImpl;
	
	private Client client = ElasticSearchUtil.getInstance().getClient();
	
	public String handler() {
		// TODO Auto-generated method stub
		logger.debug("start es select and update process...");
		
		try{
			
	 		SearchResponse response = fallbackESImpl.getEsContents(new String[]{});
	 		
			for(SearchHit hit : response.getHits().getHits()){
				String contentId = hit.getId();
			    	
				//Dwelltime API调用
				String gmpAndDwellValue = jedisCluster.hget(FallbackConstant.REDIS_KEY_GMPDWELLTIME_VALUE ,contentId);
				logger.debug("get gmp and dwelltime value="+gmpAndDwellValue+",by contentId="+contentId);
				if(gmpAndDwellValue == null){
					continue;
				}
				//获取GMP和dwelltime的值
				//GmpAndDwelltimeEntry gmpAndDwelltimeModel = FastJsonConverter.readValue(GmpAndDwelltimeEntry.class, gmpAndDwellValue);
				
				GmpAndDwelltimeEntry gmpAndDwelltimeModel = new GmpAndDwelltimeEntry();
				//获取GMP和dwelltime的值
				@SuppressWarnings("unchecked")
	    		List<Map<String, Map<String, Object>>> listMaps = (List<Map<String, Map<String, Object>>>) FastJsonConverter.readValue(List.class, gmpAndDwellValue);
	    		for(Map<String, Map<String, Object>> listMap : listMaps){
	    			Map<String,Object> map = listMap.get("-1");
	    			if(map != null){
	    				gmpAndDwelltimeModel.setDwelltime(map.get("dwelltime"));
	    				gmpAndDwelltimeModel.setGmp(map.get("ctr"));
	    			}
	    		}
				
				//在ES中添加GMP和dwelltime的值
				Map<String,Object> esSourceMap = hit.getSource();
				logger.debug("maps:"+esSourceMap.toString());
				esSourceMap.put(FallbackConstant.REDIS_KEY_GMP_VALUE, gmpAndDwelltimeModel.getGmp());
				esSourceMap.put(FallbackConstant.REDIS_KEY_DWELLTIME_VALUE, gmpAndDwelltimeModel.getDwelltime());
				UpdateRequest updateRequest = new UpdateRequest(FallbackConstant.ES_SEARCHE_INDEX, FallbackConstant.ES_SEARCHE_TYPE, contentId).doc(esSourceMap);
		        client.update(updateRequest).get();
				
			}
		}catch(Exception e){
			logger.error("EsUpdateService exception:",e);
		}
		
		return "suceess";
	}

}
