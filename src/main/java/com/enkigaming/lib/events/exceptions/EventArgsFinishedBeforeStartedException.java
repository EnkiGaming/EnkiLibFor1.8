package com.enkigaming.lib.events.exceptions;

public class EventArgsFinishedBeforeStartedException extends EventArgsStateException
{
    public EventArgsFinishedBeforeStartedException() { super(); }

    public EventArgsFinishedBeforeStartedException(String message) { super(message); }

    public EventArgsFinishedBeforeStartedException(String message, Throwable cause) { super(message, cause); }

    public EventArgsFinishedBeforeStartedException(Throwable cause) { super(cause); }
}