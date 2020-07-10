package com.inveno.feeder;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Language;
import com.inveno.common.util.ContextUtils;
import com.inveno.feeder.constant.FeederConstants;
import com.inveno.feeder.datainfo.CategoryInfo;
import com.inveno.feeder.datainfo.EditorTableEntry;
import com.inveno.feeder.datainfo.InteractionInfo;
import com.inveno.feeder.datainfo.SourceSubscriptionEntry;
import com.inveno.feeder.filter.ExpinfoCalModel;
import com.inveno.feeder.filter.ExpinfoFilter;
import com.inveno.feeder.infomapper.*;
import com.inveno.feeder.jsondatastruct.*;
import com.inveno.feeder.jsoninfomapper.JsonInfoMapperDruid;
import com.inveno.feeder.jsoninfomapper.JsonInfoMapperHDFS;
import com.inveno.feeder.model.GmpBean;
import com.inveno.feeder.model.HwIdBean;
import com.inveno.feeder.model.HwRequestBean;
import com.inveno.feeder.queue.ClientKafkaProducer;
import com.inveno.feeder.queue.TaskQueueProducer;
import com.inveno.feeder.task.ScheduledTaskWatchADTable;
import com.inveno.feeder.thrift.ChapterInfo;
import com.inveno.feeder.thrift.FeederInfo;
import com.inveno.feeder.util.*;
import com.inveno.thrift.Info;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.monitor.GaugeMonitor;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class Feeder
{
	private static final Logger flowLogger    = Logger.getLogger("feeder.flow");
	private static final Logger articleLogger = Logger.getLogger("feeder.article");
	private static final Logger expinfoLogger = Logger.getLogger("feeder.expinfo");
	private static final Logger monitorLogger = Logger.getLogger("feeder.monitor");
	private static final Logger expinfo_lifecycle_logger = Logger.getLogger("feeder.lifecycle");
	private static final Logger expinfo_remove_logger = Logger.getLogger("feeder.remove");

	private static final String PROPERTIES_FILE_TIMESTAMP  = "timestamp.properties";
	private static final String PROPERTIES_FILE_CONNECTION = "connection-info.properties";
	private static final String PROPERTIES_FILE_CONFIG     = "configs.properties";

	public static final int STATUS_GROUP_INACTIVE = 0;
	public static final int STATUS_GROUP_ACTIVE   = 1;

	private static boolean ENABLE_NOTIFY_HONEYBEE = false;

	private static boolean bOnline = false;

	private static boolean bWatchADTable = false;

	private static boolean bOversea = false;

	private static boolean bWriteRedis = true;

	private static boolean bWriteES = false;

	private static boolean bWriteDruid = false;

	private static boolean bWriteHDFS = false;

	private static boolean bEnableContentGroup = false;

	private static boolean bEnableFetchActiveItem = false;

	//private static Jedis jedis;
	//redis cluster for detail
	private static JedisCluster jedisDetail;

	//redis cluster for cache
	private static JedisCluster jedisCache;

	//private static SSDB ssdb;

	private static Client esClient;

	private static ClientJDBCTemplate clientJDBCTemplate;

	private static ClientJDBCTemplateComment clientJDBCTemplateComment;

	private static ClientKafkaConsumer clientKafka = null;

	private static String strLocalFilePath;

	private static List<String> alLanguage = new ArrayList<String>();

	//rate映射和rate系数映射
	private static Map<String,Double> RATE_MAPPING = new HashMap<String,Double>();

	private static List<CategoryInfo> listCategoryInfo = new ArrayList<CategoryInfo>();

	private static Map<String, Map<Integer, Integer>> mContentTypeCategoryExpiry = new HashMap<String, Map<Integer, Integer>>();

	private static boolean is_using_kafka = false;
	private static String strKafkaBootstrapServers;
	private static String strKafkaGroupID;
	private static String strKafkaTopic;

	private static final long EXPINFO_EFFECTIVE_TIME_CONSTRAINT_NEWS  = 24*60*60*1000;
	private static final long EXPINFO_EFFECTIVE_TIME_CONSTRAINT_VIDEO = 48*60*60*1000;
	private static final long EXPINFO_EFFECTIVE_TIME_CONSTRAINT_MEME  = 48*60*60*1000;
	private static final long EXPINFO_EFFECTIVE_TIME_CONSTRAINT_GIF  = 48*60*60*1000;
	private static final long EXPINFO_EFFECTIVE_TIME_CONSTRAINT_COMIC = 168*60*60*1000;
	private static final int DEFAULT_REDIS_EXPIRE = 30*86400;

	//config.properties
	private static String  FLOW_EXPINFO_ABTEST_OPEN = "noticias:Spanish";
	private static boolean FLOW_EXPINFO_MAIN_SWITCH = false;
	private static String  FLOW_CHANNEL_LANGUAGES   = "";
	private static Set<String>  EFFECTIVE_FLOW_EXPLORE_APP = new HashSet<String>();
	private static List<String> FLOW_EXPINFO_BLACKLIST = new ArrayList<String>();

	private static double  ATTENUATION_COEFFICIENT  = 0.9983;
	private static boolean SET_UPDATE_TIMEOUT_KEY   = false;
	private static boolean DUMP_FETCHED_ARTICLE_ID  = false;
	private static boolean EXPINFO_BYPASS_ACS = false;

	private static int CATEGORY_SYSTEM_VERSION = 1;
	private static String LANGUAGE_VERSION = "v1";
	private static String CATEGORY_VERSION = "v1";
	private static String ADULT_SCORE_VERSION = "v1";
	private static String ADULT_SCORE_VALUE   = "0.6";
	private static Map<String, Double> ADULT_SCORE_MAP = new HashMap<String, Double>();
	private static boolean IS_OPEN_DELAY_DELETE = false;
	private static int DELAY_DELETE_TIMEOUT     = 600;
	private static int DELAY_DELETE_EDIT_TYPE   = 127;
	private static final long MILLISECONDS_IN_HOUR = 3600000L;
	private static String DUMP_GMP_CSV_PATH = "/data/feeder/train_data.csv";
	
	//探索-等待池文章的最大等待时间 暂时只考虑news video
	private static  long EXPINFO_WAITING_MAX_TIME_NEWS  = 24*60*60*1000;
	private static  long EXPINFO_WAITING_MAX_TIME_VIDEO = 48*60*60*1000;	
	private static  long EXPINFO_WAITING_MAX_TIME_MEME  = 48*60*60*1000;
	private static  long EXPINFO_WAITING_MAX_TIME_GIF   = 48*60*60*1000;
	
	//完成探索所需要的下发或展示次数
	private static int EXPINFO_NEWS_THRESHOLD = 50;
	private static int EXPINFO_VIDEO_THRESHOLD = 30;
	private static int EXPINFO_MEME_THRESHOLD = 100;
	private static int EXPINFO_GIF_THRESHOLD = 100;

	//HW渠道特殊处理
	private static String HW_NEW_INFO_LSIT_KEY = "hw_new_info_list";
	private static String HW_FROM_APP = "huaweiglobal";
	private static String HW_CATEGORY_VERSION = "v28";
	private static String HW_BLOCK_FINISH_KEY = "hw_block_finish_list";
	private static String HW_BLOCK_CHECK_KEY = "hw_block_check_list";
	private static String HW_SECRETKEY = "Inveno_g2sYxpkHUydeJXAsk8D9bBHWH7d29dlG";
	private static String HW_ORGANIZATION = "Inveno";
	private static String HW_URI = "https://49.4.67.44:8443/huawei_browser_docblocknotify";

	//CTR 流量探索优化
    private static String FLOW_EXPLORE_PRODUCT = "noticiaslite";
	
	private static String MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES =
			"noticias=Spanish,noticiasusa=Spanish,noticiasboom=Spanish,noticiasboomchile=Spanish,memesteca=Spanish,noticiaslite=Spanish,hormona=Spanish,impactohoy=Spanish"
					+ ",noticias=Spanish_video,noticiasusa=Spanish_video,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_video"
					+ ",noticias=Spanish_gif,noticiasusa=Spanish_gif,noticiasboom=Spanish_video,noticiasboomchile=Spanish_video,memesteca=Spanish_video,noticiaslite=Spanish_video,hormona=Spanish_video,impactohoy=Spanish_gif"
					+ ",noticias=Spanish_meme,noticiasusa=Spanish_meme,noticiasboom=Spanish_meme,noticiasboomchile=Spanish_meme,memesteca=Spanish_meme,noticiaslite=Spanish_meme,hormona=Spanish_meme,impactohoy=Spanish_meme";
	private static Object[] logParam = null;
	private static String LOG_TAG = "&";
	
	private static String EVENT_REMOVE_WAITING_EXPLORE = "event_remove_waiting_explore";
	private static String EVENT_REMOVE_EXPLORE = "event_remove_explore";
	
	public static void storeProperties(Properties props, String properties_file_name, String comments)
	{
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(new File(properties_file_name));
			props.store(writer, comments);
		}
		catch (Exception e)
		{
			flowLogger.error("\t\t  start init write " + properties_file_name + " error ...", e);
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.flush();
					writer.close();
				}
				catch (Exception e)
				{
					//ignore
				}
			}
		}
	}
	public static Properties loadProperties(String properties_file_name)
	{
		FileReader reader = null;
		Properties props = new Properties();
		try
		{
			reader = new FileReader(new File(properties_file_name));
			props.load(reader);
		}
		catch (Exception e)
		{
			flowLogger.error("\t\t  start init read " + properties_file_name + " error ...", e);
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				//ignore
			}
		}
		return props;
	}
	private static void loadConfigFiles()
	{
		try
		{
			Properties props = loadProperties(PROPERTIES_FILE_CONFIG);

			String strLanguageList = props.getProperty("language_list");
			if (StringUtils.isNotEmpty(strLanguageList))
			{
				for (String language : strLanguageList.split(","))
				{
					alLanguage.add(language);
				}
			}
			flowLogger.info("language_list=" + alLanguage);
		
			IS_OPEN_DELAY_DELETE = Boolean.valueOf(props.getProperty("is_open_delay"));
			DELAY_DELETE_TIMEOUT = Integer.valueOf(props.getProperty("delay_time_out"));
			DELAY_DELETE_EDIT_TYPE = Integer.valueOf(props.getProperty("editor_type"));

			HW_URI = props.getProperty("hw_url");

			CATEGORY_SYSTEM_VERSION = NumberUtils.toInt(props.getProperty("category_system_version"), 1);
			CATEGORY_VERSION = props.getProperty("category_version");
			LANGUAGE_VERSION = props.getProperty("language_version");
			FLOW_EXPINFO_ABTEST_OPEN = props.getProperty("flow_expinfo_abtest_open");
			FLOW_EXPINFO_MAIN_SWITCH = Boolean.valueOf(props.getProperty("flow_expinfo_main_switch"));
			ENABLE_NOTIFY_HONEYBEE = "true".equalsIgnoreCase(props.getProperty("enable_notify_honeybee")) ? true : false;
			FLOW_CHANNEL_LANGUAGES  = props.getProperty("channellanguages");
            FLOW_EXPLORE_PRODUCT = props.getProperty("flow_explore_product");
			EXPINFO_WAITING_MAX_TIME_NEWS  = NumberUtils.toLong(props.getProperty("expinfo_waiting_max_time_news"),24*60*60*1000);
			EXPINFO_WAITING_MAX_TIME_VIDEO = NumberUtils.toLong(props.getProperty("expinfo_waiting_max_time_video"),48*60*60*1000);
			EXPINFO_WAITING_MAX_TIME_MEME  = NumberUtils.toLong(props.getProperty("expinfo_waiting_max_time_meme"),48*60*60*1000);
			EXPINFO_WAITING_MAX_TIME_GIF = NumberUtils.toLong(props.getProperty("expinfo_waiting_max_time_gif"),48*60*60*1000);
			
			String strExplinfoWaitingProductChannelLanguages = props.getProperty("expinfo_waiting_product_channel_languages");
			if (StringUtils.isNotEmpty(strExplinfoWaitingProductChannelLanguages)){
				MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES = strExplinfoWaitingProductChannelLanguages;
			}
			EXPINFO_NEWS_THRESHOLD = NumberUtils.toInt(props.getProperty("expinfo_news_threshold"), 50);
			EXPINFO_VIDEO_THRESHOLD = NumberUtils.toInt(props.getProperty("expinfo_video_threshold"), 30);
			EXPINFO_MEME_THRESHOLD = NumberUtils.toInt(props.getProperty("expinfo_meme_threshold"), 100);
			EXPINFO_GIF_THRESHOLD = NumberUtils.toInt(props.getProperty("expinfo_gif_threshold"), 100);
		
			
			String strAttenuationCoefficient = props.getProperty("attenuationcoefficient");
			if (StringUtils.isNotEmpty(strAttenuationCoefficient))
			{
				ATTENUATION_COEFFICIENT = Double.valueOf(strAttenuationCoefficient);
			}

			SET_UPDATE_TIMEOUT_KEY = Boolean.valueOf(props.getProperty("is-set-updated-timoutkey"));
			DUMP_FETCHED_ARTICLE_ID = Boolean.valueOf(props.getProperty("dump_fetched_article_id"));
			String strExpinfoBlacklist = props.getProperty("expinfo_black_cat_list");
			if (StringUtils.isNotEmpty(strExpinfoBlacklist))
			{
				for (String str : strExpinfoBlacklist.split(","))
				{
					if (StringUtils.isNotEmpty(str))
					{
						FLOW_EXPINFO_BLACKLIST.add(str);
					}
				}
			}

			EXPINFO_BYPASS_ACS = Boolean.valueOf(props.getProperty("expinfo_bypass_acs"));
			String strEffectiveFlowExploreAppLang = props.getProperty("expinfo_effective_product_language");
			if (StringUtils.isNotEmpty(strEffectiveFlowExploreAppLang)) {
				for (String str : strEffectiveFlowExploreAppLang.split(",")) {
					EFFECTIVE_FLOW_EXPLORE_APP.add(str);
				}
			}
			flowLogger.info("expinfo_effective_product_language=" + EFFECTIVE_FLOW_EXPLORE_APP);

			DUMP_GMP_CSV_PATH = props.getProperty("gmp-csv-path");

			ADULT_SCORE_VERSION = props.getProperty("adult_score_version");
			ADULT_SCORE_VALUE   = props.getProperty("adult_score_value");
			String strAdultScoreKeys = props.getProperty("adultscores");
			for (String adultScoreKey : strAdultScoreKeys.split(","))
			{
				String[] keyValues = adultScoreKey.split("=");
				String adultKey = keyValues[0];
				Double adultScore = Double.valueOf(keyValues[1]);
				String mapKey = FeederConstants.getAdultScoreKey(adultKey);
				ADULT_SCORE_MAP.put(mapKey, adultScore);
			}

			//ratemapping
			String ratemappings = props.getProperty("ratemapping");
			String[] rateDetailsMappings = ratemappings.split(",");
			for(String rateDetailsMapping : rateDetailsMappings){
				 String[] detailMappings = rateDetailsMapping.split(":");
				 String rateKey = detailMappings[0];
				 String reateValue = detailMappings[1];
				 RATE_MAPPING.put(rateKey, Double.valueOf(reateValue));
			}
			//ratecoefficientmapping
			String rateCoefficientMappings = props.getProperty("ratecoefficientmapping");
			String[] rateCoefficientDetailsMappings = rateCoefficientMappings.split(",");
			for(String rateCoefficientDetailsMapping : rateCoefficientDetailsMappings){
				String[] radms = rateCoefficientDetailsMapping.split(":");
				String rateCoefficientKey = radms[0];
				String rateCoefficientValue = radms[1];
				RATE_MAPPING.put(rateCoefficientKey, Double.valueOf(rateCoefficientValue));
			}
			//listimagemapping
			String listimageMappings = props.getProperty("listimagemapping");
			String[] listimageDetailsMappings = listimageMappings.split(",");
			for(String listimageDetailsMapping : listimageDetailsMappings){
				String[] lidms = listimageDetailsMapping.split(":");
				String listimageKey = lidms[0];
				String listimageValue = lidms[1];
				RATE_MAPPING.put(listimageKey, Double.valueOf(listimageValue));
			}
			//detailimagemapping
			String detailimageMappings = props.getProperty("detailimagemapping");
			String[] detailimageDetailsMappings = detailimageMappings.split(",");
			for(String detailimageDetailsMapping : detailimageDetailsMappings){
				String[] didms = detailimageDetailsMapping.split(":");
				String detailimageKey = didms[0];
				String detailimageValue = didms[1];
				RATE_MAPPING.put(detailimageKey, Double.valueOf(detailimageValue));
			}
			//copyrightmapping
			String copyrightMappings = props.getProperty("copyrightmapping");
			String[] copyrightDetailsMappings = copyrightMappings.split(",");
			for(String copyrightDetailsMapping : copyrightDetailsMappings){
				String[] crdms = copyrightDetailsMapping.split(":");
				String copyrightKey = crdms[0];
				String copyrightValue = crdms[1];
				RATE_MAPPING.put(copyrightKey, Double.valueOf(copyrightValue));
			}
		}
		catch (Exception e)
		{
			flowLogger.error("\t\t  start init read " + PROPERTIES_FILE_CONFIG + " error ...", e);
		}
	}
  	private static final String STR_INTERFACE_WITH_SEMANTIC = ":list:scenario:";
	private static Set<String> gSetMemesCategories = new HashSet<String>();
	private static Set<String> gSetBeautiesCategories = new HashSet<String>();
	private static void loadScenarioCategoriesMappingFromRedis() {
		HashSet<String> hsMemesCategories = new HashSet<String>();
		HashSet<String> hsBeautiesCategories = new HashSet<String>();
		try {
			Map<String, String> mapScenarioCategories = jedisCache.hgetAll("scenario_categoryid_map");
			/**
			 * key format: ${product}:${language}:${interface}:${semantic}:${id}
			 *     sample: noticias:Spanish:list:scenario:65816
			 */
			for (Map.Entry<String, String> entry : mapScenarioCategories.entrySet()) {
				String product_interface_semantic = entry.getKey();
				if (product_interface_semantic.indexOf(STR_INTERFACE_WITH_SEMANTIC) >= 0) {
					com.alibaba.fastjson.JSONObject objCategory = com.alibaba.fastjson.JSONObject.parseObject(entry.getValue());
					Integer[] arrCategoryIds = (Integer[])objCategory.getJSONArray("categoryId").toArray(new Integer[0]);

					String[] s = product_interface_semantic.split(STR_INTERFACE_WITH_SEMANTIC);
					long scenarioId = NumberUtils.toLong(s[s.length-1], 0);
					if (ContextUtils.isMemesChannel(scenarioId)) {
						flowLogger.info("loadScenarioCategoriesMappingFromRedis memes: " + objCategory);
						for (Integer categoryId : arrCategoryIds) {
							hsMemesCategories.add(String.valueOf(categoryId));
						}
					} else if (ContextUtils.isBeautyChannel(scenarioId)) {
						flowLogger.info("loadScenarioCategoriesMappingFromRedis beauties: " + objCategory);
						for (Integer categoryId : arrCategoryIds) {
							hsBeautiesCategories.add(String.valueOf(categoryId));
						}
					}
				}
			}
			gSetMemesCategories = hsMemesCategories;
			gSetBeautiesCategories = hsBeautiesCategories;
			flowLogger.info("CategoriesMappingFromRedis memes: " + gSetMemesCategories);
			flowLogger.info("CategoriesMappingFromRedis beauties: " + gSetBeautiesCategories);
		} catch (Exception e) {
			//ignore
		}
	}
	public static boolean isMemesInfo(int contentType) {
		return (ContentType.MEME.getValue() == contentType);
	}
	public static boolean isMemesInfo(FeederInfo feederInfo) {
		int contentType = NumberUtils.toInt(feederInfo.getContent_type());
		List<String> alCategories = getCategoriesListByVersion(feederInfo.getCategories(), CATEGORY_VERSION);
		if (CollectionUtils.isNotEmpty(alCategories))
		{
			return ((ContentType.MEME.getValue() == contentType) && CollectionUtils.containsAny(gSetMemesCategories, alCategories));
		}
		return false;
	}
	public static boolean isBeautyInfo(FeederInfo feederInfo) {
		int contentType = NumberUtils.toInt(feederInfo.getContent_type());
		List<String> alCategories = getCategoriesListByVersion(feederInfo.getCategories(), CATEGORY_VERSION);
		if (CollectionUtils.isNotEmpty(alCategories))
		{
			return ((ContentType.BEAUTY.getValue() == contentType) && CollectionUtils.containsAny(gSetBeautiesCategories, alCategories));
		}
		return false;
	}
	public static boolean isLockscreenInfo(FeederInfo feederInfo) {
		return (feederInfo.getContentQuality() > 6 && feederInfo.getContentQuality() < 100);
	}
	public static boolean isVideoInfo(int contentType) {
		return (ContentType.VIDEO.getValue() == contentType);
	}
	public static boolean isVideoMp4Info(int linkType){
		return (FeederConstants.LINK_TYPE == linkType);
	}
	public static boolean isVideoInfo(FeederInfo feederInfo) {
		int contentType = NumberUtils.toInt(feederInfo.getContent_type());
		return isVideoInfo(contentType) ;
	}
	public static boolean isComicInfo(int contentType) {
		return (ContentType.COMIC.getValue() == contentType);
	}
	public static boolean isComicInfo(FeederInfo feederInfo) {
		int contentType = NumberUtils.toInt(feederInfo.getContent_type());
		return isComicInfo(contentType);
	}
	public static boolean isGif(int contentType) {
		return (ContentType.GIF.getValue() == contentType);
	}
	public static boolean isGif(FeederInfo feederInfo) {
		int contentType = NumberUtils.toInt(feederInfo.getContent_type());
		return isGif(contentType);
	}
	private static String getExploreRedisMember(FeederInfo feederInfo) {
		String strResult = null;
		try {
			Object[] memberElements = new Object[] {
				feederInfo.getContent_id()
				, feederInfo.getContent_type()
				, String.valueOf(feederInfo.getLink_type())
				, String.valueOf(feederInfo.getDisplay_type())
				, String.valueOf(DateUtils.stringToTimestamp(feederInfo.getPublish_time()))
			};
			strResult = StringUtils.join(memberElements, FeederConstants.SEPARATOR_REDIS_MEMBER);
		} catch (Exception e) {
		}
		return strResult;
	}

	
	private static void removeBatchArticlesFromWaitingPool(String exploreInfoWaitingKey,String[] infoWithTypeList) {
	
		expinfo_remove_logger.info("\t\t need to remove contentId size=" + infoWithTypeList.length + "\texploreWaitingKey=" + exploreInfoWaitingKey );
		jedisCache.zrem(exploreInfoWaitingKey, infoWithTypeList);
		logProcessedArticle("remove-vaild-article-from-"+exploreInfoWaitingKey,  StringUtils.join(infoWithTypeList,","));
	}
	
	private static void removeBatchArticlesFromExpPool(String exploreInfoKey,String[] infoWithTypeList) {
		
		long result = jedisCache.zrem(exploreInfoKey, infoWithTypeList);
		expinfo_remove_logger.info("\t\t need to remove contentId size=" + infoWithTypeList.length + "\texploreInfoKey=" + exploreInfoKey+"\tresult="+result);
		logProcessedArticle("remove-vaild-article-from-"+exploreInfoKey,  StringUtils.join(infoWithTypeList,","));
	}
	private static void removeBatchArticlesFromImpressionInfoPool(String  expinfoImpressionInfokey,String[] contentIds) {		
		long result = jedisCache.hdel(expinfoImpressionInfokey, contentIds);
		expinfo_remove_logger.info("\t\t need to remove contentId length=" + contentIds.length  + "\timpressionOfInfoIdKey=" + expinfoImpressionInfokey+"\tresult="+result);
		logProcessedArticle("remove-vaild-article-from-"+expinfoImpressionInfokey, StringUtils.join(contentIds,","));
	}
	
	
	
	
	private static void removeArticleIDFromExpinfoOutOfDate(String firm_app, String main_language, String infoid_with_type, boolean bDoForLockscreen) {
		String[] s = infoid_with_type.split("#");
		String contentId = s[0];
		String contentType = s[1];
		boolean bVideoInfo = isVideoInfo(NumberUtils.toInt(contentType));
		boolean bMemesInfo = isMemesInfo(NumberUtils.toInt(contentType));
		boolean bComicInfo = isComicInfo(NumberUtils.toInt(contentType));
		boolean bGifInfo = isGif(NumberUtils.toInt(contentType));
		Map<String, String> mExpInfoKey = FeederConstants.getExpInfoKey(firm_app, main_language, bVideoInfo, bMemesInfo, bComicInfo, bDoForLockscreen, bGifInfo);
		String exploreKey = mExpInfoKey.get("origin");
		String impressionOfInfoIdKey = mExpInfoKey.get("impression");
		String allImpressionOfInfoIdKey = mExpInfoKey.get("-1impression");
		expinfoLogger.info("\t\t need to remove contentId=" + infoid_with_type + "\texploreKey=" + exploreKey + "\timpressionOfInfoIdKey=" + impressionOfInfoIdKey+ "\tallImpressionOfInfoIdKey=" + allImpressionOfInfoIdKey);
		jedisCache.hdel(impressionOfInfoIdKey, contentId);
		jedisCache.zrem(exploreKey, infoid_with_type);
		jedisCache.hdel(allImpressionOfInfoIdKey, contentId);		
		String str_product_article = FeederConstants.getRedisKey(firm_app, main_language, contentType, contentId);
		logProcessedArticle(FeederConstants.LOG_EVENT_OUT_OF_DATE_EXPINFO, str_product_article);
	}
	private static void removeArticleIdFromExpinfo(String firm_app, String main_language, String articleId, FeederInfo feederInfo, boolean bDoForLockscreen) {
		Map<String, String> mExpInfoKey = FeederConstants.getExpInfoKey(firm_app, main_language, isVideoInfo(feederInfo), isMemesInfo(feederInfo), isComicInfo(feederInfo), bDoForLockscreen, isGif(feederInfo));
		String exploreKey = mExpInfoKey.get("origin");
		String waitingKey = mExpInfoKey.get("waiting");
		String exploringKey = mExpInfoKey.get("exploring");
		String impressionOfInfoIdKey = mExpInfoKey.get("impression");
		String allImpressionOfInfoIdKey = mExpInfoKey.get("-1impression");
		if (jedisCache.hexists(impressionOfInfoIdKey, articleId)) {
			String member = getExploreRedisMember(feederInfo);
			if (StringUtils.isNotEmpty(member)) {
				jedisCache.hdel(impressionOfInfoIdKey, articleId);
				jedisCache.zrem(exploreKey, member);
				jedisCache.zrem(waitingKey, member);
				jedisCache.zrem(exploringKey, member);
				jedisCache.hdel(allImpressionOfInfoIdKey, articleId);	
				String str_product_article = FeederConstants.getRedisKey(firm_app, main_language, feederInfo.getContent_type(), feederInfo.getContent_id());
				logProcessedArticle(FeederConstants.LOG_EVENT_OFFSHELF_ARTICLE, str_product_article);
			}
		}
	}
	private static void removeArticleIDFromExpinfo(String articleId) {
		String articleRedisKey = FeederConstants.getArticleKey(articleId);
		byte[] byteKey = jedisDetail.hget(articleRedisKey.getBytes(), FeederConstants.REDIS_FIELD_NAME_ARTICLE_FEATURE.getBytes());
		if (byteKey == null)
			return;

		TDeserializer deserializer = new TDeserializer();
		FeederInfo feederInfo = new FeederInfo();
		try {
			deserializer.deserialize(feederInfo, byteKey);
		} catch (TException e) {
			flowLogger.error("[removeArticleIDFromExpinfo]", e);
		}

		try
		{
			HashSet<String> hsFirmApp = listFirmApp(feederInfo);
			for (String firm_app : hsFirmApp)
			{
				if (StringUtils.isNotEmpty(firm_app))
				{
					String main_language = "";
					if (bOversea)
					{
						main_language = getMainLanguageByVersion(feederInfo, LANGUAGE_VERSION, firm_app);
					}
					else
					{
						main_language = getMainLanguageByVersion(feederInfo.getLanguage(), LANGUAGE_VERSION);
					}
					removeArticleIdFromExpinfo(firm_app, main_language, articleId, feederInfo, false);
					if (isLockscreenInfo(feederInfo)) {
						removeArticleIdFromExpinfo(firm_app, main_language, articleId, feederInfo, true);
					}
				}
			}
		}
		catch (Exception e)
		{
			flowLogger.error("[removeArticleIDFromExpinfo]", e);
		}
	}
	public static HashSet<String> listFirmApp(FeederInfo info)
	{
		HashSet<String> hsFirmApp = new HashSet<String>();
		try
		{
			Gson gson = new GsonBuilder().create();
			List alData = gson.fromJson(info.getFirm_app(), java.util.ArrayList.class);
			if (CollectionUtils.isNotEmpty(alData))
			{
				for (int i = 0; i < alData.size(); i++)
				{
					Map mData = (Map)alData.get(i);
					hsFirmApp.add((String)mData.get("app") );
				}
			}
		}
		catch (Exception e)
		{
			flowLogger.error("[listFirmApp]", e);
		}
		return hsFirmApp;
	}
	public static Set<String> enumerateContentIdList(List<FeederInfo> info_list)
	{
		Set<String> hsContentId = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(info_list))
		{
			for (FeederInfo info : info_list)
			{
				hsContentId.add(info.getContent_id());
			}
		}
		return hsContentId;
	}
	public static boolean isEnableContentGroup()
	{
		return bEnableContentGroup;
	}
	public static void doUpdateGroupDetail(Set<String> hsModifiedGroup, Set<String> finePoolIds)
	{
		List<Map<String, Object>> alGroup = clientJDBCTemplate.getGroupInfo(hsModifiedGroup);
		if (CollectionUtils.isNotEmpty(alGroup))
		{
			HashMap<String, String> hmItemGroup = new HashMap<String, String>();
			for (Map<String, Object> mGroup : alGroup)
			{
				String groupId = (String)mGroup.get("group_id");
				String activeItemId = (String)mGroup.get("active_item_id");
				int status = (Integer)mGroup.get("status");
				String groupDetailRedisKey = FeederConstants.getGroupDetailKey(groupId);
				if (status == STATUS_GROUP_INACTIVE || StringUtils.isEmpty(activeItemId))
				{
					flowLogger.info("[updateGroupActiveItem] remove from redis for offshelf : " + groupDetailRedisKey);
					jedisCache.del(groupDetailRedisKey);
				}
				else
				{
					flowLogger.info("[updateGroupActiveItem] update to redis for detail : " + groupDetailRedisKey + "=" + activeItemId);
					jedisCache.set(groupDetailRedisKey, activeItemId);
					jedisCache.expire(groupDetailRedisKey, DEFAULT_REDIS_EXPIRE); //expire in 30 days.
					if (!groupId.equalsIgnoreCase(activeItemId))
					{
						hmItemGroup.put(activeItemId, groupId);
					}
				}
			}
			//update prediction from activeItem
			if (bEnableFetchActiveItem)
			{
				Set<String> hsActiveItemId = hmItemGroup.keySet();
				if (CollectionUtils.isNotEmpty(hsActiveItemId))
				{
					Map<String, Object> mData = clientJDBCTemplate.listInfos((String[])hsActiveItemId.toArray(new String[0]), bOversea, false);
					List<Map<String, Object>> alData = (List<Map<String, Object>>)mData.get("onshelf");
					Map<String, EditorTableEntry> mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");
					long beginTime = System.nanoTime();
					flowLogger.info("\tBegin update Prediction into Redis...");
					List<FeederInfo> alInfo = (new InfoMapperPrediction()).mapIntoFeederInfoList(alData, mEditorEntry);
					for (FeederInfo feederInfo : alInfo)
					{
						String contentId = feederInfo.getContent_id();
						String groupId = (String)hmItemGroup.get(contentId);
						if (StringUtils.isEmpty(groupId))
						{
							groupId = contentId;
						}
						flowLogger.info("\tupdate prediction group_id=" + groupId + " with active item content_id=" + contentId + " adult_score=" + feederInfo.getAdult_score());
						feederInfo.setContent_id(groupId);
					}
					doStoreIntoRedisHash(alInfo, FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_FEATURE, null, finePoolIds, true);
					flowLogger.info("\tEnd update Prediction into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
					flowLogger.info("");

					for (String groupId : hmItemGroup.values())
					{
						notifyPrimaryUpdate(groupId);
					}
				}
			}
		}
	}
	public static HashSet<String> doFilterByGroup(List<FeederInfo> info_list)
	{
		HashSet<String> hsBlackList = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(info_list))
		{
			for (FeederInfo info : info_list)
			{
				if (!info.getContent_id().equals(info.getGroup_id()))
				{
					hsBlackList.add(info.getContent_id());
				}
			}
		}
		return hsBlackList;
	}
	/**
	 * @return map of articleInfoIdsKeys and corresponding contentId list.
	 */
	public static Map<String, List<String>> enumerateArticleInfoIdsKeyMap(String prefix, String[] arrContentId) {
		Map<String, List<String>> mResult = new HashMap<String, List<String>>();
		for (String contentId : arrContentId) {
			String strAllArticleInfoIdsKey = FeederConstants.getArticleInfoIdsKey(prefix, contentId);
			List<String> alContentId = mResult.get(strAllArticleInfoIdsKey);
			if (alContentId == null) {
				alContentId = new ArrayList<String>();
				mResult.put(strAllArticleInfoIdsKey, alContentId);
			}
			alContentId.add(contentId);
		}
		return mResult;
	}
	public static void removeNewsIDs(final Set<String> removableIdsFromRedis, final Set<String> removableIdsFromES, String scenario)
	{
		if (CollectionUtils.isEmpty(removableIdsFromRedis) && CollectionUtils.isEmpty(removableIdsFromES))
			return;

		//正常下架和延迟下架
		doRemoveFromRedisHash(removableIdsFromRedis);
		//将下架资讯从探索池删除
		doRemoveFromExplorePool(removableIdsFromRedis);

		doRemoveFromES(removableIdsFromES);

		Map<String, List<String>> mapArticleInfoIdsKeyToContentIdList = enumerateArticleInfoIdsKeyMap(FeederConstants.REDIS_KEY_ALL_ARTICLE_ID, removableIdsFromRedis.toArray(new String[0]));
		for (Map.Entry<String, List<String>> entry : mapArticleInfoIdsKeyToContentIdList.entrySet()) {
			String strArticleInfoIdsKey = entry.getKey();
			String[] arrRemovedContentId = (String[])(entry.getValue().toArray(new String[0]));
			jedisDetail.zrem(strArticleInfoIdsKey, arrRemovedContentId);
		}

		mapArticleInfoIdsKeyToContentIdList = enumerateArticleInfoIdsKeyMap(FeederConstants.REDIS_KEY_FALLBACK_ARTICLE_ID, removableIdsFromES.toArray(new String[0]));
		for (Map.Entry<String, List<String>> entry : mapArticleInfoIdsKeyToContentIdList.entrySet()) {
			String strArticleInfoIdsKey = entry.getKey();
			String[] arrRemovedContentId = (String[])(entry.getValue().toArray(new String[0]));
			jedisDetail.zrem(strArticleInfoIdsKey, arrRemovedContentId);
		}

		flowLogger.info("[" + scenario + "] Total Deleted Count From Redis:" + removableIdsFromRedis.size() + "\tFrom ES:" + removableIdsFromES.size());
		flowLogger.info("[" + scenario + "] Delete ContentId From Redis: " + removableIdsFromRedis.toString());
		//flowLogger.info("[" + scenario + "] Delete ContentId From ES: " + removableIdsFromES.toString());
	}
	public static AbstractMap.SimpleEntry<String, Double> selectLanguageWithWeight(Map<String, Map<String, Double>> mapVersionWithLanguage) {
		String strLanguage = null;
		double dbLanguage = -1;
		if (MapUtils.isNotEmpty(mapVersionWithLanguage)) {
			Iterator<Entry<String, Map<String, Double>>> it = mapVersionWithLanguage.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<String, Map<String, Double>> mapLanguage = it.next();

				if (mapLanguage.getValue().get("weight") > dbLanguage)
				{
					dbLanguage = mapLanguage.getValue().get("weight");
					strLanguage = mapLanguage.getKey();
				}
			}
		}
		return new AbstractMap.SimpleEntry<String, Double>(strLanguage, dbLanguage);
	}
	public static int getAvailableLanguage(String strLanguageJSON) {
		return getAvailableLanguage(strLanguageJSON, LANGUAGE_VERSION);
	}
	private static int getAvailableLanguage(String strLanguageJSON, String languageVersions)
	{
		int availableLanguage = Language.UNKNOWN.getCode();
		String[] arrCheckVersions = languageVersions.split(":");
		String jsonString = "{\"jsondata\":" + strLanguageJSON +"}";
		JSONDataStruct3Double jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
		Map<String, Map<String, Map<String, Double>>> maps3_d = jsondata3_d.getJsondata();
		if (maps3_d != null)
		{
			AbstractMap.SimpleEntry<String, Double> entryLanguage1 = selectLanguageWithWeight(maps3_d.get(arrCheckVersions[0]));
			String strLanguage1 = entryLanguage1.getKey();
			double dbLanguage1 = entryLanguage1.getValue();

			if (arrCheckVersions.length > 1) {
				AbstractMap.SimpleEntry<String, Double> entryLanguage2 = selectLanguageWithWeight(maps3_d.get(arrCheckVersions[1]));
				String strLanguage2 = entryLanguage2.getKey();
				double dbLanguage2 = entryLanguage2.getValue();

				if (StringUtils.isNotEmpty(strLanguage1) && StringUtils.isNotEmpty(strLanguage2))
				{
					if (strLanguage1.equals(strLanguage2) && dbLanguage1 > 0.5 && dbLanguage2 > 0.5)
					{
						if (!FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage1) && !FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage2)) {
							availableLanguage |= Language.getCode(strLanguage1);
						}
						else {
							java.util.List<Language> alLanguage = new java.util.ArrayList<Language>(java.util.Arrays.asList(Language.values()));
							for (int j = 0; j < alLanguage.size(); j++) {
								Language lang = alLanguage.get(j);
								availableLanguage |= lang.getCode();
							}
						}
					}
				}
			} else if (StringUtils.isNotEmpty(strLanguage1)) {
				availableLanguage |= Language.getCode(strLanguage1);
			}
		}
		return availableLanguage;
	}
	public static String getMainLanguageByVersion(String strLanguageJSON, String version)
	{
		String main_language = Language.UNKNOWN.getName();
		String jsonString = "{\"jsondata\":" + strLanguageJSON +"}";
		JSONDataStruct3Double jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
		Map<String, Map<String, Map<String, Double>>> maps3_d = jsondata3_d.getJsondata();
		if (maps3_d != null)
		{
			Map<String, Map<String, Double>> mapVersionWithLanguages = maps3_d.get(version);
			if (mapVersionWithLanguages != null)
			{
				String strLanguage = "";
				double dbLanguage = -1;

				Iterator<Entry<String, Map<String, Double>>> iter = mapVersionWithLanguages.entrySet().iterator();
				while (iter.hasNext())
				{
					Map.Entry<String, Map<String, Double>> mapLanguage = iter.next();

					if (mapLanguage.getValue().get("weight") > dbLanguage)
					{
						dbLanguage = mapLanguage.getValue().get("weight");
						strLanguage = mapLanguage.getKey();
					}
				}

				if (dbLanguage >= 0.5 && !FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage))
					main_language = strLanguage;
			}
		}
		return main_language;
	}
	public static String getMainLanguageByVersion(FeederInfo feederInfo, String languageVersions, String firmApp)
	{
		String main_language = Language.UNKNOWN.getName();
		String[] versions = languageVersions.split(":");
		String jsonString = "{\"jsondata\":" + feederInfo.getLanguage() +"}";
		JSONDataStruct3Double jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
		Map<String, Map<String, Map<String, Double>>> maps3_d = jsondata3_d.getJsondata();
		if (maps3_d != null)
		{
			Map<String, Map<String, Double>> mapVersionWithLanguage1 = maps3_d.get(versions[0]);
			Map<String, Map<String, Double>> mapVersionWithLanguage2 = maps3_d.get(versions[1]);
			if (mapVersionWithLanguage1 != null && mapVersionWithLanguage2 != null)
			{
				String strLanguage1 = "";
				String strLanguage2 = "";
				double dbLanguage1 = -1;
				double dbLanguage2 = -1;

				Iterator<Entry<String, Map<String, Double>>> iter1 = mapVersionWithLanguage1.entrySet().iterator();
				while (iter1.hasNext())
				{
					Map.Entry<String, Map<String, Double>> mapLanguage1 = iter1.next();

					if (mapLanguage1.getValue().get("weight") > dbLanguage1)
					{
						dbLanguage1 = mapLanguage1.getValue().get("weight");
						strLanguage1 = mapLanguage1.getKey();
					}
				}

				Iterator<Entry<String, Map<String, Double>>> iter2 = mapVersionWithLanguage2.entrySet().iterator();
				while (iter2.hasNext())
				{
					Map.Entry<String, Map<String, Double>> mapLanguage2 = iter2.next();

					if (mapLanguage2.getValue().get("weight") > dbLanguage2)
					{
						dbLanguage2 = mapLanguage2.getValue().get("weight");
						strLanguage2 = mapLanguage2.getKey();
					}
				}

				//Language.V2！=Language.V3 的文章不允许进入探索池
				if (strLanguage2 != null && strLanguage1 != null && !strLanguage2.equals(strLanguage1))
				{
					flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + feederInfo.getContent_type() + " frimApp=" + firmApp + " cannot access expinfo caused by language version exist different...");
				}
				else if (dbLanguage1 >= 0.5 && dbLanguage2 >= 0.5) //权重选择
				{
					if (!FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage1) && !FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage2))
						main_language = strLanguage1;
				}
			}
		}
		return main_language;
	}
	public static List<String> getCategoriesListByVersion(String strCategoriesJSON)
	{
		return getCategoriesListByVersion(strCategoriesJSON, CATEGORY_VERSION);
	}
	private static List<String> getCategoriesListByVersion(String strCategoriesJSON, String version)
	{
		List<String> listCategories = new ArrayList<String>();

		String jsonString = null;
		JSONDataStruct3Double jsondata3_d = null;
		Map<String, Map<String, Map<String, Double>>> maps3_d = null;
		try
		{
			jsonString = "{\"jsondata\":" + strCategoriesJSON +"}";
			jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
			maps3_d = jsondata3_d.getJsondata();
			if (maps3_d != null)
			{
				int applyIndex = 0;
				String[] candidateVersion = version.split(",");
				for (int i = 0; i < candidateVersion.length; i++)
				{
					if (null != maps3_d.get(candidateVersion[i]))
					{
						applyIndex = i;
						break;
					}
				}
				String applyVersion = candidateVersion[applyIndex];
				Map<String, Map<String, Double>> mapCatIDsWithWeight = maps3_d.get(applyVersion);
				double maxWeight = -Double.MAX_VALUE;
				String selectedCategoryId = null;
				for (Entry<String, Map<String, Double>> entry : mapCatIDsWithWeight.entrySet())
				{
					String categoryId = entry.getKey();
					double weight = entry.getValue().get("weight");
					if (weight > maxWeight)
					{
						listCategories.clear();
						listCategories.add(categoryId);
						maxWeight = weight;
					}
					else if (weight == maxWeight)
					{
						listCategories.add(categoryId);
					}
				}
			}
		}
		catch (Exception e2)
		{
		}
		return listCategories;
	}

	private static void notifyPrimaryUpdate(String article_id)
	{
		String articleid_updated_key = FeederConstants.getPrimaryUpdateKey(article_id);
		jedisDetail.set(articleid_updated_key, "1");
		jedisDetail.expire(articleid_updated_key, 1800); //expire after 30 mins..
		flowLogger.info("\t\t  The article "+article_id+" has set "+articleid_updated_key);
	}
	private static void notifyCleanCache(List<FeederInfo> info_list, Map<String, EditorTableEntry> mEditorEntry)
	{
		for (FeederInfo feederInfo : info_list)
		{
			String contentId = feederInfo.getContent_id();
			if (mEditorEntry == null || mEditorEntry.get(contentId) == null)
				continue;
			//notify to honeybee to clean local cache.
			String groupId = feederInfo.getGroup_id();
			if (ENABLE_NOTIFY_HONEYBEE && StringUtils.isNotEmpty(groupId))
			{
				HashMap mInfo = new HashMap();
				mInfo.put("content_id", groupId);
				mInfo.put("timestamp", System.currentTimeMillis());
				ClientKafkaProducer.getInstance().send(TaskQueueProducer.QUEUE_NOTIFY_HONEYBEE, FastJsonConverter.writeValue(mInfo));
			}
		}
	}
	private static void doStoreIntoRedisHash(List<FeederInfo> info_list, String prefix_string, String field_name, HashSet<String> hsBlackList, Set<String> finePoolIds)
	{
		doStoreIntoRedisHash(info_list, prefix_string, field_name, hsBlackList, finePoolIds, false);
	}
	private static void doStoreIntoRedisHash(List<FeederInfo> info_list, String prefix_string, String field_name, HashSet<String> hsBlackList, Set<String> finePoolIds, boolean fFetchActiveItem)
	{
		if (info_list.size() > 0)
		{
			for (FeederInfo feederInfo : info_list)
			{
				//System.out.println(info.getContent_id());
				if (CollectionUtils.isNotEmpty(hsBlackList) && hsBlackList.contains(feederInfo.getContent_id()))
				{
					flowLogger.info("\t\t contentId="+feederInfo.getContent_id()+" cannot access " + field_name + " caused by same group id.");
					continue;
				}

				if (fFetchActiveItem)
				{
					boolean fHighImpressionChecked = clientJDBCTemplate.isHighImpressionCheckInfo(feederInfo.getGroup_id());
					if (fHighImpressionChecked)
						feederInfo.setHighGmpCheck(1);
				}
				//精品池数据处理
				if(CollectionUtils.isNotEmpty(finePoolIds)){
                    if(finePoolIds.contains(feederInfo.getContent_id())){
                        feederInfo.setContentQuality(100);
                        flowLogger.info("\t\t ContentQuality=100  contentId="+feederInfo.getContent_id());
                    }
                }
				byte[] empDtl = null;
				TSerializer serializer = new TSerializer();
				try
				{
					empDtl = serializer.serialize(feederInfo);
				}
				catch (TException e)
				{
					flowLogger.error("[doStoreIntoRedisHash]", e);
				}
				String key = FeederConstants.getRedisKey(prefix_string, feederInfo.getContent_id());
				jedisDetail.hset(key.getBytes(), field_name.getBytes(), empDtl);
				jedisDetail.expire(key.getBytes(), DEFAULT_REDIS_EXPIRE);
				byte[] infoByte = jedisDetail.hget(key.getBytes(),field_name.getBytes());
				TDeserializer deserializer = new TDeserializer();
				try {
					FeederInfo feederInfoApi = new FeederInfo();
					deserializer.deserialize(feederInfoApi, infoByte);
					flowLogger.info("\t\t[InfoMapperAPI] get contentId = "+feederInfo.getContent_id()+" setFeederInfo= "+feederInfo.getAvailableLanguage()+"setFeederInfo="+JSON.toJSONString(feederInfoApi.getAvailableLanguage()));
				}catch (TException e){
					flowLogger.error("[doStoreIntoRedisHash]", e);
				}
				flowLogger.info("\t\t[doStoreIntoRedisHash] contentId="+feederInfo.getContent_id()+" access " + field_name + ".");


            }
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for " + field_name + "...");
		}
	}
	private static void doStoreIntoRedisHashChapter(List<FeederInfo> info_list, String prefix_string, String field_name)
	{
		if (info_list.size() > 0)
		{
			for (FeederInfo feederInfo : info_list)
			{
				TSerializer serializer = new TSerializer();
				List<ChapterInfo> alChapter = feederInfo.getChapterInfoList();
				for (ChapterInfo chapterInfo : alChapter) {
					String chapterListKey = FeederConstants.getRedisKey(FeederConstants.REDIS_KEY_PREFIX_CHAPTERLIST, feederInfo.getContent_id());
					String chapterDetailKey = FeederConstants.getRedisKey(FeederConstants.REDIS_KEY_PREFIX_CHAPTERDETAIL, feederInfo.getContent_id());
					String chapterField = String.valueOf(chapterInfo.getChapter());
					byte[] chapterValue = null;
					try {
						chapterValue = serializer.serialize(chapterInfo);
					} catch (TException e) {
						flowLogger.error("[doStoreIntoRedisHashChapter]", e);
					}
					if (chapterValue != null) {
						jedisDetail.hset(chapterDetailKey.getBytes(), chapterField.getBytes(), chapterValue);
						flowLogger.info("\t\t[doStoreIntoRedisHashChapter] contentId=" + feederInfo.getContent_id() + " access key=" + chapterDetailKey + " field=" + chapterField + ".");
						jedisDetail.zadd(chapterListKey, (double)chapterInfo.getChapter(), chapterField);
						flowLogger.info("\t\t[doStoreIntoRedisHashChapter] contentId=" + feederInfo.getContent_id() + " access key=" + chapterListKey + " member=" + chapterField + " score=" + chapterInfo.getChapter() + ".");
						jedisDetail.expire(chapterDetailKey, DEFAULT_REDIS_EXPIRE);
						jedisDetail.expire(chapterListKey, DEFAULT_REDIS_EXPIRE);
					}
				}

				byte[] empDtl = null;
				try
				{
					feederInfo.setChapterInfoListIsSet(false);
					empDtl = serializer.serialize(feederInfo);
				}
				catch (TException e)
				{
					flowLogger.error("[doStoreIntoRedisHash]", e);
				}
				String key = FeederConstants.getRedisKey(prefix_string, feederInfo.getContent_id());
				jedisDetail.hset(key.getBytes(), field_name.getBytes(), empDtl);
				jedisDetail.expire(key.getBytes(), DEFAULT_REDIS_EXPIRE);
				flowLogger.info("\t\t[doStoreIntoRedisHashChapter] contentId=" + feederInfo.getContent_id() + " access " + field_name + ".");
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for " + field_name + "...");
		}
	}

	private static void doStoreIntoRedisHashBiz(List<InteractionInfo> info_list, String prefix_string, String field_name)
	{
		if (info_list.size() > 0)
		{
			for (InteractionInfo interactionInfo : info_list){
				String ballotCount = clientJDBCTemplateComment.getBallot(interactionInfo.getContentId());
				int x = 1+(int)(Math.random()*10);
				String key = FeederConstants.getRedisKey(prefix_string, interactionInfo.getContentId());
				Map<String,String> info = new HashMap<String,String>(1);
				info.put("commentCount",interactionInfo.getCommentCount());
				info.put("thumbupCount",interactionInfo.getThumbupCount());
				info.put("ballotCount",ballotCount);
				info.put("random",String.valueOf(x));
				jedisDetail.hset(key.getBytes(), field_name.getBytes(), JSON.toJSONString(info).getBytes());
				jedisDetail.expire(key.getBytes(), DEFAULT_REDIS_EXPIRE);
				flowLogger.info("\t\t[doStoreIntoRedisHashBiz] contentId=" + interactionInfo.getContentId() + " access " + field_name + ".");
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for " + field_name + "...");
		}
	}

	private static void doStoreIntoRedisHashHw(List<FeederInfo> info_list, String prefix_string)
	{
			flowLogger.info("\t\t[doStoreIntoRedisHashHw] start");
		if (info_list.size() > 0)
		{
			for (FeederInfo feederInfo : info_list){
				HashSet<String> firmApp = listFirmApp(feederInfo);
				flowLogger.info("\t\t[doStoreIntoRedisHashHw] firmAPP="+JSON.toJSON(firmApp));
				flowLogger.info("\t\t[doStoreIntoRedisHashHw] getHasCopyright="+feederInfo.getHasCopyright());
				if(firmApp.contains(HW_FROM_APP)&&feederInfo.getHasCopyright()>0){
					flowLogger.info("\t\t[doStoreIntoRedisHashHw] yes");
					String[] categories = feederInfo.getCategories().split(",");
					flowLogger.info("\t\t[doStoreIntoRedisHashHw] categories="+JSON.toJSON(categories));
					if(categories.length>0){
						String key = prefix_string+"_"+ categories[0]+feederInfo.getContent_id();
						if(feederInfo.getContent_type().equals("1")){
							//HW去重
							if(!jedisDetail.exists(key)){
								jedisDetail.rpush( prefix_string+"_"+ categories[0],feederInfo.getContent_id());
								jedisDetail.set(key,"1");
								jedisDetail.expire(key,30*24*60*60);
								flowLogger.info("\t\t[doStoreIntoRedisHashHw] contentId=" + feederInfo.getContent_id() + " access " + prefix_string +"_"+ categories[0]+ ".");
							}
						}
					}

				}
			}
		}
	}

	//详情redis删除-正常删除或延迟删除
	private static void doRemoveFromRedisHash(Set<String> removableIds) {
		if (IS_OPEN_DELAY_DELETE)
		{
			Map<String,Integer> returnMap = clientJDBCTemplate.getContentEditorLog(removableIds, DELAY_DELETE_EDIT_TYPE);
			for (String removableId : removableIds)
			{
				Integer result = returnMap.get(removableId);
				String articleRedisKey = FeederConstants.getArticleKey(removableId);
				if (result != null && result == DELAY_DELETE_EDIT_TYPE)
				{
					//延迟删除
					long ttl = jedisDetail.ttl(articleRedisKey);
					flowLogger.info("contentid="+removableId+" current ttl=" + ttl +" seconds...");
					if (ttl >= DELAY_DELETE_TIMEOUT)
					{
						jedisDetail.expire(articleRedisKey, DELAY_DELETE_TIMEOUT);
					}
					jedisDetail.hdel(articleRedisKey, FeederConstants.REDIS_FIELD_NAME_ARTICLE_FEATURE);//删除资讯池 Prediction Field
					flowLogger.info("contentid="+removableId+" delay shelves" + DELAY_DELETE_TIMEOUT+" seconds deleted...");
					continue;
				}
				//正常下架删除
				jedisDetail.del(articleRedisKey);
				flowLogger.info("contentid="+removableId+" normal shelves ...");
			}
		}
		else
		{
			//正常删除
			for (String removableId : removableIds)
			{
				String articleRedisKey = FeederConstants.getArticleKey(removableId);
				jedisDetail.del(articleRedisKey);
				flowLogger.info("contentid="+removableId+" normal shelves ...");
			}
		}
	}
	private static void doRemoveFromExplorePool(Set<String> removableIds) {
		long beginTime = System.nanoTime();
		flowLogger.info("\tBegin remove from explore pool...");
		for (String strArticelId : removableIds)
		{
			removeArticleIDFromExpinfo(strArticelId);
		}
		flowLogger.info("\tEnd remove explore pool. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
	}
	public static void doRemoveFromES(final Set<String> removableIds) {
		long beginTime = System.nanoTime();
		flowLogger.info("\tBegin remove from ES...");
		if (bWriteES && esClient != null)
		{
			BulkRequestBuilder bulkRequest = esClient.prepareBulk();
			bulkRequest.setRefreshPolicy("true");
			for (String strArticelId : removableIds)
			{
				bulkRequest.add(esClient.prepareDelete("tchannel", "tchannel", strArticelId));
			}
			try {
				BulkResponse bulkResponse = bulkRequest.execute().actionGet();
				for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
					if (StringUtils.isEmpty(itemResponse.getFailureMessage())) {
						flowLogger.info(" The id "+itemResponse.getId()+" has been removed.");
					} else {
						flowLogger.info(" The id "+itemResponse.getId()+" has not been removed. failed reason:" + itemResponse.getFailureMessage());
					}
				}
			} catch (Exception e) {
				flowLogger.error("[ElasticSearch] prepareDelete", e);
			}
		}
		flowLogger.info("\tEnd remove ES. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
	}
	private static void logProcessedArticle(String log_event, String str_product_article)
	{
		String msg = FeederConstants.getLogMessage(log_event, str_product_article, System.currentTimeMillis());
		expinfo_lifecycle_logger.info(msg);
	}
	private static void addInfoExpInfoRedis(String log_event, String expinfo_key, String impression_of_infoid_key,String all_impression_of_infoid_key, String infoid_with_type, double score)
	{
		String content_id = infoid_with_type.split(FeederConstants.SEPARATOR_REDIS_MEMBER)[0];
		if (FeederConstants.LOG_EVENT_REDUNDANT_INTO_EXPINFO.equalsIgnoreCase(log_event))
		{
			//Fixed to reject redundant adding into expinfo.
			Long rank = jedisCache.zrank(expinfo_key, infoid_with_type);
			if (rank == null)
			{
				expinfoLogger.info("\t\t reject redundant adding info expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + content_id + " abs(score)=" + score);
				return;
			}
		}
		String msg = FeederConstants.getLogMessage(log_event, expinfo_key, infoid_with_type, score, System.currentTimeMillis());
		long ret = jedisCache.zadd(expinfo_key, score, infoid_with_type);
		if (ret > 0)
		{
			String expinfo_acs_key = expinfo_key + FeederConstants.SEPARATOR_REDIS_MEMBER + content_id;
			ExpinfoFilter.insertACS(Arrays.asList(new String[]{expinfo_acs_key}));
			//for optimization of impression-based flow exploration, initialize impression of info id as -1.
			if (StringUtils.isNotEmpty(impression_of_infoid_key))
			{
				jedisCache.hset(impression_of_infoid_key, content_id, FeederConstants.INIT_VALUE_OF_IMPRESSION);

				if(StringUtils.isNotEmpty(all_impression_of_infoid_key)){
					boolean exist = jedisCache.hexists(all_impression_of_infoid_key, content_id);
					if(!exist){
						jedisCache.hset(all_impression_of_infoid_key, content_id, FeederConstants.INIT_VALUE_OF_IMPRESSION);						
					}
				}


			}
			expinfo_lifecycle_logger.info(msg);
		}
	}
	private static HashMap<String, Object> doProcessExpInfo(FeederInfo feederInfo)
	{
		HashMap<String, Object> mResult = new HashMap<String, Object>();
		Set<String> hsAppLang = new HashSet<String>();
		Set<String> hsClientChannels = new HashSet<String>();
		mResult.put("app_lang", hsAppLang);
		mResult.put("channels", hsClientChannels);
		Date publishTime = DateUtils.stringToDate(feederInfo.getPublish_time(), new Date());
		try
		{
			HashSet<String> hsFirmApp = listFirmApp(feederInfo);
			for (String firm_app : hsFirmApp)
			{
				if (firm_app.length() > 0)
				{
					String main_language = Language.UNKNOWN.getName();
					if (LANGUAGE_VERSION.contains(":"))
					{
						main_language = getMainLanguageByVersion(feederInfo, LANGUAGE_VERSION, firm_app);
					}
					else
					{
						main_language = getMainLanguageByVersion(feederInfo.getLanguage(), LANGUAGE_VERSION);
					}
					List<String> listCategories = getCategoriesListByVersion(feederInfo.getCategories(), CATEGORY_VERSION);
					if (main_language.equals(Language.UNKNOWN.getName()) == false && FLOW_EXPINFO_BLACKLIST.containsAll(listCategories) == false)
					{
						Map<String, String> mExpInfoKey = (bOversea) ? FeederConstants.getExpInfoKey(firm_app, main_language, isVideoInfo(feederInfo), isMemesInfo(feederInfo), isComicInfo(feederInfo), false, isGif(feederInfo))
																	 : FeederConstants.getExpInfoKey(firm_app, main_language, false, false, false, false,false);
						String expinfo_key = mExpInfoKey.get("origin");
						String impression_of_infoid_key = mExpInfoKey.get("impression");
						String all_impression_of_infoid_key = mExpInfoKey.get("-1impression");
						String contentType = feederInfo.getContent_type();

						//测试key
						Object[] keyElements = new Object[] {
							feederInfo.getContent_id()
							, feederInfo.getContent_type()
							, String.valueOf(feederInfo.getLink_type())
							, String.valueOf(feederInfo.getDisplay_type())
							, String.valueOf(DateUtils.stringToTimestamp(feederInfo.getPublish_time()))
						};
						String infoid_with_type = StringUtils.join(keyElements, FeederConstants.SEPARATOR_REDIS_MEMBER);

						String strAppLang = firm_app + FeederConstants.SEPARATOR_APP_LANGUAGE + main_language;
						if (CollectionUtils.isNotEmpty(EFFECTIVE_FLOW_EXPLORE_APP) && !EFFECTIVE_FLOW_EXPLORE_APP.contains(strAppLang)) {
							flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " cannot access expinfo caused by app:language " + strAppLang + " is not in effective_product_language set.");
							continue;
						}

						//过acs系统
						if (!EXPINFO_BYPASS_ACS)
						{
							String acs_key_base = firm_app + main_language + feederInfo.getContent_id();
							String[] arr_acs_key = new String[] {acs_key_base};
							if (ExpinfoFilter.checkByAcs(Arrays.asList(arr_acs_key)))
							{
								flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " cannot access expinfo caused by has been exist acs system the key =" + StringUtils.join(arr_acs_key, ","));
								continue;
							                                                                       }
						}

						//检查adult_score
						String adult_score_json = feederInfo.getAdult_score();
						HashMap<String, String> adultScoreJsonMap = JSON.parseObject(adult_score_json, HashMap.class);
						String strAdultScore = (String)adultScoreJsonMap.get(ADULT_SCORE_VERSION);
						if (StringUtils.isNotEmpty(strAdultScore))
						{
							Double adultScore = Double.parseDouble(strAdultScore);
							String adScoreKey = FeederConstants.getAdultScoreKey(firm_app, main_language);
							Boolean result = checkAdultScore(adScoreKey, adultScore, feederInfo);
							if (result)
							{
								flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " adScoreKey=" + adScoreKey + " cannot access expinfo caused by adult score=" + adultScore + " too high...");
								continue;
							}
						}

						// Store into different redis by language except zh_CN.
						if (bOversea)
						{
							String expinfo_acs_key = expinfo_key + FeederConstants.SEPARATOR_REDIS_MEMBER + feederInfo.getContent_id();
							if (ExpinfoFilter.checkByAcs(Arrays.asList(new String[]{expinfo_acs_key})))
							{
								flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " cannot access expinfo caused by existed in acs system the key " + expinfo_acs_key);
								continue;
							}

							//流量探索优化 先针对lite版本
							flowLogger.info("\t\t[ctrInit] fromApp"+firm_app);
							List<String> firmApps = Arrays.asList(FLOW_EXPLORE_PRODUCT.split(","));
							if(firmApps.contains(firm_app)&&contentType.equals(String.valueOf(ContentType.NEWS.getValue()))){
								String groupId = clientJDBCTemplate.getGroupId(feederInfo.getContent_id());
								String jsonCategories =feederInfo.getCategories();
								if(!StringUtils.isEmpty(groupId)&&!StringUtils.isEmpty(jsonCategories)){
									List<String> alCategories = Feeder.getCategoriesListByVersion(jsonCategories);
									flowLogger.info("\t\t[ctrInit] categories="+JSON.toJSONString(alCategories));
									double ctr = clientJDBCTemplate.getExploreCtrByPublisherAndCategory(firm_app,feederInfo.getPublisher(),alCategories.get(0));
									if(ctr<=0){
										flowLogger.info("\t\t [ctrInit] contentId=" + groupId + " publisher=" + feederInfo.getPublisher() + " categorieId="+alCategories.get(0) + " ctr="+ctr);
									}else{
										long time = (new Date().getTime()/1000 - feederInfo.getPublish_time_ts())/60/60;
										double timeRate = 1.005;
										double imageRate = 1;
										if(time > 2 && time <=24){
											timeRate = 1;
										}else if(time > 24){
											timeRate = 0.995;
										}
										if(feederInfo.getListImagesCount()>2){
											imageRate = 1.005;
										}
										DecimalFormat df = new DecimalFormat("0.000000000");
										SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHH");
										fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
										String updateDate = fmt.format(new Date());
										double ctrNew = Double.valueOf(df.format(ctr*timeRate*imageRate));
										Map<String,Object> gmpMap = new HashMap<>(6);
										gmpMap.put("decay_impression",30);
										gmpMap.put("content_id",groupId);
										gmpMap.put("decay_click",Double.valueOf(df.format(30*ctrNew)));
										gmpMap.put("product_id",firm_app);
										gmpMap.put("total_impression",30);
										gmpMap.put("update_date",updateDate);
										String gmpJson = JSON.toJSONString(gmpMap);
										if(ctrNew > 1){
											flowLogger.info("\t\t[ctrInit] contentId=" + groupId + " gmpInfoError="+gmpJson);
											continue;
										}
										flowLogger.info("\t\t[ctrInit] contentId=" + groupId + " gmpInfo="+gmpJson);
										ClientKafkaProducer.getInstance().send(TaskQueueProducer.ARTICLE_GMP, gmpJson);
										//set acs
										ExpinfoFilter.insertACS(Arrays.asList(new String[]{expinfo_acs_key}));
										continue;
									}
								}
							}

							//视频分类124改成contentType=2判断视频是否进入
							if (isVideoInfo(feederInfo))
							{
								if (publishTime.after(new Date(System.currentTimeMillis()-EXPINFO_EFFECTIVE_TIME_CONSTRAINT_VIDEO)))
								{
									hsAppLang.add(firm_app+FeederConstants.SEPARATOR_APP_LANGUAGE+main_language+FeederConstants.SEPARATOR_REDIS_KEY+"video");
									Double score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
									expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " all_impression_of_infoid_key =" + all_impression_of_infoid_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
									addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key, all_impression_of_infoid_key, infoid_with_type, score);
									hsClientChannels.add(expinfo_key);
								}
								else
								{
									//打印video不在48小时之内的日志
									flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " cannot access expinfo caused by publishtime before 48 hours.");
								}
							}
							//memes only applied in oversea
							else if (isMemesInfo(feederInfo)) {
								//1. 针对不符合条件的displaytype不进探索池,影响core探索进行池
								//2. displaytype没有合适场景消耗而堆积到进行池头部
								//3. core重试几次后进行池子得不到有效补充
								boolean is_need_explore = (feederInfo.getDisplay_type() & 0x400) !=0 ;
								if (publishTime.after(new Date(System.currentTimeMillis()-EXPINFO_EFFECTIVE_TIME_CONSTRAINT_MEME))  && is_need_explore )
								{
									hsAppLang.add(firm_app+FeederConstants.SEPARATOR_APP_LANGUAGE+main_language+FeederConstants.SEPARATOR_REDIS_KEY+"meme");
									Double score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
									expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " all_impression_of_infoid_key =" + all_impression_of_infoid_key+ " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
									addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key,all_impression_of_infoid_key, infoid_with_type, score);
									hsClientChannels.add(expinfo_key);
								}
								else
								{
									//打印meme不在48小时之内的日志
									flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " contenty_type=" + feederInfo.getDisplay_type() +" cannot access expinfo caused by publishtime before 48 hours or displaytype is invalid");
								}
							}
							else if (isComicInfo(feederInfo)) {
								if (publishTime.after(new Date(System.currentTimeMillis()-EXPINFO_EFFECTIVE_TIME_CONSTRAINT_MEME)))
								{
									hsAppLang.add(firm_app+FeederConstants.SEPARATOR_APP_LANGUAGE+main_language+FeederConstants.SEPARATOR_REDIS_KEY+"comic");
									Double score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
									expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
									addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key, all_impression_of_infoid_key,infoid_with_type, score);
									hsClientChannels.add(expinfo_key);
								}
								else
								{
									//打印comic不在168小时之内的日志
									 flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key +  " cannot access expinfo caused by publishtime before 48 hours");
								}
							}
							else if(isGif(feederInfo)){
								//先过滤displaytypezh只有MP4的gif资讯不进探索
								//boolean is_need_explore = (feederInfo.getDisplay_type() & 0x800) !=0 ;
								if(publishTime.after(new Date(System.currentTimeMillis()-EXPINFO_EFFECTIVE_TIME_CONSTRAINT_GIF))){
									hsAppLang.add(firm_app+FeederConstants.SEPARATOR_APP_LANGUAGE+main_language+FeederConstants.SEPARATOR_REDIS_KEY+"gif");
									Double score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
									expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " all_impression_of_infoid_key =" + all_impression_of_infoid_key+ " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
									addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key,all_impression_of_infoid_key, infoid_with_type, score);
									hsClientChannels.add(expinfo_key);
								}
								else
								{
									//打印gif不在48小时之内的日志
									flowLogger.info("\t\t contentId=" + feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " contenty_type=" + feederInfo.getDisplay_type() +" cannot access expinfo caused by publishtime before 48 hours or displaytype is invalid");
								}
							}
							else
							{
								//news 流量探索排序
								Double score = 0.0;
								if (feederInfo.getBody_image_count() > 0)
								{
									if (publishTime.after(new Date(System.currentTimeMillis()-EXPINFO_EFFECTIVE_TIME_CONSTRAINT_NEWS)))
									{
										String channelLanguage = firm_app+FeederConstants.SEPARATOR_APP_LANGUAGE+main_language;
										hsAppLang.add(channelLanguage);
										if (FLOW_EXPINFO_MAIN_SWITCH && FLOW_EXPINFO_ABTEST_OPEN.equals(channelLanguage))
										{
											Double treeValue = ExpinfoCalModel.getRedisTreeValue(feederInfo, DUMP_GMP_CSV_PATH);
											if (treeValue == 0)
											{
												score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
												expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score + " treeValue==0");
											}
											else
											{
												score = treeValue;
												expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
											}
										}
										else
										{
											score = doCurrentCalcFlowExpinfo(feederInfo.getRate(), feederInfo.getListImagesCount(), feederInfo.getBodyImagesCount(), feederInfo.getHasCopyright());
											expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + score);
										}
										addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key,all_impression_of_infoid_key, infoid_with_type, score);
										if (isLockscreenInfo(feederInfo)) {
											mExpInfoKey = FeederConstants.getExpInfoKey(firm_app, main_language, isVideoInfo(feederInfo), isMemesInfo(feederInfo), isComicInfo(feederInfo), true, isGif(feederInfo));
											expinfo_key = mExpInfoKey.get("origin");
											impression_of_infoid_key = mExpInfoKey.get("impression");
											addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key, null, infoid_with_type, score);
										}
										hsClientChannels.add(expinfo_key);
									}
									else
									{
										flowLogger.info("\t\t contentId="+infoid_with_type+",key="+expinfo_key+",cannot access news_expinfo caused by publishtime before 24 hours ...");
									}
								}
							}
						}
						else
						{
							double score = Double.valueOf(publishTime.getTime());
							expinfoLogger.info("\t\t new flow expinfo key=" + expinfo_key + " infoid_with_type=" + infoid_with_type + " contentId=" + feederInfo.getContent_id() + " abs(score)=" + publishTime.getTime());
							addInfoExpInfoRedis(FeederConstants.LOG_EVENT_FIRST_INTO_EXPINFO, expinfo_key, impression_of_infoid_key,all_impression_of_infoid_key, infoid_with_type, score);
							hsClientChannels.add(expinfo_key);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			flowLogger.error("[doProcessExpInfo]", e);
		}
		return mResult;
	}
	@SuppressWarnings("unchecked")
	private static void doStoreIntoRedisHashInfo(List<Info> info_list, String prefix_string, String field_name, String strLastWorkingTS, Set<String> editorBlack, Set<String> hsBlackList, Set<String> finePoolIds)
	{
		if (info_list.size() > 0)
		{
			Set<String> hsAppLang = new HashSet<String>();
			Set<String> hsClientChannels = new HashSet<String>();

			doAllInfoIDs(info_list, hsBlackList, finePoolIds);

			doLastCalcFlowExpinfo();

			for (Info info : info_list)
			{
				/**
				 * Set special key for such updated article to notify following procedure.
				 */
				HashSet<String> hsFirmApp = listFirmApp(info.feederInfo);
				if (CollectionUtils.isNotEmpty(hsBlackList) && hsBlackList.contains(info.feederInfo.getContent_id()))
				{
					flowLogger.info("\t\t contentId=" + info.feederInfo.getContent_id() + " cannot access " + field_name + " caused by same group id.");
					String contentType = info.feederInfo.getContent_type();
					if (CollectionUtils.isNotEmpty(hsFirmApp))
					{
						for (String firm_app : hsFirmApp)
						{
							String main_language = Language.UNKNOWN.getName();
							if (LANGUAGE_VERSION.contains(":"))
							{
								main_language = getMainLanguageByVersion(info.feederInfo, LANGUAGE_VERSION, firm_app);
							}
							else
							{
								main_language = getMainLanguageByVersion(info.feederInfo.getLanguage(), LANGUAGE_VERSION);
							}
							Map<String, String> mExpInfoKey = (bOversea) ? FeederConstants.getExpInfoKey(firm_app, main_language, isVideoInfo(info.feederInfo), isMemesInfo(info.feederInfo), isComicInfo(info.feederInfo), false, isGif(info.feederInfo))
																		 : FeederConstants.getExpInfoKey(firm_app, main_language, false, false, false, false, false);
							String expinfo_key = mExpInfoKey.get("origin");
							flowLogger.info("\t\t contentId="+ info.feederInfo.getContent_id() + " contentType=" + contentType + " expinfo_key=" + expinfo_key + " cannot access expinfo caused by same group id.");
						}
					}
					continue;
				}

				String articleRedisKey = FeederConstants.getRedisKey(prefix_string, info.feederInfo.getContent_id());
				if (jedisDetail.hexists(articleRedisKey, field_name) == false)
				{
					if (DUMP_FETCHED_ARTICLE_ID)
						articleLogger.info(info.feederInfo.getContent_id());
				}
				else
				{
					if (info.feederInfo.getUpdate_time().contains(strLastWorkingTS) == true)
					{
						flowLogger.info(info.feederInfo.getContent_id()+", repeat to enter flow expinfo caused by updatetime exist last time");
						continue;
					}
					else
					{
						if (SET_UPDATE_TIMEOUT_KEY)
						{
							notifyPrimaryUpdate(info.feederInfo.getContent_id());
						}
					}
				}

				//write info detail
				byte[] empDtl = null;
				TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
				try
				{
					empDtl = serializer.serialize(info);
				}
				catch (TException e)
				{
					flowLogger.error("[doStoreIntoRedisHashInfo]", e);
				}
				jedisDetail.hset(articleRedisKey.getBytes(), field_name.getBytes(), empDtl);
				flowLogger.info("\t\t[doStoreIntoRedisHashInfo] contentId="+ info.feederInfo.getContent_id() +" access " + field_name + ".");

				if (editorBlack.contains(info.feederInfo.getContent_id()))
				{
					flowLogger.info("\t\t contentId=" + info.feederInfo.getContent_id() + " contentType=" + info.feederInfo.getContent_type() + " cannot access expinfo caused by adult score too high...");
					continue;
				}

				for (String firm_app : hsFirmApp)
				{
					String main_language = Language.UNKNOWN.getName();
					if (LANGUAGE_VERSION.contains(":"))
					{
						main_language = getMainLanguageByVersion(info.feederInfo, LANGUAGE_VERSION, firm_app);
					}
					else
					{
						main_language = getMainLanguageByVersion(info.feederInfo.getLanguage(), LANGUAGE_VERSION);
					}
					String str_product_article = FeederConstants.getRedisKey(firm_app, main_language, info.feederInfo.getContent_type(), info.feederInfo.getContent_id());
					logProcessedArticle(FeederConstants.LOG_EVENT_NEW_PUBLISH_ARTICLE, str_product_article);
					if (isLockscreenInfo(info.feederInfo)) {
						str_product_article = FeederConstants.getRedisKey(firm_app, main_language, "lockscreen", info.feederInfo.getContent_type(), info.feederInfo.getContent_id());
						logProcessedArticle(FeederConstants.LOG_EVENT_NEW_PUBLISH_ARTICLE, str_product_article);
					}
				}

				HashMap<String, Object> mData = doProcessExpInfo(info.feederInfo);
				if (null != mData.get("app_lang"))
				{
					hsAppLang.addAll((Set<String>)mData.get("app_lang"));
				}
				if (null != mData.get("channels"))
				{
					hsClientChannels.addAll((Set<String>)mData.get("channels"));
				}

				// Convert Info.tags from id to name
				List<String> listCategories = Arrays.asList(info.tags.substring(1, info.tags.length() - 1).split(", "));

				info.setTags("NONE"); //not a valid category name..
				if (listCategories != null && listCategories.size() > 0 && listCategories.get(0).length() > 0)
				{
					int catId = Integer.valueOf(listCategories.get(0));
					for (CategoryInfo cat_info : listCategoryInfo)
					{
						if (cat_info.getId() == catId)
						{
							info.setTags(cat_info.getCategoryName());
							break;
						}
					}
				}
			}

			if (bOversea)
			{
				//流量探索
				for (String appAndlang : hsAppLang)
				{
					String[] appLangs = appAndlang.split(FeederConstants.SEPARATOR_APP_LANGUAGE);
					String firm_app   = appLangs[0];
					String language   = appLangs[1];
					String expinfo_key = FeederConstants.getExpInfoKey(firm_app, language);
					//处理前50条数据-score设置成0
					doHandlerBeforeFlowFifty(expinfo_key);
				}
			}
			else
			{
				for (String strClientChannel : hsClientChannels)
				{
					long lAllowMaxNum = 1000;
					if (strClientChannel.substring(FeederConstants.REDIS_KEY_PREFIX_EXPINFO.length()+1).contains("coolpad") ||
						strClientChannel.substring(FeederConstants.REDIS_KEY_PREFIX_EXPINFO.length()+1).contains("emui") ||
						strClientChannel.substring(FeederConstants.REDIS_KEY_PREFIX_EXPINFO.length()+1).contains("ali"))
					{
						lAllowMaxNum *= 10;
					}

					long lCutSize = jedisCache.zcard(strClientChannel) - lAllowMaxNum;
					if (lCutSize > 0)
						jedisCache.zremrangeByRank(strClientChannel, 50, lCutSize-1+50);
				}
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for " + field_name + "...");
		}
	}
	//本次流量探索池计算
	private static Double doCurrentCalcFlowExpinfo(Integer rate,Integer listImageCount,Integer detailImageCount,Integer copyrightCount)
	{
		Double rateValue = 0.0;
		//rate逻辑判断
		if (rate>=1000000){
			rateValue = RATE_MAPPING.get("100w");
		}
		if (rate>=500000 && rate<1000000){
			rateValue = RATE_MAPPING.get("50w");
		}
		if (rate<500000 && rate>=100000){
			rateValue = RATE_MAPPING.get("10w");
		}
		if (rate<100000 && rate>=10000){
			rateValue = RATE_MAPPING.get("1w");
		}
		if (rate<10000 && rate>=1000){
			rateValue = RATE_MAPPING.get("0.1w");
		}
		if (rate<1000 && rate>=100){
			rateValue = RATE_MAPPING.get("0.01w");
		}

		//listImage逻辑判断
		Double listImageValue = 0.0;
		if (listImageCount == 0){
			listImageValue = RATE_MAPPING.get("0li");
		}
		if (listImageCount == 1){
			listImageValue = RATE_MAPPING.get("1li");
		}
		if (listImageCount == 3){
			listImageValue = RATE_MAPPING.get("3li");
		}

		//detailImage逻辑判断
		Double detailImageValue = 0.0;
		if (detailImageCount < 3){
			detailImageValue = RATE_MAPPING.get("0dl");
		}
		if (detailImageCount >= 3 && detailImageCount <= 10){
			detailImageValue = RATE_MAPPING.get("3dl");
		}
		if (detailImageCount < 3){
			detailImageValue = RATE_MAPPING.get("10dl");
		}

		//copyright逻辑判断
		Double copyrightValue = 0.0;
		if (copyrightCount == 0){
			copyrightValue = RATE_MAPPING.get("0cr");
		}
		if (copyrightCount == 1){
			copyrightValue = RATE_MAPPING.get("1cr");
		}

		Double sourceRateCoefficient    = RATE_MAPPING.get("source_rate_key");
		Double listImageCoefficient     = RATE_MAPPING.get("list_image_key");
		Double detailImageCoefficient   = RATE_MAPPING.get("detail_image_key");
		Double hashCopyrightCoefficient = RATE_MAPPING.get("has_copyright_key");

		Double result = rateValue*sourceRateCoefficient+listImageValue*listImageCoefficient+detailImageValue*detailImageCoefficient+copyrightValue*hashCopyrightCoefficient;
		Double finalResult = Math.abs(1-result)+1;
		return finalResult;
	}

	//上次流量探索池计算
	private static void doLastCalcFlowExpinfo()
	{
		try
		{
			long tsCurrentTime = System.currentTimeMillis() / 1000;
			String[] arrAppLanaguage = FLOW_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firm_app = tmp[0];
				String[] arrLanguages = tmp[1].split(FeederConstants.SEPARATOR_APP_LANGUAGE);
				for (String main_language : arrLanguages)
				{
					String strContentType = null;
					if (main_language.indexOf("_") >= 0) {
						String[] s = main_language.split("_");
						main_language = s[0];
						strContentType = s[1];
					}

					String expinfo_key = (strContentType == null) ? FeederConstants.getExpInfoKey(firm_app, main_language) : FeederConstants.getExpInfoKey(firm_app, main_language, strContentType);
					Long redisSize = jedisCache.zcard(expinfo_key);
					flowLogger.info("\t\t start attenuation key=" + expinfo_key + " size=" + redisSize);
					Set<Tuple> tuples = jedisCache.zrangeWithScores(expinfo_key, 50, redisSize - 1);
					Iterator<Tuple> its = tuples.iterator();
					while (its.hasNext())
					{
						Tuple tuple = its.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						String contentType = s[1];
						long tsPublishTime = Long.parseLong(s[4]);
						boolean bNeedRemove = false;
						boolean bVideoInfo = isVideoInfo(NumberUtils.toInt(contentType));
						boolean bMemesInfo = isMemesInfo(NumberUtils.toInt(contentType));
						boolean bGifInfo = isGif(NumberUtils.toInt(contentType));
						
						if (bGifInfo && (tsCurrentTime - tsPublishTime >= (EXPINFO_EFFECTIVE_TIME_CONSTRAINT_GIF/1000))) {
							bNeedRemove = true;
						}else if (bMemesInfo && (tsCurrentTime - tsPublishTime >= (EXPINFO_EFFECTIVE_TIME_CONSTRAINT_MEME/1000))) {
							bNeedRemove = true;
						}else if (bVideoInfo && (tsCurrentTime - tsPublishTime >= (EXPINFO_EFFECTIVE_TIME_CONSTRAINT_VIDEO/1000))) {
							bNeedRemove = true;
						} else if ((!bGifInfo) && (!bMemesInfo)&& (!bVideoInfo) && tsCurrentTime - tsPublishTime >= (EXPINFO_EFFECTIVE_TIME_CONSTRAINT_NEWS/1000)) {
							bNeedRemove = true;
							//lockscreen 的只有news
							removeArticleIDFromExpinfoOutOfDate(firm_app, main_language, infoid_with_type, true);
						}

						if (bNeedRemove) {							
							removeArticleIDFromExpinfoOutOfDate(firm_app, main_language, infoid_with_type, false);
						} else {
							Double score = tuple.getScore();
							Double realScore = Math.abs(1-score);
							Double calcScore = realScore*ATTENUATION_COEFFICIENT;
							Double final_score = Math.abs(1-calcScore);
							if (expinfoLogger.isDebugEnabled())
							{
								expinfoLogger.debug("\t\t start attenuation key="+expinfo_key+",contentId="+infoid_with_type+" from oldvalue="+realScore+" attenuation to newvalue="+calcScore);
							}
							//回写redis
							addInfoExpInfoRedis(FeederConstants.LOG_EVENT_REDUNDANT_INTO_EXPINFO, expinfo_key, null,null, infoid_with_type, final_score);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			flowLogger.error("\t\t[doLastCalcFlowExpinfo]", e);
		}
	}

	//处理流量探索池的前50条数据-如已有GMP也会删除
	@SuppressWarnings("unchecked")
	private static void doHandlerBeforeFlowFifty(String expinfo_key)
	{
		Set<Tuple> rateSet = jedisCache.zrangeWithScores(expinfo_key, 0, 49);
		for (Tuple tuple : rateSet)
		{
			String infoid_with_type = tuple.getElement();
			Double rateValue = tuple.getScore();
			if (rateValue != null)
			{
				addInfoExpInfoRedis(FeederConstants.LOG_EVENT_REDUNDANT_INTO_EXPINFO, expinfo_key, null,null, infoid_with_type, 0.0f);
			}
		}
	}

	//检查adultscore方法
	private static Boolean checkAdultScore(String checkedKey,Double aduleScoreValue, FeederInfo feederInfo)
	{
		Double adultScore = ADULT_SCORE_MAP.get(checkedKey);
		if (adultScore == null)
		{
			String key = FeederConstants.getAdultScoreKey(FeederConstants.ADULT_UNKNOWN);
			adultScore = ADULT_SCORE_MAP.get(key);
		}
		if (adultScore < aduleScoreValue)
		{
			return true;
		}
		return false;
	}

	//不同频道的过期时间不同
	private static void doAllInfoIDs(List<Info> info_list, Set<String> hsBlackList, Set<String> finePoolIds)
	{
		//t_content,t_signal 加载
		if (info_list != null)
		{
			for (Info info : info_list)
			{
				String contentId = info.feederInfo.getContent_id();
                Date publishTime = DateUtils.stringToDate(info.feederInfo.getPublish_time(), new Date());
				String strAllArticleInfoIdsKey = FeederConstants.getAllArticleInfoIdsKey(contentId);
				String strFallbackArticleInfoIdsKey = FeederConstants.getFallbackArticleInfoIdsKey(contentId);
				if (CollectionUtils.isNotEmpty(hsBlackList) && hsBlackList.contains(contentId))
				{
					//flowLogger.info("\t\t contentId="+contentId+" cannot access " + FeederConstants.REDIS_KEY_ALL_INFOID + " caused by same group id.");
					flowLogger.info("\t\t contentId="+contentId+" cannot access " + strAllArticleInfoIdsKey + " caused by same group id.");
					continue;
				}
                //精品池资讯时效性处理
                if(CollectionUtils.isNotEmpty(finePoolIds)){
                    if(finePoolIds.contains(contentId)){
                        int expiryHour = 720;
                        long pbTime = publishTime.getTime();
                        long expriedTime = expiryHour * MILLISECONDS_IN_HOUR;
                        long addResultTime = expriedTime + pbTime;
                        flowLogger.info("\t\t t_high_quality_news:contentid="+contentId+",pbtime="+pbTime+",expriedtime="+expriedTime+",addresulttime="+addResultTime);
                        jedisDetail.zadd(strAllArticleInfoIdsKey, Double.valueOf(addResultTime), contentId);
                        continue;
                    }

                }
				String sourceType = info.feederInfo.getSourceType();
				if ("selfmedia".equals(sourceType)) {
					int expiryHour = 720;
					long pbTime = publishTime.getTime();
					long expriedTime = expiryHour * MILLISECONDS_IN_HOUR;
					long addResultTime = expriedTime + pbTime;
					flowLogger.info("\t\t t_content table_selfmedia:contentid="+contentId+",pbtime="+pbTime+",expriedtime="+expriedTime+",addresulttime="+addResultTime);
					//jedisDetail.zadd(FeederConstants.REDIS_KEY_ALL_INFOID, Double.valueOf(addResultTime), contentId);
					jedisDetail.zadd(strAllArticleInfoIdsKey, Double.valueOf(addResultTime), contentId);
					jedisDetail.zadd(strFallbackArticleInfoIdsKey, Double.valueOf(addResultTime), contentId);
				} else {
					List<String> categories =  getCategoriesListByVersion(info.feederInfo.getCategories(), CATEGORY_VERSION);
					if (categories.size() > 0)
					{
						String strContentType = info.feederInfo.getContent_type();
						Map<Integer, Integer> mapCategoryFallbackExpiryInfo = mContentTypeCategoryExpiry.get(String.valueOf(ContentType.DEFAULT.getValue()));
						Map<Integer, Integer> mapCategoryNormalExpiryInfo = (mContentTypeCategoryExpiry.containsKey(strContentType)) ? mContentTypeCategoryExpiry.get(strContentType) : mContentTypeCategoryExpiry.get(String.valueOf(ContentType.DEFAULT.getValue()));
						for (String strCategroyId : categories)
						{
							Integer expiryHour = mapCategoryNormalExpiryInfo.get(Integer.valueOf(strCategroyId));

							long pbTime = publishTime.getTime();
							if (expiryHour == null)
							{
								flowLogger.info("\t\t expiry hour is not set for category " + strCategroyId);
								expiryHour = 48;
							}
							long expriedTime = expiryHour * MILLISECONDS_IN_HOUR;
							long addResultTime = expriedTime + pbTime;
							flowLogger.info("\t\t t_content table:contentid="+contentId+", category="+strCategroyId+",expiryHour="+expiryHour+",pbtime="+pbTime+",expriedtime="+expriedTime+",addresulttime="+addResultTime);
							jedisDetail.zadd(strAllArticleInfoIdsKey, Double.valueOf(addResultTime), contentId);
							//
							Integer fallbackExpiryHour = mapCategoryFallbackExpiryInfo.get(Integer.valueOf(strCategroyId));
							if (fallbackExpiryHour == null)
							{
								flowLogger.info("\t\t fallback expiry hour is not set for category " + strCategroyId);
								fallbackExpiryHour = 48;
							}
							expriedTime = fallbackExpiryHour * MILLISECONDS_IN_HOUR;
							addResultTime = expriedTime + pbTime;
							flowLogger.info("\t\t t_content table:contentid="+contentId+", category="+strCategroyId+",fallback expiryHour="+fallbackExpiryHour+",pbtime="+pbTime+",expriedtime="+expriedTime+",addresulttime="+addResultTime);
							jedisDetail.zadd(strFallbackArticleInfoIdsKey, Double.valueOf(addResultTime), contentId);
						}
					}
					else
					{
						flowLogger.error("\t\t t_content table:contentid="+contentId+" has no category");
					}
				}
			}
		}
	}

	private static void doStoreIntoES(List<FeederInfo> info_list)
	{
		if (esClient == null)
			return;

		if (info_list.size() > 0)
		{
			Map<String, Object> esProperties = null;
			String jsonString = null;
			JSONDataStruct2L jsondata2l = null;
			Map<String, List<Map<String, Object>>> maps2l = null;
			JSONDataStruct3 jsondata3 = null;
			Map<String, Map<String, Map<String, Object>>> maps3 = null;
			JSONDataStruct3Double jsondata3_d = null;
			Map<String, Map<String, Map<String, Double>>> maps3_d = null;
			JSONDataStruct2Double jsondata2_d = null;
			Map<String, Map<String, Double>> maps2_d = null;
			JSONDataStruct1Double jsondata1_d = null;
			Map<String, Double> maps1_d = null;

			BulkRequestBuilder bulkRequest = esClient.prepareBulk();
			for (FeederInfo info : info_list)
			{
				esProperties = new HashMap<String, Object>();
				esProperties.put("content_id", info.getContent_id());
				esProperties.put("cp_version", info.getCp_version());
				esProperties.put("tier", info.getTier());
				esProperties.put("important_level", String.valueOf(info.getImportant_level()));
				esProperties.put("body_image_count", info.getBody_image_count());
				esProperties.put("type", (info.getType()!=null && info.getType().isEmpty()==false) ? Integer.valueOf(info.getType()):0);
				esProperties.put("content_type", info.getContent_type());
				esProperties.put("link_type", info.getLink_type());
				esProperties.put("display_type", info.getDisplay_type());
				esProperties.put("title", info.getTitle());
				esProperties.put("duration", info.getDuration());
				esProperties.put("group_id", info.getGroup_id());

				//split "local"
				jsonString = "{\"jsondata\":" + info.getLocal() +"}";
				jsondata2l = null;
				maps2l = null;
				try {
					jsondata2l = JSON.parseObject(jsonString, JSONDataStruct2L.class);
					maps2l = jsondata2l.getJsondata();
					if (maps2l != null) {
						for ( String key : maps2l.keySet() )
						{
							//System.out.println(key + ": " + maps2l.get(key).toString());
							esProperties.put("local_"+key, JSON.toJSONString(maps2l.get(key), true));
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES]", e);
				}
				//split "tags"
				jsondata3 = null;
				maps3 = null;
				try
				{
					jsonString = "{\"jsondata\":" + info.getTags() +"}";
					jsondata3 = JSON.parseObject(jsonString, JSONDataStruct3.class);
					maps3 = jsondata3.getJsondata();
					if (maps3 != null)
					{
						for ( String key : maps3.keySet() )
						{
							esProperties.put("tags_"+key, new JSONObject(maps3.get(key)).toString());
						}
					}
				}
				catch (Exception e)
				{
					flowLogger.error("[doStoreIntoES.tags]", e);
				}
				//split "categories"
				jsondata3_d = null;
				maps3_d = null;
				try {
					jsonString = "{\"jsondata\":" + info.getCategories() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("categories_"+key, new JSONObject(maps3_d.get(key)).toString());

							if (key.equals("v1") || key.equals("v8") || key.equals("v28"))
							{
								/**
								 * Due to categories v1/v8 may have multiple ids, need to set a list into ES.
								 */
								Map<String, Map<String, Double>> mapCatIDsWithWeight = maps3_d.get(key);
								Iterator<Entry<String, Map<String, Double>>> iterIDs = mapCatIDsWithWeight.entrySet().iterator();
								List<String> listVerCatIDs = new ArrayList<String>();
								while (iterIDs.hasNext()) {
									listVerCatIDs.add(iterIDs.next().getKey());
								}
								esProperties.put("category_"+key, listVerCatIDs);
							}
							else //other versions
							{
								Map<String, Map<String, Double>> ver = maps3_d.get(key);
								String strCategory = "";
								double dbCategory = -1;

								Iterator<Entry<String, Map<String, Double>>> iter = ver.entrySet().iterator();
								while (iter.hasNext())
								{
									Map.Entry<String, Map<String, Double>> mapLanguage = iter.next();

									if (mapLanguage.getValue().get("weight") > dbCategory)
									{
										dbCategory = mapLanguage.getValue().get("weight");
										strCategory = mapLanguage.getKey();
									}
								}
								if (StringUtils.isNotEmpty(strCategory))
								{
									esProperties.put("category_"+key, Integer.valueOf(strCategory));
								}
							}
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.categories]", e);
				}
				//split "language"
				try {
					jsonString = "{\"jsondata\":" + info.getLanguage() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("language_"+key, new JSONObject(maps3_d.get(key)).toString());

							Map<String, Map<String, Double>> v1 = maps3_d.get(key);
							String strLanguage = "";
							double dbLanguage = -1;

							Iterator<Entry<String, Map<String, Double>>> iter = v1.entrySet().iterator();
							while (iter.hasNext())
							{
								Map.Entry<String, Map<String, Double>> mapLanguage = iter.next();

								if (mapLanguage.getValue().get("weight") > dbLanguage)
								{
									dbLanguage = mapLanguage.getValue().get("weight");
									strLanguage = mapLanguage.getKey();
								}
							}
							//处理权重在0.5以上
							if (dbLanguage >= 0.5)
							{
								//处理language==All
								if (FeederConstants.LANGUAGE_MULTILINGUAL.equalsIgnoreCase(strLanguage))
								{
									esProperties.put("language_"+key+"_main", alLanguage);
								}
								else
								{
									esProperties.put("language_"+key+"_main", strLanguage);
								}
							}
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.language]", e);
				}
				//split "keywords"
				try {
					jsonString = "{\"jsondata\":" + info.getKeywords() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("keywords_"+key, new JSONObject(maps3_d.get(key)).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.keywords]", e);
				}
				//split "ner_person"
				try {
					jsonString = "{\"jsondata\":" + info.getNer_person() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("ner_person_"+key, new JSONObject(maps3_d.get(key)).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.ner_person]", e);
				}
				//split "ner_location"
				try {
					jsonString = "{\"jsondata\":" + info.getNer_location() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("ner_location_"+key, new JSONObject(maps3_d.get(key)).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.ner_location]", e);
				}
				//split "ner_organization"
				try {
					jsonString = "{\"jsondata\":" + info.getNer_organization() +"}";
					jsondata3_d = JSON.parseObject(jsonString, JSONDataStruct3Double.class);
					maps3_d = jsondata3_d.getJsondata();
					if (maps3_d != null) {
						for ( String key : maps3_d.keySet() )
						{
							//System.out.println(key + ": " + maps3_d.get(key).toString());
							esProperties.put("ner_organization_"+key, new JSONObject(maps3_d.get(key)).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.ner_organization]", e);
				}
				//split "emotion"
				jsondata2_d = null;
				maps2_d = null;
				try {
					jsonString = "{\"jsondata\":" + info.getEmotion() +"}";
					jsondata2_d = JSON.parseObject(jsonString, JSONDataStruct2Double.class);
					maps2_d = jsondata2_d.getJsondata();
					if (maps2_d != null) {
						for ( String key : maps2_d.keySet() )
						{
							//System.out.println(key + ": " + maps2_d.get(key).toString());
							esProperties.put("emotion_"+key, new JSONObject(maps2_d.get(key)).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.emotion]", e);
				}
				//split "adult_score"
				jsondata1_d = null;
				maps1_d = null;
				try {
					jsonString = "{\"jsondata\":" + info.getAdult_score() +"}";
					jsondata1_d = JSON.parseObject(jsonString, JSONDataStruct1Double.class);
					maps1_d = jsondata1_d.getJsondata();
					if (maps1_d != null) {
						for ( String key : maps1_d.keySet() )
						{
							//System.out.println(key + ": " + maps1_d.get(key).toString());
							esProperties.put("adult_score_"+key, maps1_d.get(key).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.adult_score]", e);
				}
				//split "news_score"
				try {
					jsonString = "{\"jsondata\":" + info.getNews_score() +"}";
					jsondata1_d = JSON.parseObject(jsonString, JSONDataStruct1Double.class);
					maps1_d = jsondata1_d.getJsondata();
					if (maps1_d != null) {
						for ( String key : maps1_d.keySet())
						{
							//System.out.println(key + ": " + maps1_d.get(key).toString());
							esProperties.put("news_score_"+key, maps1_d.get(key).toString());
						}
					}
				} catch (Exception e) {
					flowLogger.error("[doStoreIntoES.news_score]", e);
				}
				//firm_app
				try
				{
					List<String> listFirmApp = new ArrayList<String>();
					listFirmApp.addAll( listFirmApp(info) );
					if (CollectionUtils.isNotEmpty(listFirmApp))
					{
						esProperties.put("firm_app", listFirmApp);
					}
				}
				catch (Exception e)
				{
					flowLogger.error("[doStoreIntoES.firm_app]", e);
				}

				//publish_time_ts
				esProperties.put("publish_time_ts", info.getPublish_time_ts());

				//list_images
				esProperties.put("list_images", info.getListImages());

				//link
				esProperties.put("link", info.getLink());

				//source
				esProperties.put("source", info.getSource());

				//publisher
				esProperties.put("publisher", info.getPublisher());

				//source_type
				esProperties.put("source_type", info.getSourceType());

				//content_quality
				esProperties.put("content_quality", info.getContentQuality());

				bulkRequest.add(esClient.prepareIndex("tchannel", "tchannel", info.getContent_id())
					.setSource(new JSONObject(esProperties).toString())
					);
			}

			try
			{
				BulkResponse bulkResponse = bulkRequest.execute().actionGet();
				for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
					if (StringUtils.isEmpty(itemResponse.getFailureMessage())) {
						flowLogger.info(" The id "+itemResponse.getId()+" has been inserted.");
					} else {
						flowLogger.info(" The id "+itemResponse.getId()+" has not been inserted. failed reason:" + itemResponse.getFailureMessage());
					}
				}
			}
			catch (Exception e)
			{
				flowLogger.error("[ElasticSearch] prepareIndex", e);
			}
		}
		else {
			flowLogger.info("\t\tNo new data to process for ES...");
		}
	}

	public static String getCurrentHDFSFilename()
	{
		Calendar cal = (bOversea) ? Calendar.getInstance(TimeZone.getTimeZone("UTC")) : Calendar.getInstance();
		cal.setTime(new Date());
		String strFilename = "news_"+cal.get(Calendar.YEAR)+
				String.format("%02d", cal.get(Calendar.MONTH)+1)+
				String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))+
				String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))+
				String.format("%02d", (cal.get(Calendar.MINUTE)/10)*10)+
				".txt";

		return strFilename;
	}

	private static void doStoreIntoFileForDruid(List<Map<String, String>> infoMapsList)
	{
		if (infoMapsList.size() > 0)
		{
			try
			{
				File fHDFSFilename = new File(strLocalFilePath+"toDruid/"+getCurrentHDFSFilename());
				if (!fHDFSFilename.getParentFile().exists())
				{
					fHDFSFilename.getParentFile().mkdirs();
				}
				if (!fHDFSFilename.exists())
				{
					fHDFSFilename.createNewFile();
				}

				FileWriter fileWritter = new FileWriter(fHDFSFilename.getAbsolutePath(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				for (Map<String, String> infoMap : infoMapsList)
				{
					flowLogger.info("[doStoreIntoFileForDruid] contentId=" + infoMap.get("content_id"));
					bufferWritter.write(new JSONObject(infoMap).toString());
					bufferWritter.write("\n");
				}
				bufferWritter.flush();
				bufferWritter.close();
			}
			catch (IOException e)
			{
				flowLogger.error("[doStoreIntoFileForDruid]", e);
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for Druid...");
		}
	}

	private static void doStoreIntoFileForHDFS(List<Map<String, String>> infoMapsList)
	{
		if (infoMapsList != null && infoMapsList.size() > 0)
		{
			try
			{
				File fHDFSFilename = new File(strLocalFilePath+"toHDFS/"+getCurrentHDFSFilename());
				if (!fHDFSFilename.getParentFile().exists())
				{
					fHDFSFilename.getParentFile().mkdirs();
				}
				if (!fHDFSFilename.exists())
				{
					fHDFSFilename.createNewFile();
				}

				FileWriter fileWritter = new FileWriter(fHDFSFilename.getAbsolutePath(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				for (Map<String, String> infoMap : infoMapsList)
				{
					flowLogger.info("[doStoreIntoFileForHDFS] contentId=" + infoMap.get("content_id"));
					bufferWritter.write(new JSONObject(infoMap).toString());
					bufferWritter.write("\n");
				}
				bufferWritter.flush();
				bufferWritter.close();
			}
			catch (IOException e)
			{
				flowLogger.error("[doStoreIntoFileForHDFS]", e);
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for HDFS...");
		}
	}

	private static void doStoreIntoFileForSourceSubscription(List<FeederInfo> feederInfoList,List<SourceSubscriptionEntry> sourceSubscriptionInfos)
	{
		if (feederInfoList.size() > 0)
		{
			for (FeederInfo  feederInfo: feederInfoList)
			{
				String source = feederInfo.getSource();
				String channel = feederInfo.getChannel();
				for (SourceSubscriptionEntry sourceSubscriptionInfo : sourceSubscriptionInfos)
				{
					//source_id 作为KEY
					String sourceSubscriptionId = sourceSubscriptionInfo.getSubscribeId();

					String sourceSubscriptionRssName = sourceSubscriptionInfo.getRssName();

					String sourceSubscriptionChannelName = sourceSubscriptionInfo.getChannelName();

					if (channel != null && source != null)
					{
						if (channel.equals(sourceSubscriptionChannelName) && source.equals(sourceSubscriptionRssName))
						{
							String subscriptionRedisKey = FeederConstants.getSourceSubscriptionKey(sourceSubscriptionId);

							String contentId = feederInfo.getContent_id();

							//写入redis
							int countRedis = jedisDetail.zcard(subscriptionRedisKey).intValue();

							if (countRedis >= 1000)
							{
								jedisDetail.zremrangeByRank(subscriptionRedisKey, 0, countRedis-1000);
							}

							jedisDetail.zadd(subscriptionRedisKey, Double.valueOf(contentId), contentId);

							flowLogger.info("\t\tSet key = "+subscriptionRedisKey+",score = "+contentId+",contentId="+contentId+" is saved ...");
						}
					}
				}
			}
		}
		else
		{
			flowLogger.info("\t\tNo new data to process for source subscription redis...");
		}
	}
	private static List<String> listGroupId(List<EditorTableEntry> editorTableEntryList)
	{
		List<String> alGroupId = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(editorTableEntryList))
		{
			ArrayList<String> alContentId = new ArrayList<String>();
			for (EditorTableEntry entry : editorTableEntryList)
			{
				alContentId.add( entry.getContentId() );
			}
			alGroupId.addAll( clientJDBCTemplate.getDistinctGroupId(alContentId) );
		}
		return alGroupId;
	}
	public static FeederInfo applyEditorData(FeederInfo feederInfo, EditorTableEntry editorEntry)
	{
		if (editorEntry != null)
		{
			if (editorEntry.getTitle() != null)
				feederInfo.setTitle(editorEntry.getTitle());

			if (StringUtils.isNotEmpty(editorEntry.getCategories()))
				feederInfo.setCategories(editorEntry.getCategories());

			if (StringUtils.isNotEmpty(editorEntry.getContent()))
				feederInfo.setContent(editorEntry.getContent());

			if (StringUtils.isNotEmpty(editorEntry.getFlag()))
				feederInfo.setFlag(editorEntry.getFlag());

			if (StringUtils.isNotEmpty(editorEntry.getAdultScore()))
				feederInfo.setAdult_score(editorEntry.getAdultScore());

			if (editorEntry.getLinkType() != null && editorEntry.getLinkType() != -1)
				feederInfo.setLink_type(editorEntry.getLinkType());

			if (StringUtils.isNotEmpty(editorEntry.getSource()))
				feederInfo.setSource(editorEntry.getSource());

			if (StringUtils.isNotEmpty(editorEntry.getSourceType()))
				feederInfo.setSourceType(editorEntry.getSourceType());

			if (StringUtils.isNotEmpty(editorEntry.getSourceFeeds()))
				feederInfo.setSourceFeeds(editorEntry.getSourceFeeds());

			if (StringUtils.isNotEmpty(editorEntry.getChannel()))
				feederInfo.setChannel(editorEntry.getChannel());

			if (StringUtils.isNotEmpty(editorEntry.getFirmApp()))
				feederInfo.setFirm_app(editorEntry.getFirmApp());

			if (StringUtils.isNotEmpty(editorEntry.getLocal()))
				feederInfo.setLocal(editorEntry.getLocal());

			if (editorEntry.getContentType() != null && editorEntry.getContentType() != -1)
				feederInfo.setContent_type(String.valueOf(editorEntry.getContentType()));

			if (StringUtils.isNotEmpty(editorEntry.getType()))
				feederInfo.setType(editorEntry.getType());

			if (StringUtils.isNotEmpty(editorEntry.getLanguage()))
				feederInfo.setLanguage(editorEntry.getLanguage());

			if (StringUtils.isNotEmpty(editorEntry.getCopyright()))
				feederInfo.setCopyright(editorEntry.getCopyright());

			if (editorEntry.getHasCopyright() != null &&  editorEntry.getHasCopyright() >= 0)
				feederInfo.setHasCopyright(editorEntry.getHasCopyright());

			if (editorEntry.getDisplayType() != null &&  editorEntry.getDisplayType() != -1)
				feederInfo.setDisplay_type(editorEntry.getDisplayType());

			if (StringUtils.isNotEmpty(editorEntry.getFallImage()))
				feederInfo.setFallImage(editorEntry.getFallImage());

			if (StringUtils.isNotEmpty(editorEntry.getAuthor()))
				feederInfo.setAuthor(editorEntry.getAuthor());

			if (StringUtils.isNotEmpty(editorEntry.getPublisher()))
				feederInfo.setPublisher(editorEntry.getPublisher());

			if (StringUtils.isNotEmpty(editorEntry.getLink()))
				feederInfo.setLink(editorEntry.getLink());

			if (StringUtils.isNotEmpty(editorEntry.getSummary()))
				feederInfo.setSummary(editorEntry.getSummary());

			if (editorEntry.getRate() != null && editorEntry.getRate() > 0)
				feederInfo.setRate(editorEntry.getRate());

			if (StringUtils.isNotEmpty(editorEntry.getBodyImages()))
				feederInfo.setBodyImages(editorEntry.getBodyImages());

			if (StringUtils.isNotEmpty(editorEntry.getListImages()))
				feederInfo.setListImages(editorEntry.getListImages());

			if (editorEntry.getBodyImagesCount() != null &&  editorEntry.getBodyImagesCount() != -1)
				feederInfo.setBodyImagesCount(editorEntry.getBodyImagesCount());

			if (editorEntry.getListImagesCount() != null &&  editorEntry.getListImagesCount() != -1)
				feederInfo.setListImagesCount(editorEntry.getListImagesCount());

			if (StringUtils.isNotEmpty(editorEntry.getPublishTime()) && editorEntry.getPublishTime().length() > 10)
			{
				Date datePublishTime = DateUtils.stringToDate(editorEntry.getPublishTime(), new Date());
				feederInfo.setPublish_time(editorEntry.getPublishTime());
				Timestamp ts = new Timestamp(datePublishTime.getTime());
				feederInfo.setPublish_time_ts((int)(ts.getTime()/1000));
			}

			if (editorEntry.getContentQuality() != null && editorEntry.getContentQuality() != -1)
				feederInfo.setContentQuality(editorEntry.getContentQuality());

			if (editorEntry.getIs_open_comment() != null && editorEntry.getIs_open_comment() >= 0)
				feederInfo.setIsOpenComment(String.valueOf(editorEntry.getIs_open_comment()));
		}
		return feederInfo;
	}


	
	/***
	 * @date 2018年6月27日 20:29:01
	 * 针对等待池,移除满足探索阈值不需要在探索的有效文章 以及删除在等待池里无效文章
	 *  验证文章的是否在等待池超时（大于最大等待池时间）
	 * @author yezi
	 */
	private static void removeExploreWaitingPoolInvalidArticles(){
		long tsCurrentTime = System.nanoTime();
		try
		{			
			String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(FeederConstants.SEPARATOR_APP_LANGUAGE);
				for (String mainLanguage : arrLanguages)
				{
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					
					long startTime = System.nanoTime();

					String expinfoWaitingkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"eagerly") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
					String expinfoImpressionInfokey = FeederConstants.getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					
//
					Long redisSize = jedisCache.zcard(expinfoWaitingkey);
					expinfo_remove_logger.info("\t ---start waiting key=" + expinfoWaitingkey + " size=" + redisSize+" expinfoImpressionInfokey="+expinfoImpressionInfokey);
					//get waiting max time
					long maxWaitingTime = 48*60*60*1000;
					
					if(strContentType != null && strContentType.equalsIgnoreCase("video")){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_VIDEO;
					}else if(strContentType != null && strContentType.equalsIgnoreCase("gif")){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_GIF;
					}else if(strContentType != null && strContentType.equalsIgnoreCase("meme")){
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_MEME;
					}else{
						maxWaitingTime = EXPINFO_WAITING_MAX_TIME_NEWS;
					}
					
					long currentTime = new Date().getTime();
					long maxwaitingDate = currentTime - maxWaitingTime;
					Set<String> listNeedToDeletedContentData = new HashSet<String>();
					Set<String> listNeedToDeletedContentId = new HashSet<String>();
					//1 get out of date waiting time articles
					expinfo_remove_logger.info("\t" + expinfoWaitingkey +" maxwaitingDate = "+ maxwaitingDate);
					Set<Tuple> removeOutOfMaxWaitingTimeTupleSet = jedisCache.zrangeByScoreWithScores(expinfoWaitingkey, 0, maxwaitingDate);
					Iterator<Tuple> outOfMaxWaitingTimeIts = removeOutOfMaxWaitingTimeTupleSet.iterator();
					while (outOfMaxWaitingTimeIts.hasNext())
					{
						Tuple tuple = outOfMaxWaitingTimeIts.next();
						String infoid_with_type = tuple.getElement();
						String[] s = infoid_with_type.split("#");
						String contentId = s[0];
						listNeedToDeletedContentId.add(contentId);
						listNeedToDeletedContentData.add(infoid_with_type);	
						
						logParam = new Object[] { EVENT_REMOVE_WAITING_EXPLORE, expinfoWaitingkey, contentId, System.currentTimeMillis() };
						expinfo_remove_logger.info("\t  "+expinfoWaitingkey + ", out of date waiting time article,"+ StringUtils.join(logParam, LOG_TAG));
					}
										
					expinfo_remove_logger.info("\t" + expinfoWaitingkey + ", get OutOfMaxWaitingTimeSet, listNeedToDeletedContentId.size :" + listNeedToDeletedContentId.size() + " cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
					if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
						removeBatchArticlesFromWaitingPool( expinfoWaitingkey, listNeedToDeletedContentData.toArray(new String[0]));
						removeBatchArticlesFromImpressionInfoPool( expinfoImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));	
					//	removeBatchArticlesFromImpressionInfoPool( expinfoAllImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));	
					}
					
				}
			}
				
		} catch (Exception e) {
			expinfo_remove_logger.info("\t\t removeExploreWaitingPoolArticles: " + e);
		}
		
		expinfo_remove_logger.info("\t\t finish removeExploreWaitingPoolArticles cost: " + (System.nanoTime() - tsCurrentTime)/1000000 + " ms");
	}
	

	
	/**
	 * @date 2018年6月27日 20:29:01
	 *  针对探索池、等待池、进行池,移除满足探索阈值不需要在探索的有效文章
	 *  a 验证文章的impression已经满足探索阈值加入移除队列里，移除满足阈值以及非法队列里的文章信息
	 *  b 遍历所有的各产品+contentType+各池子遍历移除满足阈值以及非法队列里的文章信息
	 * @author yezi
	 */
	private static void removeBiggerThresholdVaildArticles(){
		long startTime = System.nanoTime();
		
		Map<String, Double> hmContentIdsWaitData = new HashMap<String, Double>();
		Map<String, String> hmContentIdsData = new HashMap<String, String>();
		Set<String> listNeedToDeletedContentData = new HashSet<String>();
		Set<String> listNeedToDeletedContentId = new HashSet<String>();
		Map<String, String> hmContentIdsForExplorePoolData = new HashMap<String, String>();
//		Map<String,Integer> hmContentIdsImpression = new HashMap<String,Integer>();
		
		String[] arrAppLanaguage = MONITOR_EXPLINO_WAITING_PRODUCT_CHANNEL_LANGUAGES.split(",");
		for (String strAppLanguage : arrAppLanaguage)
		{
			String[] tmp = strAppLanguage.split("=");
			if (tmp.length < 2)
				continue;
			String firmApp = tmp[0];
			String[] arrLanguages = tmp[1].split(FeederConstants.SEPARATOR_APP_LANGUAGE);
			for (String mainLanguage : arrLanguages)
			{			
				String strContentType = null;
				if (mainLanguage.indexOf("_") >= 0) {
					String[] s = mainLanguage.split("_");
					mainLanguage = s[0];
					strContentType = s[1];
				}
				String expinfoAllImpressionInfokey = FeederConstants.getExpInfoKey("-1", mainLanguage,"impressionOfInfoIdKey") ;
				
				// 其他各个产品 等待池 探索池 进行池					
				String exploreKey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage) : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType);
				String expinfoExploringkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"ing") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
				String expinfoWaitingkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"eagerly") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
				
				
			
				int threshold = EXPINFO_NEWS_THRESHOLD;
			
				Set<Tuple> effectiveExpTuples = jedisCache.zrangeWithScores(exploreKey, 0, -1);
				Set<Tuple> effectiveExploringTuples = jedisCache.zrangeWithScores(expinfoExploringkey, 0, -1);
				Set<Tuple> effectiveWaitingTuples = jedisCache.zrangeWithScores(expinfoWaitingkey, 0, -1);
				
				if(strContentType != null && strContentType.equalsIgnoreCase("video")){
					threshold = EXPINFO_VIDEO_THRESHOLD;
				}else if(strContentType != null && strContentType.equalsIgnoreCase("gif")){
					threshold = EXPINFO_GIF_THRESHOLD;
				}else if(strContentType != null && strContentType.equalsIgnoreCase("meme")){
					threshold = EXPINFO_MEME_THRESHOLD;
				}else{
					threshold = EXPINFO_NEWS_THRESHOLD;
				}
				
				
				Set<Tuple> effectiveTuples = new HashSet<Tuple> ();
				effectiveTuples.addAll(effectiveExpTuples);
				effectiveTuples.addAll(effectiveExploringTuples);
				effectiveTuples.addAll(effectiveWaitingTuples);
				
				
				Iterator<Tuple> its = effectiveTuples.iterator();
				List<String> checkImpressionArticleArr = new ArrayList<String>();
			
	
				while (its.hasNext())
				{
					Tuple tuple = its.next();
					String infoid_with_type = tuple.getElement();
					String[] s = infoid_with_type.split("#");
					String contentId = s[0];
					checkImpressionArticleArr.add(contentId);
					hmContentIdsData.put(contentId, infoid_with_type);
					hmContentIdsWaitData.put(contentId, tuple.getScore());
					if(effectiveExpTuples.contains(tuple)){
						hmContentIdsForExplorePoolData.put(contentId,exploreKey);
					}else if(effectiveExploringTuples.contains(tuple)){
						hmContentIdsForExplorePoolData.put(contentId,expinfoExploringkey);
					}else{
						hmContentIdsForExplorePoolData.put(contentId,expinfoWaitingkey);
					}
				}
				
				//获取文章在-1impression中的impression count 如果达到阈值 添加到删除队列中
				if(CollectionUtils.isNotEmpty(checkImpressionArticleArr)){	
					String[] arrContentIdAllImpression = jedisCache.hmget(expinfoAllImpressionInfokey, checkImpressionArticleArr.toArray(new String[0])).toArray(new String[0]);			
					
					int i = -1;
					for (String article : checkImpressionArticleArr) {
						i++;
						String keyInfo = "";
						try {
							 keyInfo =  hmContentIdsForExplorePoolData.get(article);
							 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
							 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
							if(arrContentIdAllImpression[i] == null ){//如果解析impression count失败，则可以认为该资讯数据为无效数据，从各个池中删掉
								listNeedToDeletedContentData.add(hmContentIdsData.get(article));
								listNeedToDeletedContentId.add(article);
								logParam = new Object[] { EVENT_REMOVE_EXPLORE, keyInfo, article,bjTime,hmContentIdsData.get(article),
										arrContentIdAllImpression[i], System.currentTimeMillis() };
								expinfo_remove_logger.info("\t  "+keyInfo + ",to remove all impression is null ,"+ StringUtils.join(logParam, LOG_TAG));
							}else if( NumberUtils.toInt(arrContentIdAllImpression[i]) < threshold){
								expinfo_remove_logger.info("\t "+keyInfo+",  get article impression count->article data: "+hmContentIdsData.get(article) + ",all impression count " + arrContentIdAllImpression[i]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
								continue;
							}else{
								listNeedToDeletedContentData.add(hmContentIdsData.get(article));
								listNeedToDeletedContentId.add(article);	
								logParam = new Object[] { EVENT_REMOVE_EXPLORE, keyInfo, article,bjTime,hmContentIdsData.get(article),
										arrContentIdAllImpression[i], System.currentTimeMillis() };
								expinfo_remove_logger.info("\t  "+keyInfo + ",to remove bigger threshold article,"+ StringUtils.join(logParam, LOG_TAG));
							}
						
							
						} catch (Exception e) {
							expinfo_remove_logger.info("\t "+keyInfo + ", get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
						}
					}
				}
				
			}
		}
		
		
		// need to delete
		//将删除队列的文章  在每个产品中的等待池 探索池 进行池 impression移除、-1impression
		if(CollectionUtils.isNotEmpty(listNeedToDeletedContentData)){
			//a 将删除队列的文章 在每个产品中的等待池 探索池 进行池 impression移除
			 expinfo_remove_logger.info("\t remove  listNeedToDeletedContentData in apps");
			
			for (String strAppLanguage : arrAppLanaguage)
			{
				String[] tmp = strAppLanguage.split("=");
				if (tmp.length < 2)
					continue;
				String firmApp = tmp[0];
				String[] arrLanguages = tmp[1].split(FeederConstants.SEPARATOR_APP_LANGUAGE);
				for (String mainLanguage : arrLanguages)
				{			
					String strContentType = null;
					if (mainLanguage.indexOf("_") >= 0) {
						String[] s = mainLanguage.split("_");
						mainLanguage = s[0];
						strContentType = s[1];
					}
					String expinfoAppImpressionInfokey = FeederConstants.getExpInfoKey(firmApp, mainLanguage,"impressionOfInfoIdKey") ;
					// 其他各个产品 等待池 探索池 进行池					
					String expinfoOtherkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage) : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType);
					String expinfoOtherExploringkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"ing") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"ing");
					String expinfoOtherWaitingkey = (strContentType == null) ? FeederConstants.getExpInfoKey(firmApp, mainLanguage,"eagerly") : FeederConstants.getExpInfoKey(firmApp, mainLanguage, strContentType,"eagerly");
					String[] arrContentIdAppImpression = jedisCache.hmget(expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0])).toArray(new String[0]);
					int j = -1;					
					for (String article : listNeedToDeletedContentId) {
						j++;
						try {							
							 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
							 String bjTime = sdf.format(new Date(hmContentIdsWaitData.get(article).longValue()));   // 时间戳转换成时间
							 expinfo_remove_logger.info("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey+",  get article impression count->article data: "+hmContentIdsData.get(article) + " impression count " + arrContentIdAppImpression[j]+" scores "+hmContentIdsWaitData.get(article)+" bjtime "+bjTime);
							
						} catch (Exception e) {							
							expinfo_remove_logger.info("\t listNeedToDeletedContentData in  "+expinfoAppImpressionInfokey + ", get article impression count->article data: "+hmContentIdsData.get(article)+" article:" + article + " error -> " + e);							
						}
					}
					
					//移除各个产品的探索池 等待池 进行池信息 					
					removeBatchArticlesFromExpPool(expinfoOtherkey, listNeedToDeletedContentData.toArray(new String[0]));
					removeBatchArticlesFromExpPool(expinfoOtherExploringkey, listNeedToDeletedContentData.toArray(new String[0]));
					removeBatchArticlesFromExpPool(expinfoOtherWaitingkey, listNeedToDeletedContentData.toArray(new String[0]));
					//移除各国-1impression
					removeBatchArticlesFromImpressionInfoPool( expinfoAppImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));
				}
			}
			String expinfoAllImpressionInfokey = FeederConstants.getExpInfoKey("-1", "Spanish","impressionOfInfoIdKey") ;
			//b 将删除队列的文章 在-1impression移除
			removeBatchArticlesFromImpressionInfoPool( expinfoAllImpressionInfokey, listNeedToDeletedContentId.toArray(new String[0]));	
		}
		
		
		expinfo_remove_logger.info("\t all ,removeBiggerThresholdVaildArticles listNeedToDeletedContentId.size:"+listNeedToDeletedContentId.size()+", cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
	
		

		
	}
	
	
	
	
	

	
	@SuppressWarnings("unchecked")
	public static String process(String strLastWorkingTS, String limit_count, boolean bOversea, boolean integrityCheck, boolean monitor_editor_by_timestamp) throws ParseException
	{
		long startTime = System.nanoTime();

		loadScenarioCategoriesMappingFromRedis();

		//fallback 的时效
		listCategoryInfo = clientJDBCTemplate.listCategoryInfo(CATEGORY_SYSTEM_VERSION);
		//保存到Map中
		Map<Integer, Integer> mCategoryExpiry = new HashMap<Integer,Integer>();
		for (CategoryInfo categoryInfo : listCategoryInfo)
		{
			mCategoryExpiry.put(Integer.valueOf(categoryInfo.getId()), categoryInfo.getExpiryHour());
		}
		mContentTypeCategoryExpiry.put(String.valueOf(ContentType.DEFAULT.getValue()), mCategoryExpiry);

		if (bOversea)
		{
			//资讯的分类时效
			List<CategoryInfo> alCategoryInfo = clientJDBCTemplate.listCategoryInfoForOversea();
			for (CategoryInfo categoryInfo : alCategoryInfo)
			{
				int categoryId = categoryInfo.getId();
				String strContentType = String.valueOf( categoryInfo.getContentType() );
				mCategoryExpiry = mContentTypeCategoryExpiry.get(strContentType);
				if (mCategoryExpiry == null)
				{
					mCategoryExpiry = new HashMap<Integer, Integer>();
					mContentTypeCategoryExpiry.put(strContentType, mCategoryExpiry);
				}
				//put the maximum expiry hour in different versions
				if (mCategoryExpiry.containsKey(categoryId)) {
					int existingExpiryHour = mCategoryExpiry.get(categoryId);
					if (categoryInfo.getExpiryHour() > existingExpiryHour) {
						mCategoryExpiry.put(categoryId, categoryInfo.getExpiryHour());
					}
				} else {
					mCategoryExpiry.put(categoryId, categoryInfo.getExpiryHour());
				}
			}
			flowLogger.info("mContentTypeCategoryExpiry:" + FastJsonConverter.writeValue(mContentTypeCategoryExpiry));
		}

		long beginTime;

		Map<String, Object> mFeederData = new HashMap<String, Object>();
		Map<String, EditorTableEntry> mEditorEntry = null;
		Set<String> finePoolIds = null;
		if (is_using_kafka)
		{
			flowLogger.info("Start to fetch articles from kafka");
			ArrayList<String> alContentId = clientKafka.acquireFeederTask();
			flowLogger.info("End to fetch articles from kafka");
			flowLogger.info("\tFetched from kafka cost: " + (System.nanoTime() - startTime)/1000000 + " ms");

			Map<String, Object> mData = clientJDBCTemplate.listInfos((String[])alContentId.toArray(new String[0]), bOversea, integrityCheck);
			flowLogger.info("\tQuery MySQL cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
			List<Map<String, Object>> alOnshelfInfo = (List<Map<String, Object>>)mData.get("onshelf");
			HashSet<String> hsOffshelfIds = (HashSet<String>)mData.get("offshelf");
			removeNewsIDs(hsOffshelfIds, hsOffshelfIds, "RemovedIDs");

			mFeederData.put("detail", new InfoMapperAPI().mapIntoFeederInfoList(alOnshelfInfo));
			mFeederData.put("prediction", new InfoMapperPrediction().mapIntoFeederInfoList(alOnshelfInfo));
			mFeederData.put("elasticsearch", new InfoMapperES().mapIntoFeederInfoList(alOnshelfInfo));
			mFeederData.put("info", new InfoMapperInfo().mapIntoFeederInfoList(alOnshelfInfo));
			mFeederData.put("subscription", new InfoMapperSourceSubscription().mapIntoFeederInfoList(alOnshelfInfo));
			//mFeederData.put("hdfs", new JsonInfoMapperHDFS().mapIntoFeederInfoList(alOnshelfInfo));
			//mFeederData.put("druid", new JsonInfoMapperDruid().mapIntoFeederInfoList(alOnshelfInfo));
		}
		else
		{
			Map<String, Object> mData = clientJDBCTemplate.listInfos(strLastWorkingTS, limit_count, bOversea, integrityCheck);
            finePoolIds = clientJDBCTemplate.getHighQualityNews();
			flowLogger.info("\tQuery MySQL cost: " + (System.nanoTime() - startTime)/1000000 + " ms");
			List<Map<String, Object>> alOnshelfInfo = (List<Map<String, Object>>)mData.get("onshelf");
			HashSet<String> hsOffshelfIds = (HashSet<String>)mData.get("offshelf");
			removeNewsIDs(hsOffshelfIds, hsOffshelfIds, "RemovedIDs");
			mEditorEntry = (Map<String, EditorTableEntry>)mData.get("editor");

			mFeederData.put("detail", new InfoMapperAPI().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("prediction", new InfoMapperPrediction().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("chapter", new InfoMapperChapter().mapIntoFeederInfoList(alOnshelfInfo, strLastWorkingTS));
			mFeederData.put("elasticsearch", new InfoMapperES().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("info", new InfoMapperInfo().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("hdfs", new JsonInfoMapperHDFS().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("druid", new JsonInfoMapperDruid().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("subscription", new InfoMapperSourceSubscription().mapIntoFeederInfoList(alOnshelfInfo, mEditorEntry));
			mFeederData.put("biz",new InfoMapperBiz().mapIntoFeederInfoList(alOnshelfInfo));

		}

		flowLogger.info("");
		Set<String> hsModifiedGroup = clientJDBCTemplate.getModifiedGroupID();

		if (MapUtils.isNotEmpty(mFeederData))
		{
			Set<String> hsProcessedContentIdList = enumerateContentIdList((List<FeederInfo>)mFeederData.get("detail"));
			HashSet<String> hsExpInfoBlackList = (bEnableContentGroup) ? doFilterByGroup((List<FeederInfo>)mFeederData.get("detail")) : new HashSet<String>();
			flowLogger.info("\thsExpInfoBlackList=" + hsExpInfoBlackList);

			if (bWriteRedis) {
				flowLogger.info("\tBegin save API into Redis...");
				beginTime = System.nanoTime();
				//still save details even there's someone in the same group.
				doStoreIntoRedisHash((List<FeederInfo>)mFeederData.get("detail"), FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_DETAIL, null, finePoolIds);
				notifyCleanCache((List<FeederInfo>)mFeederData.get("detail"), mEditorEntry);
				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");

				flowLogger.info("\tBegin save Prediction into Redis...");
				beginTime = System.nanoTime();
				doStoreIntoRedisHash((List<FeederInfo>)mFeederData.get("prediction"), FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_FEATURE, hsExpInfoBlackList, finePoolIds);
				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");

				flowLogger.info("\tBegin save Chapters into Redis...");
				beginTime = System.nanoTime();
				doStoreIntoRedisHashChapter((List<FeederInfo>)mFeederData.get("chapter"), FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_CHAPTER);
				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");

				flowLogger.info("\tBegin save BIZ into Redis...");
				beginTime = System.nanoTime();
				doStoreIntoRedisHashBiz((List<InteractionInfo>)mFeederData.get("biz"), FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_BIZ);
				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");

				flowLogger.info("\tBegin save HW into Redis...");
				beginTime = System.nanoTime();
				doStoreIntoRedisHashHw((List<FeederInfo>)mFeederData.get("detail"), HW_NEW_INFO_LSIT_KEY);
				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");
			}

			//Save into ES
			if (bWriteES && mFeederData.containsKey("elasticsearch"))
			{
				flowLogger.info("\tBegin save into ES...");
				beginTime = System.nanoTime();

				doStoreIntoES((List<FeederInfo>)mFeederData.get("elasticsearch"));

				flowLogger.info("\tEnd save into ES. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");
			}

			//Handle modified articles from t_editor
			flowLogger.info("\tBegin update from t_editor...");
			beginTime = System.nanoTime();

			Set<String> editorBlack = new HashSet<String>();
			List<FeederInfo> alFeederInfo = (List<FeederInfo>)mFeederData.get("detail");
			for (FeederInfo feederInfo : alFeederInfo)
			{
				String contentId = feederInfo.getContent_id();
				String adult_score = feederInfo.getAdult_score();
				try
				{
					if (StringUtils.isNotEmpty(adult_score))
					{
						HashMap<String, String> adultScoreJsonMap = JSON.parseObject(adult_score, HashMap.class);
						flowLogger.info("adult_score_version=" + ADULT_SCORE_VERSION + "\tadultScoreJsonMap=" + adultScoreJsonMap + "\tdata=" + adultScoreJsonMap.get(ADULT_SCORE_VERSION));
						Double finalAdultScore = Double.parseDouble((adultScoreJsonMap.get(ADULT_SCORE_VERSION)));
						flowLogger.info("finalAdultScore=" + finalAdultScore + "\tadult_score_value=" + ADULT_SCORE_VALUE);
						if (Double.valueOf(finalAdultScore).doubleValue() >= Double.valueOf(ADULT_SCORE_VALUE).doubleValue())
						{
							editorBlack.add(feederInfo.getContent_id());
						}
					}
				}
				catch (Exception e)
				{
					flowLogger.info("\t\tparse adult score failed content_id=" + contentId);
				}
			}

			flowLogger.info("\tEnd update from t_editor. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
			flowLogger.info("");

			//Save old Info with FeederInfo
			if (bWriteRedis && mFeederData.containsKey("info"))
			{
				flowLogger.info("\tBegin save INFO into Redis...");
				beginTime = System.nanoTime();

				doStoreIntoRedisHashInfo((List<Info>)mFeederData.get("info"), FeederConstants.REDIS_KEY_PREFIX_NEWS, FeederConstants.REDIS_FIELD_NAME_ARTICLE_INFO, strLastWorkingTS, editorBlack, hsExpInfoBlackList, finePoolIds);

				flowLogger.info("\tEnd save into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");
			}
			if (bOnline)
			{
				//Save into HDFS for Druid
				if (bWriteDruid && mFeederData.containsKey("druid"))
				{
					flowLogger.info("\tBegin save data under toDruid/...");
					beginTime = System.nanoTime();

					doStoreIntoFileForDruid((List<Map<String, String>>)mFeederData.get("druid"));

					flowLogger.info("\tEnd save. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
					flowLogger.info("");
				}
				//Save into HDFS
				if (bWriteHDFS && mFeederData.containsKey("hdfs"))
				{
					flowLogger.info("\tBegin save data under toHDFS/...");
					beginTime = System.nanoTime();

					doStoreIntoFileForHDFS((List<Map<String, String>>)mFeederData.get("hdfs"));

					flowLogger.info("\tEnd save. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
					flowLogger.info("");
				}
			}

			//Save Source subscription id into redis
			if (mFeederData.containsKey("subscription"))
			{
				flowLogger.info("\tBegin save Source subscription id into Redis...");
				beginTime = System.nanoTime();

				List<SourceSubscriptionEntry> sourceSubscriptionInfos = clientJDBCTemplate.getSourceSubscriptionInfos();

				doStoreIntoFileForSourceSubscription((List<FeederInfo>)mFeederData.get("subscription"), sourceSubscriptionInfos);

				flowLogger.info("\tEnd save. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
				flowLogger.info("");
			}
		}
		else
		{
			flowLogger.info("\tNo new data to process...");
			flowLogger.info("");
		}

		if (bWriteRedis) {
			flowLogger.info("\tBegin save update group detail into Redis...");
			beginTime = System.nanoTime();
			//still save details even there's someone in the same group.
			doUpdateGroupDetail(hsModifiedGroup, finePoolIds);
			flowLogger.info("\tEnd save update group detail into Redis. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
			flowLogger.info("");
		}

		if (is_using_kafka)
		{
			int nUpdateCount = 0;
			String strLastUpdateTime = null;
			if (MapUtils.isNotEmpty(mFeederData))
			{
				nUpdateCount = ((List<FeederInfo>)mFeederData.get("detail")).size();
				strLastUpdateTime = ((List<Info>)mFeederData.get("info")).get(nUpdateCount-1).feederInfo.getUpdate_time();
			}
			flowLogger.info("Update Count: "+nUpdateCount);
			flowLogger.info("Next timestamp: "+strLastUpdateTime);
			flowLogger.info("");
			return strLastUpdateTime;
		}
		else
		{
			flowLogger.info("Update Count: "+clientJDBCTemplate.getLastUpdateCount());
			flowLogger.info("Next timestamp: "+clientJDBCTemplate.getLastUpdateTime());
			flowLogger.info("");
			long delay = System.currentTimeMillis() - DateUtils.stringToTimestampMillis(clientJDBCTemplate.getLastUpdateTime());
			monitorLogger.info(SystemUtils.getIPAddress(true)+"&&feeder.status.process-time-delay&&"+ delay +"&&0&&AVG&&60&&");
			return clientJDBCTemplate.getLastUpdateTime();
		}
		
	
	}

	//feeder 程序入口
	public static void main(String[] args)
	{
		Runnable r = new Runnable() {
			public void run() {
				boolean flag = true;
				while (flag)
				{
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
					long current_datetime = new Date().getTime();
					flowLogger.info("Processing...");
					flowLogger.info("Current Date and Time in CST time zone: " + fmt.format(current_datetime));

					String cleanup_expired_record_time = null;
					String record_expired_days = "1";
					String run_period_secs = "10";
					String strLastWorkingTS = null;
					String limit_count = "1000";
					Boolean integrityCheck = true;
					Boolean monitor_editor_by_timestamp = true;
					try
					{
						Properties props = loadProperties(PROPERTIES_FILE_TIMESTAMP);
						cleanup_expired_record_time = props.getProperty("cleanup_expired_record_time");
						record_expired_days = props.getProperty("record_expired_days");
						run_period_secs = props.getProperty("run_period_secs");
						strLastWorkingTS = props.getProperty("timestamp");
						limit_count = props.getProperty("fetch_limit_count");
						integrityCheck = Boolean.valueOf(props.getProperty("integrity_check").toString());
						monitor_editor_by_timestamp = Boolean.valueOf(props.getProperty("monitor_editor_by_timestamp").toString());
						flowLogger.info("Working timestamp: " + strLastWorkingTS);
					}
					catch (Exception e)
					{
						flowLogger.fatal("[main]", e);
					}

					Date d = null;
					try
					{
						d = fmt.parse(cleanup_expired_record_time);						
						if (d != null)
						{
							long cleanup_datetime = d.getTime();
							flowLogger.info("cleanup_expired_record_time.getTime()= " + cleanup_datetime+",current_datetime="+current_datetime);
							if (current_datetime > cleanup_datetime)
							{
								double expired_date = (double)(current_datetime - (86400000*Long.valueOf(record_expired_days)));
								flowLogger.info("remove articles before " + fmt.format(expired_date));
								Set<String> setRemovableExpiredArticleId = new HashSet<String>();
								String[] arrAllArticleInfoIdsKey = FeederConstants.getAllArticleInfoIdsKeyArray();
								for (String strAllArticleInfoIdsKey : arrAllArticleInfoIdsKey) {
									setRemovableExpiredArticleId.addAll( jedisDetail.zrangeByScore(strAllArticleInfoIdsKey, 0, expired_date) );
								}
								HashSet<String> setRemovableFallbackArticleId = new HashSet<String>();
								String[] allFallbackArticleInfoIdsKey = FeederConstants.getFallbackArticleInfoIdsKeyArray();
								for (String strFallbackArticleInfoIdsKey : allFallbackArticleInfoIdsKey) {
									setRemovableFallbackArticleId.addAll( jedisDetail.zrangeByScore(strFallbackArticleInfoIdsKey, 0, expired_date) );
								}
								final Set<String> finalSetRemovableExpiredArticleId  = setRemovableExpiredArticleId;
								final Set<String> finalSetRemovableFallbackArticleId = setRemovableFallbackArticleId;
								if (CollectionUtils.isNotEmpty(setRemovableExpiredArticleId) || CollectionUtils.isNotEmpty(setRemovableFallbackArticleId)) {
									new Thread(new Runnable() {
										public void run() {
											removeNewsIDs(finalSetRemovableExpiredArticleId, finalSetRemovableFallbackArticleId, "ExpiredIDs");
										}
									}).start();
								}
						
							
							

								if (bWatchADTable)
								{
									flowLogger.info("Begin watch t_dsp_ad and save into local disk...");
									long beginTime = System.nanoTime();

									new ScheduledTaskWatchADTable().process();

									flowLogger.info("End save. (cost: " + (System.nanoTime() - beginTime)/1000000 + " ms)");
									flowLogger.info("");
								}

								cleanup_expired_record_time = fmt.format(cleanup_datetime+86400000); //next day
							}
						}
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						flowLogger.fatal("[main]", e);
					}

					String lastUpdateTime = strLastWorkingTS;
					try
					{
						lastUpdateTime = process(strLastWorkingTS, limit_count, bOversea, integrityCheck, monitor_editor_by_timestamp);
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block						
						flowLogger.fatal("[process]", e);
					}

					flowLogger.info("\t\t[hwInfoUpdateState] start");
					Set<String> contentIds = jedisDetail.smembers(HW_BLOCK_CHECK_KEY);
					for (String contentId:contentIds){
						try {
							flowLogger.info("\t\t[hwInfoUpdateState] contentId="+contentId);
							FeederInfo feederInfo = new FeederInfo();
							String key = FeederConstants.getRedisKey(FeederConstants.REDIS_KEY_PREFIX_NEWS, contentId);
								byte[] infoByte = jedisDetail.hget(key.getBytes(),FeederConstants.REDIS_FIELD_NAME_ARTICLE_DETAIL.getBytes());
							flowLogger.info("\t\t[hwInfoUpdateState] infoByte="+infoByte);
							TDeserializer deserializer = new TDeserializer();
							deserializer.deserialize(feederInfo, infoByte);
							HashSet<String> firmApp = listFirmApp(feederInfo);
							flowLogger.info("\t\t[hwInfoUpdateState] fromApp"+JSON.toJSON(firmApp));
							if(!firmApp.contains(HW_FROM_APP)) {
								//通知华为下架状态
								Boolean bool = removeInfoToHW(feederInfo.getContent_id());
								if(bool){
									jedisDetail.srem(HW_BLOCK_CHECK_KEY, feederInfo.getContent_id());
									flowLogger.info("\t\t[hwInfoUpdateState] del key=" + HW_BLOCK_CHECK_KEY + ":ContentId=" + feederInfo.getContent_id());
								}else{
									flowLogger.info("\t\t[hwInfoUpdateState] error " + ":ContentId=" + feederInfo.getContent_id());
								}
							}
						}catch (Exception e) {
							flowLogger.error("[hwInfoUpdateState]", e);
						}
					}


					if (lastUpdateTime != null)
					{
						Properties props = new Properties();
						props.setProperty("timestamp", lastUpdateTime);
						props.setProperty("record_expired_days", record_expired_days);
						props.setProperty("run_period_secs", run_period_secs);
						props.setProperty("cleanup_expired_record_time", cleanup_expired_record_time);
						props.setProperty("fetch_limit_count", limit_count);
						props.setProperty("integrity_check", integrityCheck.toString());
						props.setProperty("monitor_editor_by_timestamp", monitor_editor_by_timestamp.toString());
						storeProperties(props, PROPERTIES_FILE_TIMESTAMP, "timestamp for feeder access");
					}
					
				
					//离线处理 等待池	 进行池 探索池 无效文章			
					new Thread(new Runnable() {
						public void run() {			
							expinfo_remove_logger.info("Begin  removeExploreWaitingPoolInvalidArticles...");							
							removeExploreWaitingPoolInvalidArticles();
							expinfo_remove_logger.info("End removeExploreWaitingPoolInvalidArticles...");
							
							expinfo_remove_logger.info("Begin  removeBiggerThresholdVaildArticles...");
							removeBiggerThresholdVaildArticles();
							expinfo_remove_logger.info("End removeBiggerThresholdVaildArticles...");

						}
					}).start();
						

					try
					{
						Thread.sleep(Integer.valueOf(run_period_secs)*1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		};

		//初始化rateMap和rateCoefficientMap
		loadConfigFiles();

		String strRedisDetailIPWithPort = null;
		String strRedisCacheIPWithPort  = null;
		String strElasticsearchIPWithPort = null;
		String es_ip = "192.168.1.220";
		int es_port = 9300;
		String es_name = "my-application_zhizi";
		strLocalFilePath = "/data/feeder/";
		try
		{
			Properties props = loadProperties(PROPERTIES_FILE_CONNECTION);

			bOnline = Boolean.valueOf(props.getProperty("is-online"));
			if (bOnline)
			{
				strRedisDetailIPWithPort = props.getProperty("online-redis-ip_port");
				strRedisCacheIPWithPort  = props.getProperty("online-redis-cache-ip_port");
				strElasticsearchIPWithPort = props.getProperty("online-es-ip_port");
				es_ip   = props.getProperty("online-es-ip");
				es_port = Integer.valueOf(props.getProperty("online-es-port"));
				es_name = props.getProperty("online-es-name");
				bWriteRedis = Boolean.valueOf(props.getProperty("is-write-to-redis"));
				bWriteES    = Boolean.valueOf(props.getProperty("is-write-to-es"));
				bWriteDruid = Boolean.valueOf(props.getProperty("is-write-to-druid"));
				bWriteHDFS  = Boolean.valueOf(props.getProperty("is-write-to-hdfs"));
				strLocalFilePath = props.getProperty("online-localfile-path");
			}
			else
			{
				strRedisDetailIPWithPort = props.getProperty("local-redis-ip_port");
				strElasticsearchIPWithPort = props.getProperty("local-es-ip_port");
				es_ip   = props.getProperty("local-es-ip");
				es_port = Integer.valueOf(props.getProperty("local-es-port"));
				es_name = props.getProperty("local-es-name");
			}

			bWatchADTable = Boolean.valueOf(props.getProperty("is-watch-online-ad-table"));

			bOversea = Boolean.valueOf(props.getProperty("is-oversea"));
			bEnableContentGroup = Boolean.valueOf(props.getProperty("enable-content-group"));
			bEnableFetchActiveItem = Boolean.valueOf(props.getProperty("enable-fetch-active-item"));

			is_using_kafka = Boolean.valueOf(props.getProperty("is-using-kafka"));
			strKafkaBootstrapServers = props.getProperty("kafka-servers");
			strKafkaGroupID = props.getProperty("kafka-group-id");
			strKafkaTopic = props.getProperty("kafka-topic");
		}
		catch (Exception ex)
		{
		}
		flowLogger.info("Working Redis: " + strRedisDetailIPWithPort + "\tES: " + strElasticsearchIPWithPort);

		jedisDetail = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisDetailIPWithPort.split(";"));
		if (StringUtils.isNotEmpty(strRedisCacheIPWithPort))
			jedisCache = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisCacheIPWithPort.split(";"));
		else
			jedisCache = jedisDetail;

		Settings settings = Settings.builder().put("cluster.name", es_name).put("client.transport.sniff", true).build();
		TransportClient transportClient = new PreBuiltTransportClient(settings);
		for (String es_ip_port : strElasticsearchIPWithPort.split(";")) {
			String[] s = es_ip_port.split(":");
			if (s.length == 2)
			{
				esClient = transportClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(s[0], Integer.parseInt(s[1]))));
			}
		}

		ApplicationContext context = new FileSystemXmlApplicationContext("beans-config.xml");
		clientJDBCTemplate = ClientJDBCTemplate.getInstance();
		clientJDBCTemplateComment = ClientJDBCTemplateComment.getInstance();
		if (bOversea)
		{
			ContentCategoryExpiryInfoDaemon.getInstance().setRedisClient(jedisDetail);
			ContentCategoryExpiryInfoDaemon.getInstance().start();
		}

		if (is_using_kafka && strKafkaBootstrapServers.isEmpty() == false && strKafkaGroupID.isEmpty() == false && strKafkaTopic.isEmpty() == false)
		{
			clientKafka = new ClientKafkaConsumer(strKafkaBootstrapServers, strKafkaGroupID, strKafkaTopic);
		}

		flowLogger.info("Connection to server sucessfully");

		Thread t = new Thread(r);
		// Lets run Thread in background..
		// Sometimes you need to run thread in background for your Timer application..
		t.start(); // starts thread in background..
		// t.run(); // is going to execute the code in the thread's run method on the current thread..
		flowLogger.info("Thread started...\n");

		((ConfigurableApplicationContext)context).close();
	}

	public static int getCategorySystemVersion() {
		return CATEGORY_SYSTEM_VERSION;
	}

	public static String getStrLocalFilePath() {
		return strLocalFilePath;
	}

	public static void setStrLocalFilePath(String strLocalFilePath) {
		Feeder.strLocalFilePath = strLocalFilePath;
	}

	public static String getMixtureSource(){
		return loadProperties(PROPERTIES_FILE_CONFIG).getProperty("mixture_source");
	}


	public static boolean removeInfoToHW(String contentId){
		Boolean bool = true;
		Random rand = new Random();
		int nonce = rand.nextInt(1000000000);
		long timestamp = new Date().getTime()/1000L;
		String data = String.valueOf(String.valueOf(nonce)+String.valueOf(timestamp));
		String secretKey = HMACSHA256(data.getBytes(),HW_SECRETKEY.getBytes()).toLowerCase();
		String url = HW_URI+"?version=v1.0&timestamp="+timestamp+"&nonce="+nonce+"&secretkey="+secretKey+"&organization="+HW_ORGANIZATION;
		HwRequestBean hwRequestBean = new HwRequestBean();
		List<HwIdBean> blockList = new ArrayList<>(1);
		HwIdBean hwIdBean = new HwIdBean();
		hwIdBean.setDocId(contentId);
		blockList.add(hwIdBean);
		hwRequestBean.setBlockList(blockList);

		HttpClient httpClient = null;
		HttpPost httpPost = null;
		BufferedReader in = null ;
		try{
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", "application/json");
			StringEntity se = new StringEntity(JSON.toJSONString(hwRequestBean));
			se.setContentType("text/json");
			se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
			httpPost.setEntity(se);
			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
				if(response.getStatusLine().getStatusCode() == 200) {
					InputStream content = response.getEntity().getContent() ;
					in = new BufferedReader(new InputStreamReader(content));
					StringBuilder sb = new StringBuilder();
					String line = "" ;
					while ((line = in.readLine()) != null) {
						sb.append(line);
					}
					if(StringUtils.isNotEmpty(sb.toString())){
						bool = false;
					}else{
						flowLogger.info("\t\t[hwInfoUpdateState] msg= " + sb.toString());
					}
				}else{
					bool = false;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			bool = false;
			return bool;
		}
		return bool;
	}

	public static String HMACSHA256(byte[] data, byte[] key)
	{
		try  {
			SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			return byte2hex(mac.doFinal(data));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String byte2hex(byte[] b)
	{
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b!=null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	public static int hammingDistance(BigInteger simhash1, BigInteger simhash2, int simHashDistance) {

		BigInteger x = simhash1.xor(simhash2);
		int tot = 0;

		// 统计x中二进制位数为1的个数
		// 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，对吧，然后，n&(n-1)就相当于把后面的数字清0，
		// 我们看n能做多少次这样的操作就OK了。

		while (x.signum() != 0) {
			if (++tot > simHashDistance) {
				break;
			}
			x = x.and(x.subtract(new BigInteger("1")));
		}
		return tot;
	}


	public static List<String> getHammingDistance(Map<String, String> simHashMap){
		Set<String> contentIds = new HashSet<>();
		Map<String,String> notNullSimHashMap = new HashMap<>(simHashMap.size());
		HashMap<String,String> sameContentIdMap = new HashMap<>();
		for (Map.Entry<String, String> entry : simHashMap.entrySet()) {
			//String simhashstr = entry.getValue();
			String simhashstr = JSON.parseObject(entry.getValue()).getString("v6");
			if(StringUtils.isEmpty(simhashstr)){
				contentIds.add(entry.getKey());
			}else{
				notNullSimHashMap.put(entry.getKey(),entry.getValue());
			}
		}
		for (Map.Entry<String, String> entry : notNullSimHashMap.entrySet()) {
			if(isHammingRedo(sameContentIdMap,entry.getValue())){
				sameContentIdMap.put(entry.getKey(),entry.getValue());
			}
		}
		contentIds.addAll(sameContentIdMap.keySet());
		List<String> result = new ArrayList<>(contentIds);
		return result;
	}

	private static boolean isHammingRedo(HashMap<String,String> sameContentIdMap,String simhashstr){
		if(sameContentIdMap.size()<=0){
			return true;
		}
		String simhashstrs = JSON.parseObject(simhashstr).getString("v6");
		//String simhashstrs = simhashstr;
		BigInteger simhash = new BigInteger(simhashstrs.substring(2), 16);
		for (Map.Entry<String, String> entry : sameContentIdMap.entrySet()) {
			//String simhashNewstr = entry.getValue();
			String simhashNewstr = JSON.parseObject(entry.getValue()).getString("v6");
			BigInteger simhashNew = new BigInteger(simhashNewstr.substring(2), 16);
			if (hammingDistance(simhash, simhashNew, 28) < 28) {
				return  false;
			}
		}
		return true;
	}


}



