package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.ContentType;
import com.inveno.common.enumtype.Strategy;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.cms.CMSInterface;
import com.inveno.core.process.cms.MixedRule;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.thrift.ResponParam;

@Component("sourceRankMixedControl")
public class SourceRankMixedControl implements IPostPolicy<List<ResponParam>>
{
	private Log logger = LogFactory.getLog(this.getClass());

	private static final String DEFAULT_SOURCE_ID = "0";

	@Autowired
	private CMSInterface cMSInterface;

	@Autowired
	private MonitorLog monitorLog;

	@Override
	public List<ResponParam> process(Context context)
	{
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();
		if (logger.isDebugEnabled())
		{
			logger.debug(" uid: "+ uid  + " ,app "+  app +" begin SourceRankMixedControl " + ",time is " +(System.currentTimeMillis()) +" ,and cur = " + System.currentTimeMillis());
		}

		List<ResponParam> recommendList =  context.getResponseParamList();//推荐列表
		//modified by Genix.Li@2017/04/25, apply set of content_id rather than ResponParam to resolve redundant issue.
		Set<String> allResultSet = new HashSet<String>();
		List<ResponParam> returnList = new ArrayList<ResponParam>();//最终结果

		//取出付费列表
		List<ResponParam> paySourceList = new ArrayList<ResponParam>();
		List<ResponParam> moodList = new ArrayList<ResponParam>();
		for (ResponParam param : recommendList)
		{
			if (((int)param.getSourceRank() & 0xff) == 1)
			{
				paySourceList.add(param);
			}
			if (((int)param.getSourceRank() & 0xff) == 2)
			{
				moodList.add(param);
			}
		}

			
	
		//如果混插配置为空,直接返回
		Map<Integer, MixedRule> map = cMSInterface.getHighMixRule(context);

		if (context.getRecommendInfoData() != null && MapUtils.isNotEmpty(map))
		{
			int maxPos = cMSInterface.getMaxPosOfHighMix(context);
			if (logger.isDebugEnabled())
			{
				logger.debug(" uid: "+ uid  + " ,app "+  app +" ,and maxpos is  " + maxPos+ "," + System.currentTimeMillis()  );
			}


		Map<String, List<ResponParam>> highMix = null;
		if(ContextUtils.isBeautyChannel(context)){
		    highMix = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.BEAUTY.getValue());
		}else if(ContextUtils.isMemesChannel(context)){
		    highMix = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.MEME.getValue());
		}else if(ContextUtils.isGifChannel(context)){
            highMix = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.GIF.getValue());
        }else if(ContextUtils.isVideoChannel(context)){
            highMix = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.VIDEO.getValue());
        }else {
            highMix = context.getRecommendInfoData().getContenttypeStrategyDocumentList().get(ContentType.NEWS.getValue());
        }



			if (MapUtils.isNotEmpty(highMix)) {
				for (int i = 0; i <= maxPos && recommendList.size() > 0; i++)
				{
					MixedRule mixedrule = map.get(i);
					if (null != mixedrule)
					{
						double ratio = Math.random();
						int index = 0;
						for (int j = 0 ; j< mixedrule.getRatio().size();j++)
						{
							if (mixedrule.getRatio().get(j) >= ratio)
							{
								index = j;
								break;
							}
						}

						List<String> sourceList = mixedrule.getSource().get(index);

						if (logger.isTraceEnabled())
						{
							logger.trace("uid: "+ uid  + " ,app "+  app +" ,abtest "+ abtest +" , sourceList " + sourceList +" , ratio is "+ ratio +",and index is " + i );
						}

						String sourceId = sourceList.get(0);
						if (sourceList.size() == 1)
						{
							sourceId =  sourceList.get(0);
						}
						else
						{
							Random r = new Random();
							sourceId = sourceList.get(r.nextInt(sourceList.size())) ;
						}

						List<ResponParam> advanceList = null;
						if (sourceId.equals(Strategy.MIXED_INSERT_PAID.getCode()))
						{
							advanceList = paySourceList;
						}
						else if (sourceId.equals(Strategy.MIXED_INSERT_MOOD.getCode()))
						{
							advanceList = moodList;
						}
						else if (sourceId.equals(DEFAULT_SOURCE_ID))
						{
							advanceList = recommendList;
						}
						else
						{
							advanceList = highMix.get(sourceId);
						}

						if (logger.isTraceEnabled())
						{
							logger.trace("uid: "+ uid  + " ,app "+  app +" ,abtest "+ abtest +" , sourceId " + sourceId +",and list is " + advanceList);
						}


						int resSize = allResultSet.size();
						if (CollectionUtils.isNotEmpty(advanceList))
						{
							
							ResponParam param = advanceList.remove(0);
							String infoId = param.getInfoid();
							//at least add one info into result from mixed-info-list.
							while (!allResultSet.add(infoId) && advanceList.size() > 0)
							{
								param = advanceList.remove(0);
								infoId = param.getInfoid();
							}
							//if nothing could be added from mixed-info-list, just add one info from recommend-info-list
							if (resSize == allResultSet.size() && !sourceId.equals(DEFAULT_SOURCE_ID))
							{
								param = recommendList.remove(0);
								infoId = param.getInfoid();
								while (!allResultSet.add(infoId) && recommendList.size() > 0)
								{
									param = recommendList.remove(0);
									infoId = param.getInfoid();
								}
								//if adding from recommend-info-list, correct the strategy as recommendation
								sourceId = DEFAULT_SOURCE_ID;
							}
							//if something is added, set correct strategy code.
							if (resSize != allResultSet.size())
							{
								if (sourceId.equals(Strategy.MIXED_INSERT_PAID.getCode()))
								{
									param.setStrategy(Strategy.MIXED_INSERT_PAID.getCode());
								}
								else if (sourceId.equals(Strategy.MIXED_INSERT_MOOD.getCode()))
								{
									param.setStrategy(Strategy.MIXED_INSERT_MOOD.getCode());
								}
								else if (sourceId.equals(DEFAULT_SOURCE_ID))
								{
									//advanceList = recommendList;
								}
								else
								{
									param.setStrategy(sourceId);
								}
								returnList.add(param);
							}

							if (logger.isTraceEnabled())
							{
								logger.trace("uid: "+ uid  + " ,app "+  app +" ,abtest "+ abtest +" , sourceId " + sourceId +",and index is " + i +",and list is " + returnList.subList(0, i+1));
							}
							
						}
						else if (CollectionUtils.isNotEmpty(recommendList))
						{
							
							ResponParam param = recommendList.remove(0);
							String infoId = param.getInfoid();
							while (!allResultSet.add(infoId) && recommendList.size() > 0)
							{
								param = recommendList.remove(0);
								infoId = param.getInfoid();
							}
							if (resSize != allResultSet.size())
							{
								returnList.add(param);
							}
						}
					}
				}
			}

			
			returnList.addAll(recommendList);
			context.setResponseParamList(returnList);
			
			long end  = System.currentTimeMillis();
			monitorLog.addResTimeLogByProduct(context, MonitorType.SOURCERANKRULE_RESPONSE_TIME,(end - cur));
			if (logger.isInfoEnabled())
			{
				logger.info(" uid: "+ uid  + ", app: "+ app +" language: "+context.getLanguage() +" end SourceRankMixedControl, time is " +(end-cur) +", size is " + returnList.size()  +", and cur = " + System.currentTimeMillis() );
			}	
	}
		return null;
	}
}
