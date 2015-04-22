package com.enkigaming.lib.collections;

import com.enkigaming.lib.exceptions.NullArgumentException;
import com.enkigaming.lib.encapsulatedfunctions.Transformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

// Get feedback on this class. Specifically, the code for drawing values from contained queues.

/**
 * Queue that draws values from other contained queues. Naturally, immutable as far as values is concerned, as it
 * doesn't hold actual values, just queues to draw values from. Determines the next value using a Lambda.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @param <T> The type of object to be drawn from the queue.
 */
public class CombinedQueue<T> implements Queue<T>
{
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Constructor. Generates with just the next-value-getting lambda object, and no queues to draw values from. Queues
     * to be drawn from are to be added.
     * @param keyGetter The lambda object used for grabbing the comparable used for determining the next value.
     */
    public CombinedQueue(Transformer<T, Comparable> keyGetter)
    { this.keyGetter = keyGetter; }
    
    /**
     * Constructor. Generates with next-value-getting lambda object and queues to draw from.
     * @param keyGetter The lambda object used for grabbing the comparable used for determining the next value.
     * Determines the next value from the next items in each of the held queues.
     * @param queues The queues from which to draw values.
     */
    public CombinedQueue(Transformer<T, Comparable> keyGetter, Queue<? extends T>... queues)
    { this(keyGetter, Arrays.asList(queues)); }
    
    /**
     * Constructor. Generates with next-value-getting lambda object and queues to draw from.
     * @param queues The queues from which to draw values.
     * @param keyGetter The lambda object used for grabbing the comparable used for determining the next value.
     * Determines the next value from the next items in each of the held queues.
     */
    public CombinedQueue(Queue<? extends T>[] queues, Transformer<T, Comparable> keyGetter)
    { this(keyGetter, queues); }
    
    /**
     * Constructor. Generates with next-value-getting lambda object and queues to draw from.
     * @param keyGetter The lambda object used for grabbing the comparable used for determining the next value.
     * Determines the next value from the next items in each of the held queues.
     * @param queues The queues from which to draw values.
     */
    public CombinedQueue(Transformer<T, Comparable> keyGetter, Collection<? extends Queue<? extends T>> queues)
    {
        this.keyGetter = keyGetter;
        memberQueues.addAll((Collection<? extends Queue<T>>)queues);
    }
    
    /**
     * Constructor. Generates with next-value-getting lambda object and queues to draw from.
     * @param queues The queues from which to draw values.
     * @param keyGetter The lambda object used for grabbing the comparable used for determining the next value.
     * Determines the next value from the next items in each of the held queues.
     */
    public CombinedQueue(Collection<? extends Queue<? extends T>> queues, Transformer<T, Comparable> keyGetter)
    { this(keyGetter, queues); }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * The queues getting values drawn from.
     */
    final Collection<Queue<? extends T>> memberQueues = new ArrayList<Queue<? extends T>>();
    
