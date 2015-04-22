package com.enkigaming.lib.events;

import com.enkigaming.lib.tuples.Triplet;
import java.util.Collection;
import java.util.Queue;

/**
 * The base interface defining the standard methods of EventArgs objects that should be used in conjunction with events,
 * being passed in via a raise and passed out to event listeners.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public interface EventArgs
{
    /**
     * Work-around for standard methods that should only really be available to implementations of the Event interface.
     * 
     * Contains methods required for Event implementations to interact with Event Args objects at a technical level.
     * 
     * ... This doesn't make me feel comfy. If I weren't using interfaces, these methods would just have a package
     * privacy level.
     */
    public static interface TechnicalAccessor // I know static isn't needed here, but unambiguity never hurt anyone.
    {
        /**
         * Marks the event args as being used in an in-progress pre-event raise, and ensures it's in the correct state to be
         * used as such. If this event args has a parent args, defers to the same method on the parent args.
         */
        void markAsUsingPreEvent();
    
        /**
         * Marks the event args as having been using in a pre-event raise, and ensure it's in the correct state to be
         * marked as such. If this event args has a parent args, defers to the same method on the parent args.
         */
        void markAsUsedPreEvent();

        /**
         * Marks the event args as being used in an in-progress post-event raise, and ensures it's in the correct state to be
         * used as such. If this event args has a parent args, defers to the same method on the parent args.
         */
        void markAsUsingPostEvent();

        /**
         * Marks the event args as having been using in a post-event raise, and ensure it's in the correct state to be
         * marked as such. If this event args has a parent args, defers to the same method on the parent args.
         */
        void markAsUsedPostEvent();

        /**
         * Specifies the Event object that this EventArgs object is associated with. That is, the Event object that this
         * EventArgs object was passed to in an event raise.
         * @param event The event to associate with this EventArgs object.
         */
        void setEvent(Event<? extends EventArgs> event);
        
        /**
         * Specifies the EventArgs object that was used to generate this for a dependent event raise.
         * @param args The args this object was generated from.
         */
        void setParentArgs(EventArgs args);
        
        /**
         * Marks the EventArgs object as immutable, for the monitor and later priorities.
         */
        void makeImmutable();
        
        /**
         * Specifies an EventArgs object that was generated using this object for a dependent event raise.
         * @param args The args that was generated from this.
         */
        void addDependentArgs(EventArgs args);
        
        /**
         * Specifies other args (that haven't been generated from another arg) that were raised in the same multi raise.
         * @param args The EventArgs object that was used as an EventArgs in the same raise as this.
         */
        void addRelatedMasterArgs(EventArgs args);
        
        /**
         * Specifies other args (that haven't been generated from another arg) that were raised in the same multi raise.
         * @param args The EventArgs object that was used as an EventArgs in the same raise as this.
         */
        void addRelatedMasterArgs(EventArgs... args);
        
        /**
         * Specifies other args (that haven't been generated from another arg) that were raised in the same multi raise.
         * @param args The EventArgs object that was used as an EventArgs in the same raise as this.
         */
        void addRelatedMasterArgs(Collection<? extends EventArgs> args);
        
        /**
         * Stores a Queue of EventListeners. Intended for storing in a pre-event raise, all of the listeners that were
         * called using this EventArgs object, so they can be referenced in the post-event raise rather than generating
         * a fresh queue of listeners that may result in a different set of listeners being called should listeners be
         * registered or deregistered from the event
         * @param listenerQueue 
         */
        void setListenerQueue(Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> listenerQueue);
        
        /**
         * Gets the listener queue stored on the event args object. Intended for returning the queue of listeners that
         * was used in the pre-event raise, for the post-event raise. Should contain listeners that were present in the
         * pre-event raise with a post-event priority.
         * @return The queue of listeners to be raised.
         */
        Queue<Triplet<EventListener<? extends EventArgs>, Double, EventArgs>> getListenerQueue();
    }
    
    /**
     * Whether or not the event raise this event args object is passed from to the listener is cancelled by event args.
     * 
     * Returns isCancelled() called on getMasterArgs() if it returns something, if this isn't a master args.
     * @return True if the event is cancelled, false if it isn't.
     */
    boolean isCancelled(); // If this class has master args, use master args cancellation state.
    
    /**
     * Sets whether or not the event raise will be cancelled.
     * @param cancellation True if the event should be cancelled, false if it shouldn't be.
     * @return True if the event was cancelled before calling this method, false if it wasn't.
     */
    boolean setCancelled(boolean cancellation);
    
    /**
     * Whether or not the event args object should be able to be modified in any way.
     * @return True if it should be able to be modified, false if it shouldn't.
     */
    boolean shouldBeMutable();
    
    /**
     * Gets the event args objects that were used in the same event raise, including this.
     * @return A collection containing the aforementioned eventargs.
     */
    Collection<EventArgs> getRelatedArgs();
    
    /**
     * Gets the event args objects that haven't been generated from another one (aka the master ones) that were used
     * in the same event raise, include this or this one's.
     * @return A collection containing the aforementioned eventargs.
     */
    Collection<EventArgs> getRelatedMasterArgs();
    
    /**
     * Gets the eventargs objects that were generated using this eventargs object.
     * @param getDependantsCascadingly Whether or not to include objects that were generated from args that were
     * generated from this.
     * @return A collection containing eventargs objects that were generated using this eventargs object.
     */
    Collection<EventArgs> getDependentArgs(boolean getDependantsCascadingly);
    
    /**
     * Gets the eventargs objects that were generated using this eventargs object. 
     * @param includeThis Whether or not to include this object in the returned collection.
     * @param getDependantsCascadingly Whether or not to include objects that were generated from args that were
     * generated from this.
     * @return A collection containing eventargs objects that were generated using this eventargs object.
     */
    Collection<EventArgs> getDependentArgs(boolean includeThis, boolean getDependantsCascadingly);
    
    /**
     * Gets the eventargs objects that were generated ultimately using this eventargs object, whether directly, or by
     * being generated from an eventargs object that was generated from this, and so on.
     * @return A collection containing eventargs objects that were generated using this eventargs object.
     */
    Collection<EventArgs> getDependentArgs();
    
    /**
     * Gets the eventargs objects that were generated specifically using this eventargs object. Does not include
     * eventargs objects that were generated using other ones that were, in turn, generated from this.
     * @return A collection containing eventargs objects that were generated using this eventargs object.
     */
    Collection<EventArgs> getDirectlyDependentArgs();
    
    /**
     * Gets the eventargs object that this was generated from.
     * @return The eventargs object this was generated from, or null if this wasn't automatically generated from another
     * eventargs object.
     */
    EventArgs getParentArgs();
    
    /**
     * Gets the eventargs object that this was ultimately generated from. That is, the parent args if they weren't
     * generated from another, or their parent args if they were, and so on.
     * @return The master eventargs object for this hierarchy, or itself if this wasn't generated from another eventargs
     * object.
     */
    EventArgs getMasterArgs();
    
    /**
     * Gets the event that was raised in order for this eventargs object to be passed to listeners.
     * @return The responsible event.
     */
    Event<? extends EventArgs> getEvent();
    
    /**
     * Gets an object containing methods that should only be accessed/used by implementations of Event.
     * @return The technical accessor object.
     */
    TechnicalAccessor getTechnicalAccessor();
}