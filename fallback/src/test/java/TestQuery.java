import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

public class TestQuery
{
	public static void main(String[] args) {
		String firmApp = "noticias";
		Integer categoryId = Integer.parseInt(args[0]);
		String language = "Spanish";
		String contentType = "1";
		java.util.List<String> alContentTypes = java.util.Arrays.asList(contentType);
	
		System.out.println(" FirmApp:" + firmApp +",categoryId :" + categoryId+"args:"+args);
    	try {
			List<Integer> categoryIdList = new ArrayList<Integer>();
			categoryIdList.add(categoryId);

			//adult_score
		    Double adultScoreValue = Double.valueOf(0.6d);
		    String adultScoreVersion = "v2";
		    
			//设定ES查询条件
			ESQCondition esqCondition = new ESQCondition();
			
			//公共条件查询如(app,category等)
			BoolQueryBuilder qbuilder = (BoolQueryBuilder)CommonQueryBuild.getCommonQueryBuilder(firmApp.toLowerCase(), categoryIdList, false);
			
			//语言种类
			String[] versions = new String[]{"v2", "v3"};
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
				esQryImgCnt = Integer.valueOf("1");
			} else {
				QueryBuilder matchQuery = QueryBuilders.termQuery("content_type", contentType);
				qbuilder.must(matchQuery);
				esQryImgCnt = Integer.valueOf("0");
			}
			
			QueryBuilder pic_rangeQuery = QueryBuilders.rangeQuery("body_image_count").gte(esQryImgCnt);
			qbuilder.must(pic_rangeQuery);
			
			//N天之内时间
			RangeQueryBuilder timeLimit = QueryBuilders.rangeQuery("publish_time_ts");
			timeLimit.gte(DateUtils.addHours(new Date(), -48).getTime() / 1000);
			timeLimit.lte(DateUtils.addHours(new Date(), 0).getTime() / 1000);
			qbuilder.must(timeLimit);
			
			String adultScoreEsKey = "adult_score"+FallbackConstant.BLANK_VALUE+adultScoreVersion;
			String ctrEsKey = firmApp+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//渠道CTR名称
			String allCtrEsKey = FallbackConstant.ALL_ES_CTR_NAME+FallbackConstant.BLANK_VALUE+FallbackConstant.ES_CTR_NAME;//-1CTR名称

			//依 -1 gmp 排序
			FieldSortBuilder fieldSort = SortBuilders.fieldSort(allCtrEsKey).order(SortOrder.DESC);
			esqCondition.setSortBuilder(fieldSort);
			
			//查询数量
			esqCondition.setSize(1000);
			esqCondition.setQueryBuilder(qbuilder);

			System.out.println("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType);
			System.out.println("es query condition :" + qbuilder.toString());

			String[] arrAvailableField = new String[]{"group_id", "content_id", "publish_time_ts", "content_type", "link_type", "display_type", adultScoreEsKey, ctrEsKey, allCtrEsKey};

			ElasticSearchHelper esHelper = ElasticSearchHelper.newInstance(new String[]{"10.10.10.100:9300","10.10.10.101:9300","10.10.10.102:9300","10.10.10.103:9300"}, "ELK-us-east-1");

			SearchResponse response = esHelper.search(new String[]{FallbackConstant.ES_SEARCHE_INDEX}, new String[]{FallbackConstant.ES_SEARCHE_TYPE}, esqCondition, arrAvailableField);

			System.out.println("firmApp:" + firmApp + ", categoryId:" + categoryId + ", contentType:" + contentType + " es query count :" + response.getHits().getHits().length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}