package tw.qing.util;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CaseInsensitiveHashMap extends HashMap
{
	public boolean containsKey(Object key)
	{
		if (key instanceof String)
			return super.containsKey(((String) key).toUpperCase());
		return super.containsKey(key);
	}
	public void putAll(Map m)
	{
		super.putAll(new CaseInsensitiveHashMap(m));
	}
	public Object remove(Object key) 
	{
		if( key instanceof String )
			return super.remove(((String) key).toUpperCase());
		return super.remove(key);
	}
	public Object put(Object key, Object value)
	{
		if( key instanceof String )
			return super.put(((String) key).toUpperCase(), value);
		return super.put(key, value);
	}
	public Object get(Object key)
	{
		if( key instanceof String )
			return super.get(((String) key).toUpperCase());
		return super.get(key);
	}
	//
	public CaseInsensitiveHashMap(Map m)
	{
		Set set = m.keySet();
		Iterator it = set.iterator();
		while( it.hasNext() )
		{
			Object k = it.next();
			put(k, m.get(k));
		}
	}
	public CaseInsensitiveHashMap()
	{
		super();
	}
}