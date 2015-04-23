package com.enkigaming.lib.misc.coordinates;

public class CoOrdinate2d implements XYCoOrdPair
{
    public CoOrdinate2d(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    final int x, y;

    @Override
    public int getX()
    { return x; }

    @Override
    public int getY()
    { return y; }
}