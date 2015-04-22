package com.enkigaming.lib.exceptions;

public class UnableToParseException extends Exception
{
    public UnableToParseException(Object objectUnableToParse)
    {
        this.objectUnableToParse = objectUnableToParse;
    }
    
    public UnableToParseException(Object objectUnableToParse, String message)
    {
        super(message);
        this.objectUnableToParse = objectUnableToParse;
    }
    
    protected final Object objectUnableToParse;
    
    public Object getObjectUnableToParse()
    { return objectUnableToParse; }
}