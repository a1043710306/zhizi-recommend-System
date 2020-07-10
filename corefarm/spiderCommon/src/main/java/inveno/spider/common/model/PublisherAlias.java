package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class PublisherAlias implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private String alias;

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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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
		return "PublisherAlias [id=" + id + ", name=" + name + ", alias=" + alias + ", status=" + status + ", operator="
				+ operator + ", updateTime=" + updateTime + "]";
	}

}
