package inveno.spider.common.exception;

public class ProfileAlreadyExistsException extends Exception
{
    private static final String PROFILE_EXISTS_MSG="The profile %s already exists in the server_%s,"
            .concat("if you want to upload successful, please delete it on that server.");

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public ProfileAlreadyExistsException()
    {
    }
    public ProfileAlreadyExistsException(String profile,String machineID)
    {
       this(String.format(PROFILE_EXISTS_MSG, profile,machineID)); 
    }

    /**
     * @param message
     */
    public ProfileAlreadyExistsException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public ProfileAlreadyExistsException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ProfileAlreadyExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
}
