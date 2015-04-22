package com.enkigaming.lib.tuples;

public class ContinuingTuple<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth, Next extends Singlet>
    extends Decet<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth>
{
    public ContinuingTuple(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth,
                           Seventh seventh, Eighth eighth, Ninth ninth, Tenth tenth, Next next)
    {
        super(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
        this.next = next;
    }
    
    protected final Next next;
    
    public Next getNext()
    { return next; }
}