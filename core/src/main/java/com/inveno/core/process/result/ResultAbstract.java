package com.inveno.core.process.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.expinfo.MEMEExpInfoOfImpressionProcess;
import com.inveno.core.process.expinfo.VideoExpInfoOfImpressionProcess;
import com.inveno.core.process.expinfo.face.ExpInfo;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.util.KeyUtils;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.impl.ArticleFilterImpl;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

@Component
public abstract class ResultAbstract implements IResultProcess
{
	public static final String SUFFIX_IMPRESSION_ACS_KEY = "impression";

	@Autowired
	private JedisCluster jedisCluster;

	@Autowired
	private ExpInfo expInfoProcess;

	@Autowired
	private ExpInfo expInfoOfImpressionProcess;

	@Autowired
	private VideoExpInfoOfImpressionProcess videoExpInfoOfImpressionProcess;

	@Autowired
	private MEMEExpInfoOfImpressionProcess memeExpInfoOfImpressionProcess;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Autowired
	private MonitorLog monitorLog;

	private Log logger = LogFactory.getLog(ResultAbstract.class);

	private static Log cmslogger = LogFactory.getLog("cmslog");

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	public Map<String,Map<Double, List<String>>> forceInsertCache = new ConcurrentHashMap<String, Map<Double, List<String>>>();

	private Map<String,Set<String>> topInfoCache = new ConcurrentHashMap<String, Set<String>>();

	@Override
	public void cache(Context context) {
		beforCache(context);
		getInfoByCache(context);
		afterCache(context);
	}

	public void beforCache(Context context) {}

	public void getInfoByCache(Context context) {}

	public void afterCache(Context context) {
		displayTypeStg(context);
	}

	@Override
	public void noCache(Context context) {
		beforNoCache(context);
		getInfoByNoCache(context);
		afterNoCache(context);
	}

	public void beforNoCache(Context context) {}

	public void getInfoByNoCache(Context context) {}

	public void afterNoCache(Context context) {
		displayTypeStg(context);
	}


	/**
	 * display type 重排
	 * @param context
	 * 1、Display_type处理规则放在core里处理；
	 * 2、Display_type现行处理逻辑为，在同一次request下发中：
	 *  a）取用户上报的display_type能力集合 交 文章的display_type能力集合，取最高位
	 *  b）通栏或banner须<=1
	 *  c）对于支持多图和三图的文章：
	 *       针对新加坡，需要判断app_ver
	 *              若app_ver < Hotoday_v2.2.8 或 Mata_v1.1.0，则以多图形式下发；
	 *              若app_ver >= Hotoday_v2.2.8 或 Mata_v1.1.0，则以三图形式下发；
	 *       针对美东，不需判断app_ver，一律以三图形式下发
	 *  d）对其它display_type不做限制
	 */
	private void displayTypeStg(Context context) {
		boolean ifDisplayType = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifDisplayType"));

		if (!ifDisplayType) {
			return ;
		}
		//国内不做控制
		if( context.getLanguage().equalsIgnoreCase("zh_CN") ){
			return ;
		}
		//需要为infoList 接口
		if( null == context.getZhiziListReq()  ){
			return ;
		}

		List<ResponParam> resultList = context.getResponseParamList();
		int sizeOfTongLan = 0;
		if( CollectionUtils.isNotEmpty(resultList) ){
			for (ResponParam re : resultList) {
				if( re.getStrategy().equals(Strategy.TOP) )  {
					sizeOfTongLan++;
					re.setDisplayType(0x80);
				}else{

					if( re.getDisplayType() != 0 ){

						int dispaly = (int) ((re.getDisplayType())&(context.getDisplay()));
						//获取display的最高位
						int highestOneBit = Integer.highestOneBit(dispaly);
						//判断是否为通栏,最多有一条
						if( sizeOfTongLan <= 0 ){
							if( (re.getDisplayType()& 0x08) != 0 ||  (dispaly& 0x80) != 0){
								sizeOfTongLan++;
								re.setDisplayType(0x08);
							}
							//已经有banner则需要去除通栏位
						}else{
							if( 0x08 == highestOneBit ){
								highestOneBit = Integer.highestOneBit(dispaly-0x08);
							}
						}
						/**
						 * 对于支持多图和三图的文章:针对新加坡,需要判断app_ver
						 * 若app_ver < Hotoday_v2.2.8 或 Mata_v1.1.0，则以多图形式下发;
						 * 若app_ver >= Hotoday_v2.2.8 或 Mata_v1.1.0，则以三图形式下发针对美东,不需判断app_ver,一律以三图形式下发
						 */
						String appVersion = context.getZhiziListReq().getAppVersion();
						if( (appVersion.contains("Hotoday") || appVersion.contains("Mata")) &&  (re.getDisplayType()& 0x04) != 0 && (re.getDisplayType()& 0x10) != 0 ){
							if( appVersion.compareToIgnoreCase("Hotoday_v2.2.8") < 0 || appVersion.compareToIgnoreCase("Mata_v1.1.0") < 0 ){
								re.setDisplayType(0x10);//多图文章
							}else{
								re.setDisplayType(0x04);//三图文章
							}
						}else{
							re.setDisplayType(highestOneBit);
						}
					}
				}
			}
		}
	}

