package com.enkigaming.lib.events;

import com.enkigaming.lib.collections.CollectionMethods;
import com.enkigaming.lib.encapsulatedfunctions.Converger;
import com.enkigaming.lib.events.exceptions.EventArgsMultipleUseException;
import com.enkigaming.lib.events.exceptions.EventArgsUsedPostBeforePreException;
import com.enkigaming.lib.misc.MutableWrapper;
import org.junit.Test;
import com.enkigaming.lib.testing.ThrowableAssertion;
import static org.junit.Assert.*;
import static com.enkigaming.lib.testing.Assert.*;
import com.enkigaming.lib.tuples.Pair;
import com.enkigaming.lib.tuples.Triplet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.NotImplementedException;

public abstract class EventTest
{
    public abstract Event<EventArgs> getNewEvent();
    
    public abstract EventArgs getNewArgs();
    
    public void assertThatEventDoesntHaveDependants(String msg, Event<? extends EventArgs> event)
    {
        Collection<Event<? extends EventArgs>> thisAsCollection = Arrays.<Event<? extends EventArgs>>asList(event);
        
        assertCollectionEquals(msg + ".1", event.getDependentEvents(true, true),      thisAsCollection);
        assertCollectionEquals(msg + ".2", event.getDependentEvents(true, false),     thisAsCollection);
        assertCollectionEmpty (msg + ".3", event.getDependentEvents(false, true));
        assertCollectionEmpty (msg + ".4", event.getDependentEvents(false, false));
        assertCollectionEmpty (msg + ".5", event.getDependentEvents());
        assertCollectionEmpty (msg + ".6", event.getDirectlyDependentEvents());
        assertCollectionEquals(msg + ".7", event.getThisAndDependentEvents(),         thisAsCollection);
        assertCollectionEquals(msg + ".8", event.getThisAndDirectlyDependentEvents(), thisAsCollection);
    }
    
    public void assertThatEventHasTheseDependants(String msg,
                                                  Event<? extends EventArgs> event,
                                                  Collection<Event<? extends EventArgs>> dependants,
                                                  Collection<Event<? extends EventArgs>> indirectDependants)
    {
        if(dependants.isEmpty())
        {
            assertThatEventDoesntHaveDependants(msg, event);
            return;
        }
        
        Collection<Event<? extends EventArgs>> thisAndDependants
            = CollectionMethods.combineCollections(true, Arrays.asList(event), dependants);
        
        Collection<Event<? extends EventArgs>> directAndIndirectDependants
            = CollectionMethods.combineCollections(true, dependants, indirectDependants);
        
        Collection<Event<? extends EventArgs>> thisAndDirectAndIndirectDependants
            = CollectionMethods.combineCollections(true, Arrays.asList(event), dependants, indirectDependants);
        
        assertCollectionEquals(msg + ".1", event.getDependentEvents(true, true),      thisAndDirectAndIndirectDependants);
        assertCollectionEquals(msg + ".2", event.getDependentEvents(true, false),     thisAndDependants);
        assertCollectionEquals(msg + ".3", event.getDependentEvents(false, true),     directAndIndirectDependants);
        assertCollectionEquals(msg + ".4", event.getDependentEvents(false, false),    dependants);
        assertCollectionEquals(msg + ".5", event.getDependentEvents(),                directAndIndirectDependants);
        assertCollectionEquals(msg + ".6", event.getDirectlyDependentEvents(),        dependants);
        assertCollectionEquals(msg + ".7", event.getThisAndDependentEvents(),         thisAndDirectAndIndirectDependants);
        assertCollectionEquals(msg + ".8", event.getThisAndDirectlyDependentEvents(), thisAndDependants);
    }
    
    public void assertThatEventDoesntHaveListeners(String msg, Event<? extends EventArgs> event)
    {
        assertCollectionEmpty(msg + ".1", event.getListeners());
        assertCollectionEmpty(msg + ".2", event.getDependentListeners(true, true));
        assertCollectionEmpty(msg + ".3", event.getDependentListeners(true, false));
        assertCollectionEmpty(msg + ".4", event.getDependentListeners(false, true));
        assertCollectionEmpty(msg + ".5", event.getDependentListeners(false, false));
        assertCollectionEmpty(msg + ".6", event.getDependentListeners());
        assertCollectionEmpty(msg + ".7", event.getDirectlyDependentListeners());
        assertCollectionEmpty(msg + ".8", event.getThisAndDependentListeners());
        assertCollectionEmpty(msg + ".9", event.getThisAndDirectlyDependentListeners());
    }
    
    public <T extends EventArgs> void assertThatEventHasTheseListeners(
            String msg,
            Event<T> event,
            Collection<EventListener<T>> listeners,
            Collection<EventListener<? extends EventArgs>> directlyDependentListeners,
            Collection<EventListener<? extends EventArgs>> indirectlyDependentListeners)
    {
        if(listeners.isEmpty() && directlyDependentListeners.isEmpty() && indirectlyDependentListeners.isEmpty())
        {
            assertThatEventDoesntHaveListeners(msg, event);
            return;
        }
        
        Collection<EventListener<? extends EventArgs>> thisAndDirectDependants
            = CollectionMethods.combineCollections(true, listeners, directlyDependentListeners);
        
        Collection<EventListener<? extends EventArgs>> directAndIndirectDependants
            = CollectionMethods.combineCollections(true, directlyDependentListeners, indirectlyDependentListeners);
        
        Collection<EventListener<? extends EventArgs>> thisAndDirectAndIndirectDependants
            = CollectionMethods.combineCollections(true, listeners, directAndIndirectDependants);
        
        assertCollectionEquals(msg + ".1", event.getListeners(), listeners);
        assertCollectionEquals(msg + ".2", event.getDependentListeners(true, true),      thisAndDirectAndIndirectDependants);
        assertCollectionEquals(msg + ".3", event.getDependentListeners(true, false),     thisAndDirectDependants);
        assertCollectionEquals(msg + ".4", event.getDependentListeners(false, true),     directAndIndirectDependants);
        assertCollectionEquals(msg + ".5", event.getDependentListeners(false, false),    directlyDependentListeners);
        assertCollectionEquals(msg + ".6", event.getDependentListeners(),                directAndIndirectDependants);
        assertCollectionEquals(msg + ".7", event.getDirectlyDependentListeners(),        directlyDependentListeners);
        assertCollectionEquals(msg + ".8", event.getThisAndDependentListeners(),         thisAndDirectAndIndirectDependants);
        assertCollectionEquals(msg + ".9", event.getThisAndDirectlyDependentListeners(), thisAndDirectDependants);
    }
    
