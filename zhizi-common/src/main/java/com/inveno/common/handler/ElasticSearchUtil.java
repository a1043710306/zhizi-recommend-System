package com.inveno.common.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.TermsEnum;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.sort.SortBuilder;

import com.inveno.common.Constants;
import com.inveno.common.bean.ESQCondition;
 
public class ElasticSearchUtil {

	private static Client client;
	
	private static ElasticSearchUtil elasticSearchHandler = null;
	
	private static Log logger = LogFactory.getLog(ElasticSearchUtil.class);
	
	public static ElasticSearchUtil getInstances() {
		if (elasticSearchHandler == null) {
			elasticSearchHandler = new ElasticSearchUtil();
		}
		return elasticSearchHandler;
	}
	
	
	static{
		logger.info("init ElasticSearchUtil");;
		try {
			Settings settings = Settings.settingsBuilder()
			        .put("cluster.name", Constants.ES_CLUTER_NAME)
			        .put("client.transport.sniff", true)
			       .put("threadpool.bulk.type",  "fixed")
				     .put("threadpool.bulk.size" ,1000)
				     .put("threadpool.bulk.queue_size", 1000)
				     .put("threadpool.index.type" , "fixed")
				     .put("threadpool.index.size" , 1000)
				     .put("threadpool.index.queue_size" , 1000)
				     .put("threadpool.search.type",  "fixed")
				     .put("threadpool.search.size" ,1000)
				     .put("threadpool.search.queue_size", 1000)
			        .build();
			client = TransportClient
					.builder()
					.settings(settings)
					.build()
					.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(Constants.ES_HOST),Constants.ES_PORT) )
					//.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(Constants.ES_HOST1),Constants.ES_PORT1) )
					//.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(Constants.ES_HOST2),Constants.ES_PORT2) )
					//.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(Constants.ES_HOST3),Constants.ES_PORT3) )
					;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private ElasticSearchUtil() {
		
		
		/*Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", Constants.ES_CLUTER_NAME)
		        .put("index.store.type", "memory")
		        .put("path.home", "/usr/local/elasticsearch-robinson/elasticsearch-rtfbb")
		        //.put("client.transport.sniff", true)
		        .build();
		client = NodeBuilder.nodeBuilder().settings(settings)
				.clusterName(Constants.ES_CLUTER_NAME).node().client();*/
	}
	
	/**
	 * 生成索引方法,传入为bytes数组
	 * @param json
	 * @param index
	 * @param type
	 * @param _id
	 * @return
	 */
	public IndexResponse generateIndex(byte[] json,String index,String type,String _id ) {
 		IndexResponse response = client
				.prepareIndex(index, type , _id)
				.setRefresh(true)
				.setSource(json)
				.execute()
				.actionGet();
		return response ;
	}
	
	
	/**
	 * 
	 * @param json
	 * @param index
	 * @param type
	 * @param _id
	 * @return
	 */
	public IndexResponse generateIndex(String json,String index,String type,String _id ) {
		
 		IndexResponse response = client
				.prepareIndex(index, type , _id)
				.setRefresh(true)
				.setSource(json)
				.execute()
				.actionGet();
		return response ;
	}
	
