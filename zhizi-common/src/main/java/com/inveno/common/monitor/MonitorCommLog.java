package com.inveno.common.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.enumtype.InterfaceType;
import com.inveno.common.enumtype.MonitorType;
import com.inveno.common.util.ContextUtils;
import com.inveno.common.util.SysUtil;

@Component("monitorCommLog")
public class MonitorCommLog {
	
	public static Log logger = LogFactory.getLog(MonitorCommLog.class);
	
	public static Log monitorLog = LogFactory.getLog("monitorlog");
	
	public static String hostname = "192.168.1.10";
	
	//private static boolean useIPv4 = true;
	
	public final static String PRE="&&";
	
	
	static {
//		String prefix = Constants.monitorIpPrefix;
//		try {
//			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//			for (NetworkInterface intf : interfaces) {
//				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//				for (InetAddress addr : addrs) {
//					if (addr.isSiteLocalAddress() && !addr.isLoopbackAddress()
//							&& addr.getHostAddress().indexOf(":") == -1) {
//						String sAddr = addr.getHostAddress();
//						// boolean isIPv4 =
//						// InetAddressUtils.isIPv4Address(sAddr);
//						boolean isIPv4 = sAddr.indexOf(':') < 0;
//
//						if (isIPv4 && sAddr.startsWith(prefix)){
//							hostname = sAddr;
//						}
//					}
//				}
//			}
//		} catch (Exception ex) {
//		}
//		
//		if( StringUtils.isNotEmpty(Constants.MONITOR_IP)){
//			hostname = Constants.MONITOR_IP;
//		}
		try {
		hostname = SysUtil.getIPAddress(true);
		} catch (Exception ex) {
			logger.error("MonitorCommLog--static", ex);
		}
	}
	
	public MonitorCommLog(){}
	
//	public MonitorCommLog(String loginfo){
//		 monitorLog = LogFactory.getLog(loginfo);
//	}
	
