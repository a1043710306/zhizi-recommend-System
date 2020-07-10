package com.inveno.common.exception;

public class ServerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServerException()
    {
    }

    public ServerException(Throwable e)
    {
        super(e);
    }

    public ServerException(String msg)
    {
        super(msg);
    }

    public ServerException(String msg, Throwable e)
    {
        super(msg, e);
    }

}
