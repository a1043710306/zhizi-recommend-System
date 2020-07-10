package inveno.spider.common.exception;

public class ResourceNotFoundException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MSG="\tResource not found.";
    
    public ResourceNotFoundException()
    {
       super(DEFAULT_MSG); 
    }
    
    public ResourceNotFoundException(String message)
    {
        super(message+DEFAULT_MSG);
    }

}
