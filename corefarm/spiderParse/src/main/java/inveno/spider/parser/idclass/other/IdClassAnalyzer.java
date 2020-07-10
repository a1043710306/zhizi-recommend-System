package inveno.spider.parser.idclass.other;


import inveno.spider.parser.base.ContentFormatter;
import inveno.spider.parser.base.Html2Xml;
import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.base.NodeInfo;
import inveno.spider.parser.exception.AnalyzeException;
import inveno.spider.parser.exception.ExtractException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class IdClassAnalyzer {
    private XPathHelper mXPathHelper;
    public IdClassAnalyzer(XPathHelper pathHelper) {
        mXPathHelper = pathHelper;
    }

    public String analyzeLink(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo... targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes) + "/@href";
    }

    public String analyzeTitle(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo... targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes);
    }

    public String analyzeDate(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo... targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes);
    }

    public String analyzeContent(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo... targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes);
    }

    public String analyzeNextPage(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo[] targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes) + "/@href";
    }
    
    public String analyzeReplyNumber(String html, Strategy html2xmlStrategy, NodeInfo[] targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes);
    }
    
    public String analyzeClickNumber(String html, Strategy html2xmlStrategy, NodeInfo[] targetNodes) throws AnalyzeException {
        return analyzeForNode(html, html2xmlStrategy, targetNodes);
    }

    public String analyzeForNode(String html, Html2Xml.Strategy html2xmlStrategy, NodeInfo... targetNodes) throws AnalyzeException {
        try {
            String xml = Html2Xml.convert(html, html2xmlStrategy);
            Document doc = mXPathHelper.toNode(xml);
            if(targetNodes.length==0) {
                throw new IllegalArgumentException("targetNodes is empty");

            } else {
                Node[] nodes = new Node[targetNodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = findTargetNode(doc, targetNodes[i], html2xmlStrategy);
                    if(nodes[i]==null) {
                        throw new AnalyzeException("Unable to locate selected node with info: " + targetNodes[i]);
                    }
                }
                return identifyNodeXPath(doc, nodes);
            }
        } catch (ExtractException e) {
            throw new AnalyzeException("", e);
        }
    }
    
    private Node findTargetNode(Document doc, NodeInfo nodeInfo, Html2Xml.Strategy html2xmlStrategy) throws ExtractException {
        FindTargetVisitor visitor = new FindTargetVisitor(nodeInfo, html2xmlStrategy);
        new NodeVisitable(doc).start(visitor);

        return visitor.getNode();
    }
    
    private class FindTargetVisitor implements NodeVisitor {
        private NodeInfo mNodeInfo;
        private String mContent;
        private Node mResultNode;
        private double mResultDiff;
        public FindTargetVisitor(NodeInfo nodeInfo, Html2Xml.Strategy html2xmlStrategy) throws ExtractException {
            mNodeInfo = nodeInfo;
            if(nodeInfo.getText()==null) {
                mContent = null;
            } else {
                mContent = removeSpaces(StringEscapeUtils.unescapeHtml(ContentFormatter.format(mXPathHelper.toNode(Html2Xml.convert("<div>" + mNodeInfo.getText() + "</div>", html2xmlStrategy)))));
//try {
//    IOUtils.write(mContent+"\n", System.out, "Big5");
//} catch (IOException e) {
//    e.printStackTrace();
//}
                if(mContent.length()<2) throw new ExtractException("Content for compare is too short, length=" + mContent.length());
            }
            mResultNode = null;
            mResultDiff = -1;
        }
        
        private String removeSpaces(String str) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                if(!Character.isWhitespace(str.charAt(i))) sb.append(str.charAt(i));
            }
            return sb.toString();
        }

        public Node getNode() {
            return mResultNode;
        }

        public void visit(Node node) {
            if(mNodeInfo.getTag()!=null && !mNodeInfo.getTag().equals(node.getNodeName())) return;

            NamedNodeMap map = node.getAttributes();
            
            for (String key : mNodeInfo.getKeySet()) {
                String targetValue = mNodeInfo.getAttribute(key);
                targetValue = StringEscapeUtils.unescapeHtml(targetValue);

                Node attr = map.getNamedItem(key);
                if(attr==null) return;
                String value = attr.getNodeValue();

                if(!targetValue.equals(value)) return;
            }

            double similarity = -1;
            if(mContent!=null) {
                // try to compare text
                similarity = matchText(node);
                if(similarity > 0.2) return;
                
                if(mResultDiff>=0.0) {
                    if(similarity > mResultDiff) return;    // less similar node
                }
            }

            // found target node
            mResultNode = node;
            mResultDiff = similarity;
        }
        
        private double matchText(Node node) {
            String textContent = removeSpaces(StringEscapeUtils.unescapeHtml(ContentFormatter.format(node)));
            if(textContent.length()==0) return 1.0;
//try {
//    IOUtils.write(textContent+"\n", System.out, "Big5");
//} catch (IOException e) {
//    e.printStackTrace();
//}
            int dist = StringUtils.getLevenshteinDistance(textContent, mContent);
            int total = Math.max(textContent.length(), mContent.length());

            return (double)dist/(double)total;
        }
    }

    private String identifyNodeXPath(Document doc, Node[] nodes) throws AnalyzeException {
        // target: find the most specific and minimum xpath

        XPathNodeChain baseParentChain = null;
        
        for (Node node : nodes) {
            XPathNodeChain parentChain = new XPathNodeChain();
            // identify along parents all id, class attributes
            Node cur = node;
            do {
                if("#document".equals(cur.getNodeName())) break;
                parentChain.add(getXPathNode(cur)); 
            } while((cur = cur.getParentNode())!=null);

            if(baseParentChain==null) {
                baseParentChain = parentChain;
            } else {
                baseParentChain.generalizeWith(parentChain);
            }
        }

        return baseParentChain.generate(); 
    }

    private XPathNode getXPathNode(Node node) {
        String tag = node.getNodeName();

        // calculate level
        int level = 0;
        Node cur = node;
        while((cur=cur.getParentNode())!=null) {
            level++;
        }

        // calculate number of same tag siblings and pos
        int numSameTagSiblings = 0;
        int pos = 1;
        cur = node;
        while((cur=cur.getPreviousSibling())!=null) {
            if(tag.equals(cur.getNodeName())) {
                numSameTagSiblings++;
                pos++;
            }
        }
        cur = node;
        while((cur=cur.getNextSibling())!=null) {
            if(tag.equals(cur.getNodeName())) {
                numSameTagSiblings++;
            }
        }

        // get id / class attributes
        XPathNode xpathNode = new XPathNode(tag, level, numSameTagSiblings, pos);
        NamedNodeMap nodeMap = node.getAttributes();
        if(nodeMap!=null) {
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Node attr = nodeMap.item(i);
                // TODO only get id, class attribute for now
                if("id".equals(attr.getNodeName())) {
                    xpathNode.addIdAttr(attr.getNodeValue());
                } else if("class".equals(attr.getNodeName())) {
                    xpathNode.addClassAttr(attr.getNodeValue());
                }
            }
        }
        
        return xpathNode;
    }
}
