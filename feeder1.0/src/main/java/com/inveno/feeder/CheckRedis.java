package com.inveno.feeder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inveno.feeder.thrift.FeederInfo;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

public class CheckRedis 
{
	private static boolean fDebugMode = true;

	private static boolean bOnline = false;

	private static boolean bOversea = false;

	//redis cluster for detail
	private static JedisCluster jedis;

	//redis cluster for cache
	private static JedisCluster jedisCache;

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("./run.sh com.inveno.feeder.CheckRedis <content_id> [field_name of news_${content_id}, default:Prediction]");
			return;
		}

		String strRedisDetailIPWithPort = null;
		String strRedisCacheIPWithPort  = null;
		try
		{
			Properties props = Feeder.loadProperties("connection-info.properties");

			bOnline = Boolean.valueOf(props.getProperty("is-online"));
			if (bOnline)
			{
				strRedisDetailIPWithPort = props.getProperty("online-redis-ip_port");
				strRedisCacheIPWithPort  = props.getProperty("online-redis-cache-ip_port");
			}
			else
			{
				strRedisDetailIPWithPort = props.getProperty("local-redis-ip_port");
			}
		}
		catch (Exception ex)
		{
		}
		System.out.println("Working Redis: " + strRedisDetailIPWithPort);

		jedis = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisDetailIPWithPort.split(";"));
		if (!StringUtils.isEmpty(strRedisCacheIPWithPort))
			jedisCache = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisCacheIPWithPort.split(";"));
		else
			jedisCache = jedis;

		try
		{
			String prefix_string = "news";
			String contentId = args[0];
			String key = prefix_string + "_" + contentId;
			String field_name = (args.length < 2) ? "Prediction" : args[1];
			byte[] empTd = jedis.hget(key.getBytes(), field_name.getBytes());
			FeederInfo feederInfo = new FeederInfo();
			TDeserializer deserializer = new TDeserializer();
			deserializer.deserialize(feederInfo, empTd);
			System.out.println(feederInfo);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		finally
		{
			try
			{
				jedis.close();
			}
			catch (IOException e)
			{
				//ignore
			}
		}
	}
}