package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class Publisher implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private int copyrighted;

	private int rate;

	private int isFilter;

	private int status;

	private String operator;
	
	private Date updateTime;

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCopyrighted() {
		return copyrighted;
	}

	public void setCopyrighted(int copyrighted) {
		this.copyrighted = copyrighted;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getIsFilter() {
		return isFilter;
	}

	public void setIsFilter(int isFilter) {
		this.isFilter = isFilter;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return "Publisher [id=" + id + ", name=" + name + ", copyrighted=" + copyrighted + ", rate=" + rate
				+ ", isFilter=" + isFilter + ", status=" + status + ", operator=" + operator + ", updateTime="
				+ updateTime + "]";
	}

}
