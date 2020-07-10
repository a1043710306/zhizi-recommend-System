package com.inveno.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import com.inveno.thrift.RecNewsListReq;
import com.inveno.thrift.ResponParam;
import com.inveno.thrift.TimelineNewsListReq;
import com.inveno.thrift.UserInfo;
import com.inveno.thrift.ZhiziListReq;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

import net.sf.ehcache.Element;

/**
 * 
  * @ClassName: Context
  * @Description: 参数传递上下文
  * @author huangyiming
  * @date 2016年4月7日 下午3:01:37
  *
 */
public class Context implements Serializable {
	private static final long serialVersionUID = -3113680526981330449L;

	/**
	 * @deprecated useless for overseas
	 * q和qb接口的request对象
	 */
	public RecNewsListReq recNewsListReq;

	/**
	 * @deprecated useless for overseas
	 * qi和qcn接口的request对象
	 */
	public TimelineNewsListReq timelineNewsListReq;

	/**
	 * 最后的结果集对象
	 */
	public List<ResponParam> responseParamList = new ArrayList<ResponParam>();

	public List<ResponParam> cacheResponseParamList = new ArrayList<ResponParam>();

	/**
	 * 进行备份;
	 */
	public List<ResponParam> responseParamListBak = new ArrayList<ResponParam>();

	/**
	 * 最新资讯对象
	 */
	public List<ResponParam> newsQList = new ArrayList<ResponParam>();

	public List<Double> indexList = new ArrayList<Double>();

	private String uid;

	private String app;

	private int num;

	private ZhiziListReq zhiziListReq;

	private String abtestVersion;

	private int contentQuality = 0;

	/**
	 * 是否存在缓存
	 */
	public int offset = 0;

	public long contentType;

	public Set<Integer> setAvailableContentType;

	public long display;

	public long linkType;

	public long scenario = -1;

	private List<Integer> categoryids;

	private String language;

	private RecommendInfoData recommendInfoData;
	
	private String publisher;

	//兴趣加强各分类资讯的获取数量, Map<ContentType, Map<CategoryId, ArticleAmount>>
	private Map<Integer, Integer> mapContenttypeInterestBoostArticleAmount = new HashMap<Integer, Integer>();
	private Map<Integer, Map<Integer, Integer>> mapContenttypeBoostCategoryArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();

	//兴趣探索各分类资讯的获取数量, Map<ContentType, Map<CategoryId, ArticleAmount>>
	private Map<Integer, Integer> mapContenttypeInterestExploreArticleAmount = new HashMap<Integer, Integer>();
	private Map<Integer, Map<Integer, Integer>> mapContenttypeExploreCategoryArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();

	//各资讯类型的候选池/混插池获取数量, Map<ContentType, Map<Version/Strategy, ArticleAmount>>
	private Map<Integer, Map<Integer, Integer>> mapContenttypeStrategyArticleAmount = new HashMap<Integer, Map<Integer, Integer>>();

	//推荐列表资讯量
	private int fetchNum = 1000;

	ArticleFilterImpl articleFilter = new ArticleFilterImpl();
	ArticleProperty articleProperty = new ArticleProperty();

	private UserInfo userInfo;

	public Element cacheElement = null;

	private Map<String, String> mComponentConfig = new HashMap<String, String>();
	private Map<String, String> mAbtestConfig = new HashMap<String, String>();

	private boolean bPipeLineInvokeTimeout = false;  
	private boolean bAsynPipeLineTimeout = false; //获取推荐列表 fallback列表 按照线程执行流程在coreTimeout时间内没有执行完毕 是否超时
	private String requestId;
	
	// foryou explore delivery
	// news:video = 5:1
	@SuppressWarnings("unused")
	private int index_offset_delivery;
	@SuppressWarnings("unused")
	private int video_index_offset_delivery_remainder;
	
	private long abilityType;
	
	private String contentId;

	public Context() {
	}

