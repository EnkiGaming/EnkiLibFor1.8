package com.enkigaming.mc.lib.compatability;

import com.enkigaming.mc.lib.misc.BlockCoOrdinate;
import java.util.UUID;

public abstract class EnkiPlayer
{
    public abstract UUID getId();
    
    public abstract String getUsername();
    
    public abstract String getDisplayName();
    
    public abstract Object getPlatformSpecificInstance();
    
    public abstract Integer getWorldId();
    
    public EnkiWorld getWorld()
    { return CompatabilityAccess.getWorld(getWorldId()); }
    
    public abstract void teleportTo(int worldId, int x, int y, int z);
    
    public void teleportTo(BlockCoOrdinate destination)
    { this.teleportTo(destination.getWorldId(), destination.getX(), destination.getY(), destination.getZ()); }
}