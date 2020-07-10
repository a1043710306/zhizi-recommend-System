package inveno.spider.common.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ContentDao;
import inveno.spider.common.model.Content;

/**
 * 资讯入库Dao
 *  Class Name: ContentDaoImpl.java
 *  Description: 
 *  @author liyuanyi  DateTime 2016年3月18日 下午6:44:23 
 *  @company inveno 
 *  @version 1.0
 */
public class ContentDaoImpl implements ContentDao {
	
	@Override
	public int deleteByPrimaryKey(String contentId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Content record) {
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.ContentMapper.insert", record);
	}

	@Override
	public int insertSelective(Content record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Content selectByPrimaryKey(String contentId) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ContentMapper.selectByPrimaryKey", contentId);
	}

	@Override
	public int updateByPrimaryKeySelective(Content record) {
		// TODO Auto-generated method stub
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.ContentMapper.updateByPrimaryKeySelective", record);
	}

	@Override
	public int updateByPrimaryKeyWithBLOBs(Content record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateByPrimaryKey(Content record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Content> queryContentList(String contentId) {
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.ContentMapper.selectListById", contentId);
	}

	@Override
	public Content queryContentListByLink(String link) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ContentMapper.selectListByLink", link);
	}
	
	@Override
	public Content queryContentByLinkOrExtraLink(String link, String extraLink) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("link", link);
		map.put("extraLink", extraLink);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.ContentMapper.selectByLinkOrExtraLink", map);
	}

	@Override
	public List<Content> selectEditorLog(String contentId) {
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.ContentMapper.selectEditorLog", contentId);
	}

	@Override
	public Content selectLastest2MonthContentByLink(String link, String currentTable, String beforeTable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content selectLastest2MonthContentByLinkOrExtraLink(String link, String extraLink, String currentTable,
			String beforeTable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insert2CurrentMonth(Content targetContent, String currentTable) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update2CurrentByPrimaryKeySelective(Content content, String currentTable, String beforeTable) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updatePublisherByPrimaryKeySelective(String publisher, String contentId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("publisher", publisher);
		map.put("contentId", contentId);
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.ContentMapper.updatePublisherByPrimaryKeySelective", map);
	}

	@Override
	public List<Content> queryByDiscoveryTimeAndState(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return new ArrayList<Content>();
	}

	@Override
	public Content queryDetailByID(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateStateByContentId(String contentId, Integer state, Integer offshelfCode, String offshelfReason,
			String currentTable, String beforeTable) {
		// TODO Auto-generated method stub
		return 0;
	}

}
