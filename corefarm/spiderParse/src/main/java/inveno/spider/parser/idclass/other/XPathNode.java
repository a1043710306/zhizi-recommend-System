package inveno.spider.parser.idclass.other;

import java.util.SortedSet;
import java.util.TreeSet;

public class XPathNode {
    private String mTag;
    private int mLevel;
    private int mNumSameTagSiblings;
    private int mPos;
    private boolean mHasPosInfo;
    private SortedSet<String> mIdAttrs;   // should normally just have one
    private SortedSet<String> mClassAttrs;    // should normally just have one
    public XPathNode(String tag, int level, int numSameTagSiblings, int pos) {
        mTag = tag;
        mLevel = level;
        mNumSameTagSiblings = numSameTagSiblings;
        mPos = pos;
        mHasPosInfo = numSameTagSiblings>0;
        mIdAttrs = new TreeSet<String>();
        mClassAttrs = new TreeSet<String>();
    }
    public String getTag() {
        return mTag;
    }
    public int getLevel() {
        return mLevel;
    }
    public SortedSet<String> getIdAttrs() {
        return mIdAttrs;
    }
    public SortedSet<String> getClassAttrs() {
        return mClassAttrs;
    }
    public int getNumSameTagSiblings() {
        return mNumSameTagSiblings;
    }
    public int getPos() {
        return mPos;
    }
    public void addIdAttr(String value) {
        mIdAttrs.add(value);
    }
    public void addClassAttr(String value) {
        mClassAttrs.add(value);
    }
    public boolean hasPosInfo() {
        return mHasPosInfo;
    }
    public void removePosInfo() {
        mHasPosInfo = false;
    }
}
