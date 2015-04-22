package com.enkigaming.lib.events;

import com.enkigaming.lib.collections.CombinedQueue;
import com.enkigaming.lib.collections.SortedQueue;
import static com.enkigaming.lib.convenience.SanityChecks.*;
import com.enkigaming.lib.encapsulatedfunctions.Converger;
import com.enkigaming.lib.encapsulatedfunctions.Transformer;
import com.enkigaming.lib.exceptions.NullArgumentException;
import com.enkigaming.lib.tuples.Pair;
import com.enkigaming.lib.tuples.Triplet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

public class StandardEvent<T extends EventArgs> implements Event<T>
{
    protected final Map<EventListener<T>, Double> listeners = new HashMap<EventListener<T>, Double>();
    
    protected final Map<Event<?>, Converger<Object, T, ? extends EventArgs>> dependentEvents
        = new HashMap<Event<?>, Converger<Object, T, ? extends EventArgs>>();
    
    @Override
    public Collection<Event<? extends EventArgs>> getDependentEvents(boolean includeThis,
                                                                     boolean includeDependantsCascadingly)
    {
        if(!includeThis && !includeDependantsCascadingly)
            synchronized(dependentEvents)
            { return dependentEvents.keySet(); }
        
        Collection<Event<?>> events;
        
        synchronized(dependentEvents)
        { events = new HashSet<Event<?>>(dependentEvents.keySet()); }
        
        if(includeDependantsCascadingly)
        {
            Collection<Event<? extends EventArgs>> cascadingDependants = new HashSet<Event<? extends EventArgs>>();
            
            for(Event<? extends EventArgs> i : events)
                cascadingDependants.addAll(i.getDependentEvents(false, true));
            
            events.addAll(cascadingDependants);
        }
        
        if(includeThis)
            events.add(this);
        
        return events;
    }

    @Override
    public Collection<Event<? extends EventArgs>> getDependentEvents()
    { return getDependentEvents(false, true); }

    @Override
    public Collection<Event<? extends EventArgs>> getDirectlyDependentEvents()
    { return getDependentEvents(false, false); }

    @Override
    public Collection<Event<? extends EventArgs>> getThisAndDependentEvents()
    { return getDependentEvents(true, true); }

    @Override
    public Collection<Event<? extends EventArgs>> getThisAndDirectlyDependentEvents()
    { return getDependentEvents(true, false); }

    @Override
    public Map<Event<? extends EventArgs>, Converger<Object, T, ? extends EventArgs>>
        getDirectlyDependentEventsAndArgsGetters()
    {
        synchronized(dependentEvents)
        { return new HashMap(dependentEvents); }
    }

    @Override
    public Collection<EventListener<T>> getListeners()
    {
        synchronized(listeners)
        { return new HashSet<EventListener<T>>(listeners.keySet()); }
    }

    @Override
    public Map<EventListener<T>, Double> getListenersWithPriorities()
    {
        synchronized(listeners)
        { return new HashMap<EventListener<T>, Double>(listeners); }
    }

    @Override
    public Collection<EventListener<? extends EventArgs>> getDependentListeners(boolean includeListenersOfThis,
                                                                                boolean includeDependantsCascadingly)
    {
        // Collection that allows multiple values, as the same listener may be registered to multiple events.
        Collection<EventListener<?>> returnListeners = new ArrayList<EventListener<?>>();
        
        synchronized(dependentEvents)
        {
            for(Event<?> i : getDependentEvents(includeListenersOfThis, includeDependantsCascadingly))
                returnListeners.addAll(i.getListeners());
        }
        
        return returnListeners;
    }

    @Override
    public Collection<EventListener<? extends EventArgs>> getDependentListeners()
    { return getDependentListeners(false, true); }

    @Override
    public Collection<EventListener<? extends EventArgs>> getDirectlyDependentListeners()
    { return getDependentListeners(false, false); }

    @Override
    public Collection<EventListener<? extends EventArgs>> getThisAndDependentListeners()
    { return getDependentListeners(true, true); }

    @Override
    public Collection<EventListener<? extends EventArgs>> getThisAndDirectlyDependentListeners()
    { return getDependentListeners(true, false); }
    
