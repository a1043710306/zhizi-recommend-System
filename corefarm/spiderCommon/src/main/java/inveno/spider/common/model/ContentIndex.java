package inveno.spider.common.model;

import java.io.Serializable;

public class ContentIndex implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	
	private String contentId;

	private String tableName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
