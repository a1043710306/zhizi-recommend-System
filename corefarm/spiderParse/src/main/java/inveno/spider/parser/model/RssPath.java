package inveno.spider.parser.model;

import java.io.Serializable;

public class RssPath implements Serializable{
	
	private String title;
	
	private String pubDate;
	
	private String content;
	
	private String description;
	
	private String source;
	
	private String tags;
	
	private String link;

	public RssPath(){}
	
	
	
	public RssPath(String title, String pubDate, String content,
			String description, String source, String tags,String link) {
		super();
		this.title = title;
		this.pubDate = pubDate;
		this.content = content;
		this.description = description;
		this.source = source;
		this.tags = tags;
		this.link = link;
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

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
}
