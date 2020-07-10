package inveno.spider.parser.base;

import inveno.spider.parser.exception.ExtractException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



public class Html implements Serializable {
    private static final long serialVersionUID = 2L;
    private String mHtml;
    private Map<Html2Xml.Strategy, String> mXmls;

    public Html(String html) {
        mHtml = html;
        mXmls = new HashMap<Html2Xml.Strategy, String>(2);
    }

    public String getHtml() {
        return mHtml;
    }

    public String getXml(Html2Xml.Strategy strategy) {
        try {
            String xml = mXmls.get(strategy);
            if(xml==null) {
                xml = Html2Xml.convert(mHtml, strategy);
                mXmls.put(strategy, xml);
            }
            return xml;
        } catch (ExtractException e) {
            return null;
        }
        
    }
}
