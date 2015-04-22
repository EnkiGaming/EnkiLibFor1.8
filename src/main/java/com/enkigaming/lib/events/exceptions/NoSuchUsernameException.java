package com.enkigaming.lib.events.exceptions;

public class NoSuchUsernameException extends RuntimeException
{
    public NoSuchUsernameException(String name)
    {
        super();
        this.name = name;
    }

    public NoSuchUsernameException(String name, String message)
    {
        super(message);
        this.name = name;
    }
    
    public NoSuchUsernameException(String name, Throwable cause)
    {
        super(cause);
        this.name = name;
    }
    
    public NoSuchUsernameException(String name, String message, Throwable cause)
    {
        super(message, cause);
        this.name = name;
    }
    
    String name;
    
    public String getName()
    { return name; }
}