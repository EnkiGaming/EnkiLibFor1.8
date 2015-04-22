package com.enkigaming.lib.events.exceptions;

public class EventArgsUsedPostBeforePreException extends EventArgsStateException
{
    public EventArgsUsedPostBeforePreException() { super(); }

    public EventArgsUsedPostBeforePreException(String message) { super(message); }

    public EventArgsUsedPostBeforePreException(String message, Throwable cause) { super(message, cause); }

    public EventArgsUsedPostBeforePreException(Throwable cause) { super(cause); }
}