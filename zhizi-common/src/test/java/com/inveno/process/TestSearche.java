package com.inveno.process;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.inveno.common.bean.ESQCondition;
import com.inveno.common.handler.ElasticSearchUtil;

public class TestSearche {
	
	public static void main(String[] args) throws IOException {
		
		
 		/*String[] indexs = {"schannel"};
 		String[] types = {"schannel"};
 		
 		
 		ESQCondition eSQCondition= new ESQCondition();
 		
 		eSQCondition.setSize(10);
 		eSQCondition.setSortBuilder(SortBuilders.fieldSort("release_time").order(SortOrder.DESC));
 		
 		//eSQCondition.setQueryBuilder(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("content", "股市")));
 		
 		eSQCondition.setQueryBuilder(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("category_name", "股票")));
  		
 		System.out.println(ElasticSearchUtil.getInstances().search(indexs, types, eSQCondition));*/
		
		System.out.println(ElasticSearchUtil.getInstances().isDuplicated("schannel", "schannel", "30450130", "30450131", "title"));
		
	}

}
