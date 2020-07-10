package inveno.spider.parser.base;

import java.util.List;

public interface CrawlQueue {

    public void submit(Page page);

    public void submitAll(List<Page> pages);
    
}
