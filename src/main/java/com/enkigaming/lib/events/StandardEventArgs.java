package com.enkigaming.lib.events;

import com.enkigaming.lib.events.exceptions.EventArgsFinishedBeforeStartedException;
import com.enkigaming.lib.events.exceptions.EventArgsModifiedWhenImmutableException;
import com.enkigaming.lib.events.exceptions.EventArgsMultipleUseException;
import com.enkigaming.lib.events.exceptions.EventArgsUsedPostBeforePreException;
import com.enkigaming.lib.tuples.Triplet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class StandardEventArgs implements EventArgs
{
    protected enum Status
    {
        Unused,
        UsingPreEvent,
        UsedPreEvent,
        UsingPostEvent,
        UsedPostEvent
    }
    
    Event<? extends EventArgs> event = null;
    boolean cancelled = false;
    boolean mutable = true;
    EventArgs parentArgs = null;
    Status status = Status.Unused;
    Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenerQueue = null;
    
    final Set<EventArgs> relatedMasterArgs = new HashSet<EventArgs>();
    final Set<EventArgs> dependentArgs     = new HashSet<EventArgs>();
    
    final Object cancelledBusy     = new Object();
    final Object mutableBusy       = new Object();
    final Object parentArgsBusy    = new Object();
    final Object eventBusy         = new Object();
    final Object statusBusy        = new Object();
    final Object listenerQueueBusy = new Object();
    
    @Override
    public boolean isCancelled()
    {
        EventArgs master = getMasterArgs();
            
        if(master != this)
            return master.isCancelled();
        
        synchronized(cancelledBusy)
        { return cancelled; }
    }

    @Override
    public boolean setCancelled(boolean cancellation)
    {
        if(!shouldBeMutable())
            throw new EventArgsModifiedWhenImmutableException();
            
        EventArgs master = getMasterArgs();

        if(master != this)
            return master.setCancelled(cancellation);

        synchronized(cancelledBusy)
        {
            boolean oldValue = cancelled;
            cancelled = cancellation;
            return oldValue;
        }
    }

    @Override
    public boolean shouldBeMutable()
    {
        EventArgs master = getMasterArgs();

        if(master != this)
            return master.shouldBeMutable();
        
        synchronized(mutableBusy)
        { return mutable; }
    }
    
    protected void makeImmutable()
    {
        EventArgs master = getMasterArgs();

        if(master != this)
        {
            master.getTechnicalAccessor().makeImmutable();
            return;
        }
        
        synchronized(mutableBusy)
        { mutable = false; }
    }

    @Override
    public Collection<EventArgs> getRelatedMasterArgs()
    {
        EventArgs master = getMasterArgs();

        if(master != this)
            return master.getRelatedMasterArgs();
        
        synchronized(relatedMasterArgs)
        { return new ArrayList<EventArgs>(relatedMasterArgs); }
    }
    
    @Override
    public Collection<EventArgs> getRelatedArgs()
    {
        Collection<EventArgs> masters = new HashSet<EventArgs>(getRelatedMasterArgs());
        EventArgs thisMaster = getMasterArgs();
        
        if(thisMaster == this)
            masters.add(this);
        else
            masters.add(thisMaster);
        
        Collection<EventArgs> relatedArgs = new HashSet<EventArgs>();
        
        for(EventArgs i : masters)
            relatedArgs.addAll(i.getDependentArgs(true, true));
        
        relatedArgs.remove(this);
        
        return relatedArgs;
    }
    
    protected void addRelatedMasterArgs(EventArgs args)
    {
        synchronized(relatedMasterArgs)
        { relatedMasterArgs.add(args); }
    }

    protected void addRelatedMasterArgs(EventArgs... args)
    {
        synchronized(relatedMasterArgs)
        { relatedMasterArgs.addAll(Arrays.asList(args)); }
    }

    protected void addRelatedMasterArgs(Collection<? extends EventArgs> args)
    {
        synchronized(relatedMasterArgs)
        { relatedMasterArgs.addAll(args); }
    }

    @Override
    public Collection<EventArgs> getDependentArgs(boolean getDependantsCascadingly)
    { return getDependentArgs(false, getDependantsCascadingly); }

    @Override
    public Collection<EventArgs> getDependentArgs(boolean includeThis, boolean getDependantsCascadingly)
    {
        Collection<EventArgs> returnArgs;
        
        synchronized(dependentArgs)
        { returnArgs = new HashSet<EventArgs>(dependentArgs); }
        
        if(getDependantsCascadingly)
        {
            Collection<EventArgs> cascaded = new HashSet<EventArgs>();
            
            for(EventArgs i : returnArgs)
                cascaded.addAll(i.getDependentArgs(false, true));
            
            returnArgs.addAll(cascaded);
        }
        
        if(includeThis)
            returnArgs.add(this);
        
        return returnArgs;
    }

    @Override
    public Collection<EventArgs> getDependentArgs()
    { return getDependentArgs(false, true); }

    @Override
    public Collection<EventArgs> getDirectlyDependentArgs()
    { return getDependentArgs(false, false); }
    
    protected void addDependentArgs(EventArgs args)
    {
        synchronized(dependentArgs)
        { dependentArgs.add(args); }
    }

    @Override
    public EventArgs getParentArgs()
    {
        synchronized(parentArgsBusy)
        { return parentArgs; }
    }
    
    protected void setParentArgs(EventArgs args)
    {
        synchronized(parentArgsBusy)
        { parentArgs = args; }
    }

    @Override
    public EventArgs getMasterArgs()
    {
        synchronized(parentArgsBusy)
        {
            if(parentArgs == null)
                return this;
            
            return parentArgs.getMasterArgs();
        }
    }

    @Override
    public Event<? extends EventArgs> getEvent()
    {
        synchronized(eventBusy)
        { return event; }
    }
    
    protected void setEvent(Event<? extends EventArgs> event)
    {
        synchronized(eventBusy)
        { this.event = event; }
    }
    
    protected void markAsUsingPreEvent()
    {
        EventArgs master = getMasterArgs();
            
        if(master != this)
        {
            master.getTechnicalAccessor().markAsUsingPreEvent();
            return;
        }
        
        synchronized(statusBusy)
        {
            if(status != Status.Unused)
                throw new EventArgsMultipleUseException();
            
            status = Status.UsingPreEvent;
        }
    }

    protected void markAsUsedPreEvent()
    {
        EventArgs master = getMasterArgs();
            
        if(master != this)
        {
            master.getTechnicalAccessor().markAsUsedPreEvent();
            return;
        }
        
        synchronized(statusBusy)
        {
            switch(status)
            {
                case Unused:
                    throw new EventArgsFinishedBeforeStartedException("Has not been marked as using pre-event.");
                case UsingPreEvent:
                {
                    status = Status.UsedPreEvent;
                    break;
                }
                case UsedPreEvent:
                case UsingPostEvent:
                case UsedPostEvent:
                    throw new EventArgsMultipleUseException("Already used pre-event.");
            }
        }
    }

    protected void markAsUsingPostEvent()
    {
        EventArgs master = getMasterArgs();
            
        if(master != this)
        {
            master.getTechnicalAccessor().markAsUsingPostEvent();
            return;
        }
        
        synchronized(statusBusy)
        {
            switch(status)
            {
                case Unused:
                    throw new EventArgsUsedPostBeforePreException("Has not been marked as being used pre-event.");
                case UsingPreEvent:
                    throw new EventArgsUsedPostBeforePreException("Has not finished being used pre-event.");
                case UsedPreEvent:
                {
                    status = Status.UsingPostEvent;
                    break;
                }
                case UsingPostEvent:
                case UsedPostEvent:
                    throw new EventArgsMultipleUseException("Already used post-event.");
            }
        }
    }

    protected void markAsUsedPostEvent()
    {
        EventArgs master = getMasterArgs();
            
        if(master != this)
        {
            master.getTechnicalAccessor().markAsUsedPostEvent();
            return;
        }
        
        synchronized(statusBusy)
        {
            switch(status)
            {
                case Unused:
                    throw new EventArgsUsedPostBeforePreException("Has not been marked as being used pre-event.");
                case UsingPreEvent:
                    throw new EventArgsUsedPostBeforePreException("Has not finished being used pre-event.");
                case UsedPreEvent:
                    throw new EventArgsFinishedBeforeStartedException("Has not been marked as using post-event.");
                case UsingPostEvent:
                {
                    status = Status.UsedPostEvent;
                    break;
                }
                case UsedPostEvent:
                    throw new EventArgsMultipleUseException("Already used post-event.");
            }
        }
    }
    
    protected void setListenerQueue(Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> queue)
    {
        synchronized(listenerQueueBusy)
        { listenerQueue = queue; }
    }
    
    protected Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> getListenerQueue()
    {
        synchronized(listenerQueueBusy)
        { return listenerQueue; }
    }

    @Override
    public TechnicalAccessor getTechnicalAccessor()
    {
        return new TechnicalAccessor()
        {
            @Override
            public void markAsUsingPreEvent()
            { StandardEventArgs.this.markAsUsingPreEvent(); }

            @Override
            public void markAsUsedPreEvent()
            { StandardEventArgs.this.markAsUsedPreEvent(); }

            @Override
            public void markAsUsingPostEvent()
            { StandardEventArgs.this.markAsUsingPostEvent(); }

            @Override
            public void markAsUsedPostEvent()
            { StandardEventArgs.this.markAsUsedPostEvent(); }

            @Override
            public void setEvent(Event<? extends EventArgs> event)
            { StandardEventArgs.this.setEvent(event); }

            @Override
            public void setParentArgs(EventArgs args)
            { StandardEventArgs.this.setParentArgs(args); }

            @Override
            public void makeImmutable()
            { StandardEventArgs.this.makeImmutable(); }

            @Override
            public void addDependentArgs(EventArgs args)
            { StandardEventArgs.this.addDependentArgs(args); }

            @Override
            public void addRelatedMasterArgs(EventArgs args)
            { StandardEventArgs.this.addRelatedMasterArgs(args); }

            @Override
            public void addRelatedMasterArgs(EventArgs... args)
            { StandardEventArgs.this.addRelatedMasterArgs(args); }

            @Override
            public void addRelatedMasterArgs(Collection<? extends EventArgs> args)
            { StandardEventArgs.this.addRelatedMasterArgs(args); }

            @Override
            public void setListenerQueue(Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenerQueue)
            { StandardEventArgs.this.setListenerQueue(listenerQueue); }

            @Override
            public Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> getListenerQueue()
            { return StandardEventArgs.this.getListenerQueue(); }
        };
    }
}