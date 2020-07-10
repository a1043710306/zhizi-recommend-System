package inveno.spider.parser.extractor;

import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.Page;

import java.util.List;


public interface Extractor {
    public static enum Type{Listing, Feed, Content,JsonListing,JsonContent, API}

    public List<Page> extract(Page page);
    
    public String getCharset();
    
    public Html2Xml.Strategy getHtml2XmlStrategy();
}
