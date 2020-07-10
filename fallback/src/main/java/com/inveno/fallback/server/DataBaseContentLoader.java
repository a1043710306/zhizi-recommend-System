package com.inveno.fallback.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.dao.FallbackDao;
import com.inveno.fallback.model.CategoryEntry;

@Component
public class DataBaseContentLoader implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(DataBaseContentLoader.class);
	
	private static Map<String,List<Integer>> categoryDatas = new HashMap<String,List<Integer>>();
	
	private static Map<String,Integer>  categoryMaps = new HashMap<String,Integer>();
	
	@Autowired
	private FallbackDao fallbackDao;
	
	public void loadService() {
		// TODO Auto-generated method stub
		
		//加载数据库中t_category信息
		logger.debug("start load t_category table datas...");
		List<CategoryEntry> categoryModelList = fallbackDao.queryCategoryids();
		List<Integer> categorids = new ArrayList<Integer>();
		for(CategoryEntry categoryEntry : categoryModelList){
			categorids.add(categoryEntry.getCategoryId());
			categoryMaps.put(categoryEntry.getCategoryId().toString(), categoryEntry.getExpiryHour());
		}
		categoryDatas.put(FallbackConstant.T_CATEGORYID_VALUES,categorids);
		
		
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	public static List<Integer> getValues(String key){
		return categoryDatas.get(key);
	}
	
	public static Integer getCategoryExpiredHour(String categoryId){
		return categoryMaps.get(categoryId);
	}

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		loadService();
	}

}
