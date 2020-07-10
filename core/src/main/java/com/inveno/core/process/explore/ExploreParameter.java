package com.inveno.core.process.explore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

public class ExploreParameter {
	private static final Logger logger = Logger.getLogger(ExploreParameter.class);

	public static final String EXPLORE_TYPE_NEWS  = "news";
	public static final String EXPLORE_TYPE_VIDEO = "video";
	public static final String EXPLORE_TYPE_MEME  = "meme";
	public static final String EXPLORE_TYPE_COMIC = "comic";
	public static final String EXPLORE_TYPE_GIF = "gif";
	public static final String EXPLORE_TYPE_LOCKSCREEN_NEWS = "lockscreen";
	
	public static final String EXPLORE_STRATEGY_REQUEST_BASED = "request_based";
	public static final String EXPLORE_STRATEGY_IMPRESSION_BASED = "impression_based";

	private String uid;
	private String app;
	private String language;
	private String scenario;

	private String type;
	private String strategy;
	private Map<String, String> mExpInfoKey;

	private boolean bFilterByTypes = false;
	//探索下发失败次数，超过时将此资讯从探索池删除
	private int nFilterTypeFailCountToRemove;

	//第 1 轮下发投放倍数
	private double multipleOfFirstRound;
	//第 n 轮下发投放倍数
	private double multipleOfOtherRound;
	//完成探索所需要的下发或展示次数
	private int threshold;
	//进行池的随机选取头部范围
	private int effectiveRange;
	//探索概率
	private double probability;
	//进行池安全水位 默认是3*effectiveRange
	private int safeRange;
	//填补一个探索位最大可以重试的次数 默认是5
	private int maxRetryCount;
	//补充进行池，优先从等待池出资讯的比例， 默认0.5
	private float waitingSupplementExploringRatio;
	// 为了防止资讯不断的进入进行池，不断的下发，设置资讯在等待池中最小的等待时间
	private int minWaitingTimeSecond;
	// 在等待池中最大的等待时间,如果超时了则丢掉这条数据，默认是48h
	private int maxWaitingTimeSecond;
	// 最大的可发布时间 second
	//private long availablePublishTimeSecond;
	// 单次在等待池中等待的最大反馈时间， 默认十分钟
	private int waitingFeedbackTimeSecond;
	// 等待池补充进行池的时候，等待池／等待池应该补充的资讯 的 最小的优化比例，默认2.0
	private float waitingIntoExploringOptimizeMinRatio;

	
	/***
	 * 2018-7-11 16:32:31
	 *  等待池进入等待池 处理策略
	 * 补充进行池策略：等待池大小=S ，等待池的量进入进行池 的比值=R， 等待池文章量/(探索池文章量 + 等待池文章量)=r
	 * S=[2000,+] --> R=0.95 
	 * S=[100,2000] --> R=[0.6,r] 
	 * S=[0,100] --> R=[0.1,r]
	 */
	//补充进行池，等待池的量进入进行池 最小阈值是100 用来改变等待池进入进行池比例的临界点
	private int waitingSupplementExploringMinRange ;
	// 等待池补充进行池的时候，等待池数量 如果大于这个值的话 优先从等待池出资讯的比例变为默认最大值，默认2000
	private int waitingSupplementExploringThreshold ;
	//补充进行池，等待池的量大于的阈值2000时，最大资讯的比例， 默认0.95
	private float waitingSupplementExploringMaxRatio;
	
	//补充进行池，等待池的量小于的阈值2000，大于100的在这范围内时 最小资讯的比例， 默认0.6
	private float waitingSupplementExploringInRangeMinRatio;
	//补充进行池，等待池的量小于100范围时最小资讯的比例， 默认0.1
	private float waitingSupplementExploringLessRangeMinRatio;	
	// 探索池进入进行池的衰减系数 ，默认0.95
	private float exploreSupplementExploringAttenuationRatio;
	


	private ArticleFilterImpl articleFilterImpl;
	private ArticleProperty articleProperty;
	
	private int offset;
	
