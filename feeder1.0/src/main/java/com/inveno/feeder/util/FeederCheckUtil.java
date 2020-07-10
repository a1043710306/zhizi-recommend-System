package com.inveno.feeder.util;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.inveno.feeder.model.ImagesEntry;

/**
 * 
 * 因eclipse在export第三方jar包有问题(java.io.FileNotFoundException: URL [rsrc:com/inveno/feeder/] cannot be resolved to absolute file path because it does not reside in the file system: rsrc:com/inveno/feeder/)
 * @author klausliu
 *
 */
public class FeederCheckUtil
{
	private static final Logger flowLogger = Logger.getLogger("feeder.flow");

	//读取超时  
	private final static int SOCKET_TIMEOUT = 2500;  
	
	// 连接超时  
	private final static int CONNECTION_TIMEOUT = 2000;  
	
	// 每个HOST的最大连接数量  
	private final static int MAX_CONN_PRE_HOST = 20;  
	
	// 连接池的最大连接数  
	private final static int MAX_CONN = 100;  
	
	// 连接池  
	private final static HttpConnectionManager httpConnectionManager;  
	
	//设置连接池
	static {  
		httpConnectionManager = new MultiThreadedHttpConnectionManager();  
		HttpConnectionManagerParams params = httpConnectionManager.getParams();  
		params.setConnectionTimeout(CONNECTION_TIMEOUT);  
		params.setSoTimeout(SOCKET_TIMEOUT);  
		params.setDefaultMaxConnectionsPerHost(MAX_CONN_PRE_HOST);  
		params.setMaxTotalConnections(MAX_CONN);
		params.setTcpNoDelay(true);//启用Nagle算法
		params.setLinger(1);//设置socket延迟关闭时间
		params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());//retry3次
		params.setStaleCheckingEnabled(false);//设置是否启用旧连接检查--当服务端关闭连接时有风险
	}

	private HttpClient httpClient = new HttpClient(httpConnectionManager);
	
	public Boolean checkLink(String contentId, List<ImagesEntry> imagesModels)
	{
		GetMethod method = null;
		if(imagesModels != null && imagesModels.size() > 0)
		{
			for (ImagesEntry imagesModel : imagesModels)
			{
				String src = imagesModel.getSrc();
				if (src != null && !"".equals(src.trim()))
				{
					try
					{
						method = new GetMethod(src);
						// Execute the method.
						int statusCode = httpClient.executeMethod(method);
						if (statusCode != HttpStatus.SC_OK)
						{
							flowLogger.info("contentId="+contentId+",url="+src+",return status code="+statusCode+" is invalid...");
							return false;
						}
						flowLogger.info("contentId="+contentId+",url="+src+",return status code="+statusCode);
					}
					catch (HttpException e)
					{
						flowLogger.error("Feeder checkLink Fatal protocol violation: " + e.getMessage()+"contentId="+contentId+",url="+src+" is invalid...");
						return false;
					}
					catch (IOException e)
					{
						flowLogger.error("Feeder checkLink Fatal transport error: " + e.getMessage()+"contentId="+contentId+",url="+src+" is invalid...");
						return false;
					}
					catch(Exception e)
					{
						flowLogger.error("Exception: " + e.getMessage()+"contentId="+contentId+",url="+src+" is invalid...");
						return false;
					}
					finally
					{
						if(method != null) method.releaseConnection();//断开连接
					}
				}
				else
				{
					flowLogger.info("contentId="+contentId+",url="+src+",content don't down load ...");
					return false;
				}
			}
			return true;
		}
		else
		{
			flowLogger.info("contentId="+contentId+",list images size="+imagesModels.size()+" can down load ...");
			return true;
		}
	}
}
