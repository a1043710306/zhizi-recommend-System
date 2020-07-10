package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.dubbo.common.json.ParseException;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Strategy;
import com.inveno.core.bean.Category;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

@Component("multiStrategyMixControl")
public class MultiStrategyMixControl implements IPostPolicy<List<ResponParam>>{
	 
	private Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private MonitorLog monitorLog;

//	@Override
//	public List<ResponParam> process(Context context) {
//		String strRequestInfo = ContextUtils.toRequestInfoString(context);
//		long cur = System.currentTimeMillis();
//		String uid = context.getUid();
//		String app = context.getApp();
//		String abtest = context.getAbtestVersion();
//		
//		
//		if (!ContextUtils.isForYouChannel(context)) {
//			return null;
//		}
//
//	   logger.info("context.getAvailableContentType=="+context.getAvailableContentType());
//		
//		List<ResponParam> responseList = context.getResponseParamList();
//		if (CollectionUtils.isEmpty(responseList) ){
//			return null;
//		}
//			
//		try {
//			responseList = reRank(context, responseList);
//			long end  = System.currentTimeMillis();
//			monitorLog.addResTimeLogByProduct(context, MonitorType.MULTIMIX_RESPONSE_TIME, (end - cur));
//			logger.debug(strRequestInfo + " end MultiStrategyMixControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
//			context.setResponseParamList(responseList);
//		} catch (Exception e) {
//			logger.error(" uid :" + uid + " ,app " + app + "  multiStrategyMixControl Exception,time is,and cur = "	+ System.currentTimeMillis() + e.getCause(), e);
//		}
//
////		//remove--bug tracking yezi 2018年3月30日18:15:26
////			logger.debug(" uid: " + uid + " ,app " + app + " end multiStrategyMixControl , abtestVersion is "	+ abtest + "  , time is " + (System.currentTimeMillis())
////					+ ",size is " + context.getResponseParamList().size()	+ " ,and cur = " + System.currentTimeMillis() + " , after list " + responseList);
//			
//		
//		return null;
//	}
	

	
	
	@Override
	public List<ResponParam> process(Context context) {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();
		
		
		if (!ContextUtils.isForYouChannel(context)) {
			return null;
		}

	   logger.info("context.getAvailableContentType=="+context.getAvailableContentType());
		Map<Integer, List<String>> mMixedInsertPoolMapping = new HashMap<Integer, List<String>>();
		List<String> multiContentTypeStrategyWithRatioList = new ArrayList<String>();
		Map<Integer,Integer>  multiContentTypeStrategyWithRatioMap = new HashMap<Integer,Integer>();
		
		Map<String, List<ResponParam>> contenttypeStrategyDocumentList = new HashMap<String, List<ResponParam>>();
		List<ResponParam> responseList = context.getResponseParamList();
		if (CollectionUtils.isEmpty(responseList) ){
			return null;
		}
		if (null != responseList) {
			//multiStrategyWithRatioMap
			String multiContentTypeStrategyWithRatioStr   = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "multiStrategyWithRatioMap");
//		   if(logger.isDebugEnabled()){	//StringUtils.isEmpty(multiContentTypeStrategyWithRatioStr)
////			    multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#1:1\",\"1#1:2\", \"32#1:1\"]";//multiContentTypeStrategyWithRatioStr = "{1:2, 2:1, 1:2, 128:1,1:2, 32:1}";//2*news、1*video、2*news、1*memes、2*news、1*gif	
//		   	 	multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#220:1\",\"1#1:2\", \"32#221:1\"]";
//		   }	
		   
		   if(StringUtils.isNotEmpty(multiContentTypeStrategyWithRatioStr)){
			   try {
			        multiContentTypeStrategyWithRatioList =  JSON.parseArray(multiContentTypeStrategyWithRatioStr, String.class);
			   } catch (Exception e) {
					logger.error(context.toString() + " multiStrategyWithRatioMap parsing error : ", e);
					
				}
		   }else{
			   logger.info( " multiStrategyWithRatioMap null ");
				return null;
			}
			//mixedInsertStrategiesStr
			String mixedInsertStrategiesStr   = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "MixedInsertStrategies");
//			if(logger.isDebugEnabled()){//StringUtils.isEmpty(mixedInsertStrategiesStr)
//				mixedInsertStrategiesStr = "{2:[\"2\"],128:[\"220\"],32:[\"221\"]}";
////				 mixedInsertStrategiesStr = "{2:[\"2\"],128:[\"1\"],32:[\"1\"]}";//contentType:strategyList				"{2:[\"2\"],128:[\"1\",\"101\"],32:[\"1\"]}";
//			}
		
