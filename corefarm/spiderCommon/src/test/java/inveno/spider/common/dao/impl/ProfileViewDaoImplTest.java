package inveno.spider.common.dao.impl;

import static org.junit.Assert.*;

import java.util.List;

import inveno.spider.common.dao.ProfileViewDao;
import inveno.spider.common.view.model.ProfileView;

import org.junit.Test;

public class ProfileViewDaoImplTest
{
    
    private ProfileViewDao profileViewDao = new ProfileViewDaoImpl();

    @Test
    public void testSave()
    {
        ProfileView profile = new ProfileView();
        profile.setPubCode("test123");
        profile.setProfileName("test123");
        
        int id = profileViewDao.save(profile);
        System.out.println(id);
        
    }

    @Test
    public void testFindByKeyword()
    {
        List<ProfileView> list = profileViewDao.findByKeyword(null,1,20,null,null);
        for(ProfileView profile:list)
        {
            System.out.println(profile);
        }
    }

}
