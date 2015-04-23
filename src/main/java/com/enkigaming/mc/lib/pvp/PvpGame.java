package com.enkigaming.mc.lib.pvp;

import com.enkigaming.lib.events.Event;
import com.enkigaming.lib.events.StandardEvent;
import com.enkigaming.lib.events.StandardEventArgs;
import com.enkigaming.mc.lib.compatability.CompatabilityAccess;
import com.enkigaming.mc.lib.misc.BlockCoOrdinate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class PvpGame
{
    public static enum PlayerGameState
    {
        inLobby,
        inGame
    }
    
    public static class GameState
    {
        public GameState(String name)
        { this.name = name; }
        
        protected String name;
        protected Set<GameState> subStates = new HashSet<GameState>();
        
        public String getName()
        { return name; }
        
        protected void addSubState(GameState state)
        { subStates.add(state); }
        
        protected void addSubStates(GameState... states)
        { Collections.addAll(subStates, states); }
        
        public void addSubStates(Collection<? extends GameState> states)
        { subStates.addAll(states); }
        
        public void addAsSubStateTo(GameState state)
        { state.addSubState(this); }
        
        public boolean hasSubState(GameState state)
        { 
            for(GameState i : subStates)
            {
                if(i == state)
                    return true;
                
                if(i.hasSubState(state))
                    return true;
            }
            
            return false;
        }
    }
    
    public static class GameStates
    {
        public final GameState waitingForNewGame = new GameState("WaitingForNewGame");
        public final GameState inGame = new GameState("InGame");
        
        /*
        New states can be added by declaring them in a subclass of GameStates. Substates (such as "inDeathMatch" being
        a substate of "inGame" may be added and declared substate at the time of declaration with the following as
        example:
        
        public final GameState inDeathMatch = new GameState("InDeathMatch"){{ this.addAsSubStateTo(inGame); }};
        */
    }
    
    public static class PlayerJoinedArgs extends StandardEventArgs
    {
        public PlayerJoinedArgs(UUID playerId, BlockCoOrdinate startingPosition)
        {
            this.playerId = playerId;
            this.startingPosition = startingPosition;
        }
        
        UUID playerId;
        BlockCoOrdinate startingPosition;
        
        final Object startingPositionLock = new Object();
        
        public UUID getPlayerId()
        { return playerId; }
        
        public BlockCoOrdinate getStartingPosition()
        {
            synchronized(startingPositionLock)
            { return startingPosition; }
        }
        
        public BlockCoOrdinate setStartingPosition(BlockCoOrdinate newStartingPosition)
        {
            synchronized(startingPositionLock)
            {
                BlockCoOrdinate temp = startingPosition;
                startingPosition = newStartingPosition;
                return temp;
            }
        }
    }
    
    public PvpGame()
    {
        players = new HashMap<UUID, PlayerGameState>();
        lobbySpawn = new BlockCoOrdinate();
        possibleGameStates = getNewGameStatesObject();
        gameState = possibleGameStates.waitingForNewGame;
    }
    
    Map<UUID, PlayerGameState> players;
    BlockCoOrdinate lobbySpawn;
    GameStates possibleGameStates;
    GameState gameState;
    
    public static final Event<PlayerJoinedArgs> playerJoinedEvent = new StandardEvent<PlayerJoinedArgs>();
    
    public void teleportPlayersToLobby()
    {
        for(UUID playerId : players.keySet())
            CompatabilityAccess.getPlayer(playerId).teleportTo(lobbySpawn);
    }
    
    public void teleportPlayerToLobby(UUID playerId)
    { CompatabilityAccess.getPlayer(playerId).teleportTo(lobbySpawn); }
    
    public GameStates getPossibleGameStates()
    { return possibleGameStates; }
    
    public GameState getCurrentGameState()
    { return gameState; }
    
    public void addPlayer(UUID playerId)
    {
        PlayerJoinedArgs args = new PlayerJoinedArgs(playerId, lobbySpawn);
        playerJoinedEvent.raise(this, args);
        
        try
        {
            players.put(playerId, PlayerGameState.inLobby);
            if(args.getStartingPosition() != null)
                CompatabilityAccess.getPlayer(playerId).teleportTo(args.getStartingPosition());
        }
        finally
        { playerJoinedEvent.raisePostEvent(this, args); }
    }
    
    public void removePlayer(UUID playerId)
    { players.remove(playerId); }
    
    public void removePlayer(UUID playerId, BlockCoOrdinate whereToTpThem)
    {
        removePlayer(playerId);
        CompatabilityAccess.getPlayer(playerId).teleportTo(whereToTpThem);
    }
    
    public abstract BlockCoOrdinate getNewSpawnPointForPlayer(UUID playerId);
    
    protected abstract GameStates getNewGameStatesObject();
}