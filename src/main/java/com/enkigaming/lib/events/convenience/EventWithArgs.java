package com.enkigaming.lib.events.convenience;

import com.enkigaming.lib.events.Event;
import com.enkigaming.lib.events.EventArgs;
import com.enkigaming.lib.tuples.Pair;

public class EventWithArgs<ArgsType extends EventArgs> extends Pair<Event<ArgsType>, ArgsType>
{
    public EventWithArgs(Event<ArgsType> event, ArgsType args)
    { super(event, args); }
    
    public EventWithArgs(Pair<? extends Event<ArgsType>, ? extends ArgsType> otherPair)
    { super(otherPair.getFirst(), otherPair.getSecond()); }
    
    public Event<ArgsType> getEvent()
    { return this.first; }
    
    public ArgsType getArgs()
    { return this.second; }
}