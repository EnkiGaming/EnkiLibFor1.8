package com.enkigaming.mc.lib.misc;

import com.enkigaming.lib.misc.coordinates.CoOrdinate3d;
import com.enkigaming.mc.lib.compatability.CompatabilityAccess;
import com.enkigaming.mc.lib.compatability.EnkiWorld;

public class BlockCoOrdinate extends CoOrdinate3d
{
    public BlockCoOrdinate()
    { this(0, 0, 0, 0); }
    
    public BlockCoOrdinate(int x, int z)
    { this(0, x, 0, z); }
    
    public BlockCoOrdinate(int x, int y, int z)
    { this(0, x, y, z); }
    
    public BlockCoOrdinate(int worldId, int x, int y, int z)
    {
        super(x, y, z);
        this.worldId = worldId;
    }
    
    final int worldId;
    
    public int getWorldId()
    { return worldId; }
    
    public EnkiWorld getWorld()
    { return CompatabilityAccess.getWorld(worldId); }
}