package com.enkigaming.mc.lib.compatability;

public abstract class EnkiWorld
{
    public EnkiWorld(int worldId)
    { this.worldId = worldId; }
    
    final int worldId;
    
    public abstract Object getPlatformSpecificInstance();
    
    public int getWorldId()
    { return worldId; }
    
    public abstract String getName();
}