package inveno.spider.parser.idclass.other;


import inveno.spider.parser.exception.AnalyzeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class XPathNodeChain {
    List<XPathNode> chain = new ArrayList<XPathNode>();

    public void add(XPathNode node) {
        chain.add(0, node);
    }

    public void generalizeWith(XPathNodeChain that) throws AnalyzeException {
        int maxLevel = Math.max(this.getLevel(), that.getLevel());

        for(int level=0; level<=maxLevel; level++) {
            int thisIndex = this.getIndexByLevel(level);
            int thatIndex = that.getIndexByLevel(level);
            if(thisIndex == -1) {
                continue;

            } else if(thatIndex == -1) {
                chain.remove(thisIndex);

            } else { // both are non null
                XPathNode node = generalize(this.get(thisIndex), that.get(thatIndex));
                if(node==null)
                    chain.remove(thisIndex);
                else
                    chain.set(thisIndex, node);
            }
        }
        if(chain.isEmpty()) {
            throw new AnalyzeException("Unable to generalize with " + that.generate());
        }
    }

    private int getLevel() {
        if(chain.isEmpty()) return 0;
        return chain.get(chain.size()-1).getLevel();
    }

    private XPathNode generalize(XPathNode n1, XPathNode n2) {
        if(!n1.getTag().equals(n2.getTag())) return null;
        n1.getIdAttrs().retainAll(n2.getIdAttrs());
        n1.getClassAttrs().retainAll(n2.getClassAttrs());
        if(n1.getPos()!=n2.getPos()) {
            n1.removePosInfo();
        }
        return n1;
    }

    private XPathNode get(int index) {
        return chain.get(index);
    }

    private int getIndexByLevel(int level) {
        for (int i = 0; i < chain.size(); i++) {
            if(chain.get(i).getLevel()==level) return i;
        }
        return -1;
    }

    public String generate() {
        if(chain.isEmpty()) return "*";

        StringBuilder sb = new StringBuilder();

        int prevLevel = chain.get(0).getLevel();

        for (XPathNode node : chain) {
            sb.append("/");
            
            int nodeLevel = node.getLevel();
            if(nodeLevel-prevLevel>1)
                sb.append("/");
            prevLevel = nodeLevel;

            sb.append(node.getTag());
            
            List<String> conditions = new ArrayList<String>();
            if(!node.getIdAttrs().isEmpty()) {
                for (String value : node.getIdAttrs()) {
                    conditions.add("@id='"+value+"'");
                }
            }
            if(!node.getClassAttrs().isEmpty()) {
                for (String value : node.getClassAttrs()) {
                    conditions.add("@class='"+value+"'");
                }
            }
            if(!conditions.isEmpty()) {
                sb.append("[").append(StringUtils.join(conditions, " and ")).append("]");

            } else if(node.hasPosInfo()) {
                if(node.getNumSameTagSiblings()==node.getPos()-1) {
                    sb.append("[last()]");
                } else {
                    sb.append("[").append(node.getPos()).append("]");
                }
            }
        }
        return sb.toString();
    }
}
