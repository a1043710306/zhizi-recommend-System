package tw.qing.lwdba;

import java.util.HashMap;

import tw.qing.sys.StringManager;

public class SQLManager
{
	public static HashMap hmManager = new HashMap();
	//
	private StringManager sqlStmtManager;
	private String databaseType = "default";
	//
	public static SQLManager getSQLManager()
	{
		return getSQLManager("sql");
	}
	public static SQLManager getSQLManager(String sqlFile)
	{
		synchronized( hmManager )
		{
			SQLManager sm = (SQLManager) hmManager.get(sqlFile);
			if( sm == null )
			{
				sm = new SQLManager(sqlFile);
				hmManager.put(sqlFile, sm);
			}
			return sm;
		}
	}
	public void setDatabaseType(String _databaseType)
	{
		databaseType = _databaseType;
	}
	public String getDatabaseType()
	{
		return databaseType;
	}
	public String getSQL(String key)
	{
		return sqlStmtManager.getString(key);
	}
	public String getSQL(String key, Object[] arg)
	{
		return sqlStmtManager.getString(key, SQLUtil.getSQLValue(arg, databaseType));
	}
	public String getSQL(String key, Object arg)
	{
		return sqlStmtManager.getString(key, SQLUtil.getSQLValue(arg, databaseType));
	}
	public String getSQL(String key, Object arg1, Object arg2)
	{
		return sqlStmtManager.getString(key, SQLUtil.getSQLValue(arg1, databaseType), SQLUtil.getSQLValue(arg2, databaseType));
	}
	public String getSQL(String key, Object arg1, Object arg2, Object arg3)
	{
		return sqlStmtManager.getString(key, SQLUtil.getSQLValue(arg1, databaseType), SQLUtil.getSQLValue(arg2, databaseType), SQLUtil.getSQLValue(arg3, databaseType));
	}
	public String getSQL(String key, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return sqlStmtManager.getString(key, SQLUtil.getSQLValue(arg1, databaseType), SQLUtil.getSQLValue(arg2, databaseType), SQLUtil.getSQLValue(arg3, databaseType), SQLUtil.getSQLValue(arg4, databaseType));
	}
	private SQLManager(String sqlFile)
	{
		sqlStmtManager = StringManager.getManager(sqlFile);
	}
	public static void main(String argv[])
	{
		String sql = getSQLManager().getSQL("Customer.selectAll");
		System.out.println(sql);
	}
}