package com.inveno.feeder.constant;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.inveno.feeder.thrift.FeederInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import static com.inveno.feeder.Feeder.isComicInfo;
import static com.inveno.feeder.Feeder.isGif;
import static com.inveno.feeder.Feeder.isMemesInfo;
import static com.inveno.feeder.Feeder.isVideoInfo;

public class FeederConstants
{
	//分隔符	 
	public static final String REDIS_KEY_PREFIX_ADULT_SCORE     = "adult";
	public static final String REDIS_KEY_PREFIX_NEWS            = "news";//资讯
	public static final String REDIS_KEY_PREFIX_CHAPTERLIST     = "chapterlist";//漫画章节
	public static final String REDIS_KEY_PREFIX_CHAPTERDETAIL   = "chapterdetail";//漫画详情
	public static final String REDIS_KEY_PREFIX_EXPINFO         = "expinfo";//探索池
	public static final String REDIS_KEY_PREFIX_SUBSCRIPTION    = "rss";//订阅源
	public static final String REDIS_KEY_PREFIX_GROUP_DETAIL    = "FEEDER:ITEM2GROUP";//资讯group更新
	public static final String REDIS_KEY_SUFFIX_PRIMARY_UPDATE  = "::updated::withinhalfhour"; //（和初选模块的交互，资讯详情有更新时会写这个 key 让初选知道有哪些资讯应该要重新获取）

	public static final String ADULT_UNKNOWN                    = "unknown";

	public static final String LANGUAGE_MULTILINGUAL            = "Multilingual";

	//GMP分类器模型gmpPredictClassifierModel
	public static final String GMP_CLASSIFIER_MODEL             = "gmpPredictClassifierModel"; 

	public static final String SEPARATOR_APP_LANGUAGE           = ":";
	public static final String SEPARATOR_REDIS_KEY              = "_";
	public static final String SEPARATOR_REDIS_MEMBER           = "#";
	public static final String SEPARATOR_REDIS_FIELD            = ":";
	public static final String SEPARATOR_REDIS_VALUE            = null;
	public static final String SEPARATOR_LOG_TAG                = "&";

	public static final String REDIS_KEY_ARTICLE_GMP            = "article-gmp-hash-key";
	public static final String REDIS_KEY_ALL_ARTICLE_ID         = "article-ids-key";//全量资讯池
	public static final String REDIS_KEY_FALLBACK_ARTICLE_ID    = "article-fallback-ids-key";//fallback资讯池
	public static final String REDIS_FIELD_NAME_ARTICLE_DETAIL  = "API";//资讯详情
	public static final String REDIS_FIELD_NAME_ARTICLE_FEATURE = "Prediction";//资讯特徵信息（初选，规则模块使用）
	public static final String REDIS_FIELD_NAME_ARTICLE_INFO    = "INFO";//向下兼容国内旧版本的智子系统(已废弃)
	public static final String REDIS_FIELD_NAME_ARTICLE_CHAPTER = "Chapter";//向下兼容国内旧版本的智子系统(漫画已废弃)
	public static final String REDIS_FIELD_NAME_ARTICLE_BIZ = "BIZ";//文章评论数和点击数

	public static final String LOG_EVENT_NEW_PUBLISH_ARTICLE    = "new-publish-article";
	public static final String LOG_EVENT_FIRST_INTO_EXPINFO     = "first-into-expinfo";
	public static final String LOG_EVENT_REDUNDANT_INTO_EXPINFO = "redundant-into-expinfo";
	public static final String LOG_EVENT_OFFSHELF_ARTICLE       = "offshelf-article";
	public static final String LOG_EVENT_OUT_OF_DATE_EXPINFO    = "out-of-date-expinfo";

	public static final String INIT_VALUE_OF_IMPRESSION         = "-1";
	public static final String DATE_TIMESTAMP_FORMAT            = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String  REDIS_KEY_ARTICLE_GMP_SPARK = "article-gmp-spark-streaming-hash-key";

	public static final int LINK_TYPE = 32768;

