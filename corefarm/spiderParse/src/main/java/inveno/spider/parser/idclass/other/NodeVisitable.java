package inveno.spider.parser.idclass.other;

import org.w3c.dom.NodeList;

import org.w3c.dom.Node;

public class NodeVisitable {
    private Node mRoot;
    private NodeVisitor mVisitor;
    public NodeVisitable(Node root) {
        mRoot = root;
    }
    public void start(NodeVisitor visitor) {
        mVisitor = visitor;
        visitRecursive(mRoot);
    }
    private void visitRecursive(Node node) {
        mVisitor.visit(node);
        NodeList nodeList = node.getChildNodes();
        if(nodeList==null) return;
        for (int i = 0; i < nodeList.getLength(); i++) {
            visitRecursive(nodeList.item(i));
        }
    }
}

interface NodeVisitor {
    void visit(Node node);
}