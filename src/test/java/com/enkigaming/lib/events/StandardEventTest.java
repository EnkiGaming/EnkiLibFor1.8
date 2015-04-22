package com.enkigaming.lib.events;

public class StandardEventTest extends EventTest
{
    @Override
    public Event<EventArgs> getNewEvent()
    { return new StandardEvent<EventArgs>(); }

    @Override
    public EventArgs getNewArgs()
    { return new StandardEventArgs(); }
}