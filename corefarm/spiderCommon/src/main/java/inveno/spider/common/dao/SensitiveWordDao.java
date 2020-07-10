package inveno.spider.common.dao;

import inveno.spider.common.model.SensitiveWord;

import java.util.List;

public interface SensitiveWordDao
{
	List<SensitiveWord> find(int id);
}
