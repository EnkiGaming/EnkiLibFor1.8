package com.enkigaming.lib.tuples;

public class Octet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth>
    extends Septet<First, Second, Third, Fourth, Fifth, Sixth, Seventh>
{
    public Octet(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth, Seventh seventh,
                 Eighth eighth)
    {
        super(first, second, third, fourth, fifth, sixth, seventh);
        this.eighth = eighth;
    }
    
    protected final Eighth eighth;
    
    public Eighth getEighth()
    { return eighth; }
}