    protected void callListenersPreEvent(Object sender,
                                         Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenersQueue,
                                         boolean shareCancellation)
    {
        boolean currentCancellationState = false;
        
        while(!listenersQueue.isEmpty())
        {
            Triplet<EventListener<? extends EventArgs>, Double, EventArgs> current = listenersQueue.peek();
            double currentPriority = current.getSecond();
            
            if(currentPriority >= ListenerPriority.Post.getNumericalValue())
                break;
            
            listenersQueue.remove();
            
            if(currentPriority >= ListenerPriority.Monitor.getNumericalValue())
                current.getThird().getTechnicalAccessor().makeImmutable();
            
            if(shareCancellation)
                current.getThird().setCancelled(currentCancellationState);
            
            // The type arguments of current.getListener() are guaranteed to match the type arguments of
            // current.getArgs().
            ((EventListener<EventArgs>)current.getFirst()).onEvent(sender, current.getThird());
            
            if(shareCancellation)
                currentCancellationState = current.getThird().isCancelled();
        }
    }
    
    protected void callListenersPostEvent(Object sender,
                                          Queue<Triplet<EventListener<?>, Double, EventArgs>> listenersQueue)
    {
        while(!listenersQueue.isEmpty())
        {
            Triplet<EventListener<?>, Double, EventArgs> current = listenersQueue.poll();
            
            // The type arguments of current.getListener() are guaranteed to match the type arguments of
            // current.getArgs().
            ((EventListener<EventArgs>)current.getFirst()).onEvent(sender, current.getThird());
        }
    }

    @Override
    public void raise(Object sender, T args)
    {
        if(args == null)
            throw new NullArgumentException("args");
        
        args.getTechnicalAccessor().markAsUsingPreEvent();
        
        Queue<Triplet<EventListener<?>, Double, EventArgs>> listenersQueue
            = getThisAndDependentArgsAsQueue(sender, args);
        
        // Sharing cancellation state not necessary: As all event args will be derived from the passed args, they'll all
        // defer their cancellation state to it.
        callListenersPreEvent(sender, listenersQueue, false);
        
        // Attach queue to args for later reference.
        args.getTechnicalAccessor().setListenerQueue(listenersQueue);
        
        args.getTechnicalAccessor().markAsUsedPreEvent();
    }

    @Override
    public void raisePostEvent(Object sender, T args)
    {
        if(args == null)
            throw new NullArgumentException("args");
        
        args.getTechnicalAccessor().markAsUsingPostEvent();
        callListenersPostEvent(sender, args.getTechnicalAccessor().getListenerQueue());
        args.getTechnicalAccessor().markAsUsedPostEvent();
    }

    @Override
    public void raiseAlongside(Object sender, T args, Pair<? extends Event<?>, EventArgs> otherEvent)
    { raiseAlongside(sender, args, true, otherEvent); }

    @Override
    public void raiseAlongside(Object sender, T args, Pair<? extends Event<?>, EventArgs>... otherEvents)
    { raiseAlongside(sender, args, true, otherEvents); }

    @Override
    public void raiseAlongside(Object sender, T args, Collection<? extends Pair<? extends Event<?>, EventArgs>> otherEvents)
    { raiseAlongside(sender, args, true, otherEvents); }

    @Override
    public void raiseAlongside(Object sender, T args, boolean shareCancellation, Pair<? extends Event<?>, EventArgs> otherEvent)
    { raiseAlongside(sender, args, shareCancellation, Arrays.asList(otherEvent)); }

    @Override
    public void raiseAlongside(Object sender, T args, boolean shareCancellation, Pair<? extends Event<?>,
                               EventArgs>... otherEvents)
    { raiseAlongside(sender, args, shareCancellation, Arrays.asList(otherEvents)); }

