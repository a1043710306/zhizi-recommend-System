package com.inveno.core.process.cms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.inveno.core.util.SysUtil;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

@Component("loadForceInsert")
public class LoadForceInsert {
	
	private static Log logger = LogFactory.getLog("cmslog");
	
	private static Map<String,Map<Double ,String>> forceInsert = new ConcurrentHashMap<String, Map<Double,String>>();  
	
	private static Map<String,Map<Double ,String>> forceInsertKeyMap = new ConcurrentHashMap<String, Map<Double,String>>();
	
	public Map<Double,String> getCacheForceInsert(String key){
		if( forceInsert.isEmpty() || StringUtils.isEmpty(key) ){
			return Collections.emptyMap();
		}
		return forceInsert.get(key);
	}
	
	public void setCacheForceKey(String key){
		if( StringUtils.isEmpty(key) ){
			return ;
		}
		//logger.info("setCacheForceKey :" + key);
		forceInsertKeyMap.put(key, Collections.emptyMap());
	}
	
	public void initForceInsert(){
		
		Map<String,Map<Double ,String>> forceInsertTemp = new ConcurrentHashMap<String, Map<Double,String>>(forceInsertKeyMap);  
		Set<String> keyset = forceInsertTemp.keySet();
		forceInsertTemp.putIfAbsent("stginfo:timeliness:high:Hindi:list:scenario:65803:119", Collections.emptyMap());
		forceInsertTemp.putIfAbsent("stginfo:timeliness:low:Hindi:list:scenario:65803:119", Collections.emptyMap());
		forceInsertTemp.putIfAbsent("stginfo:timeliness:high:Indonesian:list:scenario:65797:41", Collections.emptyMap());
		forceInsertTemp.putIfAbsent("stginfo:timeliness:low:Indonesian:list:scenario:65797:41", Collections.emptyMap());
		logger.info("initForceInsert key " + forceInsertTemp );
		
		JedisCluster jedisCluster = SysUtil.getBean("jedisCluster");
		for (String key : keyset)
		{
			try
			{
				if( key.contains("stginfo:timeliness:high:"))
				{
					String cursor = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
					boolean cycleIsFinished = false;
					
					Map<Double, String> highMap = new HashMap<Double, String>();
					while (!cycleIsFinished) {
						ScanResult<Tuple> scanResult = jedisCluster.zscan(key, cursor);
						List<Tuple> resultList = scanResult.getResult();
						for (Tuple t : resultList)
						{
							if (t.getScore() == 0.0 && StringUtils.isNotEmpty(t.getElement()))
							{
								highMap.put(0.0D, t.getElement());
							}
						}
						
						cursor = scanResult.getStringCursor();
						if (cursor.equals("0"))
						{
							cycleIsFinished = true;
						}
					}
					if (highMap.isEmpty())
					{
						forceInsertTemp.remove(key);
					}
					forceInsertTemp.put(key, highMap);
				}
				else
				{
					String cursor = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
					boolean cycleIsFinished = false;
					
					Map<Double, String> lowMap = new HashMap<Double, String>();
					while (!cycleIsFinished) {
						ScanResult<Tuple> scanResult = jedisCluster.zscan(key, cursor);
						List<Tuple> resultList = scanResult.getResult();
						for (Tuple t : resultList) {
							if( t.getScore() >= 1 && StringUtils.isNotEmpty(t.getElement())){
								lowMap.put(t.getScore(), t.getElement());
							}
						}
						
						cursor = scanResult.getStringCursor();
						if (cursor.equals("0"))
						{
							cycleIsFinished = true;
						}
					}
					if (lowMap.isEmpty())
					{
						forceInsertTemp.remove(key);
					}
					forceInsertTemp.put(key, lowMap);
				}
			}
			catch (Exception e)
			{
				logger.error("initForceInsert load key " + key, e);
				continue;
			}
		}
		
		forceInsert = forceInsertTemp;
		logger.info("initForceInsert load cache form redis :" + forceInsertTemp);
	}
}
