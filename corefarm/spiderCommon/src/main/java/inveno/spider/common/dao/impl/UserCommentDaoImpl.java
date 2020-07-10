package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.UserCommentDao;
import inveno.spider.common.model.UserComment;

public class UserCommentDaoImpl implements UserCommentDao
{
   	@Override
	public int insert(UserComment record)
	{
		return SessionFactory.getInstance().insert("inveno.spider.common.mapper.UserCommentMapper.insert", record);
	}
}
