package com.enkigaming.lib.tuples;

public class Singlet<First>
{
    public Singlet(First first)
    { this.first = first; }
    
    protected final First first;
    
    public First getFirst()
    { return first; }
}