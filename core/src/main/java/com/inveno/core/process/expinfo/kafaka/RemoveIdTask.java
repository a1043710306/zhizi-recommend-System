package com.inveno.core.process.expinfo.kafaka;

import redis.clients.jedis.JedisCluster;

@Deprecated
public class RemoveIdTask {
	
	private static JedisCluster jedisCluster;
	
	public static void doTask() {
		final String zooKeeper = "192.168.1.223:8181";
		final String groupId = "test_group";
		final String topic = "test-topic";
		final int threads = 4;
		KafakaConsumer task = new KafakaConsumer(zooKeeper, groupId, topic,jedisCluster);
		task.run(threads);
		task.shutdown();
	}

}
