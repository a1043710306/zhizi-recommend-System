package inveno.spider.common.dao;

import inveno.spider.common.model.CategoryVersionMapping;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public interface CategoryVersionMappingDao
{
    List<CategoryVersionMapping> selectAll();
}