			if(StringUtils.isNotEmpty(mixedInsertStrategiesStr)){
				try {
					 mMixedInsertPoolMapping = JSON.parseObject(mixedInsertStrategiesStr, HashMap.class);
				} catch (Exception e) {
					logger.error(context.toString() + " MixedInsertStrategies parsing error : ", e);
					
				}		
			}else{
				 logger.info( " mixedInsertStrategiesStr null ");
				return null;
			}
				
				
			
			//代码中的strategy 与下发的strategy 的映射关系Map
			HashMap<String,String> mixedContentTypeStrategyWithResponStrategyMaping = new HashMap<String,String>();//HashMap<contentType:strategy/version ,responseStrage>  <gif:1,mixed_insert_gifboost>
			String mixedContentTypeStrategyWithResponStrategyStr = context.getComponentConfiguration("mixedStrategyToResponStrategyMapping");
			if(StringUtils.isNotEmpty(mixedContentTypeStrategyWithResponStrategyStr)){
				try {
				mixedContentTypeStrategyWithResponStrategyMaping = JSON.parseObject(mixedContentTypeStrategyWithResponStrategyStr, HashMap.class);
				} catch (Exception e) {
					logger.error(context.toString() + " mixedContentTypeStrategyWithResponStrategyMaping parsing error : ", e);
					
				}
//				if(logger.isDebugEnabled()){
//					logger.debug("mixedContentTypeStrategyWithResponStrategyMaping=="+mixedContentTypeStrategyWithResponStrategyMaping);
//				}
			}
			if(mixedContentTypeStrategyWithResponStrategyMaping==null || mixedContentTypeStrategyWithResponStrategyMaping.size()<0){
				mixedContentTypeStrategyWithResponStrategyMaping.put("2#2", Strategy.MIXED_INSERT_VIDEOBOOST.getCode());//video推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("32#1",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("128#1",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("128#220",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("32#221",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
//				mixedContentTypeStrategyWithResponStrategyMaping.put("128#101", "103");//beauty 推荐---test
			}
			
			
			 
			
//			
			
//			//指定contentType与下发的strategy 的映射关系Map
//			HashMap<String,String> mixedContentTypeWithResponStrategyMaping = new HashMap<String,String>();
//			mixedContentTypeWithResponStrategyMaping.put("2", Strategy.MIXED_INSERT_VIDEOBOOST.getCode());//video推荐
//			mixedContentTypeWithResponStrategyMaping.put("32",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
//			mixedContentTypeWithResponStrategyMaping.put("128",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
			
		
			// 根据需要multiContentTypeStrategyWithRatioList，mixedInsertStrategiesStr 获取要下发的mix数据
			HashSet<Integer> multiContentType = new HashSet<Integer>();//有哪些contentType 要下发
			for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {
				if (StringUtils.isNotEmpty(contentTypeWithRatioItem)) {
					String  contentTypeWithStrategy  = String.valueOf(contentTypeWithRatioItem.split(":")[0]);
					int contentType = Integer.valueOf(contentTypeWithStrategy.split("#")[0]);

					if (!multiContentType.contains(contentType) && context.getAvailableContentType().contains(contentType)) {//防止多个contentType重复下发,并且有在能力集合中
						List<String> mixedInsertStrategiesList = mMixedInsertPoolMapping.get(contentType);//根据contentType得到strategyList		
					
//						List<ResponParam> contentTypeDocList = new ArrayList<ResponParam>();
						if (mixedInsertStrategiesList != null) {
							
							Map<String,List<ResponParam>> strategyDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType);
							if(strategyDocumentList != null){
								for (String strategyCode : mixedInsertStrategiesList) {
	//								logger.debug("contentType="+contentType+",class=multiStrategyMixControl,strategyCode="+strategyCode);
								
										List<ResponParam> documentList = strategyDocumentList.get(strategyCode);// context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);
	//									logger.debug(context.toString() + ",class=multiStrategyMixControl, contentType:"+contentType+",strategyCode="+strategyCode+",documentList="+documentList.size());
										//线上不要增加这样的日志 
//										 if(logger.isDebugEnabled()){
//										 	logger.debug(context.toString() + ",class=multiStrategyMixControl, contentType:"+contentType+",strategyCode="+strategyCode+",documentList="+documentList);
//										 }
										if (documentList != null && documentList.size() > 0) {
		//									contentTypeDocList.addAll(mapStrategyToDocumentList);
										
											contenttypeStrategyDocumentList.put(contentType+"#"+strategyCode, documentList);//"128:1",List<responseList>
											
											
										} else{
											//暂时定的监控：一个contentType 对应一个strategy的监控处理
											if(contentType ==  ContentType.MEME.getValue()){
												 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_MEMES_FAIL_COUNT);//请求失败次数
											}
											if(contentType ==  ContentType.GIF.getValue()){
												 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_GIF_FAIL_COUNT);//请求失败次数
											}
											
//												logger.debug("strRequestInfo="+strRequestInfo+",contentType="+contentType+",class=multiStrategyMixControl,strategyCode="+strategyCode+",documentList is null");
											
										}
								}
							}else{
									//暂时定的监控：一个contentType 对应一个strategy的监控处理
										if(contentType ==  ContentType.MEME.getValue()){
											 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_MEMES_FAIL_COUNT);//请求失败次数
										}
										if(contentType ==  ContentType.GIF.getValue()){
											 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_GIF_FAIL_COUNT);//请求失败次数
										} 
										
//									    logger.debug("strRequestInfo="+strRequestInfo+",contentType="+contentType+",class=multiStrategyMixControl,strategyDocumentList is null");
										
								  }
							

						}else{
							//暂时定的监控：一个contentType 对应一个strategy的监控处理
							if(contentType ==  ContentType.MEME.getValue()){
								 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_MEMES_FAIL_COUNT);//请求失败次数
							}
							if(contentType ==  ContentType.GIF.getValue()){
								 monitorLog.addCntLogByProduct(context,MonitorType.MULTIMIX_GIF_FAIL_COUNT);//请求失败次数
							} 
							
