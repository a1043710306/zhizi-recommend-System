package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;



/**
 * 频道资讯信息实体类(已审核)
 * @author liming
 * @date 2012-7-25
 * 
 */
//@Entity
//@Table(name = "s_channel_content_audit")
public class ChannelContentAudit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** 主键 */
	private String id;

	/** 创建时间 */
	//@Column = "create_time")
	private Date createTime;

	/** 图片路径 */
	//@Column = "img_url")
	private String imgUrl;

	/** 缩略图片路径 */
	//@Column = "cut_img_url")
	private String cutImgUrl;

	/** 标题 */
	//@Column = "title")
	private String title;

	/** 推送标题 */
	//@Column = "push_title")
	private String pushTitle;

	/** 简介 */
	//@Column = "intro")
	private String intro;

	/** 内容 */
	//@Column = "content")
	private String content;

	/** 频道类型ID */
	//@Column = "type_code")
	private String typeCode;

	/** 预发布日期 */
	//@Column = "start_time")
	//@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	/** 新闻来源 */
	//@Column = "source")
	private String source;

	/** 客户端显示来源 */
	//@Column = "show_source")
	private String showSource;

	/** 跳出时，信息指向路径 */
	//@Column = "url")
	private String url;

	/** 状态 */
	//@Column = "state")
	private int state;

	/** 是否跳出，0、不跳出，1、跳出 */
	//@Column = "open_out")
	private int openOut;

	/** 当是专版的子资讯时，其父资讯的ID */
	//@Column = "parent_id")
	private String parentId;

	/** rss信息ID */
	//@Column = "rss_id")
	private String rssId;

	/** 是否是专题，0、不是，1、是 */
	//@Column = "is_special")
	private int special;

	/** 地址适配 **/
	//@Column = "address")
	private String address;

	/** 信息原始发布时间 */
	//@Column = "original_time")
	//@Temporal(TemporalType.TIMESTAMP)
	private Date originalTime;

	/** 在Flyshare的发布时间 */
	//@Column = "release_time")
	//@Temporal(TemporalType.TIMESTAMP)
	private Date releaseTime;

	/** 查看原文的地址 */
	//@Column = "original_url")
	private String originalUrl;

	/** 是否是推送资讯，0、不是，1、是 */
	//@Column = "is_push")
	private int push;


	/** 资讯的无间隔排序 */
	//@Column = "channel_index")
	private int channelIndex;

	/** 标示资讯是否重要 0代表不重要 1代表重要 */
	//@Column = "is_major")
	private int major;

	/** 标示资讯图片是为重新录入 0代表非重新录入 1代表重新录入 */
	//@Column = "is_reentry")
	private int reEntry;

	/** 标示老的专题是否有更新 0代表没有更新，1代表有更新，2代表新的资讯已经发布 */
	//@Column = "is_update_special")
	private int updateSpecial;

	/** 推送时间 */
	//@Column = "pushtime")
	//@Temporal(TemporalType.TIMESTAMP)
	private Date pushTime;

	/** 对已经发布的资讯进行修改，做时间标示 */
	//@Column = "update_time")
	//@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;

	/** 资讯评论功能状态 0代表打开 1代表关闭 */
	//@Column = "is_open_comment")
	private String isOpenComment;

	/** 资讯内容是否有图 2没有图 1有图 */
	//@Column = "is_have_img")
	private String isHaveImg;
	
	/** 是否推荐widget展示 2不展示 1展示 */
	//@Column = "is_widget_show")
	private String isWidgetShow;
	
	/** 是否是我们自己的资讯 1是 2不是 */
//	//@Column = "is_owned")
//	private int isOwned;
	
	/**这个资讯的属于哪一个客户*/
//	//@Column = "user_id")
//	private String UserId;
	
	//第三方资讯的Id，如腾讯新闻，搜狐新闻等等
	//@Column = "news_id")
	private String newsId;
	
	//来自某个新闻的客户端;如腾讯新闻客户端
	//@Column = "from_apk")
	private String fromApk;
	
	//文章的类型：0表示普通信息  1表示组图信息（腾讯资讯的表示）
	//@Column = "article_type")
	private String articletype;
	
	//资讯的摘要
	//@Column = "summary")
	private String summary;
	
	//表示资讯是否是动态API获取的  0表示不是动态Api   1表示是动态api的
	//@Column = "isapi")
	private int isApi;
	
	private String tags;
	private String locations;//资讯涉及的地区列表,json格式
	private int score;//资讯评分
	
	//图片数量
	private int imgCount;
	//是否被过滤，0未被过滤，非0表示过滤原因
	private int filtered;
	
	
	/**城市id**/
	//@Column = "city_id")
	private String cityId;
	
	//1：资讯 2:视频 3 :音频 4:小说  5 :电商
	private int datatype;
	
	//0-普通资讯,1-头条,2-精编,3-无标识头条。4-热门
	private int infoType;
	
	//分类
	private String categoryName;

	private int second;

	public int getSecond()
	{
		return second;
	}

	public void setSecond(int _second)
	{
		second = _second;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public String getCityId() {
		return cityId;
	}
	
	public void setCityId(String cityId) {
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
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

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
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

	public String getRssId() {
		return rssId;
	}

	public void setRssId(String rssId) {
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


	public int getChannelIndex() {
		return channelIndex;
	}

	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
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
		if("0".equals(isOpenComment)){
			isOpenComment="开";
		}else if("1".equals(isOpenComment)){
			isOpenComment="关";
		}
		return isOpenComment;
	}

	public void setIsOpenComment(String isOpenComment) {
		this.isOpenComment = isOpenComment;
	}

	public String getIsHaveImg() {
		return isHaveImg;
	}

	public void setIsHaveImg(String isHaveImg) {
		this.isHaveImg = isHaveImg;
	}

//	public int getIsOwned() {
//		return isOwned;
//	}
//
//	public void setIsOwned(int isOwned) {
//		this.isOwned = isOwned;
//	}
//
//	public String getUserId() {
//		return UserId;
//	}
//
//	public void setUserId(String userId) {
//		UserId = userId;
//	}

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

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getLocations()
    {
        return locations;
    }

    public void setLocations(String locations)
    {
        this.locations = locations;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public int getImgCount()
    {
        return imgCount;
    }

    public void setImgCount(int imgCount)
    {
        this.imgCount = imgCount;
    }

    public int getFiltered()
    {
        return filtered;
    }

    public void setFiltered(int filtered)
    {
        this.filtered = filtered;
    }

    public int getDatatype()
    {
        return datatype;
    }

    public void setDatatype(int dataType)
    {
        this.datatype = dataType;
    }

}
