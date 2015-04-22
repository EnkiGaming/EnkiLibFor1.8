package com.enkigaming.lib.tuples;

public class Quintet <First, Second, Third, Fourth, Fifth> extends Quartet<First, Second, Third, Fourth>
{
    public Quintet(First first, Second second, Third third, Fourth fourth, Fifth fifth)
    {
        super(first, second, third, fourth);
        this.fifth = fifth;
    }
    
    protected final Fifth fifth;
    
    public Fifth getFifth()
    { return fifth; }
}