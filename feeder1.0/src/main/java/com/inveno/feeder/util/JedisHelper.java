package com.inveno.feeder.util;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public class JedisHelper
{
	public static Jedis newClientInstance(String ip, int port)
	{
		return new Jedis(ip, port, 10000);
	}
	public static JedisCluster newClientInstance(String[] ip_port_list)
	{
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		for (int j = 0; j < ip_port_list.length; j++)
		{
			String[] s = ip_port_list[j].split(":");
			if (s.length == 2)
			{
				jedisClusterNodes.add(new HostAndPort(s[0], Integer.valueOf(s[1])));
			}
		}
		JedisPoolConfig config = new JedisPoolConfig();
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		config.setMaxIdle(50);
		config.setMaxWaitMillis(10000);
		config.setMaxTotal(500);
		return new JedisCluster(jedisClusterNodes, config);
	}
}
