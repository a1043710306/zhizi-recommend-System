package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import inveno.spider.common.utils.ModelBuilder;
import inveno.spider.common.utils.NotMapModelBuilder;

public class Article implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 资讯ID */
	private String contentId;

	/**
	 * 资讯类型 0 普通资讯 1 视频 2 音频 3 读书 4 普通广告 5 置顶曝光广告 6 活动链接类型 7 大图频道类型
	 */
	private String type = "0";

	/**
	 * 原始发布者 (e.g. 宝宝网，美家美户, 人民日报)
	 */
	private String publisher = "";

	/**
	 * 来源类型(e.g. web / app/RSS/Blog/SocialMedia)
	 */
	private String sourceType = "";

	/**
	 * 来源 (e.g. 头条，一点咨询, 微信)
	 */
	private String source = "";

	/**
	 * 知名作者
	 */
	private String author = "";

	/**
	 * 语言 中国：zh_CN 英文：en
	 */
	private String language = "";

	/**
	 * 国家 中国：CN 英国：UN 美国：USA
	 */
	private String country = "";

	/**
	 * 原文章发布时间
	 */
	private Date publishTime;

	/**
	 * 文章第一次采集时间
	 */
	private Date discoveryTime;

	/**
	 * 爬虫最后一次采集时间
	 */
	private Date fetchTime;

	/**
	 * item在来源的id（e.g. 在微信或头条里的id）
	 */
	private String sourceItemId = "";

	/**
	 * only available for type CONTENT_TYPE_CLIP and CONTENT_TYPE_VIDEO
	 */
	private int duration = 0;
	/**
	 * only for s_channel_content/s_channel_content_audit to save duration of
	 * clip
	 */
	private int second = 0;

	/**
	 * 版权信息
	 */
	private String copyright = "";

	/**
	 * 是否有版权 0:无版权。 1:有版权
	 */
	private Integer hasCopyright = 0;

	/**
	 * 详情页内容包含所有图片数量
	 */
	private Integer bodyImagesCount = 0;

	/**
	 * 列表页(引导图小图)数量
	 */
	private Integer listImagesCount = 0;

	/**
	 * publisher的page rank属性
	 */
	private Double publisherPagerankScore = 0.0;

	/**
	 * 抓取到的用户评论
	 */
	private String sourceComment;
	/**
	 * 抓取到的用户评论数
	 */
	private Integer sourceCommentCount = 0;
	/**
	 * 抓取到的用点赞数
	 */
	private Integer sourceLikeCount;
	/**
	 * 抓取到的文章阅读数
	 */
	private Integer sourceImpressionCount;

	/**
	 * 是否允许评论 0：不允许。1：允许
	 */
	private Integer isOpenComment;

	/**
	 * 自家用户评论数
	 */
	private Integer commentCount = 0;

	/**
	 * 自家分享数
	 */
	private Integer shareCount = 0;

	/**
	 * 自家点赞数
	 */
	private Integer thumbupCount = 0;

	/**
	 * 自家点差数
	 * 
	 */
	private Integer thumbdownCount = 0;

	/**
	 * 处理此条数据的cp的版本号
	 */
	private String cpVersion = "";

	/**
	 * 来源公众号
	 */
	private String sourceFeeds = "";

	/**
	 * 来源频道 (种子) 种子url
	 */
	private String sourceFeedsUrl = "";

	/**
	 * 频道
	 */
	private String channel = "";

	/**
	 * 爬取到的keywords {v1:[{str:"吴奇隆"},{str:"刘诗诗"}],v2:[{str:"婚礼"},{str:"裸婚"}]}
	 */
	private Map<String, KeyWord[]> keywords;

	/**
	 * 爬虫基于该字段来爬取内容，是一个唯一的网址，会根据此生成uuid
	 */
	private String link = "";

	/**
	 * 资讯标题
	 */
	private String title = "";

	/**
	 * 资讯正文
	 */
	private String content = "";

	/**
	 * 资讯简介
	 */
	private String summary = "";

	/**
	 * 详情页内容包含所有图片、简介(通过爬虫爬去过来的原图) [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"}]
	 */
	private Img[] bodyImages;

	/**
	 * 列表页(引导图小图集合 文件名标明图片尺寸,爬虫负责裁图) [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"}]
	 */
	private Img[] listImages;

	/**
	 * 瀑布流里面的首图url
	 */
	private String fallImage = "";

	/**
	 * 可以展示的图片（可以支持多张） [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:""},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:""}]
	 */
	private Img[] displayListImages;

	/**
	 * 曝光的渠道 [{app:\"emui\"},{app:\"coolpad\"}]
	 */
	private String firmApp = "[]";

	/**
	 * 根据link生成的item的唯一标识（UUID5: URL_NAMESPACE）
	 */
	private String itemUuid = "";

	/**
	 * 由爬取到source, source_feeds mapping过来的分类
	 * [{category:12,weight:0.8},{category:2,weight:0.2}]
	 */
	private Map<String, Category[]> categories;

	/**
	 * 正文的分词结果
	 */
	private String contentSegments = "";

	/**
	 * 地域属性(confidence取值[0-1]）
	 * [{country:"中国",province:"广东省",city:"深圳市",confidence:0.8}]
	 */
	private Local[] local;

	/**
	 * facebook scoring
	 */
	private int rate;
	/**
	 * 爬虫类型
	 */
	private String crawlerType;

	/**
	 * 媒体编码 该编码只用于日志打印，不保存数据库
	 */
	private String pubCode;

	/**
	 * youtube的视频分类
	 */
	private String videoCategory;
	/**
	 * youtube的视频自身分类
	 */
	private String youtubeCategory;

	/**
	 * 漫画分类
	 */
	private String categoriesComic;

	/**
	 * 文章 GIF MP4
	 */
	private String[] bodyGIFs;

	/**
	 * Youtube Topics
	 */
	private String[] topics;

	public String getVideoCategory() {
		return videoCategory;
	}

	public void setVideoCategory(String videoCategory) {
		this.videoCategory = videoCategory;
	}

	@Override
	public String toString() {
		// return (new Gson()).toJson(this, Article.class);
		// modify by Symon at 2017年3月31日11:06:00，防止特殊字符被gson自动转成unicode
		return NotMapModelBuilder.getInstance().toJson(this, Article.class);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPubCode() {
		return pubCode;
	}

	public void setPubCode(String pubCode) {
		this.pubCode = pubCode;
	}

	public String getSourceFeeds() {
		return sourceFeeds;
	}

	public void setSourceFeeds(String sourceFeeds) {
		this.sourceFeeds = sourceFeeds;
	}

	public String getSourceFeedsUrl() {
		return sourceFeedsUrl;
	}

	public void setSourceFeedsUrl(String sourceFeedsUrl) {
		this.sourceFeedsUrl = sourceFeedsUrl;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setBodyImages(Img[] bodyImages) {
		this.bodyImages = bodyImages;
	}

	public void setListImages(Img[] listImages) {
		this.listImages = listImages;
	}

	public String getFallImage() {
		return fallImage;
	}

	public void setFallImage(String fallImage) {
		this.fallImage = fallImage;
	}

	public void setDisplayListImages(Img[] displayListImages) {
		this.displayListImages = displayListImages;
	}

	public String getFirmApp() {
		return firmApp;
	}

	public void setFirmApp(String firmApp) {
		this.firmApp = firmApp;
	}

	public String getItemUuid() {
		return itemUuid;
	}

	public void setItemUuid(String itemUuid) {
		this.itemUuid = itemUuid;
	}

	public void setKeywords(Map<String, KeyWord[]> keywords) {
		this.keywords = keywords;
	}

	public void setCategories(Map<String, Category[]> categories) {
		this.categories = categories;
	}

	public String getContentSegments() {
		return contentSegments;
	}

	public void setContentSegments(String contentSegments) {
		this.contentSegments = contentSegments;
	}

	public void setLocal(Local[] local) {
		this.local = local;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId == null ? null : contentId.trim();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type == null ? null : type.trim();
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher == null ? null : publisher.trim();
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType == null ? null : sourceType.trim();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source == null ? null : source.trim();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author == null ? null : author.trim();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language == null ? null : language.trim();
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country == null ? null : country.trim();
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Date fetchTime) {
		this.fetchTime = fetchTime;
	}

	public Date getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}

	public Date getDiscoveryTime() {
		return discoveryTime;
	}

	public void setDiscoveryTime(Date discoveryTime) {
		this.discoveryTime = discoveryTime;
	}

	public String getSourceItemId() {
		return sourceItemId;
	}

	public void setSourceItemId(String sourceItemId) {
		this.sourceItemId = sourceItemId == null ? null : sourceItemId.trim();
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int _duration) {
		duration = _duration;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int _second) {
		second = _second;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright == null ? null : copyright.trim();
	}

	public Integer getHasCopyright() {
		return hasCopyright;
	}

	public void setHasCopyright(Integer hasCopyright) {
		this.hasCopyright = hasCopyright;
	}

	public Integer getBodyImagesCount() {
		return bodyImagesCount;
	}

	public void setBodyImagesCount(Integer bodyImagesCount) {
		this.bodyImagesCount = bodyImagesCount;
	}

	public Integer getListImagesCount() {
		return listImagesCount;
	}

	public void setListImagesCount(Integer listImagesCount) {
		this.listImagesCount = listImagesCount;
	}

	public Double getPublisherPagerankScore() {
		return publisherPagerankScore;
	}

	public void setPublisherPagerankScore(Double publisherPagerankScore) {
		this.publisherPagerankScore = publisherPagerankScore;
	}

	public String getSourceComment() {
		return sourceComment;
	}

	public void setSourceComment(String _sourceComment) {
		sourceComment = _sourceComment;
	}

	public Integer getSourceCommentCount() {
		return sourceCommentCount;
	}

	public void setSourceCommentCount(Integer sourceCommentCount) {
		this.sourceCommentCount = sourceCommentCount;
	}

	public Integer getSourceLikeCount() {
		return sourceLikeCount;
	}

	public void setSourceLikeCount(Integer sourceLikeCount) {
		this.sourceLikeCount = sourceLikeCount;
	}

	public Integer getSourceImpressionCount() {
		return sourceImpressionCount;
	}

	public void setSourceImpressionCount(Integer sourceImpressionCount) {
		this.sourceImpressionCount = sourceImpressionCount;
	}

	public Integer getIsOpenComment() {
		return isOpenComment;
	}

	public void setIsOpenComment(Integer isOpenComment) {
		this.isOpenComment = isOpenComment;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getShareCount() {
		return shareCount;
	}

	public void setShareCount(Integer shareCount) {
		this.shareCount = shareCount;
	}

	public Integer getThumbupCount() {
		return thumbupCount;
	}

	public void setThumbupCount(Integer thumbupCount) {
		this.thumbupCount = thumbupCount;
	}

	public Integer getThumbdownCount() {
		return thumbdownCount;
	}

	public void setThumbdownCount(Integer thumbdownCount) {
		this.thumbdownCount = thumbdownCount;
	}

	public String getCpVersion() {
		return cpVersion;
	}

	public void setCpVersion(String cpVersion) {
		this.cpVersion = cpVersion == null ? null : cpVersion.trim();
	}

	public Map<String, KeyWord[]> getKeywords() {
		return keywords;
	}

	public Img[] getBodyImages() {
		return bodyImages;
	}

	public Img[] getListImages() {
		return listImages;
	}

	public Img[] getDisplayListImages() {
		return displayListImages;
	}

	public Map<String, Category[]> getCategories() {
		return categories;
	}

	public Local[] getLocal() {
		return local;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int _rate) {
		rate = _rate;
	}

	public String getCrawlerType() {
		return crawlerType;
	}

	public void setCrawlerType(String crawlerType) {
		this.crawlerType = crawlerType;
	}

	/**
	 * 根据Object转换成JSON，存储到数据库 Description:
	 * 
	 * @author liyuanyi DateTime 2016年3月20日 下午4:44:29
	 * @param o
	 * @return
	 */
	public String getObjectToJson(Object o) {
		if (o == null)
			return "[]";
		return ModelBuilder.getInstance().toJson(o);
	}

	public <T> T getObjectFromJson(String json, Class<T> clazz) {
		return JSON.parseObject(json, clazz);
	}

	public Img[] parseJson2ImgArray(String json) {
		JSONArray array = JSONArray.parseArray(json);
		if (array == null)
			return null;
		Img[] tArray = new Img[array.size()];
		for (int i = 0; i < array.size(); i++) {
			tArray[i] = JSONObject.toJavaObject(array.getJSONObject(i), Img.class);
		}
		return tArray;
	}

	public Local[] parseJson2LocalArray(String json) {
		JSONArray array = JSONArray.parseArray(json);
		if (array == null)
			return null;
		Local[] tArray = new Local[array.size()];
		for (int i = 0; i < array.size(); i++) {
			tArray[i] = JSONObject.toJavaObject(array.getJSONObject(i), Local.class);
		}
		return tArray;
	}

	public Article() {
	}

	public Article(Content content) {
		if (content != null) {
			this.setType(content.getType());
			this.setPublisher(content.getPublisher());
			this.setSourceType(content.getSourceType());
			this.setSource(content.getSource());
			this.setAuthor(content.getAuthor());
			this.setLanguage(content.getLanguage());
			this.setCountry(content.getCountry());
			this.setPublishTime(content.getPublishTime() == null ? new Date() : content.getPublishTime());
			this.setDiscoveryTime(content.getDiscoveryTime() == null ? new Date() : content.getDiscoveryTime());
			this.setFetchTime(content.getFetchTime() == null ? new Date() : content.getFetchTime());
			this.setSourceItemId(content.getSourceItemId());
			this.setDuration(content.getDuration());
			this.setCopyright(content.getCopyright());
			this.setHasCopyright(content.getHasCopyright());
			this.setBodyImagesCount(content.getBodyImagesCount());
			this.setListImagesCount(content.getListImagesCount());
			this.setPublisherPagerankScore(content.getPublisherPagerankScore());
			this.setSourceComment(content.getSourceComment());
			this.setSourceCommentCount(content.getSourceCommentCount());
			this.setSourceImpressionCount(
					content.getSourceImpressionCount() == null ? 0 : content.getSourceImpressionCount());
			this.setSourceLikeCount(content.getSourceLikeCount() == null ? 0 : content.getSourceLikeCount());
			this.setIsOpenComment(content.getIsOpenComment());
			this.setCommentCount(content.getCommentCount());
			this.setShareCount(content.getShareCount());
			this.setThumbupCount(content.getThumbupCount());
			this.setThumbdownCount(content.getThumbdownCount());
			this.setCpVersion(content.getCpVersion());
			this.setSourceFeeds(content.getSourceFeeds());
			this.setSourceFeedsUrl(content.getSourceFeedsUrl());
			this.setKeywords(getObjectFromJson(content.getKeywords(), HashMap.class));
			this.setLink(content.getLink());
			this.setTitle(content.getTitle());
			this.setContent(content.getContent());
			this.setSummary(content.getSummary());
			this.setBodyImages(parseJson2ImgArray(content.getBodyImages()));
			this.setListImages(parseJson2ImgArray(content.getListImages()));
			this.setFallImage(content.getFallImage());
			this.setDisplayListImages(parseJson2ImgArray(content.getDisplayListImages()));
			this.setFirmApp(content.getFirmApp());
			this.setItemUuid(content.getItemUuid());
			this.setCategories(getObjectFromJson(content.getCategories(), HashMap.class));
			this.setContentSegments(content.getContentSegments());
			this.setLocal(parseJson2LocalArray(content.getLocal()));
			this.setPubCode(content.getPubCode());
			this.setChannel(content.getChannel());
			this.setRate(content.getRate());
			this.setCrawlerType(content.getCrawlerType());
			this.setYoutubeCategory(content.getYoutubeCategory());
		}
	}

	public String getCategoriesComic() {
		return categoriesComic;
	}

	public void setCategoriesComic(String categoriesComic) {
		this.categoriesComic = categoriesComic;
	}

	public String getYoutubeCategory() {
		return youtubeCategory;
	}

	public void setYoutubeCategory(String youtubeCategory) {
		this.youtubeCategory = youtubeCategory;
	}

	public String[] getBodyGIFs() {
		return bodyGIFs;
	}

	public String[] getTopics() {
		return this.topics;
	}
}
