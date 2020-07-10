package inveno.spider.common.dao;

import java.util.List;

import inveno.spider.common.view.model.ProfileView;

public interface ProfileViewDao
{

    int save(ProfileView profileView);
    List<ProfileView> findByKeyword(String condition,int pageNo, int batchSize,final String sort,final String order);
    int getCount(String keyword);
    
    void deleteById(int id);
    void update(ProfileView profileView);
}
