package com.enkigaming.mcforge.lib.eventlisteners;

import com.enkigaming.mcforge.lib.EnkiLib;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerLogInForCachingEventListener
{
    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        EnkiLib.getInstance().getUsernameCache().recordUsername(event.player.getGameProfile().getId(),
                                                                event.player.getGameProfile().getName());
    }
}