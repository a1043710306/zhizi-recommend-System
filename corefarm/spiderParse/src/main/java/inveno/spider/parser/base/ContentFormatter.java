package inveno.spider.parser.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


public class ContentFormatter {
    private static final char[] spaces = {' ', ' ', '　'};   // normal(32), ascii160(160), full(12288)
    
    private static final List<String> filterTags = new ArrayList<String>(Arrays.asList(new String[]{
            "head", "iframe", "script", "style", "noscript", "select", "fieldset", "button"}));

    private static final List<String> paragraphTags = new ArrayList<String>(Arrays.asList(new String[]{
            "p", "div", "td", "h1", "h2", "h3", "h4", "h5", "h6", "h7"}));
    
    private static final List<String> remainTags=new ArrayList<String>(Arrays.asList(new String[]{
       "p","strong","font","img"     
    }));
    
    private static final int lineLengthForConvertBr = 80;
    

    public static String formatTitle(String text) {
        text = text.replace('\r', ' ');
        text = text.replace('\n', ' ');
        text = normalizeSpace(text);
        return StringUtils.trim(text);
    }
    
    public static String format(Node node) {
        StringBuilder sb = new StringBuilder();
        boolean isConvertBr = extractText(node, sb, null, 0);
        String string = sb.toString();
        if(isConvertBr) string = addNewLines(string);
        return removeRedundantNewLines(normalizeSpace(string));
    }

    public static String format(Node node, String filterNodes) {
        StringBuilder sb = new StringBuilder();
        if(filterNodes != null && filterNodes.toLowerCase().equals("auto")){
        	autofilterNoiseNodes(node);
        	filterNodes = null;
        }
//        filterNodes = "h4 | font[@color] | div[@class='footerLinks'] | div[@class='page' and @id='content_bottom']";
        List<FilterNode> manualFilterNodes = getManualFilterNodes(filterNodes);
        boolean isConvertBr = extractText(node, sb, manualFilterNodes, 0);
        String string = sb.toString();
        if(isConvertBr) string = addNewLines(string);
        return removeRedundantNewLines(normalizeSpace(string));
    }
    
    private static void autofilterNoiseNodes(Node e){
    	//纯a标签不做判断,避免误删文章里面的链接
    	if ("a".equalsIgnoreCase(e.getNodeName())) {
    		return;
    	}
    	
		StringBuffer alinktext = new StringBuffer();
		StringBuffer text = new StringBuffer();
		int linkSize = getLinkDensity(e, alinktext, text);
		float ldy = (float)alinktext.length()/(float)(text.length() > 0?text.length():1);
		if(ldy > 0.5f || text.length() < linkSize * 5){
			e.getParentNode().removeChild(e);
		}else{
			//遍历子结点
			for(int i = 0; i <  e.getChildNodes().getLength(); i ++){
	    		Node child = e.getChildNodes().item(i);
	    		autofilterNoiseNodes(child);
	    	}
			
		}
    	return;
    }
    
