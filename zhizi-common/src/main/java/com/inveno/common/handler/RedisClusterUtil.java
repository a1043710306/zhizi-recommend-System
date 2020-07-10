package com.inveno.common.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inveno.common.Constants;


public class RedisClusterUtil extends JedisCluster{
	
	private static RedisClusterUtil redisClusterUtil = null;
	
	private RedisClusterUtil(Set<HostAndPort>  jedisClusterNodes,GenericObjectPoolConfig poolConfig){
		super(jedisClusterNodes, poolConfig);
		/*Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //Jedis Cluster will attempt to discover cluster nodes automatically
        jedisClusterNodes.add(new HostAndPort(Constants.REDIS_HOST, Constants.REDIS_PORT));*/
		
	}
	
	public static RedisClusterUtil getInstance(){
 		
		
		if (redisClusterUtil == null) {
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
			//Jedis Cluster will attempt to discover cluster nodes automatically
	        jedisClusterNodes.add(new HostAndPort(Constants.REDIS_HOST, Constants.REDIS_PORT));
	        JedisPoolConfig config = new JedisPoolConfig();  
	        config.setTestOnBorrow(Boolean.valueOf(Constants.REDIS_TESTONBORROW));
	        config.setTestOnReturn(Boolean.valueOf(Constants.REDIS_TESTONRETURN));
	        config.setMaxWaitMillis(Constants.REDIS_MAXWAITMILLIS);
	        config.setMaxIdle(Constants.REDIS_MAXIDLE);
	        config.setMaxTotal(Constants.REDIS_MAXTOTAL);
	        
			redisClusterUtil = new RedisClusterUtil(jedisClusterNodes,config);
		}
		
		return redisClusterUtil;
	}
	                                                                         
	
	public static void main(String[] args) {
		//RedisClusterUtil redisClusterUtil = RedisClusterUtil.getInstance();
		String json = "{categoryId:[1,13,3,24,51,40,41]}";
		//{categoryId:[1,2,3,4,5,6,7,8]}
		//{categoryId:[10,11,12,13,14]}
		//{categoryId:[16,18]}
		//{categoryId:[20,24,28,29]}
		//{categoryId:[37,38,39,40,41,42,43,44,45]}
		//{categoryId:[47,48,49,50,51,52,53,54,55]}
		
		 JSONObject  jsonObject = JSONObject.parseObject(json);
		 List<Integer> categoryIds = JSON.parseArray(jsonObject.getJSONArray("categoryId").toString(), Integer.class);
		 
		 System.out.println(jsonObject.get("rank"));
		
		 System.out.println(categoryIds);
		//redisClusterUtil.hset("scenario_categoryid_map", "fuyiping-gionee_zh_cn_all_131329", json);
		 
		 //0febb9b4-486_zh_cn_all_1 
	}
	
}
