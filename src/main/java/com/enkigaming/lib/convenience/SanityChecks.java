package com.enkigaming.lib.convenience;

import com.enkigaming.lib.exceptions.NullArgumentException;
import com.enkigaming.lib.tuples.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;

public class SanityChecks
{
    public static interface DeepNullChecker
    { void check(Object toCheck, String msg); }
    
    public static void nullCheck(Collection<? extends Pair<? extends Object, ? extends String>> nullables)
    {
        for(Pair<? extends Object, ? extends String> i : nullables)
            if(i.getFirst() == null)
                throw new NullArgumentException(i.getSecond());
    }
    
    public static void nullCheck(Map<? extends String, ? extends Object> nullables)
    {
        for(Map.Entry<? extends String, ? extends Object> i : nullables.entrySet())
            if(i.getValue() == null)
                throw new NullArgumentException(i.getKey());
    }
    
    public static void nullCheck(Pair<? extends Object, ? extends String>... nullables)
    {
        for(Pair<? extends Object, ? extends String> i : nullables)
            if(i.getFirst() == null)
                throw new NullArgumentException(i.getSecond());
    }
    
    public static void nullCheck(Object[]... nullables)
    {
        for(Object[] i : nullables)
        {
            if(i.length < 2)
                throw new IllegalArgumentException("Incomplete member of nullables.");
            
            if(i[0] == null)
                throw new NullArgumentException(i[1].toString());
        }
    }
    
    static DeepNullChecker[] deepNullCheckers = new DeepNullChecker[]
    {
        new DeepNullChecker() // List
        {
            @Override
            public void check(Object toCheck, String msg)
            {
                if(toCheck instanceof List)
                {
                    Collection<Pair<Object, String>> lowerNullables = new HashSet<Pair<Object, String>>();

                    for(int i = 0; i < ((List)toCheck).size(); i++)
                        lowerNullables.add(new Pair<Object, String>(((List)toCheck).get(i),
                                           msg + "[" + i + "]"));

                    deepNullCheck(lowerNullables);
                }
            }
        },
        
        new DeepNullChecker() // Array
        {
            @Override
            public void check(Object toCheck, String msg)
            {
                if(toCheck instanceof Object[])
                {
                    Collection<Pair<Object, String>> lowerNullables = new HashSet<Pair<Object, String>>();

                    for(int i = 0; i < ((Object[])toCheck).length; i++)
                        lowerNullables.add(new Pair<Object, String>(((Object[])toCheck)[i],
                                           msg + "[" + i + "]"));

                    deepNullCheck(lowerNullables);
                }
            }
        },
        
        new DeepNullChecker() // Map
        {
            @Override
            public void check(Object toCheck, String msg)
            {
                if(toCheck instanceof Map)
                {
                    Collection<Pair<Object, String>> lowerNullables = new HashSet<Pair<Object, String>>();
                    
                    for(Object i : ((Map)toCheck).entrySet())
                        lowerNullables.add(new Pair<Object, String>(((Map.Entry)i).getValue(),
                                           msg + "[" + ((Map.Entry)i).getKey().toString() + "]"));
                    
                    deepNullCheck(lowerNullables);
                }
            }
        },
        
        new DeepNullChecker() // Collection
        {
            @Override
            public void check(Object toCheck, String msg)
            {
                if(toCheck instanceof Collection)
                {
                    Collection<Pair<Object, String>> lowerNullables = new HashSet<Pair<Object, String>>();
                    
                    for(Object i : (Collection)toCheck)
                        lowerNullables.add(new Pair<Object, String>(i, msg + "[?]"));
                    
                    deepNullCheck(lowerNullables);
                }
            }
        }
    };
    
    static void deepNullCheckObject(Object obj, String msg)
    {
        for(DeepNullChecker i : deepNullCheckers)
            i.check(obj, msg);
    }
    
    public static void deepNullCheck(Collection<? extends Pair<? extends Object, ? extends String>> nullables)
    {
        for(Pair<? extends Object, ? extends String> i : nullables)
        {
            if(i.getFirst() == null)
                throw new NullArgumentException(i.getSecond());
            
            deepNullCheckObject(i.getFirst(), i.getSecond());
        }
    }
    
    public static void deepNullCheck(Map<? extends String, ? extends Object> nullables)
    {
        for(Map.Entry<? extends String, ? extends Object> i : nullables.entrySet())
        {
            if(i.getValue() == null)
                throw new NullArgumentException(i.getKey());
            
            deepNullCheckObject(i.getValue(), i.getKey());
        }
    }
    
    public static void deepNullCheck(Pair<? extends Object, ? extends String>... nullables)
    {
        for(Pair<? extends Object, ? extends String> i : nullables)
        {
            if(i.getFirst() == null)
                throw new NullArgumentException(i.getSecond());
            
            deepNullCheckObject(i.getFirst(), i.getSecond());
        }
    }
    
    public static void deepNullCheck(Object[]... nullables)
    {
        for(int i = 0; i < nullables.length; i++)
        {
            if(nullables[i].length < 2)
                throw new IllegalArgumentException("Incomplete member of nullables. [" + i + "]");
            
            if(nullables[i][0] == null)
                throw new NullArgumentException(nullables[i][1].toString());
            
            deepNullCheckObject(nullables[i][0], nullables[i][1].toString());
        }
    }
}