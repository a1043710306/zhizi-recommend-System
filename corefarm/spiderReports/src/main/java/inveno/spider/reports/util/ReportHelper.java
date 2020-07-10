package inveno.spider.reports.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ReportHelper
{
	private static final Logger log = Logger.getLogger(ReportHelper.class);

	private static String getValue(String key, HashMap mData)
	{
		if (mData != null && mData.get(key) != null)
			return (mData.get(key)).toString();
		else
			return key;
	}
	private static String getValue(String key, HashMap mData, HashMap mMeta)
	{
		if (mData != null && mData.get(key) != null)
			return (mData.get(key)).toString();
		else if (mMeta != null && mMeta.get(key) != null)
			return (mMeta.get(key)).toString();
		else
			return key;
	}
	public static String prepareReportFile(java.io.File templateFile, java.io.File reportDirectory, String reportFileName, HashMap mMeta, ArrayList<HashMap> alData) throws Exception
	{
		TemplateParser tp = new TemplateParser(templateFile);
		tp.doParse();
		HashMap tm = tp.getMap();

		StringBuffer sb = new StringBuffer();
		if (tm != null)
		{
			String[] item = null;
			String value = null;
			int n=0;
			//
			item = (String[])tm.get("HeaderPrefix");
			if (item != null)
			{
				for (int i = 0; i < item.length; i++)
				{
					sb.append( getValue(item[i], mMeta) );
				}
			}

			item = (String[])tm.get("HeaderTemplate");
			if (item != null)
			{
				for (int i = 0; i < item.length; i++)
				{
					sb.append( getValue(item[i], mMeta) );
				}
			}

			item = (String[])tm.get("DirectoryPrefix");
			if (item != null)
			{
				for (int i = 0; i < item.length; i++)
				{
					sb.append( getValue(item[i], mMeta) );
				}
			}

			item = (String[])tm.get("DirectoryTemplate");
			if (item != null)
			{
				for (int j = 0; j < alData.size(); j++)
				{
					HashMap mData = (HashMap)alData.get(j);
					for (int i = 0; i < item.length; i++)
					{
						sb.append( getValue(item[i], mData, mMeta) );
					}
				}
			}

			item = (String[])tm.get("ContentPrefix");
			if (item != null)
			{
				for (int i = 0; i < item.length; i++)
				{
					sb.append( getValue(item[i], mMeta) );
				}
			}

			item = (String[])tm.get("ContentTemplate");
			if (item != null)
			{
				for (int j = 0; j < alData.size(); j++)
				{
					HashMap mData = (HashMap)alData.get(j);
					for (int i = 0; i < item.length; i++)
					{
						sb.append( getValue(item[i], mData, mMeta) );
					}
				}
			}
			item = (String[])tm.get("ContentPostfix");
			if (item != null)
			{
				for (int i = 0; i < item.length; i++)
				{
					sb.append( getValue(item[i], mMeta) );
				}
			}
		}

		java.io.File tempFile = null;
		String html = sb.toString();
		try
		{
			tempFile = java.io.File.createTempFile(reportFileName, "");
			org.apache.commons.io.FileUtils.writeStringToFile(tempFile, html, "utf8");
			java.io.File reportFile = new java.io.File(reportDirectory, reportFileName);
			org.apache.commons.io.FileUtils.copyFile(tempFile, reportFile);
			return reportFile.getCanonicalPath();
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (tempFile != null)
				tempFile.deleteOnExit();
		}
	}
}