package tw.qing.lwdba;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class SQLExecutorManager
{
	private static Logger log = Logger.getLogger(SQLExecutorManager.class);
	private static SQLExecutorManager instance;
	//
	private HashMap hmSQLExecutor = new HashMap();
	//
	public static SQLExecutorManager getInstance()
	{
		if( instance == null )
			instance = new SQLExecutorManager();
		return instance;
	}
	//
	public SQLExecutor getSQLExecutor() throws SQLException, ClassNotFoundException
	{
		return  getSQLExecutor("default");
	}
	public SQLExecutor getSQLExecutor(String poolName) throws SQLException, ClassNotFoundException
	{
		synchronized( hmSQLExecutor )
		{
			SQLExecutor se = (SQLExecutor) hmSQLExecutor.get(poolName);
			if( se == null )
			{
				se = new SQLExecutor(poolName);
				hmSQLExecutor.put(poolName, se);
			}
			return se;
		}
	}
	//
	private SQLExecutorManager()
	{
	}
}