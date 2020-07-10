package com.inveno.core.process.gmp.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.inveno.common.bean.Context;
import com.inveno.common.bean.RecommendInfoData;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.init.PolicyAbstract;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.PrimarySelectionFilteredInterface;
import com.inveno.thrift.ResponParam;

@Component("primarySelectionFiltered")
public class PrimarySelectionFilteredImpl  extends PolicyAbstract<List<ResponParam>> {
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Autowired
	private  PrimarySelectionFilteredInterface <RecommendInfoData> dubbPrimarySelectionFiltered;
	
	@Autowired
	private MonitorLog monitorLog;
	
	@Override
	public List<ResponParam> process(Context context) throws TException {
		long tsStart = System.currentTimeMillis(), tsEnd;

		boolean bInvokedFailed = false;
		String strRequestInfo = ContextUtils.toRequestInfoString(context);
		RecommendInfoData recommendInfoData = null;
		try {
			if (logger.isTraceEnabled()) {
				logger.trace(strRequestInfo + ", begin invoke PrimarySelectionFilteredImpl context " + JSON.toJSONString(context));
			}

			logger.info(strRequestInfo + ", begin invoke PrimarySelectionFilteredImpl");
			monitorLog.addCntLogByProduct(context, MonitorType.GMP_INVOKE_COUNT);
			recommendInfoData = dubbPrimarySelectionFiltered.process(context);
			
			context.setRecommendInfoData(recommendInfoData);
		} catch (Exception e) {
			monitorLog.addCntLogByProduct(context, MonitorType.GMP_FAIL_COUNT);
			logger.info(strRequestInfo + ", invoke PrimarySelectionFilteredImpl failed", e);
			recommendInfoData = null;
			bInvokedFailed = true;
		} finally {
			int nDataCount = (recommendInfoData == null || recommendInfoData.getListResponRaram() == null) ? 0 : recommendInfoData.getListResponRaram().size();
			tsEnd = System.currentTimeMillis();
			long tsSpent = tsEnd - tsStart;
			monitorLog.addResTimeLogByConfigId(context, MonitorType.GMP_RESPONSE_TIME, tsSpent);
			monitorLog.addResTimeLogByProduct(context, MonitorType.GMP_RESPONSE_TIME, tsSpent);
			if (!bInvokedFailed && nDataCount <= 0) {
				monitorLog.addCntLogByProduct(context, MonitorType.GMP_EMPTY_LIST_COUNT);
			}
			logger.info(strRequestInfo + ", end invoke PrimarySelectionFilteredImpl, info list size is " + nDataCount + " spend time:" + tsSpent + " ms ");
		}

		if (recommendInfoData == null)
			return Collections.emptyList();
		else
			return recommendInfoData.getListResponRaram();
	}
}
