package com.inveno.common.filter.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.inveno.common.Constants;

 
public class CommonQueryBuild {
	
	public static QueryBuilder getCommonQueryBuilder(String apps,List<Integer> list){
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		
		//低时效性
		BoolQueryBuilder boolQueryBuilder_lower = QueryBuilders.boolQuery();
		boolQueryBuilder_lower.must(QueryBuilders.rangeQuery("release_time")
				.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_LOWER).getTime() / 1000)
				.lte(new Date().getTime()/1000))
		.must(QueryBuilders.termQuery("timeprop", "-1"));  
 		
		//高时效性
		BoolQueryBuilder boolQueryBuilder_high = QueryBuilders.boolQuery();
		boolQueryBuilder_high.must(QueryBuilders.rangeQuery("release_time")
				.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_HIGH).getTime() / 1000)
				.lte(new Date().getTime()/1000))
		.must(QueryBuilders.termQuery("timeprop", "1")); //timeprop
		
		//中时效性
		BoolQueryBuilder boolQueryBuilder_mid = QueryBuilders.boolQuery();
		boolQueryBuilder_mid.must(QueryBuilders.rangeQuery("release_time")
				.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_MID).getTime() / 1000)
				.lte(new Date().getTime()/1000))
		.must(QueryBuilders.termQuery("timeprop", "0")); //timeprop
		
		
		//三选一
		boolQueryBuilder.should(boolQueryBuilder_lower);
		boolQueryBuilder.should(boolQueryBuilder_high);
		boolQueryBuilder.should(boolQueryBuilder_mid);
		boolQueryBuilder.minimumShouldMatch("1");
		
		
		
		//分类
		if(CollectionUtils.isNotEmpty(list)){
			QueryBuilder termsQuery_cat = QueryBuilders.termsQuery("city_id", list);
			boolQueryBuilder.must(termsQuery_cat);	
		}
		
		//app筛选条件 渠道
		if(apps != null){
			QueryBuilder matchQuery = QueryBuilders.termQuery("apps", apps);
			boolQueryBuilder.must(matchQuery);
		}
		return boolQueryBuilder;
		
	}
	
	
	public static QueryBuilder getCommonQueryBuilder(String apps,List<Integer> list,boolean timeless){
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		
		if( timeless ){
			
			//低时效性
			BoolQueryBuilder boolQueryBuilder_lower = QueryBuilders.boolQuery();
			boolQueryBuilder_lower.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_LOWER).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "-1"));  
	 		
			//高时效性
			BoolQueryBuilder boolQueryBuilder_high = QueryBuilders.boolQuery();
			boolQueryBuilder_high.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_HIGH).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "1")); //timeprop
			
			//中时效性
			BoolQueryBuilder boolQueryBuilder_mid = QueryBuilders.boolQuery();
			boolQueryBuilder_mid.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Constants.TIMELESS_MID).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "0")); //timeprop
			
			
			//三选一
			boolQueryBuilder.should(boolQueryBuilder_lower);
			boolQueryBuilder.should(boolQueryBuilder_high);
			boolQueryBuilder.should(boolQueryBuilder_mid);
			boolQueryBuilder.minimumShouldMatch("1");
		}
		
		//分类
		if(CollectionUtils.isNotEmpty(list)){
			QueryBuilder termsQuery_cat = QueryBuilders.termsQuery("category_id", list);
			boolQueryBuilder.must(termsQuery_cat);	
		}
		
		//app筛选条件 渠道
		if(apps != null){
			QueryBuilder matchQuery = QueryBuilders.termQuery("firm_app", apps);
			boolQueryBuilder.must(matchQuery);
		}
		return boolQueryBuilder;
		
	}
	
public static QueryBuilder getCommonQueryBuilder(String apps,List<Integer> list,String categoryVersion){
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		
		//分类
		if(CollectionUtils.isNotEmpty(list)){
			QueryBuilder termsQuery_cat = QueryBuilders.termsQuery(categoryVersion, list);
//			QueryBuilder termsQuery_cat = QueryBuilders.termsQuery("category_id", list);
			
			boolQueryBuilder.must(termsQuery_cat);	
		}
		
		//app筛选条件 渠道
		if(apps != null){
			QueryBuilder matchQuery = QueryBuilders.termQuery("firm_app", apps);
			boolQueryBuilder.must(matchQuery);
		}
		return boolQueryBuilder;
		
	}

}