	/**
	 * 返回的资讯
	 * @param context
	 * @param resultSet
	 * @param list
	 * @return
	 */
	public List<ResponParam> infoToReturn(Context context, boolean ifCache, Set<ResponParam> resultSet) {

		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();
		List<ResponParam> resultList = new ArrayList<ResponParam>();
		List<ResponParam> relist = context.getResponseParamList();
		List<Double> indexList =  new ArrayList<Double>();

		String uid = context.getUid() ;
		String app = context.getApp() ;
		int reqFetchCnt = context.getNum();

		for (int i = 0; i < reqFetchCnt; i++) {
			resultList.add(null);//初始化返回list
			indexList.add( (double)i );//索引位置;
		}

		//强插资讯
		getStgInfo(resultList, indexList, context, resultSet);
		end = System.currentTimeMillis();
		logger.info("uid is :"+ uid + " , app is : " + app +  "  end getStgInfo time is " + (end-cur) +",and  remain size is " + indexList.size() +",and cur = " + System.currentTimeMillis());

		if (indexList.size() == 0)
		{
			context.setIndexList(indexList);
			return resultList;
		}

		//流量探索
		addFlowExplorationInfo(context, resultList, indexList, resultSet, ifCache);
		end = System.currentTimeMillis();
		logger.info("uid is :"+ uid + ", app is " + app + "  end addFlowExplorationInfo time is " + (end-cur) +",and remain size is " + indexList.size() +",and cur = " + System.currentTimeMillis());

		if (indexList.size() == 0)
		{
			context.setIndexList(indexList);
			return resultList;
		}

		//增加短期兴趣
		//addShortInterest(context, resultList, indexList,resultSet);
		/*end  = System.currentTimeMillis();
		logger.info(" uid is :"+ uid + ",end addShortInterest time is " + (end-cur) +" ,and result is " + resultList +" ,and  indexList is "+ indexList +" ,and cur = " + System.currentTimeMillis());

		if( indexList.size() == 0 ){
			context.setIndexList(indexList);
			return resultList;
		}
		*/
		//继续拼接推荐的资讯
		if (!ifCache)
		{
			addInfo(context, relist, resultList, indexList, resultSet);
		}

		context.setIndexList(indexList);
		return resultList;
	}

	public abstract void addInfo(Context context, List<ResponParam> relist, List<ResponParam> resultList, List<Double> indexList, Set<ResponParam> resultSet);

