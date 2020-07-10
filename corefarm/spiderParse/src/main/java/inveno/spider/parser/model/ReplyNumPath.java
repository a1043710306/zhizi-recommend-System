package inveno.spider.parser.model;

import inveno.spider.parser.base.ParseStrategy.PathStrategy;
import inveno.spider.parser.base.ParseStrategy.ReplyNumExtractionStrategy;

public class ReplyNumPath extends Path
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ReplyNumExtractionStrategy numStrategy;

    public ReplyNumPath(PathStrategy strategy, String path,
            ReplyNumExtractionStrategy numStrategy, String regularExpression,
            String replaceWith, String matchStrategy)
    {
        super(strategy, path, regularExpression, replaceWith, matchStrategy);
        this.numStrategy = numStrategy;
    }

    public ReplyNumExtractionStrategy getNumStrategy()
    {
        return numStrategy;
    }

}
