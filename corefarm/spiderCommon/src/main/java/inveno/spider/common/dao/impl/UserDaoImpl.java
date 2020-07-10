package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.UserDao;
import inveno.spider.common.model.User;

public class UserDaoImpl implements UserDao
{

    @Override
    public User find(String username, String password)
    {
        User bean = new User(username,password);
        User user = SessionFactory.getInstance().selectOne("inveno.spider.common.mapper.UserMapper.select", bean);
        return user;
    }

}