	public static String getLogMessage(Object... args)
	{
		return SEPARATOR_LOG_TAG + StringUtils.join(args, SEPARATOR_LOG_TAG);
	}
	public static String getRedisKey(String... args)
	{
		String[] values = new String[]{};
		if (ArrayUtils.isNotEmpty(args))
		{
			for (int i = 0; i < args.length; i++)
				values = (String[])ArrayUtils.add(values, args[i]);
			return StringUtils.join(values, SEPARATOR_REDIS_KEY);
		}
		return null;
	}
	public static String getAdultScoreKey(String... args)	
	{
		String[] values = new String[]{};
		if (ArrayUtils.isNotEmpty(args))
		{
			values = (String[])ArrayUtils.add(values, REDIS_KEY_PREFIX_ADULT_SCORE);
			for (int i = 0; i < args.length; i++)
				values = (String[])ArrayUtils.add(values, args[i]);
			return StringUtils.join(values, SEPARATOR_REDIS_KEY);
		}
		return null;
	}
	public static String getArticleKey(String contentId)
	{
		if (StringUtils.isNotEmpty(contentId))
			return getRedisKey(REDIS_KEY_PREFIX_NEWS, contentId);
		else
			return null;
	}
	public static String[] getFallbackArticleInfoIdsKeyArray() {
		ArrayList<String> alKeys = new ArrayList<String>();
		for (int i = 0; i < 100; i++)
			alKeys.add( REDIS_KEY_FALLBACK_ARTICLE_ID + "-" + i );
		return (String[])alKeys.toArray(new String[0]);
	}
	public static String getFallbackArticleInfoIdsKey(String contentId) {
		int remainder = (Math.abs(contentId.hashCode()) % 100);
		return REDIS_KEY_FALLBACK_ARTICLE_ID + "-" + remainder;
	}
	public static String[] getAllArticleInfoIdsKeyArray() {
		ArrayList<String> alKeys = new ArrayList<String>();
		for (int i = 0; i < 100; i++)
			alKeys.add( REDIS_KEY_ALL_ARTICLE_ID + "-" + i );
		return (String[])alKeys.toArray(new String[0]);
	}
	public static String getAllArticleInfoIdsKey(String contentId) {
		int remainder = (Math.abs(contentId.hashCode()) % 100);
		return REDIS_KEY_ALL_ARTICLE_ID + "-" + remainder;
	}
	public static String[] getAllArticleInfoIdsCtrKeyArray() {
		ArrayList<String> alKeys = new ArrayList<String>();
		for (int i = 0; i < 100; i++)
			alKeys.add( REDIS_KEY_ARTICLE_GMP_SPARK + "-" + i );
		return (String[])alKeys.toArray(new String[0]);
	}
	public static String getAllArticleInfoIdsCtrKey(String contentId) {
		int remainder = (Math.abs(contentId.hashCode()) % 100);
		return REDIS_KEY_ARTICLE_GMP_SPARK + "-" + remainder;
	}
	public static String getArticleInfoIdsKey(String prefix, String contentId) {
		int remainder = (Math.abs(contentId.hashCode()) % 100);
		return prefix + "-" + remainder;
	}
	public static Map<String, String> getExpInfoKey(String app, String language, boolean bVideo, boolean bMEME, boolean bComic, boolean bLockscreen, boolean bGif)
	{
		String prefix    = REDIS_KEY_PREFIX_EXPINFO;
		String separator = SEPARATOR_REDIS_KEY;

		StringBuffer sb = new StringBuffer();
 		sb.append(prefix).append(separator).append(app);
		sb.append(separator).append(language);
		
		StringBuffer allsb = new StringBuffer();
		allsb.append(prefix).append(separator).append("-1").append(separator).append(language);
 		if (bLockscreen) { 			
 			sb.append(separator).append("lockscreen");
 		}
 		String key_base = sb.toString();
 		if (bVideo) {
 			sb.append(separator).append("video");
 		} else if (bMEME) {
 			sb.append(separator).append("meme");
 		} else if (bComic) {
 			sb.append(separator).append("comic");
 		}else if(bGif){
			sb.append(separator).append("gif");
		}
 		String expinfo_key = sb.toString();

		String eagerly_key = expinfo_key + separator + "eagerly";
		String flowing_key = expinfo_key + separator + "ing";
		String impressionOfInfoIdKey = key_base + separator + "impressionOfInfoIdKey";
		
		String impressionOfInfoIdKeyAll = allsb.toString()  + separator + "impressionOfInfoIdKey";
		if (bLockscreen) {
			impressionOfInfoIdKeyAll = impressionOfInfoIdKey;
		}

		Map<String, String> mResult = new HashMap<String, String>();
		mResult.put("origin", expinfo_key);
		mResult.put("waiting", eagerly_key);
		mResult.put("exploring", flowing_key);
		mResult.put("impression", impressionOfInfoIdKey);
		mResult.put("-1impression", impressionOfInfoIdKeyAll);
		return mResult;
	}

	public static String getExpInfoKey(String productId, String language, Object... args)
	{
		Object[] values = new Object[]{};
		values = ArrayUtils.add(values, REDIS_KEY_PREFIX_EXPINFO);
		values = ArrayUtils.add(values, productId);
		values = ArrayUtils.add(values, language);
		if (ArrayUtils.isNotEmpty(args))
		{
			for (int i = 0; i < args.length; i++)
				values = ArrayUtils.add(values, args[i]);
		}
		return StringUtils.join(values, SEPARATOR_REDIS_KEY);
	}
	public static String getPrimaryUpdateKey(String articleId)
	{
		return articleId + REDIS_KEY_SUFFIX_PRIMARY_UPDATE;
	}
	public static String getSourceSubscriptionKey(String sourceSubscriptionId)
	{
		return getRedisKey(REDIS_KEY_PREFIX_SUBSCRIPTION, sourceSubscriptionId);
	}
	public static String getGroupDetailKey(String groupId)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(REDIS_KEY_PREFIX_GROUP_DETAIL);
		sb.append(":");
		sb.append(groupId);
		return sb.toString();
	}
}