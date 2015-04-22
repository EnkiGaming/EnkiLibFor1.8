package com.enkigaming.mc.lib.compatability;

public abstract class EnkiBlock
{
    public EnkiBlock()
    { this(0, 0, 0, 0); }
    
    public EnkiBlock(int x, int z)
    { this(0, x, 0, z); }
    
    public EnkiBlock(int x, int y, int z)
    { this(0, x, y, z); }
    
    public EnkiBlock(int worldId, int x, int y, int z)
    {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    final int worldId, x, y, z;
    
    public int getX()
    { return x; }
    
    public int getY()
    { return y; }
    
    public int getZ()
    { return z; }
    
    public int getWorldId()
    { return worldId; }
}