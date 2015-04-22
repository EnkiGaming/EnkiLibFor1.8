package com.enkigaming.lib.events;

/**
 * Listener that gets registered to events in order to run arbitrary code when an event is raised. Immutable.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @param <T> The type of the eventargs object to be passed into the onEvent method, the type of eventargs used by the
 * event this gets registered to.
 */
public interface EventListener<T extends EventArgs>
{
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Constructor. Defaults the priority to Normal.
     */
//    public EventListener()
//    { this(ListenerPriority.Normal.getNumericalValue()); }
    
    /**
     * Constructor. Uses the numerical value of the passed ListenerPriority.
     * @param priority The priority of the event listener, how early or late it should be called in the event raise.
     */
//    public EventListener(ListenerPriority priority)
//    { this(priority.getNumericalValue()); }
    
    /**
     * Constructor.
     * @param priority The priority of the event listener, how early or late it should be called in the event raise.
     */
//    public EventListener(double priority)
//    { this.priority = priority; }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * How early or late the event listener is called when the event it's registered to is raised.
     */
    //final private double priority;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods">
    /**
     * Gets the priority, how early late the listener is called on its event raise.
     * @return The priority, as a double value.
     */
//    public double getPriority()
//    { return priority; }
    
    /**
     * The method called when the event this listener is registered to is raised.
     * @param sender The object on which the event was raised.
     * @param args The eventargs object passed to all event listeners of the event this method is being called by.
     */
    public void onEvent(Object sender, T args);
    //</editor-fold>
}