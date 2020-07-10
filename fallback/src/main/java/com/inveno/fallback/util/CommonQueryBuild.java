package com.inveno.fallback.util;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.server.PropertyContentLoader;

 
public class CommonQueryBuild {
	
	
	public static QueryBuilder getCommonQueryBuilder(String apps, List<Integer> list, boolean timeless, Double...args){
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if( timeless ){
			//低时效性
			BoolQueryBuilder boolQueryBuilder_lower = QueryBuilders.boolQuery();
			boolQueryBuilder_lower.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Integer.valueOf(PropertyContentLoader.getValues("TIMELESS_LOWER"))).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "-1"));  
	 		
			//高时效性
			BoolQueryBuilder boolQueryBuilder_high = QueryBuilders.boolQuery();
			boolQueryBuilder_high.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Integer.valueOf(PropertyContentLoader.getValues("TIMELESS_HIGH"))).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "1")); //timeprop
			
			//中时效性
			BoolQueryBuilder boolQueryBuilder_mid = QueryBuilders.boolQuery();
			boolQueryBuilder_mid.must(QueryBuilders.rangeQuery("release_time")
					.gte( DateUtils.addHours(new Date(), Integer.valueOf(PropertyContentLoader.getValues("TIMELESS_MID"))).getTime() / 1000)
					.lte(new Date().getTime()/1000))
			.must(QueryBuilders.termQuery("timeprop", "0")); //timeprop
			
			
			//三选一
			boolQueryBuilder.should(boolQueryBuilder_lower);
			boolQueryBuilder.should(boolQueryBuilder_high);
			boolQueryBuilder.should(boolQueryBuilder_mid);
			boolQueryBuilder.minimumShouldMatch("1");
		}
		
		//分类
		if (CollectionUtils.isNotEmpty(list)) {
			//QueryBuilder termsQuery_cat = QueryBuilders.termsQuery("category_"+PropertyContentLoader.getValues(FallbackConstant.ES_CATEGORY_VERSION), list);
			QueryBuilder termsQuery_cat = QueryBuilders.termsQuery("category_v28", list);
			boolQueryBuilder.must(termsQuery_cat);	
		}
		
		//app筛选条件 渠道
		if(apps != null){
//			if(apps.equals("huaweiglobal")||apps.equals("Huawei_colombia")||apps.equals("Huawei_other")){
//				apps = "noticias";
//			}
			QueryBuilder matchQuery = QueryBuilders.termQuery("firm_app.keyword", apps);
			boolQueryBuilder.must(matchQuery);
			
		}
		
		//个性设置
		if (args != null && args.length > 0){
			Double gmpValueUp = args[0];
			Double gmpValueDown = args[1];
			RangeQueryBuilder  gmp_rangeQuery = QueryBuilders.rangeQuery(FallbackConstant.REDIS_KEY_GMP_VALUE);
			gmp_rangeQuery.lte(gmpValueUp);
			gmp_rangeQuery.gte(gmpValueDown);
			boolQueryBuilder.must(gmp_rangeQuery);	
			
			Double dwelltimeValueUp = args[2];
			Double dwelltimeValueDown = args[3];
			RangeQueryBuilder dwelltime_rangeQuery = QueryBuilders.rangeQuery(FallbackConstant.REDIS_KEY_DWELLTIME_VALUE);
			dwelltime_rangeQuery.lte(dwelltimeValueUp);
			dwelltime_rangeQuery.gte(dwelltimeValueDown);
			boolQueryBuilder.must(dwelltime_rangeQuery);	
		}
		return boolQueryBuilder;
		
	}

}
