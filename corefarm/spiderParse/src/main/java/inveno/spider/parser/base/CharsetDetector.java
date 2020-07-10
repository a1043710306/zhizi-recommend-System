package inveno.spider.parser.base;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

public class CharsetDetector {
    private static Pattern metaPattern = Pattern.compile(
            "<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>",
            Pattern.CASE_INSENSITIVE);
    private static Pattern charsetPattern = Pattern.compile(
            "charset=([^ \"\'/]+)",
            Pattern.CASE_INSENSITIVE);
    /**
     * e.g:<br/>
     * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;<br/>
     * or<br/>
     * &lt?xml version="1.0" encoding="us-ascii"?&gt;&lt;?xml-stylesheet title="XSL_formatting" type="text/xsl" href="App_Themes/2007silver/RssFeeds.xsl"?&gt;
     */
    private static Pattern xmlPattern = Pattern.compile(
            //old
//            "<\\?xml.*encoding=\"(.*)\".*\\?>",
            //In the XML file,it must be in character(<?xml) beginning.
            "^[<?xml](.*)encoding=\"(\\S+)\"",
            Pattern.CASE_INSENSITIVE);
    private static String DEFAULT_CHARSET = "ISO-8859-1";
    
    private static Map<String, String> encodingReplacementMap = new HashMap<String, String>();
    static {
        encodingReplacementMap.put("gb2312", "GBK");
        encodingReplacementMap.put("big5", "Big5_HKSCS");
        encodingReplacementMap.put("utf_8", "UTF-8");
    }

    public static String getCharset(byte[] bytes, String defaultCharset) {
        if(defaultCharset==null) defaultCharset = DEFAULT_CHARSET;
        String content;
        try {
            content = new String(bytes, defaultCharset);
        } catch (UnsupportedEncodingException e) {
            defaultCharset = DEFAULT_CHARSET;
            try {
                content = new String(bytes, defaultCharset);
            } catch (UnsupportedEncodingException e1) {
                throw new RuntimeException(e1);
            }
        }
        
        // try get charset information from html page
        Matcher m = metaPattern.matcher(content);

        if(m.find()) {
            String meta = m.group(1);
            m = charsetPattern.matcher(meta);
            if(m.find()) {
                String charset = StringUtils.remove(m.group(1), ';');
                if(StringUtils.isNotBlank(charset)) return replacement(charset);
            }
        }
        
        // try get charset information from xml page
        m = xmlPattern.matcher(content);
        if(m.find()) {
            String charset = StringUtils.remove(m.group(2), ';');//old:m.group(1)
            if(StringUtils.isNotBlank(charset)) return replacement(charset);
        }
        
        if(defaultCharset.equalsIgnoreCase(DEFAULT_CHARSET)) {
            // try nsDetector
            DetectObserver observer = new DetectObserver();
            nsDetector det = new nsDetector(nsPSMDetector.CHINESE);
            det.Init(observer);
            det.DoIt(bytes, bytes.length, false);
            det.DataEnd();
            if(observer.getCharset()!=null)
                return replacement(observer.getCharset());
        }

        return replacement(defaultCharset);
    }
    
    private static String replacement(String charset) {
        String replacement = encodingReplacementMap.get(StringUtils.lowerCase(charset));
        return replacement!=null ? replacement : charset; 
    }

    private static class DetectObserver implements nsICharsetDetectionObserver {
        private String mCharset = null;
        public void Notify(String charset) {
            this.mCharset = charset;
        }
        public String getCharset() {
            return mCharset;
        }
    }
}
