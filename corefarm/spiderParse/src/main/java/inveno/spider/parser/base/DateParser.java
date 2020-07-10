package inveno.spider.parser.base;

import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;


public class DateParser {
	private static final Logger log = LoggerFactory.make();
	
	   /**
     * Default time zone is Chinese,so the time offset is zero. 
     */
    public static final int TIME_OFFSET_CHINA=0;
	
	private static final Pattern datePattern = Pattern.compile("\\d{2,4}[-/年\\.]\\d{1,2}[-/月\\.]\\d{1,2}[日]?((\\s)*\\d{1,2}:\\d{1,2})?");
    //Because the SimpleDateFormat is not thread safe,so it cann't be static.
	private final DateFormat[] simpleFormats = new DateFormat[] {
    	new SimpleDateFormat("yy-MM-dd HH:mm"),
        new SimpleDateFormat("yy-MM-dd"),
        new SimpleDateFormat("yy年MM月dd日 HH:mm"),
        new SimpleDateFormat("yy年MM月dd日"),
        new SimpleDateFormat("yy/MM/dd HH:mm"),
        new SimpleDateFormat("yy/MM/dd"),
        new SimpleDateFormat("yy.MM.dd"),
        new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z",Locale.US),
        
    };
    
    private static final String[] relativeSecondStrings=new String[]{"秒前","秒以前"};
    private static final String[] relativeMinuteStrings=new String[]{"分钟前","分鐘前","分钟以前","分鐘以前","分前"};
    private static final String[] relativeHourStrings = new String[] { "小时前",
            "小時前", "小时以前", "小時以前", "个小时前", "個小時前", "个小时以前", "個小時以前", "钟头前",
            "鐘頭前", "钟头以前","鐘頭以前", "个钟头前","個鐘頭前", "个钟头以前","個鐘頭以前","小时之前","小時之前","hour ago","hours ago"  };
    private static final String[] relativeDayStrings=new String[]{"天前","日前","天以前","日以前","日之前","day ago","days ago"};
    
    private static final String[] todayStrings = new String[]{"今天", "今日", "TODAY", "Today", "today","24小時內","24小时内","刚刚"};
    private static final String[] yesterdayStrings = new String[]{"昨天", "昨日", "YESTERDAY", "Yesterday", "yesterday"};

    //提取到的日期是否可为null，如此标记为true则返回null,否则报异常
    private boolean isDateCanNull=false;
    
    private int timeOffset;
    
    public DateParser(int timeOffset)
    {
        this.timeOffset = timeOffset;
    }
    
    public Date extractDate(String dateStr, ParseStrategy.DateExtractionStrategy strategy, String customDateFormat, String country) throws ExtractException {
        dateStr = Utils.replaceSpace(dateStr);
        dateStr = replaceRelativeStringWithDate(dateStr);
        
        if(strategy==ParseStrategy.DateExtractionStrategy.Simple) {
            for (String todayStr : todayStrings) {
                if(dateStr.contains(todayStr))
                    dateStr = replaceTodayWithDate(dateStr, todayStr, simpleFormats[1]);
            }
            for (String yesterdayStr : yesterdayStrings) {
                if(dateStr.contains(yesterdayStr))
                    dateStr = replaceYesterdayWithDate(dateStr, yesterdayStr, simpleFormats[1]);
            }
        		
        	String date = getDate(dateStr);
            return getTimeByTimeOffset(simpleExtract(date));
        } else if(strategy==ParseStrategy.DateExtractionStrategy.Custom) {
            if(customDateFormat==null || "".equals(customDateFormat)) throw new ExtractException("Invalid date format " + customDateFormat);
            
            Locale locale = null;
            if (country != null && "China".equalsIgnoreCase(country))
                locale = Locale.CHINA;
            else if (country != null && "ChinaTw".equalsIgnoreCase(country))
            {
                locale = Locale.CHINA;
                String[] yy = dateStr.trim().split("-");
                int YY = Integer.parseInt(yy[0]) + 1911;
                dateStr = dateStr.replace(yy[0], YY + "");
            } else
                locale = Locale.US;

            SimpleDateFormat customDf = new SimpleDateFormat(customDateFormat, locale);
            for (String todayStr : todayStrings) {
                if(dateStr.contains(todayStr))
                    dateStr = replaceTodayWithDate(dateStr, todayStr, customDf);
            }
            for (String yesterdayStr : yesterdayStrings) {
                if(dateStr.contains(yesterdayStr))
                    dateStr = replaceYesterdayWithDate(dateStr, yesterdayStr, customDf);
            }
            
            return getTimeByTimeOffset(customExtract(dateStr, customDateFormat, customDf));
        } else {
            throw new ExtractException("No such strategy " + strategy);
        }
    }
    
    /**
     * Get time by the time offset with China.
     * @param date
     * @return
     */
    private Date getTimeByTimeOffset(Date date)
    {
        if(null==date)
        {
            return null;
        }
        return DateUtils.addHours(date, this.timeOffset);
    }
    
