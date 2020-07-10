package com.inveno.fallback.redis.impl;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.redis.FallbackRedis;
import com.inveno.fallback.util.SystemUtils;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.sortedset.ZAddParams;

@Component
public class FallbackRedisImpl implements FallbackRedis {

	private static final Logger logger = LoggerFactory.getLogger(FallbackRedisImpl.class);

	private static final Logger monitorLogger = LoggerFactory.getLogger("monitor");

	@Autowired
	private JedisCluster jedisCluster;

	public void process(List<MessageContentEntry> messageContentModelList, String key, int redisSize, String category) {
		logger.debug("fallbackRedisImpl process key " + key + " start..");
		int loadCount = messageContentModelList.size();

		if (!"all".equals(category)) {
			logger.debug(key + ",load content:" + messageContentModelList.toString());
		}

		long currentTS = System.currentTimeMillis() / 1000;
		int processSize = Math.min(loadCount, redisSize);
		logger.debug("key=" + key + ",redisSize=" + redisSize + ",loadCount=" + loadCount + ",processSize=" + processSize);
		//改变做法以避免删除后无法填加成功
		Set<String> hsNeedRemoved = jedisCluster.zrange(key, 0, -1);
		//设置fallback池
		for (int i = 0; i < processSize; i++) {
			MessageContentEntry entry = messageContentModelList.get(i);
			if (currentTS - entry.getPublishTimeTs() < 1)
				continue;
			Double score = (entry.getScore() == 0) ? Math.abs(Double.valueOf(currentTS - entry.getPublishTimeTs()))
					: (1 - entry.getScore());
			Object[] memberElement = new Object[] { entry.getGroupId(), entry.getContentType(), entry.getLinkType(),
					entry.getDisplayType() };
			String member = StringUtils.join(memberElement, FallbackConstant.FALLBACK_SUFFIX_WELL);
			jedisCluster.zadd(key, score, member);
			hsNeedRemoved.remove(member);
		}
		if (hsNeedRemoved.size() > 0) {
			logger.debug("process remove fallback data, redis key is " + key + ", size=" + hsNeedRemoved.size());
			jedisCluster.zrem(key, (String[])hsNeedRemoved.toArray(new String[0]));
		}
		long dataCount = jedisCluster.zcard(key);
		logger.debug("finish process add fallback data, redis key is " + key + ", size=" + dataCount);
		monitorLogger.info(SystemUtils.getIPAddress(true) + "&&fallback.query.newinfos-count&&" + dataCount + "&&0&&ORIGINAL&&600&&key=" + key);
	}
}