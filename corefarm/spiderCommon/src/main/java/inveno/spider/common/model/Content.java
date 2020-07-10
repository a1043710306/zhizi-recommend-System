package inveno.spider.common.model;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonElement;

import inveno.spider.common.Constants;
import inveno.spider.common.utils.ImageCloudHelper;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.common.utils.NotMapModelBuilder;

/**
 * 抓取表的结构体 Class Name: Content.java Description:
 *
 * @author liyuanyi DateTime 2016年3月18日 下午4:20:29
 * @company inveno
 * @version 1.0
 */
public class Content implements Serializable {
	public static final int STATE_NORMAL = 1;
	public static final int STATE_AUDIT = 2;
	public static final int STATE_OFFSHELF = 3;

	public static final int COPYRIGHT_INVALID = 0;
	public static final int COPYRIGHT_VALID = 1;
	public static final int COPYRIGHT_LEVEL_NONE = 0;
	public static final int COPYRIGHT_LEVEL_UNKNOWN = 1;
	public static final int COPYRIGHT_LEVEL_NORMAL = 2;
	public static final int COPYRIGHT_LEVEL_IMPORTANT = 3;

	public static final int OFFSHELF_CODE_SENSITIVE = 1;
	public static final int OFFSHELF_CODE_ARTIFICIAL = 2;
	public static final int OFFSHELF_CODE_DUPLICATED = 3;
	public static final int OFFSHELF_CODE_COPYRIGHT = 4;
	public static final int OFFSHELF_CODE_IMAGELINK = 5;
	public static final int OFFSHELF_CODE_GIFLENGTH = 6;
	public static final int OFFSHELF_CODE_WORDCOUNT = 7;
	public static final int OFFSHELF_CODE_TO_BE_OFFLINE = 13;
	public static final int OFFSHELF_CODE_TO_BE_CHECKING = 14;
	public static final int OFFSHELF_CODE_FEED_NOT_FOUND = 15;
	public static final int OFFSHELF_CODE_GIF_DIMENSION_FILTER = 18;
	public static final int OFFSHELF_CODE_SOURCE_IMPRESSION_COUNT_CHECKING = 20;
	public static final int OFFSHELF_CODE_VIDEO_COUNT_CHECKING = 21;
	public static final int OFFSHELF_CODE_FALLIMAGE_IS_EMPTY = 24;
	public static final int OFFSHELF_CODE_VIDEO_IS_LIVE = 23;
	public static final int OFFSHELF_CODE_IFRAME_DISPLAY_ERROR = 31;

	public static final int COMMENT_ALLOW = 1;
	public static final int COMMENT_REFUSE = 0;

	public static final int APP_COMMENT_ALLOW = 0;
	public static final int APP_COMMENT_REFUSE = 1;

	public static final int CONTENT_TYPE_GENERAL = 0;
	public static final int CONTENT_TYPE_CLIP = 1;
	public static final int CONTENT_TYPE_VIDEO = 2;
	public static final int CONTENT_TYPE_TEXT = 3;
	public static final int CONTENT_TYPE_TOPIC = 4;
	public static final int CONTENT_TYPE_ANIMATION = 5;
	public static final int CONTENT_TYPE_BANNER = 6;
	public static final int CONTENT_TYPE_WALLPAPER = 7;
	public static final int CONTENT_TYPE_GALLERY = 8;
	public static final int CONTENT_TYPE_ADVERTISING = 9;
	public static final int CONTENT_TYPE_ADVERTORIAL = 10;
	public static final int CONTENT_TYPE_SPONSOREDAD = 11;
	public static final int CONTENT_TYPE_AUDIO = 12;

	public static final int DISPLAY_TYPE_TEXT = 0x0001; // 无图
	public static final int DISPLAY_TYPE_SINGLE_PIC = 0x0002; // 单张缩略图
	public static final int DISPLAY_TYPE_TRIPPLE_PIC = 0x0004; // 三图
	public static final int DISPLAY_TYPE_BANNER = 0x0008; // 通栏
	public static final int DISPLAY_TYPE_MULTIPLE_PIC = 0x0010; // 多图文章
	public static final int DISPLAY_TYPE_WATERFALL = 0x0020; // 瀑布流
	public static final int DISPLAY_TYPE_GALLERY = 0x0040; // 图集
	public static final int DISPLAY_TYPE_SLIDE = 0x0080; // 轮播
	public static final int DISPLAY_TYPE_VOTE_TEXT = 0x0100; // 文字投票卡片
	public static final int DISPLAY_TYPE_VOTE_PIC = 0x0200; // 两图投票卡片
	public static final int DISPLAY_TYPE_WALLPAPER = 0x0400; // 单张大图
	public static final int DISPLAY_TYPE_ANIMATION = 0x0800; // 单张GIF
	public static final int DISPLAY_TYPE_VIDEO = 0x1000; // 单条视频
	public static final int DISPLAY_TYPE_COMICS = 0x10000; // 漫画单竖图
	public static final int DISPLAY_TYPE_LOCKSCREEN_NEW = 0x00020000; // 锁屏的图文资讯
	public static final int DISPLAY_TYPE_LOCKSCREEN_MEMES = 0x00040000; // 锁屏的memes资讯
	public static final int DISPLAY_TYPE_GIF_MPEG4 = 0x00080000; // GIF to MP4
	public static final int DISPLAY_TYPE_VIDEO_THUMBNAIL = 0x00100000; // 单张小图视频

