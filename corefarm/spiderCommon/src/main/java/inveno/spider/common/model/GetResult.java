package inveno.spider.common.model;

import java.io.Serializable;

public class GetResult implements Serializable
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum ContextKey
    {
        exception
    }

    private boolean mSuccess;
    private long mStart;
    private long mEnd;

    private Exception exception;

    public GetResult(boolean success, long start, long end, Exception exception)
    {
        mSuccess = success;
        mStart = start;
        mEnd = end;
        this.exception = exception;
    }

    public long getDuration()
    {
        return mEnd - mStart;
    }

    public boolean isSuccess()
    {
        return mSuccess;
    }

    public long getEnd()
    {
        return mEnd;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }
}
