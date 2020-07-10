package com.inveno.feeder.datainfo;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * ContentEditorLog表实体类
 * @author klaus liu
 *
 */
public class ContentEditorLog implements Serializable {

	private static final long serialVersionUID = 6995746912825156442L;
	
	//自增ID
	private Integer id;
	
	//资讯ID
	private String contentId;
	
	//修改类型
	private Integer editorType;
	
	//操作者
	private String operator;

	//操作时间
	private Date operateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public Integer getEditorType() {
		return editorType;
	}

	public void setEditorType(Integer editorType) {
		this.editorType = editorType;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Date getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(Date operateTime) {
		this.operateTime = operateTime;
	}

	@Override
	public String toString() {
		return "ContentEditorLog [id=" + id + ", contentId=" + contentId + ", editorType=" + editorType + ", operator="
				+ operator + ", operateTime=" + operateTime + "]";
	}
	
}
