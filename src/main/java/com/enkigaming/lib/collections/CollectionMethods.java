package com.enkigaming.lib.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CollectionMethods
{
    public static <T> Collection<T> combineCollection(Collection<? extends T>... collections)
    { return combineCollections(true, collections); }
    
    public static <T> Collection<T> combineCollections(boolean allowDuplicates, Collection<? extends T>... collections)
    {
        Collection<T> combinedCollection;
        
        if(allowDuplicates)
            combinedCollection = new ArrayList<T>();
        else
            combinedCollection = new HashSet<T>();
        
        for(Collection<? extends T> i : collections)
            combinedCollection.addAll(i);
        
        return combinedCollection;
    }
}