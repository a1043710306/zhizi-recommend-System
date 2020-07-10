package inveno.spider.common.model;

import java.io.Serializable;

public class ContentExtend implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int IS_DISPLAY_AD_YES = 1;
	public static final int IS_DISPLAY_AD_NO = 0;

	public static final int IS_CONTAINS_SOCIAL_PLUGIN_NO = 0;
	public static final int IS_CONTAINS_SOCIAL_PLUGIN_FACEBOOK = 1;
	public static final int IS_CONTAINS_SOCIAL_PLUGIN_TWITTER = 2;
	public static final int IS_CONTAINS_SOCIAL_PLUGIN_INSTAGRAM = 3;
	public static final int IS_CONTAINS_SOCIAL_PLUGIN_OTHER = 4;

	private String contentId;

	private int contentQuality;

	private int timeliness;

	private String bodyVideos;
	private String categoriesComic;
	private String youtubeCategory;

	private int isDisplayAd = IS_DISPLAY_AD_YES;

	private int isContainsSocialPlugin = IS_CONTAINS_SOCIAL_PLUGIN_NO;

	/**
	 * 文章 Topic(Youtube来源于API)
	 */
	private String topics;


	public String getBodyVideos() {
		return bodyVideos;
	}

	public void setBodyVideos(String bodyVideos) {
		this.bodyVideos = bodyVideos;
	}

	public int getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(int timeliness) {
		this.timeliness = timeliness;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public int getContentQuality() {
		return contentQuality;
	}

	public void setContentQuality(int contentQuality) {
		this.contentQuality = contentQuality;
	}

	@Override
	public String toString() {
		return "ContentExtend [contentId=" + contentId + ", contentQuality=" + contentQuality + ", timeliness="
				+ timeliness + ", bodyVideos=" + bodyVideos + ", categoriesComic=" + categoriesComic
				+ ", youtubeCategory=" + youtubeCategory
				+ ", isDisplayAd=" + isDisplayAd + ", isContainsSocialPlugin=" + isContainsSocialPlugin + "]";
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

	public void setIsDisplayAd(int isDisplayAd) {
		this.isDisplayAd = isDisplayAd;
	}

	public void setIsContainsSocialPlugin(int isContainsSocialPlugin) {
		this.isContainsSocialPlugin = isContainsSocialPlugin;
	}

	public int getIsDisplayAd() {
		return isDisplayAd;
	}

	public int getIsContainsSocialPlugin() {
		return isContainsSocialPlugin;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}
}
