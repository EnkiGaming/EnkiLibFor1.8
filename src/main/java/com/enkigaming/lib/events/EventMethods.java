package com.enkigaming.lib.events;

import com.enkigaming.lib.tuples.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class EventMethods
{
    //<editor-fold defaultstate="collapsed" desc="Multiple raise">
    public static void raiseMultiple(Object sender, Pair<Event<?>, EventArgs>... events)
    { raiseMultiple(sender, Arrays.asList(events)); }
    
    public static void raiseMultiple(Object sender, Collection<Pair<Event<?>, EventArgs>> events)
    {
        Collection<Pair<Event<?>, EventArgs>> alongsideEvents
        = new ArrayList<Pair<Event<?>, EventArgs>>(events);
        
        Pair<Event<?>, EventArgs> toCallOn = null;
        
        for(Pair<Event<?>, EventArgs> i : alongsideEvents)
        {
            toCallOn = i;
            break;
        }
        
        if(toCallOn == null)
            return;
        
        alongsideEvents.remove(toCallOn);
        ((Event<EventArgs>)toCallOn.getFirst()).raiseAlongside(sender, toCallOn.getSecond(), events);
    }
    
    public static void raiseMultiple(Object sender, boolean shareCancellation, Collection<Pair<Event<?>, EventArgs>> events)
    {
        Collection<Pair<Event<?>, EventArgs>> alongsideEvents
        = new ArrayList<Pair<Event<?>, EventArgs>>(events);
        
        Pair<Event<?>, EventArgs> toCallOn = null;
        
        for(Pair<Event<?>, EventArgs> i : alongsideEvents)
        {
            toCallOn = i;
            break;
        }
        
        if(toCallOn == null)
            return;
        
        alongsideEvents.remove(toCallOn);
        ((Event<EventArgs>)toCallOn.getFirst()).raiseAlongside(sender, toCallOn.getSecond(), shareCancellation, events);
    }
    
    public static void raiseMultiplePostEvent(Object sender, Pair<Event<?>, EventArgs>... events)
    { raiseMultiple(sender, Arrays.asList(events)); }
    
    public static void raiseMultiplePostEvent(Object sender, Collection<Pair<Event<?>, EventArgs>> events)
    {
        Collection<Pair<Event<?>, EventArgs>> alongsideEvents
        = new ArrayList<Pair<Event<?>, EventArgs>>(events);
        
        Pair<Event<?>, EventArgs> toCallOn = null;
        
        for(Pair<Event<?>, EventArgs> i : alongsideEvents)
        {
            toCallOn = i;
            break;
        }
        
        if(toCallOn == null)
            return;
        
        alongsideEvents.remove(toCallOn);
        ((Event<EventArgs>)toCallOn.getFirst()).raisePostEventAlongside(sender, toCallOn.getSecond(), events);
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Event args relating">
    public static void relateArgs(EventArgs... args)
    { relateArgs(Arrays.asList(args)); }
    
    public static void relateArgs(Collection<? extends EventArgs> args)
    {
        for(EventArgs i : args)
        {
            Collection<EventArgs> toRelate = new ArrayList<EventArgs>(args);
            toRelate.remove(i);
            
            i.getTechnicalAccessor().addRelatedMasterArgs(toRelate);
        }
    }
    //</editor-fold>
}