    private static int getLinkDensity(Node node , StringBuffer alinktext, StringBuffer text){
    	int linkSize = 0;
    	if ("a".equalsIgnoreCase(node.getNodeName())) {
    		alinktext.append(node.getTextContent().trim());	
    		linkSize ++;
    	}
    	
    	if (node.getNodeType() == Node.TEXT_NODE) {
    		text.append(trimHtmlMultipleSpaces(node.getNodeValue().trim()));
    	}
    	
    	NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
        	linkSize += getLinkDensity(nodeList.item(i), alinktext ,text);
            
        }
    	return linkSize;
    }
    
    
    private static List<FilterNode> getManualFilterNodes(String filterNodes){
    	if(filterNodes == null) return null;
    	List<FilterNode> manualFilterNodes = new ArrayList<FilterNode>();
    	if(filterNodes.contains("/")) 
    		throw new RuntimeException("filterNodes is wrong, for it contains '/'. ");
    	String [] tags = filterNodes.split("\\s*\\|\\s*");
    	for(String tag : tags){
    		FilterNode filterNode = new FilterNode();
    		tag = tag.trim();
    		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
    		Matcher matcher = pattern.matcher(tag);
    		if(matcher.find()){
    			String tagName = tag.substring(0, tag.indexOf("["));
    			String strAttributes = matcher.group(1);
    			filterNode.setNodeName(tagName);
    			filterNode.setAttributeNodes(parseAttributeNodes(strAttributes));
    		} else{
    			filterNode.setNodeName(tag);
//    			manualFilterTags.put(tag, null);
    		}
    		manualFilterNodes.add(filterNode);
    	}
    	return manualFilterNodes;
    }
    
    private static HashMap<String, String> parseAttributeNodes(String strAttributes){
		HashMap<String,String> attributeNodes = new HashMap<String, String>();
//		@border='1' and @width and @id='table1'
		String[] attributes = strAttributes.split("\\s*and\\s*");
		for(String attribute: attributes){
//			@id='table1'
			attribute = attribute.trim();
			if(attribute.length()==0) throw new RuntimeException("The attribute only contains spaces.");
			if('@'!=attribute.charAt(0)) throw new RuntimeException("The first char of the attribute is not '@' : " + attribute);
			Pattern pattern = Pattern.compile("=\\s*'(.+?)'");
    		Matcher matcher = pattern.matcher(attribute);
    		if(matcher.find()){
    			String attributeName = attribute.substring(1, attribute.indexOf("=")).trim();
    			String attributeValue = matcher.group(1);
    			attributeNodes.put(attributeName, attributeValue);
    		} else{
    			String attributeName = attribute.substring(1).trim();
				attributeNodes.put(attributeName, null);
    		}
		}
		return attributeNodes;
    }

    private static boolean extractText(Node node, StringBuilder sb, List<FilterNode> manualFilterNodes, int level) {
//        if(hasUpper(node.getNodeName()))
//            throw new RuntimeException(node.getNodeName());
    	
    	boolean isConvertBr = false;
		if (level>0 && manualFilterNodes != null) {
			for (FilterNode filterNode : manualFilterNodes) {
				if (filterNode.getNodeName().equals(node.getNodeName())) {
					HashMap<String, String> filterAttributeNodes = filterNode.getAttributeNodes();
					if (filterAttributeNodes == null) return isConvertBr;
					else {
						boolean filter = isFilterNode(node, filterAttributeNodes);
						if (filter) return isConvertBr;
					}
				}
			}
		}
		
        if("#text".equals(node.getNodeName())) {
            String text = node.getNodeValue();
            text = trimHtmlMultipleSpaces(text);
            if(StringUtils.isBlank(text)) return isConvertBr;
            if(text.length()>lineLengthForConvertBr) isConvertBr = true;
            sb.append(text);

        } else if("br".equals(node.getNodeName())) {
            sb.append("<br/>");
        }
//        else if("strong".equalsIgnoreCase(node.getNodeName())) {
//            sb.append(DOM3C.asXML(node));
//        } 
//        else if("font".equalsIgnoreCase(node.getNodeName())) {
//            sb.append(DOM3C.asXML(node));
//        } 
//        else if("p".equals(node.getNodeName())){
//            NodeList nodeList = node.getChildNodes();
//           if(nodeList!=null && nodeList.getLength()>0)
//           {
//               sb.append("<p>");
//               for (int i = 0; i < nodeList.getLength(); i++) {
//                   boolean b = extractText(nodeList.item(i), sb, manualFilterNodes, ++level);
//                   if(b) isConvertBr=true;
//               }
//            sb.append("</p>");
//           }else
//           {
//               sb.append(DOM3C.asXML(node));
//           }
//        }
//        else if("img".equals(node.getNodeName())){
//                sb.append(DOM3C.asXML(node));
//                return isConvertBr;
//        }        
        else if(remainTags.contains(node.getNodeName()))
        {
            NodeList nodeList = node.getChildNodes();
            if(nodeList!=null && nodeList.getLength()>0)
            {
                sb.append("<").append(node.getNodeName()).append(">");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    boolean b = extractText(nodeList.item(i), sb, manualFilterNodes, ++level);
                    if(b) isConvertBr=true;
                }
                sb.append("</").append(node.getNodeName()).append(">");
            }else
            {
                sb.append(DOM3C.asXML(node));
            } 
        }
        else if(filterTags.contains(node.getNodeName())) {
            return isConvertBr;

        } else if(paragraphTags.contains(node.getNodeName())) {
            sb.append("<br/><br/>");
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                boolean b = extractText(nodeList.item(i), sb, manualFilterNodes, ++level);
                if(b) isConvertBr=true;
            }
            sb.append("<br/><br/>");
        } else {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                boolean b = extractText(nodeList.item(i), sb, manualFilterNodes, ++level);
                if(b) isConvertBr=true;
            }
        }
        return isConvertBr;
    }
    
    private static boolean isFilterNode(Node node, HashMap<String, String> filterAttributeNodes){
    	boolean filter = true;
		NamedNodeMap attributesMap = node.getAttributes();
		if(attributesMap==null) {
			filter = false;
			return filter;
		}
		for(Map.Entry<String, String> entry : filterAttributeNodes.entrySet()){
			String filterAttributeName = entry.getKey();
			String filterAttributeValue = entry.getValue();
			Node attribute = attributesMap.getNamedItem(filterAttributeName);
			// attributesMap doesn't contain filter attribute
			if(attribute==null){
				filter = false;
				break;
			} else if(filterAttributeValue!=null){
				if(!filterAttributeValue.equals(attribute.getNodeValue())){
					filter = false;
        			break;
				}
			}
		}
		return filter;
		
    }
    
    private static boolean hasUpper(String nodeName) {
        for (int i = 0; i < nodeName.length(); i++) {
            if(Character.isUpperCase(nodeName.charAt(i))) return true;
        }
        return false;
    }

    private static String normalizeSpace(String text) {
        // normalize space to ascii 32
        text = text.replace(spaces[1], ' ');
        text = text.replace(spaces[2], ' ');
        return text;
    }
    
    private static String trimHtmlMultipleSpaces(String text) {
        text = text.replace('\r', ' ');
        text = text.replace('\n', ' ');

        if (StringUtils.isBlank(text) && text.length() > 0) // return a single space
            return " ";

        boolean lastIsSpace = false;
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                if (lastIsSpace)
                    continue;
                lastIsSpace = true;
            } else {
                lastIsSpace = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
    private static String addNewLines(String string){
    	 try {
             BufferedReader reader = new BufferedReader(new StringReader(string));
             StringBuilder sb = new StringBuilder(string.length());
             String line = null;
             while((line=reader.readLine())!=null) {
                 if(StringUtils.isBlank(line)) 
                	 sb.append('\n');
                 else 
                	 sb.append(line).append("\n\n");    
             }
             return sb.toString();
         } catch(IOException e) {
             throw new RuntimeException(e);
         }
    }
    
    private static String removeRedundantNewLines(String string) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(string));
            StringBuilder sb = new StringBuilder(string.length());
            int emptyLineCount = 0;
            String line = null;
            while((line=reader.readLine())!=null) {
                if(StringUtils.isBlank(line)) {
                    emptyLineCount++;
                    if(emptyLineCount>1) continue;
                    else sb.append('\n');
                } else {
                    emptyLineCount = 0;
                    sb.append(StringUtils.trim(line)).append('\n');
                }
            }
            return sb.toString();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
