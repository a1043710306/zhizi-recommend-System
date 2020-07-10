package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class FilterWord implements Serializable{

	
	/**
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年5月26日 
	 *  
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	
	private String word;
	
	private String firmName;
	
	private int status;
	
	private Date createTime;
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}
}
