package com.inveno.fallback.redis.impl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisCluster;

import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.redis.FallbackRedis;

@Component
public class ScoreFallbackRedisImpl implements FallbackRedis{

	private static final Logger logger = LoggerFactory.getLogger(ScoreFallbackRedisImpl.class);
	
	@Autowired
    private JedisCluster jedisCluster;
	
	public void process(List<MessageContentEntry> messageContentModelList, String key, int size, String category) {
		// TODO Auto-generated method stub
		
		handlerExpiredKeys(key);
			
		if(messageContentModelList.size() == 0){
			logger.debug("categoryId:"+key+" loader datas count is "+messageContentModelList.size());
			return;
		}
			
		logger.debug("ScoreFallbackRedisImpl process key "+key+" start..");
		int loadCount = messageContentModelList.size();
		int redisSize = size;
		logger.debug("load counts size :"+loadCount);
		logger.debug("redis set size"+redisSize);
		
		 //list 数据小于redisStroeSize
        if(loadCount < redisSize){
        	logger.debug("loadCount < redisSize start...");
        	 long tempCount = redisSize-loadCount;//大于多少
        		 long redisTempCount = jedisCluster.zcard(key);
        		 if(redisTempCount > tempCount){
        			 //删除多余的数据
        			  long moreDataCount = redisTempCount - tempCount;
        			  jedisCluster.zremrangeByRank(key,0, moreDataCount-1);
        			  for(MessageContentEntry messageContentModel : messageContentModelList){
               			jedisCluster.zadd(key, messageContentModel.getScore(), messageContentModel.getId());
               		}
        			 
        		 }else{//直接进入redis
        			 for(MessageContentEntry messageContentModel : messageContentModelList){
             			jedisCluster.zadd(key, messageContentModel.getScore(), messageContentModel.getId());
             		}
        		 }
        	 
         }
         
         //list 数据小于redisStroeSize
        if(loadCount == redisSize){
        	logger.debug("loadCount = redisSize start...");
        	 //全部删除
        		jedisCluster.zremrangeByScore(key, Double.MIN_VALUE, Double.MAX_VALUE);
        	 //全部入库
    	      for(MessageContentEntry messageContentModel : messageContentModelList){
    			jedisCluster.zadd(key, messageContentModel.getScore(), messageContentModel.getId());
        	 }
         }
         
         //list 数据大于redisStroeSize
         if(loadCount > redisSize){
        	 logger.debug("loadCount > redisSize start...");
        	//全部删除
        	 jedisCluster.zremrangeByScore(key, Double.MIN_VALUE, Double.MAX_VALUE);
        	 //全部入库
	       	     for(MessageContentEntry messageContentModel : messageContentModelList){
	       			   jedisCluster.zadd(key, messageContentModel.getScore(), messageContentModel.getId());
       		     }
        	 long tempCount =  loadCount - redisSize;
        		 jedisCluster.zremrangeByRank(key,0, tempCount-1);
        }
		
	}

	//处理过期的redis-key
	private void handlerExpiredKeys(String key){
		logger.debug("start handler key : "+key);
		Set<String> contentIds = jedisCluster.zrange(key, 0, -1);
		for(String contentId : contentIds){
			if(jedisCluster.zrank("AllInfoIDs", contentId) == null){
				jedisCluster.zrem(key, contentId);
				logger.debug("member:"+contentId+" is deleted in key "+key);
			}
		}
	}

}
