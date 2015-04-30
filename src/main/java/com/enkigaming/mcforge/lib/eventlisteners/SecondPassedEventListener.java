package com.enkigaming.mcforge.lib.eventlisteners;

import com.enkigaming.mc.lib.compatability.CompatabilityEvents;
import com.enkigaming.mc.lib.compatability.CompatabilityEvents.SecondTickArgs;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SecondPassedEventListener
{
    byte tickCounter = 0;
    int secondCounter = 0;
    
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event)
    {
        if(++tickCounter == 20)
        {
            tickCounter = 0;
            SecondTickArgs args = new SecondTickArgs(++secondCounter);
            CompatabilityEvents.secondPassed.raise(this, args);
            CompatabilityEvents.secondPassed.raisePostEvent(this, args);
        }
    }
}