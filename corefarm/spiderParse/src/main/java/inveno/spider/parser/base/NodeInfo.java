package inveno.spider.parser.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeInfo {
    private String mTag;
    private String mText;
    private Map<String, String> mAttrs;

    public NodeInfo(String tag) {
        this(tag, null);
    }
    
    public NodeInfo(String tag, String text) {
        mTag = tag;
        mText = text;
        mAttrs = new HashMap<String, String>();
    }
    public String getText() {
        return mText;
    }
    public void setText(String text) {
        mText = text;
    }
    public String getTag() {
        return mTag;
    }
    public String getAttribute(String key) {
        return mAttrs.get(key);
    }
    public void putAttribute(String key, String value) {
        mAttrs.put(key, value);
    }
    public Set<String> getKeySet() {
        return mAttrs.keySet();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mTag);
        sb.append(", ").append(mText);
        for (Map.Entry<String, String> entry : mAttrs.entrySet()) {
            sb.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
