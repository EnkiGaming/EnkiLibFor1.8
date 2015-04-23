package com.enkigaming.lib.misc.coordinates;

public class CoOrdinate3d implements XYZCoOrdSet
{
    public CoOrdinate3d(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    final int x, y, z;

    @Override
    public int getX()
    { return x; }

    @Override
    public int getY()
    { return y; }

    @Override
    public int getZ()
    { return z; }
}