	/**
	 * 删除索引,根据_id删除索引
	 * @param index
	 * @param type
	 * @param _id
	 * @return
	 */
	public DeleteResponse deleteIndex(String index ,String type,String _id ){
		DeleteResponse response = client.prepareDelete(index, type, _id).get();
		return response;
	}
	
	
	/**
	 * searche the new from es;
	 * 查询索引,
	 * eSQCondition需要传入大小,查询条件;
	 * @param indexs :the es index
	 * @param types :the es type
	 * @param eSQCondition :size default 100,QueryBuilder the main condition ,the class implemented QueryBuilder
	 *  SortBuilder:the field sort
	 * @return
	 */
	public SearchResponse search(String[] indexs,String[] types,ESQCondition eSQCondition){
		
		
		SearchResponse response = null;
		if (null == eSQCondition.getSortBuilder()) {

			response = client.prepareSearch(indexs).setTypes(types)
			// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(eSQCondition.getQueryBuilder())// the search
																// condition
					.setPostFilter(eSQCondition.getPostFilter())// optional
					.setSize(eSQCondition.getSize())// the get size
					.execute().actionGet();
		} else {

			if (null != eSQCondition.getSortBuilders()) {

				SearchRequestBuilder searchRequestBuilder = client
						.prepareSearch(indexs).setTypes(types)
						// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(eSQCondition.getQueryBuilder())// the search
																	// condition
						.setSize(eSQCondition.getSize());

				for (SortBuilder sortBuilder : eSQCondition.getSortBuilders()) {
					searchRequestBuilder.addSort(sortBuilder);
				}
				response = searchRequestBuilder.execute().actionGet();

			} else {

				response = client.prepareSearch(indexs).setTypes(types)
				// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(eSQCondition.getQueryBuilder())// the search
																	// condition
						.setPostFilter(eSQCondition.getPostFilter())// optional
						.setSize(eSQCondition.getSize())// the get size
						.addSort(eSQCondition.getSortBuilder())// the order by
																// item
						.execute().actionGet();
			}

		}
		
 		return response;
	}
	
	/**
	 * 查询索引,资讯只查询id字段时候,传入isSource参数
	 * @param indexs
	 * @param types
	 * @param eSQCondition
	 * @param isSource
	 * @return
	 */
	public SearchResponse search(String[] indexs,String[] types,ESQCondition eSQCondition,boolean isSource){
		isSource = true;
		
		SearchResponse response;
		if (null != eSQCondition.getSortBuilder()) {
			response = client.prepareSearch(indexs).setTypes(types)
			// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(eSQCondition.getQueryBuilder())// the search
																// condition
					.setPostFilter(eSQCondition.getPostFilter())// optional
					.setSize(eSQCondition.getSize())// the get size
					.addSort(eSQCondition.getSortBuilder())// the order by item
					.setFetchSource(new String[] { "id" }, null)// add field
					.execute().actionGet();
		} else {

			if (null != eSQCondition.getSortBuilders()) {

				SearchRequestBuilder searchRequestBuilder = client
						.prepareSearch(indexs).setTypes(types)
						// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(eSQCondition.getQueryBuilder())// the search
																	// condition
						.setSize(eSQCondition.getSize());

				for (SortBuilder sortBuilder : eSQCondition.getSortBuilders()) {
					searchRequestBuilder.addSort(sortBuilder);
				}
				response = searchRequestBuilder.execute().actionGet();

			} else {
				response = client.prepareSearch(indexs).setTypes(types)
				// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(eSQCondition.getQueryBuilder())// the search
																	// condition
						.setPostFilter(eSQCondition.getPostFilter())// optional
						.setSize(eSQCondition.getSize())// the get size
						// .addSort(eSQCondition.getSortBuilder())//the order by
						// item
						.setFetchSource(new String[] { "id" }, null)// add field
						.execute().actionGet();
			}

		}
 		
 		/*List<String> idlist = new ArrayList<String>();
 		for (SearchHit hit : response.getHits().getHits()) {0
 			idlist.add(hit.getId());
		}*/
 		return response;
	}
	
	/**
	 * 查询索引,资讯只查询id字段时候,传入fields 为需要查询的字段名称数组
	 * @param indexs
	 * @param types
	 * @param eSQCondition
	 * @param isSource
	 * @return
	 */
	public SearchResponse search(String[] indexs,String[] types,ESQCondition eSQCondition,String[] fields){
		
		SearchRequestBuilder searchRequestBuilder = client
				.prepareSearch(indexs).setTypes(types)
				.setQuery(eSQCondition.getQueryBuilder())// the search
				//.setPostFilter(eSQCondition.getPostFilter())
				.setSize(eSQCondition.getSize());
		
		if( null != fields &&  fields.length > 0  ){
			searchRequestBuilder.setFetchSource(fields, null);
		}
		
		if( CollectionUtils.isNotEmpty(eSQCondition.getSortBuilders())  ){
			for (SortBuilder sortBuilder : eSQCondition.getSortBuilders()) {
				searchRequestBuilder.addSort(sortBuilder);
			}
		}else if( null !=  eSQCondition.getSortBuilder() ){
			searchRequestBuilder.addSort(eSQCondition.getSortBuilder());
		}
		
		return searchRequestBuilder.execute().actionGet();
	}
	
