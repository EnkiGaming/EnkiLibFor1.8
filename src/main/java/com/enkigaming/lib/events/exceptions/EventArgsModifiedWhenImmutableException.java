package com.enkigaming.lib.events.exceptions;

public class EventArgsModifiedWhenImmutableException extends UnsupportedOperationException
{
    public EventArgsModifiedWhenImmutableException() { super(); }
    
    public EventArgsModifiedWhenImmutableException(String message) { super(message); }
    
    public EventArgsModifiedWhenImmutableException(Throwable cause) { super(cause); }
    
    public EventArgsModifiedWhenImmutableException(String message, Throwable cause) { super(message, cause); }
}