package inveno.spider.parser.model;

import inveno.spider.parser.base.ParseStrategy.ClickNumExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;

public class ClickNumPath extends Path
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ClickNumExtractionStrategy numStrategy;

    public ClickNumPath(PathStrategy strategy, String path,
            ClickNumExtractionStrategy numStrategy, String regularExpression,
            String replaceWith, String matchStrategy)
    {
        super(strategy, path, regularExpression, replaceWith, matchStrategy);
        this.numStrategy = numStrategy;
    }

    public ClickNumExtractionStrategy getNumStrategy()
    {
        return numStrategy;
    }

}
