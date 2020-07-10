package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class TproductPreAuditContent implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date createTime;

	private String contentId;

	private String firmApp;

	private Integer status;

	private Integer dispatched;

	private Integer checkStatus;

	private Date checkTime;

	private String operator;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getFirmApp() {
		return firmApp;
	}

	public void setFirmApp(String firmApp) {
		this.firmApp = firmApp;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getDispatched() {
		return dispatched;
	}

	public void setDispatched(Integer dispatched) {
		this.dispatched = dispatched;
	}

	public Integer getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(Integer checkStatus) {
		this.checkStatus = checkStatus;
	}

	public Date getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return "TproductPreAuditContent [id=" + id + ", createTime=" + createTime + ", contentId=" + contentId
				+ ", firmApp=" + firmApp + ", status=" + status + ", dispatched=" + dispatched + ", checkStatus="
				+ checkStatus + ", checkTime=" + checkTime + ", operator=" + operator + "]";
	}
	
}
