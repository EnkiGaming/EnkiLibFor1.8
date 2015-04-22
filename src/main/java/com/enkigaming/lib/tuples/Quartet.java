package com.enkigaming.lib.tuples;

public class Quartet<First, Second, Third, Fourth> extends Triplet<First, Second, Third>
{
    public Quartet(First first, Second second, Third third, Fourth fourth)
    {
        super(first, second, third);
        this.fourth = fourth;
    }
    
    protected final Fourth fourth;
    
    public Fourth getFourth()
    { return fourth; }
}