	/**
	 * addExpInfo
	 * @param context
	 * @param resultList
	 * @param indexList
	 * @param resultSet
	 * @param ifCache
	 */
	private void addFlowExplorationInfo(Context context,List<ResponParam> resultList,List<Double> indexList, Set<ResponParam> resultSet, boolean ifCache  ) {
		int channelId = ContextUtils.getChannelId(context);
		if( context.getZhiziListReq() == null && channelId > 0 ){
			return ;
		}

		String uid = context.getUid();
		String app = context.getApp();
		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();

		List<Double> indexListForNoCache = new ArrayList<Double>();
		if( !ifCache ){
			indexListForNoCache = indexList.stream().filter( (index) ->  (index != 0 && index!=1 && index != 2) ).collect(Collectors.toList());
		}else{
			indexListForNoCache.addAll(indexList);
		}


		if (indexListForNoCache.size() > 0) {
			boolean bUseImpression = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "useImpression"));
			try {
				if (!bUseImpression) {
					//获取流量探索的资讯
					expInfoProcess.getExpInfo(context, indexListForNoCache, resultList, indexList, resultSet, bUseImpression);
					monitorLog.addResTimeLogByProduct(context, MonitorType.EXP_INFO_RESPONSE_TIME, System.currentTimeMillis() -cur);
				} else {
					boolean bVideoChannel = ContextUtils.isVideoChannel(context);
					boolean bMemesChannel = ContextUtils.isMemesChannel(context);
					if (bVideoChannel) {
						videoExpInfoOfImpressionProcess.getExpInfo(context, indexListForNoCache, resultList, indexList, resultSet, bUseImpression);
					} else if (bMemesChannel) {
						memeExpInfoOfImpressionProcess.getExpInfo(context, indexListForNoCache, resultList, indexList, resultSet, bUseImpression);
					} else{
						expInfoOfImpressionProcess.getExpInfo(context, indexListForNoCache, resultList, indexList, resultSet, bUseImpression);
					}
					monitorLog.addResTimeLogByProduct(context, MonitorType.EXP_INFO_RESPONSE_TIME, System.currentTimeMillis() -cur);
				}
			} catch (Exception e) {
				logger.error("addFlowExplorationInfo getExpInfo error ,and uid is "  + uid + " abtest "+  context.getAbtestVersion(), e);
			}

