package inveno.spider.reports.util;

import java.io.IOException;
import java.util.*;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.sys.JsonStringManager;
import inveno.spider.reports.facade.ContentFacade;
import inveno.spider.reports.facade.DashboardFacade;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.reports.util.SourceFeedFilter;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.google.gson.*;

import tw.qing.util.DateUtil;
import tw.qing.util.TextUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.sys.StringManager;

//use httpclient 4.x
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by Genix.Li on 2016/08/06.
 *
 * Statistic published amount for each channel(firm_app)
 */
public class ElasticSearchHelper
{
	private static final Logger log = Logger.getLogger(ElasticSearchHelper.class);

	public static final String DATE_PATTERN_DAILY      = "yyyy-MM-dd 00:00:00";
	public static final String DATE_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATE_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";

	public static String ELASTICSEARCH_QUERY_URI = null;
	public static String ELASTICSEARCH_FIELD_CATEGORY = null;
	public static String ELASTICSEARCH_FIELD_LANGUAGE = null;

	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
			ELASTICSEARCH_QUERY_URI      = smgr.getString("elasticsearch.restful.uri");
			ELASTICSEARCH_FIELD_CATEGORY = smgr.getString("elasticsearch.field.category");
			ELASTICSEARCH_FIELD_LANGUAGE = smgr.getString("elasticsearch.field.language");
		}
		catch (Exception e)
		{
			log.fatal("[Exit]", e);
			System.exit(-1);
		}
	}
	public ElasticSearchHelper()
	{
	}
	private int getArticleCount(String[] arrContentId)
	{
		int nArticle = 0;
		String query = prepareQuery(arrContentId);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try
		{
			HttpPost post = new HttpPost(ELASTICSEARCH_QUERY_URI);
			post.setEntity(new org.apache.http.entity.StringEntity(query));
			response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			entity.writeTo(os);
			EntityUtils.consume(entity);
			os.flush();
			os.close();
			String json = os.toString();
			HashMap mResult = (HashMap)inveno.spider.common.utils.JsonUtils.toJavaObject((new Gson()).fromJson(json, JsonElement.class));
			nArticle = Integer.parseInt(String.valueOf(((HashMap)mResult.get("hits")).get("total")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (response != null)
			{
				try
				{
					response.close();
				}
				catch (IOException ioe)
				{
					//ignore
				}
			}
		}
		finally
		{
			try
			{
				httpclient.close();
			}
			catch (IOException ioe)
			{
				//ignore
			}
		}
		log.debug("query=" + query + "\tresult=" + nArticle);
		return nArticle;
	}
	private String prepareQuery(String[] arrContentId)
	{
		ArrayList alShould = new ArrayList();
		HashMap mQueryString, mFieldCondition;
		for (int i = 0; i < arrContentId.length; i++)
		{
			mQueryString = new HashMap();
			mFieldCondition = new HashMap();
			mFieldCondition.put("content_id", arrContentId[i]);
			mQueryString.put("term", mFieldCondition);
			alShould.add(mQueryString);
		}
		HashMap mQuery = new HashMap();
		HashMap mBoolean = new HashMap();
		HashMap mCriteria = new HashMap();
		mCriteria.put("should", alShould);
		mBoolean.put("bool", mCriteria);
		mQuery.put("filter", mBoolean);
		mQuery.put("from", 0);
		mQuery.put("size", 0);
		return (new Gson()).toJson(mQuery);
	}
	public void doSearch(String filename) throws Exception
	{
		List alContentId = org.apache.commons.io.FileUtils.readLines(new java.io.File(filename), "utf8");
		ArrayList<String[]> alQueryGroup = new ArrayList<String[]>();
		ArrayList alTemp = new ArrayList();
		for (int i = 0; i < alContentId.size(); i++)
		{
			alTemp.add( alContentId.get(i) );
			if (alTemp.size() >= 300)
			{
				alQueryGroup.add( (String[])alTemp.toArray(new String[0]) );
				alTemp = new ArrayList();
			}
		}
		if (alTemp.size() > 0)
		{
			alQueryGroup.add( (String[])alTemp.toArray(new String[0]) );
		}
		for (int j = 0; j < alQueryGroup.size(); j++)
		{
			String[] arrContentId = (String[])alQueryGroup.get(j);
			int nArticle = getArticleCount(arrContentId);
			if (nArticle > 0)
			{
				System.out.println(nArticle + " exists in (" + TextUtil.getString(arrContentId) + ")");
			}
		}
	}
	public static void main(String[] args)
	{
		try
		{
			ElasticSearchHelper helper = new ElasticSearchHelper();
			helper.doSearch(args[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
