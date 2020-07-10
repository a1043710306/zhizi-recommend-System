package com.inveno.fallback.es.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.es.FallbackES;
import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.server.DataBaseContentLoader;
import com.inveno.fallback.server.PropertyContentLoader;
import com.inveno.fallback.util.CommonQueryBuild;
import com.inveno.fallback.util.ESQCondition;
import com.inveno.fallback.util.ElasticSearchUtil;

@Component
public class FallbackESImpl implements FallbackES {

	private static final Logger logger = LoggerFactory.getLogger(FallbackESImpl.class);
	
	public int udateEsContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public List<MessageContentEntry> getEsContents(String firmApp, Integer categoryId, String language, String contentType, List<String> alContentTypes, Double...args) {
		// TODO Auto-generated method stub
		//logger.info("FirmApp:" + firmApp +" categoryId:" + categoryId + " args:" + StringUtils.join(args, ","));
		//获取ctr过滤条件配置
		HashMap<String,String> fallbackCategoryMap = new HashMap<String,String>();
        String fallbackCategoryGnpThreshold = PropertyContentLoader.getValues(FallbackConstant.FALLBACK_CATEGORY_GNP_THRESHOLD);
        if(StringUtils.isNotEmpty(fallbackCategoryGnpThreshold)) {
        	String[] category = fallbackCategoryGnpThreshold.split(",");
    		for (String string : category) {
    			String[] s =  string.split(":");
    			fallbackCategoryMap.put(s[0], s[1]);
    		}
        }
        
        List<MessageContentEntry> result =  this.getEsInfoHaveCtrOderbyCtr(fallbackCategoryMap,firmApp, categoryId, language, contentType, alContentTypes, args);
        int infoSize = result.size();
        int esSize = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.ES_LOADER_COUNT_TOREDIS));
       
		
        //判断数据是否满足1000
        if(infoSize == esSize) {
        	return result;
        }else {
        	esSize = esSize - infoSize;
        	List<MessageContentEntry> resultAmount = this.getEsInfoMissingCtrByPublishTime(fallbackCategoryMap,esSize, firmApp, categoryId, language, contentType, alContentTypes, args);
        	result.addAll(resultAmount);
        }
        
