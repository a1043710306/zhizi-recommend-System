package inveno.spider.parser.base;

import inveno.spider.parser.exception.ExtractException;

public class Html2Xml
{
    public enum Strategy
    {
        tagSoup, htmlCleaner
       
       
    }

    public static String convert(String src, Strategy strategy)
            throws ExtractException
    {
        // if the strategy is null,default to htmlCleaner.
        if ((null == strategy) || (strategy == Strategy.htmlCleaner))
        {
            return Html2XmlHtmlCleaner.convert(src);
        } else if (strategy == Strategy.tagSoup)
        {
            return Html2XmlTagSoup.convert(src);
        } else
        {
            throw new IllegalArgumentException("Unknown Html2Xml strategy "
                    + strategy);
        }
    }
}
