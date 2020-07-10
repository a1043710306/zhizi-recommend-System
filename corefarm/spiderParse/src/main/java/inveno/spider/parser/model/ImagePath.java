package inveno.spider.parser.model;

import java.io.Serializable;


public class ImagePath implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String srcTag;
    
    public ImagePath(String srcTag)
    {
        this.srcTag=srcTag;
    }

    public String getSrcTag()
    {
        return srcTag;
    }

    public void setSrcTag(String srcTag)
    {
        this.srcTag = srcTag;
    }

}
