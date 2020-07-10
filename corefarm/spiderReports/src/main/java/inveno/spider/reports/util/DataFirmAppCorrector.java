package inveno.spider.reports.util;

import java.util.*;

import org.apache.log4j.Logger;
import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tw.qing.util.PrimitiveTypeUtil;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.util.JsonUtils;
import inveno.spider.reports.facade.ContentFacade;

import com.google.gson.*;

/**
 * Created by dell on 2016/5/17.
 */
public class DataFirmAppCorrector
{
	private static final Logger log = Logger.getLogger(DataFirmAppCorrector.class);

	public String mappingV4Category(HashMap hmCategoryV1ToV4, String category)
	{
		String newCategory = category;
		try
		{
			HashMap mCategory = (HashMap)JsonUtils.toJavaObject((new Gson()).fromJson(category, JsonElement.class));
			ArrayList alV1Category = (ArrayList)mCategory.get("v1");
			int nV1Category = (alV1Category == null) ? 0 : alV1Category.size();
			ArrayList alV4Category = (ArrayList)mCategory.remove("v4");
			int nV4Category = (alV4Category == null) ? 0 : alV4Category.size();

			//using mapping if v4 not exist.
			HashMap mNewCategory = new HashMap();
			ArrayList alNewV4Category = new ArrayList();
			for (Object key : mCategory.keySet())
			{
				String version = (String)key;
				ArrayList alOldCategory = (ArrayList)mCategory.get(version);
				if ("v1".equalsIgnoreCase(version))
				{
					ArrayList alNewV1Category = new ArrayList();
					for (int i = 0; i < alOldCategory.size(); i++)
					{
						int categoryId = Integer.parseInt(String.valueOf(((HashMap)alOldCategory.get(i)).get("category")));
						Integer mappingCategoryId = (Integer)hmCategoryV1ToV4.get(categoryId);
						HashMap hm = new HashMap();
						hm.put("category", categoryId);
						alNewV1Category.add(hm);
						if (mappingCategoryId == null)
						{
							log.warn("[mappingCategory] missing mapping for " + categoryId);
							mappingCategoryId = -1;
						}
						hm = new HashMap();
						hm.put("category", mappingCategoryId);
						alNewV4Category.add(hm);
					}
					mNewCategory.put("v1", alNewV1Category);
				}
				else
				{
					ArrayList alNewCategory = new ArrayList();
					for (int i = 0; i < alOldCategory.size(); i++)
					{
						int categoryId = Integer.parseInt(String.valueOf(((HashMap)alOldCategory.get(i)).get("category")));
						HashMap hm = new HashMap();
						hm.put("category", categoryId);
						alNewCategory.add(hm);
					}
					mNewCategory.put(version, alNewCategory);
				}
			}
			mNewCategory.put("v4", alNewV4Category);
			newCategory = (new GsonBuilder()).create().toJson(mNewCategory);
		}
		catch (Exception e)
		{
			log.error("[mappingV4Category] error " + category, e);
		}
		return newCategory;
	}
	public HashSet<String> listFirmMapping(int rssId)
	{
		HashSet<String> hs = new HashSet<String>();
		ArrayList alFirmApp = ContentFacade.getInstance().listFirmMapping(rssId);
		for (int i = 0; i < alFirmApp.size(); i++)
		{
			HashMap mFirmApp = (HashMap)alFirmApp.get(i);
			hs.add((String)mFirmApp.get("service_name"));
		}
		return hs;
	}
	private String firmToJsonArray(Set<String> firmApp)
	{
		JsonArray apps = new JsonArray();
		JsonObject app = new JsonObject();
		for(String str:firmApp) 
		{
			app = new JsonObject();
			app.addProperty("app", str);
			apps.add(app);
		}
		
		return apps.toString();
	}
	public void correctTurnData()
	{
		java.util.Date update_time = new java.util.Date();
		Gson gson = new Gson();
		HashMap versionCategoryMapping = ContentFacade.getInstance().getVersionCategoryMapping();
		System.out.println("versionCategoryMapping:" + versionCategoryMapping);

		Date startTime = tw.qing.util.DateUtil.stringToDate("2016-07-05 00:00:00", "yyyy-MM-dd HH:mm:ss");
		Date endTime   = new Date();
		ArrayList alContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listContentByTimeRange(startTime, endTime);
		int nTotalContent = alContent.size();
		for (int i = 0; i < nTotalContent; i++)
		{
			log.info("processing " + i + " / " + nTotalContent);
			HashMap mContent = (HashMap)alContent.get(i);
			String contentId = (String)mContent.get("content_id");
			HashMap mChannelContent = ContentFacade.getInstance().getChannelContent(Integer.parseInt(contentId));
			int rssId = Integer.parseInt(String.valueOf(mChannelContent.get("rss_id")));
			try
			{
				//correct categories
				/*
				boolean fUpdate = true;
				Map<String, Object> mNewCategories = null;
				String categories = (String)mContent.get("categories");
				String mappingCategory = mappingV4Category(versionCategoryMapping, categories);

				String title   = (String)mContent.get("title");
				String content = (String)mContent.get("content");
				String[] imageLink = ImageLinkFilter.extractImageLink(content);
				ArrayList alBodyImages = new ArrayList();
				for (int j = 0; imageLink != null && j < imageLink.length; j++)
				{
					HashMap mImage = new HashMap();
					//System.out.println(contentId + "\t" + imageLink[j]);
					int[] d = ImageLinkFilter.determineDimension(imageLink[j]);
					String format = ImageLinkFilter.determineFormat(imageLink[j]);
					mImage.put("src", imageLink[j]);
					mImage.put("width", d[0]);
					mImage.put("height", d[1]);
					mImage.put("desc", "");
					mImage.put("format", ((format == null) ? "jpg" : format));
					alBodyImages.add(mImage);
				}
				*/
				String firmApp = firmToJsonArray(listFirmMapping(rssId));
				HashMap mData = new HashMap();
				mData.put("content_id", contentId);
				mData.put("firm_app", firmApp);
				mData.put("update_time", update_time);
				ContentFacade.getInstance().updateData(mData);
				/*
				boolean fOffshelf = (!ImageLinkFilter.getInstance().accept(content));
				if (fOffshelf)
				{
					HashMap mData = new HashMap();
					mData.put("content_id", String.valueOf(contentId));
					mData.put("state", 3);
					mData.put("offshelf_code", 5);
					mData.put("offshelf_reason", "");
					mData.put("update_time", update_time);
					ContentFacade.getInstance().updateData(mData);
				}
				else
				{
					int state = Integer.parseInt( String.valueOf(mContent.get("state")) );
					int offshelf_code = Integer.parseInt( String.valueOf(mContent.get("offshelf_code")) );
					if (state == 3 && offshelf_code == 5)
					{
						HashMap mData = new HashMap();
						mData.put("content_id", String.valueOf(contentId));
						mData.put("state", 1);
						mData.put("offshelf_code", 0);
						mData.put("offshelf_reason", "");
						mData.put("update_time", update_time);
						ContentFacade.getInstance().updateData(mData);
					}
					/*
					Object[] obj = DataCorrector.determineDisplayType(title, content, alBodyImages, alBodyImages);
					int displayType = Integer.parseInt(String.valueOf(obj[0]));
					String fall_image = (String)obj[1];
					//correct display_type
					if (fUpdate)
					{
						HashMap mData = new HashMap();
						mData.put("content_id", String.valueOf(contentId));
						mData.put("display_type", displayType);
						if (fall_image != null)
							mData.put("fall_image", fall_image);
						mData.put("categories", mappingCategory);
						mData.put("update_time", update_time);
						mData.put("cp_flag", new StringBuffer("null"));
						ContentFacade.getInstance().updateData(mData);
					}
					*/
				//}
			}
			catch (Exception e)
			{
				log.error("[correctTurnData failed! content_id=" + contentId + "]", e);
			}
		}
	}
	public static void main(String[] args)
	{
		try
		{
			DataFirmAppCorrector task = new DataFirmAppCorrector();
			task.correctTurnData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}