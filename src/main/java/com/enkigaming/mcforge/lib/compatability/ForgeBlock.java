package com.enkigaming.mcforge.lib.compatability;

import com.enkigaming.mc.lib.compatability.EnkiBlock;

public class ForgeBlock extends EnkiBlock
{
    public ForgeBlock()
    { super(); }
    
    public ForgeBlock(int x, int z)
    { super(x, z); }
    
    public ForgeBlock(int x, int y, int z)
    { super(x, y, z); }
    
    public ForgeBlock(int worldId, int x, int y, int z)
    { super(worldId, x, y, z); }
}