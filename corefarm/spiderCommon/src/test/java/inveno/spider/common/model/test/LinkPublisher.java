package inveno.spider.common.model.test;

import java.io.Serializable;
import java.util.Date;

public class LinkPublisher implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String link;
	private String publisher;
	private String sourceFeedsUrl;
	private Date updateTime;
	private String tableName;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getSourceFeedsUrl() {
		return sourceFeedsUrl;
	}
	public void setSourceFeedsUrl(String sourceFeedsUrl) {
		this.sourceFeedsUrl = sourceFeedsUrl;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
