package inveno.spider.reports.task;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.sys.JsonStringManager;
import inveno.spider.reports.facade.ReportFacade;
import inveno.spider.reports.util.ReportHelper;
import inveno.spider.reports.util.SourceFeedFilter;
import inveno.spider.common.mail.MailSender;

import tw.qing.sys.StringManager;
import tw.qing.util.DateUtil;
import tw.qing.util.PrimitiveTypeUtil;
import tw.qing.util.TextUtil;

import java.util.*;


/**
 * Created by Genix.Li on 2016/05/06.
 */
public class DailySourceFilterPublishStatistic extends AbstractStatistic
{
	private static final Logger log = Logger.getLogger(DailySourceFilterPublishStatistic.class);

	private static String SMTP_HOST;
	private static int    SMTP_PORT;
	private static boolean SMTP_TLS;
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;

	private String strStatisticTime;
	private String strArticleFetchDate;
	private java.io.File fReportDirectory;

	static
	{
		try
		{
			StringManager smgr = StringManager.getManager("system");
			SMTP_HOST     = System.getProperty("smtp.host", smgr.getString("smtp.host"));
			SMTP_PORT     = Integer.parseInt(System.getProperty("smtp.port", smgr.getString("smtp.port")));
			SMTP_TLS      = Boolean.valueOf(System.getProperty("smtp.tls", smgr.getString("smtp.tls"))).booleanValue();
			SMTP_USERNAME = System.getProperty("smtp.username", smgr.getString("smtp.username"));
			SMTP_PASSWORD = System.getProperty("smtp.password", smgr.getString("smtp.password"));
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}

	public DailySourceFilterPublishStatistic(String _strArticleFetchDate)
	{
		Calendar c = Calendar.getInstance();
		strStatisticTime    = DateUtil.dateToString(c.getTime(), DATETIME_PATTERN_MINUTE);
		strArticleFetchDate = _strArticleFetchDate;
		fReportDirectory = new java.io.File("reports" + java.io.File.separator + strArticleFetchDate);
		if (!fReportDirectory.exists())
			fReportDirectory.mkdirs();
	}

	public ArrayList<Object> generateMailAttach(String attachTemplateFileName, String detailTemplateFileName, String source) throws Exception
	{
		JsonStringManager jsmgr = JsonStringManager.getManager("filter");
		String prefix  = "sourcefilter";

		HashMap mImageConstraint = new HashMap();
		for (Map.Entry<Object, Object> entry : ((HashMap<Object, Object>)jsmgr.getObject("image_constraint.overall")).entrySet())
		{
			String key = (String)entry.getKey();
			int idx = key.lastIndexOf(".");
			if (idx >= 0) key = key.substring(idx + 1);
			mImageConstraint.put(key, entry.getValue());
		}

		StringBuffer sb = new StringBuffer();
		for (String json_key : jsmgr.getKeys())
		{
			HashMap mMeta    = new HashMap();
			ArrayList alData = new ArrayList();

			if (!json_key.startsWith(prefix))
				continue;
			String channel = json_key.substring(prefix.length()+1);
			ArrayList alFilter = (ArrayList)jsmgr.getObject(json_key);
			for (int i = 0; alFilter != null && i < alFilter.size(); i++)
			{
				HashMap mFilter = (HashMap)alFilter.get(i);
				mFilter.put("image_constraint", mImageConstraint);
				SourceFeedFilter filter = new SourceFeedFilter(channel, mFilter);
				HashMap<String, HashMap<String, String>> mSource = ReportFacade.getInstance().getRssInfoBySourceFilter(source, filter);
				String[] arrRssId = (String[])mSource.keySet().toArray(new String[0]);

				ArrayList alPublishedArticle = ReportFacade.getInstance().getDailyPublishedArticle(arrRssId, strArticleFetchDate);
				ArrayList alCandidateArticle = ReportFacade.getInstance().getDailyCandidateArticle(arrRssId, strArticleFetchDate);
				ArrayList alArticle = new ArrayList();
				if (alPublishedArticle != null) alArticle.addAll( alPublishedArticle );
				if (alCandidateArticle != null) alArticle.addAll( alCandidateArticle );
				int nTotalBizCount      = (arrRssId == null)  ? 0 : arrRssId.length;
				int nTotalArticleCount  = (alArticle == null) ? 0 : alArticle.size();
				int nNonPublishArticleCount = 0;
				int nFilteredArticleCount   = 0;
				int nPublishArticleCount    = 0;

				HashSet hsActiveSourceFeeds = new HashSet();

				HashMap<String, HashMap> mRssRelease = ReportFacade.getInstance().getRssReleaseFlag(arrRssId, filter.getFirmId());
				for (int j = 0; alArticle != null && j < alArticle.size(); j++)
				{
					HashMap mArticle = ((HashMap)alArticle.get(j));
					String source_feeds = String.valueOf(mArticle.get("rss_id"));
					hsActiveSourceFeeds.add( source_feeds );

					boolean fChannelRelease = false;
					HashMap mRssFlag = (HashMap)mRssRelease.get(source_feeds);
					if (mRssFlag == null)
					{
						log.warn("there's no corresponding releaseFlag for " + source_feeds);
					}
					else
					{
						int releaseFlag = PrimitiveTypeUtil.getInt( mRssFlag.get("releaseFlag") );
						int status = (mRssFlag.get("status") == null) ? 0 : PrimitiveTypeUtil.getInt( mRssFlag.get("status") );
						//設置且開啟渠道自動發佈
						fChannelRelease = (releaseFlag == 1 && status == 1);
					}

					if (fChannelRelease && mArticle.get("filtered") == null)
					{
						//自動發佈成功
						nPublishArticleCount++;
					}
					else if (fChannelRelease && mArticle.get("filtered") != null)
					{
						//自動發佈被過濾
						nFilteredArticleCount++;
					}
					else
					{
						nNonPublishArticleCount++;
					}
				}
				int nActiveSourceFeedsCount = hsActiveSourceFeeds.size();
				HashMap mData = filter.toMap();
				mData.put("totalBizCount", new Integer(nTotalBizCount));
				mData.put("activeBizCount", new Integer(nActiveSourceFeedsCount));
				mData.put("totalArticleCount", new Integer(nTotalArticleCount));
				mData.put("nonPublishArticleCount", new Integer(nNonPublishArticleCount));
				mData.put("filteredArticleCount", new Integer(nFilteredArticleCount));
				mData.put("publishArticleCount", new Integer(nPublishArticleCount));
				alData.add( mData );
			}

			String reportFileName = "statistic_channel_" + strArticleFetchDate + "_" + channel + ".html";
			String filename = ReportHelper.prepareReportFile(new java.io.File(detailTemplateFileName), fReportDirectory, reportFileName, mMeta, alData);
			sb.append( org.apache.commons.io.FileUtils.readFileToString(new java.io.File(filename), "utf8") );
		}

		HashMap mMeta    = new HashMap();
		ArrayList alData = new ArrayList();
		mMeta.put("content", sb.toString());
		ArrayList<Object> alAttach = new ArrayList<Object>();
		String reportFileName = "statistic_detail_" + strArticleFetchDate + ".html";
		String attach_filename = ReportHelper.prepareReportFile(new java.io.File(attachTemplateFileName), fReportDirectory, reportFileName, mMeta, alData);
		log.info("attach_filename=" + attach_filename);
		alAttach.add( attach_filename );
		return alAttach;
	}
	private ArrayList<Object> generateFilterReasonAttach(String templateFileName, ArrayList<SourceFeedFilter> alFilterInfo, ArrayList alFilteredReason) throws Exception
	{
		ArrayList<Object> alAttach = new ArrayList<Object>();
		ArrayList alReason = ReportFacade.getInstance().listFilteredReason();
		for (int i = 0; alFilterInfo != null && i < alFilterInfo.size(); i++)
		{
			HashMap mMeta    = new HashMap();
			ArrayList alData = new ArrayList();

			SourceFeedFilter filter = alFilterInfo.get(i);
			HashMap mFilteredReason = (HashMap)alFilteredReason.get(i);
			mMeta.put("filter.name", filter.getName());
			for (int j = 0; j < alReason.size(); j++)
			{
				HashMap mReason = (HashMap)alReason.get(j);
				int code = Integer.parseInt(String.valueOf(mReason.get("code")));
				HashMap mData = new HashMap();
				mData.put("code", code);
				mData.put("type_name", mReason.get("type_name"));
				if (null == mFilteredReason.get(code))
					mData.put("filtered_count", 0);
				else
					mData.put("filtered_count", PrimitiveTypeUtil.getInt(mFilteredReason.remove(code)));
				alData.add(mData);
			}
			for (Object obj : mFilteredReason.entrySet())
			{
				Map.Entry entry = (Map.Entry)obj;
				int code = Integer.parseInt(String.valueOf(entry.getKey()));
				HashMap mData = new HashMap();
				mData.put("code", code);
				mData.put("type_name", "not auto release / unknown");
				mData.put("filtered_count", PrimitiveTypeUtil.getInt(mFilteredReason.get(code)));
				alData.add(mData);
			}
			String reportFileName = "statistic_filtered_reason_" + filter.getName() + "_" + strArticleFetchDate + ".html";
			String filename = ReportHelper.prepareReportFile(new java.io.File(templateFileName), fReportDirectory, reportFileName, mMeta, alData);
			alAttach.add(filename);
		}
		return alAttach;
	}
	private String generateFilterReason(String templateFileName, ArrayList<SourceFeedFilter> alFilterInfo, ArrayList alFilteredReason) throws Exception
	{
		ArrayList<Object> alAttach = generateFilterReasonAttach(templateFileName, alFilterInfo, alFilteredReason);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < alAttach.size(); i++)
		{
			String filename = (String)alAttach.get(i);
			sb.append( org.apache.commons.io.FileUtils.readFileToString(new java.io.File(filename), "utf8") );
		}
		return sb.toString();
	}
	private static HashMap getDataPipelineStatisticContent(String[] arrRssId, ArrayList alArticle, String source, SourceFeedFilter filter) throws Exception
	{
		int nTotalArticleCount = alArticle.size();

		HashSet hsActiveSourceFeeds = new HashSet();
		HashMap<String, Integer> mActiveSourceFeedsArticleCount = new HashMap<String, Integer>();
		int ar_totalBizCount      = 0;
		int ar_activeBizCount     = 0;
		int ar_filterArticleCount = 0;
		int ar_totalArticleCount  = 0;
		HashSet hsARActiveBiz = new HashSet();
		int mr_totalBizCount      = 0;
		int mr_activeBizCount     = 0;
		int mr_filterArticleCount = 0;
		int mr_totalArticleCount  = 0;
		HashSet hsMRActiveBiz = new HashSet();
		int pf_totalBizCount      = 0;
		int pf_activeBizCount     = 0;
		int pf_filterArticleCount = 0;
		int pf_totalArticleCount  = 0;
		HashSet hsPFActiveBiz = new HashSet();

		HashMap<String, HashMap> mRssRelease = (filter == null) ? ReportFacade.getInstance().getRssReleaseFlag(arrRssId) : ReportFacade.getInstance().getRssReleaseFlag(arrRssId, filter.getFirmId());
		String[] arrRssIdRC = (String[])mRssRelease.keySet().toArray(new String[0]);
		for (int j = 0; arrRssIdRC != null && j < arrRssIdRC.length; j++)
		{
			boolean fAutoRelease = false;
			HashMap mRssFlag = (HashMap)mRssRelease.get(arrRssIdRC[j]);
			if (mRssFlag == null)
			{
				log.warn("there's no corresponding releaseFlag for " + arrRssIdRC[j]);
			}
			else
			{
				int releaseFlag = PrimitiveTypeUtil.getInt( mRssFlag.get("releaseFlag") );
				int status = (mRssFlag.get("status") == null) ? 0 : PrimitiveTypeUtil.getInt( mRssFlag.get("status") );
				fAutoRelease = (releaseFlag == 1 && status == 1);
			}
			if (fAutoRelease)
			{
				//auto_release
				ar_totalBizCount++;
			}
			else
			{
				//manual_release
				mr_totalBizCount++;
			}
		}

		HashMap mFilteredReason = new HashMap();
		for (int j = 0; alArticle != null && j < alArticle.size(); j++)
		{
			HashMap mArticle = ((HashMap)alArticle.get(j));
			String source_feeds = String.valueOf(mArticle.get("rss_id"));
			hsActiveSourceFeeds.add( source_feeds );

			boolean fChannelRelease = false;
			HashMap mRssFlag = (HashMap)mRssRelease.get(source_feeds);
			if (mRssFlag == null)
			{
				log.warn("there's no corresponding releaseFlag for " + source_feeds);
			}
			else
			{
				int releaseFlag = PrimitiveTypeUtil.getInt( mRssFlag.get("releaseFlag") );
				int status = (mRssFlag.get("status") == null) ? 0 : PrimitiveTypeUtil.getInt( mRssFlag.get("status") );
				//設置且開啟渠道自動發佈
				fChannelRelease = (releaseFlag == 1 && status == 1);
			}

			if (fChannelRelease && mArticle.get("filtered") == null)
			{
				//自動發佈成功
				hsARActiveBiz.add( source_feeds );
				ar_totalArticleCount++;
			}
			else if (fChannelRelease && mArticle.get("filtered") != null)
			{
				//自動發佈被過濾
				int filtered = PrimitiveTypeUtil.getInt(mArticle.get("filtered"));
				if (filtered == 0)
				{
					log.debug("[AutoRelease] auto release not working for article_id : " + mArticle.get("id") + "\trss-id : " + source_feeds);
				}
				int nFilteredCount = (null == mFilteredReason.get( filtered )) ? 0 : PrimitiveTypeUtil.getInt(mFilteredReason.get(filtered));
				nFilteredCount++;
				mFilteredReason.put(filtered, nFilteredCount);
				hsARActiveBiz.add( source_feeds );
				ar_totalArticleCount++;
				hsPFActiveBiz.add( source_feeds );
				pf_filterArticleCount++;
			}
			else 
			{
				hsMRActiveBiz.add( source_feeds );
				mr_filterArticleCount++;
			}
		}

		ar_activeBizCount = hsARActiveBiz.size();
		mr_activeBizCount = hsMRActiveBiz.size();
		pf_activeBizCount = hsPFActiveBiz.size();

		int nTotalBizCount     = (arrRssId == null) ? 0 : arrRssId.length;
		int nActiveSourceFeedsCount = hsActiveSourceFeeds.size();

		HashMap mData = new HashMap();
		mData.put("filter.name", ((filter == null) ? "" : filter.getName()));
		mData.put("filter.channel.s_content.totalBizCount", nTotalBizCount);
		mData.put("filter.channel.s_content.activeBizCount", nActiveSourceFeedsCount);
		mData.put("filter.channel.s_content.filterArticleCount", "");
		mData.put("filter.channel.s_content.totalArticleCount", nTotalArticleCount);

		mr_totalArticleCount = nTotalArticleCount   - mr_filterArticleCount;
		pf_totalArticleCount = ar_totalArticleCount - pf_filterArticleCount;
		int publishTotalArticleCount = pf_totalArticleCount;

		//未自动发佈
		mData.put("filter.manualrelease.s_content.totalBizCount", mr_totalBizCount);
		mData.put("filter.manualrelease.s_content.activeBizCount", mr_activeBizCount);
		mData.put("filter.manualrelease.s_content.filterArticleCount", mr_filterArticleCount);
		mData.put("filter.manualrelease.s_content.totalArticleCount", mr_totalArticleCount);
		//配置公众号并开自动发佈
		mData.put("filter.autorelease.s_content.totalBizCount", ar_totalBizCount);
		mData.put("filter.autorelease.s_content.activeBizCount", ar_activeBizCount);
		mData.put("filter.autorelease.s_content.filterArticleCount", ar_filterArticleCount);
		mData.put("filter.autorelease.s_content.totalArticleCount", ar_totalArticleCount);
		//原运营平台过滤
		mData.put("filter.platform.s_content.totalBizCount", "");
		mData.put("filter.platform.s_content.activeBizCount", pf_activeBizCount);
		mData.put("filter.platform.s_content.filterArticleCount", pf_filterArticleCount);
		mData.put("filter.platform.s_content.totalArticleCount", pf_totalArticleCount);
		//发佈量
		mData.put("publish.s_content.totalBizCount", "");
		mData.put("publish.s_content.activeBizCount", "");
		mData.put("publish.s_content.filterArticleCount", "");
		mData.put("publish.s_content.totalArticleCount", publishTotalArticleCount);
		//推送失败
		mData.put("failed.delivery.totalBizCount", "");
		mData.put("failed.delivery.activeBizCount", "");
		mData.put("failed.delivery.filterArticleCount", "");
		mData.put("failed.delivery.totalArticleCount", "");
		//有展示资讯量
		mData.put("success.delivery.totalBizCount", "");
		mData.put("success.delivery.activeBizCount", "");
		mData.put("success.delivery.filterArticleCount", "");
		mData.put("success.delivery.totalArticleCount", "");

		HashMap mResult = new HashMap();
		mResult.put("filteredReason", mFilteredReason);
		mResult.put("statisticData", mData);
		return mResult;
	}
	public static HashMap getDataPipelineStatisticContent(String strArticleFetchDate, String source, SourceFeedFilter filter) throws Exception
	{
		HashMap<String, HashMap<String, String>> mSource = (filter==null) ? ReportFacade.getInstance().getRssInfoBySource(source)
																		  : ReportFacade.getInstance().getRssInfoBySourceFilter(source, filter);
		//符合渠道過濾的所有 RSS ID
		String[] arrRssId = (String[])mSource.keySet().toArray(new String[0]);
		//符合渠道過濾的RSS ID 在 s_channel_contnet 的文章數，不代表它們都會自動發佈到渠道，必須滿足 t_auto_release_rss.releaseFlag && t_new_firm_source.status = 1
		ArrayList alPublishedArticle = ReportFacade.getInstance().getDailyPublishedArticle(arrRssId, strArticleFetchDate);
		ArrayList alCandidateArticle = ReportFacade.getInstance().getDailyCandidateArticle(arrRssId, strArticleFetchDate);
		ArrayList alArticle = new ArrayList();
		if (alPublishedArticle != null) alArticle.addAll( alPublishedArticle );
		if (alCandidateArticle != null) alArticle.addAll( alCandidateArticle );

		return getDataPipelineStatisticContent(arrRssId, alArticle, source, filter);
	}
	public static HashMap getDataPipelineStatisticContent(Date articleFetchTimeStart, Date articleFetchTimeEnd, String source, SourceFeedFilter filter) throws Exception
	{
		HashMap<String, HashMap<String, String>> mSource = (filter==null) ? ReportFacade.getInstance().getRssInfoBySource(source)
																		  : ReportFacade.getInstance().getRssInfoBySourceFilter(source, filter);
		//符合渠道過濾的所有 RSS ID
		String[] arrRssId = (String[])mSource.keySet().toArray(new String[0]);
		//符合渠道過濾的RSS ID 在 s_channel_contnet 的文章數，不代表它們都會自動發佈到渠道，必須滿足 t_auto_release_rss.releaseFlag && t_new_firm_source.status = 1
		ArrayList alPublishedArticle = ReportFacade.getInstance().getDailyPublishedArticleByTimeRange(arrRssId, articleFetchTimeStart, articleFetchTimeEnd);
		ArrayList alCandidateArticle = ReportFacade.getInstance().getDailyCandidateArticleByTimeRange(arrRssId, articleFetchTimeStart, articleFetchTimeEnd);
		ArrayList alArticle = new ArrayList();
		if (alPublishedArticle != null) alArticle.addAll( alPublishedArticle );
		if (alCandidateArticle != null) alArticle.addAll( alCandidateArticle );

		return getDataPipelineStatisticContent(arrRssId, alArticle, source, filter);
	}
	public HashMap<String, Object> generateMailContent(String templateFileName, String source) throws Exception
	{
		HashMap mMeta    = new HashMap();
		ArrayList alData = new ArrayList();
		mMeta.put("statisticTime", strStatisticTime);
		mMeta.put("fetchDate", strArticleFetchDate);
		ArrayList alRssInfo = ReportFacade.getInstance(AbstractDBFacade.DBNAME_WECHAT).getAllRssInfo(source);
		//新庫原始爬取量
		mMeta.put("crawler.t_content.totalBizCount", "");
		mMeta.put("crawler.t_content.totalArticleCount", "");
		mMeta.put("crawler.t_content.activeBizCount", "");
		//新庫清洗入庫量
		int nTotalBizCount          = (alRssInfo == null) ? 0 : alRssInfo.size();
		int nTotalArticleCount      = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getDailyCrawledArticleCount(source, strArticleFetchDate);
		int nActiveSourceFeedsCount = ReportFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getDailyCrawledActiveSourceFeedCount(source, strArticleFetchDate);
		mMeta.put("filter.cleaner.t_content.totalBizCount", String.format(NUMERIC_FORMAT, nTotalBizCount));
		mMeta.put("filter.cleaner.t_content.totalArticleCount", String.format(NUMERIC_FORMAT, nTotalArticleCount));
		mMeta.put("filter.cleaner.t_content.activeBizCount", String.format(NUMERIC_FORMAT, nActiveSourceFeedsCount));

		HashMap<String, HashMap<String, String>> mSourceFeeds = ReportFacade.getInstance().getRssInfoBySource(source);
		String[] arrRssId = (String[])mSourceFeeds.keySet().toArray(new String[0]);
		//舊庫發佈量
		ArrayList alPublishedArticle = ReportFacade.getInstance().getDailyPublishedArticle(arrRssId, strArticleFetchDate);
		ArrayList alCandidateArticle = ReportFacade.getInstance().getDailyCandidateArticle(arrRssId, strArticleFetchDate);
		ArrayList alArticle = new ArrayList();
		if (alPublishedArticle != null) alArticle.addAll( alPublishedArticle );
		if (alCandidateArticle != null) alArticle.addAll( alCandidateArticle );
		nTotalBizCount     = (arrRssId == null)  ? 0 : arrRssId.length;
		nTotalArticleCount = (alArticle == null) ? 0 : alArticle.size();
		HashSet hsActiveSourceFeeds = new HashSet();
		HashMap<String, Integer> mSourceFeedsArticleCount = new HashMap<String, Integer>();
		for (int i = 0; alArticle != null && i < alArticle.size(); i++)
		{
			HashMap mArticle = ((HashMap)alArticle.get(i));
			String source_feeds = String.valueOf(mArticle.get("rss_id"));
			hsActiveSourceFeeds.add( source_feeds );

			Integer nArticleCount = (Integer)mSourceFeedsArticleCount.get(source_feeds);
			if (nArticleCount == null)
			{
				nArticleCount = 0;
			}
			nArticleCount++;
			mSourceFeedsArticleCount.put(source_feeds, nArticleCount);
		}
		nActiveSourceFeedsCount = hsActiveSourceFeeds.size();
		//舊庫原始爬取量
		mMeta.put("crawler.s_content.totalBizCount", "");
		mMeta.put("crawler.s_content.activeBizCount", "");
		mMeta.put("crawler.s_content.filterArticleCount", "");
		mMeta.put("crawler.s_content.totalArticleCount", "");

		mMeta.put("filter.operation.s_content.totalBizCount", "");
		mMeta.put("filter.operation.s_content.activeBizCount", "");
		mMeta.put("filter.operation.s_content.filterArticleCount", "");
		mMeta.put("filter.operation.s_content.totalArticleCount", "");
		//舊庫清洗入庫量
		mMeta.put("filter.cleaner.s_content.totalBizCount", String.format(NUMERIC_FORMAT, nTotalBizCount));
		mMeta.put("filter.cleaner.s_content.activeBizCount", String.format(NUMERIC_FORMAT, nActiveSourceFeedsCount));
		mMeta.put("filter.cleaner.s_content.filterArticleCount", "");
		mMeta.put("filter.cleaner.s_content.totalArticleCount", String.format(NUMERIC_FORMAT, nTotalArticleCount));

		JsonStringManager jsmgr = JsonStringManager.getManager("filter");
		String prefix  = "sourcefilter";
		String channel = "overall";
		String json_key = TextUtil.getString(new String[]{prefix, channel}, ".");
		HashMap mImageConstraint = new HashMap();
		for (Map.Entry<Object, Object> entry : ((HashMap<Object, Object>)jsmgr.getObject("image_constraint.overall")).entrySet())
		{
			String key = (String)entry.getKey();
			int idx = key.lastIndexOf(".");
			if (idx >= 0) key = key.substring(idx + 1);
			mImageConstraint.put(key, entry.getValue());
		}
		log.info("mImageConstraint:" + mImageConstraint);
		ArrayList alFilter = (ArrayList)jsmgr.getObject(json_key);
		ArrayList<SourceFeedFilter> alFilterInfo = new ArrayList<SourceFeedFilter>();
		ArrayList<HashMap> alFilteredReason = new ArrayList<HashMap>();
		for (int i = 0; alFilter != null && i < alFilter.size(); i++)
		{
			HashMap mFilter = (HashMap)alFilter.get(i);
			mFilter.put("image_constraint", mImageConstraint);
			SourceFeedFilter filter = new SourceFeedFilter(channel, mFilter);
			alFilterInfo.add(filter);

			HashMap mContent = getDataPipelineStatisticContent(strArticleFetchDate, source, filter);
			HashMap<Object, Object> mFilteredReason = (HashMap<Object, Object>)mContent.get("filteredReason");
			HashMap<Object, Object> mStatisticData  = (HashMap<Object, Object>)mContent.get("statisticData");

			for (Map.Entry<Object, Object> entry : mStatisticData.entrySet())
			{
				String fieldName = (String)entry.getKey();
				Object value = entry.getValue();
				if (value != null && value instanceof Number)
				{
					mStatisticData.put(fieldName, String.format(NUMERIC_FORMAT, value));
				}
			}

			alFilteredReason.add(mFilteredReason);
			alData.add( mStatisticData );
		}
		String reportFileName = "statistic_overview_" + strArticleFetchDate + ".html";
		String filename = ReportHelper.prepareReportFile(new java.io.File(templateFileName), fReportDirectory, reportFileName, mMeta, alData);
		log.info("mail_content_filename=" + filename);

		HashMap hmMail = new HashMap();
		hmMail.put("content", org.apache.commons.io.FileUtils.readFileToString(new java.io.File(filename), "utf8"));
		hmMail.put("attachment", generateFilterReasonAttach("source_filter_reasons.tpl", alFilterInfo, alFilteredReason));
		return hmMail;
	}

