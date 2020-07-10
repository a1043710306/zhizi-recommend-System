package inveno.spider.reports.util;

import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import org.apache.log4j.Logger;

public class ImageLinkFilter
{
	public static final Logger logger = Logger.getLogger(ImageLinkFilter.class);

	private static ImageLinkFilter instance = null;

	private ImageLinkFilter()
	{
	}

	public static synchronized ImageLinkFilter getInstance()
	{
		if (instance == null)
		{
			instance = new ImageLinkFilter();
		}
		return instance;
	}

	/**
	 * 获取图片格式
	 * @param imageLink
	 * @return
	 */
	public static String determineFormat(String imageLink)
	{
		String format = null;
		try
		{
			String query = (new java.net.URL(imageLink)).getQuery();
			if (query.indexOf("=") < 0 && query.indexOf("*") >= 0)
			{
				//imageT format, no parameter name;
				int s_idx = imageLink.lastIndexOf(".");
				int e_idx = imageLink.lastIndexOf("?");
				format = imageLink.substring(s_idx+1, e_idx);
			}
			else
			{
				//image cloud format
				String[] param = query.split("&");
				for (int j = 0; j < param.length; j++)
				{
					String[] pair = param[j].split("=");
					if (pair[0].equalsIgnoreCase("fmt"))
					{
						int idx = (pair[1].startsWith(".")) ? 1 : 0;
						format = pair[1].substring(idx);
					}
				}
			}
		}
		catch (Exception e)
		{
			//ignore e
		}
		return format;
	}

	/**
	 * 获取图片尺寸
	 * @param imageLink
	 * @return
	 */
	public static int[] determineDimension(String imageLink)
	{
		int width = -1;
		int height = -1;
		try
		{
			String query = (new java.net.URL(imageLink)).getQuery();
			if (query.indexOf("=") < 0 && query.indexOf("*") >= 0)
			{
				//imageT format, no parameter name;
				String[] d = query.split("\\*");
				width  = Integer.parseInt(d[0]);
				height = Integer.parseInt(d[1]);
			}
			else
			{
				//image cloud format
				String[] param = query.split("&");
				for (int j = 0; j < param.length; j++)
				{
					String[] pair = param[j].split("=");
					if (pair[0].equalsIgnoreCase("size"))
					{
						String[] d = pair[1].split("\\*");
						width  = Integer.parseInt(d[0]);
						height = Integer.parseInt(d[1]);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.warn("[determineDimension] " + imageLink, e);
		}
		return new int[]{width, height};
	}

	/***
	 * 提取图片链接
	 * @param src
	 * @return
	 */
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
			logger.warn("[extractImageLink]", e);
		}
		return (String[])alImageLink.toArray(new String[0]);
	}

	public boolean accept(String content)
	{
		String[] imageLink = extractImageLink(content);
		if (imageLink != null && imageLink.length > 0)
		{
			for (int i = 0; i < imageLink.length; i++)
			{
				String format   = determineFormat(imageLink[i]);
				int[] dimension = determineDimension(imageLink[i]);
				if (format == null || dimension[0] <= 0 || dimension[1] <= 0)
					return false;
			}
		}
		return true;
	}
}
