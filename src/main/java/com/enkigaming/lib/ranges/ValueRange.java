package com.enkigaming.lib.ranges;

public class ValueRange<T extends Comparable<T>> implements FlatRange<T>
{
    public ValueRange(T min, T max)
    {}
    
    public ValueRange(Range<? extends T> source)
    {}
}