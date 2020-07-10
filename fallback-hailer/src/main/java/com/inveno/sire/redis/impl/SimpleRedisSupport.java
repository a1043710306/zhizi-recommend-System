package com.inveno.sire.redis.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.sire.constant.SireConstant;
import com.inveno.sire.redis.IRedis;
import com.inveno.sire.util.FastJsonConverter;

import redis.clients.jedis.JedisCluster;

/**
 * Created by Klaus Liu on 2016/9/20.
 */
@Component
public class SimpleRedisSupport implements IRedis {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRedisSupport.class);

    @Autowired
    private JedisCluster jedisCluster;

    public Map<String,Map<String,Double>> getDatas() {
        logger.debug("SimpleRedisSupport start search redis ... ");
        return segmentationHandlerDatas();
    }

    /**
     * 分段扫描redis
     * @return
     */
    private Map<String,Map<String,Double>> segmentationHandlerDatas(){
        Map<String,Map<String,Double>> scanMap = new HashMap<String,Map<String,Double>>();

        if (jedisCluster.exists(SireConstant.REDIS_KEY_ARTICLE_GMP + "-0") && jedisCluster.exists(SireConstant.REDIS_KEY_ARTICLE_GMP + "-99")) {
            int totalCount = 0;
            int nPartition = 100;
            for (int idx = 0; idx < nPartition; idx++) {
                String strGMPKey = SireConstant.REDIS_KEY_ARTICLE_GMP + "-" + String.valueOf(idx);
                Map<String, String> mapArticlesGMP = jedisCluster.hgetAll(strGMPKey);
                if (MapUtils.isNotEmpty(mapArticlesGMP)) {
                    int subTotalCount = 0;
                    for (Map.Entry<String, String> entry : mapArticlesGMP.entrySet()) {
                        String strArticleID = entry.getKey();
                        String strArticleGMP = entry.getValue();
                        if (StringUtils.isEmpty(strArticleID) || StringUtils.isEmpty(strArticleGMP))
                            continue;

                        subTotalCount++;
                        scanMap.put(strArticleID, parseGmpJsonValue(strArticleGMP));
                    }
                    totalCount += subTotalCount;
                    logger.debug("process redis " + strGMPKey + " expect size = " + mapArticlesGMP.size() + " actual size = " + subTotalCount);
                }
            }
            logger.debug("process redis " +  SireConstant.REDIS_KEY_ARTICLE_GMP + "0-99 total size = " + totalCount);
        } 

        return scanMap;
    }

    //解析GMP key list
    @SuppressWarnings("unused")
	private Map<String,Map<String,Double>> parseGmpJsonValue(List<Map.Entry<String,String>> gmpList){
        Map<String,Map<String,Double>> gmpMap = new HashMap<String,Map<String,Double>>();
        for(Map.Entry<String,String> gmpEntry : gmpList){
            String key = gmpEntry.getKey();
            String value = gmpEntry.getValue();
            if( StringUtils.isEmpty(value) || StringUtils.isEmpty(key)){
                continue;
            }
            Map<String,Double> resultGmpValue = parseGmpJsonValue(gmpEntry.getValue());
            gmpMap.put(key,resultGmpValue);
        }
        return gmpMap;
    }

    //单条解析
    private Map<String,Double> parseGmpJsonValue(String gmpJson){
        Map<String,Double> channelMap = new HashMap<String,Double>();
        @SuppressWarnings("unchecked")
        List<Map<String,Map<String,BigDecimal>>> gmpList = (List<Map<String,Map<String,BigDecimal>>>)FastJsonConverter.readValue(List.class,gmpJson);
        for(Map<String,Map<String,BigDecimal>> gmpMap : gmpList){
            //各个产品的key值
            for(String app : gmpMap.keySet()){
                Map<String,BigDecimal> ctrMap = gmpMap.get(app);
                if(ctrMap != null){
                    Number gmpValue = ctrMap.get(SireConstant.ES_CTR_NAME);
                    channelMap.put(app, Double.valueOf(gmpValue.toString()));
                }
            }
        }
        return channelMap;
    }

}