	public ExploreParameter(Context context, String _type, String _strategy) {
		uid = context.getUid();
		app = context.getApp();
		language = context.getLanguage();
		type = _type;
		strategy = _strategy;
		scenario = "0x" + StringUtils.leftPad(Long.toHexString(context.getScenario()), 6, "0");
		articleFilterImpl = context.getArticleFilter();
		articleProperty = context.getArticleProperty();

		mExpInfoKey = getExpInfoKey(app, language);

		String strThreshold = null;
		String strMultipleOfFirstRound = null;
		String strMultipleOfOtherRound = null;
		String strEffectiveRange = null;
		String strProbability = null;
		String strSafeRange = null;
		String strMaxRetryCount = null;
		String strWaitingSupplementExploringRatio = null;
		String strMinWaitingTimeSecond = null;
		String strMaxWaitingTimeSecond = null;
		String strWaitingFeedbackTimeSecond = null;
		String strWaitingIntoExploringOptimizeMinRatio = null;
		
		String strWaitingSupplementExploringThreshold = null;
		String strWaitingSupplementExploringMaxRatio = null;
		String strExploreSupplementExploringAttenuationRatio = null;
		String strWaitingSupplementExploringMinRange = null;
		String strWaitingSupplementExploringInRangeMinRatio = null;
		String strWaitingSupplementExploringLessRangeMinRatio = null;

		if (EXPLORE_STRATEGY_REQUEST_BASED.equals(strategy)) {
			strThreshold = context.getComponentConfiguration("explore", app, type, "needRequestCount");
			strMultipleOfFirstRound = context.getComponentConfiguration("explore", app, type, "multipleByRequestOfFirstRound");
			strMultipleOfOtherRound = context.getComponentConfiguration("explore", app, type, "multipleByRequestOfOtherRound");
			strEffectiveRange = context.getComponentConfiguration("explore", app, type, "effectiveExploringRange");
			strProbability = context.getComponentConfiguration("explore", app, type, "exploreProbability");
		} else if (EXPLORE_STRATEGY_IMPRESSION_BASED.equals(strategy)) {
			strThreshold = context.getComponentConfiguration("explore", app, type, "needImpressionCount");
			strMultipleOfFirstRound = context.getComponentConfiguration("explore", app, type, "multipleByImpressionOfFirstRound");
			strMultipleOfOtherRound = context.getComponentConfiguration("explore", app, type, "multipleByImpressionOfOtherRound");
			strEffectiveRange = context.getComponentConfiguration("explore", app, type, "effectiveExploringRange");
			strProbability = context.getComponentConfiguration("explore", app, type, "exploreProbability");
			strSafeRange = context.getComponentConfiguration("explore", app, type, "safeRange");
			strMaxRetryCount = context.getComponentConfiguration("explore", app, type, "fillOneExploreIndexMaxRetryCount");
			strWaitingSupplementExploringRatio = context.getComponentConfiguration("explore", app, type, "waitingSupplementExploringRatio");
			strMinWaitingTimeSecond = context.getComponentConfiguration("explore", app, type, "minWaitingTimeSecond");
			strMaxWaitingTimeSecond = context.getComponentConfiguration("explore", app, type, "maxWaitingTimeSecond");
			strWaitingFeedbackTimeSecond = context.getComponentConfiguration("explore", app, type, "waitingFeedbackTimeSecond");
			strWaitingIntoExploringOptimizeMinRatio = context.getComponentConfiguration("explore", app, type,
					"waitingIntoExploringOptimizeMinRatio");

			strWaitingSupplementExploringThreshold = context.getComponentConfiguration("explore", app, type,
					"waitingSupplementExploringThreshold");
			strWaitingSupplementExploringMaxRatio = context.getComponentConfiguration("explore", app, type,
					"waitingSupplementExploringMaxRatio");
			strExploreSupplementExploringAttenuationRatio = context.getComponentConfiguration("explore", app, type,
					"exploreSupplementExploringAttenuationRatio");
			
			strWaitingSupplementExploringMinRange = context.getComponentConfiguration("explore", app, type,
					"waitingSupplementExploringMinRange");
			strWaitingSupplementExploringInRangeMinRatio = context.getComponentConfiguration("explore", app, type,
					"waitingSupplementExploringInRangeMinRatio");
			strWaitingSupplementExploringLessRangeMinRatio = context.getComponentConfiguration("explore", app, type,
					"waitingSupplementExploringLessRangeMinRatio");
			
		} else {
			throw new IllegalArgumentException("Unknow explore strategy " + strategy);
		}

		bFilterByTypes = Boolean.parseBoolean(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "filterTypes"));
		nFilterTypeFailCountToRemove = NumberUtils
				.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "flowfilterTypeFailCnt2Remove"), 5000);

		multipleOfFirstRound = NumberUtils.toDouble(strMultipleOfFirstRound, 1.5d);
		multipleOfOtherRound = NumberUtils.toDouble(strMultipleOfOtherRound, 1.5d);
		threshold = NumberUtils.toInt(strThreshold, 50);
		effectiveRange = NumberUtils.toInt(strEffectiveRange, 50);
		probability = NumberUtils.toDouble(strProbability, 0.15d);
		safeRange = NumberUtils.toInt(strSafeRange, 150);
		maxRetryCount = NumberUtils.toInt(strMaxRetryCount, 5);
		waitingSupplementExploringRatio = NumberUtils.toFloat(strWaitingSupplementExploringRatio, 0.5f);
		minWaitingTimeSecond = NumberUtils.toInt(strMinWaitingTimeSecond, 60);
		maxWaitingTimeSecond = NumberUtils.toInt(strMaxWaitingTimeSecond, 172800);
		waitingFeedbackTimeSecond = NumberUtils.toInt(strWaitingFeedbackTimeSecond, 600);
		waitingIntoExploringOptimizeMinRatio = NumberUtils.toFloat(strWaitingIntoExploringOptimizeMinRatio, 2.0f);
		
		waitingSupplementExploringThreshold = NumberUtils.toInt(strWaitingSupplementExploringThreshold, 2000);
		waitingSupplementExploringMaxRatio = NumberUtils.toFloat(strWaitingSupplementExploringMaxRatio, 0.95f);
		exploreSupplementExploringAttenuationRatio = NumberUtils.toFloat(strExploreSupplementExploringAttenuationRatio, 0.95f);
		waitingSupplementExploringMinRange = NumberUtils.toInt(strWaitingSupplementExploringMinRange, 100);
		waitingSupplementExploringInRangeMinRatio = NumberUtils.toFloat(strWaitingSupplementExploringInRangeMinRatio, 0.6f);
		waitingSupplementExploringLessRangeMinRatio = NumberUtils.toFloat(strWaitingSupplementExploringLessRangeMinRatio, 0.1f);
		if (logger.isDebugEnabled()) {
			logger.info(ContextUtils.toRequestInfoString(context) + " ExploreParameter: " + this.toString());
		}
	}

	private Map<String, String> getExpInfoKey(String app, String language) {
		String prefix = Constants.REDIS_KEY_PREFIX_EXPINFO;
		String separator = Constants.SEPARATOR_REDIS_KEY;
		String suffix = (Constants.ENABLE_EXPLORE_TEST) ? "testing" : "";

		StringBuffer sb = new StringBuffer();
		StringBuffer allSb = new StringBuffer();
		sb.append(prefix).append(separator).append(app);
		allSb.append(prefix).append(separator).append("-1");
		if (!language.equalsIgnoreCase(Constants.ZH_CN)){
			sb.append(separator).append(language);
			allSb.append(separator).append(language);
		}
		String key_base = sb.toString();		
		if (!EXPLORE_TYPE_NEWS.equalsIgnoreCase(type))
			sb.append(separator).append(type);			
		
		
		String expinfo_key = sb.toString();
		String eagerly_key = expinfo_key + separator + "eagerly";
		String flowing_key = expinfo_key + separator + "ing";
		
		String impressionOfInfoIdKey = key_base + separator + "impressionOfInfoIdKey";
		String allImpressionOfInfoIdKey = allSb.toString() + separator + "impressionOfInfoIdKey";
		if (EXPLORE_TYPE_LOCKSCREEN_NEWS.equalsIgnoreCase(type)){
			impressionOfInfoIdKey = sb.toString() + separator + "impressionOfInfoIdKey";
			allImpressionOfInfoIdKey = impressionOfInfoIdKey;//目前来说锁屏取值还是没有算在-1的
		}			

		if (StringUtils.isNotEmpty(suffix)) {
			expinfo_key += separator + suffix;
			eagerly_key += separator + suffix;
			flowing_key += separator + suffix;
		}

		Map<String, String> mResult = new HashMap<String, String>();
		mResult.put("explore", expinfo_key);
		mResult.put("waiting", eagerly_key);
		mResult.put("exploring", flowing_key);
		mResult.put("impression", impressionOfInfoIdKey);
		mResult.put("-1impression", allImpressionOfInfoIdKey);
		return mResult;
	}

	/**
	 * get redis key for explore pool.
	 */
	public String getRedisKeyForExplore() {
		return mExpInfoKey.get("explore");
	}

	/**
	 * get redis key for exploring pool.
	 */
	public String getRedisKeyForExploring() {
		return mExpInfoKey.get("exploring");
	}

	/**
	 * get redis key for waiting pool.
	 */
	public String getRedisKeyForWaiting() {
		return mExpInfoKey.get("waiting");
	}

	/**
	 * get redis key for impression count.
	 */
	public String getRedisKeyForImpressionCount() {
		return mExpInfoKey.get("impression");
	}
	/**
	 * get redis key for impression count.
	 */
	public String getRedisKeyForAllImpressionCount() {
		return mExpInfoKey.get("-1impression");
	}

	/**
	 * get redis key for need request count.
	 */
	public String getRedisKeyForNeedRequestCount(String contentId) {
		StringBuffer sb = new StringBuffer();
		sb.append(contentId).append(Constants.SEPARATOR_REDIS_KEY).append(app);
		
		if (EXPLORE_TYPE_LOCKSCREEN_NEWS.equalsIgnoreCase(type)){
			sb.append(Constants.SEPARATOR_REDIS_KEY).append(type);
		} 
		
		if (Constants.ENABLE_EXPLORE_TEST) {
			sb.append(Constants.SEPARATOR_REDIS_KEY).append("testing");
		}

		return sb.toString();
	}

	//
	public String getStrategy() {
		return strategy;
	}

	public void setMultipleOfFirstRound(double _multipleOfFirstRound) {
		multipleOfFirstRound = _multipleOfFirstRound;
	}

	public double getMultipleOfFirstRound() {
		return multipleOfFirstRound;
	}

	public void setMultipleOfOtherRound(double _multipleOfOtherRound) {
		multipleOfOtherRound = _multipleOfOtherRound;
	}

	public double getMultipleOfOtherRound() {
		return multipleOfOtherRound;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setEffectiveRange(int _effectiveRange) {
		effectiveRange = _effectiveRange;
	}

	public int getEffectiveRange() {
		return effectiveRange;
	}

	public void setProbability(double _probability) {
		probability = _probability;
	}

	public double getProbability() {
		return probability;
	}

	public int getSafeRange() {
		return safeRange;
	}

	public void setSafeRange(int _safeRange) {
		this.safeRange = _safeRange;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public float getWaitingSupplementExploringRatio() {
		return waitingSupplementExploringRatio;
	}

	public void setWaitingSupplementExploringRatio(float _waitingSupplementExploringRatio) {
		this.waitingSupplementExploringRatio = _waitingSupplementExploringRatio;
	}

	public int getMinWaitingTimeSecond() {
		return minWaitingTimeSecond;
	}

	public void setMinWaitingTimeSecond(int _minWaitingTimeSecond) {
		this.minWaitingTimeSecond = _minWaitingTimeSecond;
	}

	public int getMaxWaitingTimeSecond() {
		return maxWaitingTimeSecond;
	}

	public void setMaxWaitingTimeSecond(int _maxWaitingTimeSecond) {
		this.maxWaitingTimeSecond = _maxWaitingTimeSecond;
	}

	public double getExploringScore(int needRequestCount) {
		return Double.valueOf(
				(new SimpleDateFormat("yyyyMMddHH")).format(new Date()) + StringUtils.leftPad(String.valueOf(needRequestCount), 3, "0"));
	}

	public double getWaitingScore() {
		return new Date().getTime();
	}

	public String getApp() {
		return app;
	}

	public String getUid() {
		return uid;
	}

	public String getRedisKeyForContentInfo(String contentId) {
		return "news_" + contentId;
	}

	public String getRedisKeyForFilterTypeFailCount(String contentId) {
		return "filterTypeFailCount_" + contentId;
	}

	public int getWaitingFeedbackTimeSecond() {
		return waitingFeedbackTimeSecond;
	}

	public void setWaitingFeedbackTimeSecond(int _waitingFeedbackTimeSecond) {
		waitingFeedbackTimeSecond = _waitingFeedbackTimeSecond;
	}

	public float getWaitingIntoExploringOptimizeMinRatio() {
		return waitingIntoExploringOptimizeMinRatio;
	}

	public void setWaitingIntoExploringOptimizeMinRatio(float waitingIntoExploringOptimizeMinRatio) {
		this.waitingIntoExploringOptimizeMinRatio = waitingIntoExploringOptimizeMinRatio;
	}

	public ArticleFilterImpl getArticleFilterImpl() {
		return articleFilterImpl;
	}

	public ArticleProperty getArticleProperty() {
		return articleProperty;
	}

	public boolean isFilterByTypes() {
		return bFilterByTypes;
	}

	public int getFilterTypeFailCountToRemove() {
		return nFilterTypeFailCountToRemove;
	}

	public String getScenario() {
		return scenario;
	}

	public String getType() {
		return type;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	
	public int getWaitingSupplementExploringThreshold() {
		return waitingSupplementExploringThreshold;
	}

	public void setWaitingSupplementExploringThreshold(
			int waitingSupplementExploringThreshold) {
		this.waitingSupplementExploringThreshold = waitingSupplementExploringThreshold;
	}

	public float getWaitingSupplementExploringMaxRatio() {
		return waitingSupplementExploringMaxRatio;
	}

	public void setWaitingSupplementExploringMaxRatio(
			float waitingSupplementExploringMaxRatio) {
		this.waitingSupplementExploringMaxRatio = waitingSupplementExploringMaxRatio;
	}

	public float getExploreSupplementExploringAttenuationRatio() {
		return exploreSupplementExploringAttenuationRatio;
	}

	public void setExploreSupplementExploringAttenuationRatio(
			float exploreSupplementExploringAttenuationRatio) {
		this.exploreSupplementExploringAttenuationRatio = exploreSupplementExploringAttenuationRatio;
	}

	
	public int getWaitingSupplementExploringMinRange() {
		return waitingSupplementExploringMinRange;
	}

	public void setWaitingSupplementExploringMinRange(
			int waitingSupplementExploringMinRange) {
		this.waitingSupplementExploringMinRange = waitingSupplementExploringMinRange;
	}

	public float getWaitingSupplementExploringInRangeMinRatio() {
		return waitingSupplementExploringInRangeMinRatio;
	}

	public void setWaitingSupplementExploringInRangeMinRatio(
			float waitingSupplementExploringInRangeMinRatio) {
		this.waitingSupplementExploringInRangeMinRatio = waitingSupplementExploringInRangeMinRatio;
	}

	public float getWaitingSupplementExploringLessRangeMinRatio() {
		return waitingSupplementExploringLessRangeMinRatio;
	}

	public void setWaitingSupplementExploringLessRangeMinRatio(
			float waitingSupplementExploringLessRangeMinRatio) {
		this.waitingSupplementExploringLessRangeMinRatio = waitingSupplementExploringLessRangeMinRatio;
	}

	public String toString() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("app", app);
		map.put("language", language);
		map.put("type", type);
		map.put("strategy", strategy);
		map.put("scenario", scenario);
		map.put("multipleOfFirstRound", multipleOfFirstRound);
		map.put("multipleOfOtherRound", multipleOfOtherRound);
		map.put("threshold", threshold);
		map.put("effectiveRange", effectiveRange);
		map.put("probability", probability);
		map.put("safeRange", safeRange);
		map.put("maxRetryCount", maxRetryCount);
		map.put("waitingSupplementExploringRatio", waitingSupplementExploringRatio);
		map.put("minWaitingTimeSecond", minWaitingTimeSecond);
		map.put("maxWaitingTimeSecond", maxWaitingTimeSecond);
		map.put("waitingFeedbackTimeSecond", waitingFeedbackTimeSecond);
		map.put("waitingIntoExploringOptimizeMinRatio", waitingIntoExploringOptimizeMinRatio);
		map.put("waitingSupplementExploringThreshold", waitingSupplementExploringThreshold);
		map.put("waitingSupplementExploringMaxRatio", waitingSupplementExploringMaxRatio);
		map.put("exploreSupplementExploringAttenuationRatio", exploreSupplementExploringAttenuationRatio);
		map.put("waitingSupplementExploringMinRange", waitingSupplementExploringMinRange);		
		map.put("waitingSupplementExploringLessRangeMinRatio", waitingSupplementExploringLessRangeMinRatio);		
		map.put("waitingSupplementExploringInRangeMinRatio", waitingSupplementExploringInRangeMinRatio);		
		return JSON.toJSONString(map);
	}
}