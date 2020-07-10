package inveno.spider.parser.model;

import java.io.Serializable;

public class ManualRedirectConfig implements Serializable
{
    public static enum Strategy
    {
        content, url
    }

    private String ifMathRedirect;
    private Strategy redirectStrategy;

    private String regularExpression;
    private String replaceWith;
    
    private boolean unescapeHtml=false;

    /**
     * The default value is <code>true</code>.If the value is true,then,the
     * header of content's link must contain refer url,otherwise mustn't be
     * contain.
     * 
     */
    private boolean contentAddRefererUrl=true;

    public ManualRedirectConfig()
    {
    }

    public ManualRedirectConfig(String ifMathRedirect,
            String redirectStrategy, String regularExpression,
            String replaceWith)
    {
        this.ifMathRedirect = ifMathRedirect;
        setRedirectStrategy(redirectStrategy);
        this.regularExpression = regularExpression;
        this.replaceWith = replaceWith;
        this.contentAddRefererUrl=true;
    }
    
    /**
     * 
     * @param ifMathRedirect
     * @param redirectStrategy
     * @param regularExpression
     * @param replaceWith
     * @param contentAddRefererUrl
     */
    public ManualRedirectConfig(String ifMathRedirect,
            String redirectStrategy, String regularExpression,
            String replaceWith,boolean contentAddRefererUrl,
            boolean isUnescapeHtml)
    {
        this.ifMathRedirect = ifMathRedirect;
        setRedirectStrategy(redirectStrategy);
        this.regularExpression = regularExpression;
        this.replaceWith = replaceWith;
        this.contentAddRefererUrl = contentAddRefererUrl;
        this.setUnescapeHtml(isUnescapeHtml);
    }

    public String getIfMathRedirect()
    {
        return ifMathRedirect;
    }

    public Strategy getRedirectStrategy()
    {
        return redirectStrategy;
    }

    public String getRegularExpression()
    {
        return regularExpression;
    }

    public String getReplaceWith()
    {
        return replaceWith;
    }

    public boolean isContentAddRefererUrl()
    {
        return contentAddRefererUrl;
    }

    public void setContentAddRefererUrl(boolean contentAddRefererUrl)
    {
        this.contentAddRefererUrl = contentAddRefererUrl;
    }

    public void setIfMathRedirect(String ifMathRedirect)
    {
        this.ifMathRedirect = ifMathRedirect;
    }

    public void setRedirectStrategy(String redirectStrategy)
    {
        this.redirectStrategy = "url".equals(redirectStrategy) ? Strategy.url
                : Strategy.content;
    }

    public void setRegularExpression(String regularExpression)
    {
        this.regularExpression = regularExpression;
    }

    public void setReplaceWith(String replaceWith)
    {
        this.replaceWith = replaceWith;
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
