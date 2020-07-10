package inveno.spider.common;

import java.io.InputStream;
import java.util.Map;

//user httpclient 3.x
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import inveno.spider.common.utils.HttpClientUtils;

public class PHashTest
{
	
	public static String uploadImage(String url) throws Exception
	{
		String pHashUrl = "http://localhost:8555";
		PostMethod method = new PostMethod(pHashUrl);
//		Map<String, Object> map = HttpClientUtils.getInputStreamByUrl(url);
		
//		method.setRequestEntity(new InputStreamRequestEntity((InputStream)map.get("stream")));
		HttpClient client = new HttpClient();
		client.executeMethod(method);
		String respStr = method.getResponseBodyAsString();
		return respStr;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(uploadImage("http://cloudimg.hotoday.in/v1/icon?id=-3208449242368061582&size=500*706&fmt=.jpeg"));
	}
	
}