    /**
     * The lambda object that derives the comparable used for determining the next value from the values held in the
     * queues.
     */
    final Transformer<T, Comparable> keyGetter;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods">
    //<editor-fold defaultstate="collapsed" desc="Next value getters/poppers">
    /**
     * Gets the next value. Used for the remove, poll, element, and peek methods.
     * @param remove Whether or not to remove the next value from the queue.
     * @return The next value. Drawn from the next value of one of the contained queues.
     * @throws NoSuchElementException when there are no queues in the combined queue, or all contained queues are empty.
     */
    public T get(boolean remove)
    {
        // This algorithm works. That's not to say it's ideal. It's contrived, and an obvious workaround for maintaining
        // thread safety. If you happen to see this comment and know of a better algorithm for grabbing the next value
        // from a memberqueue in order decided by keyGetter while maintaining thread-safety, tell me <3
        
        // Uses a lambda to derive a comparable rather than a comparator as using a comparator removes the ability to
        // check whether the variable within the next value that's being measured is the same, which may happen where
        // the compared value is changed in a way that still results in the value being next in the queue it's in.
        
        // Work on a copy of memberQueues
        Collection<Queue<? extends T>> memberQueuesCopy = getQueues();
        
        Queue<? extends T> nextQueue = null;
        T nextValue = null;
        Comparable nextValueComparable = null;
        
        // Run through all member queues, filling those variables with the variables corresponding to the queue with the
        // lowest next comparable derived via keyGetter.
        
        // Establish a queue has the next value by iterating through the rest of the list before returning to it, and if
        // it still has the same values (that is, hasn't been modified in a way that affects its order in this queue)
        // after having compared the old values against the rest of the collection, return it as the next value.
        
        // null values always come last.
        for(int i = 0; i < 100; i++) // 100-loop threshhold
        {
            boolean populatedQueueFound = false;
            
            for(Queue<? extends T> j : memberQueuesCopy)
            {
                if(j != null)
                {
                    synchronized(j)
                    {
                        if(!j.isEmpty())
                        {
                            populatedQueueFound = true;
                            
                            T jValue = j.peek();
                            
                            if(jValue == null && nextQueue == null) // If there is no next queue, use this one.
                                nextQueue = j;
                            else if(jValue != null)
                            {
                                Comparable jValueComparable = keyGetter.get(jValue);
                                
                                if(j == nextQueue)
                                {
                                    if(nextValue == jValue &&
                                       nextValueComparable == jValueComparable)
                                    {
                                        if(remove)
                                            return nextQueue.remove();
                                        else
                                            return jValue;
                                    }
                                    
                                    nextValue = jValue;
                                    nextValueComparable = jValueComparable;
                                }
                                else
                                {
                                    if((jValueComparable == null && nextQueue == null) || // There is no next queue, use this one.
                                       (jValueComparable != null &&
                                        (nextValueComparable == null || // Currently stored next value is null (last) and this one should be used before.
                                         jValueComparable.compareTo(nextValueComparable) < 0))) // This one comes before the currently stored next value.
                                    {
                                        nextQueue = j;
                                        nextValue = jValue;
                                        nextValueComparable = jValueComparable;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if(!populatedQueueFound)
                throw new NoSuchElementException();
        }
        
        throw new RuntimeException("Loop threshhold reached for combined queue - either something is constantly " +
                "modifying one of the reference queues, or there is a problem with the " +
                "CombinedQueue.get method.");
        // replace with specific exception pertaining to the threshhold reached.
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
    /**
     * Gets the queues from which the combined queue draws values.
     * @return A collection of the contained queues. Modifying the collection will not modify the the contents of this
     * queue.
     */
    public Collection<Queue<? extends T>> getQueues()
    {
        synchronized(memberQueues)
        { return new ArrayList<Queue<? extends T>>(memberQueues); }
    }
    
    @Override
    public int size()
    {
        Collection<Queue<? extends T>> memberQueuesCopy = getQueues();
        int currentSize = 0;
        
        for(Queue<? extends T> i : memberQueuesCopy)
            if(i != null)
                synchronized(i)
                { currentSize += i.size(); }
        
        return currentSize;
    }
    
    @Override
    public boolean isEmpty()
    {
        Collection<Queue<? extends T>> memberQueuesCopy = getQueues();
        boolean empty = true;
        
        for(Queue<? extends T> i : memberQueuesCopy)
            if(i != null)
                synchronized(i)
                {
                    if(!i.isEmpty())
                        empty = false;
                }
        
        return empty;
    }
    
    @Override
    public boolean contains(Object o)
    {
        for(Queue<? extends T> i : getQueues())
            if(i != null)
                synchronized(i)
                {
                    if(i.contains(o))
                        return true;
                }
        
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> clctn)
    { return toFlatCollection().containsAll(clctn); }
    
    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            final Collection<T> cache = new ArrayList<T>();
            
            {
                for(Queue<? extends T> i : getQueues())
                    if(i != null)
                        synchronized(i)
                        { cache.addAll(i); }
            }
            
            final Iterator<T> cacheIterator = cache.iterator();
            T lastReturnedValue = null;
            
            @Override
            public boolean hasNext()
            { return cacheIterator.hasNext(); }
            
            @Override
            public T next()
            {
                lastReturnedValue = cacheIterator.next();
                return lastReturnedValue;
            }
            
            @Override
            public void remove()
            { CombinedQueue.this.remove(lastReturnedValue); }
        };
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Converters">
    @Override
    public Object[] toArray()
    { return toFlatCollection().toArray(); }
    
    @Override
    public <Ta> Ta[] toArray(Ta[] ts)
    { return toFlatCollection().<Ta>toArray(ts); }
    
    /**
     * Converts the combined queue into a flat collection, containing the values contained in all of the queues combined
     * into a single collection.
     * @return A collection containing all members of all contained queues.
     */
    public Collection<T> toFlatCollection()
    {
        Collection<T> returnValues = new ArrayList<T>();
        
        for(Queue<? extends T> i : getQueues())
            if(i != null)
                synchronized(i)
                { returnValues.addAll(i); }
        
        return returnValues;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Mutators">
    //<editor-fold defaultstate="collapsed" desc="Add queues">
    /**
     * Adds a queue to be combined queue so that its members may be referenced or removed by the queue.
     * @param queue The queue to add.
     * @return Whether or not the queue was successfully added. It may not be added if it is already present.
     */
    public boolean addQueue(Queue<? extends T> queue)
    {
        if(queue == null)
            throw new NullArgumentException();
        
        synchronized(memberQueues)
        {
            if(!memberQueues.contains(queue))
                return memberQueues.add(queue);
        }
        
        return false;
    }
    
    /**
     * Adds a collection of queues to the combined queue so that their members may be referenced or removed by the
     * queue.
     * @param queues The queues to add.
     * @return True if the queue was modified as a result of this method call, false if it wasn't. Values may not be
     * added if they're already present.
     */
    public boolean addAllQueues(Collection<? extends Queue<? extends T>> queues)
    {
        boolean changed = false;
        
        synchronized(memberQueues)
        {
            for(Queue<? extends T> i : queues)
                if(addQueue(i))
                    changed = true;
        }
        
        return changed;
    }
    
    /**
     * Adds a collection of queues to the combined queue so that their members may be referenced or removed by the
     * queue.
     * @param queues The queues to add.
     * @return True if the queue was modified as a result of this method call, false if it wasn't. Values may not be
     * added if they're already present.
     */
    public boolean addAllQueues(Queue<? extends T>... queues)
    {
        boolean changed = false;
        
        synchronized(memberQueues)
        {
            for(Queue<? extends T> i : queues)
                if(addQueue(i))
                    changed = true;
        }
        
        return changed;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Remove Queues">
    /**
     * Removes a queue from the combined queue such that its values are no longer referenced/deleted by it.
     * @param queue The queue to remove.
     * @return True if it was removed, false if it wasn't. Id est, if it wasn't present.
     */
    public boolean removeQueue(Queue<? extends T> queue)
    {
        synchronized(memberQueues)
        { return memberQueues.remove(queue); }
    }
    
    /**
     * Removes a collection of queues from the combined queue such that their values are no longer references/deleted
     * by it.
     * @param queues The queues to remove.
     * @return True if the queue was modified as a result of this function call. Else, false.
     */
    public boolean removeAllQueues(Collection<? extends Queue<? extends T>> queues)
    {
        synchronized(memberQueues)
        { return memberQueues.removeAll(queues); }
    }
    
    /**
     * Removes a collection of queues from the combined queue such that their values are no longer references/deleted
     * by it.
     * @param queues The queues to remove.
     * @return True if the queue was modified as a result of this function call. Else, false.
     */
    public boolean removeAllQueues(Queue<? extends T>... queues)
    {
        synchronized(memberQueues)
        { return memberQueues.removeAll(Arrays.asList(queues)); }
    }
    
    /**
     * Removes all members bar the passed ones such that they're the only ones that will contribute values/lose values
     * as a result of interaction with the combined queue.
     * @param queues The queues to keep.
     * @return True if the queue was modified as a result of this call. Else, false.
     */
    public boolean retainAllQueues(Collection<? extends Queue<? extends T>> queues)
    {
        synchronized(memberQueues)
        { return memberQueues.retainAll(queues); }
    }
    
    /**
     * Removes all members bar the passed ones such that they're the only ones that will contribute values/lose values
     * as a result of interaction with the combined queue.
     * @param queues The queues to keep.
     * @return True if the queue was modified as a result of this call. Else, false.
     */
    public boolean retainAllQueues(Queue<? extends T>... queues)
    {
        synchronized(memberQueues)
        { return memberQueues.retainAll(Arrays.asList(queues)); }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Remove values">
    @Override
    public boolean remove(Object o)
    {
        for(Queue<? extends T> i : getQueues())
            if(i != null)
                synchronized(i)
                {
                    if(i.remove(o))
                        return true;
                }
        
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> clctn)
    {
        boolean changed = false;
        
        for(Object i : clctn)
            if(remove(i))
                changed = true;
        
        return changed;
    }
    
    @Override
    public boolean retainAll(Collection<?> clctn)
    {
        boolean changed = false;
        
        for(Queue<? extends T> i : getQueues())
            if(i != null)
                synchronized(i)
                {
                    if(i.retainAll(clctn))
                        changed = true;
                }
        
        return changed;
    }
    
    @Override
    public void clear()
    {
        for(Queue<? extends T> i : getQueues())
            if(i != null)
                i.clear();
    }
    //</editor-fold>
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Unsupported mutators">
    @Override
    public boolean add(T e)
    {
        throw new UnsupportedOperationException("Combined list does not contain members, it draws values from " +
                "referenced queues. Add values to one of the contained queues.");
    }
    
    @Override
    public boolean offer(T e)
    {
        throw new UnsupportedOperationException("Combined list does not contain members, it draws values from " +
                "referenced queues. Add values to one of the contained queues.");
    }
    
    @Override
    public boolean addAll(Collection<? extends T> clctn)
    {
        throw new UnsupportedOperationException("Combined list does not contain members, it draws values from " +
                "referenced queues. Add values to one of the contained queues.");
    }
    //</editor-fold>
    //</editor-fold>
}