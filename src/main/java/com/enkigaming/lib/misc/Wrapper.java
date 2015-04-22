package com.enkigaming.lib.misc;

public class Wrapper<T>
{
    public Wrapper()
    { this(null); }
    
    public Wrapper(T value)
    { this.value = value; }
    
    protected T value;
    
    protected final Object valueLock = new Object();
    
    public T get()
    {
        synchronized(valueLock)
        { return value; }
    }
}