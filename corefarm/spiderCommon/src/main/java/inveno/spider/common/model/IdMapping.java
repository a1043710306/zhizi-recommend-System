package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

/**
 * ID mapping 的实体类
 *  Class Name: IdMapping.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年4月12日 下午3:20:31 
 *  @company inveno 
 *  @version 1.0
 */
public class IdMapping implements Serializable{
	
	
	/**
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年4月12日 
	 *  
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer newsId;
	
	private Date createTime;
	
	private String description;

	public Integer getNewsId() {
		return newsId;
	}

	public void setNewsId(Integer newsId) {
		this.newsId = newsId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
