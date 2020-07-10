package inveno.spider.common.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ContentDao;
import inveno.spider.common.model.Content;
import inveno.spider.common.utils.LoggerFactory;

public class SplitTableContentDaoImpl implements ContentDao {
	
	private static final Logger LOG = LoggerFactory.make();

	@Override
	public int deleteByPrimaryKey(String contentId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Content record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insertSelective(Content record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Content selectByPrimaryKey(String contentId) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.selectByPrimaryKey", contentId);
	}

	@Override
	public int updateByPrimaryKeySelective(Content record) {
		return SessionFactory.getInstance().update("inveno.spider.common.mapper.SplitTableContentMapper.updateByPrimaryKeySelective", record);
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
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.SplitTableContentMapper.selectListById", contentId);
	}

	@Override
	public Content queryContentListByLink(String link) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.selectListByLink", link);
	}
	
	@Override
	public Content queryContentByLinkOrExtraLink(String link, String extraLink) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("link", link);
		map.put("extraLink", extraLink);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.selectByLinkOrExtraLink", map);
	}

	@Override
	public List<Content> selectEditorLog(String contentId) {
		return SessionFactory.getInstance().selectList("inveno.spider.common.mapper.SplitTableContentMapper.selectEditorLog", contentId);
	}

	@Override
	public Content selectLastest2MonthContentByLink(String link, String currentTable, String beforeTable) {
		if(StringUtils.isEmpty(currentTable) || StringUtils.isEmpty(beforeTable)) {
			LOG.error("generator table name is empty|link=" + link);
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("link", link);
		map.put("currentTableName", currentTable);
		map.put("beforeTableName", beforeTable);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.selectLastest2MonthContentByLink", map);
	}

	@Override
	public Content selectLastest2MonthContentByLinkOrExtraLink(String link, String extraLink, String currentTable, String beforeTable) {
		if(StringUtils.isEmpty(currentTable) || StringUtils.isEmpty(beforeTable)) {
			LOG.error("generator table name is empty|link=" + link);
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("link", link);
		map.put("extraLink", extraLink);
		map.put("currentTableName", currentTable);
		map.put("beforeTableName", beforeTable);
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.selectLastest2MonthContentByLinkOrExtraLink", map);
	}

	@Override
	public int insert2CurrentMonth(Content record, String currentTable) {
		if(StringUtils.isEmpty(currentTable)) {
			LOG.error("generator table name is empty|link=" + record.getLink());
			return -1;
		}
		record.setCurrentMonthTableName(currentTable);
		if(EscapeString(record))
			return SessionFactory.getInstance().insert("inveno.spider.common.mapper.SplitTableContentMapper.insert2CurrentMonth", record);
		return -1;
	}

	/**
	 * @param record
	 */
	private boolean EscapeString(Content record) {
/*		boolean success = false;
		try {
			Field[] fields = record.getClass().getDeclaredFields();
			for(Field f : fields) {
				if(f.getType().getName().equals("java.lang.String")) {
					String fieldName = f.getName();
					PropertyDescriptor pd = new PropertyDescriptor(fieldName, record.getClass()); 
					Method getMethod = pd.getReadMethod();
					String oldValue = (String) getMethod.invoke(record);
					Method setMethod = pd.getWriteMethod();
					String newValue = StringEscapeUtils.escapeSql(oldValue);
					setMethod.invoke(record, new Object[] { newValue });
					success =  true;
				}
			}
		} catch(Exception e) {
			LOG.error("String Escape link:" + record.getLink() + " has exception:", e);
			success = false;
		}
		return success;*/
		return true;
	}

	@Override
	public int update2CurrentByPrimaryKeySelective(Content record, String currentTable, String beforeTable) {
		if(StringUtils.isEmpty(currentTable) || StringUtils.isEmpty(beforeTable)) {
			LOG.error("generator table name is empty|link=" + record.getLink());
			return -1;
		}
		record.setCurrentMonthTableName(currentTable);
		if(EscapeString(record)) {
			int affected = SessionFactory.getInstance().update("inveno.spider.common.mapper.SplitTableContentMapper.update2CurrentByPrimaryKeySelective", record);
			if(affected < 1) {
				record.setCurrentMonthTableName(beforeTable);
				affected = SessionFactory.getInstance().update("inveno.spider.common.mapper.SplitTableContentMapper.update2CurrentByPrimaryKeySelective", record);
				return affected;
			}
		}
		return -1;
	}

	@Override
	public int updatePublisherByPrimaryKeySelective(String publisher, String contentId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Content> queryByDiscoveryTimeAndState(Map<String, Object> params) {
		LOG.info("queryByDiscoveryTimeAndState start.");
		List<Content> results = SessionFactory.getInstance().selectList("inveno.spider.common.mapper.SplitTableContentMapper.queryByDiscoveryTimeAndState", params);
		LOG.info("queryByDiscoveryTimeAndState end.");
		return results == null ? new ArrayList<Content>() : results;
	}

	@Override
	public Content queryDetailByID(Map<String, Object> map) {
		return SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.SplitTableContentMapper.queryDetailByID", map);
	}

	@Override
	public int updateStateByContentId(String contentId, Integer state, Integer offshelfCode, String offshelfReason,
			String currentTable, String beforeTable) {
		if(StringUtils.isEmpty(currentTable) || StringUtils.isEmpty(beforeTable)) {
			LOG.error("generator table name is empty|contentId=" + contentId);
			return -1;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", currentTable);
		map.put("state", state);
		map.put("offshelfCode", offshelfCode);
		map.put("offshelfReason", offshelfReason);
		map.put("contentId", contentId);
		int affected = SessionFactory.getInstance().update("inveno.spider.common.mapper.SplitTableContentMapper.updateStateByContentId", map);
		if(affected < 1) {
			map.put("tableName", beforeTable);
			affected = SessionFactory.getInstance().update("inveno.spider.common.mapper.SplitTableContentMapper.updateStateByContentId", map);
			return affected;
		}
		return -1;
	}

}
