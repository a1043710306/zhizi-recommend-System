package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class ImageUrlFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	/**
	 * 1: fall_image 2:body_image
	 * 
	 */
	private int type;
	private Date updateTime;
	private String operator;
	private int status;
	private String imageId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

}
