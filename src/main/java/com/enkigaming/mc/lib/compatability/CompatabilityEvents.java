package com.enkigaming.mc.lib.compatability;

import com.enkigaming.lib.events.Event;
import com.enkigaming.lib.events.StandardEvent;
import com.enkigaming.lib.events.StandardEventArgs;

public class CompatabilityEvents
{
    public static class SecondTickArgs extends StandardEventArgs
    {  }
    
    /**
     * Is raised once every second, according to in-game ticks. For Minecraft normally, this is once every 20 ticks
     */
    public static Event<SecondTickArgs> secondPassed = new StandardEvent<SecondTickArgs>();
}