	/**
	 * 
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月21日 上午11:30:29
	 *  @param logType
	 *  @param context
	 *  @param requestType
	 */
	public void addMonitorLogByConfig(String logType, String requestType, String abtest) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + 1 + PRE + 0 + PRE + "ORIGINAL" + PRE + 60 + PRE + "request="+ requestType + ",abtest=" + abtest);
	}
	
	public void addMonitorLogByProduct(String logType, String requestType, String app) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + 1 + PRE + 0 + PRE + "ORIGINAL" + PRE + 60 + PRE + "request="+ requestType + ",app=" + app);
	}
	
	public void addCountMonitorLogByProduct(String logType, String requestType, String app,int count) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + count + PRE + 0 + PRE + "ORIGINAL" + PRE + 60 + PRE + "request="+ requestType + ",app=" + app);
	}
	
	public void addMonitorLogByProductAndScenario(String logType, String requestType, String app, String scenario, String language) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + 1 + PRE + 0 + PRE + "ORIGINAL" + PRE + 60 + PRE + "request="+ requestType + ",app=" + app+",scenario="+scenario+",language="+language);
	}
	
	
	public void addMonitorLogResTimeByConfig(String logType, String requestType, String abtest, long timeMi) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + timeMi + PRE + 1 + PRE + "AVG" + PRE + 60 + PRE + "request="+ requestType + ",abtest=" + abtest);
	}
	
	public void addMonitorLogResTimeByProduct(String logType, String requestType, String productName, long usedTime) {
		// 2016-04-18 15:00:04||192.168.1.228&&getlogic.q.callcount
		// &&1000&&0&&ORIGINAL&&60&&qtype=https, callee=algorithm
		monitorLog.info(hostname + PRE + logType + PRE + usedTime + PRE + 1 + PRE + "AVG" + PRE + 60 + PRE + "request="+ requestType + ",app=" + productName);
	}
	
	
	
	/**
	 * 次数 可以一次性写入的
	 * 增加监控日志:以product做区分
	 * @param context
	 * @param monitorType
	 */
	public void addCntCountLogByProduct(Context context, MonitorType monitorType,int count) {
		try {
			if (null != context.getZhiziListReq()) {
				if (ContextUtils.isForYouChannel(context)) {
					addCountMonitorLogByProduct(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getApp(),count);
				} else {
					addCountMonitorLogByProduct(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getApp(),count);
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addCountMonitorLogByProduct(monitorType.getType(), InterfaceType.QB.getType(), context.getApp(),count);
			} else if (context.getScenario() > 0) {
				addCountMonitorLogByProduct(monitorType.getType(), InterfaceType.QCN.getType(), context.getApp(),count);
			} else {
				addCountMonitorLogByProduct(monitorType.getType(), InterfaceType.Q.getType(), context.getApp(),count);
			}
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}
	
	/**
	 * 次数
	 * 增加监控日志:以product做区分
	 * @param context
	 * @param monitorType
	 */
	public void addCntLogByProduct(Context context, MonitorType monitorType) {
		try {
			if (null != context.getZhiziListReq()) {
				if (ContextUtils.isForYouChannel(context)) {
					addMonitorLogByProduct(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getApp());
				} else {
					addMonitorLogByProduct(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getApp());
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addMonitorLogByProduct(monitorType.getType(), InterfaceType.QB.getType(), context.getApp());
			} else if (context.getScenario() > 0) {
				addMonitorLogByProduct(monitorType.getType(), InterfaceType.QCN.getType(), context.getApp());
			} else {
				addMonitorLogByProduct(monitorType.getType(), InterfaceType.Q.getType(), context.getApp());
			}
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}
	
	/**
	 * 次数
	 * 增加监控日志:以product和Sernario做区分
	 * @param context
	 * @param monitorType
	 */
	public void addCntLogByProductAndScenario(Context context, MonitorType monitorType) {
		try {
			if( null != context.getZhiziListReq() ){
				if (ContextUtils.isForYouChannel(context)) {
					addMonitorLogByProductAndScenario(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getApp(), String.valueOf(context.getScenario()), context.getLanguage());
				} else {
					addMonitorLogByProductAndScenario(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getApp(), String.valueOf(context.getScenario()), context.getLanguage());
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addMonitorLogByProductAndScenario(monitorType.getType(), InterfaceType.QB.getType(), context.getApp(), "0-qb", context.getLanguage());
			} else if (context.getScenario() > 0) {
				addMonitorLogByProductAndScenario(monitorType.getType(), InterfaceType.QCN.getType(), context.getApp(), String.valueOf(context.getScenario()), context.getLanguage());
			} else {
				addMonitorLogByProductAndScenario(monitorType.getType(), InterfaceType.Q.getType(), context.getApp(), "0", context.getLanguage());
			}
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}
	
	/**
	 * 响应时间
	 * 增加监控日志:以product做区分
	 * @param context
	 * @param monitorType
	 */
	public void addResTimeLogByProduct(Context context, MonitorType monitorType, long time) {
		try {
			if( null != context.getZhiziListReq()){
				if (ContextUtils.isForYouChannel(context)) {
					addMonitorLogResTimeByProduct(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getApp(), time);
				} else {
					addMonitorLogResTimeByProduct(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getApp(), time);
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addMonitorLogResTimeByProduct(monitorType.getType(), InterfaceType.QB.getType(), context.getApp(), time);
			} else if (context.getScenario() > 0) {
				addMonitorLogResTimeByProduct(monitorType.getType(), InterfaceType.QCN.getType(), context.getApp(), time);
			} else {
				addMonitorLogResTimeByProduct(monitorType.getType(), InterfaceType.Q.getType(), context.getApp(),time);
			}
			
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}
	
	/**
	 * 新的scenario接口
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月22日 上午11:37:48
	 *  @param context
	 */
	public void addResTimeLogByConfigId(Context context, MonitorType monitorType, long time) {
		try {
			if( null != context.getZhiziListReq() ){
				if (ContextUtils.isForYouChannel(context)) {
					addMonitorLogResTimeByConfig(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getAbtestVersion(), time);
				} else {
					addMonitorLogResTimeByConfig(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getAbtestVersion(), time);
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addMonitorLogResTimeByConfig(monitorType.getType(), InterfaceType.QB.getType(), context.getAbtestVersion(), time);
			} else if (context.getScenario() > 0) {
				addMonitorLogResTimeByConfig(monitorType.getType(), InterfaceType.QCN.getType(), context.getAbtestVersion(), time);
			} else {
				addMonitorLogResTimeByConfig(monitorType.getType(), InterfaceType.Q.getType(), context.getAbtestVersion(),time);
			}
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}

	
	/**
	 * 老接口的日志监控
	 *  Description:
	 *  @author liyuanyi  DateTime 2016年6月22日 下午2:12:07
	 *  @param context
	 *  @param monitorType
	 */
	public void addCntMonitorLogByConfigId(Context context, MonitorType monitorType) {
		try {
			if( null != context.getZhiziListReq() ){
				if (ContextUtils.isForYouChannel(context)) {
					addMonitorLogByConfig(monitorType.getType(), InterfaceType.FORYOU.getType(), context.getAbtestVersion());
				} else {
					addMonitorLogByConfig(monitorType.getType(), InterfaceType.SCENARIO.getType(), context.getAbtestVersion());
				}
			} else if (context.getRecNewsListReq() != null && context.getRecNewsListReq().isNeedBanner()) {
				addMonitorLogByConfig(monitorType.getType(), InterfaceType.QB.getType(), context.getAbtestVersion());
			} else if (context.getScenario() > 0) {
				addMonitorLogByConfig(monitorType.getType(), InterfaceType.QCN.getType(), context.getAbtestVersion());
			} else {
				addMonitorLogByConfig(monitorType.getType(), InterfaceType.Q.getType(), context.getAbtestVersion());
			}
		} catch (Exception e) {
			logger.error("monitor Log happen error", e);
		}
	}
}
