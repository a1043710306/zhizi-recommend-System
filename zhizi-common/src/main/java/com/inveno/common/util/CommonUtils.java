package com.inveno.common.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class CommonUtils {
	
	/**
	 * 
	 *  Description:
	 *  拼凑infoId字段去布隆过滤器查询
	 *  @author liyuanyi  DateTime 2016年1月21日 上午10:10:01
	 *  @param uuid
	 *  @param firmName
	 *  @param infoId
	 *  @return
	 */
	public static String spliceStr(String uuid,String firmName,String infoId)
	{
		return uuid+firmName+infoId;
	}
	
	
	public static String doPost(String url, Map<String, String> params) {
		CloseableHttpClient httpclient  = null;
		try {
			httpclient = HttpClients.createDefault();
  			HttpPost post = new HttpPost(url);
  			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(500).setConnectTimeout(500).build();//设置请求和传输超时时间
			post.setConfig(requestConfig);
			
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			
			for (Map.Entry<String, String> entry : params.entrySet()) {
				NameValuePair nvp = new BasicNameValuePair(entry.getKey(), entry.getValue());
				nvps.add(nvp);
			}
			post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

			CloseableHttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				return EntityUtils.toString(entity,"utf-8");
			}
			return "字符串为空";
		} catch (Exception e) {
			return "字符串为空";
		}finally{
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param json
	 * @return
	 */
	public static String doPost(String url, String json) {
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpPost post = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(500).setConnectTimeout(500).build();//设置请求和传输超时时间
			post.setConfig(requestConfig);
			
			StringEntity s  = new StringEntity(json.toString(),Charset.forName("utf-8"));
			s.setContentType("application/json; charset=UTF-8");
			s.setContentEncoding("utf-8");
			//s.setContentEncoding("UTF-8");
//			s.setContentType("application/json");
			post.setEntity(s);
			
			
  			CloseableHttpResponse response = httpclient.execute(post);
  			
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
			
			
 			return "字符串为空";
		} catch (Exception e) {
			return "字符串为空";
		}finally{
			try {
				httpclient.close();
			} catch (IOException e) {
 				e.printStackTrace();
			}
 		}
	}

}
