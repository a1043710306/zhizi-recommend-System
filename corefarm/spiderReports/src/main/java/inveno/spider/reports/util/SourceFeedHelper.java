package inveno.spider.reports.util;

import java.util.*;

import org.apache.log4j.Logger;

import org.apache.commons.io.FileUtils;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.reports.facade.ContentFacade;

import tw.qing.lwdba.*;

/**
 * Created by dell on 2016/5/17.
 */
public class SourceFeedHelper
{
	private static final Logger log = Logger.getLogger(SourceFeedHelper.class);

	public void correctTurnData(List<String> alData)
	{
		for (int i = 0; i < alData.size(); i++)
		{
			String line = (String)alData.get(i);
			String[] s = line.split("\t");
			String source     = s[0];
			String sourceType = s[1];
			String category   = s[2];
			String url        = s[3];
			String channel    = s[4];

			try
			{
				ArrayList alSourceFeed = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getSourceFeeds(source, channel);
				if (alSourceFeed.size() == 1)
				{
					HashMap mSourceFeed = (HashMap)alSourceFeed.get(0);
					DBRow dr = new DBRow("t_source_feeds", new String[]{"id"});
					dr.setColumn("id", mSourceFeed.get("id"));
					dr.setColumn("source_feeds_url", url);
					dr.setColumn("source_feeds_category", category);
					String sql = dr.toUpdateString();
					ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).executeUpdate(sql);
				}
				else
				{
					System.out.println("[invalid]\t" + line);
				}
			}
			catch (Exception e)
			{
				log.fatal("[correctTurnData]", e);
			}
		}
	}
	public static void main(String[] args)
	{
		try
		{
			SourceFeedHelper task = new SourceFeedHelper();
			List<String> alData = FileUtils.readLines(new java.io.File(args[0]), "utf8");
			task.correctTurnData(alData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}