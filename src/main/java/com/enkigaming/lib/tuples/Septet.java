package com.enkigaming.lib.tuples;

public class Septet<First, Second, Third, Fourth, Fifth, Sixth, Seventh>
    extends Sextet<First, Second, Third, Fourth, Fifth, Sixth>
{
    public Septet(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth, Seventh seventh)
    {
        super(first, second, third, fourth, fifth, sixth);
        this.seventh = seventh;
    }
    
    protected final Seventh seventh;
    
    public Seventh getSeventh()
    { return seventh; }
}