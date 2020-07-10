package inveno.spider.reports.util;

import java.util.*;

import org.apache.log4j.Logger;
import com.google.gson.*;
import org.apache.commons.lang.StringUtils;

import inveno.spider.common.facade.AbstractDBFacade;
import inveno.spider.common.utils.JsonUtils;
import inveno.spider.common.utils.TerminatableThread;
//import inveno.spider.common.RabbitmqHelper;
import inveno.spider.reports.facade.ContentFacade;

import com.google.gson.*;

/**
 * Created by dell on 2016/5/17.
 */
public class FirmAppAppendingDaemon extends TerminatableThread
{
	private static final Logger log = Logger.getLogger(FirmAppAppendingDaemon.class);

	public static final String QUEUE_CONTENT_FIRMAPP = "PROPIETORY_CONTENT_FIRMAPP";

	private static FirmAppAppendingDaemon instance = null;

	private boolean fRunning = false;

	public static synchronized FirmAppAppendingDaemon getInstance()
	{
		if (instance == null)
		{
			instance = new FirmAppAppendingDaemon();
		}
		return instance;
	}
	private FirmAppAppendingDaemon()
	{
	}

	public void start()
	{
		if (!isTerminated())
		{
			doTask();
		}
	}
	private void doTask()
	{
		while (true)
		{
			if (isTerminated())
			{
				break;
			}

			String json =null;// RabbitmqHelper.getInstance().getMessage(QUEUE_CONTENT_FIRMAPP);
			if (!StringUtils.isEmpty(json))
			{
				ArrayList alData = (ArrayList)JsonUtils.toJavaObject( (new Gson()).fromJson(json, JsonElement.class) );
				for (int i = 0; i < alData.size(); i++)
				{
					HashMap mData = (HashMap)alData.get(i);
					String contentId = String.valueOf(mData.get("content_id"));
					String firmApp = String.valueOf(mData.get("firm_app"));
					log.info("append firm_app=" + firmApp + " for content_id=" + contentId);
					HashMap mContent = ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).getContent(Integer.parseInt(contentId));
					HashSet<String> hsFirmApp = new HashSet<String>();
					hsFirmApp.add(firmApp);
					String currentFirmApp = (String)mContent.get("firm_app");
					if (!StringUtils.isEmpty(currentFirmApp))
					{
						ArrayList alFirmApp = (ArrayList)JsonUtils.toJavaObject( (new Gson()).fromJson((String)mContent.get("firm_app"), JsonElement.class) );
						for (int j = 0; j < alFirmApp.size(); j++)
						{
							HashMap mFirmApp = (HashMap)alFirmApp.get(j);
							hsFirmApp.add((String)mFirmApp.get("app"));
						}
					}
					String updatedFirmApp = firmToJsonArray(hsFirmApp);
					HashMap mUpdateData = new HashMap();
					mUpdateData.put("content_id", contentId);
					mUpdateData.put("firm_app", updatedFirmApp);
					mUpdateData.put("update_time", new java.util.Date());
					try
					{
						ContentFacade.getInstance(AbstractDBFacade.DBNAME_CRAWLER).updateData(mUpdateData);
					}
					catch (Exception e)
					{
						log.fatal("[updateData]", e);
					}
				}
			}
			try
			{
				Thread.currentThread().sleep(10);
			}
			catch (Exception e)
			{
			}
		}
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
	public static void main(String[] args)
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run()
				{
					FirmAppAppendingDaemon.getInstance().terminated();
					while (true)
					{
						if (FirmAppAppendingDaemon.getInstance().isTerminated())
							break;
						try
						{
							Thread.currentThread().sleep(100);
						}
						catch (Exception e)
						{
							log.fatal("[Termination]", e);
						}
					}
					//RabbitmqHelper.getInstance().close();
				}
			});
			FirmAppAppendingDaemon.getInstance().start();
		}
		catch (Exception e)
		{
			log.fatal("[main]", e);
		}
	}
}