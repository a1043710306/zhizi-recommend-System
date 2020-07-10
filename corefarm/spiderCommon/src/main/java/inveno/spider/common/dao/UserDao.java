package inveno.spider.common.dao;

import inveno.spider.common.model.User;

public interface UserDao
{
    User find(String username,String password);

}