			if (logger.isDebugEnabled()) {
				end  = System.currentTimeMillis();
				logger.debug("uid :"+uid +", app: "+ app +" end getExpInfo time is " + (end - cur) +" and  resultList is " + resultList
						+",and cur = " + System.currentTimeMillis());
			}
		}
	}

	/**
	 *
	 * @param resultList
	 * @param indexList
	 * @param resultSet
	 * @param reqFetchCnt
	 * @param offset
	 * @param uid
	 * @param app
	 * @param abTestVersion
	 */
	public void getStgInfo(List<ResponParam> resultList, List<Double> indexList, Context context, Set<ResponParam> resultSet)
	{
		int reqFetchCnt = context.getNum();
		int offset = context.getOffset();
		String uid = context.getUid();
		String app = context.getApp();
		String abTestVersion =  context.getAbtestVersion();
		long scenario = context.getScenario();

		long cur = System.currentTimeMillis();
		long end  = System.currentTimeMillis();
		int cmsMaxCnt = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "cmsMaxCnt"), 30);
		boolean bForceInsert = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifForceInsert"));

		if (!bForceInsert) {
			return ;
		}

		String interfaceName = "q";
		String semantic = "channel";
		if (context.getZhiziListReq() != null) {
			interfaceName = "list";
			semantic = "scenario";
		} else if (context.getRecNewsListReq() != null) {
			if (!context.getRecNewsListReq().isNeedBanner()) {
				interfaceName = "q";
			} else {
				interfaceName = "qb";
			}
		} else if (context.getScenario() > 0) {
			interfaceName = "qcn";
		}

		StringBuffer timeliness_high = new StringBuffer("stginfo:timeliness:high:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);

		StringBuffer timeliness_low = new StringBuffer("stginfo:timeliness:low:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);


		//设置此两个 pos2Resp,和list位置,是由于需要过已读,可能存在强插资讯已读的情况,因此设置此两个变量,
		//如果直接拼接resultList,由于存在null占位符,直接过已读可能存在问题;
		HashMap<ResponParam, Double> resp2Pos = new HashMap<ResponParam, Double>();
		List<ResponParam> list2Acs = new ArrayList<ResponParam>();

		Map<Double, List<String>> highMap = forceInsertCache.get(timeliness_high.toString());
		Map<Double, List<String>> lowMap = forceInsertCache.get(timeliness_low.toString());

		if (lowMap == null || lowMap.isEmpty() )
		{
			this.forceInsertCache.put(timeliness_low.toString(),  Collections.emptyMap());
		}
		if (highMap == null || highMap.isEmpty())
		{
			this.forceInsertCache.put(timeliness_high.toString(), Collections.emptyMap());
		}

		if (lowMap != null)
		{
			if (offset >= cmsMaxCnt)
			{
				//do nothing
			}
			else
			{
				//遍历
				for (Map.Entry<Double, List<String>> entry : lowMap.entrySet())
				{
					double key = entry.getKey();
					if (key >= offset + 1 && key <= offset + reqFetchCnt)
					{
						List<String> alCandidate = entry.getValue();
						String strategyCode = "";
						String contentId = null;
						//only through acs w/ force_insert candidates for lock screen
						if (alCandidate.size() > 1 && ContextUtils.isLockscreen(context))
						{
							strategyCode = Strategy.FIRSTSCREEN_FORCE_INSERT.getCode();
							ArrayList<String> filteredListTmp = new ArrayList<String>();
							//candidateId pattern : ${infoid}#${contentType}#${linkType}#${displayType}
							HashMap<String, String> mInfoIdWithType = new HashMap<String, String>();
							for (String infoIdWithType : alCandidate)
							{
								StringBuffer sb = new StringBuffer();
								String infoId = infoIdWithType.split("#")[0];
								mInfoIdWithType.put(infoId, infoIdWithType);
								sb.append(infoId);
								sb.append("#");
								sb.append(String.valueOf(scenario));
								sb.append("#");
								sb.append(SUFFIX_IMPRESSION_ACS_KEY);
								filteredListTmp.add(sb.toString());
							}
							List<String> filteredList = (ArrayList<String>)filteredListTmp.clone();
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " starting acs filter for FIRSTSCREEN_FORCE_INSERT result_list.size=" + filteredList.size() + "\tresult_list=" + filteredList);
							try
							{
								filteredList = readFilterInfo.filterIdList(context,uid, app, filteredList);
							}
							catch (Exception e)
							{
								logger.error("", e);
							}
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " finishing acs filter for FIRSTSCREEN_FORCE_INSERT result_list.size=" + filteredList.size() + "\tresult_list=" + filteredList);
							for (String infoId : filteredList)
							{
								infoId = infoId.split("#")[0];
								String infoIdWithType = (String)mInfoIdWithType.get(infoId);
								infoId = filterTypes(infoIdWithType, context);
								if (StringUtils.isNotEmpty(infoId))
								{
									contentId = infoId;
									break;
								}
							}
							logger.info("uid is "  + uid + " abtest "+  abTestVersion + " apply content for FIRSTSCREEN_FORCE_INSERT content_id=" + contentId);
						}
						else if (alCandidate.size() > 0)
						{
							String value = (String)alCandidate.get(0);
							contentId = filterTypes(value, context);
							strategyCode = Strategy.FORCE_INSERT.getCode();
						}
						if (StringUtils.isNotEmpty(contentId))
						{
							ResponParam re = new ResponParam(strategyCode, contentId);
							resp2Pos.put(re, key - offset - 1);
							list2Acs.add(re);
						}
					}
				}
			}
		}

		if (highMap != null)
		{
			for (Map.Entry<Double, List<String>> entry : highMap.entrySet())
			{
				String value = entry.getValue().get(0);
				String contentId = filterTypes(value, context);
				if (StringUtils.isNotEmpty(contentId))
				{
					ResponParam re = new ResponParam(Strategy.FORCE_INSERT.getCode(), contentId);
					resp2Pos.put(re, 0.0);
					list2Acs.add(re);
				}
			}
		}

		List<ResponParam> list2AcsTmp = list2Acs;
		//过已读
		if (CollectionUtils.isNotEmpty(list2AcsTmp))
		{
			if (logger.isDebugEnabled())
			{
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",app is "+ app +" begin cmsStg readFilter time is " + (end-cur)  +",and size is " + list2AcsTmp.size() +",and cur = " + System.currentTimeMillis());
			}
			list2AcsTmp = readedFilter(context,uid, app, list2AcsTmp);
			if (logger.isDebugEnabled())
			{
				end  = System.currentTimeMillis();
				logger.debug("uid is :"+ uid + ",app is "+ app +" end cmsStg readFilter time is " + (end-cur)  +",and size is " + list2AcsTmp.size() +",and cur = " + System.currentTimeMillis());
			}
		}


		//结果拼接
		for (ResponParam resp : list2AcsTmp)
		{
			if (resultSet.add(resp) && indexList.size() > 0)
			{
				resultList.set(resp2Pos.get(resp).intValue(), resp);
				indexList.remove(resp2Pos.get(resp));
			}

			threadPoolTaskExecutor.submit(()->{
				//判断是否和cache中重复,如果重复则需要将q中进行删除
				if( jedisCluster.hexists( KeyUtils.getDetailKeyByUid(context) , resp.getInfoid())){
					jedisCluster.zrem( KeyUtils.getMainKey(context).getBytes(),  resp.getInfoid().getBytes() );
					if(logger.isDebugEnabled()){
						logger.debug("uid is :"+ uid + ",app is "+ app +" cmsStg removeDuplicate time is " + ( System.currentTimeMillis()-cur) +",and infoid  is " + resp.getInfoid() +",and cur = " + System.currentTimeMillis());
					}
				}
			});
		}
	}

	@Scheduled(cron="0/30 * * * * ? ")
	public void loadForceInsert(){
		cmslogger.info("loadForceInsert key :" + this.forceInsertCache.keySet()) ;
		Set<String> keyset = forceInsertCache.keySet();
		for (String key : keyset)
		{
			try {
					if (key.contains("stginfo:timeliness:high:"))
					{
						Set<byte[]> timeliness_high_info = jedisCluster.zrange(key.getBytes(), 0, 0);
						Map<Double, List<String>> highMap = new HashMap<Double, List<String>>();
						for (byte[] string : timeliness_high_info)
						{
							ArrayList<String> alCandidate = new ArrayList<String>();
							alCandidate.add(new String(string));
							highMap.put(0.0D, alCandidate);
						}
						if (highMap.isEmpty())
						{
							this.forceInsertCache.remove(key);
						}
						else
						{
							this.forceInsertCache.put(key, highMap);
						}
					}
					else
					{
						Set<Tuple> timeliness_lower_info = jedisCluster.zrangeWithScores(key.getBytes(), 0, -1);
						Map<Double, List<String>> lowMap = new HashMap<Double, List<String>>();
						for (Tuple tuple : timeliness_lower_info)
						{
							if (StringUtils.isNotEmpty(tuple.getElement()))
							{
								double pos = tuple.getScore();
								ArrayList<String> alCandidate = (ArrayList<String>)lowMap.get(pos);
								if (alCandidate == null)
								{
									alCandidate = new ArrayList<String>();
									lowMap.put(pos, alCandidate);
								}
								alCandidate.add( tuple.getElement() );
							}
						}
						if (lowMap.isEmpty())
						{
							this.forceInsertCache.remove(key);
						}
						else
						{
							this.forceInsertCache.put(key, lowMap);
						}
					}
				} catch (Exception e) {
					logger.error("initForceInsert load key " + key, e);
					continue;
				}
		}

		cmslogger.info("loadForceInsert load cache form redis :" + forceInsertCache) ;
	}

	static private String filterTypes(String contentInfo, Context context)
	{
		if( context.getZhiziListReq() != null &&  contentInfo.split("#").length == 4 )
		{
			ArticleProperty article_property = context.getArticleProperty();
			ArticleFilterImpl articleFilter = context.getArticleFilter();

			String contentInfoArr[] = contentInfo.split("#");
			article_property.setType(contentInfoArr[1]); //content_type
			article_property.setLink_type(contentInfoArr[2]);
			article_property.setDisplay_type(contentInfoArr[3]);
			article_property.setContent_id(contentInfoArr[0]);

			if ( !articleFilter.isValidArticle(article_property) )
			{
				return null;
			}
			else
			{
				return contentInfo.split("#")[0];
			}
		}
		else
		{
			return contentInfo;
		}
	}

	/**
	 * 获取置顶资讯;
	 * @param resultList
	 * @param context
	 */
	public void getTopInfo(List<ResponParam> resultList, Context context) {

		boolean ifTopInfo = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "ifTopInfo"));

		if( !ifTopInfo ){
			return ;
		}

		if( context.getZhiziListReq() != null  ){
			if( context.getZhiziListReq().getOperation() == 2 || context.getZhiziListReq().getOperation() == 3){
				return ;
			}
		}else{
			return ;
		}

		String interfaceName = "q";
		String semantic = "channel";
		String abTestVersion = context.getAbtestVersion();
		String uid = context.getUid();
		long cur = System.currentTimeMillis();

		if (context.getZhiziListReq() != null) {
			interfaceName = "list";
			semantic = "scenario";
		} else if (context.getRecNewsListReq() != null) {
			if (!context.getRecNewsListReq().isNeedBanner()) {
				interfaceName = "q";
			} else {
				interfaceName = "qb";
			}
		} else if (context.getScenario() > 0) {
			interfaceName = "qcn";
		}

		StringBuffer topkey = new StringBuffer("topinfo:").append(context.getLanguage())
				.append(":").append(interfaceName).append(":").append(semantic).append(":")
				.append(context.getScenario()).append(":").append(abTestVersion);

		Set<String> topinfoId = topInfoCache.get(topkey.toString());
		if( null == topinfoId || topinfoId.size() <=0 ){
			topInfoCache.put(topkey.toString() , Collections.emptySet());
			topinfoId = Collections.emptySet();
		}

		List<ResponParam> list2Acs = new ArrayList<ResponParam>();
		for (String infoId : topinfoId) {
			list2Acs.add( new ResponParam( Strategy.TOP.getCode() , infoId) );
		}

		//过已读
		/*if( CollectionUtils.isNotEmpty(list2Acs) ){
			logger.info("uid is :"+ uid + ",app is "+ app +" begin getTopInfo readFilter time is " + (System.currentTimeMillis()-cur) +" ,and size is " + list2Acs.size() +" ,and cur = " + System.currentTimeMillis());
			list2Acs =	readedFilter(uid, app, list2Acs);
			logger.info("uid is :"+ uid + ",app is "+ app +" end getTopInfo readFilter time is " + (System.currentTimeMillis()-cur) +" ,and size is " + list2Acs.size() +" ,and cur = " + System.currentTimeMillis());
		}*/

		for (ResponParam re : list2Acs) {
			resultList.add( 0, re );
		}

		if( logger.isDebugEnabled()  ){
			logger.debug("uid is :"+ uid + ",end getTopInfo time is " + (System.currentTimeMillis()-cur)
					+" ,and info is " + list2Acs +" ,and cur = " + System.currentTimeMillis() + " topinfoId " + topinfoId);
		}
	}

	@Scheduled(cron="0 0/1 * * * ? ")
	public void loadTopCache(){
		cmslogger.info("loadTopCache key :" + this.topInfoCache) ;
		Set<String> keyset =  topInfoCache.keySet();
		for (String key : keyset) {
			try {
					Set<String> topInfo = jedisCluster.zrange(key,0,0);
					if( topInfo.size()<=0 ){
						this.topInfoCache.remove(key);
					}else{
						this.topInfoCache.put(key, topInfo);
					}

				} catch (Exception e) {
					logger.error("initForceInsert load key " + key, e);
					continue;
				}
		}

		cmslogger.info("loadTopCache load cache form redis :" + topInfoCache) ;
	}


	/**
	 * 增加最新资讯
	 * @param resultList
	 * @param i
	 * @param lastestInfoList
	 * @param indexlist
	 * @param resultSet
	 */
	public void addNewInfo(List<ResponParam> resultList, int reqNum,List<ResponParam> lastestInfoList, List<Double> indexlist, Set<ResponParam> resultSet) {

		for (int i = 0; i < lastestInfoList.size() && indexlist.size() > 0 ; i++) {
			ResponParam res = lastestInfoList.get( i );
			if( resultSet.add(res) ){
				resultList.set( indexlist.get(0).intValue(),  lastestInfoList.get( i ) );
				indexlist.remove( 0 );
			}
		}

	}




	/**
	 * @throws Exception
	 *
	 * readedFilter(已读过滤)
	 * @Title: readedFilter
	 * @param @param list
	 * @param @return    设定文件
	 * @return List<String>    返回类型
	 * @throws
	 */
	private List<ResponParam> readedFilter(Context context, String uid,String app, List<ResponParam> list){
		//long cur = System.currentTimeMillis();
		//logger.info(" begin  ================ ResultProcessAllImpl readedFilter====================time is " + (System.currentTimeMillis() - cur));
		//ReadedInfoHandler readFilterInfo =  SysUtil.getBean("readFilterInfo");
		try {
			list = readFilterInfo.filterIds(context,uid, app, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//logger.info(" end ================ResultProcessAllImpl readedFilter====================time is " + (System.currentTimeMillis() - cur));
		return list;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	@Deprecated
	public void addShortInterest(Context context,List<ResponParam> resultList,List<Double> indexList ,Set<ResponParam> resultSet  ){

		long begin =  System.currentTimeMillis();
		if( indexList.size() <= 0 ){
			return ;
		}

		String uid = context.getUid();
		String app = context.getApp();

		//if isAddShortInterest
		boolean isAddShortInterest = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "isAddShortInterest"));

		if( !isAddShortInterest ){
			return ;
		}

		//key
		StringBuilder key = new StringBuilder();
		key.append("related-articles-"+UsrIdHash(uid, 200));
		String valueOfInterest = jedisCluster.hget(key.toString(), uid);

		//如果短期兴趣不为空
		List<ResponParam> list = new ArrayList<ResponParam>();

		if(logger.isDebugEnabled()){
			logger.debug("uid is :"+ uid + ",app is "+ app +" addShortInterest time is " + (System.currentTimeMillis()- begin)
					+",and valueOfInterest  is " + valueOfInterest +",and cur = " + System.currentTimeMillis());
		}
		if( StringUtils.isNotEmpty(valueOfInterest) ){
			try {
				//parseArray
				JSONObject jsonObject = JSON.parseObject(valueOfInterest);
				List<String> idList = JSON.parseArray(JSON.parseArray(jsonObject.getJSONArray("relatedArticle").toString(), String.class).get(0),String.class);

				//过已读
				ReadedInfoHandler readFilterInfo =  SysUtil.getBean("readFilterInfo");
				List<String> filteredList =  readFilterInfo.filterIdList(context,uid, app, idList);

				//如果已读之后不为空
				if( CollectionUtils.isNotEmpty(filteredList) ){
					//判断是否和缓存中是否重复
					for (String infoId : filteredList) {
						//不存在,1.目前为ranking ,无缓存  2.cache中不存在
						if( jedisCluster.hexists( KeyUtils.getDetailKeyByUid(context) ,  infoId) ){
							jedisCluster.zrem( new String(uid+"::"+app+"::q").getBytes(),  infoId.getBytes() );
							if(logger.isDebugEnabled()){
								logger.debug("uid is :"+ uid + ",app is "+ app +" addShortInterest removeDuplicate time is " + (System.currentTimeMillis()- begin)
										+",and infoid  is " + infoId +",and cur = " + System.currentTimeMillis());
							}
						}else{

							ResponParam res = new ResponParam(Strategy.SHORT_INTEREST.getCode(), infoId);
							if( resultSet.add(res) ){
								list.add(res);
								if(logger.isDebugEnabled()){
									logger.debug("uid :"+uid +", app: "+ app +" end addShortInterest time is " + (System.currentTimeMillis() - begin)
											+" and  list is " + list  +",and cur = " + System.currentTimeMillis());
								}
							}
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error("=== addShortInterest Exception ,and uid is "  + uid + " abtest "+  context.getAbtestVersion() +"===",e);
			}
		}

		if( !CollectionUtils.isEmpty(list) && indexList.size() > 0 ){
			for (int i = 0; i < list.size() && indexList.size() > 0  ; i++) {
				resultList.set( indexList.get( indexList.size() - 1  ).intValue() , list.get(i));
				indexList.remove( indexList.size() - 1 );
			}
		}
	}


	/**
	 * 去重方法
	 * @param context
	 */
	public void removeDulInfo(Context context) {
		List<ResponParam> list = context.getResponseParamList();
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		Set<ResponParam> resultSet = new HashSet<ResponParam>();
		Iterator<ResponParam> it = list.iterator();
		List<ResponParam> newList = new ArrayList<ResponParam>();
		while (it.hasNext()) {
			ResponParam re = it.next();
			if (resultSet.add(re)) {
				newList.add(re);
			}
		}
		//如果有重复,并且有去重则 log.info
		if( newList.size() !=  context.getResponseParamList().size()){
			logger.info("uid is :"+ context.getUid() +" ,checkDulInfo and size is " + newList.size());
		}
		context.setResponseParamList(newList);
	}

	static String UsrIdHash(String str, int len) {
		len = 200;
		final int INT_MAX = 2147483647;
		int h = 0;

		if (str != null) {
			for (int i = 0; i < str.length(); ++i) {
				h = 31 * h + str.charAt(i);
			}
		}
		String hashid = Integer.toString((h & INT_MAX) % len);
		return hashid;
	}
}
