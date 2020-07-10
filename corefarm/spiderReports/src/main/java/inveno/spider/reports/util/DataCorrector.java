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

/**
 * Created by dell on 2016/5/17.
 */
public class DataCorrector
{
	private static final Logger log = Logger.getLogger(DataCorrector.class);

	public static Object[] determineDisplayType(String title, String content, ArrayList alListImage, ArrayList alBodyImage)
	{
		int _displayType = 0x00;
		String _fallImage = null;
		try
		{
			boolean hasTitle   = (title != null && title.length() > 0);
			boolean hasContent = (content != null && content.length() > 0);
			if (hasTitle && hasContent)
			{
				_displayType |= 0x01;
				_displayType |= 0x20;
			}

			if (alBodyImage.size() >= 1)
				_displayType |= 0x02;
			if (alBodyImage.size() >= 3)
				_displayType |= 0x04;
			if (alListImage.size() >= 3)
				_displayType |= 0x10;
			if (alBodyImage.size() >= 6)
				_displayType |= 0x40;

			//判斷通欄
			for (int i = 0; i < alBodyImage.size(); i++)
			{
				HashMap mBodyImage = (HashMap)alBodyImage.get(i);
				System.out.println(i + "\t" + mBodyImage);
				String format = (String)mBodyImage.get("format");
				if (mBodyImage.get("width") != null && mBodyImage.get("height") != null)
				{
					int width  = Integer.parseInt(String.valueOf(mBodyImage.get("width")));
					int height = Integer.parseInt(String.valueOf(mBodyImage.get("height")));
					if (width >= 400 && height >= 200)
					{
						_displayType |= 0x80;
					}
					if (width >= 664 && height >= 400)
					{
						_displayType |= 0x08;
						if (_fallImage == null)
						{
							StringBuffer sb = new StringBuffer();
							sb.append((String)mBodyImage.get("src"));
							sb.append("&size=" + width + "*" + height);
							if (format != null)
							{
								sb.append("&fmt=." + format);
							}
							_fallImage = sb.toString();
						}
					}
					if (width >= 480 && height >= 320)
						_displayType |= 0x400;
				}
				boolean bGif = "gif".equalsIgnoreCase(format);
				if (bGif)
					_displayType |= 0x800;
			}
		}
		catch (Exception e)
		{
			//ignore e
		}

		return new Object[]{_displayType, _fallImage};
	}
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
			//if (nV1Category != nV4Category)
			{
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
		}
		catch (Exception e)
		{
			log.error("[mappingV4Category] error " + category, e);
		}
		return newCategory;
	}	
	public void correctTurnData(ArrayList alTurn)
	{
		java.util.Date update_time = new java.util.Date();
		Gson gson = new Gson();
		HashMap versionCategoryMapping = ContentFacade.getInstance().getVersionCategoryMapping();
		System.out.println("versionCategoryMapping:" + versionCategoryMapping);
		for (int i = 0; i < alTurn.size(); i++)
		{
			HashMap mTurnData = (HashMap)alTurn.get(i);
			int contentId = Integer.parseInt( String.valueOf(mTurnData.get("news_id")) );
			try
			{
				HashMap mChannelContent = ContentFacade.getInstance().getChannelContent(contentId);
				HashMap mContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getContent(contentId);
				String title   = (String)mContent.get("title");
				String content = (String)mContent.get("content");

				String[] imageLink = ImageLinkFilter.extractImageLink(content);
				ArrayList alBodyImages = new ArrayList();
				for (int j = 0; imageLink != null && j < imageLink.length; j++)
				{
					HashMap mImage = new HashMap();
					int[] d = ImageLinkFilter.determineDimension(imageLink[j]);
					String format = ImageLinkFilter.determineFormat(imageLink[j]);
					mImage.put("src", imageLink[j]);
					mImage.put("width", d[0]);
					mImage.put("height", d[1]);
					mImage.put("desc", "");
					mImage.put("format", ((format == null) ? "jpg" : format));
					alBodyImages.add(mImage);
				}
				//correct categories
				boolean fUpdate = true;
				Map<String, Object> mNewCategories = null;
				String categories = (String)mContent.get("categories");
				String mappingCategory = mappingV4Category(versionCategoryMapping, categories);

				if (fUpdate)
				{
					System.out.println("categoryId===> " + contentId + " categories " + categories + " --> " + mappingCategory);
				}
				//correct display_type
				if (fUpdate)
				{
					Object[] obj = determineDisplayType(title, content, alBodyImages, alBodyImages);
					int displayType = Integer.parseInt(String.valueOf(obj[0]));
					String fall_image = (String)obj[1];
					HashMap mData = new HashMap();
					mData.put("content_id", String.valueOf(contentId));
					//mData.put("body_images", gson.toJson(alBodyImages));
					//mData.put("body_images_count", alBodyImages.size());
					//mData.put("language", language);
					mData.put("display_type", displayType);
					if (fall_image != null)
						mData.put("fall_image", fall_image);
					//mData.put("categories", gson.toJson(mNewCategories));
					mData.put("update_time", update_time);
					//mData.put("cp_flag", new StringBuffer("null"));
					ContentFacade.getInstance().updateData(mData);
				}

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
			DataCorrector task = new DataCorrector();
			java.util.Calendar c = java.util.Calendar.getInstance();
			java.util.Date endTime = c.getTime();
			c.add(Calendar.DAY_OF_YEAR, -2);
			java.util.Date startTime = c.getTime();
			ArrayList alTransit = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).listTurnDataByTimeRange(startTime, endTime);
			task.correctTurnData(alTransit);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}