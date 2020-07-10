package inveno.spider.parser.utils;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class EDCName implements NamespaceContext{
	 
    public String getNamespaceURI(String prefix) { 
        if (prefix.equals("ns1")) 
        { 
            return "http://edc.usgs.gov"; 
        } 
        else if (prefix.equals("content")) 
        {
        	
            return "http://purl.org/rss/1.0/modules/content/"; 
        }
        else { 
            return "http://edc/usgs/gov/xsd"; 
        } 
 
    }
 
    public String getPrefix(String namespace) { 
        if (namespace.equals("http://edc.usgs.gov")) { 
            return "ns1"; 
        } else { 
            return "ns"; 
        } 
    } 
 
    public Iterator<?> getPrefixes(String namespace) { 
        return null; 
    } 

}
