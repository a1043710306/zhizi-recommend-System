package inveno.spider.parser.report;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrawlerEvent {
    public static enum Type {feeds,listing,content,forumContent,messageContent,other,jsonListing,jsonContent}

    private Type mType;
    private String mMessage;
    private String mUrl;
    private String mStacktrace;
    public CrawlerEvent(Type type, String message, String url, Throwable e) {
        mType = type;
        mMessage = message;
        mUrl = url;
        StringWriter s = new StringWriter();
        e.printStackTrace(new PrintWriter(s));
        mStacktrace = s.toString();
    }
    public Type getType() {
        return mType;
    }
    public String getMessage() {
        return mMessage;
    }
    public String getUrl() {
        return mUrl;
    }
    public String getStacktrace() {
        return mStacktrace;
    }
}
