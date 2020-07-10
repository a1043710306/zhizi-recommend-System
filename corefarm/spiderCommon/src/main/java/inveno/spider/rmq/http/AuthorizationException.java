package inveno.spider.rmq.http;

/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public class AuthorizationException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public AuthorizationException()
    {
        super("Authorization error,please check it try again.");
    }

    /**
     * @param message
     */
    public AuthorizationException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public AuthorizationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public AuthorizationException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
