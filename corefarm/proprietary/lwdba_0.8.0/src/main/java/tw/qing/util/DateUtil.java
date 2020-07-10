package tw.qing.util;

import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

public class DateUtil
{
	public static String dateToString(Date date)
	{
		try
		{
			return DateFormat.getInstance().format(date);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static String dateToString(Date date, String pattern)
	{
		SimpleDateFormat format;
		//
		Calendar c = Calendar.getInstance();
		c.clear();
		c.setTime(date);
		//
		try
		{
			format = new SimpleDateFormat(pattern);
			return format.format(c.getTime());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
	public static Date stringToDate(String s)
	{
		if( s == null )
			return new Date();
		StringBuffer format = new StringBuffer();

		if( s.length() >= 8 )
		{
			if(s.indexOf('-')>=0)
				format.append("yyyy-MM-dd");
			else if(s.indexOf('/')>=0)
				format.append("yyyy/MM/dd");
			else
				format.append("yyyyMMdd");
			if (s.indexOf(':')>=0)
				format.append(" HH:mm:ss");
		}
		else
		{
			if(s.indexOf('-')>=0)
				format.append("yyyy-MM");
			else if(s.indexOf('/')>=0)
				format.append("yyyy/MM");
			else
				format.append("yyyyMM");
		}
		return stringToDate(s, format.toString());
	}
	public static Date stringToDate(String s, String pattern)
	{
		SimpleDateFormat format;
		ParsePosition pos = new ParsePosition(0);
		//
		try
		{
			format = new SimpleDateFormat(pattern);
			return format.parse(s, pos);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}