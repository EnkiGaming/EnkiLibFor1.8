package com.enkigaming.mc.lib.compatability;

import java.util.UUID;

/* This isn't pretty, but at least it allows good integration with multiple platforms without changing any of the code
   of projects that use it. Maybe rename this later on to something more succinct? */
public class CompatabilityAccess
{
    public static interface Getter
    {
        EnkiPlayer getPlayer(UUID playerId);
        EnkiBlock getBlock(int worldId, int x, int y, int z);
        EnkiWorld getWorld(int worldId);
    }
    
    static Getter getter;
    
    public static EnkiPlayer getPlayer(UUID playerId)
    { return getter.getPlayer(playerId); }
    
    public static EnkiBlock getBlock(int worldId, int x, int y, int z)
    { return getter.getBlock(worldId, x, y, z); }
    
    public static EnkiWorld getWorld(int worldId)
    { return getter.getWorld(worldId); }
    
    public static void setGetter(Getter newGetter)
    { getter = newGetter; }
}