package com.enkigaming.lib.events;

public class StandardEventArgsTest extends EventArgsTest
{
    @Override
    public EventArgs getNewArgs()
    { return new StandardEventArgs(); }

    @Override
    public Event<EventArgs> getNewEvent()
    { return new StandardEvent<EventArgs>(); }
}