package inveno.spider.common.dao.impl;

import static org.junit.Assert.*;
import inveno.spider.common.dao.UserDao;
import inveno.spider.common.model.User;

import org.junit.Test;

public class UserDaoImplTest
{

    @Test
    public void testFind()
    {
        UserDao dao = new UserDaoImpl();
        User user = dao.find("kaifa", "kaifa123456");
        if(user!=null)
        {
            System.out.println(user.getUserName());
        }
    }

}
