package com.enkigaming.lib.events.exceptions;

public class EventArgsStateException extends RuntimeException
{
    public EventArgsStateException() { super(); }

    public EventArgsStateException(String message) { super(message); }

    public EventArgsStateException(String message, Throwable cause) { super(message, cause); }

    public EventArgsStateException(Throwable cause) { super(cause); }
}