    @Override
    public void raiseAlongside(Object sender, T args, boolean shareCancellation,
                               Collection<? extends Pair<? extends Event<?>, EventArgs>> otherEvents)
    {
        // Sanity checks.
        // Mark all args as currently using pre event.
        // Get queue of listeners and args to raise.
        // > Assign queues to all args to be raised.
        // > Combine queues into single queue.
        // > Call all listeners in combined queue with priorities before post.
        // Mark all args as having been used pre event.
        
        deepNullCheck(new Pair<Object, String>(otherEvents, "otherEvents"),
                      new Pair<Object, String>(args,        "args"       ));
        
        args.getTechnicalAccessor().markAsUsingPreEvent();
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            i.getSecond().getTechnicalAccessor().markAsUsingPreEvent();
        
        Collection<Queue<Triplet<EventListener<?>, Double, EventArgs>>> queues
            = new HashSet<Queue<Triplet<EventListener<?>, Double, EventArgs>>>();
        
        args.getTechnicalAccessor().setListenerQueue(getThisAndDependentArgsAsQueue(sender, args));
        queues.add(args.getTechnicalAccessor().getListenerQueue());
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
        {
            i.getSecond().getTechnicalAccessor()
                .setListenerQueue(getEventAndDependantsArgsAsQueue(i.getFirst(), sender, i.getSecond()));
            
            queues.add(i.getSecond().getTechnicalAccessor().getListenerQueue());
        }
        
        Queue<Triplet<EventListener<?>, Double, EventArgs>> combinedQueue
            = new CombinedQueue<Triplet<EventListener<?>, Double, EventArgs>>
                (queues, new Transformer<Triplet<EventListener<?>, Double, EventArgs>, Comparable>()
        {
            @Override
            public Comparable get(Triplet<EventListener<?>, Double, EventArgs> parent)
            { return parent.getSecond(); }
        });
        
        callListenersPreEvent(sender, combinedQueue, shareCancellation);
        
        args.getTechnicalAccessor().markAsUsedPreEvent();
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            i.getSecond().getTechnicalAccessor().markAsUsedPreEvent();
    }

    @Override
    public void raisePostEventAlongside(Object sender, T args, Pair<? extends Event<?>, EventArgs> otherEvent)
    { raisePostEventAlongside(sender, args, Arrays.asList(otherEvent)); }

    @Override
    public void raisePostEventAlongside(Object sender, T args, Pair<? extends Event<?>, EventArgs>... otherEvents)
    { raisePostEventAlongside(sender, args, Arrays.asList(otherEvents)); }

    @Override
    public void raisePostEventAlongside(Object sender, T args,
                                        Collection<? extends Pair<? extends Event<?>, EventArgs>> otherEvents)
    {
        // Sanity checks
        // Mark all args as being used post-event.
        // Combine listener queues from args.
        // Call listeners
        // Mark all args as having been used post-event.
        
        if(otherEvents == null)
            throw new NullArgumentException("otherEvents.");
        
        if(args == null)
            throw new NullArgumentException("args.");
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            if(i == null)
                throw new NullArgumentException("Member of otherEvents.");
        
        args.getTechnicalAccessor().markAsUsingPostEvent();
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            i.getSecond().getTechnicalAccessor().markAsUsingPostEvent();
        
        Collection<Queue<Triplet<EventListener<?>, Double, EventArgs>>> queues
            = new HashSet<Queue<Triplet<EventListener<?>, Double, EventArgs>>>();
        
        queues.add(args.getTechnicalAccessor().getListenerQueue());
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            queues.add(i.getSecond().getTechnicalAccessor().getListenerQueue());
        
        Queue<Triplet<EventListener<?>, Double, EventArgs>> combinedQueue
            = new CombinedQueue<Triplet<EventListener<?>, Double, EventArgs>>
                (queues, new Transformer<Triplet<EventListener<?>, Double, EventArgs>, Comparable>()
        {
            @Override
            public Comparable get(Triplet<EventListener<?>, Double, EventArgs> parent)
            { return parent.getSecond(); }
        });
        
        callListenersPostEvent(sender, combinedQueue);
        
        args.getTechnicalAccessor().markAsUsedPostEvent();
        
        for(Pair<? extends Event<?>, EventArgs> i : otherEvents)
            i.getSecond().getTechnicalAccessor().markAsUsedPostEvent();
    }

    @Override
    public void register(EventListener<T> listener)
    { register(ListenerPriority.Normal.getNumericalValue(), listener); }

    @Override
    public void register(EventListener<T>... listeners)
    { register(ListenerPriority.Normal.getNumericalValue(), Arrays.asList(listeners)); }

    @Override
    public void register(EventListener<T> listener, double priority)
    { register(priority, listener); }

