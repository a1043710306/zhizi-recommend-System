package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 频道资讯信息实体类
 * 
 * @author liyuanyi
 * @date 2014-11-14
 * 
 */
// @Entity
// @Table(name = "s_channel_content")
public class ChannelContent implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	public ChannelContent() {

	}


	/** 主键 */
	// @Id
	// @Column(name = "id")
	// @GeneratedValue(generator = "ccGenerate", strategy =
	// GenerationType.IDENTITY)
	private int id;

	/** 创建时间 */
	// @Column(name = "create_time")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	// /** 图片路径 */
	// @Column(name = "img_url")
	private String imgUrl;

	// /** 缩略图片路径 */
	// @Column(name = "cut_img_url")
	private String cutImgUrl;

	// /** 标题 */
	// @Column(name = "title")
	private String title;

	// /** 推送标题 */
	// @Column(name = "push_title")
	private String pushTitle;

	// /** 简介 */
	// @Column(name = "intro")
	private String intro;

	// /** 内容 */
	// @Column(name = "content")
	private String content;

	// /** 频道类型ID */
	// @Column(name = "type_code")
	private int typeCode;

	// /** 预发布日期 */
	// @Column(name = "start_time")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	// /** 新闻来源 */
	// @Column(name = "source")
	private String source;

	// /**客户端显示来源*/
	// @Column(name = "show_source")
	private String showSource;

	// /** 跳出时，信息指向路径 */
	// @Column(name = "url")
	private String url;

	// /** 状态 */
	// @Column(name = "state")
	private int state;

	// /** 是否跳出，0、不跳出，1、跳出 */
	// @Column(name = "open_out")
	private int openOut;

	// /** 当是专版的子资讯时，其父资讯的ID */
	// @Column(name = "parent_id")
	private String parentId;

	// /** rss信息ID */
	// @Column(name = "rss_id")
	private int rssId;

	// /** 是否是专题，0、不是，1、是 */
	// @Column(name = "is_special")
	private int special;

	// /** 地址适配 **/
	// @Column(name = "address")
	private String address;

	// /** 信息原始发布时间 */
	// @Column(name = "original_time")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date originalTime;

	// /** 在Flyshare的发布时间 */
	// @Column(name = "release_time")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date releaseTime;

	// /** 查看原文的地址 */
	// @Column(name = "original_url")
	private String originalUrl;

	// /** 是否是推送资讯，0、不是，1、是 */
	// @Column(name = "is_push")
	private int push;

	// /** 频道 */
	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn(name = "type_code", referencedColumnName = "type_code",
	// insertable = false, updatable = false)
	// private Channel channel;

	// /**根据typeCode获取对应的name(xuanjunren) */
	// public String getChannelsName(){
	// return
	// ServiceFacade.getConfigService().findChanlNameByCode(Integer.valueOf(typeCode));
	// }

	// /** 资讯的无间隔排序 */
	// @Column(name = "channel_index")
	// private int channelIndex;

	// /** 标示资讯是否重要 0代表不重要 1代表重要 */
	// @Column(name = "is_major")
	private int major;

	// /** 标示资讯图片是为重新录入 0代表非重新录入 1代表重新录入 */
	// @Column(name = "is_reentry")
	private int reEntry;

	// /** 标示老的专题是否有更新 0代表没有更新，1代表有更新，2代表新的资讯已经发布 */
	// @Column(name = "is_update_special")
	private int updateSpecial;

	// /**推送开始时间 */
	// @Column(name = "pushtime")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date pushTime;

	// /**推送结束时间 */
	// @Column(name = "pushtime_end")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date pushTimeEnd;

	// /**对已经发布的资讯进行修改，做时间标示*/
	// @Column(name = "update_time")
	// @Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;

	// /** 资讯评论功能状态 0代表打开 1代表关闭 */
	// @Column(name = "is_open_comment")
	private String isOpenComment;
	public static final int COMMENT_ALLOW  = 0;
	public static final int COMMENT_REFUSE = 1;

	// /** 是否推荐widget展示 2不展示 1展示 0：符合widget展示但还没被推荐*/
	// @Column(name = "is_widget_show")
	private String isWidgetShow;

	// /** 资讯内容是否有图 2没有图 1有图 */
	// @Column(name = "is_have_img")
	private String isHaveImg;
	// /** 城市id */
	// @Column(name = "city_id")
	private int cityId;

	// /** t_market_source中的广告id xjr 2013-7-3*/
	// @Column(name = "market_id")
	private int marketId;

	// /** t_market_source中的广告引导标题 xjr 2013-7-3*/
	// @Column(name = "market_name")
	private String marketName;

	// /** t_market_source中的广告标题 xjr 2013-7-3*/
	// @Column(name = "market_title")
	private String marketTitle;

	// /**这个资讯的属于哪一个客户*/
	// @Column(name = "user_id")
	private String userId;

	// //第三方资讯的Id，如腾讯新闻，搜狐新闻等等
	// @Column(name = "news_id")
	private String newsId;

	// //来自某个新闻的客户端;如腾讯新闻客户端
	// @Column(name = "from_apk")
	private String fromApk;

	// //文章的类型：0表示普通信息 1表示组图信息（腾讯资讯的表示）
	// @Column(name = "article_type")
	private String articletype;

	// //资讯的摘要
	// @Column(name = "summary")
	private String summary;

	// //表示资讯是否是动态API获取的 0表示不是动态Api 1表示是动态api的
	// @Column(name = "isapi")
	private int isApi;

	// //合作者的资讯类型
	// @Column(name = "subtype")
	private String subtype;

	// *********************合库加的字段 liyuanyi 2014-1-13*************************//
	// 废弃资讯类型，1：为普通资讯。2：为视频。3：为音频
	// @Column(name = "cp_type")
	private String cpType;

	/** 内容数据类型 116 1 资讯 2 视频 3 音频 */
	// @Column(name = "datatype")
	private String datatype;

	/** 客户端参数 */
	// @Column(name = "arg")
	private String arg;

	/** 热点 **/
	// @Column(name = "hot")
	private byte hot;

	// 内容合作商分类
	// @Column(name = "sub_type")
	private String subType;


	/** 是否为头条 **/
	// @Column(name = "is_head")
	private byte isHead;

	/** 资讯分数 **/
	// @Column(name = "score")
	private int score;

	/** 标签 **/
	private String tags;

	/** 地域标签 **/
	private String locations;

	/**
	 * 资讯类型：0：普通资讯。1：头条资讯。2：精编资讯
	 */
	private int infoType;

	/**
	 * 回滚资讯标识。0：不回滚。1：回滚。
	 */
	private int rollBack;

	/**
	 * 关键词
	 */
	private String keyWord;

	/** 本资讯正文含图数量 **/
	protected int imgCount;

	/** 是否设置通栏，默认为0。 1:通栏 **/
	private int banner;

	/** 该咨询需要推送的渠道 **/
	private String firmName;

	// 是否强制发布
	private int isforceRelase;

	// 标签ID
	private int tagsId;

	// 推送的版本 高中低
	private String pushVersion;

	// 分类名字
	private String categoryName;

	private int isOriginal;

	private String areaIds;

	private String areadtos;

	private String isSuccess;
	private int exposure;

	private String pushJson;

	private String templateJson;

	private String strategyJson;
	
	private String strategyJson2;
	
	private int activeFlag;
	
	private String version1;
	
	private String version2;
	
	private String jslog;

	private int second;

	public int getSecond()
	{
		return second;
	}

	public void setSecond(int _second)
	{
		second = _second;
	}

	public String getStrategyJson2() {
		return strategyJson2;
	}

	public void setStrategyJson2(String strategyJson2) {
		this.strategyJson2 = strategyJson2;
	}

	public String getTemplateJson() {
		return templateJson;
	}

	public void setTemplateJson(String templateJson) {
		this.templateJson = templateJson;
	}

	public String getStrategyJson() {
		return strategyJson;
	}

	public void setStrategyJson(String strategyJson) {
		this.strategyJson = strategyJson;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getPushVersion() {
		return pushVersion;
	}

	public void setPushVersion(String pushVersion) {
		this.pushVersion = pushVersion;
	}

	public int getTagsId() {
		return tagsId;
	}

	public void setTagsId(int tagsId) {
		this.tagsId = tagsId;
	}

	public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public int getIsforceRelase() {
		return isforceRelase;
	}

	public void setIsforceRelase(int isforceRelase) {
		this.isforceRelase = isforceRelase;
	}

	public int getBanner() {
		return banner;
	}

	public void setBanner(int banner) {
		this.banner = banner;
	}

	public String getLocations() {
		return locations;
	}

	public void setLocations(String locations) {
		this.locations = locations;
	}

	public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public int getRollBack() {
		return rollBack;
	}

	public void setRollBack(int rollBack) {
		this.rollBack = rollBack;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public int getImgCount() {
		return imgCount;
	}

	public void setImgCount(int imgCount) {
		this.imgCount = imgCount;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public byte getIsHead() {
		return isHead;
	}

	public void setIsHead(byte isHead) {
		this.isHead = isHead;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public byte getHot() {
		return hot;
	}

	public void setHot(byte hot) {
		this.hot = hot;
	}

	public int getMarketId() {
		return marketId;
	}

	public void setMarketId(int marketId) {
		this.marketId = marketId;
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getMarketTitle() {
		return marketTitle;
	}

	public void setMarketTitle(String marketTitle) {
		this.marketTitle = marketTitle;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getIsWidgetShow() {
		return isWidgetShow;
	}

	public void setIsWidgetShow(String isWidgetShow) {
		this.isWidgetShow = isWidgetShow;
	}

	public Date getPushTime() {
		return pushTime;
	}

	public void setPushTime(Date pushTime) {
		this.pushTime = pushTime;
	}

	/**
	 * 获取推送开始时间字符串
	 * 
	 * @return
	 */
	// public String getPushTimeStr(){
	// return DateUtil.formatDateTime(pushTime);
	// }

	public int getReEntry() {
		return reEntry;
	}

	public void setReEntry(int reEntry) {
		this.reEntry = reEntry;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}


	/** 根据TypeCode得到频道名称 */
	// public String getChanlName() {
	// // return getChannel().getName();
	// return ServiceFacade.getConfigService().findChanlNameByCode(typeCode);
	// }

	/** 根据专题的Id得到，专题下面资讯的数量 */
	// public int getInfoNum() {
	// int infoNum = 0;
	// infoNum = ServiceFacade.getChannelContentServie().findInfoNumBySpeId(
	// getId());
	// return infoNum;
	// }

	/** 根据parentId得到专题的标题 */
	// public String getTitleById() {
	// String speTitle = "";
	// if (StringUtil.isNotEmpty(getParentId())) {
	// speTitle = ServiceFacade.getChannelContentServie()
	// .findSpecialTitleById(getParentId());
	// }
	// return speTitle;
	// }

	/** 得到是否重要的文字描述 */
	// public String getMajorDesc() {
	// String majorDesc = "不重要";
	// if (major == 1) {
	// majorDesc = "重要";
	// }
	// return majorDesc;
	// }

	/** 标示是否为专题添加新资讯 0是没有 其他代表有新资讯 */
	// public int getIsUpdateSp() {
	// return ServiceFacade.getChannelContentServie().checkNewInfoForSpe(
	// getId());
	// }

	// /** 标示资讯内容是否包含图片 0标示内容不包含图片 1标示内容包含图片 */
	// public int getIsHaveImg() {
	// return ServiceFacade.getChannelContentServie().checkHaveImg(getId());
	// }

	public Date getCreateTime() {
		return createTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(int typeCode) {
		this.typeCode = typeCode;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getOpenOut() {
		return openOut;
	}

	public void setOpenOut(int openOut) {
		this.openOut = openOut;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public int getRssId() {
		return rssId;
	}

	public void setRssId(int rssId) {
		this.rssId = rssId;
	}

	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getOriginalTime() {
		return originalTime;
	}

	public void setOriginalTime(Date originalTime) {
		this.originalTime = originalTime;
	}

	public Date getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(Date releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public int getPush() {
		return push;
	}

	public void setPush(int push) {
		this.push = push;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getUpdateSpecial() {
		return updateSpecial;
	}

	public void setUpdateSpecial(int updateSpecial) {
		this.updateSpecial = updateSpecial;
	}

	public String getCutImgUrl() {
		return cutImgUrl;
	}

	public void setCutImgUrl(String cutImgUrl) {
		this.cutImgUrl = cutImgUrl;
	}

	public String getPushTitle() {
		return pushTitle;
	}

	public void setPushTitle(String pushTitle) {
		this.pushTitle = pushTitle;
	}

	public String getShowSource() {
		return showSource;
	}

	public void setShowSource(String showSource) {
		this.showSource = showSource;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getIsOpenComment() {
		return isOpenComment;
	}

	public String getIsOpenCommentDesc() {
		if (String.valueOf(COMMENT_ALLOW).equals(isOpenComment)) {
			isOpenComment = "已打开";
		} else if (String.valueOf(COMMENT_REFUSE).equals(isOpenComment)) {
			isOpenComment = "已关闭";
		}
		return isOpenComment;
	}

	public void setIsOpenComment(String isOpenComment) {
		this.isOpenComment = isOpenComment;
	}

	public Date getPushTimeEnd() {
		return pushTimeEnd;
	}

	public void setPushTimeEnd(Date pushTimeEnd) {
		this.pushTimeEnd = pushTimeEnd;
	}

	/**
	 * 获取推送结束时间字符串
	 * 
	 * @return
	 */
	// public String getPushTimeEndStr(){
	// return DateUtil.formatDateTime(getPushTimeEnd());
	// }

	public void setIsHaveImg(String isHaveImg) {
		this.isHaveImg = isHaveImg;
	}

	public String getIsHaveImg() {
		return isHaveImg;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNewsId() {
		return newsId;
	}

	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}

	public String getFromApk() {
		return fromApk;
	}

	public void setFromApk(String fromApk) {
		this.fromApk = fromApk;
	}

	public String getArticletype() {
		return articletype;
	}

	public void setArticletype(String articletype) {
		this.articletype = articletype;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getIsApi() {
		return isApi;
	}

	public void setIsApi(int isApi) {
		this.isApi = isApi;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getCpType() {
		return cpType;
	}

	public void setCpType(String cpType) {
		this.cpType = cpType;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public int getIsOriginal() {
		return isOriginal;
	}

	public void setIsOriginal(int isOriginal) {
		this.isOriginal = isOriginal;
	}

	public String getAreaIds() {
		return areaIds;
	}

	public void setAreaIds(String areaIds) {
		this.areaIds = areaIds;
	}

	public String getAreadtos() {
		return areadtos;
	}

	public void setAreadtos(String areadtos) {
		this.areadtos = areadtos;
	}

	public String getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(String isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getExposure() {
		return exposure;
	}

	public void setExposure(int exposure) {
		this.exposure = exposure;
	}

	public String getPushJson() {
		return pushJson;
	}

	public void setPushJson(String pushJson) {
		this.pushJson = pushJson;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getVersion1() {
		return version1;
	}

	public void setVersion1(String version1) {
		this.version1 = version1;
	}

	public String getVersion2() {
		return version2;
	}

	public void setVersion2(String version2) {
		this.version2 = version2;
	}

	public String getJslog() {
		return jslog;
	}

	public void setJslog(String jslog) {
		this.jslog = jslog;
	}

	
}
