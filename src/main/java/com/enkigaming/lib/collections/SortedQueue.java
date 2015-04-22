package com.enkigaming.lib.collections;

import com.enkigaming.lib.encapsulatedfunctions.Transformer;
import com.enkigaming.lib.misc.SortedListHandler;
import com.enkigaming.lib.exceptions.NullArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A queue ordered using a comparable derived from the contained objects using a Lambda object to get a comparable.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @param <T> The type of the objects to be contained in the queue.
 */
public class SortedQueue<T> implements Queue<T>
{
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Constructor. Generates the queue with no values.
     * @param keyGetter The Lambda object used to derive the comparable from contained objects in order to order them.
     */
    public SortedQueue(Transformer<T, Comparable> keyGetter)
    {
        members = new ArrayList<T>();
        //this.keyGetter = keyGetter;
        membersHandler = new SortedListHandler<T>(keyGetter);
    }
    
    /**
     * Constructor. Generates the queue with the passed values.
     * @param keyGetter The Lambda object used to derive the comparable from contained objects in order to order them.
     * @param members The objects to pre-fill this queue with.
     */
    public SortedQueue(Transformer<T, Comparable> keyGetter, T... members)
    { this(keyGetter, Arrays.asList(members)); }
    
    /**
     * Constructor. Generates the queue with the passed values.
     * @param keyGetter The Lambda object used to derive the comparable from contained objects in order to order them.
     * @param members The objects to pre-fill this queue with.
     */
    public SortedQueue(Transformer<T, Comparable> keyGetter, Collection<? extends T> members)
    {
        //this.keyGetter = keyGetter;
        membersHandler = new SortedListHandler<T>(keyGetter);
        this.members = membersHandler.quickSort(new ArrayList<T>(members));
    }
    
    /**
     * Constructor. Generates the queue with the passed values.
     * @param keyGetter The Lambda object used to derive the comparable from contained objects in order to order them.
     * @param members The objects to pre-fill this queue with.
     */
    public SortedQueue(T[] members, Transformer<T, Comparable> keyGetter)
    { this(keyGetter, Arrays.asList(members)); }
    
    /**
     * Constructor. Generates the queue with the passed values.
     * @param keyGetter The Lambda object used to derive the comparable from contained objects in order to order them.
     * @param members The objects to pre-fill this queue with.
     */
    public SortedQueue(Collection<? extends T> members, Transformer<T, Comparable> keyGetter)
    { this(keyGetter, members); }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * The contents of the queue.
     */
    final List<T> members;
    
    /**
     * The handler used for sorting/searching the members list.
     */
    final SortedListHandler<T> membersHandler;
    
    /**
     * The lambda object used for extracting the key by this the queue is sorted from the individual members.
     */
    //final Transformer<T, Comparable> keyGetter;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods">
    //<editor-fold defaultstate="collapsed" desc="Next value getters/poppers">
    /**
     * Gets the next value in the queue. Used by remove, peek, poll, and element.
     * @param remove Whether or not the remove the next item in the queue from the queue, as well as return it.
     * @return The next item in the queue.
     * @throws NoSuchElementException if the queue is empty.
     */
    public T get(boolean remove)
    {
        synchronized(members)
        {
            if(members.size() <= 0)
                throw new NoSuchElementException();
            
            if(remove)
                return members.remove(0);
            else
                return members.get(0);
        }
    }
    
    @Override
    public T remove()
    { return get(true); }
    
    @Override
    public T poll()
    {
        try
        { return get(true); }
        catch(NoSuchElementException e)
        { return null; }
    }
    
    @Override
    public T element()
    { return get(false); }
    
    @Override
    public T peek()
    {
        try
        { return get(false); }
        catch(NoSuchElementException e)
        { return null; }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Accessors">
    @Override
    public int size()
    {
        synchronized(members)
        { return members.size(); }
    }
    
    @Override
    public boolean isEmpty()
    {
        synchronized(members)
        { return members.isEmpty(); }
    }
    
    @Override
    public boolean contains(Object o)
    {
        synchronized(members)
        { return members.contains(o); }
    }
    
    @Override
    public Iterator<T> iterator()
    {
        synchronized(members)
        { return members.iterator(); }
    }
    
    @Override
    public boolean containsAll(Collection<?> clctn)
    {
        synchronized(members)
        { return members.containsAll(clctn); }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Converters">
    @Override
    public Object[] toArray()
    {
        synchronized(members)
        { return members.toArray(); }
    }
    
    @Override
    public <T> T[] toArray(T[] ts)
    {
        synchronized(members)
        { return members.toArray(ts); }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Mutators">
    //<editor-fold defaultstate="collapsed" desc="Adders">
    @Override
    public boolean add(T e)
    {
        if(e == null)
            throw new NullArgumentException("e");
        
        synchronized(members)
        { return membersHandler.insertEvenIfAlreadyPresent(members, e) >= 0; }
    }
    
    @Override
    public boolean addAll(Collection<? extends T> clctn)
    {
        if(clctn == null)
            throw new NullArgumentException("clctn");
        
        for(T i : clctn)
            if(i == null)
                throw new NullArgumentException("Member of clctn");
        
        synchronized(members)
        {
            if(members.addAll(clctn))
            {
                List<T> temp = membersHandler.quickSort(members);
                members.clear();
                members.addAll(temp);
                return true;
            }
            
            return false;
        }
    }
    
    @Override
    public boolean offer(T e)
    { return add(e); }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Removers">
    @Override
    public boolean remove(Object o)
    {
        if(o == null)
            throw new NullArgumentException("o");
        
        synchronized(members)
        { return members.remove(o); }
    }
    
    @Override
    public boolean removeAll(Collection<?> clctn)
    {
        synchronized(members)
        { return members.removeAll(clctn); }
    }
    
    @Override
    public boolean retainAll(Collection<?> clctn)
    {
        synchronized(members)
        { return members.retainAll(clctn); }
    }
    
    @Override
    public void clear()
    {
        synchronized(members)
        { members.clear(); }
    }
    //</editor-fold>
    //</editor-fold>
    //</editor-fold>
}