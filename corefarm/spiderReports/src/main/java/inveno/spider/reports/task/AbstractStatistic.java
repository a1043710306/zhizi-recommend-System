package inveno.spider.reports.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.*;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.util.JsonUtils;
import inveno.spider.common.model.Content;
import inveno.spider.reports.facade.ContentFacade;

public abstract class AbstractStatistic
{
	public static final String DATE_PATTERN                = "yyyy-MM-dd";
	public static final String NUMERIC_FORMAT              = "%,8d";

	public static final String DATETIME_PATTERN_HOURLY     = "yyyy-MM-dd HH:00:00";
	public static final String DATETIME_PATTERN_MINUTE     = "yyyy-MM-dd HH:mm:00";
	public static final String SQL_DATETIME_PATTERN_HOURLY = "%Y-%m-%d %H:00:00";
	public static final String SQL_DATETIME_PATTERN_DAILY  = "%Y-%m-%d 00:00:00";

	public static final String FIELD_VALUE_UNKNOWN = "unknown";
	public static final String LANGUAGE_ZH_CN = "zh_CN";
	private static final Logger log = Logger.getLogger(AbstractStatistic.class);

	public HashMap<String, Integer> increaseAmount(HashMap<String, Integer> mCount, String keyInfo, int increament)
	{
		int crawlerAmount = (null == mCount.get(keyInfo)) ? 0 : ((Integer)mCount.get(keyInfo)).intValue();
		crawlerAmount += increament;
		mCount.put(keyInfo, crawlerAmount);
		return mCount;
	}
	public Date convertToCSTTime(Date localTime)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(localTime);
		c.add(Calendar.HOUR, 8);
		return c.getTime();
	}
	public Date convertToUTCTime(Date localTime)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(localTime);
		c.add(Calendar.HOUR, -8);
		return c.getTime();
	}
	public String[] addOptionAll(String[] options)
	{
		HashSet<String> hsKey = new HashSet<String>(Arrays.asList(options));;
		hsKey.add("all");
		return (String[])hsKey.toArray(new String[0]);
	}
	public String[] getDisplayCategory(String jsonCategory)
	{
		HashMap mVersionCategory = (HashMap)JsonUtils.toJavaObject((new Gson()).fromJson(jsonCategory, JsonElement.class));
		ArrayList alCategory = (ArrayList)mVersionCategory.get("v4");
		if (alCategory == null || alCategory.size() <= 0)
		{
			return new String[]{"100"};
		}
		else
		{
			HashSet<String> hsCategoryId = new HashSet<String>();
			for (int j = 0; j < alCategory.size(); j++)
			{
				int categoryId = Integer.parseInt( String.valueOf(((HashMap)alCategory.get(j)).get("category")) );
				hsCategoryId.add(String.valueOf(categoryId));
			}
			return (String[])hsCategoryId.toArray(new String[0]);
		}
	}
	public HashSet<String> enumAllBodyImageCountHint()
	{
		HashSet<String> hsAllBodayImageCount = new HashSet<String>();
		hsAllBodayImageCount.add("none");
		hsAllBodayImageCount.add("single");
		hsAllBodayImageCount.add("double");
		hsAllBodayImageCount.add("multiple");
		return hsAllBodayImageCount;
	}
	public String getDisplayBodyImagesCountHint(int bodyImagesCount)
	{
		if (bodyImagesCount <= 0)
			return "none";
		else if (bodyImagesCount == 1)
			return "single";
		else if (bodyImagesCount == 2)
			return "double";
		else if (bodyImagesCount >= 3)
			return "multiple";
		else
			return "all";
	}
	public HashSet<String> enumAllContentType()
	{
		HashSet<String> hsAllContentType = new HashSet<String>();
		hsAllContentType.add("news");
		hsAllContentType.add("short_video");
		hsAllContentType.add(FIELD_VALUE_UNKNOWN);
		return hsAllContentType;
	}
	public String getDisplayContentType(int contentType)
	{
		if (contentType == ((int)(1 << Content.CONTENT_TYPE_GENERAL)))
			return "news";
		else if (contentType == ((int)(1 << Content.CONTENT_TYPE_CLIP)))
			return "short_video";
		else
			return FIELD_VALUE_UNKNOWN;
	}
	public String capitalFirstCharacter(String s)
	{
		String result = s;
		if (s != null && s.length() > 0)
		{
			StringBuffer sb = new StringBuffer();
			sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
			result = sb.toString();
		}
		if (LANGUAGE_ZH_CN.equalsIgnoreCase(result))
			result = LANGUAGE_ZH_CN;
		return result;
	}
	public HashSet<String> enumAllCategory()
	{
		HashSet<String> hsAllCategory = new HashSet<String>();
		try
		{
			int version = 4;
			ArrayList alCategory = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listCategoryByVersion(version);
			for (int i = 0; i < alCategory.size(); i++)
			{
				HashMap mCategory = (HashMap)alCategory.get(i);
				hsAllCategory.add(String.valueOf(mCategory.get("id")));
			}
		}
		catch (Exception e)
		{
		}
		return hsAllCategory;
	}
	public HashSet<String> enumAllProduct()
	{
		HashSet<String> hsAllProduct = new HashSet<String>();
		try
		{
			ArrayList alProduct = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listAllProduct();
			for (int i = 0; i < alProduct.size(); i++)
			{
				HashMap mProduct = (HashMap)alProduct.get(i);
				hsAllProduct.add((String)mProduct.get("product"));
			}
		}
		catch (Exception e)
		{
			log.error("enumAllProduct has exception:", e);
		}
		return hsAllProduct;
	}
	public String[] enumFirmApp(String firm_app)
	{
		HashSet<String> hsApp = new HashSet<String>();
		try
		{
			ArrayList alFirmApp = (firm_app == null) ? new ArrayList() : (ArrayList)inveno.spider.common.utils.JsonUtils.toJavaObject((new Gson()).fromJson(firm_app, JsonElement.class));
			if (alFirmApp.size() <= 0)
				hsApp.add(FIELD_VALUE_UNKNOWN);
			else
			{
				for (int j = 0; j < alFirmApp.size(); j++)
				{
					HashMap mFirmApp = (HashMap)alFirmApp.get(j);
					String app = (String)mFirmApp.get("app");
					if (!StringUtils.isEmpty(app))
					{
						hsApp.add(app);
					}
				}
			}
		}
		catch (Exception e)
		{
			//ignore
		}
		return (String[])hsApp.toArray(new String[0]);
	}
}