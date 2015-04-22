package com.enkigaming.lib.tuples;

public class Pair<First, Second> extends Singlet<First>
{
    public Pair(First first, Second second)
    {
        super(first);
        this.second = second;
    }
    
    protected final Second second;
    
    public Second getSecond()
    { return second; }
}