	private static void printCliHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + DailySourceFilterPublishStatistic.class.getCanonicalName(), createOptions());
	}

	private static Options createOptions()
	{
		Options options = new Options();

		options.addOption(
				Option.builder().argName("date")
				.longOpt("date")
				.hasArg(true)
				.desc("discovery_date for statistic.[format: yyyy-MM-dd, default: yesterday]")
				.required(false)
				.build()
		);

		options.addOption(
				Option.builder().argName("source")
				.longOpt("source")
				.hasArg(true)
				.desc("source list for statistic.[format: comma-separated string, like: 微信.")
				.required(true)
				.build()
		);

		options.addOption(
				Option.builder().argName("help")
				.longOpt("help")
				.hasArg(false)
				.desc("print help messages.")
				.required(false)
				.build()
		);

		return options;
	}

	public static void main(String[] args)
	{
		try
		{
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = null;
			try
			{
				cmd = parser.parse(createOptions(), args);
			}
			catch (ParseException e)
			{
				printCliHelp();
				throw new Exception("Error in parsing argument:" + e.getMessage());
			}

			if (cmd.hasOption("help"))
			{
				printCliHelp();
				return;
			}

			String dateString = null;
			if (cmd.hasOption("date"))
			{
				dateString  = cmd.getOptionValue("date");
			}
			else
			{
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_YEAR, -1);
				dateString = DateUtil.dateToString(c.getTime(), DATE_PATTERN);
			}

			String source = cmd.getOptionValue("source");

			DailySourceFilterPublishStatistic task = new DailySourceFilterPublishStatistic(dateString);

			HashMap mData = task.generateMailContent("./source_filter_overview.tpl", source);
			ArrayList<Object> alAttached = new ArrayList<Object>();
			String mailContent = (String)mData.get("content");
			if (null != mData.get("attachment"))
			{
				alAttached.addAll((ArrayList<Object>)mData.get("attachment"));
			}
			alAttached.addAll( task.generateMailAttach("./source_filter_attachement.tpl", "./source_filter_detail.tpl", source) );
			String configPrefix = DailySourceFilterPublishStatistic.class.getSimpleName() + ".";
			StringManager smgr = StringManager.getManager("system");
			String mailSender = smgr.getString("report.mail.sender");
			String[] mailReceiver = TextUtil.getStringList( smgr.getString(configPrefix + "report.mail.receiver") );
			String mailSubject = smgr.getString(configPrefix + "report.mail.subject", source) ;
			MailSender ms = new MailSender(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_TLS);
			ms.setMailFrom(mailSender);
			ms.setSubject(mailSubject);
			ms.setMailTo(mailReceiver);
			ms.setHtmlContent(mailContent);
			if (alAttached.size() > 0)
			{
				Object[] attached = alAttached.toArray();
				ms.setAttachedFile(attached);
			}
			ms.send();
		}
		catch (Exception e)
		{
			log.fatal("", e);
		}
	}
}
