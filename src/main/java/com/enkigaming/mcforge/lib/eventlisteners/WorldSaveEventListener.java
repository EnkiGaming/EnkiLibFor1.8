package com.enkigaming.mcforge.lib.eventlisteners;

import com.enkigaming.mcforge.lib.EnkiLib;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldSaveEventListener
{
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    { EnkiLib.getInstance().getFileHandling().save(); }
}