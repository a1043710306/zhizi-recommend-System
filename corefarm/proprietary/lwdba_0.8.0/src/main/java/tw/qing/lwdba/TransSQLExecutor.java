package tw.qing.lwdba;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class TransSQLExecutor extends SQLExecutor
{
	private static Logger log = Logger.getLogger(TransSQLExecutor.class);
	//
	private Connection theOne;
	//
	public TransSQLExecutor() throws ClassNotFoundException, SQLException
	{
		this("default");
	}
	public TransSQLExecutor(String _poolName) throws ClassNotFoundException, SQLException
	{
		super(_poolName);
		//
		theOne = DriverManager.getConnection("jdbc:apache:commons:dbcp:"+poolName);
		theOne.setAutoCommit(false);
	}
	public void close() throws SQLException
	{
		// never close the pool!
		theOne.setAutoCommit(true);
		super.recycleConnection(theOne);
	}
	public Connection getConnection() throws SQLException
	{
		return theOne;
	}
	public void recycleConnection(Connection conn) throws SQLException
	{
		// do nothing
	}
	public void commit() throws SQLException
	{
		theOne.commit();
	}
	public void rollback() throws SQLException
	{
		theOne.rollback();
	}
}