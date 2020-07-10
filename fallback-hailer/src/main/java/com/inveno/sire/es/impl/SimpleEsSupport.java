package com.inveno.sire.es.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.inveno.sire.constant.SireConstant;
import com.inveno.sire.es.Ies;
import com.inveno.sire.util.ESQCondition;
import com.inveno.sire.util.ElasticSearchUtil;
import com.inveno.sire.util.SystemUtils;

/**
 * Created by Klaus Liu on 2016/9/20.
 */
@Component
public class SimpleEsSupport implements Ies {
    private static final Logger monitorLogger = LoggerFactory.getLogger("monitor");
    private static final Logger logger = LoggerFactory.getLogger(SimpleEsSupport.class);

    public void updateDatas(Map<String, Map<String, Double>> mapDocGmp) {
       try {
            List<String> contentIdsList = new ArrayList(mapDocGmp.keySet());

            int nUpdateCount = contentIdsList.size();
            //解决ES too_many_clauses: maxClauseCount is set to 1024问题
            logger.debug("start to updateDatas size " + nUpdateCount);
            if (contentIdsList.size() > SireConstant.ES_MAX_CLAUSE_COUNT) {
                List<List<String>> result = segmentList(contentIdsList, SireConstant.ES_MAX_CLAUSE_COUNT);
                int index = 1;
                for (List<String> subResult : result) {
                    logger.debug("sublist"+index+" start updateDatas size = "+subResult.size());
                    SearchResponse response = segmentSearchEsDatas(subResult);
                    //start update datas of es
                    updateDatasEs(response, mapDocGmp);
                    logger.debug("sublist"+index+" end updateDatas size = "+subResult.size());
                    index++;
                }
            } else {
                SearchResponse response = segmentSearchEsDatas(contentIdsList);
                updateDatasEs(response, mapDocGmp);
            }
            logger.debug("end to updateDatas size " + nUpdateCount);
            String local_ip = java.net.InetAddress.getLocalHost().getHostAddress();
            monitorLogger.info(local_ip + "&&feeder.status.update-es-gmp&&" + nUpdateCount + "&&0&&ORIGINAL&&600&&");
        } catch (Exception e) {
            logger.error("update es error :",e);
        }
    }

    private  List<List<String>> segmentList(List<String> targe,int size) {
        List<List<String>> listArr = new ArrayList<List<String>>();
        //获取被拆分的数组个数
        int arrSize = targe.size()%size==0?targe.size()/size:targe.size()/size+1;
        for(int i=0;i<arrSize;i++) {
            List<String> sub = new ArrayList<String>();
            //把指定索引数据放入到list中
            for(int j=i*size;j<=size*(i+1)-1;j++) {
                if(j<=targe.size()-1) {
                    sub.add(targe.get(j));
                }
            }
            listArr.add(sub);
        }
        return listArr;
    }

    private SearchResponse segmentSearchEsDatas(List<String> segmentContentIdsList){
        ESQCondition esqCondition = new ESQCondition();
        esqCondition.setSize(SireConstant.ES_MAX_CLAUSE_COUNT);//设置查询条数大小
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryBuilder conditionId = QueryBuilders.termsQuery("content_id", segmentContentIdsList);
        boolQueryBuilder.must(conditionId);
        esqCondition.setQueryBuilder(boolQueryBuilder);
        SearchResponse response = ElasticSearchUtil.getInstances().search(new String[]{SireConstant.ES_SEARCHE_INDEX},
                new String[]{SireConstant.ES_SEARCHE_TYPE}, esqCondition);
        logger.debug("search es size="+response.getHits().totalHits());
        return response;
    }

    private void updateDatasEs(SearchResponse response, Map<String, Map<String, Double>> contentIdsMap) {
        try {
            if (response != null) {
                Map<String, Map<String, Object>> mUpdateDoc = new HashMap<String, Map<String, Object>>();
                for (SearchHit hit : response.getHits().getHits()) {
                    String contentId = hit.getId();
                    Map<String, Object> mDoc = hit.getSource();
                    Map<String, Double> gmpMap = contentIdsMap.get(contentId);
                    for (String app : gmpMap.keySet()) {
                        mDoc.put(app+"_"+SireConstant.ES_CTR_NAME, gmpMap.get(app));
                    }
                    mUpdateDoc.put(contentId, mDoc);
                }
                ElasticSearchUtil.bulkUpdate(mUpdateDoc, SireConstant.ES_SEARCHE_INDEX, SireConstant.ES_SEARCHE_TYPE);
            }
        } catch (Exception e) {
            logger.error("update es error", e);
        }
    }
}
