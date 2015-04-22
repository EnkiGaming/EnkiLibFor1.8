package com.enkigaming.lib.events;

import com.enkigaming.lib.collections.CollectionMethods;
import com.enkigaming.lib.events.exceptions.EventArgsFinishedBeforeStartedException;
import com.enkigaming.lib.events.exceptions.EventArgsModifiedWhenImmutableException;
import com.enkigaming.lib.events.exceptions.EventArgsMultipleUseException;
import com.enkigaming.lib.events.exceptions.EventArgsStateException;
import com.enkigaming.lib.events.exceptions.EventArgsUsedPostBeforePreException;
import org.junit.Test;
import com.enkigaming.lib.testing.ThrowableAssertion;
import static org.junit.Assert.*;
import static com.enkigaming.lib.testing.Assert.*;
import com.enkigaming.lib.testing.NoThrowableAssertion;
import com.enkigaming.lib.tuples.Triplet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public abstract class EventArgsTest
{
    public abstract EventArgs getNewArgs();
    
    public abstract Event<EventArgs> getNewEvent();
    
    private void makeRelationship(EventArgs parent, EventArgs dependant)
    {
        parent.getTechnicalAccessor().addDependentArgs(dependant);
        dependant.getTechnicalAccessor().setParentArgs(parent);
    }
    
    @Test
    public void testCancellation()
    {
        final EventArgs args = getNewArgs();
        
        assertFalse("1", args.isCancelled());
        assertFalse("2", args.setCancelled(false));
        assertFalse("3", args.isCancelled());
        assertFalse("4", args.setCancelled(true));
        assertTrue ("5", args.isCancelled());
        assertTrue ("6", args.setCancelled(true));
        assertTrue ("7", args.isCancelled());
        assertTrue ("8", args.setCancelled(false));
        assertFalse("9", args.isCancelled());
        
        args.getTechnicalAccessor().makeImmutable();
        
        assertFalse("10", args.isCancelled());
        
        new ThrowableAssertion("11", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args.setCancelled(true); }
        };
        
        new ThrowableAssertion("12", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args.setCancelled(false); }
        };
        
        assertFalse("13", args.isCancelled());
        
        final EventArgs args2 = getNewArgs();
        
        assertFalse("14", args2.setCancelled(true));
        
        args2.getTechnicalAccessor().makeImmutable();
        
        assertTrue("15", args2.isCancelled());
        
        new ThrowableAssertion("16", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args2.setCancelled(true); }
        };
        
        new ThrowableAssertion("17", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args2.setCancelled(false); }
        };
        
        assertTrue("18", args2.isCancelled());
    }
    
    @Test
    public void testMutability()
    {
        final EventArgs args = getNewArgs();
        
        assertTrue("1", args.shouldBeMutable());
        
        assertFalse("2", args.isCancelled());
        args.setCancelled(true);
        assertTrue("3", args.isCancelled());
        args.setCancelled(false);
        assertFalse("4", args.isCancelled());
        
        assertTrue("5", args.shouldBeMutable());
        
        args.getTechnicalAccessor().makeImmutable();
        
        assertFalse("6", args.shouldBeMutable());
        
        assertFalse("7", args.isCancelled());
        
        new ThrowableAssertion("8", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args.setCancelled(true); }
        };
        
        assertFalse("9", args.isCancelled());
        
        new ThrowableAssertion("10", EventArgsModifiedWhenImmutableException.class)
        {
            @Override
            public void code() throws Exception
            { args.setCancelled(false); }
        };
        
        assertFalse("11", args.isCancelled());
    }
    
    @Test
    public void testMasterArgs()
    {
        EventArgs args = getNewArgs();
        EventArgs master1 = getNewArgs();
        EventArgs master2 = getNewArgs();
        
        assertSame("1", args, args.getMasterArgs());
        
        args.getTechnicalAccessor().setParentArgs(master1);
        
        assertSame("2", master1, args.getMasterArgs());
        assertSame("3", master1, master1.getMasterArgs());
        
        args.getTechnicalAccessor().setParentArgs(master2);
        
        assertSame("4", master2, args.getMasterArgs());
        assertSame("5", master2, master2.getMasterArgs());
        
        master2.getTechnicalAccessor().setParentArgs(master1);
        
        assertSame("6", master1, args.getMasterArgs());
        assertSame("7", master1, master2.getMasterArgs());
        assertSame("8", master1, master1.getMasterArgs());
    }
    
    @Test
    public void testParentArgsAndDependentArgs()
    {
        EventArgs args            = getNewArgs();
        EventArgs parentArgs      = getNewArgs();
        EventArgs grandparentArgs = getNewArgs();
        EventArgs uncleArgs       = getNewArgs();
        EventArgs cousinArgs      = getNewArgs();
        
        
        assertNull("1.1", args.getParentArgs());
        assertNull("1.2", parentArgs.getParentArgs());
        assertNull("1.3", grandparentArgs.getParentArgs());
        assertNull("1.4", uncleArgs.getParentArgs());
        assertNull("1.5", cousinArgs.getParentArgs());
        
        ensureEventArgsDoesntReturnDependants("2.1", args);
        ensureEventArgsDoesntReturnDependants("2.2", parentArgs);
        ensureEventArgsDoesntReturnDependants("2.3", grandparentArgs);
        ensureEventArgsDoesntReturnDependants("2.4", uncleArgs);
        ensureEventArgsDoesntReturnDependants("2.5", cousinArgs);
        
        
        makeRelationship(grandparentArgs, parentArgs);
        makeRelationship(parentArgs, args);
        makeRelationship(grandparentArgs, uncleArgs);
        makeRelationship(uncleArgs, cousinArgs);
        
        
        assertSame("3.1.1", args.getParentArgs(), parentArgs);
        assertSame("3.1.2", args.getMasterArgs(), grandparentArgs);
        ensureEventArgsDoesntReturnDependants("3.1.3", args);
        
        assertSame("3.2.1", parentArgs.getParentArgs(), grandparentArgs);
        assertSame("3.2.2", parentArgs.getMasterArgs(), grandparentArgs);
        ensureArgsHasDependencies("3.2.3", parentArgs, args);
        
        assertNull("3.3.1", grandparentArgs.getParentArgs());
        assertSame("3.3.2", grandparentArgs.getMasterArgs(), grandparentArgs);
        ensureArgsHasDependencies("3.3.3", grandparentArgs, parentArgs, uncleArgs);
        
        assertSame("3.4.1", uncleArgs.getParentArgs(), grandparentArgs);
        assertSame("3.4.2", uncleArgs.getMasterArgs(), grandparentArgs);
        ensureArgsHasDependencies("3.4.3", uncleArgs, cousinArgs);
        
        assertSame("3.5.1", cousinArgs.getParentArgs(), uncleArgs);
        assertSame("3.5.2", cousinArgs.getMasterArgs(), grandparentArgs);
        ensureEventArgsDoesntReturnDependants("3.5.3", cousinArgs);
    }
    
    private void ensureEventArgsDoesntReturnDependants(String message, EventArgs argsToCheck)
    {
        assertCollectionEquals(message + ".1",
                               argsToCheck.getDependentArgs(true, true),
                               Arrays.asList(argsToCheck));
        assertCollectionEquals(message + ".2",
                               argsToCheck.getDependentArgs(true, false),
                               Arrays.asList(argsToCheck));
        assertCollectionEmpty(message + ".3", argsToCheck.getDependentArgs(false, true));
        assertCollectionEmpty(message + ".4", argsToCheck.getDependentArgs(false, false));
        assertCollectionEmpty(message + ".5", argsToCheck.getDependentArgs(true));
        assertCollectionEmpty(message + ".6", argsToCheck.getDependentArgs(false));
        assertCollectionEmpty(message + ".7", argsToCheck.getDependentArgs());
        assertCollectionEmpty(message + ".8", argsToCheck.getDirectlyDependentArgs());
    }
    
    private void ensureArgsHasDependencies(String message, EventArgs argsToCheck, EventArgs... dependencies)
    {
        EventArgs itself = argsToCheck;
        Collection<EventArgs> immediateDependants = Arrays.asList(dependencies);
        Collection<EventArgs> cascadingDependants = new ArrayList<EventArgs>();
        
        for(EventArgs i : dependencies)
            cascadingDependants.addAll(i.getDependentArgs(true, true));
        
        assertCollectionEquals(message + ".1",
                               argsToCheck.getDependentArgs(true, true),
                               CollectionMethods.combineCollection(cascadingDependants, Arrays.asList(argsToCheck)));
        
        assertCollectionEquals(message + ".2",
                               argsToCheck.getDependentArgs(true, false),
                               CollectionMethods.combineCollection(immediateDependants, Arrays.asList(argsToCheck)));
        
        assertCollectionEquals(message + ".3",
                               argsToCheck.getDependentArgs(false, true),
                               cascadingDependants);
        
        assertCollectionEquals(message + ".4",
                               argsToCheck.getDependentArgs(false, false),
                               immediateDependants);
        
        assertCollectionEquals(message + ".5",
                               argsToCheck.getDependentArgs(true), 
                               cascadingDependants);
        
        assertCollectionEquals(message + ".6",
                               argsToCheck.getDependentArgs(false), 
                               immediateDependants);
        
        assertCollectionEquals(message + ".7",
                               argsToCheck.getDependentArgs(), 
                               cascadingDependants);
        
        assertCollectionEquals(message + ".8",
                               argsToCheck.getDirectlyDependentArgs(), 
                               immediateDependants);
    }
    
    @Test
    public void testRelatedArgs()
    {
        EventArgs args = getNewArgs();
        EventArgs parentArgs = getNewArgs();
        EventArgs otherArgs = getNewArgs();
        EventArgs otherParentArgs = getNewArgs();
        
        
        assertNull("1.1", args.getParentArgs());
        assertNull("1.2", parentArgs.getParentArgs());
        assertNull("1.3", otherArgs.getParentArgs());
        assertNull("1.4", otherParentArgs.getParentArgs());
        
        
        assertSame("2.1", args, args.getMasterArgs());
        assertSame("2.2", parentArgs, parentArgs.getMasterArgs());
        assertSame("2.3", otherArgs, otherArgs.getMasterArgs());
        assertSame("2.4", otherParentArgs, otherParentArgs.getMasterArgs());
        
        
        ensureEventArgsDoesntReturnDependants("3.1", args);
        ensureEventArgsDoesntReturnDependants("3.2", parentArgs);
        ensureEventArgsDoesntReturnDependants("3.3", otherArgs);
        ensureEventArgsDoesntReturnDependants("3.4", otherParentArgs);
        
        
        makeRelationship(parentArgs, args);
        makeRelationship(otherParentArgs, otherArgs);
        
        
        assertSame("4.1", args.getParentArgs(), parentArgs);
        assertNull("4.2", parentArgs.getParentArgs());
        assertSame("4.3", otherArgs.getParentArgs(), otherParentArgs);
        assertNull("4.4", otherParentArgs.getParentArgs());
        
        
        assertSame("5.1", parentArgs, args.getMasterArgs());
        assertSame("5.2", parentArgs, parentArgs.getMasterArgs());
        assertSame("5.3", otherParentArgs, otherArgs.getMasterArgs());
        assertSame("5.4", otherParentArgs, otherParentArgs.getMasterArgs());
        
        
        ensureEventArgsDoesntReturnDependants("6.1", args);
        ensureArgsHasDependencies("6.2", parentArgs, args);
        ensureEventArgsDoesntReturnDependants("6.3", otherArgs);
        ensureArgsHasDependencies("6.4", otherParentArgs, otherArgs);
    }
    
    @Test
    public void testUsageState()
    {
        final EventArgs argsNormal = getNewArgs();
        final EventArgs argsUsePostBeforePre = getNewArgs();
        final EventArgs argsFinishedBeforeStarted = getNewArgs();
        final EventArgs argsMultipleUse = getNewArgs();
        
        new NoThrowableAssertion("1.1", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsNormal.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new NoThrowableAssertion("1.2", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsNormal.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new NoThrowableAssertion("1.3", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsNormal.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new NoThrowableAssertion("1.4", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsNormal.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new ThrowableAssertion("2.1", EventArgsUsedPostBeforePreException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new ThrowableAssertion("2.2", EventArgsUsedPostBeforePreException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new NoThrowableAssertion("2.3", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new NoThrowableAssertion("2.4", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new ThrowableAssertion("2.5", EventArgsFinishedBeforeStartedException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new NoThrowableAssertion("2.6", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new NoThrowableAssertion("2.7", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsUsePostBeforePre.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new ThrowableAssertion("3.1", EventArgsFinishedBeforeStartedException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new NoThrowableAssertion("3.2", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new NoThrowableAssertion("3.3", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new ThrowableAssertion("3.4", EventArgsFinishedBeforeStartedException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new NoThrowableAssertion("3.5", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new NoThrowableAssertion("3.6", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsFinishedBeforeStarted.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
        
        new NoThrowableAssertion("4.1", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new ThrowableAssertion("4.1.1", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new NoThrowableAssertion("4.2", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new ThrowableAssertion("4.2.1", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new ThrowableAssertion("4.2.2", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new NoThrowableAssertion("4.3", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPostEvent();}
        };
        
        new ThrowableAssertion("4.3.1", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new ThrowableAssertion("4.3.2", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new ThrowableAssertion("4.3.3", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new NoThrowableAssertion("4.4", EventArgsStateException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPostEvent();}
        };
        
        new ThrowableAssertion("4.4.1", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPreEvent(); }
        };
        
        new ThrowableAssertion("4.4.2", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPreEvent(); }
        };
        
        new ThrowableAssertion("4.4.3", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsingPostEvent(); }
        };
        
        new ThrowableAssertion("4.4.4", EventArgsMultipleUseException.class)
        {
            @Override
            public void code()
            { argsMultipleUse.getTechnicalAccessor().markAsUsedPostEvent(); }
        };
    }
    
    @Test
    public void testEvent()
    {
        EventArgs args = getNewArgs();
        Event<EventArgs> event1 = getNewEvent();
        Event<EventArgs> event2 = getNewEvent();
        
        assertNull("1", args.getEvent());
        
        args.getTechnicalAccessor().setEvent(event1);
//        
//        assertSame("2", event1, args.getEvent());
//        
//        args.getTechnicalAccessor().setEvent(event2);
//        
//        assertSame("3", event2, args.getEvent());
//        
//        args.getTechnicalAccessor().setEvent(null);
//        
//        assertSame("4", null, args.getEvent());
    }
    
    @Test
    public void testListenerQueue()
    {
        EventArgs args = getNewArgs();
        Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> queue1 = new LinkedList<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>();
        Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> queue2 = new LinkedList<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>>();
        
        assertNull("1", args.getTechnicalAccessor().getListenerQueue());
        
        args.getTechnicalAccessor().setListenerQueue(queue1);
        
        assertSame("2", queue1, args.getTechnicalAccessor().getListenerQueue());
        
        args.getTechnicalAccessor().setListenerQueue(queue2);
        
        assertSame("3", queue2, args.getTechnicalAccessor().getListenerQueue());
        
        args.getTechnicalAccessor().setListenerQueue(null);
        
        assertNull("1", args.getTechnicalAccessor().getListenerQueue());
    }
}