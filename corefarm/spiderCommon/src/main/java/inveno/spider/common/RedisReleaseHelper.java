package inveno.spider.common;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.common.utils.Util;

import java.io.IOException;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisReleaseHelper
{
    private JedisPoolConfig config = null;
    private JedisPool pool = null;
    private static final Logger LOG = LoggerFactory.make();
    

    private static RedisReleaseHelper instance;
    
    public enum Key{
        SYS_PROFILE,KEY_QUEUE_LIST;
    }

    private RedisReleaseHelper()
    {
        config = new JedisPoolConfig();
        // configuration
        config.setMaxTotal(1000);
        pool = new JedisPool(config, Constants.REDIS_RELEASE_HOST, Constants.REDIS_RELEASE_PORT,Constants.REDIS_TIMEOUT);
    }

    public synchronized static RedisReleaseHelper getInstance()
    {
        if (null == instance)
        {
            instance = new RedisReleaseHelper();
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
    
    public void addSetValue(String key, String value,int expireSeconds)
    {
    	Jedis jedis = null;
        try
        {
            jedis = getJedis();
            jedis.sadd(key, value);
            jedis.expire(key, expireSeconds);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } finally
        {
            close(jedis);
        }
    }

    public void set(String key, String value)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            String code = jedis.set(key, value);
            LOG.debug("Set value return code:"+code);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } finally
        {
            close(jedis);
        }
    }

    public void set(String key, String value,int expireSeconds)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            String code = jedis.set(key, value);
            if(Protocol.Keyword.OK.name().equals(code))
            {
                jedis.expire(key, expireSeconds);
            }
            LOG.debug("Set value return code:"+code);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } finally
        {
            close(jedis);
        }
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
            pool.returnBrokenResource(jedis);
            jedis = null;
        } 
        LOG.error(exception);
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
    
    public String get(String key)
    {
        Jedis jedis = null;
        String value = null;
        try
        {
            jedis = getJedis();
            value = jedis.get(key);
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } finally
        {
            close(jedis);
        }
        
        return value;
    }
    public <T> T getObject(String key)
    {
        Jedis jedis = null;
        byte[] value = null;
        T object = null;
        try
        {
            jedis = getJedis();
            value = jedis.get(Util.serialize(key));
            if(null!=value)
            {
            object = Util.deserialize(value);
            }
        } catch (JedisConnectionException e)
        {
            returnBrokenResource(jedis,e);
        } catch (IOException e)
        {
            LOG.error("",e);
        } catch (ClassNotFoundException e)
        {
            LOG.error("",e);
        } finally
        {
            close(jedis);
        }
        
        return object;
    }
    
    
    
    public void set(String key,Object value)
    {
        
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            String code = jedis.set(Util.serialize(key), Util.serialize(value));
            LOG.debug("Set value return code:"+code);
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

}
