package inveno.spider.common.dao;

import org.apache.ibatis.annotations.Param;

/**
 * 
 *  Class Name: SequenceDao.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年3月20日 上午11:38:57 
 *  @company inveno 
 *  @version 1.0
 */
public interface SequenceDao {
	/**
	 * 获取序列
	 * 
	 * @param seqName
	 *            序列名称
	 * @return
	 */
	public String querySequenceVal(@Param("seqName") String seqName);
}
