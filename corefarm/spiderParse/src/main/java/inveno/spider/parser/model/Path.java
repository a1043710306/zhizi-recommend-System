package inveno.spider.parser.model;

import java.io.Serializable;

import inveno.spider.parser.base.ParseStrategy.PathStrategy;

public class Path implements Serializable {
    private PathStrategy mStrategy;
    private String mPath;
    private String mRegularExpression;
    private String mReplaceWith;
    private String mMatchStrategy;
    
  //Title by a multi-part composition
    private String additionalPath;
    
    private boolean unescapeHtml=true;

    public Path(PathStrategy strategy, String path, String regularExpression, String replaceWith, String matchStrategy) {
        mStrategy = strategy;
        mPath = path;
        mRegularExpression = regularExpression;
        mReplaceWith = replaceWith;
        mMatchStrategy = matchStrategy;
    }
    
    //Title path
    public Path(PathStrategy strategy, String path, String regularExpression,
            String replaceWith, String matchStrategy,String additionalPath)
    {
        this(strategy,path,regularExpression,replaceWith,matchStrategy);
        this.additionalPath = additionalPath;
    }
    
    //Link path
    public Path(PathStrategy strategy, String path, String regularExpression,
            String replaceWith, String matchStrategy,boolean unescapeHtml)
    {
        this(strategy,path,regularExpression,replaceWith,matchStrategy);
        this.setUnescapeHtml(unescapeHtml);
    }
    
    
    public PathStrategy getStrategy() {
        return mStrategy;
    }
    public void setStrategy(PathStrategy strategy){
    	this.mStrategy = strategy;
    }
    public String getPath() {
        return mPath;
    }
    public void setPath(String path){
    	this.mPath = path;
    }
    public String getRegularExpression() {
        return mRegularExpression;
    }
    public void setRegularExpression(String regularExpression) {
        mRegularExpression = regularExpression;
    }
    public String getReplaceWith() {
        return mReplaceWith;
    }
    public void setReplaceWith(String replaceWith) {
        mReplaceWith = replaceWith;
    }
	public String getMatchStrategy() {
		return mMatchStrategy;
	}
	public void setMatchStrategy(String matchStrategy) {
		mMatchStrategy = matchStrategy;
	}
    public String getAdditionalPath()
    {
        return additionalPath;
    }
    public void setAdditionalPath(String additionalPath)
    {
        this.additionalPath = additionalPath;
    }

    public boolean isUnescapeHtml()
    {
        return unescapeHtml;
    }

    public void setUnescapeHtml(boolean unescapeHtml)
    {
        this.unescapeHtml = unescapeHtml;
    }
    
}
