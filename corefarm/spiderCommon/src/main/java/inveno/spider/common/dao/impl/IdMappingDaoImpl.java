package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.IdMappingDao;
import inveno.spider.common.model.IdMapping;

import java.util.List;

public class IdMappingDaoImpl implements IdMappingDao {

	@Override
	public boolean insert(IdMapping idMapping) {
		final String sqlId="inveno.spider.common.mapper.IdMappingMapper.insert";
        return SessionFactory.getInstance().insert(sqlId, idMapping) == 1;
	}

	@Override
	public boolean insertList(List<IdMapping> idMappingList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer selectLastOne() {
		final String sqlId="inveno.spider.common.mapper.IdMappingMapper.selectLastOne";
        return SessionFactory.getInstance().selectOne(sqlId, null);
	}

	@Override
	public List<IdMapping> selectList(Integer newsId) {
		final String sqlId="inveno.spider.common.mapper.IdMappingMapper.selectList";
        return SessionFactory.getInstance().selectList(sqlId, newsId);
	}
}
