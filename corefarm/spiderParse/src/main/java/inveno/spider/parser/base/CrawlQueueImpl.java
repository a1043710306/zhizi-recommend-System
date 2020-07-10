package inveno.spider.parser.base;

import inveno.spider.common.RabbitmqHelper;
import inveno.spider.parser.extractor.Extractor;
import inveno.spider.parser.model.Profile;
import inveno.spider.rmq.model.QueuePrefix;

import java.util.List;

public class CrawlQueueImpl implements CrawlQueue
{
    private Profile profile;

    public CrawlQueueImpl(Profile profile)
    {
        this.profile = profile;
    }

    public void submit(Page page)
    {
        String queueName = "";
        if ((this.profile.isListingJavascriptProcess() && page.getType()==Extractor.Type.Listing)
                || (this.profile.isContentJavascriptProcess() && page.getType()==Extractor.Type.Content))
        {
            queueName = QueuePrefix.SYS_JS_PAGE_QUEUE.name();
        } else
        {
            queueName = QueuePrefix.SPIDER.getValue() + page.getPubCode();
        }
        RabbitmqHelper.getInstance().sendMessage(queueName, page);
    }

    public void submitAll(List<Page> pages)
    {
        for (Page page : pages)
        {
            submit(page);
        }
    }
}
