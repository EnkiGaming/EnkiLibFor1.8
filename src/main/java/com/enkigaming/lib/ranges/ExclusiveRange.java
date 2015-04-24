package com.enkigaming.lib.ranges;

import java.util.Collection;

public class ExclusiveRange<T extends Comparable<T>> implements Range<T>
{
    public ExclusiveRange(T min, T max)
    {}
    
    public ExclusiveRange(Range<? extends T> source)
    {}
    
    public ExclusiveRange(Range<? extends T>... sources)
    {}
    
    public ExclusiveRange(Collection<? extends Range<? extends T>> sources)
    {}
    
    public ExclusiveRange(T min, T max, Range<? extends T> toExclude)
    {}
    
    public ExclusiveRange(T min, T max, Range<? extends T>... toExclude)
    {}
    
    public ExclusiveRange(T min, T max, Collection<? extends Range<? extends T>> toExclude)
    {}
}