	/**
	 * 批量查询,此方法存在性能问题,进行注释;
	 * @param index
	 * @param type
	 * @param queryBuilders
	 * @param sizes
	 * @return
	 */
	/*public MultiSearchResponse mulstSearch(String index,String type,QueryBuilder [] queryBuilders,int[] sizes){
		
		MultiSearchRequestBuilder mr = client.prepareMultiSearch();
		for (int i = 0; i < queryBuilders.length; i++) {
			SearchRequestBuilder sr = client.prepareSearch(index).setTypes(type).setQuery(queryBuilders[i]).setSize(sizes[i]).setFetchSource(new String[]{"id"}, null);
			mr.add(sr);
		}
		
		return mr.execute().actionGet();
	}*/
	
	public BulkResponse mulstUpdate(HashMap<String,Double> map,String index,String type){
		BulkRequestBuilder bulkRequest  = client.prepareBulk();
 		
		for( String infoid : map.keySet()){
			Map json = new HashMap();
			json.put("ctr", map.get(infoid));
  			bulkRequest.add(client.prepareIndex(index, type, infoid).setSource(json)  );
 		}
		
		/*bulkRequest.add(client.prepareUpdate("schannel", "schannel", String.valueOf(infoid)).setDoc(json));
		bulkRequest.add(client.prepareUpdate("schannel", "schannel", String.valueOf(infoid)).setDoc(json));*/
		//bulkRequest.add(client.prepareIndex(index, type, String.valueOf(infoid)).setSource(json)  );
		BulkResponse bulkResponse = bulkRequest.get();
		
		for (BulkItemResponse bulkItemResponse : bulkResponse) {
			System.out.println(bulkItemResponse.isFailed());
			
		}
		
		return bulkResponse;
		
	}
	
	
	/**
	 * 
	 * @param index :es index
	 * @param type :es type
	 * @param infoid :the es infoid
	 * @param infoid_2 :another es infoid
	 * @param field : the field to compare,eg:title
	 * @return ture:Duplicated false: not Duplicated
	 * @throws IOException
	 */
	public boolean isDuplicated(String index,String type,String infoid, String infoid_2,String field) {
		
		List<String> list1;
		List<String> list2;
		try {
			list1 = listofTermVector(index,type,infoid, field);
			list2 = listofTermVector(index,type,infoid_2, field);
			return isDuplicated(list1, list2);
		} catch (IOException e) {
			e.printStackTrace();
		}
 		return false;
 	}

	/**
	 * 是否重复
	 * @param list1
	 * @param list2
	 * @return
	 */
	private boolean isDuplicated(List<String> list1, List<String> list2) {
		List<String> intersection = intersection(list1, list2);
		List<String> union = union(list1, list2);
		
		double threshold = 0.45;
		double jaccard = intersection.size() * 1.0 / union.size();
		if (intersection.size() > 0 && jaccard >= threshold) {
 			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 进行两个list  交集
	 * @param list1
	 * @param list2
	 * @return
	 */
	private <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();
		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}
		return list;
	}
	
	/***
	 * 并行两个list 交集
	 * @param list1
	 * @param list2
	 * @return
	 */
	private <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();
		set.addAll(list1);
		set.addAll(list2);
		return new ArrayList<T>(set);
	}
	
	/**
	 * 获取TermVector
	 * @param index
	 * @param type
	 * @param infoid
	 * @param field
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> listofTermVector(String index, String type, String infoid, String field) throws IOException {

		TermVectorsResponse response = client
				.prepareTermVectors(index, type, infoid).setPayloads(false)
				.setOffsets(false).setPositions(false)
				.setFieldStatistics(false).setTermStatistics(false)
				.setSelectedFields(field).execute().actionGet();

		TermsEnum iterator = response.getFields().terms(field).iterator();
		Set<T> set = new HashSet<T>();
		while ((iterator.next()) != null) {
			set.add((T) iterator.term().utf8ToString());
		}
		return new ArrayList<T>(set);
	}
	

	
}
