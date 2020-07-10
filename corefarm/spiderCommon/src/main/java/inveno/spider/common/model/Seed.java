package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Spider seed.
 * 
 * @version 1.0 2014年6月17日
 * 
 * @author xiaochun.tang@anchormobile.com
 * 
 */
public class Seed implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    
    /**
     * url,feed,json
     */
    private String urlType;

    /** Rss名称 */
    // @Column(name = "rss_name")
    private String rssName;

    /** 信息频道类型 */
    // @Column(name = "type_code")
    private int typeCode;

    /** Rss路径 */
    // @Column(name = "url")
    private String url;

    /** 来源等级,1/2/3 */
    // @Column(name = "level")
    private int level;

    /** 简介 */
    // @Column(name = "intro")
    private String intro;

    /**
     * 状态 1 未审核 2不合格 3合格 4 删除
     */
    // @Column(name = "state")
    private int state;

    /**
     * 是否抓取该源的资讯 1 抓 0不抓
     */
    // @Column(name = "get_status")
    private int getStatus;

    /** 创建时间 */
    // @Column(name = "create_time")
    // @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /** 最近抓取时间时间 */
    // @Column(name = "catche_time")
    // @Temporal(TemporalType.TIMESTAMP)
    private Date catcheTime;

    /** 最后修改时间时间 */
    // @Column(name = "update_time")
    // @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /** 删除时间 */
    // @Column(name = "delete_time")
    // @Temporal(TemporalType.TIMESTAMP)
    private Date deleteTime;

    /** 审核意见 */
    // @Column(name = "objection")
    private String objection;

    /** 来源 */
    // @Column(name="source")
    private String source;
    /** 标识 */
    // @Column(name="icon")
    private String icon;

    /** 这个rss源对应的广告 */
    // @Column(name="market_id")
    private String marketId;

    /**
     * 是否是API 0,不是；1，是；默认是0 xjr 2013-6-26
     */
    // @Column(name = "api")
    private int api;

    /**
     * 是否是符合我们json的格式 0,不符合；1，符合；默认是0
     */
    // @Column(name = "json")
    private int json;

    /**
     * 密钥
     */
    // @Column(name = "secret_key")
    private String secretKey;

    /**
     * 数据类型 内容数据类型 116 1 资讯 2 视频 3 音频 4，小说
     */
    // @Column(name = "datatype")
    private String dataType;
    /**
     * 上次请求的时间
     */
    // @Column(name = "request_time")
    private Date requestTime;

    /**
     * 类型 0：手动发布 1：自动发布
     * */
    // @Column(name = "auto_publish")
    private int autoPublish;

    // @Column(name="category")
    private String category;

    // 类型名称
    // @Column(name="category_name")
    private String categoryName;

    /** 内容供应商渠道id，对应s_rss的provider_id，t_user_key的firm_id */
    // @Column(name = "provider_id")
    private int providerId;

    // @Column(name = "tags")
    private String tags;
    
    private String rssTags;
    
    //地域
    private String areaTags;
    
    private int isOpenComment;
    
    //0-普通资讯,1-头条,2-精编,3-无标识头条。4-热门
    private int infoType;
    
    //是否需要算法判断分类 0 : 不需要。 1：需要
    private int checkCategoryFlag;
    
    public int getCheckCategoryFlag() {
		return checkCategoryFlag;
	}

	public void setCheckCategoryFlag(int checkCategoryFlag) {
		this.checkCategoryFlag = checkCategoryFlag;
	}

	public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getRssName()
    {
        return rssName;
    }

    public void setRssName(String rssName)
    {
        this.rssName = rssName;
    }

    public int getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(int typeCode)
    {
        this.typeCode = typeCode;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getIntro()
    {
        return intro;
    }

    public void setIntro(String intro)
    {
        this.intro = intro;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getDeleteTime()
    {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime)
    {
        this.deleteTime = deleteTime;
    }

    public Date getCatcheTime()
    {
        return catcheTime;
    }

    public void setCatcheTime(Date catcheTime)
    {
        this.catcheTime = catcheTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getObjection()
    {
        return objection;
    }

    public void setObjection(String objection)
    {
        this.objection = objection;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String getMarketId()
    {
        return marketId;
    }

    public void setMarketId(String marketId)
    {
        this.marketId = marketId;
    }

    public int getApi()
    {
        return api;
    }

    public void setApi(int api)
    {
        this.api = api;
    }

    public int getAutoPublish()
    {
        return autoPublish;
    }

    public void setAutoPublish(int autoPublish)
    {
        this.autoPublish = autoPublish;
    }

    public int getJson()
    {
        return json;
    }

    public void setJson(int json)
    {
        this.json = json;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public Date getRequestTime()
    {
        return requestTime;
    }

    public void setRequestTime(Date requestTime)
    {
        this.requestTime = requestTime;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public int getProviderId()
    {
        return providerId;
    }

    public void setProviderId(int providerId)
    {
        this.providerId = providerId;
    }

    public int getGetStatus()
    {
        return getStatus;
    }

    public void setGetStatus(int getStatus)
    {
        this.getStatus = getStatus;
    }

    public String getUrlType()
    {
        return urlType;
    }

    public void setUrlType(String urlType)
    {
        this.urlType = urlType;
    }

    public String getRssTags()
    {
        return rssTags;
    }

    public void setRssTags(String rssTags)
    {
        this.rssTags = rssTags;
    }

    public String getAreaTags()
    {
        return areaTags;
    }

    public void setAreaTags(String areaTags)
    {
        this.areaTags = areaTags;
    }

    public int getIsOpenComment()
    {
        return isOpenComment;
    }

    public void setIsOpenComment(int isOpenComment)
    {
        this.isOpenComment = isOpenComment;
    }
}
