package inveno.spider.reports.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import inveno.spider.common.utils.HttpClientUtils;
import inveno.spider.reports.util.GenerateTableName;
import tw.qing.lwdba.DBFacade;
import tw.qing.lwdba.QueryResult;
import tw.qing.util.DateUtil;

public class ImportantSourceMonitor {

	private static final Logger logger = Logger.getLogger(ImportantSourceMonitor.class);

	public static void main(String[] args) {
		DBFacade db = null;
		try {
			db = new DBFacade("default");
			List<String> sources = findMonitorSources(db);
			if(sources != null && sources.size() > 0) {
				Map<String, Map<Integer, Long>> results = monitorSources(db, sources);
				for(String srouce : sources) {
					if(!results.containsKey(srouce)) {
						Map<Integer, Long> map = new HashMap<>();
						map.put(0, Long.valueOf(0));
						map.put(1, Long.valueOf(0));
						results.put(srouce, map);
					}
				}
				logger.debug("results:" + results);
				
				StringBuilder sb = new StringBuilder();
				for(String source: results.keySet()) {
					StringBuilder sourceStr = new StringBuilder();
					Map<Integer, Long> stateCount = results.get(source);
					Long crawlerCount = stateCount.containsKey(Integer.valueOf(0)) ? stateCount.get(Integer.valueOf(0)) : Long.valueOf(0);
					Long publishCount = stateCount.containsKey(Integer.valueOf(1)) ? stateCount.get(Integer.valueOf(1)) : Long.valueOf(0);
					sourceStr.append("source:").append(source)
							.append(",crawlerCount:").append(crawlerCount)
							.append(",publishCount:").append(publishCount)
							.append("\n");
					sb.append(sourceStr.toString());
				}
				
				//172.31.31.10:24680/message/weixin post {"phone":"shaohua.wei|chuling.lu","msg":"sadjasjdsajl"}
				HttpClientUtils.post("http://172.31.31.10:24680/message/weixin", bulidPostBody(sb.toString()), "application/json", "utf-8", null, null);
			}
		} catch (Exception e) {
			logger.error("init db has exception:", e);
		}
	}

	private static String bulidPostBody(String body) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("phone", "shaohua.wei|chuling.lu|hao.zhang|yue.qin");
		Calendar c = Calendar.getInstance();
		Date todayDate = c.getTime();
		c.add(Calendar.DAY_OF_YEAR, -1);
		Date yestodayDate = c.getTime();
		String todayDateStr = DateUtil.dateToString(yestodayDate, "yyyy-MM-dd");
		String dateStr = "date:" + todayDateStr + "\n";
		jsonObject.put("msg", dateStr + body);
		return jsonObject.toJSONString();
	}

	/**
	 * @param db
	 * @param sources
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	private static Map<String, Map<Integer, Long>> monitorSources(DBFacade db, List<String> sources) {
		Map<String, Map<Integer, Long>> sourceMonitorCountMap = new HashMap<>();
		try {
			Calendar c = Calendar.getInstance();
			Date todayDate = c.getTime();
			c.add(Calendar.DAY_OF_YEAR, -1);
			Date yestodayDate = c.getTime();
			if (TimeZone.getTimeZone("UTC").equals(TimeZone.getDefault()))
			{
				todayDate = convertToCSTTime(todayDate);
				yestodayDate = convertToCSTTime(yestodayDate);
			}
			String todayDateStr = DateUtil.dateToString(todayDate, "yyyy-MM-dd");
			String yestodayDateStr = DateUtil.dateToString(yestodayDate, "yyyy-MM-dd");
			String currentTableName = GenerateTableName.generatorTableName(false);
			String crawlerCountSqlFormat = "select count(1) as count,source,state from %s where fetch_time>='%s' and fetch_time<'%s' AND source in(%s) group by source,state";
			String crawlerCountSql = String.format(crawlerCountSqlFormat, currentTableName, yestodayDateStr, todayDateStr, buildSourceStr(sources));
			QueryResult qr = db.sqlQuery(crawlerCountSql);
			ArrayList result = qr.getRows();
			logger.debug("result count:" + result.size());
			Map<String, Map<Integer, Long>> sourceStateCountMap = new HashMap<>();
			for (int i = 0; i < result.size(); i++) {
				HashMap map = (HashMap) result.get(i);
				String source = (String) map.get("source");
				Integer state = (Integer) map.get("state");
				Long count = (Long) map.get("count");
				Map<Integer, Long> stateCountMap = new HashMap<>();
				if(sourceStateCountMap.containsKey(source)) {
					stateCountMap = sourceStateCountMap.get(source);
				}
				stateCountMap.put(state, count);
				sourceStateCountMap.put(source, stateCountMap);
				
				//
				Map<Integer, Long> stateMonitorCountMap = new HashMap<>();
				if(state == 1) {
					if(sourceMonitorCountMap.containsKey(source)) {
						stateMonitorCountMap = sourceMonitorCountMap.get(source);
						// publish count
						if(stateMonitorCountMap.containsKey(Integer.valueOf(1))) {
							stateMonitorCountMap.put(Integer.valueOf(1), stateMonitorCountMap.get(Integer.valueOf(1)) + count);
						} else {
							stateMonitorCountMap.put(Integer.valueOf(1), count);
						}
					} else {
						stateMonitorCountMap.put(Integer.valueOf(1), count);
					}
					sourceMonitorCountMap.put(source, stateMonitorCountMap);
				}
				if(sourceMonitorCountMap.containsKey(source)) {
					stateMonitorCountMap = sourceMonitorCountMap.get(source);
					// publish count
					if(stateMonitorCountMap.containsKey(Integer.valueOf(0))) {
						stateMonitorCountMap.put(Integer.valueOf(0), stateMonitorCountMap.get(Integer.valueOf(0)) + count);
					} else {
						stateMonitorCountMap.put(Integer.valueOf(0), count);
					}
				} else {
					stateMonitorCountMap.put(Integer.valueOf(0), count);
				}
				sourceMonitorCountMap.put(source, stateMonitorCountMap);
			}
			
			logger.debug("state result:" + sourceStateCountMap);
		} catch(Exception e) {
			logger.error("monitorSources has exception:", e);
		}
		
		return sourceMonitorCountMap;
	}
	
	private static String buildSourceStr(List<String> sources) {
		if(sources == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for(String source : sources) {
			sb.append("'").append(source).append("',");
		}
		return sb.substring(0, sb.length() - 1);
	}

	public static Date convertToCSTTime(Date localTime)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(localTime);
		c.add(Calendar.HOUR, 8);
		return c.getTime();
	}

	/**
	 * @param db 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private static List<String> findMonitorSources(DBFacade db) {
		List<String> sources = new ArrayList<String>();
		try {
			String findSourceSql = "select value from db_mta.t_dictionary where type_name='Important_source'";
			QueryResult qr = db.sqlQuery(findSourceSql);
			ArrayList result = qr.getRows();
			for (int i = 0; i < result.size(); i++) {
				HashMap map = (HashMap) result.get(i);
				Object descObj = map.get("value");
				if (descObj != null) {
					String desc = (String) descObj;
					if (StringUtils.isNotBlank(desc))
						sources.add(desc);
				}
			}
		} catch (Exception e) {
			logger.error("findMonitorSources has exception:", e);
		}
		return sources;
	}

}