	public static final int LINK_TYPE_WEBVIEW = 0x00000001;
	public static final int LINK_TYPE_NATIVE = 0x00000002;
	public static final int LINK_TYPE_FORWARDPV = 0x00000004;
	public static final int LINK_TYPE_BROWSER = 0x00000008;
	public static final int LINK_TYPE_TOPIC = 0x00000010;
	public static final int LINK_TYPE_NOACTION = 0x00000020;
	public static final int LINK_TYPE_PLAY_IN_LIST = 0x00000040;
	public static final int LINK_TYPE_PLAY_IN_DETAIL = 0x00000080;
	public static final int LINK_TYPE_READSOURCE = 0x00000100;
	public static final int LINK_TYPE_ACTIVATE = 0x00000200;
	public static final int LINK_TYPE_PHONECALL = 0x00000400;
	public static final int LINK_TYPE_SHORTMESSAGE = 0x00000800;
	public static final int LINK_TYPE_SMARTVIEW = 0x00001000;
	public static final int LINK_TYPE_H5NATIVE = 0x00002000;
	public static final int LINK_TYPE_MP4 = 0x00008000;
	public static final int LINK_TYPE_GALLERY = 0x00010000;

	private static final Logger LOG = LoggerFactory.make();

	private static final long serialVersionUID = 1L;

	private HashSet<String> hsFallImageIdBlacklist = new HashSet<String>();
	private ContentExtend contentExtend = null;
	private String currentMonthTableName;
	private ArrayList<String> blockquoteList = new ArrayList<String>();
	/** 资讯ID */
	private String contentId;

	/**
	 * 资讯类型 0 普通资讯 1 短视频 - 短视频特指在列表页直接播放视频 2 长视频 - 长视频指需要自身播放器或跳转到第三方播放器播放的视频 3
	 * 话题 4 专题 5 动态GIF图 6 banner 7 美图（单张大图形式） 8 图集 9 硬文广告 10 软文广告 11 第三方广告 12 音频
	 */
	private String type;
	/**
	 * content type, just for compatible w/ interface between server and
	 * app-client the value should be exponential of 2 according to value of
	 * type.
	 */
	private int contentType;

	/**
	 * 原始发布者 (e.g. 宝宝网，美家美户, 人民日报)
	 */
	private String publisher;

	/**
	 * 来源类型(e.g. web / app/RSS/Blog/SocialMedia)
	 */
	private String sourceType;

	/**
	 * 来源 (e.g. 头条，一点咨询, 微信)
	 */
	private String source;

	/**
	 * 知名作者
	 */
	private String author;

	/**
	 * 语言 中国：zh_CN 英文：en
	 */
	private String language;

	/**
	 * 国家 中国：CN 英国：UN 美国：USA
	 */
	private String country;

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
	private String sourceItemId;

	/**
	 * only available for type CONTENT_TYPE_CLIP and CONTENT_TYPE_VIDEO
	 */
	private int duration = 0;

	/**
	 * 版权信息
	 */
	private String copyright;

	/**
	 * 是否有版权 0:无版权。 1:有版权
	 */
	private Integer hasCopyright;

	/**
	 * 详情页内容包含所有图片数量
	 */
	private Integer bodyImagesCount;

	/**
	 * 列表页(引导图小图)数量
	 */
	private Integer listImagesCount;

	/**
	 * publisher的page rank属性
	 */
	private Double publisherPagerankScore;

	/**
	 * 抓取到的用户评论
	 */
	private String sourceComment;
	/**
	 * 抓取到的用户评论数
	 */
	private Integer sourceCommentCount;
	/**
	 * 抓取到的用点赞数
	 */
	private Integer sourceLikeCount = 0;
	/**
	 * 抓取到的文章阅读数
	 */
	private Integer sourceImpressionCount = 0;

	/**
	 * 是否允许评论 0：不允许。1：允许
	 */
	private Integer isOpenComment;

	/**
	 * 自家用户评论数
	 */
	private Integer commentCount;

	/**
	 * 自家分享数
	 */
	private Integer shareCount;

	/**
	 * 自家点赞数
	 */
	private Integer thumbupCount;

	/**
	 * 自家点差数
	 */
	private Integer thumbdownCount;

	/**
	 * 处理此条数据的cp的版本号
	 */
	private String cpVersion;

	/**
	 * 来源公众号
	 */
	private String sourceFeeds;

	/**
	 * 来源频道 (种子) 种子url
	 */
	private String sourceFeedsUrl;

	/**
	 * 频道
	 */
	private String channel;

	/**
	 * 爬取到的keywords {v1:[{str:"吴奇隆"},{str:"刘诗诗"}],v2:[{str:"婚礼"},{str:"裸婚"}]}
	 */
	private String keywords;

	/**
	 * 爬虫基于该字段来爬取内容，是一个唯一的网址，会根据此生成uuid
	 */
	private String link;

	/**
	 * 资讯标题
	 */
	private String title;

	/**
	 * 资讯正文
	 */
	private String content;

	/**
	 * 资讯简介
	 */
	private String summary;

	/**
	 * 详情页内容包含所有图片、简介(通过爬虫爬去过来的原图) [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"}]
	 */
	private String bodyImages;

	/**
	 * 列表页(引导图小图集合 文件名标明图片尺寸,爬虫负责裁图) [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width:450,height:275,format:"jpg"}]
	 */
	private String listImages;

	/**
	 * 瀑布流里面的首图url
	 */
	private String fallImage;

	/**
	 * 可以展示的图片（可以支持多张） [{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width=450,height=275,format:""},{src:
	 * "http://img.lem88.com/flyshare/upload/spiderimg3/20160317/153516-8655-0.jpg?450*275"
	 * ,desc:"",width=450,height=275,format:""}]
	 */
	private String displayListImages;

	/**
	 * 资讯符合展示类型的缩略图集合
	 */
	private String displayThumbnails;
	/**
	 * 曝光的渠道 [{app:\"emui\"},{app:\"coolpad\"}]
	 */
	private String firmApp;

