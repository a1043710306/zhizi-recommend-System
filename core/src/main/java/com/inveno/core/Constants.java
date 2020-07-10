package com.inveno.core;


import java.util.ArrayList;
import java.util.List;

import com.inveno.core.util.Config;

public class Constants {

    public static final String REDIS_KEY_PREFIX_EXPINFO = "expinfo";

    public static final String SEPARATOR_REDIS_KEY      = "_";

    /**
     * 用于缓存中多个字段拼接时候的分隔符
     */
    public static final String CHAR_SPLIT="|";
    
	public static final String DEFAULT_VALUE="CHN|zh_cn";
	
    public static final String STR_SPLIT_PRE = "::";
    
    public static final String STR_SPLIT_PRE_LINE = "_";
    
    //cache的q名字
    public static final String CACHE_NAME_Q = "q";
    
    //cache的qb名字
    public static final String CACHE_NAME_QB = "qb";
    
    //cache的qcn名字
    public static final String CACHE_NAME_QCN = "qcn";
    
    //cache的offset
    public static final String CACHE_NAME_OFFSET = "offset";
    
    public static final String STR_ALL="all";
    
    //scenario map
    public static final String SCENARIO_CATEGORYID_MAP = "scenario_categoryid_map";
    
    //scenario优先加载的数量
    public static final int SCENARIO_PRIORITY_NUM = 5;
    
    //中文
    public static final String ZH_CN = "zh_CN";
    
    public static final String MONITOR_IP;
    
    public static final String DEFAULT_LANGUAGE;
    
    //public static boolean doImpressionTrigger = false;
    
    public static String monitorIpPrefix = "192.168.1";

    public static final String CONFIG_SEGMENT_PRIMARY_SELECTION = "gmp";
    public static final String CONFIG_SEGMENT_CORE = "core";
    public static final String CONFIG_SEGMENT_GBDT = "GBDT";

    public static boolean ENABLE_EXPLORE_TEST = false;

    public static final String LOG_TAG = "&&";
    public static final String START_LOG_TAG = "||";
    //置顶
    public static final long TOPFORCEINSERT = 0x1;

    public static final int CALLRELATEDTYPE_PUBLISHER = 1;
    static
    {
        Config config = new Config("core-config.properties");
        
        monitorIpPrefix = config.getProperty("monitor.ip.prefix");

        MONITOR_IP = config.getProperty("monitor.ip");
        
        DEFAULT_LANGUAGE = (config.getProperty("default.language") == null ?"zh_cn":config.getProperty("default.language"));
        
        ENABLE_EXPLORE_TEST = Boolean.valueOf(config.getProperty("core.explore.test"));
    }
}
