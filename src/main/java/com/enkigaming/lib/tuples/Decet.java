package com.enkigaming.lib.tuples;

public class Decet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth>
    extends Nonet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth>
{
    public Decet(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth, Seventh seventh,
                 Eighth eighth, Ninth ninth, Tenth tenth)
    {
        super(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
        this.tenth = tenth;
    }
    
    protected final Tenth tenth;
    
    public Tenth getTenth()
    { return tenth; }
}