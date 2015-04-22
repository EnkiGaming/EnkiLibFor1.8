package com.enkigaming.mc.lib.compatability;

import java.util.UUID;

public class CompatabilityAccess
{
    public static interface Getter
    {
        EnkiPlayer getPlayer(UUID playerId);
        EnkiBlock getBlock(int worldId, int x, int y, int z);
    }
    
    static Getter getter;
    
    public static EnkiPlayer getPlayer(UUID playerId)
    { return getter.getPlayer(playerId); }
    
    public static EnkiBlock getBlock(int worldId, int x, int y, int z)
    { return getter.getBlock(worldId, x, y, z); }
    
    public static void setGetter(Getter newGetter)
    { getter = newGetter; }
}