package com.inveno.core.service;

import com.inveno.common.bean.Context;
import com.inveno.thrift.NewsListResp;
import com.inveno.thrift.NewsListRespzhizi;

public interface ICoreService {

	public NewsListResp oldRecNewsRq(Context context);
	
	public NewsListResp oldTimelineRq(Context context);
	
	public NewsListRespzhizi infoList(Context context);
	
	public NewsListRespzhizi relatedRecommendList(Context context);
}

