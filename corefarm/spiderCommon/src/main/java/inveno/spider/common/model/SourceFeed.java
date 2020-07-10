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
public class SourceFeed implements Serializable
{
	private static final long serialVersionUID = 1L;

	//default by pass all filter check, like sensitive, publisher..etc.
	public static final int FILTER_CHECK_DEFAULT = 0;
	//skip all filter check and automatically publish.
	public static final int FILTER_CHECK_EXEMPT  = 1;
	//do not publish
	public static final int FILTER_CHECK_BLOCK   = 2;
	//publish after auditing
	public static final int FILTER_CHECK_AUDIT   = 3;

	private int id;
	private String sourceType;
	private String source;
	private String sourceFeeds;
	private String sourceFeedsName;
	private String sourceFeedsUrl;
	private String sourceFeedsCategory;
	private String country;
	private String channel;
	private String publisher;
	private int contentType;
	private String language;
	private int exemptReview;
	private int novelty;
	private int authority;
	private int contentQuality;
	private int effectiveness;
	private int adultScore;
	private int advertisementScore;
	private int audienceScale;
	private int copyrighted;
	private int isOpenComment;
	private int linkType;
	private int spiderStatus;
	private Date updateTime;
	private String operator;
	private String flag;
	
	private int timeliness;
	private String crawlerType;
	
	public String getCrawlerType() {
		return crawlerType;
	}

	public void setCrawlerType(String crawlerType) {
		this.crawlerType = crawlerType;
	}

	public int getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(int timeliness) {
		this.timeliness = timeliness;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public int getAdultScore()
	{
		return adultScore;
	}

	public void setAdultScore(int adultScore)
	{
		this.adultScore = adultScore;
	}

	public int getAdvertisementScore()
	{
		return advertisementScore;
	}

	public void setAdvertisementScore(int advertisementScore)
	{
		this.advertisementScore = advertisementScore;
	}

	public int getAudienceScale()
	{
		return audienceScale;
	}

	public void setAudienceScale(int audienceScale)
	{
		this.audienceScale = audienceScale;
	}

	public int getAuthority()
	{
		return authority;
	}

	public void setAuthority(int authority)
	{
		this.authority = authority;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public int getContentQuality()
	{
		return contentQuality;
	}

	public void setContentQuality(int contentQuality)
	{
		this.contentQuality = contentQuality;
	}

	public int getCopyrighted()
	{
		return copyrighted;
	}

	public void setCopyrighted(int copyrighted)
	{
		this.copyrighted = copyrighted;
	}

	public int getEffectiveness()
	{
		return effectiveness;
	}

	public void setEffectiveness(int effectiveness)
	{
		this.effectiveness = effectiveness;
	}

	public int getExemptReview()
	{
		return exemptReview;
	}

	public void setExemptReview(int exemptReview)
	{
		this.exemptReview = exemptReview;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getIsOpenComment()
	{
		return isOpenComment;
	}

	public void setIsOpenComment(int isOpenComment)
	{
		this.isOpenComment = isOpenComment;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public int getLinkType()
	{
		return linkType;
	}

	public void setLinkType(int linkType)
	{
		this.linkType = linkType;
	}

	public int getNovelty()
	{
		return novelty;
	}

	public void setNovelty(int novelty)
	{
		this.novelty = novelty;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getSourceFeeds()
	{
		return sourceFeeds;
	}

	public void setSourceFeeds(String sourceFeeds)
	{
		this.sourceFeeds = sourceFeeds;
	}

	public String getSourceFeedsCategory()
	{
		return sourceFeedsCategory;
	}

	public void setSourceFeedsCategory(String sourceFeedsCategory)
	{
		this.sourceFeedsCategory = sourceFeedsCategory;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String _publisher)
	{
		publisher = _publisher;
	}

	public int getContentType()
	{
		return contentType;
	}

	public void setContentType(int _contentType)
	{
		contentType = _contentType;
	}

	public String getSourceFeedsName()
	{
		return sourceFeedsName;
	}

	public void setSourceFeedsName(String sourceFeedsName)
	{
		this.sourceFeedsName = sourceFeedsName;
	}

	public String getSourceFeedsUrl()
	{
		return sourceFeedsUrl;
	}

	public void setSourceFeedsUrl(String sourceFeedsUrl)
	{
		this.sourceFeedsUrl = sourceFeedsUrl;
	}

	public String getSourceType()
	{
		return sourceType;
	}

	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}

	public int getSpiderStatus()
	{
		return spiderStatus;
	}

	public void setSpiderStatus(int spiderStatus)
	{
		this.spiderStatus = spiderStatus;
	}

	public Date getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(Date updateTime)
	{
		this.updateTime = updateTime;
	}
}
