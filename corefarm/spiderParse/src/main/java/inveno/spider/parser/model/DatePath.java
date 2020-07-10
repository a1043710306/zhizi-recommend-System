package inveno.spider.parser.model;


import inveno.spider.parser.base.ParseStrategy.DateExtractionStrategy;
import inveno.spider.parser.base.ParseStrategy.PathStrategy;

public class DatePath extends Path
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private DateExtractionStrategy dateStrategy;
    private String pattern; // used only if dateStrategy = Custom
    private String country;

    public DatePath(PathStrategy strategy, String path,
            DateExtractionStrategy dateStrategy, String pattern,
            String country, String regularExpression, String replaceWith,
            String matchStrategy)
    {
        
        super(strategy, path, regularExpression, replaceWith, matchStrategy);
        this.dateStrategy = dateStrategy;
        this.pattern = pattern;
        this.country = country;
    }

    public DateExtractionStrategy getDateStrategy()
    {
        return dateStrategy;
    }

    public void setDateStrategy(DateExtractionStrategy dateStrategy)
    {
        this.dateStrategy = dateStrategy;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

}
