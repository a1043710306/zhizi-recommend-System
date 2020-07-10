package inveno.spider.common.dao;

import inveno.spider.common.model.Category;

import java.util.List;

public interface CategoryDao
{
    List<Category> find();
    public List<Category> listCategory(int version);
}
