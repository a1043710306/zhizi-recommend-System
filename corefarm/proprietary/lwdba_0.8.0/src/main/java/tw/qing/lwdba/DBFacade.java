package tw.qing.lwdba;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class DBFacade
{
	private static Logger log = Logger.getLogger(DBFacade.class);
	//
	protected SQLExecutor sqlExecutor;
	protected SQLManager sqlManager;
	private boolean fGotoLastSupported = true;
	//
	public DBFacade() throws SQLException, ClassNotFoundException
	{
		this("default");
	}
	public DBFacade(String poolName) throws SQLException, ClassNotFoundException
	{
		sqlExecutor = SQLExecutorManager.getInstance().getSQLExecutor(poolName);
		sqlManager = SQLManager.getSQLManager(sqlExecutor.getSQLFile());
		sqlManager.setDatabaseType(getDatabaseType());
	}
	public String getDatabaseType()
	{
		return sqlExecutor.getDatabaseType();
	}
	public int sqlUpdate(String update) throws SQLException
	{
		log.debug("SQLUpdate: "+update);
		return sqlExecutor.executeUpdate(update);
	}
	public QueryResult sqlQuery(String query) throws SQLException
	{
		return sqlQuery(query, 0, -1, false);
	}
	public QueryResult sqlQuery(String query, int idxRow, int count, boolean fReturnTotal) throws SQLException
	{
		log.debug("SQLQuery: "+query);
		ResultSet rs = sqlExecutor.executeQuery(query);
		int nTotalRow = -1;
		if( idxRow > 0 )
		{
			ResultSet rsReturn[] = new ResultSet[1];
			nTotalRow = gotoRow(query, rs, idxRow, fReturnTotal, rsReturn);
			if( rsReturn[0] != null )
				rs = rsReturn[0];
		}
		QueryResult qr = new QueryResult(rs, nTotalRow, count, true);
		sqlExecutor.closeResultSet(rs);
		return qr;
	}
	public ArrayList sqlQueryRows(String query) throws SQLException
	{
		return sqlQueryRows(query, 0, -1);
	}
	public ArrayList sqlQueryRows(String query, int idxRow, int count) throws SQLException
	{
		QueryResult qr = sqlQuery(query, idxRow, count, false);
		return qr.getRows();
	}
	private int getRowCount(ResultSet rs) throws SQLException
	{
		int current = rs.getRow();
		while( rs.next() )
			current = rs.getRow();
		return current;
	}
	private int gotoRow(String query, ResultSet rs, int idxRow, boolean fReturnTotal, ResultSet rsReturn[]) throws SQLException
	{
		int nTotalRow = -1;
		//
		if( fReturnTotal )
		{
			if( fGotoLastSupported )
			{
				try
				{
					if( rs.getRow() == 0 )
					{
						nTotalRow = getRowCount(rs);
						sqlExecutor.closeResultSet(rs);
						//
						rs = sqlExecutor.executeQuery(query);
						rsReturn[0] = rs;
					}
					else
					{
						rs.afterLast();
						nTotalRow = rs.getRow()-1;
						rs.beforeFirst();
					}
				}
				catch(SQLException e)
				{
					fGotoLastSupported = false;
				}
			}
			if( fGotoLastSupported == false )
			{
				nTotalRow = getRowCount(rs);
				sqlExecutor.closeResultSet(rs);
				//
				rs = sqlExecutor.executeQuery(query);
				rsReturn[0] = rs;
			}
		}
		if( idxRow > 0 )
		{
			if( fGotoLastSupported )
			{
				try
				{
					rs.relative(idxRow);
				}
				catch(SQLException e)
				{
					fGotoLastSupported = false;
				}
			}
			if( fGotoLastSupported == false )
			{
				for(int i=0;i<idxRow;i++)
					rs.next();
			}
		}
		return nTotalRow;
	}
}