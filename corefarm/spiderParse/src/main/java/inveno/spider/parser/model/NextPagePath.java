package inveno.spider.parser.model;

import inveno.spider.parser.base.ParseStrategy.PageStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;

public class NextPagePath extends Path
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int maxpages;
    private PageStrategy pageStrategy;

    public NextPagePath(PathStrategy strategy, String path, int maxpages,
            PageStrategy pageStrategy, String regularExpression,
            String replaceWith, String matchStrategy)
    {
        super(strategy, path, regularExpression, replaceWith, matchStrategy);
        this.maxpages = maxpages;
        this.pageStrategy = pageStrategy;
    }

    public int getMaxpages()
    {
        return maxpages;
    }

    public PageStrategy getPageStrategy()
    {
        return pageStrategy;
    }
}
