package inveno.spider.common.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.SeedDao;
import inveno.spider.common.model.Seed;

public class SeedDaoImpl implements SeedDao
{

    @Override
    public List<Seed> findByName(String name, int pageNo, int batchSize,
            String sort, String order)
    {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("offset", (pageNo-1)*batchSize);
        parameter.put("size", batchSize);
        parameter.put("sort", sort);
        parameter.put("order", order);
        if(null!=name && name.trim().length()>0)
        {
            parameter.put("keyword", "%"+name+"%");
        }
        

        List<Seed> seeds = SessionFactory.getInstance().selectList(
                "inveno.spider.common.mapper.SeedMapper.findByName",
                parameter);
        if (null == seeds || seeds.size() == 0)
        {
            return Collections.emptyList();
        }
        return seeds;
    }

    @Override
    public int getCount(String keyword)
    {
        String value=null;
        if(null!=keyword && keyword.trim().length()>0)
        {
            value = "%"+keyword+"%";
        }
        
        int count = 0;
        count =(Integer) SessionFactory.getInstance().selectOne(
                "inveno.spider.common.mapper.SeedMapper.selectCount", value);
        return count;
    }

    @Override
    public List<Seed> loadByProfileId(int profileId)
    {
        if (profileId <= 0)
        {
            return Collections.emptyList();
        }
        List<Seed> seeds = SessionFactory.getInstance().selectList(
                "inveno.spider.common.mapper.SeedMapper.selectByProfileId",
                profileId);
        if (null == seeds || seeds.size() == 0)
        {
            return Collections.emptyList();
        }
        return seeds;
    }

    @Override
    public void updateProfileId(int profileId)
    {
        if (profileId > 0)
        {
            SessionFactory.getInstance().update(
                    "inveno.spider.common.mapper.SeedMapper.updateProfileId",
                    profileId);
        }
    }

    @Override
    public void updateProfileIdByIds(String profileId, String... ids)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("profileId", profileId);
        map.put("seeds", ids);
        SessionFactory.getInstance().update(
                "inveno.spider.common.mapper.SeedMapper.updateProfileIdById",
                map);

    }

    @Override
    public boolean exists(String url)
    {

        Integer id = SessionFactory.getInstance().selectOne(
                "inveno.spider.common.mapper.SeedMapper.findByUrl",
                url);
        
        return id==null || id.intValue()<=0?false:true ;
    }

}
