package com.enkigaming.mc.lib.compatability;

import com.enkigaming.mc.lib.misc.BlockCoOrdinate;

public abstract class EnkiBlock extends BlockCoOrdinate
{
    public EnkiBlock()
    { this(0, 0, 0, 0); }
    
    public EnkiBlock(int x, int z)
    { this(0, x, 0, z); }
    
    public EnkiBlock(int x, int y, int z)
    { this(0, x, y, z); }
    
    public EnkiBlock(int worldId, int x, int y, int z)
    { super(worldId, x, y, z); }
}