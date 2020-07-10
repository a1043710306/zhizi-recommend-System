package com.inveno.feeder.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.inveno.feeder.constant.FeederConstants;

public class DateUtils
{
	private static final Logger log = Logger.getLogger(DateUtils.class);

	public static String getStringDateFormat(Object datetime, String pattern)
	{
		String strReturnDatetime = "";

		if (datetime != null)
		{
			if (datetime instanceof Long)
			{
				SimpleDateFormat fmt = new SimpleDateFormat(pattern);
				strReturnDatetime = fmt.format(new Date((long)datetime));
			}
			else
			{
				strReturnDatetime = datetime.toString();
			}
		}

		return strReturnDatetime;
	}
	public static String dateToString(Date date)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(FeederConstants.DATE_TIMESTAMP_FORMAT);
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return fmt.format(date);
	}
	public static long stringToTimestamp(String strDate)
	{
		return (long)(stringToTimestampMillis(strDate)/1000L);
	}
	public static long stringToTimestampMillis(String strDate)
	{
		long ts = 0;
		Date date = stringToDate(strDate, null);
		if (date != null)
			ts = date.getTime();
		return ts;
	}
	public static Date stringToDate(String strDate)
	{
		return stringToDate(strDate, null);
	}
	public static Date stringToDate(String strDate, Date defaultValue)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(FeederConstants.DATE_TIMESTAMP_FORMAT);
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		Date date = defaultValue;
		if (StringUtils.isNotEmpty(strDate))
		{
			try
			{
				date = fmt.parse(strDate);
			}
			catch (ParseException e)
			{
				log.error("[stringToDate]", e);
			}
		}
		return date;
	}
}