    public String replaceTodayWithDate(String dateStr, String todayStr, DateFormat df){
    	String today = df.format(new Date());
    	return dateStr.replaceAll(todayStr, " " + today + " ");
    }
    
    public String replaceYesterdayWithDate(String dateStr, String yesterdayStr, DateFormat df){
        String yesterday = df.format(DateUtils.addDays(new Date(), -1));
        return dateStr.replaceAll(yesterdayStr, " " + yesterday + " ");
    }
    
    /**
     * 处理相对时间
     * @param dateStr
     * @return
     */
    private String replaceRelativeStringWithDate(String dateStr) throws ExtractException {
    	if(StringUtils.isBlank(dateStr)) return dateStr;
    	
    	//纯数字时间，一般为13位数字
    	dateStr=dateStr.trim();
    	if(dateStr.matches("\\d{13}")){
    		return Utils.formatDate(new Date(Long.parseLong(dateStr)), "yyyy-MM-dd HH:mm:ss");
    	}
    	
    	int relatives=0;
    	for (String secondStr : relativeSecondStrings) {//以秒来描述相对值
			if(dateStr.contains(secondStr)){
				relatives=extractRelativeDate(dateStr,secondStr);
                dateStr = Utils.formatDate(DateUtils.addSeconds(new Date(), 0-relatives), "yyyy-MM-dd HH:mm:ss");
                return dateStr;
			}
        }
    	
    	for (String minuteStr : relativeMinuteStrings) {//以分来描述相对值
			if(dateStr.contains(minuteStr)){
				relatives=extractRelativeDate(dateStr,minuteStr);
                dateStr = Utils.formatDate(DateUtils.addMinutes(new Date(), 0-relatives), "yyyy-MM-dd HH:mm:ss");
                return dateStr;
			}
        }
    	
    	for (String hourStr : relativeHourStrings) {//以时来描述相对值
			if(dateStr.contains(hourStr)){
				relatives=extractRelativeDate(dateStr,hourStr);
                dateStr = Utils.formatDate(DateUtils.addHours(new Date(), 0-relatives), "yyyy-MM-dd HH:mm:ss");
                return dateStr;
			}
        }
    	
    	for (String dayStr : relativeDayStrings) {//以日来描述相对值
			if(dateStr.contains(dayStr)){
				relatives=extractRelativeDate(dateStr,dayStr);
                dateStr = Utils.formatDate(DateUtils.addDays(new Date(), 0-relatives), "yyyy-MM-dd HH:mm:ss");
                return dateStr;
			}
        }
    	
    	//默认，不处理
    	return dateStr;
    }
    
    private int extractRelativeDate(String dateStr,String relativeStr) throws ExtractException {
    	//return Integer.parseInt(Utils.getRegExpReplace(dateStr, ".*(\\d+)"+relativeStr+".*", "\\1", "false"));
        return Integer.parseInt(Utils.getRegExpReplace(dateStr, ".*(\\d+).*"+relativeStr+".*", "\\1", "false"));
    }
    
    public String getDate(String dateStr) throws ExtractException {
    	Matcher m = datePattern.matcher(dateStr);
    	if(m.find()){
    		String date = m.group();
    		return date;
    	}else{
    		String message="Cannot find date in " + dateStr;
    		if(isDateCanNull){
    			log.info(message);
        		return null;
    		}else{
    			throw new ExtractException(message);
    		}
    	}
    }

    private Date simpleExtract(String dateStr) throws ExtractException {
        if (null != dateStr)
        {
            for (DateFormat df : simpleFormats)
            {
                try
                {
                    return df.parse(dateStr);
                } catch (ParseException e)
                {
                    continue;
                }
            }
        }

        String message = dateStr + " cannot be extracted with strategy "
                + ParseStrategy.DateExtractionStrategy.Simple;
        if (isDateCanNull)
        {
            log.info(message);
            return null;
        } else
        {
            throw new ExtractException(message);
        }
    }
    
    private Date customExtract(String dateStr, String customDateFormat, DateFormat df) throws ExtractException {
        String originalDateStr = dateStr;
        dateStr = StringUtils.trim(dateStr);
        dateStr = removeDateSuffix(dateStr);
        
        ParseException ex = null;
        while(!"".equals(dateStr)) {
            try {
                return df.parse(dateStr);
            } catch (ParseException e) {
                ex = e;
                dateStr = StringUtils.substringAfter(dateStr, " ");
                continue;
            }
        }
        
        String message=originalDateStr + " cannot be extracted with " + customDateFormat;
		if(isDateCanNull){
			log.info(message,ex);
    		return null;
		}else{
			throw new ExtractException(message,ex);
		}
    }
    
    private String removeDateSuffix(String dateStr) {
        return dateStr.replaceAll("(\\d)(th|st|nd|rd)", "$1");
    }

	public boolean isDateCanNull() {
		return isDateCanNull;
	}

	public void setDateCanNull(boolean isDateCanNull) {
		this.isDateCanNull = isDateCanNull;
	}

    public int getTimeOffset()
    {
        return timeOffset;
    }
}
