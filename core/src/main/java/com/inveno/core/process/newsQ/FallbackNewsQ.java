package com.inveno.core.process.newsQ;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;
import com.inveno.zhiziArticleFilter.ArticleFilterProperty;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.IArticleFilter;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component("fallbackNewsQ")
public class FallbackNewsQ {
	public static final String SUFFIX_IMPRESSION_ACS_KEY = "impression";
	public static Log fallbackLogger = LogFactory.getLog("fallback");

	@Autowired
	private ReadedInfoHandler readFilterInfo;

	@Autowired
	private JedisCluster jedisClusterFallBack;

	public List<ResponParam> process(Context context) throws TException {
		String strRequestInfo = ContextUtils.toRequestInfoString(context);

		String uid = context.getUid();
		List<String> nofallbackSenarioArrList = new ArrayList<String>();
		String nofallbackSenarioArr = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "nofallbackSenarioArr");
		if (StringUtils.isNotEmpty(nofallbackSenarioArr)) {
			nofallbackSenarioArrList = Arrays.asList(nofallbackSenarioArr.split(";"));
		}

		fallbackLogger.debug(strRequestInfo + " | nofallbackSenarioArrList :" + nofallbackSenarioArrList);

		if (CollectionUtils.isNotEmpty(nofallbackSenarioArrList)
				&& nofallbackSenarioArrList.contains(String.valueOf(context.getScenario()))) {
			return Collections.emptyList();
		}

		String app = context.getApp();

		String abtest = context.getAbtestVersion();
		String language = context.getLanguage();
		if (language == null || language.isEmpty()) {
			language = Constants.ZH_CN;
		}

		IArticleFilter proc = null;
		String className = "com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl";
		try {
			if (Class.forName(className).newInstance() instanceof IArticleFilter) {
				proc = (IArticleFilter) Class.forName(className).newInstance();
			}
		} catch (Exception e1) {
			fallbackLogger.error("Try to get " + className + " FAIL!", e1);
		}
		if (proc != null) {

			ArticleFilterProperty articleFilterProp = proc.getQueryProperty();
			articleFilterProp.setZhizi_request(context.getZhiziListReq());
			proc.setQueryProperty(articleFilterProp);

			if (context.getZhiziListReq() != null) {
				if (fallbackLogger.isDebugEnabled()) {
					fallbackLogger.debug(strRequestInfo + " | FallbackNewsQ using IArticleFilter, ZhiziListReq: "
							+ articleFilterProp.getZhizi_request().getContent_type() + " "
							+ articleFilterProp.getZhizi_request().getLink_type() + " "
							+ articleFilterProp.getZhizi_request().getDisplay());
				}
			} else {
				fallbackLogger.debug(strRequestInfo + " | FallbackNewsQ using IArticleFilter");
			}
		}

