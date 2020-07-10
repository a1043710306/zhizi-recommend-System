package tw.qing.lwdba;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import tw.qing.util.CaseInsensitiveHashMap;

public class QueryResult
{
	private ArrayList alRow = new ArrayList();
	private int nTotalRow = -1;
	//
	public ArrayList getRows()
	{
		return alRow;
	}
	public int getTotalRowCount()
	{
		return nTotalRow;
	}
	public QueryResult(ResultSet rs, int _nTotalRow, int nMax, boolean columnCaseSensitive) throws SQLException
	{
		nTotalRow = _nTotalRow;
		//
		ResultSetMetaData rsmd = rs.getMetaData();
		//
		int nColumn = rsmd.getColumnCount();
		//
		int n = 0;
		while( rs.next() )
		{
			HashMap hm = null;
			if( columnCaseSensitive )
				hm = new HashMap();
			else
				hm = new CaseInsensitiveHashMap();
			//
			for(int i=1;i <= nColumn;i++)
			{
				hm.put(rsmd.getColumnName(i), rs.getObject(i));
			}
			alRow.add(hm);
			n++;
			if( nMax > 0 && n >= nMax)
				break;
		}
	}
}