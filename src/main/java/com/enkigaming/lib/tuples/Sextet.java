package com.enkigaming.lib.tuples;

public class Sextet<First, Second, Third, Fourth, Fifth, Sixth> extends Quintet<First, Second, Third, Fourth, Fifth>
{
    public Sextet(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth)
    {
        super(first, second, third, fourth, fifth);
        this.sixth = sixth;
    }
    
    protected final Sixth sixth;
    
    public Sixth getSixth()
    { return sixth; }
}