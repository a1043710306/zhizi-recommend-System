package inveno.spider.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
//for https
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//user httpclient 3.x
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
//use httpclient 4.x
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import inveno.spider.common.Constants;

@SuppressWarnings("deprecation")
public class ImageCloudHelper
{
	private static final Logger log = Logger.getLogger(ImageCloudHelper.class);

	private static ImageCloudHelper instance = null;

	public static final String HTTP_HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";

	public static synchronized ImageCloudHelper getInstance()
	{
		if (instance == null)
			instance = new ImageCloudHelper();
		return instance;
	}
	private ImageCloudHelper()
	{
	}

	private CloseableHttpClient getHttpClient()
	{
		SSLSocketFactory sf = null;
		try
		{
			X509TrustManager tm = new X509TrustManager()
			{

				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
				{
				}
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
				{
				}
				public X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}
			};
			X509HostnameVerifier verifier = new X509HostnameVerifier()
			{
				public boolean verify(String arg0, SSLSession arg1)
				{
					return false;
				}
				public void verify(String host, SSLSocket ssl) throws IOException
				{
				}
				public void verify(String host, X509Certificate cert) throws SSLException
				{
				}
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException
				{

				}
			};
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]{tm}, null);
			sf = new SSLSocketFactory(sslcontext, verifier);
		}
		catch (NoSuchAlgorithmException e)
		{
			log.fatal("[getHttpClient]", e);
		}
		catch (KeyManagementException e)
		{
			log.fatal("[getHttpClient]", e);
		}
		Scheme http  = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
		Scheme https = new Scheme("https", 443, sf);
		SchemeRegistry supportedSchemes = new SchemeRegistry();
		supportedSchemes.register(http);
		supportedSchemes.register(https);

		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(supportedSchemes);
		return new DefaultHttpClient(connectionManager);
	}
	
	@SuppressWarnings("rawtypes")
	public String uploadImageNew(String image_file_path, String app, String uid, long timestamp, HashMap mAttribute) throws Exception {
		InputStream stream1 = null;
		InputStream stream2 = null;
		ByteArrayOutputStream os = null;
		String url = URLDecoder.decode(image_file_path, "utf-8");
		String imageUrl = new URI(url, false).toString();
		String tk_src_prefix = app + ":" + uid + ":" + timestamp + ":" + Constants.IMAGE_CLOUD_ACCESS_KEY + ":";
//		String tk_src_prefix = app + ":" + uid + ":" + timestamp + ":" + "34F<S932JF;<,/SF*F56#DSfd+9fw?zF" + ":";
		os = new ByteArrayOutputStream();
		byte[] b = tk_src_prefix.getBytes();
		os.write(b, 0, b.length);
		Map<String, Object> map1 = null;
		Map<String, Object> map2 = null;
		HttpClient client = null;
		PostMethod method = null;
		byte[] tmpB = null;
		byte[] tmpA = null;
		try {
			if(image_file_path.startsWith("https")) {
				log.info("getInputStreamByUrl-----------------before");
				map1 = HttpClientUtils.getInputStreamByUrl(imageUrl);
				log.info("getInputStreamByUrl-----------------after");
				stream1 = (InputStream) map1.get("stream");
				b = IOUtils.toByteArray(stream1);
				tmpA = b;
			} else {
				b = downloadResourceLink(imageUrl);
				tmpB = b;
			}
			os.write(b, 0, b.length);
			os.flush();
			StringBuffer sb = new StringBuffer(Constants.IMAGE_CLOUD_URI_UPLOAD);
//			StringBuffer sb = new StringBuffer("http://192.168.1.235:13000/upload");
			sb.append("?");
			sb.append("app=" + app);
			sb.append("&uid=" + uid);
			sb.append("&tm=" + timestamp);
			sb.append("&tk=" + md5Hex(os.toByteArray()));
			if (mAttribute.get("resourceLink") != null)
			{
				String link = (String)mAttribute.get("resourceLink");
				String domain = (new java.net.URL(link)).getHost();
				sb.append("&src_domain=" + domain);
				sb.append("&src_image_url=" + java.net.URLEncoder.encode(link, "utf8"));
			}
			if (mAttribute.get("articleLink") != null)
			{
				String link = (String)mAttribute.get("articleLink");
				sb.append("&src_page_url=" + java.net.URLEncoder.encode(link, "utf8"));
			}
			if (mAttribute.get("contentId") != null)
			{
				String contentId = (String)mAttribute.get("contentId");
				sb.append("&info_id=" + java.net.URLEncoder.encode(contentId, "utf8"));
			}
			
			String requestImageCloudUrl = sb.toString();
			log.info("before request cloud-image imageUrl=" + image_file_path + ",requestUrl=" + requestImageCloudUrl);
			method = new PostMethod(requestImageCloudUrl);
			log.info("after request cloud-image imageUrl=" + image_file_path + ",requestUrl=" + requestImageCloudUrl);
			if(image_file_path.startsWith("https")) {
				stream2 =  new java.io.ByteArrayInputStream(tmpA);
			} else {
				stream2 =  new java.io.ByteArrayInputStream(tmpB);
			}
			log.debug("before upload image to cloud  ms,url:  " + url);
			method.setRequestEntity(new InputStreamRequestEntity(stream2));
			client = new HttpClient();
			long start = System.currentTimeMillis();
			client.executeMethod(method);
			long end = System.currentTimeMillis();
			log.debug("after upload image to cloud use time:" + (end-start) + " ms,url:" + url);
			return method.getResponseBodyAsString();
		} finally {
			if(stream2 != null) 
				stream2.close();
			if(stream1 != null) 
				stream1.close();
			if(map1 != null && map1.containsKey("get")) {
				HttpGet get = (HttpGet) map1.get("get");
				get.releaseConnection();
				org.apache.http.client.HttpClient cl = (org.apache.http.client.HttpClient) map1.get("client");
				if (image_file_path.startsWith("https") && cl != null && cl instanceof CloseableHttpClient) {
					((CloseableHttpClient) cl).close();
				}
			}
			if(map2 != null && map2.containsKey("get")) {
				HttpGet get = (HttpGet) map2.get("get");
				get.releaseConnection();
				org.apache.http.client.HttpClient cl = (org.apache.http.client.HttpClient) map2.get("client");
				if (image_file_path.startsWith("https") && cl != null && cl instanceof CloseableHttpClient) {
					((CloseableHttpClient) cl).close();
				}
			}
			if(method != null) {
				method.releaseConnection();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String[] extractImageLink(String src)
	{
		ArrayList alImageLink = new ArrayList();
		try
		{
			Parser parser = Parser.createParser(src, System.getProperty("file.encoding"));
			NodeFilter filter = new NodeClassFilter(ImageTag.class);
			NodeList list = parser.extractAllNodesThatMatch(filter);
			SimpleNodeIterator iterator = list.elements();
			while(iterator.hasMoreNodes())
			{
				//这个地方需要记住
				Node node = iterator.nextNode();
				TagNode tagNode = new TagNode();
				//一旦得到了TagNode ， 就可以得到其中的属性值
				tagNode.setText(node.toHtml());
				String imgLink = null;
				if (tagNode.getAttribute("src") != null && tagNode.getAttribute("src").length() > 0)
				{
					imgLink = tagNode.getAttribute("src");
				}
				if (imgLink != null)
					alImageLink.add( imgLink );
			}
		}
		catch (Exception e)
		{
			log.warn("[extractImageLink]", e);
		}
		return (String[])alImageLink.toArray(new String[0]);
	}

	/**
	 * upload image to cloud repository.
	 * @param app - assigned by cloud repository service, like "crawler"
	 * @param uid - unique device identity
	 * @param timestamp - current timestamp in millisecond.
	 * @param image_file_path - file canonical path for uploading image.
	 *
	 * @return server response in JSON format, or null if any exception occurs.
	 */
	@SuppressWarnings("rawtypes")
	public String uploadImage(String app, String uid, long timestamp, String image_file_path) throws Exception
	{
		return uploadImage(app, uid, timestamp, image_file_path, new HashMap());
	}
	@SuppressWarnings("rawtypes")
	public String uploadImage(String app, String uid, long timestamp, String image_file_path, HashMap mAttribute) throws Exception
	{
		File fImage = new File(image_file_path);
		String tk_src_prefix = app + ":" + uid + ":" + timestamp + ":" + Constants.IMAGE_CLOUD_ACCESS_KEY + ":";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] b = tk_src_prefix.getBytes();
		os.write(b, 0, b.length);
		b = org.apache.commons.io.FileUtils.readFileToByteArray(fImage);
		os.write(b, 0, b.length);
		os.flush();
		os.close();
		StringBuffer sb = new StringBuffer(Constants.IMAGE_CLOUD_URI_UPLOAD);
		sb.append("?");
		sb.append("app=" + app);
		sb.append("&uid=" + uid);
		sb.append("&tm=" + timestamp);
		sb.append("&tk=" + md5Hex(os.toByteArray()));
		if (mAttribute.get("resourceLink") != null)
		{
			String link = (String)mAttribute.get("resourceLink");
			String domain = (new java.net.URL(link)).getHost();
			sb.append("&src_domain=" + domain);
			sb.append("&src_image_url=" + java.net.URLEncoder.encode(link, "utf8"));
		}
		if (mAttribute.get("articleLink") != null)
		{
			String link = (String)mAttribute.get("articleLink");
			sb.append("&src_page_url=" + java.net.URLEncoder.encode(link, "utf8"));
		}
		if (mAttribute.get("contentId") != null)
		{
			String contentId = (String)mAttribute.get("contentId");
			sb.append("&info_id=" + java.net.URLEncoder.encode(contentId, "utf8"));
		}
		//System.out.println(md5Hex(os.toByteArray()));
		String url = sb.toString();
		PostMethod method = new PostMethod(url);
		method.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(fImage)));
		HttpClient client = new HttpClient();
		client.executeMethod(method);

		return method.getResponseBodyAsString();
	}
	@SuppressWarnings("rawtypes")
	public String uploadImage(String app, String uid, long timestamp, InputStream is) throws Exception
	{
		return uploadImage(app, uid, timestamp, is, new HashMap());
	}
	@SuppressWarnings("rawtypes")
	public String uploadImage(String app, String uid, long timestamp, InputStream is, HashMap mAttribute) throws Exception
	{
		String tk_src_prefix = app + ":" + uid + ":" + timestamp + ":" + Constants.IMAGE_CLOUD_ACCESS_KEY + ":";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] b = tk_src_prefix.getBytes();
		os.write(b, 0, b.length);
		b = org.apache.commons.io.IOUtils.toByteArray(is);
		os.write(b, 0, b.length);
		os.flush();
		os.close();
		StringBuffer sb = new StringBuffer(Constants.IMAGE_CLOUD_URI_UPLOAD);
		sb.append("?");
		sb.append("app=" + app);
		sb.append("&uid=" + uid);
		sb.append("&tm=" + timestamp);
		sb.append("&tk=" + md5Hex(os.toByteArray()));
		String imglink = null;
		String pagelink = null;
		if (mAttribute.get("resourceLink") != null)
		{
			imglink = (String)mAttribute.get("resourceLink");
			String domain = (new java.net.URL(imglink)).getHost();
			sb.append("&src_domain=" + domain);
			sb.append("&src_image_url=" + java.net.URLEncoder.encode(imglink, "utf8"));
		}
		if (mAttribute.get("articleLink") != null)
		{
			pagelink = (String)mAttribute.get("articleLink");
			sb.append("&src_page_url=" + java.net.URLEncoder.encode(pagelink, "utf8"));
		}
		if (mAttribute.get("contentId") != null)
		{
			String contentId = (String)mAttribute.get("contentId");
			sb.append("&info_id=" + java.net.URLEncoder.encode(contentId, "utf8"));
		}
		//System.out.println(md5Hex(os.toByteArray()));
		String url = sb.toString();
		PostMethod method = new PostMethod(url);
		method.setRequestEntity(new ByteArrayRequestEntity(b));
		HttpClient client = new HttpClient();
		client.executeMethod(method);
		String respStr = method.getResponseBodyAsString();
		log.debug("imglink:" + imglink + ",response str:" + respStr);
		return respStr;
	}
	public String uploadImageFromBase64(String articleLink, String base64Str, long timestamp, String app, String uid) throws Exception {
		if(StringUtils.isEmpty(base64Str) || StringUtils.isEmpty(articleLink)) {
			log.error("articleLink or base64Str is empty.");
			return null;
		}
		String tmpBase64Str = base64Str;
		tmpBase64Str = base64Str.replaceAll("data:image/\\w+;base64,", "");
		ByteArrayOutputStream os = null;
		String tk_src_prefix = app + ":" + uid + ":" + timestamp / 1000 + ":" + Constants.IMAGE_CLOUD_ACCESS_KEY + ":";
		os = new ByteArrayOutputStream();
		byte[] b = tk_src_prefix.getBytes();
		os.write(b, 0, b.length);
		HttpClient client = null;
		PostMethod method = null;
		try {
			byte[] base64Byte = tmpBase64Str.getBytes();
			os.write(base64Byte, 0, base64Byte.length);
			os.flush();
			
			StringBuffer sb = new StringBuffer(Constants.IMAGE_CLOUD_URI_UPLOAD);
			String domain = (new java.net.URL(articleLink)).getHost();
			sb.append("?");
			sb.append("app=" + app);
			sb.append("&uid=" + uid);
			sb.append("&src_domain=" + domain);
			sb.append("&base64=1");
			sb.append("&src_image_url=" + URLEncoder.encode(articleLink,"utf-8"));
			sb.append("&src_page_url=" + URLEncoder.encode(articleLink,"utf-8"));
			sb.append("&tm=" + timestamp / 1000);
			sb.append("&tk=" + md5Hex(os.toByteArray()));
			String requestImageCloudUrl = sb.toString();
			log.info("request cloud-image imageUrl=" + articleLink + ",requestUrl=" + requestImageCloudUrl);
			method = new PostMethod(requestImageCloudUrl);
			method.setRequestEntity(new StringRequestEntity(tmpBase64Str, null, "utf-8"));
			client = new HttpClient();
			client.executeMethod(method);
			return method.getResponseBodyAsString();
		} finally {
			if(method != null) {
				method.releaseConnection();
			}
		}
	}
	public String addFaceDetection(String app, String id) throws Exception
	{
		StringBuffer sb = new StringBuffer(Constants.IMAGE_CLOUD_URI_UPLOAD);
		sb.append("?");
		sb.append("app=" + app);
		sb.append("&id=" + id);
		String url = sb.toString();
		PostMethod method = new PostMethod(url);
		HttpClient client = new HttpClient();
		client.executeMethod(method);
		return method.getResponseBodyAsString();
	}
	public long getResourceContentLength(String url) throws Exception
	{
		long contentLength = -1;
		CloseableHttpClient httpclient = getHttpClient();
		CloseableHttpResponse response = null;
		try
		{
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("User-Agent", HTTP_HEADER_USER_AGENT);
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			contentLength = entity.getContentLength();
		}
		finally
		{
			if (response != null)
				response.close();
			httpclient.close();
		}
		return contentLength;
	}
	public byte[] downloadResourceLink(String url) throws Exception
	{
		byte[] result = null;
		long start = System.currentTimeMillis();
		CloseableHttpClient httpclient = getHttpClient();
		CloseableHttpResponse response = null;
		try
		{
			log.debug("http get downloadResourceLink ----before-----> "+url);
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("User-Agent", HTTP_HEADER_USER_AGENT);
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			entity.writeTo(os);
			os.flush();
			os.close();
			log.debug("http get downloadResourceLink ----after-----> "+url);
			result = os.toByteArray();
		}
		finally
		{
			if (response != null)
				response.close();
			httpclient.close();
		}
		long end = System.currentTimeMillis();
		log.debug("downloadResourceLink use time:" + (end-start) + " ms,url:" + url);
		
		return result;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedHashMap<String, ImageInfo> downloadResourceLink(String content, HashMap hmVariable) throws Exception
	{
		LinkedHashMap<String, ImageInfo> mResource = new LinkedHashMap<String, ImageInfo>();

		String app = (String)hmVariable.get("app");
		String uid = (String)hmVariable.get("uid");
		long timestamp = (long)(System.currentTimeMillis()/1000);
		String[] img_src = extractImageLink( content );
		for (int i = 0; img_src != null && i < img_src.length; i++)
		{
			if (img_src[i].startsWith(inveno.spider.common.Constants.IMAGE_CLOUD_URI_ICON))
			{
				ImageInfo img = ImageInfo.fromUrl(img_src[i]);
				if (img != null)
				{
					mResource.put(img_src[i], img);
					continue;
				}
			}

			log.debug("[downloadResourceLink] " + img_src[i]);
			try
			{
				HashMap mAttribute = new HashMap();
				mAttribute.put("contentId", hmVariable.get("contentId"));
				mAttribute.put("articleLink", hmVariable.get("articleLink"));
				mAttribute.put("resourceLink", img_src[i]);
				log.debug("test uploadImageNew before ------------");
				String json = uploadImageNew(img_src[i],app, uid, timestamp, mAttribute);
				log.debug("test uploadImageNew after ------------");
				ImageInfo img = new ImageInfo(json);
				mResource.put(img_src[i], img);
			}
			catch (Exception e)
			{
				log.error("upload image has exception,link:" + img_src[i] + ",e:" + e);
				continue;
			}
		}
		return mResource;
	}
	private static String md5Hex(byte[] data)
	{
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
	}
	
}