    @Test
    public void testDependentEvents()
    {
        Event<EventArgs> event            = getNewEvent(), brotherEvent = getNewEvent(), parentEvent = getNewEvent();
        Event<EventArgs> grandparentEvent = getNewEvent(), uncleEvent   = getNewEvent(), cousinEvent = getNewEvent();
        
        Converger<Object, EventArgs, EventArgs> argsGetter = new Converger<Object, EventArgs, EventArgs>()
        {
            @Override
            public EventArgs get(Object first, EventArgs second)
            { return getNewArgs(); }
        };
        
        assertThatEventDoesntHaveDependants("1.1", event);
        assertThatEventDoesntHaveDependants("1.2", brotherEvent);
        assertThatEventDoesntHaveDependants("1.3", parentEvent);
        assertThatEventDoesntHaveDependants("1.4", grandparentEvent);
        assertThatEventDoesntHaveDependants("1.5", uncleEvent);
        assertThatEventDoesntHaveDependants("1.6", cousinEvent);
        
        grandparentEvent.register(parentEvent,  argsGetter);
        grandparentEvent.register(uncleEvent,   argsGetter);
        uncleEvent      .register(cousinEvent,  argsGetter);
        parentEvent     .register(event,        argsGetter);
        parentEvent     .register(brotherEvent, argsGetter);
        
        assertThatEventDoesntHaveDependants("2.1", event);
        assertThatEventDoesntHaveDependants("2.2", brotherEvent);
        assertThatEventHasTheseDependants("2.3",
                                          parentEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(event, brotherEvent),
                                          new ArrayList<Event<? extends EventArgs>>());
        assertThatEventHasTheseDependants("2.4",
                                          grandparentEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(parentEvent, uncleEvent),
                                          Arrays.<Event<? extends EventArgs>>asList(event, brotherEvent, cousinEvent));
        assertThatEventHasTheseDependants("2.5",
                                          uncleEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(cousinEvent),
                                          new ArrayList<Event<? extends EventArgs>>());
        assertThatEventDoesntHaveDependants("2.6", cousinEvent);
        
        parentEvent.deregister(brotherEvent);
        grandparentEvent.deregister(uncleEvent);
        
        assertThatEventDoesntHaveDependants("3.1", event);
        assertThatEventDoesntHaveDependants("3.2", brotherEvent);
        assertThatEventHasTheseDependants("3.3",
                                          parentEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(event),
                                          new ArrayList<Event<? extends EventArgs>>());
        assertThatEventHasTheseDependants("3.4",
                                          grandparentEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(parentEvent),
                                          Arrays.<Event<? extends EventArgs>>asList(event));
        assertThatEventHasTheseDependants("3.5",
                                          uncleEvent,
                                          Arrays.<Event<? extends EventArgs>>asList(cousinEvent),
                                          new ArrayList<Event<? extends EventArgs>>());
        assertThatEventDoesntHaveDependants("3.6", cousinEvent);
        
        grandparentEvent.deregister(parentEvent);
        uncleEvent      .deregister(cousinEvent);
        parentEvent     .deregister(event);
        
        assertThatEventDoesntHaveDependants("4.1", event);
        assertThatEventDoesntHaveDependants("4.2", brotherEvent);
        assertThatEventDoesntHaveDependants("4.3", parentEvent);
        assertThatEventDoesntHaveDependants("4.4", grandparentEvent);
        assertThatEventDoesntHaveDependants("4.5", uncleEvent);
        assertThatEventDoesntHaveDependants("4.6", cousinEvent);
    }
    
