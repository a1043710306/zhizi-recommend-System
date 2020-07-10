package inveno.spider.reports.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tw.qing.lwdba.SQLUtil;
import tw.qing.util.TextUtil;

public class SourceFeedFilter
{
	private static final Logger log = Logger.getLogger(SourceFeedFilter.class);

	private String channel;
	private String name;
	private int firmId;
	private String description;
	private String[] effectiveness;
	private String[] quality;
	private int[] adult_score;
	private int[] advertisement_score;
	private int[] audience_scale;
	private ImageSizeConstraint image_constraint;

	private static HashMap<String, int[]> mDimension = new HashMap<String, int[]>();

	public class ImageSizeConstraint
	{
		private float aspectRatio;
		private int minWidth;
		private int minHeight;
		private int maxWidth;
		private int maxHeight;

		public ImageSizeConstraint(float _aspectRatio, int _minWidth, int _minHeight, int _maxWidth, int _maxHeight)
		{
			aspectRatio = _aspectRatio;
			minWidth    = _minWidth;
			minHeight   = _minHeight;
			maxWidth    = _maxWidth;
			maxHeight   = _maxHeight;
		}
		public float getAspectRatio()
		{
			return aspectRatio;
		}
		public int getMinWidth()
		{
			return minWidth;
		}
		public int getMaxWidth()
		{
			return maxWidth;
		}
		public int getMinHeight()
		{
			return minHeight;
		}
		public int getMaxHeight()
		{
			return maxHeight;
		}
	}

