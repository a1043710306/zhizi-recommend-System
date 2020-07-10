package com.inveno.server.contentgroup.datainfo;

import java.io.Serializable;
import java.util.Date;

public class EditorTableEntry implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String contentId;
	private String title;
	private Integer categoryId;
	private String categories;
	private String content;
	private String keywords;
	private String flag;
	private String adultScore ;
	private Integer linkType;
	private String link;
	private String linkMd5;
	private String source;
	private String sourceType;
	private String sourceFeeds;
	private String channel;
	private String firmApp;
	private String local;
	private Integer contentType;
	private String type;
	private String sourceItemId;
	private String duration;
	private String language;
	private String country;
	private String copyright;
	private Integer hasCopyright;
	private Integer displayType;
	private String summary;
	private String fallImage;
	private String author;
	private String publisher;
	private Integer rate;
	private String bodyImages;
	private String listImages;
	private Integer bodyImagesCount;
	private Integer listImagesCount;
	private String displayListImages;
	private String picAdultScore;
	private String publishTime;
	private Date updateTime ;
	private String operator;
	private Integer status;
	private Integer is_open_comment;
	private Integer contentQuality;
	
	public Integer getContentQuality() {
		return contentQuality;
	}
	public void setContentQuality(Integer contentQuality) {
		this.contentQuality = contentQuality;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getAdultScore() {
		return adultScore;
	}
	public void setAdultScore(String adultScore) {
		this.adultScore = adultScore;
	}
	public String getFirmApp() {
		return firmApp;
	}
	public void setFirmApp(String firmApp) {
		this.firmApp = firmApp;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public Integer getIs_open_comment() {
		return is_open_comment;
	}
	public void setIs_open_comment(Integer is_open_comment) {
		this.is_open_comment = is_open_comment;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Integer getLinkType() {
		return linkType;
	}
	public void setLinkType(Integer linkType) {
		this.linkType = linkType;
	}
	public String getLinkMd5() {
		return linkMd5;
	}
	public void setLinkMd5(String linkMd5) {
		this.linkMd5 = linkMd5;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public String getSourceFeeds() {
		return sourceFeeds;
	}
	public void setSourceFeeds(String sourceFeeds) {
		this.sourceFeeds = sourceFeeds;
	}
	
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public Integer getContentType() {
		return contentType;
	}
	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}
	public String getSourceItemId() {
		return sourceItemId;
	}
	public void setSourceItemId(String sourceItemId) {
		this.sourceItemId = sourceItemId;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Integer getHasCopyright() {
		return hasCopyright;
	}
	public void setHasCopyright(Integer hasCopyright) {
		this.hasCopyright = hasCopyright;
	}
	public Integer getDisplayType() {
		return displayType;
	}
	public void setDisplayType(Integer displayType) {
		this.displayType = displayType;
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
	public Integer getRate() {
		return rate;
	}
	public void setRate(Integer rate) {
		this.rate = rate;
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
	public String getDisplayListImages() {
		return displayListImages;
	}
	public void setDisplayListImages(String displayListImages) {
		this.displayListImages = displayListImages;
	}
	public String getPicAdultScore() {
		return picAdultScore;
	}
	public void setPicAdultScore(String picAdultScore) {
		this.picAdultScore = picAdultScore;
	}
	public String getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	@Override
	public String toString() {
		return "EditorTableEntry [id=" + id + ", contentId=" + contentId + ", title=" + title + ", categoryId="
				+ categoryId + ", categories=" + categories + ", content=" + content + ", keywords=" + keywords
				+ ", flag=" + flag + ", adultScore=" + adultScore + ", linkType=" + linkType + ", link=" + link
				+ ", linkMd5=" + linkMd5 + ", source=" + source + ", sourceType=" + sourceType + ", sourceFeeds="
				+ sourceFeeds + ", channel=" + channel + ", firmApp=" + firmApp + ", local=" + local + ", contentType="
				+ contentType + ", type=" + type + ", sourceItemId=" + sourceItemId + ", duration=" + duration
				+ ", language=" + language + ", country=" + country + ", copyright=" + copyright + ", hasCopyright="
				+ hasCopyright + ", displayType=" + displayType + ", summary=" + summary + ", fallImage=" + fallImage
				+ ", author=" + author + ", publisher=" + publisher + ", rate=" + rate + ", bodyImages=" + bodyImages
				+ ", listImages=" + listImages + ", bodyImagesCount=" + bodyImagesCount + ", listImagesCount="
				+ listImagesCount + ", displayListImages=" + displayListImages + ", picAdultScore=" + picAdultScore
				+ ", publishTime=" + publishTime + ", updateTime=" + updateTime + ", operator=" + operator + ", status="
				+ status + ", is_open_comment=" + is_open_comment + "]";
	}
}