	public Context(RecNewsListReq req) {
		this.recNewsListReq = req;
		this.uid = req.getBase().getUid();
		this.app = req.getBase().getApp();
		this.num = req.getNum();
		this.abtestVersion = req.abTestVersion.trim();
		this.language = req.getBase().getLanarea();

		articleProperty.setProduct_id(this.app);
		articleProperty.setPublish_timestamp(new Date().getTime()); //ensure within 48hours..
		articleProperty.setAdult_score(0.0D); //ensure pass check..
		articleProperty.setFirm_app(null);
	}

	public Context(TimelineNewsListReq req) {
		this.timelineNewsListReq = req;
		this.uid = req.getBase().getUid();
		this.app = req.getBase().getApp();
		this.num = req.getNum();
		this.abtestVersion = req.abTestVersion.trim();
		this.language = req.getBase().getLanarea();

		articleProperty.setProduct_id(this.app);
		articleProperty.setPublish_timestamp(new Date().getTime()); //ensure within 48hours..
		articleProperty.setAdult_score(0.0D); //ensure pass check..
		articleProperty.setFirm_app(null);
	}

	public Context(ZhiziListReq req) {
		this.zhiziListReq = req;
		this.uid = req.getUid();
		this.app = req.getApp();
		this.num = req.getNum();
		this.contentType = turnStringToLong(req.getContent_type());
		this.display = turnStringToLong(req.getDisplay());
		this.linkType = turnStringToLong(req.getLink_type());
		this.scenario = turnStringToLong(req.getScenario());
		this.abtestVersion = req.abTestVersion.trim();
		this.language = req.getLanguage();
		this.requestId = req.getRequest_id();
		this.setAvailableContentType = parseAvailableContentType(contentType);	
		this.abilityType = turnStringToLong(req.getAbility_type());
		if(StringUtils.isNotEmpty(req.getContent_id())){
			this.contentId = req.getContent_id().trim();
		}
		
		
		articleFilter.getQueryProperty().setZhizi_request(req);
		articleProperty.setProduct_id(this.app);
		articleProperty.setPublish_timestamp(new Date().getTime()); //ensure within 48hours..
		articleProperty.setAdult_score(0.0D); //ensure pass check..
		articleProperty.setFirm_app(null);
	}


	private Set<Integer> parseAvailableContentType(long contentTypeAvailability) {
		String str = Long.toBinaryString(contentTypeAvailability);
		Set<Integer> setContentType = new HashSet<Integer>();
		for (int j = 0; j < str.length(); j++) {
			if (str.charAt(j) == '1') {
				setContentType.add( (new Double(Math.pow(2, (str.length() - j - 1))).intValue()) );
			}
		}
		return setContentType;
	}

