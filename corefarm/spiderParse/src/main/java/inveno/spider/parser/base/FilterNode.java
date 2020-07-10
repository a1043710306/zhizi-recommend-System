package inveno.spider.parser.base;

import java.util.HashMap;

public class FilterNode {
	
	private String mNodeName;
	private HashMap<String, String> mAttributeNodes;
	
	public String getNodeName() {
		return mNodeName;
	}
	public void setNodeName(String nodeName) {
		mNodeName = nodeName;
	}
	public HashMap<String, String> getAttributeNodes() {
		return mAttributeNodes;
	}
	public void setAttributeNodes(HashMap<String, String> attributeNodes) {
		mAttributeNodes = attributeNodes;
	}
		

}
