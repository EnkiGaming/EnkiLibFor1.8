package com.enkigaming.mcforge.lib.compatability;

import com.enkigaming.lib.events.exceptions.NoSuchUsernameException;
import com.enkigaming.mc.lib.compatability.EnkiPlayer;
import com.enkigaming.mcforge.lib.EnkiLib;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class ForgePlayer implements EnkiPlayer
{
    public ForgePlayer(UUID playerId)
    { this.playerId = playerId; }
    
    public ForgePlayer(String Username)
    {
        // Attempt to get UUID from username cache.
        playerId = EnkiLib.getLastRecordedIDForName(Username);
        
        if(playerId != null)
            return;
        
        // Attempt to get UUID from online players.
        List<EntityPlayer> playersOnline
            = new ArrayList<EntityPlayer>(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
        
        for(EntityPlayer i : playersOnline)
            if(i.getGameProfile().getName().equalsIgnoreCase(Username))
            {
                playerId = i.getGameProfile().getId();
                return;
            }
        
        // Give up D:
        throw new NoSuchUsernameException(Username);
    }
    
    public ForgePlayer(EntityPlayer player)
    { playerId = player.getGameProfile().getId(); }
    
    UUID playerId;
    
    @Override
    public UUID getId()
    { return playerId; }

    @Override
    public String getUsername()
    {
        // Attempt to get name from username cache.
        String name = EnkiLib.getLastRecordedNameOf(playerId);
        
        if(name != null)
            return name;
        
        // Attempt to get name from online players.
        List<EntityPlayer> playersOnline
            = new ArrayList<EntityPlayer>(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
        
        for(EntityPlayer i : playersOnline)
            if(i.getGameProfile().getId().equals(playerId))
                return i.getGameProfile().getName();
        
        // Give up D:
        return null;
    }

    @Override
    public String getDisplayName()
    { return getPlatformSpecificInstance().getDisplayName().getUnformattedText(); }

    @Override
    public EntityPlayer getPlatformSpecificInstance()
    {
        List<EntityPlayer> playersOnline
            = new ArrayList<EntityPlayer>(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
        
        for(EntityPlayer i : playersOnline)
            if(i.getGameProfile().getId().equals(playerId))
                return i;
        
        return null;
    }

}