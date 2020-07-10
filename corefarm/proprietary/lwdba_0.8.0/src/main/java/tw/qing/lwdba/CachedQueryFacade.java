package tw.qing.lwdba;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
//
import org.apache.log4j.Logger;
//
import net.spy.memcached.MemcachedClient;
//
import tw.qing.lwdba.DBFacade;
import tw.qing.sys.StringManager;
import tw.qing.util.MD5Digest;

public class CachedQueryFacade extends DBFacade
{
	private static final Logger log = Logger.getLogger(CachedQueryFacade.class);
	//
	private static StringManager smgr = StringManager.getManager("system");
	private static CachedQueryFacade instance;
	//
	private String memcachedIP = "127.0.0.1";
	private int memcachedPort = 11211;
	private MemcachedClient memcachedClient;
	private HashMap hmMD5Key = new HashMap();
	//
	private boolean fEnableSQLCache = true;
	//
	public static synchronized CachedQueryFacade getInstance()
	{
		if( instance == null )
		{
			try
			{
				instance = new CachedQueryFacade();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		return instance;
	}
	//
	public HashMap[] sqlQueryCached(String sql) throws SQLException
	{
		if(fEnableSQLCache)
		{
			HashMap[] hm = (HashMap[]) getFromCache(sql, 10*60);//in second
			if (hm == null)
			{
				ArrayList al = sqlQueryRows(sql);
				if(al!=null)
					hm = (HashMap[])al.toArray(new HashMap[0]);
				putIntoCache(sql, hm, 10*60);//in second
			}
			return hm;
		}
		else
		{
			ArrayList al = sqlQueryRows(sql);
			if(al!=null)
			{
				HashMap[] hm = (HashMap[])al.toArray(new HashMap[0]);
				return hm;
			}
			return null;
		}
	}
	// refreshInterval is in second!
	public HashMap[] sqlQueryCached(String sql, long refreshInterval) throws SQLException
	{
		if(fEnableSQLCache)
		{
			HashMap[] hm = (HashMap[]) getFromCache(sql, (int)(refreshInterval));
			if (hm == null)
			{
				ArrayList al = sqlQueryRows(sql);
				if(al!=null)
					hm = (HashMap[])al.toArray(new HashMap[0]);
				putIntoCache(sql, hm, (int)(refreshInterval));
			}
			return hm;
		}
		else
		{
			ArrayList al = sqlQueryRows(sql);
			if(al!=null)
			{
				HashMap[] hm = (HashMap[])al.toArray(new HashMap[0]);
				return hm;
			}
			return null;
		}
	}
	public void invalidateSQLQueryCached(String sql)
	{
		if(fEnableSQLCache)
			removeFromCache(sql);
	}
	public HashMap[] refreshSQLQueryCached(String sql) throws SQLException
	{
		if(fEnableSQLCache)
			removeFromCache(sql);
		return sqlQueryCached(sql);
	}
	private boolean checkAndReconnectMemcached()
	{
		try
		{
			if( memcachedClient == null )
			{
				memcachedClient = new MemcachedClient(new InetSocketAddress(memcachedIP, memcachedPort));
			}
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			memcachedClient = null;
			return false;
		}
	}
	public void updateCache(String key, Object value)
	{
		if(fEnableSQLCache)
			putIntoCache(key, value, 10*60);
	}
	// refreshInterval is in second!
	private void putIntoCache(String key, Object value, int refreshInterval)
	{
		try
		{
			if(fEnableSQLCache)
			{
				if( checkAndReconnectMemcached() )
				{
					memcachedClient.set(transKey(key), refreshInterval, value);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			memcachedClient = null;
		}
	}
	private Object getFromCache(String key, int refreshInterval)
	{
		try
		{
			if( checkAndReconnectMemcached() )
			{
				return memcachedClient.get(transKey(key));
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			memcachedClient = null;
			return null;
		}
	}
	private void removeFromCache(String key)
	{
		try
		{
			if( checkAndReconnectMemcached() )
			{
				memcachedClient.delete(transKey(key));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			memcachedClient = null;
		}
	}
	private boolean isOverLongKey(String key)
	{
		return key.length() > 250;
	}
	private String getMD5Key(String key)
	{
		String md5Key = (String) hmMD5Key.get(key);
		if( md5Key == null )
		{
			try
			{
				md5Key = MD5Digest.getDigest(key, "Ken");
			}
			catch(java.security.NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			hmMD5Key.put(key, md5Key);
		}
		return md5Key;
	}
	private String transKey(String key)
	{
//		if( !isOverLongKey(key) )
//			return key;
		return getMD5Key(key);
	}
	//
	private CachedQueryFacade() throws Exception
	{
		super(smgr.getString("mode"));
		//
		String enableSQLCacheVal = smgr.getString("EnableSQLCache");
		if( enableSQLCacheVal != null )
			fEnableSQLCache = (new Boolean(enableSQLCacheVal)).booleanValue();
		if( fEnableSQLCache )
		{
			String s = smgr.getString("memcached.host");
			if( s != null )
				memcachedIP = s;
		}
	}
}