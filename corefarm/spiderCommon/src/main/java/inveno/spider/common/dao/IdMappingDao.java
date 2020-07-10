package inveno.spider.common.dao;

import inveno.spider.common.model.IdMapping;

import java.util.List;

public interface IdMappingDao {
	
	boolean insertList(List<IdMapping> idMappingList);
	
	boolean insert(IdMapping idMapping);
	
	Integer selectLastOne();
	
	List<IdMapping> selectList(Integer newsId);
}
