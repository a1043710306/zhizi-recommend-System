package inveno.spider.common.model;

public class Browser
{
    public enum Type
    {
        MSIE6, MSIE7, MSIE8,FIREFOX
    }

    public static final Browser.Type convert(String src)
    {
        for (Type type : Type.values())
        {
            if (type.name().equalsIgnoreCase(src))
            {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown browser type " + src);
    }

}