    @Override
    public void register(EventListener<T> listener, ListenerPriority priority)
    { register(priority.getNumericalValue(), listener); }

    @Override
    public void register(double priority, EventListener<T> listener)
    {
        synchronized(listeners)
        {
            listeners.put(listener, priority);
            //System.out.println("Listeners now contains: " + listeners.toString());
        }
    }

    @Override
    public void register(ListenerPriority priority, EventListener<T> listener)
    { register(priority.getNumericalValue(), listener); }

    @Override
    public void register(double priority, EventListener<T>... listeners)
    { register(priority, Arrays.asList(listeners)); }
    
    @Override
    public void register(ListenerPriority priority, EventListener<T>... listeners)
    { register(priority.getNumericalValue(), Arrays.asList(listeners)); }
    
    @Override
    public void register(double priority, Collection<EventListener<T>> listeners)
    {
        synchronized(listeners)
        {
            for(EventListener<T> i : listeners)
                this.listeners.put(i, priority);
        }
    }
    
    @Override
    public void register(ListenerPriority priority, Collection<EventListener<T>> listeners)
    { register(priority.getNumericalValue(), listeners); }
    
    @Override
    public void register(Collection<EventListener<T>> listeners, double priority)
    { register(priority, listeners); }
    
    @Override
    public void register(Collection<EventListener<T>> listeners, ListenerPriority priority)
    { register(priority.getNumericalValue(), listeners); }

    @Override
    public <TArgs extends EventArgs> void register(Event<TArgs> event, Converger<Object, T, TArgs> eventArgsGetter)
    { register(eventArgsGetter, event); }
    
    @Override
    public <TArgs extends EventArgs> void register(Converger<Object, T, TArgs> eventArgsGetter, Event<TArgs> event)
    {
        synchronized(dependentEvents) 
        { dependentEvents.put(event, eventArgsGetter); }
    }
    
    @Override
    public <TArgs extends EventArgs> void register(Converger<Object, T, TArgs> eventArgsGetter,
                                                   Event<? extends TArgs>... events)
    { register(eventArgsGetter, Arrays.asList(events)); }
    
    @Override
    public <TArgs extends EventArgs> void register(Converger<Object, T, TArgs> eventArgsGetter,
                                                   Collection<? extends Event<? extends TArgs>> events)
    {
        synchronized(dependentEvents)
        {
            for(Event<? extends TArgs> i : events)
                dependentEvents.put(i, eventArgsGetter);
        }
    }

    @Override
    public EventListener<T> deregister(EventListener<T> listener)
    {
        synchronized(listeners)
        { return listeners.remove(listener) == null ? null : listener; }
    }

    @Override
    public Collection<EventListener<T>> deregister(EventListener<T>... listeners)
    {
        Collection<EventListener<T>> deregistered = new HashSet<EventListener<T>>();
        
        synchronized(this.listeners)
        {
            for(EventListener<T> i : listeners)
                if(this.listeners.remove(i) != null)
                    deregistered.add(i);
        }
        
        return deregistered;
    }

    @Override
    public Event<? extends EventArgs> deregister(Event<? extends EventArgs> event)
    {
        synchronized(dependentEvents)
        { return dependentEvents.remove(event) == null ? null : event; }
    }

    @Override
    public Collection<Event<? extends EventArgs>> deregister(Event<? extends EventArgs>... events)
    {
        Collection<Event<? extends EventArgs>> deregistered = new HashSet<Event<? extends EventArgs>>();
        
        synchronized(dependentEvents)
        {
            for(Event<? extends EventArgs> i : events)
                if(this.dependentEvents.remove(i) != null)
                    deregistered.add(i);
        }
        
        return deregistered;
    }
    
    protected static Collection<EventArgs> getArgsFrom(Collection<? extends Pair<Event<?>, EventArgs>> eventsWithArgs)
    {
        Collection<EventArgs> args = new ArrayList<EventArgs>();
        
        for(Pair<Event<?>, EventArgs> i : eventsWithArgs)
            args.add(i.getSecond());
        
        return args;
    }
    