		int newsQTimes = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "newsQTimes"), 2);
		int newsinfoCount = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "newsInfoCount"), 300);
		
		//2018年11月17日19:51:18 yezi 基于fallback逻辑 按照request下发消重
		boolean isEnableImpressionACSForScenario = false;
		boolean isEnableImpressionACSForLockscreen = false;
		String strEnableImpressionACSForLockscreen  = context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_PRIMARY_SELECTION, "isEnableImpressionACSForLockscreen");
		if(StringUtils.isNotEmpty(strEnableImpressionACSForLockscreen)){
			isEnableImpressionACSForLockscreen = Boolean.parseBoolean(strEnableImpressionACSForLockscreen);
		}
		boolean bLockscreen = ContextUtils.isLockscreen(context.getScenario());
		boolean fEnableImpressionACS = (isEnableImpressionACSForScenario || (bLockscreen && isEnableImpressionACSForLockscreen));
		boolean bACSWithScenario = false;
		if (bLockscreen)
		{
			bACSWithScenario = true;
		}
		
		
		List<String> listKeys = new ArrayList<String>();
		List<Object> listCategories = new ArrayList<Object>();
		listCategories.addAll(context.getCategoryids());
		if (listCategories.isEmpty()) {
			listCategories.add("all");
		}

		String strSpecificAppFallbackKey = "";
		boolean ifVideoChannel = false;
		if (context.getContentType() == 2) {
			ifVideoChannel = true;
		}
		Iterator<Object> iterCategories = listCategories.iterator();
		while (iterCategories.hasNext()) {
			Object cat_id = iterCategories.next();

			if (app.equals("debug")) {
				strSpecificAppFallbackKey = "fallback_debug_" + language + "_" + cat_id;
			} else {
				strSpecificAppFallbackKey = "fallback_" + app + "_" + language + "_" + cat_id;
			}

			if (ifVideoChannel) {
				strSpecificAppFallbackKey += "_2";
			}

			listKeys.add(strSpecificAppFallbackKey);
		}

		long begin = System.currentTimeMillis();

		if (fallbackLogger.isDebugEnabled()) {
			fallbackLogger.debug(strRequestInfo + " | begin query FallbackNewsQ-" + listKeys);
		}
		

		int nCategoryInfoCount = newsinfoCount / listKeys.size();

		int times = 1;
		Map<String, ArrayList<String>> mapCategoryDataList = new HashMap<String, ArrayList<String>>();
		List<String> candidateList = new ArrayList<String>();
		List<String> resultList = new ArrayList<String>();
		Map<String, Double> mapIDScores = new HashMap<String, Double>();
		Map<String, String> mapIDCategory = new HashMap<String, String>();
		do {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
			fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			long currentTimastamp = (new Date()).getTime();
			candidateList.clear();
			
			ArticleProperty article_property = new ArticleProperty();
			for (int idx = 0; idx < listKeys.size(); idx++) {
				ArrayList<String> listIDsWithDescription = new ArrayList<String>();
				Set<Tuple> fallbackData = jedisClusterFallBack.zrangeWithScores(listKeys.get(idx), 0, times * nCategoryInfoCount - 1);
				
				int fallbackDataIndex = 0;
				
				for (Tuple t : fallbackData) {
					String key = t.getElement();
					if (StringUtils.isNotEmpty(key)) {
						double gmp = 1 - t.getScore();
						String infoId = key.split("#")[0];
						if(fallbackDataIndex == 0){//增加contentType 判断逻辑 防止 scenario forU频道下发时 category包含133 123 的 所以fallback的时候会放出，先保证下发时只能包含news
							try {
								int contentType = Integer.parseInt( key.split("#")[1]);
								if(ContextUtils.isForYouChannel(context)&&  contentType != ContentType.NEWS.getValue()){
									break;
								}
							} catch (Exception e) {
								break;//parse 异常
							}
							
						}
						fallbackDataIndex++;

						/*
						//1. score<1 : score=1-gmp
						//2. score>1 ; score=current_time-publish_time
						if (gmp < 0) {
							fallbackLogger.info("fallback ranking ignore id because no gmp, id:" + infoId + ", info:" + listKeys.get(idx)
									+ ", score:" + t.getScore());
							continue;
						} else if (gmp < NumberUtils.toDouble(context.getComponentConfiguration("fallback", "minGmpThreshold"))) {
							fallbackLogger.info("fallback ranking ignore id because gmp too small, id:" + infoId + ", info:" + listKeys.get(idx)
									+ ", minGmpThreshold:" + context.getComponentConfiguration("fallback", "minGmpThreshold") + ", gmp:"
									+ String.format("%.6f", gmp));
							continue;
						}
						*/

						listIDsWithDescription.add(key);
						mapIDScores.put(infoId, t.getScore());
						mapIDCategory.put(infoId, listKeys.get(idx));
					}
				}

				for (String strIDWithDescription : listIDsWithDescription) {
					String[] arrTmp = strIDWithDescription.split("#");
					String infoId = "";
					if (proc != null) {
						article_property.setContent_id(arrTmp[0]);
						article_property.setProduct_id(app);
						article_property.setPublish_timestamp(currentTimastamp); //ensure within 48hours..
						article_property.setAdult_score(0.0D); //ensure pass check..
						article_property.setFirm_app(null);
						if (arrTmp.length > 3) {
							article_property.setType(arrTmp[1].equals("null") ? "0" : arrTmp[1]);
							article_property.setLink_type(arrTmp[2].equals("null") ? "2" : arrTmp[2]);
							article_property.setDisplay_type(arrTmp[3].equals("null") ? "1" : arrTmp[3]);
						}

						if (proc.isValidArticle(article_property)) {
							infoId = article_property.getContent_id();
							//candidateList.add(article_property.getContent_id());
						}
					} else {
						infoId = arrTmp[0];
						//candidateList.add(arrTmp[0]);
					}
					
					if(StringUtils.isNotEmpty(infoId)){
						StringBuffer sb = new StringBuffer();
						sb.append(infoId);
						
							if(bACSWithScenario){
								sb.append("#");
								sb.append(String.valueOf(context.getScenario()));
							}
							
							if(fEnableImpressionACS){
								sb.append("#");
								sb.append(SUFFIX_IMPRESSION_ACS_KEY);
							}
							candidateList.add(sb.toString());
						
						
						
					}					
					
				}
			}

			if (candidateList.size() > 0) {
				try {
					resultList = readFilterInfo.filterIdList(context,uid, app, candidateList);
				} catch (Exception e) {
					fallbackLogger.error(strRequestInfo + " | query new info through redis happen error: ", e);
				}finally{
					if (fallbackLogger.isDebugEnabled()) {
						fallbackLogger.debug(strRequestInfo + " | moudle=FallbackNewsQ,bACSWithScenario="+bACSWithScenario+",fEnableImpressionACS="+fEnableImpressionACS
								+",filterList size= "+candidateList.size()+ " | read=" + times + ", result size=" + resultList.size() + ", context.getNum()=" + context.getNum());
					}
				}
			}

			if (times >= newsQTimes || resultList.size() >= context.getNum()) {
				break;
			}

			times++;
		} while (true);

		for (String infoId : resultList) {
			if(bACSWithScenario || fEnableImpressionACS){
				infoId = infoId.split("#")[0];
			}
			String category = (String)mapIDCategory.get(infoId);
			if (StringUtils.isEmpty(category))
				continue;
			if (mapCategoryDataList.get(category) == null) {
				mapCategoryDataList.put(category, new ArrayList<String>());
			}
			mapCategoryDataList.get(category).add(infoId);
		}

		@SuppressWarnings("unchecked")
		List<String> resultListIDs = interleave((ArrayList<String>[]) mapCategoryDataList.values().toArray(new ArrayList[0]));
		List<ResponParam> list = new ArrayList<ResponParam>();
		for (String infoId : resultListIDs) {
			if (list.size() > context.getNum()) {
				break;
			}

			double gmp = 1 - mapIDScores.get(infoId);
			ResponParam responParam = new ResponParam(Strategy.FALLBACK.getCode(), infoId);
			responParam.setGmp(gmp);
			list.add(responParam);
			fallbackLogger.info(strRequestInfo + " | fallback ranking add id:" + infoId + ", info:" + mapIDCategory.get(infoId) + ", gmp:" + String.format("%.6f", gmp));
		}

		long end = System.currentTimeMillis();
		fallbackLogger.info(strRequestInfo + " | end get fallback data, cost: " + (end - begin) + " ms ");
		return list;
	}

	@SafeVarargs
	public static <t> List<t> interleave(List<t>... lists) {
		int max = 0, sum = 0;
		for (List<t> l : lists) {
			assert l != null;
			if (l.size() > max) {
				max = l.size();
			}
			sum += l.size();
		}
		List<t> ret = new ArrayList<>(sum);
		for (int i = 0; i < max; i++) {
			for (List<t> l : lists) {
				if (i < l.size()) {
					ret.add(l.get(i));
				}
			}
		}
		return ret;
	}
}
