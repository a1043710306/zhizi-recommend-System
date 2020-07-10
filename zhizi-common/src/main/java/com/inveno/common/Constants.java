package com.inveno.common;


import com.inveno.common.util.Config;

public class Constants {

	public static final String ES_HOST;
	public static final String ES_HOST1;
	public static final String ES_HOST2;
	public static final String ES_HOST3;

	public static final int ES_PORT;
	public static final int ES_PORT1;
	
	public static final int ES_PORT2;
	public static final int ES_PORT3;

	public static final String ES_CLUTER_NAME;

	public static final String REDIS_HOST;

	public static final int REDIS_PORT;
	
	public static final String REDIS_TESTONBORROW;
	
	public static final String REDIS_TESTONRETURN;
	
	public static final int REDIS_MAXWAITMILLIS;
	
	public static final int REDIS_MAXIDLE;
	
	public static final int REDIS_MAXTOTAL;
	
	public static final int TIMELESS_HIGH;
	
	public static final int TIMELESS_MID;
	
	public static final int TIMELESS_LOWER;
	
	
	public static final String CTR_QUEUE_NAME;
	
	
	
	public static final int CTR_RATE;
	
	
/*	reids.testOnBorrow=true
	redis.testOnReturn=true
	reids.MaxWaitMillis=2000
	redis.MaxIdle=300
	redis.MaxTotal=600*/
	
	/**
	 * 用户上次点击的  pre::click::set::渠道::用户uuid
	 */
	public static final String REDISKEY_PRE_CLICK_INFO = "pre::click::set::";

	static {
		Config config = new Config("common-config.properties");
		
		Config config_db = new Config("db-connection-com.properties");

		ES_HOST = config.getProperty("es.host");

		ES_PORT = config.getIntProperty("es.port");

		REDIS_HOST = config.getProperty("redis.host");

		REDIS_PORT = config.getIntProperty("redis.port");

		ES_CLUTER_NAME = config.getProperty("es.cluter.name");
		
		REDIS_TESTONBORROW = config_db.getProperty("reids.testOnBorrow");
		
		REDIS_TESTONRETURN = config_db.getProperty("redis.testOnReturn");
		
		REDIS_MAXWAITMILLIS = config_db.getIntProperty("redis.MaxWaitMillis");
		
		REDIS_MAXIDLE = config_db.getIntProperty("redis.MaxIdle");
		
		REDIS_MAXTOTAL = config_db.getIntProperty("redis.MaxTotal");
		
		TIMELESS_HIGH = config.getIntProperty("timeliness.high");
		
		TIMELESS_MID = config.getIntProperty("timeliness.mid");
		
		TIMELESS_LOWER = config.getIntProperty("timeliness.lower");
		
		CTR_QUEUE_NAME = config.getProperty("ctr_queue_name");
		
		CTR_RATE = config.getIntProperty("ctr_rate");
		
		ES_HOST1 = config.getProperty("es.host1");
		
		ES_HOST2 = config.getProperty("es.host2");
		
		ES_HOST3 = config.getProperty("es.host3");
		
		ES_PORT1 = config.getIntProperty("es.port1");
		
		ES_PORT2 = config.getIntProperty("es.port2");
		
		ES_PORT3 = config.getIntProperty("es.port3");
	}
}
