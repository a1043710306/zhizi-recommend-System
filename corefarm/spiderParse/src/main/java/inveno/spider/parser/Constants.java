package inveno.spider.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import inveno.spider.common.utils.Config;

public class Constants
{

    public static final Date EARLIEST_DATE;
    
    // max number of error events
    // stored in report, -1 for
    // unlimited
    public static final int REPORT_MAX_EVENTS;
    
    public static final String TIMEOUT_THRESHHOLD;
    public static final String MAX_ERROR_TIMES_PER_PAGE;
    public static final int PARSER_CONCURRENT_JOBS;
    public static final int PARSER_PROFILE_CONCURRENT_WAITING_NUMBERS; // max number of a profile on waiting
    public static final boolean START_ON_STARTUP;


    static
    {
        Config config = new Config("spider-parser.properties");
        try
        {
            EARLIEST_DATE = new SimpleDateFormat("yyyyMMdd").parse("20130101");
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
        
        TIMEOUT_THRESHHOLD = config.getPropertyNotEmpty("parser.timeout.threshhold");
        MAX_ERROR_TIMES_PER_PAGE = config.getPropertyNotEmpty("parser.max.error.times.per.page");
        
        REPORT_MAX_EVENTS = config
                .getIntPropertyNotEmpty("parser.report.max.events");

        PARSER_CONCURRENT_JOBS = config
                .getIntPropertyNotEmpty("parser.concurrent.jobs");
        
        PARSER_PROFILE_CONCURRENT_WAITING_NUMBERS = config
                .getIntPropertyNotEmpty("parser.profile.concurrent.waiting.numbers");   
        START_ON_STARTUP = config.getBooleanPropertyNotEmpty("parser.start.on.startup");
    }

}
