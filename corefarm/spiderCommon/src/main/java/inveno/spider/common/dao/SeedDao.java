package inveno.spider.common.dao;

import java.util.List;

import inveno.spider.common.model.Seed;

public interface SeedDao
{

    List<Seed> loadByProfileId(int profileId);
    
    List<Seed> findByName(String name,int pageNo, int batchSize,final String sort,final String order);
    
    /**
     * Get count of all.
     * @return
     */
    int getCount(String keyword);
    
    void updateProfileIdByIds(String profileId,String... ids);
    
    void updateProfileId(int profileId);
    
    boolean exists(String url);
}
