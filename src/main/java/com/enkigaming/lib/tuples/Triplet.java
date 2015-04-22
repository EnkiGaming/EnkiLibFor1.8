package com.enkigaming.lib.tuples;

public class Triplet<First, Second, Third> extends Pair<First, Second>
{
    public Triplet(First first, Second second, Third third)
    {
        super(first, second);
        this.third = third;
    }
    
    protected final Third third;
    
    public Third getThird()
    { return third; }
}