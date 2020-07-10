package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 敏感词类
 *  Class Name: SensitiveWord.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年4月28日 下午3:06:03 
 *  @company inveno 
 *  @version 1.0
 */
public class SensitiveWord implements Serializable{
	
	/**
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年4月28日 
	 *  
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	
	private String word;
	
	//1：有效  0：失效
	private int status;
	
	private Date createTime;

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
	
	
}
