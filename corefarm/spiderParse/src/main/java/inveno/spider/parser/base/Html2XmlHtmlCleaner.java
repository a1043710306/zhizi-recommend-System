package inveno.spider.parser.base;

import inveno.spider.parser.exception.ExtractException;

import java.io.StringWriter;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;

public class Html2XmlHtmlCleaner {
    public static String convert(String src) throws ExtractException {
		try {
		    HtmlCleaner cleaner = new HtmlCleaner();
		    
		    CleanerProperties props = cleaner.getProperties();
		    props.setIgnoreQuestAndExclam(true);  // ignore <?xml ?> tag in the middle
		    props.setUseCdataForScriptAndStyle(false);
		    
//		    props.setOmitUnknownTags(true);
//		    props.setTreatDeprecatedTagsAsContent(true);
		    
		    TagNode tagNode = cleaner.clean(src);
		    StringWriter writer = new StringWriter();
		    new SimpleXmlSerializer(props).write(tagNode, writer, "UTF-8");
	        
	        return writer.toString();
		} catch(Exception e) {
		    throw new ExtractException("Fail to convert html to xml",e);
		}
	}
}
