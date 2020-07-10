package com.inveno.core.process.last.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.inveno.common.bean.Context;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.last.ILastProcess;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.process.post.IPostProcess;
import com.inveno.core.util.SysUtil;
import com.inveno.core.Constants;
import com.inveno.thrift.ResponParam;

public class LastProcessImpl implements ILastProcess
{
	private Log logger = LogFactory.getLog(this.getClass());

	private Log logELK = LogFactory.getLog("elklog");

	@Override
	public boolean process(Context context) {

		String strRequestInfo = ContextUtils.toRequestInfoString(context);

		context.setPipeLineInvokeTimeout(false);
		
		
		logger.debug(strRequestInfo + " end LastProcessImpl.process!");
		
	
		
		return true;
	}

	private MonitorLog monitorLog;

	@Override
	public void setMonitorLog(MonitorLog monitorLog) {
		this.monitorLog = monitorLog;
	}
}
