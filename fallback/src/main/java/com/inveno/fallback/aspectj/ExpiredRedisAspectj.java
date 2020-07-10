package com.inveno.fallback.aspectj;

import java.util.Iterator;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.server.DataBaseContentLoader;
import com.inveno.fallback.server.PropertyContentLoader;
import com.inveno.fallback.util.ESQCondition;
import com.inveno.fallback.util.ElasticSearchUtil;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component
//@Aspect
public class ExpiredRedisAspectj {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredRedisAspectj.class);
	
	@Autowired
    private JedisCluster jedisCluster;
	
	private final static Long ONE_HOURS_MILLIS = 3600000l;
	
	//@Pointcut("execution (* com.inveno.fallback.redis..*.*(..))")
	public void redisMethod(){
	}
	
	//@Before("redisMethod()")
	public void handlerExpiredKeys(JoinPoint joinPoint){
		
		String key =  (String) joinPoint.getArgs()[1];
		logger.debug("start handler redis key ="+key);

		Long currentTime = System.currentTimeMillis();
		logger.debug("current system time:"+currentTime);
		
		String categoryId =  (String) joinPoint.getArgs()[3];
		logger.debug("category id ="+categoryId);
		
		Integer expiredHour = DataBaseContentLoader.getCategoryExpiredHour(categoryId); 
		if("all".equals(categoryId)){
			 expiredHour = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.EXPIRED_HOUR));	
		}
		
	    logger.debug("expiredHour:"+expiredHour);
	    
	    Long expiredHourMills = ONE_HOURS_MILLIS*expiredHour;
	    
	    Long betweenDaySecs = (currentTime-expiredHourMills)/1000;
	    logger.debug("betweenDaySecs:"+betweenDaySecs);
	    
		Set<Tuple> scoreValues = jedisCluster.zrangeWithScores(key, 0, -1);
		Iterator<Tuple> its = scoreValues.iterator();
		while(its.hasNext()){
			Tuple tuple = its.next();
			Double score = tuple.getScore();
			String member =  tuple.getElement();
			//删除过期KEY
			if(betweenDaySecs.intValue() > score.intValue()){
				jedisCluster.zrem(key, member);
				logger.debug("the key ="+key+" member:"+member+" is deleted by expired...");
			}else{
				//查找在ES中是否下架 如下架也删除
				String contentId = member.split(FallbackConstant.FALLBACK_SUFFIX_WELL)[0];
				ESQCondition esqCondition = new ESQCondition();
				BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
				QueryBuilder termsQueryContentId = QueryBuilders.termsQuery(("content_id"), contentId);
				boolQueryBuilder.must(termsQueryContentId);	
				esqCondition.setQueryBuilder(boolQueryBuilder);
				SearchResponse response=  ElasticSearchUtil.getInstance().search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition,new String[]{"content_id","publish_time_ts"});
				if(response.getHits().getHits().length == 0){
					jedisCluster.zrem(key, member);
					logger.debug("the key ="+key+" member:"+member+" is deleted by shelves...");
				}
			}
			
		}
		
	}
	
}

