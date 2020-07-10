package com.inveno.core.util;

import com.inveno.common.bean.Context;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.Constants;

public class KeyUtils {
	
	
	/**
	 * eg: uid::app::q , 867605020276604::coolpad::q
	 * 
	 * @param context
	 * @return
	 */
	public static String getMainKey(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		if( context.getApp().equals("fuyiping-gionee") ){
			sb.append(context.getScenario());
			sb.append(Constants.STR_SPLIT_PRE);
		}
		sb.append(Constants.CACHE_NAME_Q);
		return sb.toString();
	}
	
	/**
	 * eg: uid::app::offset , 867605020276604::coolpad::offset
	 * 
	 * @param context
	 * @return
	 */
	public static String getMainOffset(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		if( context.getApp().equals("fuyiping-gionee") ){
			sb.append(context.getScenario());
			sb.append(Constants.STR_SPLIT_PRE);
		}
		sb.append(Constants.CACHE_NAME_OFFSET);
		return sb.toString();
	}

	/**
	 * eg: uid::app::qb , 867605020276604::coolpad::qb
	 * 
	 * @param context
	 * @return
	 */
	public static String getQBKey(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_QB);
		return sb.toString();
	}

	/**
	 * eg: uid::app::qb::offset , 867605020276604::coolpad::qb::offset
	 * 
	 * @param context
	 * @return
	 */
	public static String getQBOffsetKey(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_QB);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_OFFSET);
		return sb.toString();
	}

	/**
	 * eg: uid::app::qcn::channelId , 867605020276604::coolpad::qcn::1
	 * 
	 * @param context
	 * @return
	 */
	public static String getQCNKey(Context context, int channelId) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_QCN);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(channelId);
		return sb.toString();
	}

	/**
	 * eg: uid::app::qcn::channelId::offset
	 * 867605020276604::coolpad::qcn::1::offset
	 * 
	 * @param context
	 * @return
	 */
	public static String getQCNOffsetKey(Context context, int channelId) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_QCN);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(channelId);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_OFFSET);
		return sb.toString();
	}

	/**
	 * uid::app::language::config_scenario::q Description:
	 * 
	 * @author liyuanyi DateTime 2016年6月6日 下午10:28:06
	 * @param context
	 * @param channel
	 * @return
	 */
	public static String subChannelKey_1(Context context, String scenario) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(scenario);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_Q);
		return sb.toString();
	}

	/**
	 * 
	 * Description:
	 * 
	 * @author liyuanyi DateTime 2016年6月11日 下午4:27:08
	 * @param context
	 * @return
	 * 
	 * eg: 99000523605937::fuyiping-gionee::zh_CN::131329::q
	 */
	public static String subChannelKey(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		//sb.append(contextTurnScenario(context));
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
		boolean bVideoChannel = ContextUtils.isVideoChannel(context);
		if (bVideoChannel) {
			int channelId = ContextUtils.getChannelId(context);
			sb.append(String.valueOf(channelId));
		} else {
			sb.append(context.getScenario());
		}
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_Q);
		return sb.toString();

	}

	/**
	 * 跟CMS给的值对齐 Description:
	 * 
	 * @author liyuanyi DateTime 2016年6月11日 下午4:28:00
	 * @param context
	 * @return
	 */
	public static String contextTurnScenario_(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
//		sb.append(Constants.STR_ALL);
		sb.append(Constants.STR_SPLIT_PRE);
		boolean bVideoChannel = ContextUtils.isVideoChannel(context);
		if (bVideoChannel) {
			int channelId = ContextUtils.getChannelId(context);
			sb.append(String.valueOf(channelId));
		} else {
			sb.append(context.getScenario());
		}
		return sb.toString();
	}

	/**
	 * uid::app::channel::offset Description:
	 * 
	 * @author liyuanyi DateTime 2016年6月7日 下午3:40:06
	 * @param context
	 * @param channel
	 * @return
	 * 
	 * 		eg: 99000523605937::fuyiping-gionee::zh_CN::1::offset
	 */
	public static String subChannelOffsetKey(Context context, Integer channel) {
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
		boolean bVideoChannel = ContextUtils.isVideoChannel(context);
		if (bVideoChannel) {
			int channelId = ContextUtils.getChannelId(context);
			sb.append(String.valueOf(channelId));
		} else {
			sb.append(context.getScenario());
		}
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(Constants.CACHE_NAME_OFFSET);
		
		/*return context.getUid() + Constants.STR_SPLIT_PRE + context.getApp() + Constants.STR_SPLIT_PRE
				+ context.getLanguage() + Constants.STR_SPLIT_PRE + context.getScenario() + Constants.STR_SPLIT_PRE
				+ Constants.CACHE_NAME_OFFSET;*/
		
		return sb.toString();
	}
	
	/**
	 *   99000782076845::coolpad::detail
	 * @param context
	 * @return
	 */
	public static String getDetailKeyByUid(Context context){
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append("detail");
		return sb.toString();
	}
	
	/**
	 * 用户从初选获取的大小
	 * 99000557718159::coolpad::zh_CN::fetchsize 
	 * @param context
	 * @return
	 */
	public static String getFetchNumKeyByUid(Context context){
		StringBuffer sb = new StringBuffer();
		sb.append(context.getUid());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append("fetchsize");
		return sb.toString();
	}
	
	
	/**
	 * 获取相关推荐key-value对应的文章列表信息
	 * 99000557718159::coolpad::zh_CN::fetchsize 
	 * @param context
	 * @return
	 * relatedRankingCacheKey = context.getApp() + "#" + context.getLanguage() + "#" + contentType + "#" + relateCallTypeKey + "#" +relateCallTypeValue+"#relate-rank";
	 */
	public static String getRelatedRankingCacheKey(Context context,String relateCallTypeKey,String relateCallTypeValue){
		StringBuffer sb = new StringBuffer();
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.contentType);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(relateCallTypeKey);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(relateCallTypeValue);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append("relateRank");
		return sb.toString();
	}
	
	
	/**
	 * 获取相关推荐key-value对应的文章列表中文的详细信息
	 * coolpad::zh_CN::fetchsize 
	 * @param context
	 * @return
	 * relatedRankingCacheKey = context.getApp() + "#" + context.getLanguage() + "#" + contentType + "#" + relateCallTypeKey + "#" +relateCallTypeValue+"#relate-rank";
	 */
	public static String getRelatedNewsDetailCacheKey(Context context,String relateCallTypeKey,String relateCallTypeValue){
		StringBuffer sb = new StringBuffer();
		sb.append(context.getApp());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.getLanguage());
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(context.contentType);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(relateCallTypeKey);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append(relateCallTypeValue);
		sb.append(Constants.STR_SPLIT_PRE);
		sb.append("relatedNewsDetail");
		return sb.toString();
	}

}
