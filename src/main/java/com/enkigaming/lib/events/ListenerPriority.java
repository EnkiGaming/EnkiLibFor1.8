package com.enkigaming.lib.events;

/**
 * Representation of priority used in events. Priorities are actually handled as doubles, and this enum only acts as a
 * guide.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public enum ListenerPriority
{
    /**
     * Immediately upon event raise.
     */
    VeryEarly (0),
    
    /**
     * Early upon event raise.
     */
    Early     (1),
    
    /**
     * Normal priority, default.
     */
    Normal    (2),
    
    /**
     * Late upon event raise.
     */
    Late      (3),
    
    /**
     * Last to get called before the event args are made immutable.
     */
    VeryLate  (4),
    
    /**
     * Last to get called before the thing the event represents happens. Listeners with this priority or above are
     * called after the event args being passed to them is made immutable.
     */
    Monitor   (5),
    
    /**
     * Called after the thing the event represents happens.
     */
    Post      (6);

    /**
     * Constructor
     * @param numericalValue The double value the enum member represents as a priority.
     */
    ListenerPriority(double numericalValue)
    { this.numericalValue = numericalValue; }

    /**
     * The double value the enum member represents as a priority.
     */
    private double numericalValue;

    /**
     * Gets the numerical value of the priority enum member.
     * @return The numerical value of the priority enum member.
     */
    public double getNumericalValue()
    { return numericalValue; }
}