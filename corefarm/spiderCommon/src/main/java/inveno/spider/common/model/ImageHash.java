package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class ImageHash implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	private String contentId;

	private String imageHashMapStr;
	
	private String fallImageHashMapStr;

	private Date createTime;

	private Date updateTime;

	public String getFallImageHashMapStr() {
		return fallImageHashMapStr;
	}

	public void setFallImageHashMapStr(String fallImageHashMapStr) {
		this.fallImageHashMapStr = fallImageHashMapStr;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

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

	public String getImageHashMapStr() {
		return imageHashMapStr;
	}

	public void setImageHashMapStr(String imageHashMapStr) {
		this.imageHashMapStr = imageHashMapStr;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