	/**
	 * 根据link生成的item的唯一标识（UUID5: URL_NAMESPACE）
	 */
	private String itemUuid;

	/**
	 * 由爬取到source, source_feeds mapping过来的分类
	 * {v1:[{category:12},{category:2}],v2:[{category:12},{category:2}]}
	 */
	private String categories;

	/**
	 * 正文的分词结果
	 */
	private String contentSegments;

	/**
	 * 地域属性(confidence取值[0-1]）
	 * [{country:"中国",province:"广东省",city:"深圳市",confidence:0.8}]
	 */
	private String local;

	/**
	 * bit 0 (0x01) webview 外链（新闻源链页） bit 1 (0x02) native 新闻落地页 bit 2 (0x04)
	 * 播放视频 bit 3 (0x08) 流量双记 bit 4 (0x10) 外部浏览器打开 bit 5 (0x20) 专题打开 bit 6
	 * (0x40) 无动作 bit 7 (0x80) 列表页播放、客户端解析视频url bit 8 (0x100) 图集详情页打开 bit 9
	 * (0x200) 详情页播放、客户端解析视频url
	 */
	private int linkType;
	/**
	 * 圖片展示支持模式 bit 0 (0x01) 无图，纯文字（必须有文字） bit 1 (0x02) 单张缩略图（图片数大于等于1） bit 2
	 * (0x04) 三图（图片数量大于等于3张） bit 3 (0x08) 通栏（需规定分辨率，符合分辨率的图片数量大于等于1） bit 4
	 * (0x10) 多图文章（图片数量大于等于3张） bit 5 (0x20) 瀑布流 bit 6 (0x40) 图集（图片数量大于等于6张） bit
	 * 7 (0x80) banner(轮播),banner位置，多张图片能自动循环播放（需规定分辨率，符合分辨率的图片数量大于等于1） bit 8
	 * (0x100) 文字投票卡片 bit 9(0x200) 两图投票卡片 bit 10(0x400)
	 * 单张大图（需规定分辨率，符合分辨率的图片数量大于等于1） bit 11(0x800) 单张GIF bit 12(0x1000) 单条视频 ex:
	 * 資訊圖片有3張 => 0x16 (bit 1, bit 2, bit 4), 以此類推
	 */
	private int displayType;
	/**
	 * 媒体编码 该编码只用于日志打印，不保存数据库
	 */
	private String pubCode;
	/**
	 * for scoring facebook fans article.
	 */
	private int rate = 0;

	// 资讯状态,默认状态下为1
	private int state = STATE_NORMAL;

	// 下架状态码
	private int offshelfCode;

	// 下架原因
	private String offshelfReason;

	// 爬虫类型
	private String crawlerType;

	private String flag;
	/**
	 * 漫画分类
	 */
	private String categoriesComic;

	private String youtubeCategory;

	/**
	 * 文章 GIF MP4
	 */
	private String[] bodyGIFs;

	public Content() {
	}

