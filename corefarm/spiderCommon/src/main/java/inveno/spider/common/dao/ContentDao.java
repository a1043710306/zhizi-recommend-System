package inveno.spider.common.dao;

import java.util.List;
import java.util.Map;

import inveno.spider.common.model.Content;

public interface ContentDao {
	int deleteByPrimaryKey(String contentId);

	int insert(Content record);

	int insertSelective(Content record);

	Content selectByPrimaryKey(String contentId);

	int updateByPrimaryKeySelective(Content record);

	int updateByPrimaryKeyWithBLOBs(Content record);

	int updateByPrimaryKey(Content record);

	List<Content> queryContentList(String contentId);

	Content queryContentListByLink(String link);

	Content queryContentByLinkOrExtraLink(String link, String extraLink);

	List<Content> selectEditorLog(String contentId);

	/**
	 * 根据当前日期生成表名，并且根据link查询最近2个月的数据中是否存在
	 * 
	 * @return
	 */
	Content selectLastest2MonthContentByLink(String link, String currentTable, String beforeTable);

	/**
	 * 根据当前日期生成表名，并且根据link（extraLink）查询最近2个月的数据中是否存在
	 * 
	 * @return
	 */
	Content selectLastest2MonthContentByLinkOrExtraLink(String link, String extraLink, String currentTable,
			String beforeTable);

	/**
	 * 将数据插入当月的表
	 * 
	 * @param targetContent
	 * @return
	 */
	int insert2CurrentMonth(Content targetContent, String currentTable);

	/**
	 * 更新数据到当月表
	 * 
	 * @param content
	 * @return
	 */
	int update2CurrentByPrimaryKeySelective(Content content, String currentTable, String beforeTable);

	int updatePublisherByPrimaryKeySelective(String publisher, String contentId);
	
	int updateStateByContentId(String contentId, Integer state, Integer offshelfCode, String offshelfReason, String currentTable, String beforeTable);

	List<Content> queryByDiscoveryTimeAndState(Map<String, Object> params);

	Content queryDetailByID(Map<String, Object> map);
}