//							logger.debug("strRequestInfo="+strRequestInfo+",contentType="+contentType+",class=multiStrategyMixControl,mixedInsertStrategiesContentTypeList is null");
							
						}
						multiContentType.add(contentType);
					}

				}

			}

			int pos = 0;
			while (contenttypeStrategyDocumentList.size() > 0 && responseList.size() > pos) {
				
				for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {
					
					if (StringUtils.isNotEmpty(contentTypeWithRatioItem)&& responseList.size() > pos) {
						String  contentTypeWithStrategy  = contentTypeWithRatioItem.split(":")[0];
						String  contentType  = contentTypeWithStrategy.split("#")[0];
						int  count  = Integer.valueOf(String.valueOf(contentTypeWithRatioItem.split(":")[1]));//下发条数
						List<ResponParam> docList = contenttypeStrategyDocumentList.get(contentTypeWithStrategy);
						
						while(count > 0){
							
							 if(!ContentType.NEWS.equals(contentType)&& docList != null && docList.size()>0){//如果是news的话,context.getResponseParamList();本身准备了1000篇，如果单独处理，会把之前的pipleline做的策略全部打乱
									
								 ResponParam param = docList.get(0);
								if(param == null){
									logger.fatal(strRequestInfo + ", contentType=" +contentType + " has null entry in response.");
								}
								 String strategyCode = mixedContentTypeStrategyWithResponStrategyMaping.get(contentTypeWithStrategy);
								 if(StringUtils.isEmpty(strategyCode)){
//									 System.out.println(" mixedContentTypeStrategyWithResponStrategyMaping less contentTypeWithStrategy : "+contentTypeWithStrategy);
									 logger.error(context.toString() + " ,mixedContentTypeStrategyWithResponStrategyMaping less contentTypeWithStrategy : "+contentTypeWithStrategy);
								 }
								 param.setStrategy(strategyCode);//需要修改 具体的信息
								 responseList.add(pos, param);
								 docList.remove(0);
							 }
							 pos++;
							 count--;
						}
						
					}
					
			 }
				
				
		 }
		   
		}
		
		long end  = System.currentTimeMillis();
		monitorLog.addResTimeLogByProduct(context, MonitorType.MULTIMIX_RESPONSE_TIME, (end - cur));//需要修改 具体的信息！！！！2018年2月24日17:34:57
		
		logger.debug(strRequestInfo + " end MultiStrategyMixControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(responseList);
		return null;
	}
	
	
	
	public static List<ResponParam> reRank(Context context,List<ResponParam> inlist) {
		
		
	    ArrayList<ResponParam> list = (ArrayList<ResponParam>)inlist;
	    @SuppressWarnings("unchecked")
		ArrayList<ResponParam> result = (ArrayList<ResponParam>) list.clone();
		if (CollectionUtils.isEmpty(result) ){
			return result;
		}
		
	    Map<Integer, List<String>> mMixedInsertPoolMapping = new HashMap<Integer, List<String>>();
	    List<String> multiContentTypeStrategyWithRatioList = new ArrayList<String>();
		Map<Integer,Integer>  multiContentTypeStrategyWithRatioMap = new HashMap<Integer,Integer>();
		Map<String, List<ResponParam>> contenttypeStrategyDocumentList = new HashMap<String, List<ResponParam>>();
		
	
			//multiStrategyWithRatioMap
			String multiContentTypeStrategyWithRatioStr   = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "multiStrategyWithRatioMap");
		   if(StringUtils.isEmpty(multiContentTypeStrategyWithRatioStr)){	
//			    multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#1:1\",\"1#1:2\", \"32#1:1\"]";//multiContentTypeStrategyWithRatioStr = "{1:2, 2:1, 1:2, 128:1,1:2, 32:1}";//2*news、1*video、2*news、1*memes、2*news、1*gif	
		   	 multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#220:1\",\"1#1:2\", \"32#221:1\"]";
		   }
		   if(StringUtils.isNotEmpty(multiContentTypeStrategyWithRatioStr)){	
			   try {
			        multiContentTypeStrategyWithRatioList =  JSON.parseArray(multiContentTypeStrategyWithRatioStr, String.class);
			   } catch (Exception e) {
				   return result;	
				}
		   }
			//mixedInsertStrategiesStr
			String mixedInsertStrategiesStr   = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "MixedInsertStrategies");
			if(StringUtils.isEmpty(mixedInsertStrategiesStr)){
//				 mixedInsertStrategiesStr = "{2:[\"2\"],128:[\"1\"],32:[\"1\"]}";//contentType:strategyList				"{2:[\"2\"],128:[\"1\",\"101\"],32:[\"1\"]}";
				mixedInsertStrategiesStr = "{2:[\"2\"],128:[\"220\"],32:[\"221\"]}";
			}
			if(StringUtils.isNotEmpty(mixedInsertStrategiesStr)){	
				try {
					 mMixedInsertPoolMapping = JSON.parseObject(mixedInsertStrategiesStr, HashMap.class);
				} catch (Exception e) {
					return result;
				}				
				
			}
				
			
			//代码中的strategy 与下发的strategy 的映射关系Map
			HashMap<String,String> mixedContentTypeStrategyWithResponStrategyMaping = new HashMap<String,String>();//HashMap<contentType:strategy/version ,responseStrage>  <gif:1,mixed_insert_gifboost>
			String mixedContentTypeStrategyWithResponStrategyStr = context.getComponentConfiguration("mixedStrategyToResponStrategyMapping");
			if(StringUtils.isNotEmpty(mixedContentTypeStrategyWithResponStrategyStr)){
				try {
				mixedContentTypeStrategyWithResponStrategyMaping = JSON.parseObject(mixedContentTypeStrategyWithResponStrategyStr, HashMap.class);
				} catch (Exception e) {
					return result;
					
				}
			}
			if(mixedContentTypeStrategyWithResponStrategyMaping==null || mixedContentTypeStrategyWithResponStrategyMaping.size()<0){
				mixedContentTypeStrategyWithResponStrategyMaping.put("2#2", Strategy.MIXED_INSERT_VIDEOBOOST.getCode());//video推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("32#1",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("128#1",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("128#220",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
				mixedContentTypeStrategyWithResponStrategyMaping.put("32#221",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
//				mixedContentTypeStrategyWithResponStrategyMaping.put("128#101", "103");//beauty 推荐---test
			}
			
//			//指定contentType与下发的strategy 的映射关系Map
//			HashMap<String,String> mixedContentTypeWithResponStrategyMaping = new HashMap<String,String>();
//			mixedContentTypeWithResponStrategyMaping.put("2", Strategy.MIXED_INSERT_VIDEOBOOST.getCode());//video推荐
//			mixedContentTypeWithResponStrategyMaping.put("32",Strategy.MIXED_INSERT_GIFBOOST.getCode());//gif 推荐
//			mixedContentTypeWithResponStrategyMaping.put("128",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
		
			// 根据需要multiContentTypeStrategyWithRatioList，mixedInsertStrategiesStr 获取要下发的mix数据
			HashSet<Integer> multiContentType = new HashSet<Integer>();//有哪些contentType 要下发
			for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {
				if (StringUtils.isNotEmpty(contentTypeWithRatioItem)) {
					String  contentTypeWithStrategy  = String.valueOf(contentTypeWithRatioItem.split(":")[0]);
					int contentType = Integer.valueOf(contentTypeWithStrategy.split("#")[0]);

					if (!multiContentType.contains(contentType) && context.getAvailableContentType().contains(contentType)) {//防止多个contentType重复下发,并且有在能力集合中
						List<String> mixedInsertStrategiesList = mMixedInsertPoolMapping.get(contentType);//根据contentType得到strategyList		
					
//						List<ResponParam> contentTypeDocList = new ArrayList<ResponParam>();
						if (mixedInsertStrategiesList != null) {
							
							Map<String,List<ResponParam>> strategyDocumentList = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType);
//							System.out.println("contentType:"+contentType+",strategyCode="+strategyCode+",mapStrategyToDocumentList="+strategyDocumentList);
							if(strategyDocumentList != null){
								for (String strategyCode : mixedInsertStrategiesList) {
										List<ResponParam> documentList = strategyDocumentList.get(strategyCode);// context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);
	//									System.out.println(context.toString() + ", contentType:"+contentType+",strategyCode="+strategyCode+",mapStrategyToDocumentList="+documentList);
										if (documentList != null && documentList.size() > 0) {
		//									contentTypeDocList.addAll(mapStrategyToDocumentList);
										
											contenttypeStrategyDocumentList.put(contentType+"#"+strategyCode, documentList);//"128:1",List<responseList>
											
										} 
							  }
						  }

						}
						multiContentType.add(contentType);
					}

				}

			}

			int pos = 0;
			while (contenttypeStrategyDocumentList.size() > 0 && result.size() > pos) {
				
				for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {
					
					if (StringUtils.isNotEmpty(contentTypeWithRatioItem)&& result.size() > pos) {
						String  contentTypeWithStrategy  = contentTypeWithRatioItem.split(":")[0];
						String  contentType  = contentTypeWithStrategy.split("#")[0];
						int  count  = Integer.valueOf(String.valueOf(contentTypeWithRatioItem.split(":")[1]));//下发条数
						List<ResponParam> docList = contenttypeStrategyDocumentList.get(contentTypeWithStrategy);
						
						while(count > 0){
							
							 if(!ContentType.NEWS.equals(contentType)&& docList != null && docList.size()>0){//如果是news的话,context.getResponseParamList();本身准备了1000篇，如果单独处理，会把之前的pipleline做的策略全部打乱
									
								 ResponParam param = docList.get(0);
								 String strategyCode = mixedContentTypeStrategyWithResponStrategyMaping.get(contentTypeWithStrategy);
								 if(StringUtils.isEmpty(strategyCode)){
//									 System.out.println(" mixedContentTypeStrategyWithResponStrategyMaping less contentTypeWithStrategy : "+contentTypeWithStrategy);
								 }
//								 String strategyCode = mixedContentTypeStrategyWithResponStrategyMaping.get(contentType);//针对以后的混插策略可能是多个组合 这样的话有坑随时要改动上面的mapping关系，也就是改动Map的组合信息--经常的改动代码
								 param.setStrategy(strategyCode);//需要修改 具体的信息
								 result.add(pos, param);
								 docList.remove(0);
							 }
							 pos++;
							 count--;
						}
						
					}
					
			 }
				
				
		 }
		   
		
		
		//context.setResponseParamList(result);
		
		return result;
	}
	
	
	//Test
	public static void main(String[] args) {	
		
		
		

		///----
		HashMap<String,String> mixedContentTypeStrategyWithResponStrategyMaping22 = new HashMap<String,String>();
		
		mixedContentTypeStrategyWithResponStrategyMaping22 = JSON.parseObject("{'2#2':'14','128#1':'34','32#1':'42','128#220':'34','32#221':'42'}", HashMap.class);//{'2#2':14,'128#1':34,'32#1':42,'128#220':34,'32#221':42}
		
		System.out.println(mixedContentTypeStrategyWithResponStrategyMaping22);
		/////demo data ----------
//		context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);// ready data
		
		List<ResponParam> demoResponParamList  = new ArrayList<ResponParam>();

		int i = 0;
		while(i<100){
			ResponParam newsResponParam = new ResponParam();
			
			newsResponParam.setContentType(1);
			int groupId = 1000000+i;
			newsResponParam.setGroupId(String.valueOf(groupId+"_news"));
			i++;
			demoResponParamList.add(newsResponParam);
		}
		
		
		Map<Integer,HashMap<String, List<ResponParam> >> contenttypeStrategyDocumentListDemo = new HashMap<Integer,HashMap<String, List<ResponParam>>> ();
		HashMap<String, List<ResponParam>> newsMap =  new HashMap<String, List<ResponParam>> ();
		newsMap.put("1",demoResponParamList);
		contenttypeStrategyDocumentListDemo.put(1, newsMap);
		
		//video
		List<ResponParam> demoResponParamList1  = new ArrayList<ResponParam>();
		 i = 0;
		while(i<10){
			ResponParam videoResponParam = new ResponParam();
			
			videoResponParam.setContentType(2);
			int groupId = 1000000+i;
			videoResponParam.setGroupId(String.valueOf(groupId+"_video"));
			i++;
			demoResponParamList1.add(videoResponParam);
		}
		
		HashMap<String, List<ResponParam>> videoMap =  new HashMap<String, List<ResponParam>> ();
		videoMap.put("2",demoResponParamList1);
		contenttypeStrategyDocumentListDemo.put(2, videoMap);
		
		
		
		//memes
		List<ResponParam> demoResponParamList3  = new ArrayList<ResponParam>();
		 i = 0;
		while(i<10){
			ResponParam memesResponParam = new ResponParam();
			
			memesResponParam.setContentType(128);
			int groupId = 1033330000+i;
			memesResponParam.setGroupId(String.valueOf(groupId+"_memes"));
			i++;
			demoResponParamList3.add(memesResponParam);
		}
		HashMap<String, List<ResponParam>> memesMap =  new HashMap<String, List<ResponParam>> ();
		memesMap.put("1",demoResponParamList3);
		contenttypeStrategyDocumentListDemo.put(128, memesMap);
		
		
		//beauty
		List<ResponParam> demoBeautyResponParamList  = new ArrayList<ResponParam>();
		 i = 0;
		while(i<10){
			ResponParam beautyResponParam = new ResponParam();
			
			beautyResponParam.setContentType(128);
			int groupId = 100000+i;
			beautyResponParam.setGroupId(String.valueOf(groupId+"_beauty"));
			i++;
			demoBeautyResponParamList.add(beautyResponParam);
		}
//		HashMap<String, List<ResponParam>> beautyMap =  new HashMap<String, List<ResponParam>> ();
		memesMap.put("101",demoBeautyResponParamList);
		contenttypeStrategyDocumentListDemo.put(128, memesMap);
		
		
		//gif
		List<ResponParam> demoResponParamList4  = new ArrayList<ResponParam>();
	
		 i = 0;
		while(i<10){
			ResponParam gifResponParam = new ResponParam();
			
			gifResponParam.setContentType(32);
			int groupId = 10000+i;
			gifResponParam.setGroupId(String.valueOf(groupId+"_gif"));
			i++;
			demoResponParamList4.add(gifResponParam);
		}
		
		HashMap<String, List<ResponParam>> gifMap =  new HashMap<String, List<ResponParam>> ();
		gifMap.put("1",demoResponParamList4);
		contenttypeStrategyDocumentListDemo.put(32, gifMap);
		
		
		////demo listResponse
		List<ResponParam> demoList  = new ArrayList<ResponParam>();
		 i = 0;
		while(i<30){
			ResponParam tt = new ResponParam();
			
			tt.setContentType(1);
			int groupId = 1000+i;
			tt.setStrategy("8");
			tt.setGroupId(String.valueOf(groupId)+"_response");
			i++;
			demoList.add(tt);
		}
		
		/////demo data ----------上面是准备各个contentType+strategy 的数据，以及ListResponseInfo
		
		Map<String, List<ResponParam>> contenttypeStrategyDocumentList = new HashMap<String, List<ResponParam>>();//
		Map<Integer, List<String>> mMixedInsertPoolMapping = new HashMap<Integer, List<String>>();
//		Map<Integer,Integer>  multiContentTypeStrategyWithRatioMap = new HashMap<Integer,Integer>();
		boolean existNullStrategyDocumentList = false;
		
//		multiContentTypeStrategyWithRatioStr = "[1:2, 2:1, 1:2, 128:1,1:2, 32:1]";

//		System.out.println(multiContentTypeWithRatioList);
//		 multiContentTypeStrategyWithRatioMap = JSON.parseObject(multiContentTypeStrategyWithRatioStr,HashMap.class);
		
//		String  multiContentTypeStrategyWithRatioStr = "[\"1:2\",\"2:1\", \"1:2\", \"128:1\",\"1:2\", \"32:1\"]";//"[1:2, 2:1, 1:2, 128:1,1:2, 32:1}";	
//		String  multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#1:1\",\"1#1:2\", \"32#1:1\"]";//"[1:2, 2:1, 1:2, 128:1,1:2, 32:1}";	
		String  multiContentTypeStrategyWithRatioStr = "[\"1#1:2\",\"2#2:1\", \"1#1:2\", \"128#1:1\",\"1#1:2\", \"32#1:1\"\"128#101:1\",]";
		List<String> multiContentTypeStrategyWithRatioList =  JSON.parseArray(multiContentTypeStrategyWithRatioStr, String.class);
		String strMixedInsertContentTypeStrategies = "{2:[\"2\"],128:[\"1\",\"101\"],32:[\"1\"]}";//context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "multiStrategyWithRatioMap")
//		"{2:[\"2\"],128:[\"1\",\"101\"],32:[\"1\"]}";
		
		HashMap<String,String> mixedContentTypeStrategyWithResponStrategyMaping = new HashMap<String,String>();//HashMap<contentType:strategy/version ,responseStrage>  <gif:1,mixed_insert_gifboost>
		mixedContentTypeStrategyWithResponStrategyMaping.put("2#2", Strategy.MIXED_INSERT_VIDEOBOOST.getCode());//video推荐
		mixedContentTypeStrategyWithResponStrategyMaping.put("32#1", "42");//gif 推荐
		mixedContentTypeStrategyWithResponStrategyMaping.put("128#1",Strategy.MIXED_INSERT_MEMESBOOST.getCode());//memes 推荐
		mixedContentTypeStrategyWithResponStrategyMaping.put("128#101", "103");//beauty 推荐---test
		//mixedInsertStrategies
		
		
		if (StringUtils.isNotEmpty(strMixedInsertContentTypeStrategies)) {
		
			try {
				 mMixedInsertPoolMapping = JSON.parseObject(strMixedInsertContentTypeStrategies, HashMap.class);
			} catch (Exception e) {
//				logger.error(context.toString() + " determineStrategyParameter parsing error : ", e);
				
			}
			
		}
		System.out.println(multiContentTypeStrategyWithRatioList);
		
//		System.out.println(contenttypeStrategyDocumentListDemo.get(128).get("1"));// context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);
		
		// 根据需要contentType 获取数据
		HashSet<Integer> multiContentType = new HashSet<Integer>();
		for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {
			if (StringUtils.isNotEmpty(contentTypeWithRatioItem)) {
				String  contentTypeWithStrategy  = String.valueOf(contentTypeWithRatioItem.split(":")[0]);
				int contentType = Integer.valueOf(contentTypeWithStrategy.split("#")[0]);
//				System.out.println("need contentType:"+contentType);
//				System.out.println("need contenttypeStrategyDocumentListDemo:"+contenttypeStrategyDocumentListDemo);
				if (!multiContentType.contains(contentType)) {
					List<String> mixedInsertStrategiesList = mMixedInsertPoolMapping.get(contentType);
				
//					List<ResponParam> contentTypeDocList = new ArrayList<ResponParam>();
					if (mixedInsertStrategiesList != null) {
						
						for (String strategyCode : mixedInsertStrategiesList) {
							
//							List<ResponParam> mapStrategyToDocumentList = contenttypeStrategyDocumentListDemo.get(contentType).get(strategyCode);// context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);
							Map<String,List<ResponParam>> strategyDocumentList =   contenttypeStrategyDocumentListDemo.get(contentType);
							System.out.println("contentType:"+contentType+",strategyCode="+strategyCode+",strategyDocumentList="+strategyDocumentList);
							if(strategyDocumentList != null){
								List<ResponParam> documentList = strategyDocumentList.get(strategyCode);// context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(contentType).get(strategy);
								if (documentList != null && documentList.size() > 0) {
//									contentTypeDocList.addAll(mapStrategyToDocumentList);
								
									contenttypeStrategyDocumentList.put(contentType+"#"+strategyCode, documentList);//"128:1",List<responseList>
									
								} else {
									existNullStrategyDocumentList = true;
								}
							  }
						}

					}
					multiContentType.add(contentType);
				}

			}

		}
//		System.out.println("multiContentTypeWithStrategy:"+multiContentType);
		
		//获取最终结果集合
//		for (String contentTypeWithRatioItem : multiContentTypeWithRatioList) {
//			if (StringUtils.isNotEmpty(contentTypeWithRatioItem)) {
//				int contentType = Integer.valueOf(contentTypeWithRatioItem.split(":")[0]);
//				
//			}
//		}
//		
		List<ResponParam> result = demoList;//NEED CHANGE
		int pos = 0;
		while (contenttypeStrategyDocumentList.size() > 0 && result.size() > pos) {
			
			for (String contentTypeWithRatioItem : multiContentTypeStrategyWithRatioList) {//2018-3-14 19:01:48 ---这里
				
				if (StringUtils.isNotEmpty(contentTypeWithRatioItem)&& result.size() > pos) {
					String  contentTypeWithStrategy  = contentTypeWithRatioItem.split(":")[0];
					String  contentType  = contentTypeWithStrategy.split("#")[0];
					int  count  = Integer.valueOf(String.valueOf(contentTypeWithRatioItem.split(":")[1]));//下发条数
					List<ResponParam> contentTypeDocList = contenttypeStrategyDocumentList.get(contentTypeWithStrategy);
					
					while(count > 0){
						
						 if(!ContentType.NEWS.equals(contentType)&& contentTypeDocList != null && contentTypeDocList.size()>0){//如果是news的话,context.getResponseParamList();本身准备了1000篇，如果单独处理，会把之前的pipleline做的策略全部打乱
								
							 ResponParam param = contentTypeDocList.get(0);
							
							 String strategyCode = mixedContentTypeStrategyWithResponStrategyMaping.get(contentTypeWithStrategy);
							 if(StringUtils.isEmpty(strategyCode)){
								 System.out.println(" mixedContentTypeStrategyWithResponStrategyMaping less contentTypeWithStrategy : "+contentTypeWithStrategy);
//								 logger.error(context.toString() + " mixedContentTypeStrategyWithResponStrategyMaping less contentTypeWithStrategy : ",contentTypeWithStrategy);
							 }
							 param.setStrategy(strategyCode);//需要修改 具体的信息
							 result.add(pos, param);
							 contentTypeDocList.remove(0);
						 }
						 pos++;
						 count--;
					}
					
				}
				
		 }
			
			
	 }
		
		System.out.println("结果信息：");
		int n =0;
		for (ResponParam param : result) {
			
			if(n%2 == 0){
				System.out.println();
			}
			System.out.print(param);
			n++;
		}
		
}

	
	
	
}
