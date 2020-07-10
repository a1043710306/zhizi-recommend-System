package tw.qing.lwdba;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import tw.qing.sys.StringManager;

public class SQLExecutor
{
	private static Logger log = Logger.getLogger(SQLExecutor.class);
	//
	// DBCP objects
	private GenericObjectPool connectionPool;
	private ConnectionFactory connectionFactory;
	private PoolableConnectionFactory poolableConnectionFactory;
	protected PoolingDriver driver;
	//
	private String databaseType;
	private String driverClassName;
	private String driverURL;
	private String userName;
	private String password;
	protected int maxConnectionCount = 16;
	protected String encoding;
	protected String sqlFile;
	//
	private Properties props = new Properties();
	private HashMap hmStatement = new HashMap();
	private HashMap hmConnection = new HashMap();
	protected String poolName = "default";
	//
	SQLExecutor() throws ClassNotFoundException, SQLException
	{
		this("default");
	}
	SQLExecutor(String _poolName) throws ClassNotFoundException, SQLException
	{
		if( _poolName == null )
			poolName = "default";
		else
			poolName = _poolName;
		//
		readConfiguration(props);
		//
		Class.forName(driverClassName);
		//
		log.info("Connection Pool - "+poolName+" is creating.");
		log.info("Connection Pool - "+poolName+": databaseType: "+databaseType);
		log.info("Connection Pool - "+poolName+": driverClassName: "+driverClassName);
		log.info("Connection Pool - "+poolName+": driverURL: "+driverURL);
		log.info("Connection Pool - "+poolName+": userName: "+userName);
		log.info("Connection Pool - "+poolName+": password: "+password);
		log.info("Connection Pool - "+poolName+": maxConnectionCount: "+maxConnectionCount);
		log.info("Connection Pool - "+poolName+": encoding: "+encoding);
		//
		Class.forName("org.apache.commons.dbcp.PoolingDriver");
		driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
		boolean f = poolExists(driver, poolName);
		if( f == false )
		{
			connectionPool = new GenericObjectPool(null);
			connectionFactory = new DriverManagerConnectionFactory(driverURL, props);
			poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
			driver.registerPool(poolName, connectionPool);
		}
	}
	public void printStatus() throws SQLException
	{
		System.out.println("ActiveCount: " + connectionPool.getNumActive());
		System.out.println("IdleCount: " + connectionPool.getNumIdle());
		System.out.println("MaxActiveCount: " + connectionPool.getMaxActive());
		System.out.println("MaxIdleCount: " + connectionPool.getMaxIdle());
	}
	public void close() throws SQLException
	{
		PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
		driver.closePool(poolName);
	}
	public boolean isMaxConnectionReached()
	{
		return connectionPool.getNumActive() >= connectionPool.getMaxActive();
	}
	public int getConnectionCount()
	{
		return connectionPool.getNumActive();
	}
	public void setMaxConnectionCount(int count)
	{
		connectionPool.setMaxActive(count);
	}
	public String getDatabaseType()
	{
		return databaseType;
	}
	public Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:"+poolName);
	}
	public void recycleConnection(Connection conn) throws SQLException
	{
		conn.close();
	}
	public ResultSet executeQuery(String sql) throws SQLException
	{
		Connection conn = getConnection();
		//
		ResultSet rs = null;
		//
		if( conn != null )
		{
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
			}
			catch(SQLException e)
			{
				log.error("Exception at SQL Query: "+sql, e);
				throw e;
			}
			finally
			{
				try
				{
					if( stmt != null )
					{
						if( openResultSet(rs, stmt, conn) == false )
						{
							stmt.close();
							recycleConnection(conn);
						}
					}
				}
				catch(Exception e2)
				{
					log.error("Error while closing a statement.", e2);
				}
			}
		}
		return rs;
	}
	public int executeUpdate(String sql) throws SQLException
	{
		int nModified = -1;
		//
		Connection conn = getConnection();
		//
		if( conn != null )
		{
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				nModified = stmt.executeUpdate(sql);
			}
			catch (SQLException e)
			{
				log.error("Exception at SQL Update: "+sql, e);
				throw e;
			}
			finally
			{
				if( stmt != null )
				{
					try
					{
						stmt.close();
					}
					catch(Exception e2)
					{
						log.error("Error while closing a statement.", e2);
					}
				}
				recycleConnection(conn);
			}
		}
		return nModified;
	}
	public String getSQLFile()
	{
		return sqlFile;
	}
	private boolean openResultSet(ResultSet rs, Statement stmt, Connection conn) throws SQLException
	{
		if( rs == null )
			return false;
		hmStatement.put(rs, stmt);
		hmConnection.put(rs, conn);
		return true;
	}
	void closeResultSet(ResultSet rs) throws SQLException
	{
		if( rs == null )
			return;
		Statement stmt = (Statement) hmStatement.remove(rs);
		if( stmt != null )
			stmt.close();
		Connection conn = (Connection) hmConnection.remove(rs);
		if (conn!=null)
			conn.close();
	}
	private void readConfiguration(Properties props)
	{
		StringManager sm = StringManager.getManager("system");
		//
		String _poolName = poolName;
		//
		if( _poolName == null )
			_poolName = "default";
		//
		databaseType = sm.getString("lwdba.pool."+_poolName+".type");
		driverClassName = sm.getString("lwdba.pool."+_poolName+".driverClassName");
		driverURL = sm.getString("lwdba.pool."+_poolName+".driverURL");
		userName = sm.getString("lwdba.pool."+_poolName+".userName");
		password = sm.getString("lwdba.pool."+_poolName+".password");
		String _maxConnectionCount = sm.getString("lwdba.pool."+_poolName+".maxConnectionCount");
		if( _maxConnectionCount != null )
			maxConnectionCount = Integer.parseInt(_maxConnectionCount);
		encoding = sm.getString("lwdba.pool."+_poolName+".encoding");
		sqlFile = sm.getString("lwdba.pool."+_poolName+".sqlFile");
		if( sqlFile == null )
			sqlFile = "sql";
		// for MySQL
		if( encoding != null )
			props.put("characterEncoding",encoding);
		props.put("useUnicode", "true");
		props.put("user", userName);
		props.put("password", password);
	}
	private boolean poolExists(PoolingDriver driver, String name) throws SQLException
	{
		String s[] = driver.getPoolNames();
		for(int i=0;i<s.length;i++)
		{
			if( s[i].equals(name) )
				return true;
		}
		return false;
	}
}