package tw.qing.lwdba;

import java.sql.Timestamp;
import java.util.Date;

import tw.qing.util.PrimitiveTypeUtil;

public class SQLUtil
{
	public static String getSQLValue(Object o, String databaseType)
	{
		return getSQLValue(o, null, databaseType);
	}
	public static String getSQLValue(Object o, Class type, String databaseType)
	{
		if( o == null )
			return "null";
		if( type != null )
			o = PrimitiveTypeUtil.castValue(type, o);
		if( o instanceof SQLString || type == SQLString.class)
			return o.toString();
		else if( o instanceof byte[] )
            return escapeSQL(new String((byte[]) o));
		else if( o instanceof java.util.Date )
			return getDateSQLValue((Date)o, databaseType);
		else if( o instanceof Character || Character.TYPE.isInstance(o) )
		{
			StringBuffer sb = new StringBuffer();
			sb.append("'");
			sb.append(o.toString().charAt(0));
			sb.append("'");
			return sb.toString();
		}
		else if( o instanceof String )
		{
			StringBuffer sb = new StringBuffer();
			if (databaseType.equalsIgnoreCase("mssql"))
				sb.append("N");
			sb.append("'");
			sb.append(escapeSQL(o.toString()));
			sb.append("'");
			return sb.toString();
		}
		return o.toString();
	}
	public static String[] getSQLValue(Object[] o, String databaseType)
	{
		String[] result = new String[o.length];
		for (int i=0; i<o.length; i++)
			result[i] = getSQLValue(o[i], databaseType);
		return result;
	}
	public static String getDateSQLValue(Date date, String databaseType)
	{
		if( databaseType.equalsIgnoreCase("oracle") )
			return getDateSQLValue(date, true);
		return getDateSQLValue(date, false);
	}
	public static String getDateSQLValue(Date date, boolean fOracle)
	{
		String s = new Timestamp(date.getTime()).toString();
		if( fOracle )
		{
			StringBuffer sb = new StringBuffer("TO_DATE('");
			int p = s.lastIndexOf('.');
			if( p > 0 )
				s = s.substring(0, p);
			sb.append(s);
			sb.append("', 'YYYY-MM-DD HH24:MI:SS')");
			return sb.toString();
		}
		else
		{
			StringBuffer sb = new StringBuffer("'");
			sb.append(s);
			sb.append("'");
			return sb.toString();
		}
	}
	public static String escapeSQL(String s)
	{
		return escapeSQLSingleQuote(s);
	}

	public static String escapeSQLSingleQuote(String s)
	{
		int p = s.indexOf('\'');
		if (p < 0)
			return s;
		return s.replaceAll("'", "\\\\'");
	}
}