	public Content(Article article) {
		if (article != null) {
			this.setContentId(article.getContentId());
			this.setType(article.getType());
			this.setPublisher(article.getPublisher());
			this.setSourceType(article.getSourceType());
			this.setSource(article.getSource());
			this.setAuthor(article.getAuthor());
			this.setLanguage(article.getLanguage());
			this.setCountry(article.getCountry());
			this.setPublishTime(article.getPublishTime() == null ? new Date() : article.getPublishTime());
			this.setDiscoveryTime(article.getDiscoveryTime() == null ? new Date() : article.getDiscoveryTime());
			this.setFetchTime(article.getFetchTime() == null ? new Date() : article.getFetchTime());
			this.setSourceItemId(article.getSourceItemId());
			this.setDuration(article.getDuration());
			this.setCopyright(article.getCopyright());
			this.setHasCopyright(article.getHasCopyright());
			this.setBodyImagesCount(article.getBodyImagesCount());
			this.setListImagesCount(article.getListImagesCount());
			this.setPublisherPagerankScore(article.getPublisherPagerankScore());
			this.setSourceComment(article.getSourceComment());
			this.setSourceCommentCount(article.getSourceCommentCount());
			this.setSourceImpressionCount(
					article.getSourceImpressionCount() == null ? 0 : article.getSourceImpressionCount());
			this.setSourceLikeCount(article.getSourceLikeCount() == null ? 0 : article.getSourceLikeCount());
			this.setIsOpenComment(article.getIsOpenComment());
			this.setCommentCount(article.getCommentCount());
			this.setShareCount(article.getShareCount());
			this.setThumbupCount(article.getThumbupCount());
			this.setThumbdownCount(article.getThumbdownCount());
			this.setCpVersion(article.getCpVersion());
			this.setSourceFeeds(article.getSourceFeeds());
			this.setSourceFeedsUrl(article.getSourceFeedsUrl());
			this.setKeywords(getObjectToJson(article.getKeywords(), new HashMap()));
			this.setLink(article.getLink());
			this.setTitle(article.getTitle());
			this.setContent(article.getContent());
			this.setSummary(article.getSummary());
			this.setBodyImages(getObjectToJson(article.getBodyImages()));
			this.setListImages(getObjectToJson(article.getListImages()));
			this.setFallImage(article.getFallImage());
			this.setDisplayListImages(getObjectToJson(article.getDisplayListImages()));
			this.setFirmApp(article.getFirmApp());
			this.setItemUuid(article.getItemUuid());
			this.setCategories(getObjectToJson(article.getCategories(), new HashMap()));
			this.setContentSegments(article.getContentSegments());
			this.setLocal(getObjectToJson(article.getLocal()));
			this.setPubCode(article.getPubCode());
			this.setChannel(article.getChannel());
			this.setRate(article.getRate());
			this.setCrawlerType(article.getCrawlerType());
			this.setCategoriesComic(article.getCategoriesComic());
			this.setYoutubeCategory(article.getYoutubeCategory());
			this.setBodyGIFs(article.getBodyGIFs());
		}
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	/**
	 * 根据Object转换成JSON，存储到数据库 Description:
	 *
	 * @author liyuanyi DateTime 2016年3月20日 下午4:44:29
	 * @param o
	 * @return
	 */
	public String getObjectToJson(Object o) {
		return getObjectToJson(o, new ArrayList());
	}

	public String getObjectToJson(Object o, Object defaultValue) {
		if (o == null)
			return NotMapModelBuilder.getInstance().toJson(defaultValue);
		return NotMapModelBuilder.getInstance().toJson(o);
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

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getBodyImages() {
		return bodyImages;
	}

	public void setBodyImages(String bodyImages) {
		this.bodyImages = bodyImages;
	}

	public String getListImages() {
		return listImages;
	}

	public void setListImages(String listImages) {
		this.listImages = listImages;
	}

	public String getDisplayListImages() {
		return displayListImages;
	}

	public void setDisplayListImages(String displayListImages) {
		this.displayListImages = displayListImages;
	}

	public String getDisplayThumbnails() {
		return displayThumbnails;
	}

	public void setDisplayThumbnails(String _displayThumbnails) {
		displayThumbnails = _displayThumbnails;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
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

	public String getFallImage() {
		return fallImage;
	}

	public void setFallImage(String fallImage) {
		this.fallImage = fallImage;
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

	public String getContentSegments() {
		return contentSegments;
	}

	public void setContentSegments(String contentSegments) {
		this.contentSegments = contentSegments;
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
		this.type = (type == null) ? String.valueOf(CONTENT_TYPE_GENERAL) : type.trim();
		int _contentType = (int) (1 << Integer.parseInt(type));
		// added by Genix.Li@2016/08/11, make type & contentType synced.
		if (contentType != _contentType) {
			setContentType(_contentType);
		}
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int _contentType) {
		contentType = _contentType;
		// added by Genix.Li@2016/08/11, make type & contentType synced.
		String _type = determineType(contentType);
		if (!_type.equals(type)) {
			setType(_type);
		}
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

	public Integer getSourceLikeCount() {
		return sourceLikeCount;
	}

	public Integer getSourceImpressionCount() {
		return sourceImpressionCount;
	}

	public void setSourceCommentCount(Integer sourceCommentCount) {
		this.sourceCommentCount = sourceCommentCount;
	}

	public void setSourceLikeCount(Integer sourceLikeCount) {
		this.sourceLikeCount = sourceLikeCount;
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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getCurrentMonthTableName() {
		return currentMonthTableName;
	}

	public void setCurrentMonthTableName(String currentMonthTableName) {
		this.currentMonthTableName = currentMonthTableName;
	}

	public int getLinkType() {
		return linkType;
	}

	public void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	public HashSet<String> getHsFallImageIdBlacklist() {
		return hsFallImageIdBlacklist;
	}

	public void setHsFallImageIdBlacklist(HashSet<String> hsFallImageIdBlacklist) {
		this.hsFallImageIdBlacklist = hsFallImageIdBlacklist;
	}

	public int getDisplayType() {
		return displayType;
	}

	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}

	public int getOffshelfCode() {
		return offshelfCode;
	}

	public void setOffshelfCode(int offshelfCode) {
		this.offshelfCode = offshelfCode;
	}

	public String getOffshelfReason() {
		return offshelfReason;
	}

	public void setOffshelfReason(String offshelfReason) {
		this.offshelfReason = offshelfReason;
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

	public void determineLinkType() {
		this.linkType = determineLinkType(contentId, sourceType, source, sourceFeeds, channel);
	}

	public void determineDisplayType() {
		HashMap mContent = new HashMap();
		mContent.put("title", title);
		mContent.put("content", content);
		mContent.put("type", type);
		mContent.put("fallImage", fallImage);
		mContent.put("listImages", getListImagesArray());
		mContent.put("bodyImages", getBodyImagesArray());
		mContent.put("contentType", this.contentType);
		mContent.put("categories", getCategories());
		mContent.put("bodyGIFs", this.bodyGIFs);
		mContent.put("linkType", this.linkType);
		LOG.debug("determineDisplayType fallImage:" + fallImage + ",link:" + link);

		HashMap mResult = determineDisplayType(mContent);
		LOG.debug("determineDisplayType mResult:" + mResult + ",link:" + link);
		this.displayType = ((Integer) mResult.get("displayType")).intValue();
		if (null != mResult.get("fallImage"))
			this.fallImage = (String) mResult.get("fallImage");
		if (null != mResult.get("thumbnail"))
			this.displayThumbnails = (String) mResult.get("thumbnail");
	}

	public static String extractImageId(String imageLink) {
		String id = null;
		try {
			List<NameValuePair> alQuery = URLEncodedUtils.parse(new java.net.URI(imageLink),
					java.nio.charset.Charset.defaultCharset().toString());
			for (NameValuePair param : alQuery) {
				if ("id".equalsIgnoreCase(param.getName()))
					id = param.getValue();
			}
		} catch (Exception e) {
			LOG.error("extractImageId from imageLink has exception:" + e.getMessage());
		}
		return id;
	}

	/*
	 * public boolean cleanFallImage(String fallImage) {
	 * if(StringUtils.isBlank(fallImage)) return false; boolean clean = false;
	 * HashSet<String> hsIdBlacklist = getHsFallImageIdBlacklist();
	 * if(!StringUtils.isEmpty(fallImage)) { String id =
	 * extractImageId(fallImage); if(!StringUtils.isEmpty(id) &&
	 * hsIdBlacklist.contains(id)) { LOG.debug("fallImage in black list:" +
	 * fallImage); return true; } } return clean; }
	 */

	public static HashSet<String> getFallImageIds() {
		HashSet<String> hsIdBlacklist = new HashSet<String>();
		if (!StringUtils.isEmpty(Constants.BLACKLIST_FALLIMAGE_ID)) {
			String[] idBlackList = Constants.BLACKLIST_FALLIMAGE_ID.split(",");
			for (int i = 0; i < idBlackList.length; i++) {
				hsIdBlacklist.add(idBlackList[i]);
			}
		}

		LOG.debug("blacklist_id size:" + hsIdBlacklist.size());
		return hsIdBlacklist;
	}

	/**
	 * 列表页小缩略图的经过人脸检测的条件: 若资讯内图片数量 < 3, 则取第一个 图片宽>150px, 高>106px 的图片去做人脸检测；
	 * 若资讯内图片数量 >=3, 则取前三个 图片宽>150px, 高>106px 的图片去做人脸检测。 通栏的图片经过人脸检测的条件：
	 * 资讯内的图片数量必须>=1, 取第一个宽>250px, 高>125px 的图片去做人脸检测
	 */
	public void addFaceDetection() {
		LOG.info(">> [addFaceDetection] content_id=" + contentId);
		// bodyImages->修改为listImages
		try {
			ArrayList alBodyImage = (listImages == null) ? new ArrayList()
					: (ArrayList) inveno.spider.common.utils.JsonUtils
							.toJavaObject(NotMapModelBuilder.getInstance().build(listImages, JsonElement.class));
			ArrayList<String> alFaceDetectId = new ArrayList<String>();
			if ((displayType & 0x02) > 0 && alBodyImage.size() < 3) {
				for (int i = 0; i < alBodyImage.size(); i++) {
					HashMap mBodyImage = (HashMap) alBodyImage.get(i);
					int width = (null == mBodyImage.get("width")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("width")));
					int height = (null == mBodyImage.get("height")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("height")));
					if (width > 150 && height > 106) {
						String id = extractImageId((String) mBodyImage.get("src"));
						LOG.info(">> [addFaceDetection] content_id=" + contentId + "\tmatched src="
								+ (String) mBodyImage.get("src") + "\tid=" + id);
						if (id != null) {
							alFaceDetectId.add(id);
							break;
						}
					}
				}
			} else if ((displayType & 0x02) > 0 && alBodyImage.size() >= 3) {
				int nDetect = 0;
				for (int i = 0; i < alBodyImage.size(); i++) {
					if (nDetect >= 3)
						break;

					HashMap mBodyImage = (HashMap) alBodyImage.get(i);
					int width = (null == mBodyImage.get("width")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("width")));
					int height = (null == mBodyImage.get("height")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("height")));
					if (width > 150 && height > 106) {
						String id = extractImageId((String) mBodyImage.get("src"));
						LOG.info(">> [addFaceDetection] content_id=" + contentId + "\tmatched src="
								+ (String) mBodyImage.get("src") + "\tid=" + id);
						if (id != null) {
							alFaceDetectId.add(id);
							nDetect++;
						}
					}
				}
			} else if ((displayType & 0x08) > 0 && alBodyImage.size() >= 1) {
				for (int i = 0; i < alBodyImage.size(); i++) {
					HashMap mBodyImage = (HashMap) alBodyImage.get(i);
					int width = (null == mBodyImage.get("width")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("width")));
					int height = (null == mBodyImage.get("height")) ? 0
							: Integer.parseInt(String.valueOf(mBodyImage.get("height")));
					if (width > 250 && height > 125) {
						String id = extractImageId((String) mBodyImage.get("src"));
						LOG.info(">> [addFaceDetection] content_id=" + contentId + "\tmatched src="
								+ (String) mBodyImage.get("src") + "\tid=" + id);
						if (id != null) {
							alFaceDetectId.add(id);
							break;
						}
					}
				}
			}

			// add fallimage face detection
			if (StringUtils.isNotEmpty(fallImage)) {
				String id = extractImageId(fallImage);
				if (id != null) {
					alFaceDetectId.add(id);
				}

			}
			if (alFaceDetectId.size() > 0) {
				for (String id : alFaceDetectId) {
					try {
						String response = ImageCloudHelper.getInstance()
								.addFaceDetection(Constants.IMAGE_CLOUD_ACCESS_APP, id);
						LOG.info("[addFaceDetection] content_id=" + contentId + "\timage_id=" + id + "\tresponse="
								+ response);
					} catch (Exception e) {
						LOG.error("[addFaceDetection] content_id=" + contentId, e);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("[addFaceDetection]", e);
		}
		LOG.info("<< [addFaceDetection] content_id=" + contentId);
	}

	public ArrayList getBodyImagesArray() {
		return (bodyImages == null) ? new ArrayList()
				: (ArrayList) inveno.spider.common.utils.JsonUtils
						.toJavaObject(NotMapModelBuilder.getInstance().build(bodyImages, JsonElement.class));
	}

	public ArrayList getListImagesArray() {
		return (listImages == null) ? new ArrayList()
				: (ArrayList) inveno.spider.common.utils.JsonUtils
						.toJavaObject(NotMapModelBuilder.getInstance().build(listImages, JsonElement.class));
	}

	public inveno.spider.common.model.SourceFeed getCorrespondingSourceFeedByUrl() {
		return getCorrespondingSourceFeed(contentId, sourceType, sourceFeedsUrl);
	}

	public inveno.spider.common.model.SourceFeed getCorrespondingSourceFeed() {
		return getCorrespondingSourceFeed(contentId, sourceType, source, sourceFeeds, channel);
	}

	public static String determineType(int contentType) {
		return String.valueOf((int) (Math.log(contentType) / Math.log(2)));
	}

	public static int determineContentType(String contentId, String sourceType, String source, String sourceFeeds,
			String channel) {
		int _contentType = (1 << CONTENT_TYPE_GENERAL);

		inveno.spider.common.model.SourceFeed objSource = getCorrespondingSourceFeed(contentId, sourceType, source,
				sourceFeeds,channel);
		if (objSource != null) {
			_contentType = objSource.getContentType();
		}

		return _contentType;
	}

	public static int determineContentTypeBySourceFeedsUrl(String contentId, String sourceType, String source, String sourceFeedsUrl){
		int _contentType = (1 << CONTENT_TYPE_GENERAL);

		inveno.spider.common.model.SourceFeed objSource = getCorrespondingSourceFeed(contentId, sourceType,
				sourceFeedsUrl);
		if (objSource != null) {
			_contentType = objSource.getContentType();
		}

		return _contentType;
	}

	public static int determineLinkType(String contentId, String sourceType, String source, String sourceFeeds,
			String channel) {
		int _linkType = 0x02;

		inveno.spider.common.model.SourceFeed objSource = getCorrespondingSourceFeed(contentId, sourceType, source,
				sourceFeeds, channel);
		if (objSource != null) {
			_linkType = objSource.getLinkType();
		}

		return _linkType;
	}

	public static int determineLinkTypeBySourceFeedsUrl(String contentId, String sourceType, String source, String sourceFeedsUrl) {
		int _linkType = 0x02;

		inveno.spider.common.model.SourceFeed objSource = getCorrespondingSourceFeed(contentId, sourceType,
				sourceFeedsUrl);
		if (objSource != null) {
			_linkType = objSource.getLinkType();
		}

		return _linkType;
	}

	/**
	 * 圖片展示支持模式 bit 0 (0x00000001) 无图，纯文字（必须有正文，标题） bit 1 (0x00000002)
	 * 单张缩略图（列表页图片数大于等于1）（图片宽度大于240px， 1/2 < 宽/高 < 3） bit 2 (0x00000004)
	 * 三图（列表页图片数量大于等于3张） （图片宽度大于200px， 1/2 < 宽/高 < 4） bit 3 (0x00000008)
	 * 通栏（需规定分辨率，图片宽度大于600px，5/3<=宽/高<=14/5，符合分辨率条件的图片数量大于等于1，将此图作为fall_image）
	 * bit 4 (0x00000010) 多图文章（列表页图片数量大于等于3张）（图片宽度大于200px， 1/2 < 宽/高 < 4） bit 5
	 * (0x00000020) 瀑布流（必须有文字，标题，图片宽度大于240px,1/2<宽/高<3，满足要求图片作为fall_image） bit 6
	 * (0x00000040) 图集（详情页图片数量大于等于6张）（图片宽度大于200px， 1/2 < 宽/高 < 4） bit 7
	 * (0x00000080)
	 * banner(轮播),banner位置，多个资讯聚合在一起，能自动循环播放（需规定分辨率，每个资讯缩略图符合分辨率要求，宽大于等于400px，5/
	 * 3<=宽/高<=14/5，满足要求图片作为fall_image） bit 8 (0x00000100) 文字投票卡片 bit 9
	 * (0x00000200) 两图投票卡片 bit 10(0x00000400) 单张大图（需规定分辨率，图片宽度大于400px， 1/6 < 宽/
	 * 高 < 4 ） bit 11(0x00000800) 单张GIF（gif格式图片数量大于等于1） bit 12(0x00001000) 单条视频
	 * bit 13(0x00002000) 开屏展示方式 bit 14(0x00004000) 图文多功能按钮。 bit 15(0x00008000)
	 * 插屏弹窗展示方式。 ex: 資訊圖片有3張 => 0x16 (bit 1, bit 2, bit 4), 以此類推
	 */
	public static HashMap determineDisplayType(Map mContent) {
		String title = mContent.get("title") != null ? (String) mContent.get("title") : null;
		String content = mContent.get("content") != null ? (String) mContent.get("content") : null;
		String fallImage = mContent.get("fallImage") != null ? (String) mContent.get("fallImage") : null;
		String type = mContent.get("type") != null ? String.valueOf(mContent.get("type")) : null;
		String categories = mContent.get("categories") != null ? String.valueOf(mContent.get("categories")): null;
		int contentType = mContent.get("contentType") != null ? (Integer) mContent.get("contentType") : 0;
		ArrayList alListImage = mContent.get("listImages") != null ? (ArrayList) mContent.get("listImages")
				: new ArrayList();
		ArrayList alBodyImage = mContent.get("bodyImages") != null ? (ArrayList) mContent.get("bodyImages")
				: new ArrayList();

		String[] bodyGIFs = mContent.get("bodyGIFs") != null ? (String[]) mContent.get("bodyGIFs"): null;
		int linkType = mContent.get("linkType") != null ? (Integer) mContent.get("linkType") : 0;


		// add by Symon@2016-12-6 11:44:19，对fallImage进行黑名单过滤
		// if(cleanFallImage(fallImage)) {
		// fallImage = null;
		// }

		HashMap mResult = new HashMap();
		int _displayType = 0x00;
		String _fallImage = null;
		if (StringUtils.isNotBlank(fallImage)) {
			_fallImage = fallImage;
			LOG.debug("_fallImage:" + _fallImage);
		}
		String[] arrAvailableFallImage = new String[4]; // 依序存放符合各資訊的縮略图:
														// 通栏,瀑布流,轮播,单张大图
		// added by Genix.Li@2016/09/29, to save mapping for first thumbnail
		// each display_type.
		HashMap<Integer, String> mThumbnail = new HashMap<Integer, String>();
		try {
			boolean hasTitle = (title != null && title.length() > 0);
			boolean hasContent = (content != null && content.length() > 0);
			int galleryCount = 0;

			if (hasTitle && hasContent) {
				_displayType |= DISPLAY_TYPE_TEXT;
			}

			if (contentType == 16384) {
				_displayType |= DISPLAY_TYPE_COMICS;

			}
			if (alListImage.size() >= 3) {
				_displayType |= DISPLAY_TYPE_TRIPPLE_PIC;
				// modify by Symon@2017-1-4 14:55:53，关闭多图
				// _displayType |= DISPLAY_TYPE_MULTIPLE_PIC;
			}

			if (String.valueOf(CONTENT_TYPE_CLIP).equals(type) || String.valueOf(CONTENT_TYPE_VIDEO).equals(type))
				_displayType |= DISPLAY_TYPE_VIDEO | DISPLAY_TYPE_VIDEO_THUMBNAIL;

			// 判斷通欄
			for (int i = 0; i < alBodyImage.size(); i++) {
				HashMap mBodyImage = (HashMap) alBodyImage.get(i);
				String imageLink = (String) mBodyImage.get("src");
				String format = (String) mBodyImage.get("format");
				if (mBodyImage.get("width") != null && mBodyImage.get("height") != null) {
					int width = Integer.parseInt(String.valueOf(mBodyImage.get("width")));
					int height = Integer.parseInt(String.valueOf(mBodyImage.get("height")));
					double ratio = ((double) width / (double) height);
					StringBuilder sb = new StringBuilder();
					sb.append(imageLink);
					if (!imageLink.contains("&size="))
						sb.append("&size=").append(width).append("*").append(height);
					if (!imageLink.contains("&fmt=") && format != null)
						sb.append("&fmt=.").append(format);
					imageLink = sb.toString();

					if (width > 200) {
						if (0.5f <= ratio && ratio <= 4)
							galleryCount++;

						if (width > 240) {
							if (1.f / 2.f < ratio && ratio < 3.f)
								if (contentType == 32 || contentType == 128)
									_fallImage = (_fallImage == null) ? imageLink : _fallImage;
								else if (hasTitle && hasContent) {
									_displayType |= DISPLAY_TYPE_WATERFALL;
									_fallImage = (_fallImage == null) ? imageLink : _fallImage;
									mThumbnail.putIfAbsent(DISPLAY_TYPE_WATERFALL, imageLink);
								}

							if (width >= 400) {
								if (height >= 200 && (5.f / 3.f <= ratio && ratio <= 14.f / 5.f)) {
									_displayType |= DISPLAY_TYPE_SLIDE;
									mThumbnail.putIfAbsent(DISPLAY_TYPE_SLIDE, imageLink);
								}

								if (width > 400) {
									// memes不做lockscreen_news判断
									if (categories != null && categories.matches(".*[^\\d]133[^\\d].*")) {
										if (0.5f < ratio && ratio < 5.f / 3.f) {
											_displayType |= DISPLAY_TYPE_LOCKSCREEN_MEMES;
											mThumbnail.putIfAbsent(DISPLAY_TYPE_LOCKSCREEN_MEMES, imageLink);
										}
									} else if (3.f / 5.f < ratio && ratio < 8.f / 5.f) {
										_displayType |= DISPLAY_TYPE_LOCKSCREEN_NEW;
										mThumbnail.putIfAbsent(DISPLAY_TYPE_LOCKSCREEN_NEW, imageLink);
									}

									if (width >= 480) {
										if (height >= 320 && (1.f / 6.f < ratio && ratio < 4.f)) {
											_displayType |= DISPLAY_TYPE_WALLPAPER;
											mThumbnail.putIfAbsent(DISPLAY_TYPE_WALLPAPER, imageLink);
										}

										if (width >= 600 && (5.f / 3.f <= ratio && ratio <= 14.f / 5.f)) {
											_displayType |= DISPLAY_TYPE_BANNER;
											mThumbnail.putIfAbsent(DISPLAY_TYPE_BANNER, imageLink);
										}
									}

								}

							}
						}
					}
                }

				if (format.equalsIgnoreCase("gif")) {
					_displayType |= DISPLAY_TYPE_ANIMATION;
					mThumbnail.putIfAbsent(DISPLAY_TYPE_ANIMATION, imageLink);
				}
			}

			if ((linkType & LINK_TYPE_GALLERY) > 0 && galleryCount >= 6)
				_displayType |= DISPLAY_TYPE_GALLERY;

			// modify by Symon@2016-12-26
			// 15:56:51,因为处理bodyImage的时候会对fallImage进行处理，所以将处理顺序调整下(从在处理bodyImage之前调整到处理完bodyImage之后)
			if (alListImage.size() > 0) {
				_displayType |= DISPLAY_TYPE_SINGLE_PIC;
				mThumbnail.putIfAbsent(DISPLAY_TYPE_SINGLE_PIC, (String) ((HashMap) alBodyImage.get(0)).get("src"));
			}else if(StringUtils.isNotEmpty(_fallImage)){
				_displayType |= DISPLAY_TYPE_SINGLE_PIC;
				mThumbnail.putIfAbsent(DISPLAY_TYPE_SINGLE_PIC, _fallImage);
			}

			// gif的display_type只取 0x00080000=mpeg4；0x00000800=单张GIF；
			if (String.valueOf(CONTENT_TYPE_ANIMATION).equals(type)) {
				if(bodyGIFs != null && bodyGIFs.length > 0) {
					_displayType |= DISPLAY_TYPE_GIF_MPEG4;
					mThumbnail.putIfAbsent(DISPLAY_TYPE_GIF_MPEG4, bodyGIFs[0]);
				}

				int[] keys = new int[]{DISPLAY_TYPE_GIF_MPEG4, DISPLAY_TYPE_ANIMATION};

				_displayType &= Arrays.stream(keys).reduce(0, (a, b) -> a | b);
//				_displayType |= Arrays.stream(keys).reduce(0, (a, b) -> a | b);
				HashMap<Integer, String> _mThumbnail = new HashMap<Integer, String>();
				for (int key : keys)
					if (mThumbnail.containsKey(key))
						_mThumbnail.put(key, mThumbnail.get(key));
				mThumbnail = _mThumbnail;
			}

			// memes的display_type只取 0x00000400=big_image；0x00040000=lockscreen_memes；
			if (categories != null && categories.matches(".*[^\\d]133[^\\d].*")) {
				int[] keys = new int[]{DISPLAY_TYPE_WALLPAPER, DISPLAY_TYPE_LOCKSCREEN_MEMES};

				_displayType &= Arrays.stream(keys).reduce(0, (a, b) -> a | b);
				HashMap<Integer, String> _mThumbnail = new HashMap<Integer, String>();
				for (int key : keys)
					if (mThumbnail.containsKey(key))
						_mThumbnail.put(key, mThumbnail.get(key));
				mThumbnail = _mThumbnail;
			}

		} catch (Exception e) {
			LOG.error("determineDisplayType title:" + title + " has exception:", e);
		}

		mResult.put("displayType", _displayType);
		mResult.put("fallImage", _fallImage);
		mResult.put("thumbnail", NotMapModelBuilder.getInstance().toJson(mThumbnail));
		return mResult;
	}

	public static inveno.spider.common.model.SourceFeed getCorrespondingSourceFeed(String contentId, String sourceType,
			String sourceFeedsUrl) {
		inveno.spider.common.model.SourceFeed objSource = null;
		try {
			inveno.spider.common.dao.SourceFeedDao dao = new inveno.spider.common.dao.impl.SourceFeedDaoImpl();
			LOG.info("[getCorrespondingSourceFeed] content_id=" + contentId + "\tsourceType=" + sourceType
					+ "\tsourceFeedsUrl=" + sourceFeedsUrl);
			long startTime = System.currentTimeMillis();
			objSource = dao.querySourceFeedByUrl(sourceType, sourceFeedsUrl);
			long endTime = System.currentTimeMillis();
			LOG.info("getCorrespondingSourceFeed by (sourceType,sourceFeedsUrl) used " + (endTime - startTime)
					+ " millis.");
		} catch (Exception e) {
			LOG.error("[getCorrespondingSourceFeed]", e);
		}
		return objSource;
	}

	public static inveno.spider.common.model.SourceFeed getCorrespondingSourceFeed(String contentId, String sourceType,
			String source, String sourceFeeds, String channel) {
		inveno.spider.common.model.SourceFeed objSource = null;
		try {
			inveno.spider.common.dao.SourceFeedDao dao = new inveno.spider.common.dao.impl.SourceFeedDaoImpl();
			LOG.info("[getCorrespondingSourceFeed] content_id=" + contentId + "\tsourceType=" + sourceType + "\tsource="
					+ source + "\tsourceFeeds=" + sourceFeeds + "\tchannel=" + channel);
			long startTime = System.currentTimeMillis();
			objSource = dao.querySourceFeed(sourceType, source, sourceFeeds, channel);
			long endTime = System.currentTimeMillis();
			LOG.info("getCorrespondingSourceFeed by (sourceType,source,sourceFeedsUrl,channel) used "
					+ (endTime - startTime) + " millis.");
		} catch (Exception e) {
			LOG.error("[getCorrespondingSourceFeed]", e);
		}
		return objSource;
	}

	public ContentExtend getContentExtend() {
		return contentExtend;
	}

	public void setContentExtend(ContentExtend contentExtend) {
		this.contentExtend = contentExtend;
	}

	@Override
	public String toString() {
		// return (new Gson()).toJson(this, Content.class);
		// modify by Symon at 2017年3月31日11:06:00，防止特殊字符被gson自动转成unicode
		return NotMapModelBuilder.getInstance().toJson(this, Content.class);
	}

	public ArrayList<String> getBlockquoteList() {
		return blockquoteList;
	}

	public void setBlockquoteList(ArrayList<String> blockquoteList) {
		this.blockquoteList = blockquoteList;
	}

	public static void main(String[] args) {
		Content c = new Content();
		c.setType("5");
		c.setBodyGIFs(new String[]{"http://cloud.hotoday.in/2018/05/07/59c52f1e6aaba1162aae7cc3a9e55c1a.mp4?id=59c52f1e6aaba1162aae7cc3a9e55c1a&duration=1.44&size=320x240&fmt=.mp4"});
		c.setBodyImages("[{\"src\":\"http://cloudimg.hotoday.in/v1/icon?id=12662723979730698416&size=245*150&fmt=.gif\",\"format\":\"gif\",\"width\":245,\"height\":150,\"desc\":\"\"}]");


		c.determineDisplayType();

		System.out.println(c.displayType);
		System.out.println(c.displayThumbnails);
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

	public void setBodyGIFs(String[] bodyGIFs) {
		this.bodyGIFs = bodyGIFs;
	}
	public String[] getBodyGIFs() {
		return bodyGIFs;
	}
}