    // Needs to be static. Calls itself recursively on other event objects that may or may not have this method.
    private static <T extends EventArgs> Collection<EventArgs> generateThisAndDependentArgs(Event<T> event, Object sender, T args)
    {
        Collection<EventArgs> allArgs = new HashSet<EventArgs>();
        
        args.getTechnicalAccessor().setEvent(event);
        allArgs.add(args);
        
        Map<Event<?>, Converger<Object, T, ? extends EventArgs>> eventsAndArgsGetters
            = event.getDirectlyDependentEventsAndArgsGetters();
        
        for(Map.Entry<Event<?>, Converger<Object, T, ? extends EventArgs>> i : eventsAndArgsGetters.entrySet())
        {
            EventArgs iArgs = i.getValue().get(sender, args);
            
            iArgs.getTechnicalAccessor().setEvent(event);
            iArgs.getTechnicalAccessor().setParentArgs(args);
            
            args.getTechnicalAccessor().addDependentArgs(iArgs);
            
            // The generic type arguments of iArgs are guaranteed to match the generic type arguments of i.getKey().
            allArgs.addAll(generateThisAndDependentArgs((Event<EventArgs>)i.getKey(), sender, iArgs));
        }
        
        return allArgs;
    }
    
    protected Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> getThisAndDependentArgsAsQueue(Object sender, T args)
    {
        Collection<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenerArgsPairings = new HashSet<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>();
        
        for(EventArgs i : generateThisAndDependentArgs(this, sender, args))
            for(Map.Entry<? extends EventListener<? extends EventArgs>, Double> j : i.getEvent().getListenersWithPriorities().entrySet())
                listenerArgsPairings.add(new Triplet<EventListener<? extends EventArgs>, Double, EventArgs>(j.getKey(), j.getValue(), i));
        
        return new SortedQueue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>(listenerArgsPairings, new Transformer<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>, Comparable>()
        {
            @Override
            public Comparable get(Triplet<EventListener<? extends EventArgs>, Double, EventArgs> parent)
            { return parent.getSecond(); }
        });
    }
    
    protected static Collection<EventArgs> generateEventAndDependantsArgs(Event<? extends EventArgs> event,
                                                                          Object sender,
                                                                          EventArgs args)
    {
        // I believe this is technically referred to as the "Fuck it all" pattern. Replace this whole bit with something
        // cleaner at a later date. Maybe when I look into optimising this.
        Event<EventArgs> castedEvent = (Event<EventArgs>)event;
        
        Collection<EventArgs> allArgs = new HashSet<EventArgs>();
        
        args.getTechnicalAccessor().setEvent(event);
        allArgs.add(args);
        
        Map<Event<?>, Converger<Object, EventArgs, ? extends EventArgs>> eventsAndArgsGetters
            = castedEvent.getDirectlyDependentEventsAndArgsGetters();
        
        for(Map.Entry<Event<?>, Converger<Object, EventArgs, ? extends EventArgs>> i : eventsAndArgsGetters.entrySet())
        {
            EventArgs iArgs = i.getValue().get(sender, args);
            
            iArgs.getTechnicalAccessor().setEvent(event);
            iArgs.getTechnicalAccessor().setParentArgs(args);
            
            args.getTechnicalAccessor().addDependentArgs(iArgs);
            
            // The generic type arguments of iArgs are guaranteed to match the generic type arguments of i.getKey().
            allArgs.addAll(generateThisAndDependentArgs((Event<EventArgs>)i.getKey(), sender, iArgs));
        }
        
        return allArgs;
    }
    
    protected static Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> getEventAndDependantsArgsAsQueue(Event<?> event, Object sender, EventArgs args)
    {
        Collection<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenerArgsPairings = new HashSet<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>();
        
        for(EventArgs i : generateEventAndDependantsArgs(event, sender, args))
            for(Map.Entry<? extends EventListener<? extends EventArgs>, Double> j : i.getEvent().getListenersWithPriorities().entrySet())
                listenerArgsPairings.add(new Triplet<EventListener<? extends EventArgs>, Double, EventArgs>(j.getKey(), j.getValue(), i));
        
        return new SortedQueue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>(listenerArgsPairings, new Transformer<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>, Comparable>()
        {
            @Override
            public Comparable get(Triplet<EventListener<? extends EventArgs>, Double, EventArgs> parent)
            { return parent.getSecond(); }
        });
    }
}