    @Test
    public void testListeners()
    {
        Event<EventArgs> event            = getNewEvent(), brotherEvent = getNewEvent(), parentEvent = getNewEvent(),
                         grandparentEvent = getNewEvent(), uncleEvent   = getNewEvent(), cousinEvent = getNewEvent();
        
        Converger<Object, EventArgs, EventArgs> argsGetter = new Converger<Object, EventArgs, EventArgs>()
        {
            @Override
            public EventArgs get(Object first, EventArgs second)
            { return getNewArgs(); }
        };
        
        //<editor-fold defaultstate="collapsed" desc="Listener declarations">
        EventListener<EventArgs> eventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> eventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> eventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> brotherEventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> brotherEventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> brotherEventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> parentEventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> parentEventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> parentEventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> grandparentEventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> grandparentEventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> grandparentEventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> uncleEventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> uncleEventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> uncleEventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> cousinEventListener1 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> cousinEventListener2 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        
        EventListener<EventArgs> cousinEventListener3 = new EventListener()
        { @Override public void onEvent(Object sender, EventArgs args) { /* Listener code */ } };
        //</editor-fold>
        
        assertThatEventDoesntHaveListeners("1.1", event);
        assertThatEventDoesntHaveListeners("1.2", brotherEvent);
        assertThatEventDoesntHaveListeners("1.3", parentEvent);
        assertThatEventDoesntHaveListeners("1.4", grandparentEvent);
        assertThatEventDoesntHaveListeners("1.5", uncleEvent);
        assertThatEventDoesntHaveListeners("1.6", cousinEvent);
        
        event           .register(eventListener1,            ListenerPriority.Normal );
        event           .register(eventListener2,            ListenerPriority.Monitor);
        event           .register(eventListener3,            ListenerPriority.Post   );
        brotherEvent    .register(brotherEventListener1,     ListenerPriority.Normal );
        brotherEvent    .register(brotherEventListener2,     ListenerPriority.Monitor);
        brotherEvent    .register(brotherEventListener3,     ListenerPriority.Post   );
        parentEvent     .register(parentEventListener1,      ListenerPriority.Normal );
        parentEvent     .register(parentEventListener2,      ListenerPriority.Monitor);
        parentEvent     .register(parentEventListener3,      ListenerPriority.Post   );
        grandparentEvent.register(grandparentEventListener1, ListenerPriority.Normal );
        grandparentEvent.register(grandparentEventListener2, ListenerPriority.Monitor);
        grandparentEvent.register(grandparentEventListener3, ListenerPriority.Post   );
        uncleEvent      .register(uncleEventListener1,       ListenerPriority.Normal );
        uncleEvent      .register(uncleEventListener2,       ListenerPriority.Monitor);
        uncleEvent      .register(uncleEventListener3,       ListenerPriority.Post   );
        cousinEvent     .register(cousinEventListener1,      ListenerPriority.Normal );
        cousinEvent     .register(cousinEventListener2,      ListenerPriority.Monitor);
        cousinEvent     .register(cousinEventListener3,      ListenerPriority.Post   );
        
        assertThatEventHasTheseListeners("2.1", event,
                                         Arrays.asList(eventListener1,
                                                       eventListener2,
                                                       eventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("2.2", brotherEvent,
                                         Arrays.asList(brotherEventListener1,
                                                       brotherEventListener2,
                                                       brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("2.3", parentEvent,
                                         Arrays.asList(parentEventListener1,
                                                       parentEventListener2,
                                                       parentEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("2.4", grandparentEvent,
                                         Arrays.asList(grandparentEventListener1,
                                                       grandparentEventListener2,
                                                       grandparentEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("2.5", brotherEvent,
                                         Arrays.asList(brotherEventListener1,
                                                       brotherEventListener2,
                                                       brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("2.6", parentEvent,
                                         Arrays.asList(parentEventListener1,
                                                       parentEventListener2,
                                                       parentEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        
        grandparentEvent.register(parentEvent,  argsGetter);
        grandparentEvent.register(uncleEvent,   argsGetter);
        parentEvent     .register(event,        argsGetter);
        parentEvent     .register(brotherEvent, argsGetter);
        uncleEvent      .register(cousinEvent,  argsGetter);
        
        
        assertThatEventHasTheseListeners("3.1", event,
                                         Arrays.asList(eventListener1,
                                                       eventListener2,
                                                       eventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("3.2", brotherEvent,
                                         Arrays.asList(brotherEventListener1,
                                                       brotherEventListener2,
                                                       brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("3.3", parentEvent,
                                         Arrays.asList(parentEventListener1,
                                                       parentEventListener2,
                                                       parentEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(eventListener1,
                                                                                           eventListener2,
                                                                                           eventListener3,
                                                                                           brotherEventListener1,
                                                                                           brotherEventListener2,
                                                                                           brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("3.4", grandparentEvent,
                                         Arrays.asList(grandparentEventListener1,
                                                       grandparentEventListener2,
                                                       grandparentEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(parentEventListener1,
                                                                                           parentEventListener2,
                                                                                           parentEventListener3,
                                                                                           uncleEventListener1,
                                                                                           uncleEventListener2,
                                                                                           uncleEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(eventListener1,
                                                                                           eventListener2,
                                                                                           eventListener3,
                                                                                           brotherEventListener1,
                                                                                           brotherEventListener2,
                                                                                           brotherEventListener3,
                                                                                           cousinEventListener1,
                                                                                           cousinEventListener2,
                                                                                           cousinEventListener3));
        
        assertThatEventHasTheseListeners("3.5", uncleEvent,
                                         Arrays.asList(uncleEventListener1,
                                                       uncleEventListener2,
                                                       uncleEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(cousinEventListener1,
                                                                                           cousinEventListener2,
                                                                                           cousinEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("3.6", cousinEvent,
                                         Arrays.asList(cousinEventListener1,
                                                       cousinEventListener2,
                                                       cousinEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        
        grandparentEvent.deregister(uncleEvent);
        
        
        assertThatEventHasTheseListeners("4.1", event,
                                         Arrays.asList(eventListener1,
                                                       eventListener2,
                                                       eventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("4.2", brotherEvent,
                                         Arrays.asList(brotherEventListener1,
                                                       brotherEventListener2,
                                                       brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("4.3", parentEvent,
                                         Arrays.asList(parentEventListener1,
                                                       parentEventListener2,
                                                       parentEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(eventListener1,
                                                                                           eventListener2,
                                                                                           eventListener3,
                                                                                           brotherEventListener1,
                                                                                           brotherEventListener2,
                                                                                           brotherEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("4.4", grandparentEvent,
                                         Arrays.asList(grandparentEventListener1,
                                                       grandparentEventListener2,
                                                       grandparentEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(parentEventListener1,
                                                                                           parentEventListener2,
                                                                                           parentEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(eventListener1,
                                                                                           eventListener2,
                                                                                           eventListener3,
                                                                                           brotherEventListener1,
                                                                                           brotherEventListener2,
                                                                                           brotherEventListener3));
        
        assertThatEventHasTheseListeners("4.5", uncleEvent,
                                         Arrays.asList(uncleEventListener1,
                                                       uncleEventListener2,
                                                       uncleEventListener3),
                                         Arrays.<EventListener<? extends EventArgs>>asList(cousinEventListener1,
                                                                                           cousinEventListener2,
                                                                                           cousinEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>());
        
        assertThatEventHasTheseListeners("4.6", cousinEvent,
                                         Arrays.asList(cousinEventListener1,
                                                       cousinEventListener2,
                                                       cousinEventListener3),
                                         new ArrayList<EventListener<? extends EventArgs>>(),
                                         new ArrayList<EventListener<? extends EventArgs>>());
    }
    
    @Test
    public void testRaise()
    {
        final Event<EventArgs> event =  getNewEvent();
        EventArgs args;
        
        final MutableWrapper<String> flag = new MutableWrapper<String>("Not set");
        final String preRaiseListenedTo     = "Pre-raise event listened to.",
                     monitorRaiseListenedTo = "Monitor event listened to.",
                     postRaiseListenedTo    = "Post-raise event listened to.";

        EventListener<EventArgs> listener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                System.out.println("PreRaise listened to.");
                flag.set(preRaiseListenedTo);
            }
        };
        
        EventListener<EventArgs> monitorListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                System.out.println("Monitor listened to.");
                flag.set(monitorRaiseListenedTo);
            }
        };
        
        EventListener<EventArgs> postListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                System.out.println("PostRaise listened to.");
                flag.set(postRaiseListenedTo);
            }
        };
        
        System.out.println("Definately working ...");
        
        event.register(listener);
        event.register(monitorListener, ListenerPriority.Monitor);
        event.register(postListener, ListenerPriority.Post);
        
        assertEquals("1.1", "Not set", flag.get());
        args = getNewArgs();
        
        event.raise(this, args);
        assertEquals("1.2", monitorRaiseListenedTo, flag.get());
        System.out.println("Finished pre raise, about to start post raise.");
        event.raisePostEvent(this, args);
        assertEquals("1.3", postRaiseListenedTo, flag.get());
        
        event.deregister(listener);
        event.deregister(monitorListener);
        event.deregister(postListener);
        
        flag.set("Not set");
        args = getNewArgs();
        
        event.raise(this, args);
        event.raisePostEvent(this, args);
        
        assertEquals("2", "Not set", flag.get());
        
        final MutableWrapper<Boolean>
        first = new MutableWrapper<Boolean>(false), second   = new MutableWrapper<Boolean>(false),
        third = new MutableWrapper<Boolean>(false), fourth   = new MutableWrapper<Boolean>(false),
        fifth = new MutableWrapper<Boolean>(false), monitor  = new MutableWrapper<Boolean>(false),
        post  = new MutableWrapper<Boolean>(false), happened = new MutableWrapper<Boolean>(false);
        
        EventListener<EventArgs> firstListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertFalse("3.1.1", first  .get());
                assertFalse("3.1.2", second .get());
                assertFalse("3.1.3", third  .get());
                assertFalse("3.1.4", fourth .get());
                assertFalse("3.1.5", fifth  .get());
                assertFalse("3.1.6", monitor.get());
                assertFalse("3.1.7", post   .get());
                
                first.set(true);
                
                assertFalse("3.1.8", happened.get());
                assertTrue ("3.1.9", args.shouldBeMutable());
            }
        };
        
        EventListener<EventArgs> secondListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.2.1", first  .get());
                assertFalse("3.2.2", second .get());
                assertFalse("3.2.3", third  .get());
                assertFalse("3.2.4", fourth .get());
                assertFalse("3.2.5", fifth  .get());
                assertFalse("3.2.6", monitor.get());
                assertFalse("3.2.7", post   .get());
                
                second.set(true);
                
                assertFalse("3.2.8", happened.get());
                assertTrue ("3.2.9", args.shouldBeMutable());
            }
        };
        
        EventListener<EventArgs> thirdListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.3.1", first  .get());
                assertTrue ("3.3.2", second .get());
                assertFalse("3.3.3", third  .get());
                assertFalse("3.3.4", fourth .get());
                assertFalse("3.3.5", fifth  .get());
                assertFalse("3.3.6", monitor.get());
                assertFalse("3.3.7", post   .get());
                
                third.set(true);
                
                assertFalse("3.3.8", happened.get());
                assertTrue ("3.3.9", args.shouldBeMutable());
            }
        };
        
        EventListener<EventArgs> fourthListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.4.1", first  .get());
                assertTrue ("3.4.2", second .get());
                assertTrue ("3.4.3", third  .get());
                assertFalse("3.4.4", fourth .get());
                assertFalse("3.4.5", fifth  .get());
                assertFalse("3.4.6", monitor.get());
                assertFalse("3.4.7", post   .get());
                
                fourth.set(true);
                
                assertFalse("3.4.8", happened.get());
                assertTrue ("3.4.9", args.shouldBeMutable());
            }
        };
        
        EventListener<EventArgs> fifthListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.5.1", first  .get());
                assertTrue ("3.5.2", second .get());
                assertTrue ("3.5.3", third  .get());
                assertTrue ("3.5.4", fourth .get());
                assertFalse("3.5.5", fifth  .get());
                assertFalse("3.5.6", monitor.get());
                assertFalse("3.5.7", post   .get());
                
                fifth.set(true);
                
                assertFalse("3.5.8", happened.get());
                assertTrue ("3.5.9", args.shouldBeMutable());
            }
        };
        
        monitorListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.6.1", first  .get());
                assertTrue ("3.6.2", second .get());
                assertTrue ("3.6.3", third  .get());
                assertTrue ("3.6.4", fourth .get());
                assertTrue ("3.6.5", fifth  .get());
                assertFalse("3.6.6", monitor.get());
                assertFalse("3.6.7", post   .get());
                
                monitor.set(true);
                
                assertFalse("3.6.8", happened.get());
                assertFalse("3.6.9", args.shouldBeMutable());
            }
        };
        
        postListener = new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            {
                assertTrue ("3.7.1", first  .get());
                assertTrue ("3.7.2", second .get());
                assertTrue ("3.7.3", third  .get());
                assertTrue ("3.7.4", fourth .get());
                assertTrue ("3.7.5", fifth  .get());
                assertTrue ("3.7.6", monitor.get());
                assertFalse("3.7.7", post   .get());
                
                post.set(true);
                
                assertTrue ("3.7.8", happened.get());
                assertFalse("3.7.9", args.shouldBeMutable());
            }
        };
        
        event.register(firstListener,   ListenerPriority.VeryEarly);
        event.register(secondListener,  ListenerPriority.Early    );
        event.register(thirdListener,   ListenerPriority.Normal   );
        event.register(fourthListener,  ListenerPriority.Late     );
        event.register(fifthListener,   ListenerPriority.VeryLate );
        event.register(monitorListener, ListenerPriority.Monitor  );
        event.register(postListener,    ListenerPriority.Post     );
        
        args = getNewArgs();
        
        event.raise(this, args);
        happened.set(true);
        event.raisePostEvent(this, args);
        
        final EventArgs argsForMUExceptionChecks = args;
        
        new ThrowableAssertion("4.1.1", EventArgsMultipleUseException.class)
        {
            @Override
            public void code() throws Throwable
            { event.raise(EventTest.this, argsForMUExceptionChecks); }
        };
        
        new ThrowableAssertion("4.1.2", EventArgsMultipleUseException.class)
        {
            @Override
            public void code() throws Throwable
            { event.raisePostEvent(EventTest.this, argsForMUExceptionChecks); }
        };
        
        final EventArgs argsForUsedPostBeforePreCheck = getNewArgs();
        
        new ThrowableAssertion("4.2.1", EventArgsUsedPostBeforePreException.class)
        {
            @Override
            public void code() throws Throwable
            { event.raisePostEvent(EventTest.this, argsForUsedPostBeforePreCheck); }
        };
    }
    
    @Test
    public void testDependantRaise()
    {
        Event<EventArgs> childEvent       = getNewEvent(),
                         parentEvent      = getNewEvent(),
                         brotherEvent     = getNewEvent(),
                         uncleEvent       = getNewEvent(),
                         cousinEvent      = getNewEvent(),
                         grandparentEvent = getNewEvent();
        
        EventArgs args;
        
        Converger<Object, EventArgs, EventArgs> argsGetter = new Converger<Object, EventArgs, EventArgs>()
        {
            @Override
            public EventArgs get(Object sender, EventArgs parentArgs)
            { return getNewArgs(); }
        };
        
        MutableWrapper<Boolean> childPreFlag        = new MutableWrapper<Boolean>(false),
                                childPostFlag       = new MutableWrapper<Boolean>(false),
                                parentPreFlag       = new MutableWrapper<Boolean>(false),
                                parentPostFlag      = new MutableWrapper<Boolean>(false),
                                brotherPreFlag      = new MutableWrapper<Boolean>(false),
                                brotherPostFlag     = new MutableWrapper<Boolean>(false),
                                unclePreFlag        = new MutableWrapper<Boolean>(false),
                                unclePostFlag       = new MutableWrapper<Boolean>(false),
                                cousinPreFlag       = new MutableWrapper<Boolean>(false),
                                cousinPostFlag      = new MutableWrapper<Boolean>(false),
                                grandparentPreFlag  = new MutableWrapper<Boolean>(false),
                                grandparentPostFlag = new MutableWrapper<Boolean>(false);
        
        childEvent      .register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(childPreFlag       ));
        childEvent      .register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(childPostFlag      ));
        parentEvent     .register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(parentPreFlag      ));
        parentEvent     .register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(parentPostFlag     ));
        brotherEvent    .register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(brotherPreFlag     ));
        brotherEvent    .register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(brotherPostFlag    ));
        uncleEvent      .register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(unclePreFlag       ));
        uncleEvent      .register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(unclePostFlag      ));
        cousinEvent     .register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(cousinPreFlag      ));
        cousinEvent     .register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(cousinPostFlag     ));
        grandparentEvent.register(ListenerPriority.Normal, getListenerThatSetsFlagToTrue(grandparentPreFlag ));
        grandparentEvent.register(ListenerPriority.Post,   getListenerThatSetsFlagToTrue(grandparentPostFlag));
        
        //<editor-fold defaultstate="collapsed" desc="Tests where no dependent events are registered.">
        assertAllFalse("1.1", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                              parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                              brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                              unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                              cousinPreFlag     .get(), cousinPostFlag     .get(),  // 9, 10
                              grandparentPreFlag.get(), grandparentPostFlag.get()); // 11, 12
        
        args = getNewArgs();
        
        grandparentEvent.raise(this, args);
        grandparentEvent.raisePostEvent(this, args);
        
        assertAllFalse("1.2.1", childPreFlag  .get(), childPostFlag  .get(),  // 1, 2
                                parentPreFlag .get(), parentPostFlag .get(),  // 3, 4
                                brotherPreFlag.get(), brotherPostFlag.get(),  // 5, 6
                                unclePreFlag  .get(), unclePostFlag  .get(),  // 7, 8
                                cousinPreFlag .get(), cousinPostFlag .get()); // 9, 10
        
        assertAllTrue("1.2.2", grandparentPreFlag.get(), grandparentPostFlag.get()); // 1, 2
        
        args = getNewArgs();
        
        parentEvent.raise(this, args);
        parentEvent.raisePostEvent(this, args);
        
        assertAllFalse("1.3.1", childPreFlag  .get(), childPostFlag  .get(),  // 1, 2
                                brotherPreFlag.get(), brotherPostFlag.get(),  // 3, 4
                                unclePreFlag  .get(), unclePostFlag  .get(),  // 5, 6
                                cousinPreFlag .get(), cousinPostFlag .get()); // 7, 8
        
        assertAllTrue("1.3.2", parentPreFlag     .get(), parentPostFlag     .get(),  // 1, 2
                               grandparentPreFlag.get(), grandparentPostFlag.get()); // 3, 4
        
        args = getNewArgs();
        
        uncleEvent.raise(this, args);
        uncleEvent.raisePostEvent(this, args);
        
        assertAllFalse("1.4.1", childPreFlag  .get(), childPostFlag  .get(),  // 1, 2
                                brotherPreFlag.get(), brotherPostFlag.get(),  // 3, 4
                                cousinPreFlag .get(), cousinPostFlag .get()); // 5, 6
        
        assertAllTrue("1.4.2", parentPreFlag     .get(), parentPostFlag     .get(),  // 1, 2
                               unclePreFlag      .get(), unclePostFlag      .get(),  // 3, 4
                               grandparentPreFlag.get(), grandparentPostFlag.get()); // 5, 6
        
        args = getNewArgs();
        
        childEvent.raise(this, args);
        childEvent.raisePostEvent(this, args);
        
        assertAllFalse("1.5.1", brotherPreFlag.get(), brotherPostFlag.get(),  // 1, 2
                                cousinPreFlag .get(), cousinPostFlag .get()); // 3, 4
        
        assertAllTrue("1.5.2", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                               parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                               unclePreFlag      .get(), unclePostFlag      .get(),  // 5, 6
                               grandparentPreFlag.get(), grandparentPostFlag.get()); // 7, 8
        
        args = getNewArgs();
        
        brotherEvent.raise(this, args);
        brotherEvent.raisePostEvent(this, args);
        
        assertAllFalse("1.6.1", cousinPreFlag.get(), cousinPostFlag.get()); // 1, 2
        
        assertAllTrue("1.6.2", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                               parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                               brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                               unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                               grandparentPreFlag.get(), grandparentPostFlag.get()); // 9, 10
        
        args = getNewArgs();
        
        cousinEvent.raise(this, args);
        cousinEvent.raisePostEvent(this, args);
        
        assertAllTrue("1.7", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                             parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                             brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                             unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                             cousinPreFlag     .get(), cousinPostFlag     .get(),  // 9, 10
                             grandparentPreFlag.get(), grandparentPostFlag.get()); // 11, 12
        //</editor-fold>
        
        grandparentEvent.register(parentEvent,  argsGetter);
        grandparentEvent.register(uncleEvent,   argsGetter);
        parentEvent     .register(childEvent,   argsGetter);
        parentEvent     .register(brotherEvent, argsGetter);
        uncleEvent      .register(cousinEvent,  argsGetter);
        
        MutableWrapper.setAll(false, childPreFlag,   childPostFlag,   parentPreFlag,      parentPostFlag,
                                     brotherPreFlag, brotherPostFlag, unclePreFlag,       unclePostFlag,
                                     cousinPreFlag,  cousinPostFlag,  grandparentPreFlag, grandparentPostFlag);
        
        args = getNewArgs();
        
        grandparentEvent.raise(this, args);
        grandparentEvent.raisePostEvent(this, args);
        
        assertAllTrue("2.1", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                             parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                             brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                             unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                             cousinPreFlag     .get(), cousinPostFlag     .get(),  // 9, 10
                             grandparentPreFlag.get(), grandparentPostFlag.get()); // 11, 12
        
        MutableWrapper.setAll(false, childPreFlag,   childPostFlag,   parentPreFlag,      parentPostFlag,
                                     brotherPreFlag, brotherPostFlag, unclePreFlag,       unclePostFlag,
                                     cousinPreFlag,  cousinPostFlag,  grandparentPreFlag, grandparentPostFlag);
        
        args = getNewArgs();
        
        parentEvent.raise(this, args);
        parentEvent.raisePostEvent(this, args);
        
        assertAllFalse("2.2.1", unclePreFlag      .get(), unclePostFlag      .get(),  // 1, 2
                                cousinPreFlag     .get(), cousinPostFlag     .get(),  // 3, 4
                                grandparentPreFlag.get(), grandparentPostFlag.get()); // 5, 6
        
        assertAllTrue("2.2.2", childPreFlag  .get(), childPostFlag  .get(),  // 1, 2
                               parentPreFlag .get(), parentPostFlag .get(),  // 3, 4
                               brotherPreFlag.get(), brotherPostFlag.get()); // 5, 6
        
        args = getNewArgs();
        
        uncleEvent.raise(this, args);
        uncleEvent.raisePostEvent(this, args);
        
        assertAllFalse("2.3.1", grandparentPreFlag.get(), grandparentPostFlag.get()); // 1, 2
        
        assertAllTrue("2.3.2", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                               parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                               brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                               unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                               cousinPreFlag     .get(), cousinPostFlag     .get()); // 9, 10
        
        MutableWrapper.setAll(false, childPreFlag,   childPostFlag,   parentPreFlag,      parentPostFlag,
                                     brotherPreFlag, brotherPostFlag, unclePreFlag,       unclePostFlag,
                                     cousinPreFlag,  cousinPostFlag);
        
        args = getNewArgs();
        
        childEvent.raise(this, args);
        childEvent.raisePostEvent(this, args);
        
        assertAllFalse("2.4.1", parentPreFlag     .get(), parentPostFlag     .get(),  // 1, 2
                                brotherPreFlag    .get(), brotherPostFlag    .get(),  // 3, 4
                                unclePreFlag      .get(), unclePostFlag      .get(),  // 5, 6
                                cousinPreFlag     .get(), cousinPostFlag     .get(),  // 7, 8
                                grandparentPreFlag.get(), grandparentPostFlag.get()); // 9, 10
        
        assertAllTrue("2.4.2", childPreFlag.get(), childPostFlag.get()); // 1, 2
        
        MutableWrapper.setAll(false, childPreFlag, childPostFlag);
        
        grandparentEvent.deregister(parentEvent, uncleEvent);
        parentEvent.deregister(childEvent, brotherEvent);
        uncleEvent.deregister(cousinEvent);
        
        args = getNewArgs();
        
        grandparentEvent.raise(this, args);
        grandparentEvent.raisePostEvent(this, args);
        
        assertAllFalse("3.1", childPreFlag      .get(), childPostFlag      .get(),  // 1, 2
                              parentPreFlag     .get(), parentPostFlag     .get(),  // 3, 4
                              brotherPreFlag    .get(), brotherPostFlag    .get(),  // 5, 6
                              unclePreFlag      .get(), unclePostFlag      .get(),  // 7, 8
                              cousinPreFlag     .get(), cousinPostFlag     .get()); // 9, 10
        
        assertAllTrue("3.2", grandparentPreFlag.get(), grandparentPostFlag.get()); // 1, 2
    }
    
    public EventListener<EventArgs> getListenerThatSetsFlagToTrue(final MutableWrapper<Boolean> flag)
    {
        return new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            { flag.set(true); System.out.println("Flat set to true."); }
        };
    }
    
    public EventListener<EventArgs> getListenerThatIncrementsFlag(final MutableWrapper<Integer> flag)
    {
        return new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            { MutableWrapper.ExtensionMethods.add(flag, 1); }
        };
    }
    
    public static class listenerThatSetsFlagToTrue implements EventListener<EventArgs>
    {
        public listenerThatSetsFlagToTrue(MutableWrapper<Boolean> flag)
        { this.flag = flag; }
        
        MutableWrapper<Boolean> flag;
        
        @Override
        public void onEvent(Object sender, EventArgs args)
        { flag.set(true); }
    }
    
    @Test
    public void testMultipleRaise()
    {
        Event<EventArgs>[] events = new Event[4];
        MutableWrapper<Boolean>[] eventRaisePreFlags = new MutableWrapper[4];
        MutableWrapper<Boolean>[] eventRaisePostFlags = new MutableWrapper[4];
        EventListener<EventArgs>[] preListeners = new EventListener[4];
        EventListener<EventArgs>[] postListeners = new EventListener[4];
        EventArgs[] args = new EventArgs[4];
        
        for(int i = 0; i < events.length; i++)
        {
            events[i] = getNewEvent();
            eventRaisePreFlags[i] = new MutableWrapper<Boolean>(false);
            eventRaisePostFlags[i] = new MutableWrapper<Boolean>(false);
            preListeners[i] = getListenerThatSetsFlagToTrue(eventRaisePreFlags[i]);
            postListeners[i] = getListenerThatSetsFlagToTrue(eventRaisePostFlags[i]);
            events[i].register(preListeners[i], ListenerPriority.Normal);
            events[i].register(postListeners[i], ListenerPriority.Post);
            args[i] = getNewArgs();
        }
        
        events[0].raiseAlongside(this, args[0], new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]));
        
        assertTrue ("1.1.1.1", eventRaisePreFlags [0].get());
        assertTrue ("1.1.1.2", eventRaisePreFlags [1].get());
        assertFalse("1.1.1.3", eventRaisePostFlags[0].get());
        assertFalse("1.1.1.4", eventRaisePostFlags[1].get());
        
        MutableWrapper.setAll(false, eventRaisePreFlags);
        
        events[0].raisePostEventAlongside(this, args[0], new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]));
        
        assertFalse("1.1.2.1", eventRaisePreFlags [0].get());
        assertFalse("1.1.2.2", eventRaisePreFlags [1].get());
        assertTrue ("1.1.2.3", eventRaisePostFlags[0].get());
        assertTrue ("1.1.2.4", eventRaisePostFlags[1].get());
        
        MutableWrapper.setAll(false, eventRaisePostFlags);
        
        for(int i = 0; i < args.length; i++)
            args[i] = getNewArgs();
        
        events[0].raiseAlongside(this, args[0], new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]),
                                                new Pair<Event<EventArgs>, EventArgs>(events[2], args[2]),
                                                new Pair<Event<EventArgs>, EventArgs>(events[3], args[3]));
        
