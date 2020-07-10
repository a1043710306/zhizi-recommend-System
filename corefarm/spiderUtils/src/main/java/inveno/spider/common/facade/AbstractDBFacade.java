package inveno.spider.common.facade;

import tw.qing.lwdba.DBFacade;
import tw.qing.lwdba.QueryResult;
import tw.qing.lwdba.TransSQLExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Genix.Li on 2016/05/16.
 */
public abstract class AbstractDBFacade extends DBFacade
{
	public static final String DBNAME_DEFAULT   = "default";
	public static final String DBNAME_WECHAT    = "wechat";
	public static final String DBNAME_CRAWLER   = "crawler";
	public static final String DBNAME_DASHBOARD = "dashboard";

	public AbstractDBFacade(String dbName) throws SQLException, ClassNotFoundException
	{
		super(dbName);
	}

	protected ArrayList executeQueryRows(String sql, TransSQLExecutor tse) throws SQLException
	{
		ResultSet rs = tse.executeQuery(sql);
		QueryResult qr = new QueryResult(rs, 0, -1, true);
		return qr.getRows();
	}
}
