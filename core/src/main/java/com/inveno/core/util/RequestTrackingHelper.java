package com.inveno.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.inveno.core.Constants;

public class RequestTrackingHelper {
	private static Logger requestTrackingLogger = Logger.getLogger("request_tracking");

	public static void logCheckPoint(long tsStart, long tsEnd, String requestId, String moduleName) {
		long tsSpent = tsEnd - tsStart;
		Object[] logParam = new Object[] { tsEnd, requestId, moduleName, tsSpent };
		String strLogData = Constants.START_LOG_TAG + StringUtils.join(logParam, Constants.LOG_TAG);
		requestTrackingLogger.info(strLogData);
	}
}