        assertTrue ("1.2.1.1", eventRaisePreFlags [0].get());
        assertTrue ("1.2.1.2", eventRaisePreFlags [1].get());
        assertTrue ("1.2.1.3", eventRaisePreFlags [2].get());
        assertTrue ("1.2.1.4", eventRaisePreFlags [3].get());
        assertFalse("1.2.1.5", eventRaisePostFlags[0].get());
        assertFalse("1.2.1.6", eventRaisePostFlags[1].get());
        assertFalse("1.2.1.7", eventRaisePostFlags[2].get());
        assertFalse("1.2.1.8", eventRaisePostFlags[3].get());
        
        MutableWrapper.setAll(false, eventRaisePreFlags);
        
        events[0].raisePostEventAlongside(this, args[0], new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]),
                                                         new Pair<Event<EventArgs>, EventArgs>(events[2], args[2]),
                                                         new Pair<Event<EventArgs>, EventArgs>(events[3], args[3]));
        
        assertFalse("1.2.2.1", eventRaisePreFlags [0].get());
        assertFalse("1.2.2.2", eventRaisePreFlags [1].get());
        assertFalse("1.2.2.3", eventRaisePreFlags [2].get());
        assertFalse("1.2.2.4", eventRaisePreFlags [3].get());
        assertTrue ("1.2.2.5", eventRaisePostFlags[0].get());
        assertTrue ("1.2.2.6", eventRaisePostFlags[1].get());
        assertTrue ("1.2.2.7", eventRaisePostFlags[2].get());
        assertTrue ("1.2.2.8", eventRaisePostFlags[3].get());
        
        MutableWrapper.setAll(false, eventRaisePostFlags);
        
        for(int i = 0; i < args.length; i++)
            args[i] = getNewArgs();
        
        events[0].raiseAlongside(this, args[0], Arrays.asList(new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]),
                                                              new Pair<Event<EventArgs>, EventArgs>(events[2], args[2]),
                                                              new Pair<Event<EventArgs>, EventArgs>(events[3], args[3])));
        
        assertTrue ("1.3.1.1", eventRaisePreFlags [0].get());
        assertTrue ("1.3.1.2", eventRaisePreFlags [1].get());
        assertTrue ("1.3.1.3", eventRaisePreFlags [2].get());
        assertTrue ("1.3.1.4", eventRaisePreFlags [3].get());
        assertFalse("1.3.1.5", eventRaisePostFlags[0].get());
        assertFalse("1.3.1.6", eventRaisePostFlags[1].get());
        assertFalse("1.3.1.7", eventRaisePostFlags[2].get());
        assertFalse("1.3.1.8", eventRaisePostFlags[3].get());
        
        MutableWrapper.setAll(false, eventRaisePreFlags);
        
        events[0].raisePostEventAlongside(this, args[0], Arrays.asList(new Pair<Event<EventArgs>, EventArgs>(events[1], args[1]),
                                                                       new Pair<Event<EventArgs>, EventArgs>(events[2], args[2]),
                                                                       new Pair<Event<EventArgs>, EventArgs>(events[3], args[3])));
        
        assertFalse("1.3.2.1", eventRaisePreFlags [0].get());
        assertFalse("1.3.2.2", eventRaisePreFlags [1].get());
        assertFalse("1.3.2.3", eventRaisePreFlags [2].get());
        assertFalse("1.3.2.4", eventRaisePreFlags [3].get());
        assertTrue ("1.3.2.5", eventRaisePostFlags[0].get());
        assertTrue ("1.3.2.6", eventRaisePostFlags[1].get());
        assertTrue ("1.3.2.7", eventRaisePostFlags[2].get());
        assertTrue ("1.3.2.8", eventRaisePostFlags[3].get());
    }
    
    public EventListener<EventArgs> getCancellingListener()
    {
        return new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            { args.setCancelled(true); System.out.println("Event listened to and cancelled.");}
        };
    }
    
    public EventListener<EventArgs> getListener()
    {
        return new EventListener<EventArgs>()
        {
            @Override
            public void onEvent(Object sender, EventArgs args)
            { System.out.println("Event listened to."); }
        };
    }
    
    /*  These tests operate on the principle that any listener can find out if any other listener is cancelling the
        event raise, which isn't the case. Leaving here incase I ever decide to implement something like that. At the
        moment, cancellation state as accessible by an event listener is simply how it was left by the previous event
        listener.
    
    @Test
    public void testMultipleRaiseCancellationSharing()
    {
        testMultipleRaiseCancellationSharing_testGroup("m nc",             false, 0, 0);
        testMultipleRaiseCancellationSharing_testGroup("m c",              true,  0, 0);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{1nc}",     false, 1, 0);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{3nc}",     false, 3, 0);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{1nc}",      true,  1, 0);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{3nc}",      true,  3, 0);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{1c}",      false, 0, 1);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{1c}",       true,  0, 1);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{1nc, 1c}", false, 1, 1);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{3nc, 1c}", false, 3, 1);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{1nc, 1c}",  true,  1, 1);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{3nc, 1c}",  true,  3, 1);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{3c}",      false, 0, 3);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{3c}",       true,  0, 3);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{1nc, 3c}", false, 1, 3);
        testMultipleRaiseCancellationSharing_testGroup("m nc w/{3nc, 3c}", false, 3, 3);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{1nc, 3c}",  true,  1, 3);
        testMultipleRaiseCancellationSharing_testGroup("m c w/{3nc, 3c}",  true,  3, 3);
    }
    
    public void testMultipleRaiseCancellationSharing_testGroup(String msg,
                                                               boolean mainCancels,
                                                               int noncancellingDependentEvents,
                                                               int cancellingDependentEvents)
    {
        boolean mc = mainCancels;
        int ncd = noncancellingDependentEvents;
        int cd = cancellingDependentEvents;
        
        testMultipleRaiseCancellationSharing_testGroup(msg,                       mc, ncd, cd, new int[]{0, 0, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc",             mc, ncd, cd, new int[]{1, 0, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc",             mc, ncd, cd, new int[]{3, 0, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{1nc}",     mc, ncd, cd, new int[]{1, 1, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{3nc}",     mc, ncd, cd, new int[]{1, 3, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{1nc}",     mc, ncd, cd, new int[]{3, 1, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{3nc}",     mc, ncd, cd, new int[]{3, 3, 0});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{1c}",      mc, ncd, cd, new int[]{1, 0, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{3c}",      mc, ncd, cd, new int[]{1, 0, 3});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{1nc, 1c}", mc, ncd, cd, new int[]{1, 1, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{1nc, 3c}", mc, ncd, cd, new int[]{1, 1, 3});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{3nc, 1c}", mc, ncd, cd, new int[]{1, 3, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".1 nc w/{3nc, 3c}", mc, ncd, cd, new int[]{1, 3, 3});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{1c}",      mc, ncd, cd, new int[]{3, 0, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{3c}",      mc, ncd, cd, new int[]{3, 0, 3});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{1nc, 1c}", mc, ncd, cd, new int[]{3, 1, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{1nc, 3c}", mc, ncd, cd, new int[]{3, 1, 3});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{3nc, 1c}", mc, ncd, cd, new int[]{3, 3, 1});
        testMultipleRaiseCancellationSharing_testGroup(msg + ".3 nc w/{3nc, 3c}", mc, ncd, cd, new int[]{3, 3, 3});
    }
    
    public void testMultipleRaiseCancellationSharing_testGroup(String msg,
                                                               boolean mainCancels,
                                                               int noncancellingDependentEvents,
                                                               int cancellingDependentEvents,
                                                               int[] noncancellingAlongsides)
    {
        boolean mc = mainCancels;
        int ncd = noncancellingDependentEvents;
        int cd = cancellingDependentEvents;
        int[] nca = noncancellingAlongsides;
        
        testMultipleRaiseCancellationSharing(msg,                      mc, ncd, cd, nca, new int[]{0, 0, 0});
        testMultipleRaiseCancellationSharing(msg + ".1 c",             mc, ncd, cd, nca, new int[]{1, 0, 0});
        testMultipleRaiseCancellationSharing(msg + ".3 c",             mc, ncd, cd, nca, new int[]{3, 0, 0});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{1nc}",     mc, ncd, cd, nca, new int[]{1, 1, 0});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{3nc}",     mc, ncd, cd, nca, new int[]{1, 3, 0});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{1nc}",     mc, ncd, cd, nca, new int[]{3, 1, 0});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{3nc}",     mc, ncd, cd, nca, new int[]{3, 3, 0});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{1c}",      mc, ncd, cd, nca, new int[]{1, 0, 1});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{3c}",      mc, ncd, cd, nca, new int[]{1, 0, 3});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{1nc, 1c}", mc, ncd, cd, nca, new int[]{1, 1, 1});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{1nc, 3c}", mc, ncd, cd, nca, new int[]{1, 1, 3});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{3nc, 1c}", mc, ncd, cd, nca, new int[]{1, 3, 1});
        testMultipleRaiseCancellationSharing(msg + ".1 c w/{3nc, 3c}", mc, ncd, cd, nca, new int[]{1, 3, 3});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{1c}",      mc, ncd, cd, nca, new int[]{3, 0, 1});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{3c}",      mc, ncd, cd, nca, new int[]{3, 0, 3});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{1nc, 1c}", mc, ncd, cd, nca, new int[]{3, 1, 1});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{1nc, 3c}", mc, ncd, cd, nca, new int[]{3, 1, 3});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{3nc, 1c}", mc, ncd, cd, nca, new int[]{3, 3, 1});
        testMultipleRaiseCancellationSharing(msg + ".3 c w/{3nc, 3c}", mc, ncd, cd, nca, new int[]{3, 3, 3});
    }
    
    public void testMultipleRaiseCancellationSharing(String msg,
                                                     boolean mainCancels,
                                                     int noncancellingDependentEvents,
                                                     int cancellingDependentEvents,
                                                     int[] noncancellingAlongsides,
                                                     int[] cancellingAlongsides)
    {
        // non/cancellingAlongsides = {number of alongsides,
        //                             number of noncancelling dependants,
        //                             number of cancelling dependants}
        
        Event<EventArgs> mainEvent = getNewEvent();
        Pair<Event<EventArgs>, Boolean>[] mainEventDependants;
        Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]>[] alongsideEvents;
        
        mainEventDependants = new Pair[noncancellingDependentEvents + cancellingDependentEvents];
        
        for(int i = 0; i < mainEventDependants.length; i++)
            mainEventDependants[i] = new Pair<Event<EventArgs>, Boolean>(getNewEvent(), i < cancellingDependentEvents);
        
        alongsideEvents = new Triplet[noncancellingAlongsides[0] + cancellingAlongsides[0]];
        
        for(int i = 0; i < alongsideEvents.length; i++)
        {
            boolean thisIsCancelling = i < noncancellingAlongsides[0];
            int dependantCount = thisIsCancelling
                               ? noncancellingAlongsides[1] + noncancellingAlongsides[2]
                               :    cancellingAlongsides[1] +    cancellingAlongsides[2];
            
            alongsideEvents[i] = new Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]>(
                    getNewEvent(),
                    !(i < noncancellingAlongsides[0]),
                    new Pair[dependantCount]);
            
            for(int j = 0; j < dependantCount; j++)
            {
                alongsideEvents[i].getThird()[j] = new Pair<Event<EventArgs>, Boolean>(
                            getNewEvent(),
                            j < (thisIsCancelling ?    cancellingAlongsides[1]
                                                  : noncancellingAlongsides[1]));
            }
        }
        
        testMultipleRaiseCancellationSharing(msg, mainEvent, mainCancels, mainEventDependants, alongsideEvents);
    }
        
    public void testMultipleRaiseCancellationSharing(String msg,
                                                     Event<EventArgs> mainEvent,
                                                     boolean mainCancels,
                                                     Pair<Event<EventArgs>, Boolean>[] mainEventDependants,
                                                     Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]>[] alongsideEvents)
    {
        // mainEventDependants:
        // Pair < Event,
        //        whether or not that particular event should be cancelling
        //      >
        
        // alongsideEvents
        // Triplet < Event,
        //           Whether or not that particular event should be cancelling,
        //           array of Pairs < Events dependant on the event in the triplet,
        //                            Whether or not *these* particular events should be cancelling.
        //                          >
        //         >
        
        EventArgs mainArgs = getNewArgs();
        
        //<editor-fold defaultstate="collapsed" desc="Register dependent events">
        for(Pair<Event<EventArgs>, Boolean> i : mainEventDependants)
            mainEvent.register(i.getFirst(), new Converger<Object, EventArgs, EventArgs>()
            {
                @Override
                public EventArgs get(Object first, EventArgs second)
                { return getNewArgs(); }
            });
        
        for(Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]> i : alongsideEvents)
            for(Pair<Event<EventArgs>, Boolean> j : i.getThird())
                i.getFirst().register(j.getFirst(), new Converger<Object, EventArgs, EventArgs>()
                {
                    @Override
                    public EventArgs get(Object first, EventArgs second)
                    { return getNewArgs(); }
                });
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Register appropriate listeners">
        mainEvent.register(mainCancels ? getCancellingListener() : getListener());
        
        for(Pair<Event<EventArgs>, Boolean> i : mainEventDependants)
            i.getFirst().register(i.getSecond() ? getCancellingListener() : getListener());
        
        for(Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]> i : alongsideEvents)
        {
            i.getFirst().register(i.getSecond() ? getCancellingListener() : getListener());
            
            for(Pair<Event<EventArgs>, Boolean> j : i.getThird())
                j.getFirst().register(j.getSecond() ? getCancellingListener() : getListener());
        }
        //</editor-fold>
        
        boolean shouldBeCancelledMainOnly = false;
        boolean shouldBeCancelledAll = false;
        
        //<editor-fold defaultstate="collapsed" desc="Determine whether event raises should be cancelled">
        if(mainCancels)
        {
            shouldBeCancelledMainOnly = true;
            shouldBeCancelledAll = true;
        }
        
        if(!shouldBeCancelledAll)
            for(Pair<Event<EventArgs>, Boolean> i : mainEventDependants)
                if(i.getSecond())
                {
                    shouldBeCancelledMainOnly = true;
                    shouldBeCancelledAll = true;
                    break;
                }
        
        if(!shouldBeCancelledAll)
        {
            Outer:
            for(Triplet<Event<EventArgs>, Boolean, Pair<Event<EventArgs>, Boolean>[]> i : alongsideEvents)
            {
                if(i.getSecond())
                {
                    shouldBeCancelledAll = true;
                    break;
                }
                
                for(Pair<Event<EventArgs>, Boolean> j : i.getThird())
                    if(j.getSecond())
                    {
                        shouldBeCancelledAll = true;
                        break Outer;
                    }
            }
        }
        //</editor-fold>
        
        try
        {
            mainEvent.raise(this, mainArgs);
            assertTrue(msg + ".mainOnly", mainArgs.isCancelled() == shouldBeCancelledMainOnly);
        }
        finally
        { mainEvent.raisePostEvent(this, mainArgs); }
        
        mainArgs = getNewArgs();
        Pair<Event<EventArgs>, EventArgs>[] alongsidePairs = new Pair[alongsideEvents.length];
        
        try
        {
            for(int i = 0; i < alongsideEvents.length; i++)
                alongsidePairs[i] = new Pair<Event<EventArgs>, EventArgs>(alongsideEvents[i].getFirst(), getNewArgs());
            
            mainEvent.raiseAlongside(this, mainArgs, alongsidePairs);
            System.out.println("shouldBeCancelledAll = " + shouldBeCancelledAll);
            System.out.println("mainArgs.isCancelled() = " + mainArgs.isCancelled());
            assertTrue(msg + ".implicitsharing.main", mainArgs.isCancelled() == shouldBeCancelledAll);
            
            for(int i = 0; i < alongsidePairs.length; i++)
                assertTrue(msg + ".implicitsharing." + (i + 1), alongsidePairs[i].getSecond().isCancelled() == shouldBeCancelledAll);
        }
        finally
        { mainEvent.raisePostEventAlongside(this, mainArgs, alongsidePairs); }
        
        mainArgs = getNewArgs();
        
        try
        {
            for(int i = 0; i < alongsideEvents.length; i++)
                alongsidePairs[i] = new Pair<Event<EventArgs>, EventArgs>(alongsideEvents[i].getFirst(), getNewArgs());
            
            mainEvent.raiseAlongside(this, mainArgs, true, alongsidePairs);
            assertTrue(msg + ".explicitsharing.main", mainArgs.isCancelled() == shouldBeCancelledAll);
            
            for(int i = 0; i < alongsidePairs.length; i++)
                assertTrue(msg + ".explicitsharing." + (i + 1), alongsidePairs[i].getSecond().isCancelled() == shouldBeCancelledAll);
        }
        finally
        { mainEvent.raisePostEventAlongside(this, mainArgs, alongsidePairs); }
        
        mainArgs = getNewArgs();
        
        try
        {
            for(int i = 0; i < alongsideEvents.length; i++)
                alongsidePairs[i] = new Pair<Event<EventArgs>, EventArgs>(alongsideEvents[i].getFirst(), getNewArgs());
            
            mainEvent.raiseAlongside(this, mainArgs, false, alongsidePairs);
            assertTrue(msg + ".nosharing.main", mainArgs.isCancelled() == shouldBeCancelledMainOnly);
            
            for(int i = 0; i < alongsidePairs.length; i++)
            {
                boolean thisShouldBeCancelled = alongsideEvents[i].getSecond();
                
                if(!thisShouldBeCancelled)
                    for(Pair<Event<EventArgs>, Boolean> j : alongsideEvents[i].getThird())
                        if(j.getSecond())
                            thisShouldBeCancelled = true;
                
                assertTrue(msg + ".nosharing." + (i + 1), alongsidePairs[i].getSecond().isCancelled() == thisShouldBeCancelled);
            }
        }
        finally
        { mainEvent.raisePostEventAlongside(this, mainArgs, alongsidePairs); }
    }
    
    */
    
    // Oh my god, I just realised how much I've been raising little child events.
}