//		try {
//			List<Integer> categoryIdList = new ArrayList<Integer>();
//			categoryIdList.add(categoryId);
//
//			//adult_score
//		    Double adultScoreValue = Double.valueOf(PropertyContentLoader.getValues("adult_score_value"));
//		    String adultScoreVersion = PropertyContentLoader.getValues("adult_score_version");
//		    
//			//设定ES查询条件
//			ESQCondition esqCondition = new ESQCondition();
//			
//			//公共条件查询如(app,category等)
//			BoolQueryBuilder qbuilder = (BoolQueryBuilder)CommonQueryBuild.getCommonQueryBuilder(firmApp, categoryIdList, false, args);
//			
//			//语言种类
//			String[] versions = PropertyContentLoader.getValues(FallbackConstant.ES_LANGUAGE_VERSION).split(":");
//			for (String version : versions) {
//				qbuilder.must(QueryBuilders.termQuery("language_"+version+"_main.keyword", language));
//			}
//			
//			//contontype and image count
//			int esQryImgCnt = 1;
//			if (contentType.equals("1")) {
//				for (String strContentType : alContentTypes){
//					if (strContentType.equals("1")){
//						continue;
//					}
//					QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", strContentType);
//					qbuilder.mustNot(matchQuery);
//				}
//				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_CNT));					
//			} else {
//				QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", contentType);
//				qbuilder.must(matchQuery);
//				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_VIDEO_CNT));
//			}
//			
//			QueryBuilder pic_rangeQuery = QueryBuilders.rangeQuery("body_image_count").gte(esQryImgCnt);
//			qbuilder.must(pic_rangeQuery);
//			
//			//N天之内时间
//			RangeQueryBuilder timeLimit = QueryBuilders.rangeQuery("publish_time_ts");
//			timeLimit.gte(DateUtils.addHours(new Date(), -DataBaseContentLoader.getCategoryExpiredHour(categoryId.toString())).getTime() / 1000);
//			timeLimit.lte(DateUtils.addHours(new Date(), 0).getTime() / 1000);
//			qbuilder.must(timeLimit);
//			
//			String adultScoreEsKey = "adult_score"+FallbackConstant.BLANK_VALUE+adultScoreVersion;
//			String ctrEsKey = firmApp+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//渠道CTR名称
//			String allCtrEsKey = FallbackConstant.ALL_ES_CTR_NAME+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//-1CTR名称
//			
//			//ctr > 0 
//			QueryBuilder ctr_rangeQuery = QueryBuilders.rangeQuery(allCtrEsKey).gt(0);
//			qbuilder.must(ctr_rangeQuery);
//			
//			//依 -1 gmp 排序
//			FieldSortBuilder fieldSort = SortBuilders.fieldSort(allCtrEsKey).order(SortOrder.DESC);
//			esqCondition.setSortBuilder(fieldSort);
//			
//			//查询数量
//			esqCondition.setSize(Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.ES_LOADER_COUNT_TOREDIS)));
//			esqCondition.setQueryBuilder(qbuilder);
//
//			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query condition :" + qbuilder.toString());
//
//			String[] arrAvailableField = new String[]{"group_id", "content_id", "publish_time_ts", "content_type", "link_type", "display_type", adultScoreEsKey, ctrEsKey, allCtrEsKey};
//			SearchResponse response = ElasticSearchUtil.getInstance().search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition, arrAvailableField);
//
//			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query count :" + response.getHits().getHits().length);
//			for (SearchHit hit : response.getHits().getHits()) {
//
//				Map<String,Object> esSourceMap = hit.getSource();
//
//				String contentId = (String)esSourceMap.get("content_id");
//				String groupId   = (String)esSourceMap.get("group_id");
//
//				String esAdultScore = (String)esSourceMap.get(adultScoreEsKey);
//				Double esDouAdultScore = NumberUtils.toDouble(esAdultScore, 0.0d);
//
//				//检查adult_score
//				if (esDouAdultScore >= adultScoreValue) {
//					logger.debug("search from es contentId = " + contentId + ",adultscore = "+esDouAdultScore+" is greater than setting value = "+adultScoreValue);
//					continue;
//				}
//
//				MessageContentEntry entry = new MessageContentEntry();
//				//取-1ctr 
//				if (esSourceMap.get(allCtrEsKey) == null) {
//					entry.setScore(0.0);
//				} else {
//					entry.setScore((Double)esSourceMap.get(allCtrEsKey));
//				}
//				
//				entry.setPublishTimeTs((Integer) esSourceMap.get("publish_time_ts"));
//				entry.setContentType((String)esSourceMap.get("content_type"));
//				entry.setLinkType((Integer)esSourceMap.get("link_type"));
//				entry.setDisplayType((Integer)esSourceMap.get("display_type"));
//				entry.setId(contentId);
//				entry.setGroupId(groupId == null ? contentId : groupId);
//				result.add(entry);
//			}
//		} catch (Exception e) {
//			logger.error(" FirmApp:" + firmApp +",categoryId :" + categoryId+" get new info happen error:",e);
//		}
		return result;
	}
	
	/**
	 * 
	 * @author lm
	 * @return SearchResponse
	 *
	 */
	private List<MessageContentEntry> getEsInfoHaveCtrOderbyCtr(HashMap<String,String> fallbackCategoryMap, String firmApp, Integer categoryId, String language, String contentType, List<String> alContentTypes, Double...args) {
		logger.info("FirmApp:" + firmApp +" categoryId:" + categoryId + " args:" + StringUtils.join(args, ","));
        List<MessageContentEntry> result =  new ArrayList<MessageContentEntry>();
		try {
			List<Integer> categoryIdList = new ArrayList<Integer>();
			categoryIdList.add(categoryId);

			//adult_score
		    Double adultScoreValue = Double.valueOf(PropertyContentLoader.getValues("adult_score_value"));
		    String adultScoreVersion = PropertyContentLoader.getValues("adult_score_version");
		    
			//设定ES查询条件
			ESQCondition esqCondition = new ESQCondition();
			
			//公共条件查询如(app,category等)
			BoolQueryBuilder qbuilder = (BoolQueryBuilder)CommonQueryBuild.getCommonQueryBuilder(firmApp, categoryIdList, false, args);
			
			//语言种类
			String[] versions = PropertyContentLoader.getValues(FallbackConstant.ES_LANGUAGE_VERSION).split(":");
			for (String version : versions) {
				qbuilder.must(QueryBuilders.termQuery("language_"+version+"_main.keyword", language));
			}
			
			//contontype and image count
			int esQryImgCnt = 1;
			if (contentType.equals("1")) {
				for (String strContentType : alContentTypes){
					if (strContentType.equals("1")){
						continue;
					}
					QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", strContentType);
					qbuilder.mustNot(matchQuery);
				}
				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_CNT));					
			} else {
				QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", contentType);
				qbuilder.must(matchQuery);
				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_VIDEO_CNT));
			}
			
			QueryBuilder pic_rangeQuery = QueryBuilders.rangeQuery("body_image_count").gte(esQryImgCnt);
			qbuilder.must(pic_rangeQuery);
			
			//N天之内时间
			RangeQueryBuilder timeLimit = QueryBuilders.rangeQuery("publish_time_ts");
			timeLimit.gte(DateUtils.addHours(new Date(), -DataBaseContentLoader.getCategoryExpiredHour(categoryId.toString())).getTime() / 1000);
			timeLimit.lte(DateUtils.addHours(new Date(), 0).getTime() / 1000);
			qbuilder.must(timeLimit);
			
			String adultScoreEsKey = "adult_score"+FallbackConstant.BLANK_VALUE+adultScoreVersion;
			String ctrEsKey = firmApp+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//渠道CTR名称
			String allCtrEsKey = FallbackConstant.ALL_ES_CTR_NAME+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//-1CTR名称
			
			//ctr 过滤条件
			String ctr = fallbackCategoryMap.get(categoryId+"");
			if(StringUtils.isNotEmpty(ctr)) {
				QueryBuilder ctr_rangeQuery = QueryBuilders.rangeQuery(allCtrEsKey).gt(ctr);
				qbuilder.must(ctr_rangeQuery);
			}
			
			//依 -1 gmp 排序
			FieldSortBuilder fieldSort = SortBuilders.fieldSort(allCtrEsKey).order(SortOrder.DESC);
			esqCondition.setSortBuilder(fieldSort);
			
			//查询数量
			esqCondition.setSize(Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.ES_LOADER_COUNT_TOREDIS)));
			esqCondition.setQueryBuilder(qbuilder);

			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query condition :" + qbuilder.toString());

			String[] arrAvailableField = new String[]{"group_id", "content_id", "publish_time_ts", "content_type", "link_type", "display_type", adultScoreEsKey, ctrEsKey, allCtrEsKey};
			SearchResponse response = ElasticSearchUtil.getInstance().search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition, arrAvailableField);

			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query count :" + response.getHits().getHits().length);
			for (SearchHit hit : response.getHits().getHits()) {

				Map<String,Object> esSourceMap = hit.getSource();

				String contentId = (String)esSourceMap.get("content_id");
				String groupId   = (String)esSourceMap.get("group_id");

				String esAdultScore = (String)esSourceMap.get(adultScoreEsKey);
				Double esDouAdultScore = NumberUtils.toDouble(esAdultScore, 0.0d);

				//检查adult_score
				if (esDouAdultScore >= adultScoreValue) {
					logger.debug("search from es contentId = " + contentId + ",adultscore = "+esDouAdultScore+" is greater than setting value = "+adultScoreValue);
					continue;
				}

				MessageContentEntry entry = new MessageContentEntry();
				//取-1ctr 
				if (esSourceMap.get(allCtrEsKey) == null) {
					entry.setScore(0.0);
				} else {
					entry.setScore((Double)esSourceMap.get(allCtrEsKey));
				}
				
				entry.setPublishTimeTs((Integer) esSourceMap.get("publish_time_ts"));
				entry.setContentType((String)esSourceMap.get("content_type"));
				entry.setLinkType((Integer)esSourceMap.get("link_type"));
				entry.setDisplayType((Integer)esSourceMap.get("display_type"));
				entry.setId(contentId);
				entry.setGroupId(groupId == null ? contentId : groupId);
				result.add(entry);
			}
		} catch (Exception e) {
			logger.error(" FirmApp:" + firmApp +",categoryId :" + categoryId+" get new info happen error:",e);
		}
		return result;
		
	}
	

	/**
	 * 
	 * @author lm
	 * @return SearchResponse
	 *
	 */
	private List<MessageContentEntry> getEsInfoMissingCtrByPublishTime(HashMap<String,String> fallbackCategoryMap, int size, String firmApp, Integer categoryId, String language, String contentType, List<String> alContentTypes, Double...args) {
		
		logger.info("FirmApp:" + firmApp +" categoryId:" + categoryId + " args:" + StringUtils.join(args, ","));
        List<MessageContentEntry> result =  new ArrayList<MessageContentEntry>();
		try {
			List<Integer> categoryIdList = new ArrayList<Integer>();
			categoryIdList.add(categoryId);

			//adult_score
		    Double adultScoreValue = Double.valueOf(PropertyContentLoader.getValues("adult_score_value"));
		    String adultScoreVersion = PropertyContentLoader.getValues("adult_score_version");
		    
			//设定ES查询条件
			ESQCondition esqCondition = new ESQCondition();
			
			//公共条件查询如(app,category等)
			BoolQueryBuilder qbuilder = (BoolQueryBuilder)CommonQueryBuild.getCommonQueryBuilder(firmApp, categoryIdList, false, args);
			
			//语言种类
			String[] versions = PropertyContentLoader.getValues(FallbackConstant.ES_LANGUAGE_VERSION).split(":");
			for (String version : versions) {
				qbuilder.must(QueryBuilders.termQuery("language_"+version+"_main.keyword", language));
			}
			
			//contontype and image count
			int esQryImgCnt = 1;
			if (contentType.equals("1")) {
				for (String strContentType : alContentTypes){
					if (strContentType.equals("1")){
						continue;
					}
					QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", strContentType);
					qbuilder.mustNot(matchQuery);
				}
				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_CNT));					
			} else {
				QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", contentType);
				qbuilder.must(matchQuery);
				esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_VIDEO_CNT));
			}
			
			QueryBuilder pic_rangeQuery = QueryBuilders.rangeQuery("body_image_count").gte(esQryImgCnt);
			qbuilder.must(pic_rangeQuery);
			
			//N天之内时间
			RangeQueryBuilder timeLimit = QueryBuilders.rangeQuery("publish_time_ts");
			timeLimit.gte(DateUtils.addHours(new Date(), -DataBaseContentLoader.getCategoryExpiredHour(categoryId.toString())).getTime() / 1000);
			timeLimit.lte(DateUtils.addHours(new Date(), 0).getTime() / 1000);
			qbuilder.must(timeLimit);
			
			String adultScoreEsKey = "adult_score"+FallbackConstant.BLANK_VALUE+adultScoreVersion;
			String ctrEsKey = firmApp+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//渠道CTR名称
			String allCtrEsKey = FallbackConstant.ALL_ES_CTR_NAME+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//-1CTR名称
			
			//ctr 过滤条件
			String ctr = fallbackCategoryMap.get(categoryId+"");
			if(StringUtils.isNotEmpty(ctr)) {
				QueryBuilder ctr_rangeQuery = QueryBuilders.rangeQuery(allCtrEsKey).gt(ctr);
				qbuilder.mustNot(ctr_rangeQuery);
			}
			
			//依 -1 gmp 排序
			FieldSortBuilder fieldSort = SortBuilders.fieldSort("publish_time_ts").order(SortOrder.DESC);
			esqCondition.setSortBuilder(fieldSort);
			
			//查询数量
			esqCondition.setSize(size);
			esqCondition.setQueryBuilder(qbuilder);

			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query condition :" + qbuilder.toString());

			String[] arrAvailableField = new String[]{"group_id", "content_id", "publish_time_ts", "content_type", "link_type", "display_type", adultScoreEsKey, ctrEsKey, allCtrEsKey};
			SearchResponse response = ElasticSearchUtil.getInstance().search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition, arrAvailableField);

			logger.info("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query count :" + response.getHits().getHits().length);
			for (SearchHit hit : response.getHits().getHits()) {

				Map<String,Object> esSourceMap = hit.getSource();

				String contentId = (String)esSourceMap.get("content_id");
				String groupId   = (String)esSourceMap.get("group_id");

				String esAdultScore = (String)esSourceMap.get(adultScoreEsKey);
				Double esDouAdultScore = NumberUtils.toDouble(esAdultScore, 0.0d);

				//检查adult_score
				if (esDouAdultScore >= adultScoreValue) {
					logger.debug("search from es contentId = " + contentId + ",adultscore = "+esDouAdultScore+" is greater than setting value = "+adultScoreValue);
					continue;
				}

				MessageContentEntry entry = new MessageContentEntry();
				//取-1ctr 
				if (esSourceMap.get(allCtrEsKey) == null) {
					entry.setScore(0.0);
				} else {
					entry.setScore((Double)esSourceMap.get(allCtrEsKey));
				}
				
				entry.setPublishTimeTs((Integer) esSourceMap.get("publish_time_ts"));
				entry.setContentType((String)esSourceMap.get("content_type"));
				entry.setLinkType((Integer)esSourceMap.get("link_type"));
				entry.setDisplayType((Integer)esSourceMap.get("display_type"));
				entry.setId(contentId);
				entry.setGroupId(groupId == null ? contentId : groupId);
				result.add(entry);
			}
		} catch (Exception e) {
			logger.error(" FirmApp:" + firmApp +",categoryId :" + categoryId+" get new info happen error:",e);
		}
		return result;
		
	}

	//search and update es condition
	public SearchResponse getEsContents(String... args) {
		// TODO Auto-generated method stub
		logger.info(" args:" + args);
		SearchResponse response = null;
		try {
			
			//设定ES查询条件
			ESQCondition esqCondition = new ESQCondition();
			
			//公共条件查询如(app,category等)
			BoolQueryBuilder qbuilder = new BoolQueryBuilder();
			
			//语言种类
			//qbuilder.must(QueryBuilders.termQuery("language_v1_main", PropertyContentLoader.getValues(FallbackConstant.LANGUAGE).toLowerCase()));
			
			//2天之内时间
			QueryBuilder timeLimit = QueryBuilders.rangeQuery("publish_time_ts").gte(DateUtils.addHours(new Date(), -48).getTime() / 1000);
			qbuilder.must(timeLimit);
			
			// 图片数量
			/*int esQryImgCnt = Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.QUR_IMG_CNT));
			QueryBuilder pic_rangeQuery = QueryBuilders.rangeQuery("body_image_count").gte(esQryImgCnt);
			qbuilder.must(pic_rangeQuery);*/
			
			//contentid 排序
			FieldSortBuilder content_id_fieldSort= SortBuilders.fieldSort("content_id").order( SortOrder.DESC);
			esqCondition.setSortBuilder(content_id_fieldSort);
			
			//查询数量
			esqCondition.setSize(Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.ES_LOADER_COUNT_ESUPDATE)));
			
			esqCondition.setQueryBuilder(qbuilder);
			
			logger.info("es query condition :" + esqCondition.toString());
			
			response = ElasticSearchUtil.getInstance().search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition);
			
		} catch (Exception e) {
			logger.error(" args:"+args,e);
		}
		return response;
	}

}