	private long turnStringToLong(String strValue) {
		if (StringUtils.isNotEmpty(strValue)) {
			return Long.parseUnsignedLong(strValue.substring(2), 16);
		} else {
			return 0;
		}
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<ResponParam> getResponseParamList() {
		return responseParamList;
	}

	public  void setEndResponseParamList(List<ResponParam> responseParamList) {	
			this.responseParamList = responseParamList;	
	}
	public   void setResponseParamList(List<ResponParam> responseParamList) {
		if(!bAsynPipeLineTimeout){
			this.responseParamList = responseParamList;
		}
	}

	public RecNewsListReq getRecNewsListReq() {
		return recNewsListReq;
	}

	public void setRecNewsListReq(RecNewsListReq recNewsListReq) {
		this.recNewsListReq = recNewsListReq;
	}

	public TimelineNewsListReq getTimelineNewsListReq() {
		return timelineNewsListReq;
	}

	public void setTimelineNewsListReq(TimelineNewsListReq timelineNewsListReq) {
		this.timelineNewsListReq = timelineNewsListReq;
	}

	public List<ResponParam> getNewsQList() {
		return newsQList;
	}

	public void setNewsQList(List<ResponParam> newsQList) {
		this.newsQList = newsQList;
	}

	public List<ResponParam> getResponseParamListBak() {
		return responseParamListBak;
	}

	public void setResponseParamListBak(List<ResponParam> responseParamListBak) {
		this.responseParamListBak = responseParamListBak;
	}

	public List<Double> getIndexList() {
		return indexList;
	}

	public void setIndexList(List<Double> indexList) {
		this.indexList = indexList;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public ZhiziListReq getZhiziListReq() {
		return zhiziListReq;
	}

	public void setZhiziListReq(ZhiziListReq zhiziListReq) {
		this.zhiziListReq = zhiziListReq;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getAbtestVersion() {
		return abtestVersion;
	}

	public void setAbtestVersion(String abtestVersion) {
		this.abtestVersion = abtestVersion;
	}

	public long getContentType() {
		return contentType;
	}

	public void setContentType(long contentType) {
		this.contentType = contentType;
		this.setAvailableContentType = parseAvailableContentType(contentType);
	}

	public Set<Integer> getAvailableContentType() {
		return setAvailableContentType;
	}

	public void setAvailableContentType(Set<Integer> _setAvailableContentType) {
		setAvailableContentType = _setAvailableContentType;
	}

	public long getDisplay() {
		return display;
	}

	public void setDisplay(long display) {
		this.display = display;
	}

	public long getLinkType() {
		return linkType;
	}

	public void setLinkType(long linkType) {
		this.linkType = linkType;
	}

	public long getScenario() {
		return scenario;
	}

	public void setScenario(long scenario) {
		this.scenario = scenario;
	}

	public List<Integer> getCategoryids() {
		return categoryids;
	}

	public void setCategoryids(List<Integer> categoryids) {
		this.categoryids = categoryids;
	}

	public RecommendInfoData getRecommendInfoData() {
		return recommendInfoData;
	}

	public void setRecommendInfoData(RecommendInfoData recommendInfoData) {
		this.recommendInfoData = recommendInfoData;
	}

	public Map<Integer, Integer> getContenttypeInterestBoostArticleAmount() {
		return mapContenttypeInterestBoostArticleAmount;
	}

	public void setContenttypeInterestBoostArticleAmount(Map<Integer, Integer> mapContenttypeInterestBoostArticleAmount) {
		this.mapContenttypeInterestBoostArticleAmount = mapContenttypeInterestBoostArticleAmount;
	}

	public Map<Integer, Map<Integer, Integer>> getContenttypeBoostCategoryArticleAmount() {
		return mapContenttypeBoostCategoryArticleAmount;
	}

	public void setContenttypeBoostCategoryArticleAmount(Map<Integer, Map<Integer, Integer>> mapContenttypeBoostCategoryArticleAmount) {
		this.mapContenttypeBoostCategoryArticleAmount = mapContenttypeBoostCategoryArticleAmount;
	}

	public Map<Integer, Integer> getContenttypeInterestExploreArticleAmount() {
		return mapContenttypeInterestExploreArticleAmount;
	}

	public void setContenttypeInterestExploreArticleAmount(Map<Integer, Integer> mapContenttypeInterestExploreArticleAmount) {
		this.mapContenttypeInterestExploreArticleAmount = mapContenttypeInterestExploreArticleAmount;
	}

	public Map<Integer, Map<Integer, Integer>> getContenttypeExploreCategoryArticleAmount() {
		return mapContenttypeExploreCategoryArticleAmount;
	}

	public void setContenttypeExploreCategoryArticleAmount(Map<Integer, Map<Integer, Integer>> mapContenttypeExploreCategoryArticleAmount) {
		this.mapContenttypeExploreCategoryArticleAmount = mapContenttypeExploreCategoryArticleAmount;
	}

	public Map<Integer, Map<Integer, Integer>> getContenttypeStrategyArticleAmount() {
		return mapContenttypeStrategyArticleAmount;
	}

	public void setContenttypeStrategyArticleAmount(Map<Integer, Map<Integer, Integer>> mapContenttypeStrategyArticleAmount) {
		this.mapContenttypeStrategyArticleAmount = mapContenttypeStrategyArticleAmount;
	}

	public int getFetchNum() {
		return fetchNum;
	}

	public void setFetchNum(int fetchNum) {
		this.fetchNum = fetchNum;
	}

	public ArticleFilterImpl getArticleFilter() {
		return articleFilter;
	}

	public void setArticleFilter(ArticleFilterImpl articleFilter) {
		this.articleFilter = articleFilter;
	}

	public ArticleProperty getArticleProperty() {
		return articleProperty;
	}

	public void setArticleProperty(ArticleProperty article_property) {
		this.articleProperty = article_property;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public int getContentQuality() {
		return contentQuality;
	}

	public void setContentQuality(int contentQuality) {
		this.contentQuality = contentQuality;
	}

	public List<ResponParam> getCacheResponseParamList() {
		return cacheResponseParamList;
	}

	public void setCacheResponseParamList(List<ResponParam> cacheResponseParamList) {
		this.cacheResponseParamList = cacheResponseParamList;
	}

	public Element getCacheElement() {
		return cacheElement;
	}

	public void setCacheElement(Element cacheElement) {
		this.cacheElement = cacheElement;
	}

	public Map<String, String> getComponentConfigurationMap() {
		return mComponentConfig;
	}

	public String getComponentConfiguration(String... arrConfigName) {
		String strConfigName = StringUtils.join(arrConfigName, "/");
		return mComponentConfig.get(strConfigName);
	}

	public void setComponentConfigurationMap(Map<String, String> _mComponentConfig) {
		mComponentConfig = _mComponentConfig;
	}

	public Map<String, String> getAbtestConfigurationMap() {
		return mAbtestConfig;
	}

	public String getAbtestConfiguration(String... arrConfigName) {
		String strConfigName = StringUtils.join(arrConfigName, "/");
		return mAbtestConfig.get(strConfigName);
	}

	public void setAbtestConfigurationMap(Map<String, String> _mAbtestConfig) {
		mAbtestConfig = _mAbtestConfig;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	

	public boolean isAsynPipeLineTimeout() {
		return bAsynPipeLineTimeout;
	}

	public void setAsynPipeLineTimeout(boolean _bAsynPipeLineTimeout) {
		bAsynPipeLineTimeout = _bAsynPipeLineTimeout;
	}
	
	
	public boolean isPipeLineInvokeTimeout() {
		return bPipeLineInvokeTimeout;
	}

	public void setPipeLineInvokeTimeout(boolean _bPipeLineInvokeTimeout) {
		bPipeLineInvokeTimeout = _bPipeLineInvokeTimeout;
	}
	public int getIndex_offset_delivery() {
		return NumberUtils.toInt(getComponentConfiguration("explore", "index_offset_delivery"), 6);
	}

	public void setIndex_offset_delivery(int index_offset_delivery) {
		this.index_offset_delivery = index_offset_delivery;
	}

	public int getVideo_index_offset_delivery_remainder() {
		return NumberUtils.toInt(getComponentConfiguration("explore", "video_index_offset_delivery_remainder"), 5);
	}

	public void setVideo_index_offset_delivery_remainder(int video_index_offset_delivery_remainder) {
		this.video_index_offset_delivery_remainder = video_index_offset_delivery_remainder;
	}
	
	
	
	
	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public long getAbilityType() {
		return abilityType;
	}

	public void setAbilityType(long abilityType) {
		this.abilityType = abilityType;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" request_id: ");
		sb.append(requestId);
		sb.append(" abtest: ");
		sb.append(abtestVersion);
		sb.append(" app: ");
		sb.append(app);
		if (zhiziListReq != null) {
			sb.append(" op: ");
			sb.append(zhiziListReq.getOperation());
		}
		if (scenario > 0) {
			sb.append(" scenario: ");
			sb.append(scenario);
		}
		
		sb.append(" abilityType: ");
		sb.append(abilityType);
		if(contentId != null){
		sb.append(" contentId: ");
		sb.append(contentId);
		}
		if(publisher != null){
		sb.append(" publisher: ");
		sb.append(publisher);
		}
		
		return sb.toString();
	}
}
