package com.enkigaming.lib.extensions;

import com.enkigaming.lib.exceptions.NullArgumentException;

/**
 * A repository of standalone methods pertaining to implementors of Comparable<T> and objects that can be compared using
 * the comparing operators. Most should really be extension methods, if Java supported them.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public class ComparableMethods
{
    public static <T extends Comparable<T>> T getSmallestOf(T... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        for(int i = 0; i < values.length; i++)
        {
            if(values[i] != null)
                break;
            
            if(i == values.length - 1)
                throw new IllegalArgumentException("All members of the passed array were null.");
        }
        
        T smallest = null;
        
        for(T i : values)
            if(i != null && i.compareTo(smallest) < 0)
                smallest = i;
        
        return smallest;
    }
    
    public static byte getSmallestOfPrimitives(byte... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        byte smallest = Byte.MAX_VALUE;
        
        for(byte i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static short getSmallestOfPrimitives(short... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        short smallest = Short.MAX_VALUE;
        
        for(short i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static int getSmallestOfPrimitives(int... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        int smallest = Integer.MAX_VALUE;
        
        for(int i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static long getSmallestOfPrimitives(long... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        long smallest = Long.MAX_VALUE;
        
        for(long i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static float getSmallestOfPrimitives(float... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        float smallest = Float.MAX_VALUE;
        
        for(float i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static double getSmallestOfPrimitives(double... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        double smallest = Double.MAX_VALUE;
        
        for(double i : values)
            if(i < smallest)
                smallest = i;
        
        return smallest;
    }
    
    public static <T extends Comparable<T>> T getLargestOf(T... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        for(int i = 0; i < values.length; i++)
        {
            if(values[i] != null)
                break;
            
            if(i == values.length - 1)
                throw new IllegalArgumentException("All members of the passed array were null.");
        }
        
        T largest = null;
        
        for(T i : values)
            if(i.compareTo(largest) > 0)
                largest = i;
        
        return largest;
    }
    
    public static byte getLargestOfPrimitives(byte... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        byte largest = Byte.MIN_VALUE;
        
        for(byte i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
    
    public static short getLargestOfPrimitives(short... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        short largest = Short.MIN_VALUE;
        
        for(short i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
    
    public static int getLargestOfPrimitives(int... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        int largest = Integer.MIN_VALUE;
        
        for(int i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
    
    public static long getLargestOfPrimitives(long... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        long largest = Long.MIN_VALUE;
        
        for(long i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
    
    public static float getLargestOfPrimitives(float... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        float largest = Float.MIN_VALUE;
        
        for(float i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
    
    public static double getLargestOfPrimitives(double... values)
    {
        if(values == null)
            throw new NullArgumentException();
        
        double largest = Double.MIN_VALUE;
        
        for(double i : values)
            if(i > largest)
                largest = i;
        
        return largest;
    }
}
