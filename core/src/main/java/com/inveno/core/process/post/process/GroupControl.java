package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

@Component("groupControl")
public class GroupControl implements IPostPolicy<List<ResponParam>>{
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Override
	public List<ResponParam> process(Context context) throws TException {
		boolean bForYouChannel = ContextUtils.isForYouChannel(context);
		if (context.getZhiziListReq() != null && !bForYouChannel) {
			return null;
		}
		
		if (context.getTimelineNewsListReq() != null && context.getScenario() > 0) {
			return null;
		}
		

		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();

		if(logger.isDebugEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " begin GroupControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis());
		}

		if( context.getResponseParamList().size() < 5 ){
			return null;
		}
		
		int groupIdControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "groupIdControlwindowSize"), 8);

		if (logger.isDebugEnabled()) {
			logger.debug(" uid: " + uid + " ,app " + app + " begin GroupControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , before list " + context.getResponseParamList());
		}
		
		List<ResponParam> reList = reRank(context.getResponseParamList(), groupIdControlwindowSize);
		context.setResponseParamList(reList);
		if (logger.isDebugEnabled()) {
			logger.debug(" uid: " + uid + " ,app " + app + " end GroupControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , after list " + reList);
		}
		
		long end  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end GroupControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		return null;
	}
	
	public static List<ResponParam> reRank(List <ResponParam> inlist, int windowSize) {
		ArrayList<ResponParam> list = (ArrayList<ResponParam>)inlist;
		@SuppressWarnings("unchecked")
		ArrayList<ResponParam> result = (ArrayList<ResponParam>) list.clone();
		
		int len = result.size();
		
		HashSet<String> tagsInWindow = new HashSet<String>();
		//end = Math.min(end, len);
		
		for (int i = 0; i < result.size(); i++) {
			String groupId =  result.get(i).getGroupId();
			if (i >= windowSize) {
				tagsInWindow.remove(result.get(i - windowSize).getGroupId());
			}
			
			if ("-1".equals(groupId) || StringUtils.isEmpty(groupId)) {
				continue;
			}
			boolean isContains = false;
			for (int j = i; j < len; j++) {
				String groupIdJ =  result.get(j).getGroupId();
				isContains = tagsInWindow.contains(groupIdJ);
				if (!isContains) {
					if (i != j) { // 需要移动
						ResponParam backwardItem = result.get(j); // 前移的item
						result.remove(backwardItem);
						result.add(i, backwardItem);
					}
					break;
				}
			}
			if (isContains) {
				// report cnt++
			}
			
			if (!"-1".equals(groupId) && !StringUtils.isEmpty(groupId)) {
				tagsInWindow.add(result.get(i).getGroupId());
			}
		}
		return result;
	}
}
