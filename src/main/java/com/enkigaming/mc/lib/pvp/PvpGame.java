package com.enkigaming.mc.lib.pvp;

import com.enkigaming.lib.events.Event;
import com.enkigaming.lib.events.StandardEvent;
import com.enkigaming.lib.events.StandardEventArgs;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpGame
{
    public static enum PlayerGameState
    {
        inLobby,
        inGame
    }
    
    public static class PlayerJoinedArgs extends StandardEventArgs
    {
        
    }
    
    public PvpGame()
    {
        players = new HashMap<UUID, PlayerGameState>();
    }
    
    Map<UUID, PlayerGameState> players;
    
    public static final Event<PlayerJoinedArgs> playerJoinedEvent = new StandardEvent<PlayerJoinedArgs>();
}