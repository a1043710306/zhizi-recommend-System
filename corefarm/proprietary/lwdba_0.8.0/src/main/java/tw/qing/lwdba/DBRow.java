package tw.qing.lwdba;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class DBRow
{
	private String databaseType = "default";
	private String tableName;
	private String pkName[];
	private HashMap hmRow = new HashMap();
	//
	public DBRow(String _tableName, String _pkName)
	{
		this(_tableName, _pkName, "default");
	}
	public DBRow(String _tableName, String _pkName[])
	{
		this(_tableName, _pkName, "default");
	}
	public DBRow(String _tableName, String _pkName, String _databaseType)
	{
		databaseType = _databaseType;
		tableName = _tableName;
		pkName = new String[1];
		pkName[0] = _pkName;
	}
	public DBRow(String _tableName, String _pkName[], String _databaseType)
	{
		databaseType = _databaseType;
		tableName = _tableName;
		pkName = _pkName;
	}
	public String getTableName()
	{
		return tableName;
	}
	public String getPKName()
	{
		if( pkName == null || pkName.length <= 0 )
			return null;
		return pkName[0];
	}
	public String[] getPKNames()
	{
		return pkName;
	}
	public void setColumn(String columnName, Object value)
	{
		hmRow.put(columnName, value);
	}
	public Object getColumn(String columnName)
	{
		return hmRow.get(columnName);
	}
	public void removeColumn(String columnName)
	{
		hmRow.remove(columnName);
	}
	public HashMap getRow()
	{
		return hmRow;
	}
	public void setRow(HashMap hm)
	{
		hmRow = hm;
	}
	public void setPKValue(Object o)
	{
		setColumn(pkName[0], o);
	}
	public Object getPKValue()
	{
		return getColumn(pkName[0]);
	}
	public Object[] getPKValues()
	{
		Object o[] = new Object[pkName.length];
		for(int i=0;i<o.length;i++)
			o[i] = getColumn(pkName[i]);
		return o;
	}
	public String[] listColumnName()
	{
		Set ks = hmRow.keySet();
		String s[] = new String[ks.size()];
		Iterator it = ks.iterator();
		for(int i=0;i<s.length;i++)
			s[i] = (String) it.next();
		return s;
	}
	public String toInsertString()
	{
		String col[] = listColumnName();
		//
		StringBuffer sb = new StringBuffer("insert into ");
		sb.append(tableName);
		sb.append("(");
		for(int i=0;i<col.length;i++)
		{
			if( i > 0 )
				sb.append(", ");
			sb.append(col[i]);
		}
		sb.append(") values(");
		for(int i=0;i<col.length;i++)
		{
			if( i > 0 )
				sb.append(", ");
			Object o = getColumn(col[i]);
			sb.append(SQLUtil.getSQLValue(o, databaseType));
		}
		sb.append(")");
		return sb.toString();
	}
	public String toDeleteString()
	{
		StringBuffer sb = new StringBuffer("delete from ");
		sb.append(tableName);
		sb.append(" where ");
		for(int i=0;i<pkName.length;i++)
		{
			Object pk = getColumn(pkName[i]);
			if( i > 0 )
				sb.append(" and ");
			sb.append(pkName[i]);
			sb.append("=");
			sb.append(SQLUtil.getSQLValue(pk, databaseType));
		}
		return sb.toString();
	}
	public String toUpdateString()
	{
		String col[] = listColumnName();
		//
		StringBuffer sb = new StringBuffer("update ");
		sb.append(tableName);
		sb.append(" set ");
		boolean f = false;
		for(int i=0;i<col.length;i++)
		{
			boolean fPK = false;
			for(int j=0;j<pkName.length;j++)
			{
				if( col[i].equalsIgnoreCase(pkName[j]) )
				{
					fPK = true;
					break;
				}
			}
			if( fPK )
				continue;
			if( f )
				sb.append(", ");
			//
			f = true;
			//
			sb.append(col[i]);
			sb.append("=");
			Object o = getColumn(col[i]);
			sb.append(SQLUtil.getSQLValue(o, databaseType));
		}
		sb.append(" where ");
		for(int i=0;i<pkName.length;i++)
		{
			Object pk = getColumn(pkName[i]);
			if( i > 0 )
				sb.append(" and ");
			sb.append(pkName[i]);
			sb.append("=");
			sb.append(SQLUtil.getSQLValue(pk, databaseType));
		}
		return sb.toString();
	}
	public String toFindString()
	{
		StringBuffer sb = new StringBuffer("select * from ");
		sb.append(tableName);
		sb.append(" where ");
		for(int i=0;i<pkName.length;i++)
		{
			Object pk = getColumn(pkName[i]);
			if( i > 0 )
				sb.append(" and ");
			sb.append(pkName[i]);
			sb.append("=");
			sb.append(SQLUtil.getSQLValue(pk, databaseType));
		}
		return sb.toString();
	}
	public String toQueryString()
	{
		String col[] = listColumnName();
		//
		StringBuffer sb = new StringBuffer("select * from ");
		sb.append(tableName);
		if( col.length > 0 )
		{
			sb.append(" where ");
			for(int i=0;i<col.length;i++)
			{
				Object o = getColumn(col[i]);
				//
				if( i > 0 )
					sb.append(" and ");
				sb.append(col[i]);

				if( o instanceof NullColumnValue )
				{
					sb.append(" is null");
				}
				else
				{
					sb.append(" = ");
					sb.append(SQLUtil.getSQLValue(o, databaseType));
				}
			}
		}
		return sb.toString();
	}
	public String toString()
	{
		return hmRow.toString();
	}
}