	public SourceFeedFilter(String _channel, String _name, int _firmId, String _description, String _effectiveness, String _quality, String _adult_score, String _advertisement_score, String _audience_scale, ImageSizeConstraint _image_constraint)
	{
		channel             = _channel;
		name                = _name;
		firmId              = _firmId;
		description         = _description;
		effectiveness       = TextUtil.getStringList(_effectiveness);
		quality             = TextUtil.getStringList(_quality);
		adult_score         = TextUtil.getIntList(_adult_score);
		advertisement_score = TextUtil.getIntList(_advertisement_score);
		audience_scale      = TextUtil.getIntList(_audience_scale);
		image_constraint    = _image_constraint;
	}
	public SourceFeedFilter(String _channel, HashMap mFilter)
	{
		channel       = _channel;
		name          = (String)mFilter.get("name");
		firmId        = Integer.parseInt(String.valueOf(mFilter.get("firm_id")));
		description   = (String)mFilter.get("description");
		effectiveness = (String[])((ArrayList)mFilter.get("effectiveness")).toArray(new String[0]);
		quality       = (String[])((ArrayList)mFilter.get("quality")).toArray(new String[0]);
		ArrayList alData = null;
		alData = (ArrayList)mFilter.get("adult_score");
		if (alData != null)
		{
			int[] arrData = new int[alData.size()];
			for (int i = 0; i < alData.size(); i++)
				arrData[i] = Integer.parseInt(String.valueOf(alData.get(i)));
			adult_score = arrData;
		}
		alData = (ArrayList)mFilter.get("advertisement_score");
		if (alData != null)
		{
			int[] arrData = new int[alData.size()];
			for (int i = 0; i < alData.size(); i++)
				arrData[i] = Integer.parseInt(String.valueOf(alData.get(i)));
			advertisement_score = arrData;
		}
		alData = (ArrayList)mFilter.get("audience_scale");
		if (alData != null)
		{
			int[] arrData = new int[alData.size()];
			for (int i = 0; i < alData.size(); i++)
				arrData[i] = Integer.parseInt(String.valueOf(alData.get(i)));
			audience_scale = arrData;
		}
		HashMap mData = (HashMap)mFilter.get("image_constraint");
		if (mData != null)
		{
			try
			{
				float aspectRatio = Float.parseFloat( String.valueOf(mData.get("aspectRatio")) );
				int minWidth    = Integer.parseInt( String.valueOf(mData.get("minWidth")) );
				int minHeight   = Integer.parseInt( String.valueOf(mData.get("minHeight")) );
				int maxWidth    = Integer.parseInt( String.valueOf(mData.get("maxWidth")) );
				int maxHeight   = Integer.parseInt( String.valueOf(mData.get("maxHeight")) );
				image_constraint = new ImageSizeConstraint(aspectRatio, minWidth, minHeight, maxWidth, maxHeight);
			}
			catch (Exception e)
			{
				log.fatal("[Parsing Image Constraint]", e);
				image_constraint = null;
			}
		}
	}
	public String getName()
	{
		return name;
	}
	public int getFirmId()
	{
		return firmId;
	}
	public String getDescription()
	{
		return description;
	}
	public HashMap toMap()
	{
		HashMap mData = new HashMap();
		mData.put("channel", channel);
		mData.put("name", name);
		mData.put("firmId", firmId);
		mData.put("description", description);
		mData.put("effectiveness", TextUtil.getString(effectiveness,"/"));
		mData.put("quality", TextUtil.getString(quality,"/"));
		StringBuffer sb = null;

		sb = new StringBuffer();
		for (int i = 0; adult_score != null && i < adult_score.length; i++)
		{
			if (i > 0) sb.append("/");
			sb.append(adult_score[i]);
		}
		mData.put("adult_score", sb.toString());

		sb = new StringBuffer();
		for (int i = 0; advertisement_score != null && i < advertisement_score.length; i++)
		{
			if (i > 0) sb.append("/");
			sb.append(advertisement_score[i]);
		}
		mData.put("advertisement_score", sb.toString());

		sb = new StringBuffer();
		for (int i = 0; audience_scale != null && i < audience_scale.length; i++)
		{
			if (i > 0) sb.append("/");
			sb.append(audience_scale[i]);
		}
		mData.put("audience_scale", sb.toString());

		return mData;
	}
	public String toClauseString(String databaseType)
	{
		ArrayList<String> alClause = new ArrayList<String>();
		if (effectiveness != null && effectiveness.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < effectiveness.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(effectiveness[i], databaseType));
			}
			alClause.add("effectiveness in (" + sb.toString() + ")");
		}
		if (quality != null && quality.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < quality.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(quality[i], databaseType));
			}
			alClause.add("quality in (" + sb.toString() + ")");
		}
		if (adult_score != null && adult_score.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < adult_score.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(adult_score[i], databaseType));
			}
			alClause.add("adult_score in (" + sb.toString() + ")");
		}
		if (advertisement_score != null && advertisement_score.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < advertisement_score.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(advertisement_score[i], databaseType));
			}
			alClause.add("advertisement_score in (" + sb.toString() + ")");
		}
		if (audience_scale != null && audience_scale.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < audience_scale.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(SQLUtil.getSQLValue(audience_scale[i], databaseType));
			}
			alClause.add("audience_scale in (" + sb.toString() + ")");
		}

		return (alClause.size() <= 0) ? null : TextUtil.getString((String[])alClause.toArray(new String[0]), " AND ");
	}
	public ArrayList applyImageConstraint(ArrayList alData)
	{
		int nData = (alData == null) ? 0 : alData.size();
		log.info(TextUtil.getString(new String[]{channel, name, description}, "-") + " input article count = " + nData);
		ArrayList alFiltered = new ArrayList();
		for (int i = 0; alData != null && i < alData.size(); i++)
		{
			HashMap mData = (HashMap) alData.get(i);
			String html_content = (String) mData.get("content");
			String imgUrl = null;
			Document doc = Jsoup.parse(html_content);
			Elements media = doc.select("[src]");
			for (Element src : media)
			{
				if (src.tagName().equals("img"))
				{
					imgUrl = src.attr("abs:src");
					break;
				}
			}
			if (imgUrl == null)
			{
				alFiltered.add(mData);
				log.info("image_url=none\tfKeep=true");
			}
			else
			{
				int idx = imgUrl.lastIndexOf("?");
				String strDimension = null;
				int w = -1;
				int h = -1;
				if (idx >= 0 && imgUrl.length() > idx && imgUrl.substring(idx).indexOf("*") >= 0)
				{
					strDimension = imgUrl.substring(idx + 1);
					try
					{
						String[] s = strDimension.split("\\*");
						w = Integer.parseInt(s[0]);
						h = Integer.parseInt(s[1]);
					} catch (Exception e)
					{
						log.fatal("", e);
						w = -1;
						h = -1;
					}
				}
				else
				{
					//need to fetch image to determine width & height
					int[] dimension = (int[]) mDimension.get(imgUrl);
					if (dimension != null)
					{
						w = dimension[0];
						h = dimension[1];
					} else
					{
						try
						{
							java.awt.image.BufferedImage image = ImageHelper.decodeImage((new java.net.URL(imgUrl)).openStream());
							w = image.getWidth();
							h = image.getHeight();
						} catch (Exception e)
						{
							log.fatal("", e);
							w = -1;
							h = -1;
						}
						mDimension.put(imgUrl, new int[]{w, h});
					}
				}

				boolean fKeep = false;
				if (image_constraint == null)
				{
					alFiltered.add(mData);
					fKeep = true;
				} else if (w > 0 && h > 0)
				{
					float aspectRatio = (float) Math.min(w, h) / (float) Math.max(w, h);
					log.info("aspectRatio=" + aspectRatio);
					if (!(aspectRatio < image_constraint.getAspectRatio() &&
							w < image_constraint.getMinWidth()))
					{
						fKeep = true;
						alFiltered.add(mData);
					}
				}
				log.info("image_url=" + imgUrl + "\t" + w + "x" + h + "\tfKeep=" + fKeep);
			}
		}
		log.info(TextUtil.getString(new String[]{channel, name, description}, "-") + " remain article count = " + alFiltered.size());
		return alFiltered;
	}
}