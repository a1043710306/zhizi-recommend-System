package inveno.spider.parser.model;

import inveno.spider.parser.base.ParseStrategy.PathStrategy;

public class ContentPath extends Path
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String filterNodes;
    private boolean replaceLabelToDiv = false; // default is false
    private boolean outputURL = false;// default is false

    public boolean isReplaceLabelToDiv()
    {
        return replaceLabelToDiv;
    }

    public void setReplaceLabelToDiv(boolean replaceLabelToDiv)
    {
        this.replaceLabelToDiv = replaceLabelToDiv;
    }

    public ContentPath(PathStrategy strategy, String path,
            String regularExpression, String replaceWith, String matchStrategy,
            String filterNodes, boolean replaceLabelToDiv, boolean outputURL)
    {
        super(strategy, path, regularExpression, replaceWith, matchStrategy);
        this.filterNodes = filterNodes;
        this.replaceLabelToDiv = replaceLabelToDiv;
        this.setOutputURL(outputURL);
    }


    public String getFilterNodes()
    {
        return filterNodes;
    }

    public void setFilterNodes(String filterNodes)
    {
        this.filterNodes = filterNodes;
    }

    /**
     * @return the outputURL
     */
    public boolean isOutputURL()
    {
        return outputURL;
    }

    /**
     * @param outputURL
     *            the outputURL to set
     */
    public void setOutputURL(boolean outputURL)
    {
        this.outputURL = outputURL;
    }

}
