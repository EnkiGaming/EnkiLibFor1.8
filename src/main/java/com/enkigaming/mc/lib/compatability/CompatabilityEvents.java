package com.enkigaming.mc.lib.compatability;

import com.enkigaming.lib.events.Event;
import com.enkigaming.lib.events.StandardEvent;
import com.enkigaming.lib.events.StandardEventArgs;

public class CompatabilityEvents
{
    public static class SecondTickArgs extends StandardEventArgs
    {
        public SecondTickArgs(int secondCount)
        { this.secondCount = secondCount; }
        
        int secondCount;
        
        /**
         * Gets the number of seconds that have passed since the number of seconds started being counted. Ideally, the
         * number of in-game seconds that have passed since the server started. This may differ slightly from real
         * seconds, if the server this is being run on has a slow rate of ticks per second. (ideally, 20)
         * @return The number of in-game seconds that have passed since the server started.
         */
        public int getSecondCount()
        { return secondCount; }
    }
    
    /**
     * Is raised once every second, according to in-game ticks. For Minecraft normally, this is once every 20 ticks
     */
    public static Event<SecondTickArgs> secondPassed = new StandardEvent<SecondTickArgs>();
}