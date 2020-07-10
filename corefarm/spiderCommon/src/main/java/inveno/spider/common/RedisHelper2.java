package inveno.spider.common;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.common.utils.NotMapModelBuilder;
import inveno.spider.common.utils.Util;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.Serializable;

public class RedisHelper2
{
    private JedisPoolConfig config = null;
    private JedisPool pool = null;
    private static final Logger LOG = LoggerFactory.make();


    private static RedisHelper2 instance;

    public enum Key{
        SYS_PROFILE,KEY_QUEUE_LIST;
    }

    private RedisHelper2()
    {
        config = new JedisPoolConfig();
        // configuration
        config.setMaxTotal(1000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRunsMillis(10000);
        pool = new JedisPool(config, Constants.REDIS_HOST2, Constants.REDIS_PORT,Constants.REDIS_TIMEOUT);
    }

    public synchronized static RedisHelper2 getInstance()
    {
        if (null == instance)
        {
            instance = new RedisHelper2();
        }

        return instance;
    }

    private Jedis getJedis() throws JedisConnectionException
    {
        Jedis jedis = pool.getResource();
        if(null==jedis)
        {
            throw new JedisConnectionException("Connection of redis is null.");
        }
        return jedis;
    }

    
    public void delete(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            Long code = jedis.del(key);
            code = code ^ jedis.del(Util.serialize(key));
            LOG.debug("Delete key("+key+") return code:"+code);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } catch (IOException e)
        {
            LOG.error("",e);
        } finally
        {
            close(jedis);
        }
    }
    
    public boolean exists(String key)
    {
        Jedis jedis = null;
        boolean exists=false;
        try
        {
            jedis = getJedis();
            exists = jedis.exists(key);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } finally
        {
            close(jedis);
        }
        return exists;
    }
    
    
    private void returnBrokenResource(Jedis jedis,Exception exception)
    {
        // returnBrokenResource when the state of the object is
        // unrecoverable
        if (null != jedis)
        {
        	try {
        		pool.returnBrokenResource(jedis);
        		jedis = null;
        	} catch(Exception e) {
        		LOG.error("returnBrokenResource to pool has exception:", e);
        	}
        } 
        LOG.error("use redis occur exception:", exception);
    }
    
    public void close()
    {
        if(null!=pool)
        {
            pool.destroy();
        }
    }
    
    private void close(Jedis jedis)
    {
        // / ... it's important to return the Jedis instance to the pool
        // once you've finished using it
        if (null != jedis)
            pool.returnResource(jedis);
        
    }

    public void lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Long code = jedis.lpush(key, value);
            LOG.debug("Set value return code:" + code);
        } catch (JedisConnectionException e) {
            returnBrokenResource(jedis, e);
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            close(jedis);
        }
    }

    public <T extends Serializable> void lpush(String key, T object) {
        try {
            String text = NotMapModelBuilder.getInstance().toJson(object);
            lpush(key, text);
        } catch (Exception e) {
            LOG.error("Fail to lpush!", e);
        }
    }


    public String brpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.brpop(key, "0").get(1);
        } catch (JedisConnectionException e) {
            returnBrokenResource(jedis, e);
        } catch (Exception e) {
            LOG.error("Fail to brpop! ", e);
        } finally {
            close(jedis);
        }
        return null;
    }

    public <T extends Serializable> T brpop(String key, Class<T> clazz) {
        T result = null;
        try {
            String text = brpop(key);
            if (text != null)
                result = NotMapModelBuilder.getInstance().build(text, clazz);
        } catch (Exception e) {
            LOG.error("get brpop mesg [" + key + "] happen error!", e);
        }
        return result;
    }
}
