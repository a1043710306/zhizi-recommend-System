package com.inveno.sire.util;

import com.inveno.sire.constant.SireConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.TermsEnum;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

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
		logger.info("init ElasticSearchUtil");
		try {
			String eshostports = LoadProfileToMember.getValues(SireConstant.ES_HOST_PORT);
			String[] hostports = eshostports.split(",");
			Settings settings = Settings.builder()
			        .put("cluster.name",LoadProfileToMember.getValues(SireConstant.ES_CLUTER_NAME))
					.put("client.transport.sniff", true)
					.put("thread_pool.bulk.size", 5)
					.put("thread_pool.bulk.queue_size", 1000)
					.put("thread_pool.index.size", 5)
					.put("thread_pool.index.queue_size", 1000)
					.put("thread_pool.search.size", 5)
					.put("thread_pool.search.queue_size", 1000)
					.build();
			TransportClient transportClient = new PreBuiltTransportClient(settings);
			for (String hostport : hostports) {
				String[] s =  hostport.split(":");
				String host = s[0];
				String port = s[1];
				client = transportClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host, Integer.parseInt(port))));
			}
		} catch (Exception e) {
			logger.debug("ES exception detail:",e);
		}
		
	}

   //原生态ES Client
   public static Client getClient(){
	   return client;
   }
	
	//单列模式
	private ElasticSearchUtil() {
		
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
				.setRefreshPolicy("true")
				.setSource(json)
				.execute()
				.actionGet();
		return response ;
	}
	
	
	/**
	 * @param json
	 * @param index
	 * @param type
	 * @param _id
	 * @return
	 */
	public IndexResponse generateIndex(String json,String index,String type,String _id ) {
		
 		IndexResponse response = client
				.prepareIndex(index, type , _id)
				.setRefreshPolicy("true")
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
					.setQuery(eSQCondition.getQueryBuilder())// the search
					.setPostFilter(eSQCondition.getPostFilter())// optional
					.setSize(eSQCondition.getSize())// the get size
					.execute().actionGet();
		} else {

			if (null != eSQCondition.getSortBuilders()) {

				SearchRequestBuilder searchRequestBuilder = client
						.prepareSearch(indexs).setTypes(types)
						.setQuery(eSQCondition.getQueryBuilder())// the search
						.setSize(eSQCondition.getSize());

				for (SortBuilder sortBuilder : eSQCondition.getSortBuilders()) {
					searchRequestBuilder.addSort(sortBuilder);
				}
				response = searchRequestBuilder.execute().actionGet();

			} else {

				response = client.prepareSearch(indexs).setTypes(types)
						.setQuery(eSQCondition.getQueryBuilder())// the search
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
					.setQuery(eSQCondition.getQueryBuilder())// the search
					.setPostFilter(eSQCondition.getPostFilter())// optional
					.setSize(eSQCondition.getSize())// the get size
					.addSort(eSQCondition.getSortBuilder())// the order by item
					.setFetchSource(new String[] { "id" }, null)// add field
					.execute().actionGet();
		} else {

			if (null != eSQCondition.getSortBuilders()) {

				SearchRequestBuilder searchRequestBuilder = client
						.prepareSearch(indexs).setTypes(types)
						.setQuery(eSQCondition.getQueryBuilder())// the search
						.setSize(eSQCondition.getSize());
				for (SortBuilder sortBuilder : eSQCondition.getSortBuilders()) {
					searchRequestBuilder.addSort(sortBuilder);
				}
				response = searchRequestBuilder.execute().actionGet();

			} else {
				response = client.prepareSearch(indexs).setTypes(types)
						.setQuery(eSQCondition.getQueryBuilder())// the search
						.setPostFilter(eSQCondition.getPostFilter())// optional
						.setSize(eSQCondition.getSize())// the get size
						.setFetchSource(new String[] { "id" }, null)// add field
						.execute().actionGet();
			}

		}
 		
 		return response;
	}
	
	/**
	 * 查询索引,资讯只查询id字段时候,传入fields 为需要查询的字段名称数组
	 * @param indexs
	 * @param types
	 * @param eSQCondition
	 * @return SearchResponse
	 */
	public SearchResponse search(String[] indexs,String[] types,ESQCondition eSQCondition,String[] fields){
		
		SearchRequestBuilder searchRequestBuilder = client
				.prepareSearch(indexs).setTypes(types)
				.setQuery(eSQCondition.getQueryBuilder())// the search
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
	 * @return BulkResponse
	 */
	public BulkResponse bulkUpdate(HashMap<String, Double> map, String index, String type) {
		BulkRequestBuilder bulkRequest  = client.prepareBulk();
 		
		for (String infoid : map.keySet()) {
			Map<String,Object> json = new HashMap<String,Object>();
			json.put("ctr", map.get(infoid));
  			bulkRequest.add(client.prepareIndex(index, type, infoid).setSource(json)  );
 		}
		
		BulkResponse bulkResponse = bulkRequest.get();
		
		for (BulkItemResponse bulkItemResponse : bulkResponse) {
			System.out.println(bulkItemResponse.isFailed());
			
		}
		
		return bulkResponse;
		
	}

	public static BulkResponse bulkUpdate(Map<String, Map<String, Object>> mUpdateDoc, String index, String type) {
		BulkRequestBuilder bulkRequest  = client.prepareBulk();

		for (Map.Entry<String, Map<String, Object>> entry : mUpdateDoc.entrySet()) {
			String contentId = entry.getKey();
			Map<String, Object> mData = (Map<String, Object>)entry.getValue();
			UpdateRequest request = new UpdateRequest(index, type, contentId).doc(mData);
			bulkRequest.add(request);
		}
		
		BulkResponse bulkResponse = bulkRequest.get();
		
		for (BulkItemResponse itemResponse : bulkResponse) {
			String contentId = itemResponse.getId();
			if (itemResponse.isFailed()) {
				logger.debug("contentId = " + itemResponse.getId() + " update ES failed.");
			} else {
				String fieldCTR = "-1_" + SireConstant.ES_CTR_NAME;
				double valueCTR = NumberUtils.toDouble(String.valueOf(mUpdateDoc.get(contentId).get(fieldCTR)));
				logger.debug("contentId = " + itemResponse.getId() + " update ES successfully " + fieldCTR + "=" + valueCTR);
			}
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
