package com.enkigaming.lib.tuples;

public class Nonet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth>
    extends Octet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth>
{
    public Nonet(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth, Seventh seventh,
                 Eighth eighth, Ninth ninth)
    {
        super(first, second, third, fourth, fifth, sixth, seventh, eighth);
        this.ninth = ninth;
    }
    
    protected final Ninth ninth;
    
    public Ninth getNinth()
    { return ninth; }
}