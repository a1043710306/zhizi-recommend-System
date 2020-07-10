package inveno.spider.common.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inveno.spider.common.SessionFactory;
import inveno.spider.common.dao.ProfileViewDao;
import inveno.spider.common.view.model.ProfileView;

public class ProfileViewDaoImpl implements ProfileViewDao
{

    @Override
    public int save(ProfileView profileView)
    {
        int effectRow = -1;
        try
        {
            effectRow = SessionFactory.getInstance().insert(
                    "inveno.spider.common.mapper.ProfileViewMapper.insert",
                    profileView);
        } catch (Exception e)
        {
            return -1;
        }

        return effectRow > 0 ? profileView.getId() : 0;

    }

    @Override
    public List<ProfileView> findByKeyword(String condition, int pageNo,
            int batchSize, final String sort, final String order)
    {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("offset", (pageNo - 1) * batchSize);
        parameter.put("size", batchSize);
        parameter.put("sort", sort);
        parameter.put("order", order);
        if (null != condition && condition.trim().length() > 0)
        {
            parameter.put("keyword", "%" + condition + "%");
        }

        List<ProfileView> list = SessionFactory.getInstance().selectList(
                "inveno.spide"
                        + "r.common.mapper.ProfileViewMapper.selectByKeyword",
                parameter);
        if (null == list || list.size() == 0)
        {
            return Collections.emptyList();
        }
        return list;

    }

    @Override
    public void deleteById(int id)
    {
        SessionFactory.getInstance().insert(
                "inveno.spider.common.mapper.ProfileViewMapper.deleteById", id);

    }

    @Override
    public void update(ProfileView profileView)
    {
        SessionFactory
                .getInstance()
                .update("inveno.spider.common.mapper.ProfileViewMapper.update",
                        profileView);
    }

    @Override
    public int getCount(String keyword)
    {
        int count = 0;
        String condition = null;
        if (null != keyword && keyword.trim().length() > 0)
        {
            condition = "%" + keyword + "%";
        }
        count = (Integer) SessionFactory.getInstance().selectOne(
                "inveno.spider.common.mapper.ProfileViewMapper.selectCount",
                condition);
        return count;
    }

}
