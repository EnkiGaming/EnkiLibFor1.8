package com.enkigaming.lib.misc;

import com.enkigaming.lib.extensions.ComparableMethods;
import com.enkigaming.lib.tuples.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static com.enkigaming.lib.convenience.SanityChecks.*;
import java.util.Arrays;
import java.util.Collections;

/**
 * Holds a set of integers along with maximum values where values can be incremented, and incrementing it past its
 * maximum value causes it to roll over to its minimum value, then increment the next number up from it.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public class TieredIncrementor
{
    protected static class Tier
    {
        //<editor-fold defaultstate="collapsed" desc="Constructors">
        /**
         * Creates a Tier, with default min value and preset of 0.
         * @param max The highest value the tier can hold before overflowing. Must be above the default minimum value
         * of 0.
         */
        public Tier(int max)
        { this(0, max, 0); }
        
        /**
         * Creates a Tier, with the min value used as the preset value.
         * @param min The lowest value the tier can hold before underflowing. Must be below the maximum value.
         * @param max The highest value the tier can hold before overflowing. Must be above the minimum value.
         */
        public Tier(int min, int max)
        { this(min, max, min); }
        
        /**
         * Creates a tier, with the passed min, max, and preset values.
         * @param min The lowest value the tier can hold before underflowing. Must be below the maximum value.
         * @param max The highest value the tier can hold before overflowing. Must be above the minimum value.
         * @param currentValue The value the tier should start out with. Must be between the min and max values.
         */
        public Tier(int min, int max, int currentValue)
        {
            checkMinMaxIsValid(min, max);
            this.min = min;
            this.max = max;
            this.value = currentValue;
        }
        //</editor-fold>
        
        int min, max, value;
        
        final Object busy = new Object();
        
        //<editor-fold defaultstate="collapsed" desc="Setters">
        /**
         * Sets the tier's minimum possible value.
         * @param newMin The new minimum value.
         * @return The old minimum value.
         */
        public int setMin(int newMin)
        {
            int temp;
            
            synchronized(busy)
            {
                if(newMin > max)
                    throw new IllegalArgumentException("New min is greater than the current max.");
                
                temp = min;
                min = newMin;
            }
            
            return temp;
        }
        
        /**
         * Sets the tier's maximum possible value.
         * @param newMax The new maximum value.
         * @return The old maximum value.
         */
        public int setMax(int newMax)
        {
            int temp;
            
            synchronized(busy)
            {
                if(newMax < min)
                    throw new IllegalArgumentException("New max is less than the current min.");
                
                temp = max;
                max = newMax;
            }
            
            return temp;
        }
        
        // Replace return value with FlatRange<Integer> once ranges are added to this library
        /**
         * Sets the tier's minimum and maximum possible values.
         * @param newMin The new minimum value.
         * @param newMax The new maximum value.
         * @return A flat range of the previous minimum and maximum values.
         */
        public void setBounds(int newMin, int newMax)
        {
            int tempMin, tempMax;
            
            synchronized(busy)
            {
                if(newMin > newMax)
                    throw new IllegalArgumentException("New min is greater than new max.");
                
                tempMin = min;
                tempMax = max;
                
                min = newMin;
                max = newMax;
            }
            
            // return new ValueRange<Integer>(tempMin, tempMax);
        }
        
        /**
         * Sets the stored value of this tier.
         * @param newValue The new value of the tier.
         * @return The old value.
         */
        public int setValue(int newValue)
        {
            int temp;
            
            synchronized(busy)
            {
                checkIsBetweenBounds(newValue, "newValue", min, max);
                
                temp = value;
                value = newValue;
            }
            
            return temp;
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Relative Mutators">
        /**
         * Adds 1 to the currently stored value of the tier.
         * @return True if the increment pushed the value over the max value, and thus caused an overflow to the min
         * value. Otherwise, false.
         */
        public boolean increment()
        {
            synchronized(busy)
            {
                if(value >= max)
                {
                    value = min;
                    return true;
                }
                
                value++;
                return false;
            }
        }
        
        /**
         * Subtracts 1 from the currently stored value of the tier.
         * @return True if the decrement pulled the value under the min value, and thus caused an underflow to the max
         * value. Otherwise, false.
         */
        public boolean decrement()
        {
            synchronized(busy)
            {
                if(value <= min)
                {
                    value = max;
                    return true;
                }
                
                value--;
                return false;
            }
        }
        
        /**
         * Adds a number to the currently stored value of the tier.
         * @param amount The amount to add to the stored value.
         * @return The number of times an overflow occurred. That is, the number of times adding the number pushed the
         * held value over the maximum value and caused the value to overflow.
         */
        public int add(int amount)
        {
            if(amount < 0)
                return subtract(-amount);
            
            int overflowCount = 0;
            
            synchronized(busy)
            {
                for(;;)
                {
                    if(value + amount < max)
                    {
                        value += amount;
                        break;
                    }
                    
                    amount -= max - value;
                    value = min;
                    overflowCount++;
                }
            }
            
            return overflowCount;
        }
        
        /**
         * Subtracts a number from the currently stored value of the tier.
         * @param amount The amount to subtract from the stored value.
         * @return The number of time an underflow occurred. That is, the number of times subtracting the number pulled
         * the held value under the minimum value ad caused the value to underflow.
         */
        public int subtract(int amount)
        {
            if(amount > 0)
                return add(-amount);
            
            int underflowCount = 0;
            
            synchronized(busy)
            {
                for(;;)
                {
                    if(value - amount > min)
                    {
                        value -= amount;
                        break;
                    }
                    
                    amount -= value - min;
                    value = max;
                    underflowCount++;
                }
            }
            
            return underflowCount;
        }
        //</editor-fold>
        
        /**
         * Gets the minimum possible value. Decrementing below this causes the held value to underflow to the maximum
         * value.
         * @return The minimum possible value.
         */
        public int getMin()
        {
            synchronized(busy)
            { return min; }
        }
        
        /**
         * Gets the maximum possible value. Incrementing above this causes the held value to overflow to the minimum
         * value.
         * @return The maximum possible value.
         */
        public int getMax()
        {
            synchronized(busy)
            { return max; }
        }
        
        /**
         * Gets the held value.
         * @return The held value.
         */
        public int getValue()
        {
            synchronized(busy)
            { return value; }
        }
    }
    
    /**
     * Constructs a tiered incrementor with a single tier, preset to its minimum value, with a minimum value of 0 and a
     * maximum value of the maximum possible integer value.
     */
    public TieredIncrementor()
    { tiers.add(new Tier(defaultMin, defaultMax, defaultMin)); }
    
    /**
     * Constructs a tiered incrementor with the specified number of tiers, preset to their minimum values, with minimum
     * values of 0 and maximum values of the maximum possible integer values.
     * @param numberOfTiers The number of tiers the tiered incrementor should be constructed with.
     */
    public TieredIncrementor(int numberOfTiers)
    {
        if(numberOfTiers < 0)
            throw new IllegalArgumentException("You cannot have a negative number of tiers.");
        
        for(int i = 0; i < numberOfTiers; i++)
            tiers.add(new Tier(defaultMin, defaultMax, defaultMin));
    }
    
    /**
     * Constructs a tiered incrementor with the specified number of tiers, preset to their minimum values, with minimum
     * values of 0 and the specified maximum values.
     * @param numberOfTiers The number of tiers the tiered incrementor should be constructed with.
     * @param defaultMax The default maximum possible value to assign to tiers where one isn't specified, before a tier
     * overflows. Also, the value used as the maximum value for the tiers added at construction. Must be at or above the
     * default minimum possible value, of 0.
     */
    public TieredIncrementor(int numberOfTiers, int defaultMax)
    {
        if(numberOfTiers < 0)
            throw new IllegalArgumentException("You cannot have a negative number of tiers. (" + numberOfTiers + ")");
        
        checkMinMaxIsValid(defaultMin, defaultMax);
        
        this.defaultMax = defaultMax;
        
        for(int i = 0; i < numberOfTiers; i++)
            tiers.add(new Tier(defaultMin, defaultMax, defaultMin));
    }
    
    /**
     * Constructs a tiered incrementor with the specified number of tiers, preset to their minimum values, with the
     * specified minimum and maximum values.
     * @param numberOfTiers The number of tiers the tiered incrementor should be constructed with.
     * @param defaultMin The default maximum possible value to assign to tiers where one isn't specified, before a tier
     * overflows. Also, the value used as the maximum value for the tiers added at construction. Must be at or above the
     * provided minimum value.
     * @param defaultMax The default minimum possible value to assign to tiers where one isn't specified, before a tier
     * underflows. Also, the value used as the minimum value for the tiers added at construction. Must be to or below
     * the provided maximum value.
     */
    public TieredIncrementor(int numberOfTiers, int defaultMin, int defaultMax)
    {
        if(numberOfTiers < 0)
            throw new IllegalArgumentException("You cannot have a negative number of tiers. (" + numberOfTiers + ")");
        
        checkMinMaxIsValid(defaultMin, defaultMax);
        
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        
        for(int i = 0; i < numberOfTiers; i++)
            tiers.add(new Tier(this.defaultMin, this.defaultMax, this.defaultMin));
    }
    
    /**
     * Consructs a tiered incrementor with a number of tiers matching the number of maximum values provided, with each
     * preset to their default minimum values of 0 and each with the provided maximum value for their position.
     * @param maximumValues A list containing what should be the maximum values for each tier. Must be at or above the
     * default minimum value of 0.
     */
    public TieredIncrementor(List<Integer> maximumValues)
    {
        deepNullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    /**
     * Constructs a tiered incrementor with a number of tiers matching the number of maximum values provided, with
     * each preset to their default minimum values of 0 and each with the provided maximum value for their position,
     * with the default maximum value provided used as the default maximum value.
     * @param maximumValues A list containing what should be the maximum values for each tier. Must be at or above the
     * default minimum value of 0.
     * @param defaultMax The default value to use for new tiers where a maximum value isn't specified.
     */
    public TieredIncrementor(List<Integer> maximumValues, int defaultMax)
    {
        deepNullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        this.defaultMax = defaultMax;
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    /**
     * Constructs a tiered incrementor with a number of tiers matching the number of maximum values provided, with
     * each preset to their minimum values, which is set to the provided default min value, with the default maximum
     * value provided used as the default maximum value.
     * @param maximumValues 
     * @param defaultMin
     * @param defaultMax 
     */
    public TieredIncrementor(List<Integer> maximumValues, int defaultMin, int defaultMax)
    {
        deepNullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    /**
     * Consructs a tiered incrementor with a number of tiers matching the number of maximum values provided, with each
     * preset to their default minimum values of 0 and each with the provided maximum value for their position.
     * @param maximumValues An array containing what should be the maximum values for each tier. Must be at or above the
     * default minimum value of 0.
     */
    public TieredIncrementor(int[] maximumValues)
    {
        nullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    /**
     * Constructs a tiered incrementor with a number of tiers matching the number of maximum values provided, with
     * each preset to their default minimum values of 0 and each with the provided maximum value for their position,
     * with the default maximum value provided used as the default maximum value.
     * @param maximumValues An array containing what should be the maximum values for each tier. Must be at or above the
     * default minimum value of 0.
     * @param defaultMax The default value to use for new tiers where a maximum value isn't specified.
     */
    public TieredIncrementor(int[] maximumValues, int defaultMax)
    {
        nullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        this.defaultMax = defaultMax;
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    
    public TieredIncrementor(int[] maximumValues, int defaultMin, int defaultMax)
    {
        nullCheck(new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(null, maximumValues, defaultMin, defaultMax);
        
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        
        for(int i : maximumValues)
            tiers.add(new Tier(defaultMin, defaultMax, i));
    }
    
    public TieredIncrementor(List<Integer> minimumValues, List<Integer> maximumValues)
    {
        deepNullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                      new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.size(), maximumValues.size());
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.size() ? minimumValues.get(i) : defaultMin;
            int max = i < maximumValues.size() ? maximumValues.get(i) : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    public TieredIncrementor(List<Integer> minimumValues, List<Integer> maximumValues, int defaultMax)
    {
        deepNullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                      new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        this.defaultMax = defaultMax;
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.size(), maximumValues.size());
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.size() ? minimumValues.get(i) : defaultMin;
            int max = i < maximumValues.size() ? maximumValues.get(i) : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    public TieredIncrementor(List<Integer> minimumValues, List<Integer> maximumValues, int defaultMin, int defaultMax)
    {
        deepNullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                      new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.size(), maximumValues.size());
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.size() ? minimumValues.get(i) : defaultMin;
            int max = i < maximumValues.size() ? maximumValues.get(i) : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    public TieredIncrementor(int[] minimumValues, int[] maximumValues)
    {
        nullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                  new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.length, maximumValues.length);
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.length ? minimumValues[i] : defaultMin;
            int max = i < maximumValues.length ? maximumValues[i] : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    public TieredIncrementor(int[] minimumValues, int[] maximumValues, int defaultMax)
    {
        nullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                  new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        this.defaultMax = defaultMax;
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.length, maximumValues.length);
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.length ? minimumValues[i] : defaultMin;
            int max = i < maximumValues.length ? maximumValues[i] : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    public TieredIncrementor(int[] minimumValues, int[] maximumValues, int defaultMin, int defaultMax)
    {
        nullCheck(new Pair<Object, String>(minimumValues, "minimumValues"),
                  new Pair<Object, String>(maximumValues, "maximumValues"));
        checkMinMaxIsValid(minimumValues, maximumValues, defaultMin, defaultMax);
        
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        
        int tierCount = ComparableMethods.getLargestOfPrimitives(minimumValues.length, maximumValues.length);
        
        for(int i = 0; i < tierCount; i++)
        {
            int min = i < minimumValues.length ? minimumValues[i] : defaultMin;
            int max = i < maximumValues.length ? maximumValues[i] : defaultMax;
            
            tiers.add(new Tier(min, max, min));
        }
    }
    
    final List<Tier> tiers = new ArrayList<Tier>();
    // List<Value, Min, Max>
    
    int defaultMin = 0;
    int defaultMax = Integer.MAX_VALUE;
    
    final Object minmaxBusy = new Object();
    
    public List<Integer> getValues()
    {
        List<Integer> values = new ArrayList<Integer>();
        
        synchronized(tiers)
        {
            for(Tier i : tiers)
                values.add(i.getValue());
        }
        
        return values;
    }
    
    public int getValue(int tier)
    {
        synchronized(tiers)
        { return tiers.get(tier).getValue(); }
    }
    
    public int getMin(int tier)
    {
        synchronized(tiers)
        { return tiers.get(tier).getMin(); }
    }
    
    public int getMax(int tier)
    {
        synchronized(tiers)
        { return tiers.get(tier).getMax(); }
    }
    
    public int getDefaultMin()
    {
        synchronized(minmaxBusy)
        { return defaultMin; }
    }
    
    public int getDefaultMax()
    {
        synchronized(minmaxBusy)
        { return defaultMax; }
    }
    
    public int getTierCount()
    {
        synchronized(tiers)
        { return tiers.size(); }
    }
    
    @Override
    public String toString()
    { return toString(true, "."); }
    
    public String toString(String separator)
    { return toString(true, separator); }
    
    public String toString(boolean bigEndian, String separator)
    {
        String result = "";
        
        if(separator == null)
            separator = ".";
        
        synchronized(tiers)
        {
            if(bigEndian)
                for(int i = tiers.size(); i >= 0; i--)
                    result += tiers.get(i).getValue() + separator;
            else
                for(int i = 0; i < tiers.size(); i++)
                    result += tiers.get(i).getValue() + separator;
        }
        
        result = result.substring(0, result.length() - 1);
        
        return result;
    }
    
    public String toString(String separator, boolean bigEndian)
    { return toString(bigEndian, separator); }
    
    public void increment()
    {
        synchronized(tiers)
        {
            for(int i = 0; i < tiers.size(); i++)
                if(!tiers.get(i).increment()) // If the value didn't overflow
                    break;
        }
    }
    
    public void increment(int tier)
    {
        synchronized(tiers)
        {
            if(tier < 0 || tier >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + tier + ".");
            
            for(int i = tier; i < tiers.size(); i++)
                if(!tiers.get(i).increment()) // If the value didn't overflow
                    break;
        }
    }
    
    public void decrement()
    {
        synchronized(tiers)
        {
            for(int i = 0; i < tiers.size(); i++)
                if(!tiers.get(i).decrement()) // If the value didn't underflow
                    break;
        }
    }
    
    public void decrement(int tier)
    {
        synchronized(tiers)
        {
            if(tier < 0 || tier >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + tier + ".");
            
            for(int i = tier; i < tiers.size(); i++)
                if(!tiers.get(i).decrement()) // If the value didn't underflow
                    break;
        }
    }
    
    public void add(int amount)
    {
        synchronized(tiers)
        {
            for(int i = 0, toAdd = amount; i < tiers.size(); i++)
                if((toAdd = tiers.get(i).add(toAdd)) <= 0) // If there were no overflows as a result of Tier.add
                    break;
        }
    }
    
    public void add(int tier, int amount)
    {
        synchronized(tiers)
        {
            for(int i = tier, toAdd = amount; i < tiers.size(); i++)
                if((toAdd = tiers.get(i).add(toAdd)) <= 0) // If there were no overflows as a result of Tier.add
                    break;
        }
    }
    
    public void subtract(int amount)
    {
        synchronized(tiers)
        {
            for(int i = 0, toSubtract = amount; i < tiers.size(); i++)
                if((toSubtract = tiers.get(i).subtract(toSubtract)) <= 0) // If no underflows from Tier.subtract
                    break;
        }
    }
    
    public void subtract(int tier, int amount)
    {
        synchronized(tiers)
        {
            for(int i = 0, toSubtract = amount; i < tiers.size(); i++)
                if((toSubtract = tiers.get(i).subtract(toSubtract)) <= 0) // If no underflows from Tier.subtract
                    break;
        }
    }
    
    public int setValue(int tier, int value)
    {
        synchronized(tiers)
        { return tiers.get(tier).setValue(value); }
    }
    
    public int setMin(int tier, int min)
    {
        synchronized(tiers)
        { return tiers.get(tier).setMin(min); }
    }
    
    public int setMax(int tier, int max)
    {
        synchronized(tiers)
        { return tiers.get(tier).setMax(max); }
    }
    
    // TODO: Make this method return a FlatRange<Integer> when I add that to the lib.
    public void setBounds(int tier, int min, int max)
    {
        synchronized(tiers)
        { tiers.get(tier).setBounds(min, max); }
    }
    
    public int setDefaultMin(int min)
    {
        synchronized(minmaxBusy)
        {
            checkMinMaxIsValid(min, defaultMax);
            
            int temp = defaultMin;
            defaultMin = min;
            return temp;
        }
    }
    
    public int setDefaultMax(int max)
    {
        synchronized(minmaxBusy)
        {
            checkMinMaxIsValid(defaultMin, max);
            
            int temp = defaultMax;
            defaultMax = max;
            return temp;
        }
    }
    
    // TODO: Make this method return a FlatRange<Integer> when I add that to the lib.
    public void setDefaultBounds(int min, int max)
    {
        checkMinMaxIsValid(min, max);
        
        synchronized(minmaxBusy)
        {
            defaultMin = min;
            defaultMax = max;
        }
    }
    
    public void addTier()
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        synchronized(tiers)
        { tiers.add(new Tier(min, max)); }
    }
    
    public void addTier(int preset)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        { tiers.add(new Tier(min, max, preset)); }
    }
    
    public void addTier(int preset, int max)
    {
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        { tiers.add(new Tier(min, max, preset)); }
    }
    
    public void addTier(int preset, int min, int max)
    {
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        { tiers.add(new Tier(min, max, preset)); }
    }
    
    public void addTiers(int numberOfTiersToAdd)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        synchronized(tiers)
        {
            for(int i = 0; i < numberOfTiersToAdd; i++)
                tiers.add(new Tier(min, max));
        }
    }
    
    public void addTiers(int numberOfTiersToAdd, int max)
    {
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        
        synchronized(tiers)
        {
            for(int i = 0; i < numberOfTiersToAdd; i++)
                tiers.add(new Tier(min, max));
        }
    }
    
    public void addTiers(int numberOfTiersToAdd, int min, int max)
    {
        checkMinMaxIsValid(min, max);
        
        synchronized(tiers)
        {
            for(int i = 0; i < numberOfTiersToAdd; i++)
                tiers.add(new Tier(min, max));
        }
    }
    
    public void addTiers(int numberOfTiersToAdd, int preset, int min, int max)
    {
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            for(int i = 0; i < numberOfTiersToAdd; i++)
                tiers.add(new Tier(min, max, preset));
        }
    }
    
    public void addTiers(int[] presets)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            for(int i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(int[] presets, int max)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            for(int i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(int[] presets, int[] maxes)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"),
                  new Pair<Object, String>(maxes,   "maxes"));
         
        int min, max;
         
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(null, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", null, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(presets.length, maxes.length);
        
        synchronized(tiers)
        {
            for(int i = 0; i < count; i++)
            {
                int thisMax    = i < maxes  .length ? maxes  [i] : max;
                int thisPreset = i < presets.length ? presets[i] : min;
                
                tiers.add(new Tier(min, thisMax, thisPreset));
            }
        }
    }
    
    public void addTiers(int[] presets, int min, int max)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            for(int i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(int[] presets, int[] mins, int[] maxes)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"),
                  new Pair<Object, String>(mins,    "mins"),
                  new Pair<Object, String>(maxes,   "maxes"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(mins, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", mins, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(mins.length, maxes.length, presets.length);
        
        synchronized(tiers)
        {
            for(int i = 0; i < count; i++)
            {
                int thisMin    = i < mins   .length ? mins   [i] : min;
                int thisMax    = i < maxes  .length ? maxes  [i] : max;
                int thisPreset = i < presets.length ? presets[i] : thisMin;
                
                tiers.add(new Tier(thisMin, thisMax, thisPreset));
            }
        }
    }
    
    public void addTiers(Collection<Integer> presets)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(presets, "presets", min, max);
        
        synchronized(tiers)
        {
            for(int i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(Collection<Integer> presets, int max)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", min, max);
        
        synchronized(tiers)
        {
            for(int i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(List<Integer> presets, List<Integer> maxes)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"),
                      new Pair<Object, String>(maxes,   "maxes"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(null, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", null, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(presets.size(), maxes.size());
        
        synchronized(tiers)
        {
            for(int i = 0; i < count; i++)
            {
                int thisMax = i < maxes.size() ? maxes.get(i) : max;
                int thisPreset = i < presets.size() ? presets.get(i) : min;
                
                tiers.add(new Tier(min, thisMax, thisPreset));
            }
        }
    }
    
    public void addTiers(Collection<Integer> presets, int min, int max)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", min, max);

        synchronized(tiers)
        {
            for(Integer i : presets)
                tiers.add(new Tier(min, max, i));
        }
    }
    
    public void addTiers(List<Integer> presets, List<Integer> mins, List<Integer> maxes)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"),
                      new Pair<Object, String>(mins,    "mins"),
                      new Pair<Object, String>(maxes,   "maxes"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(mins, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", mins, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(mins.size(), maxes.size(), presets.size());
        
        synchronized(tiers)
        {
            for(int i = 0; i < count; i++)
            {
                int thisMin    = i < mins .size()   ? mins   .get(i) : min;
                int thisMax    = i < maxes.size()   ? maxes  .get(i) : max;
                int thisPreset = i < presets.size() ? presets.get(i) : thisMin;
                
                tiers.add(new Tier(thisMin, thisMax, thisPreset));
            }
        }
    }
    
    public void insertTier(int position)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");

            tiers.add(position, new Tier(min, max));
        }
    }
    
    public void insertTier(int position, int preset)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Position was out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");

            tiers.add(position, new Tier(min, max, preset));
        }
    }
    
    public void insertTier(int position, int preset, int max)
    {
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Position was out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            tiers.add(position, new Tier(min, max, preset));
        }
    }
    
    public void insertTier(int position, int preset, int min, int max)
    {
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Position was out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            tiers.add(position, new Tier(min, max, preset));
        }
    }
    
    public void insertTiers(int position, int amount)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Position was out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < amount; i++)
                tiers.add(position + i, new Tier(min, max));
        }
    }
    
    public void insertTiers(int position, int amount, int preset)
    {
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < amount; i++)
                tiers.add(position + i, new Tier(min, max, preset));
        }
    }
    
    public void insertTiers(int position, int amount, int preset, int max)
    {
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < amount; i++)
                tiers.add(position + i, new Tier(min, max, preset));
        }
    }
    
    public void insertTiers(int position, int amount, int preset, int min, int max)
    {
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(preset, "preset", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < amount; i++)
                tiers.add(position + i, new Tier(min, max, preset));
        }
    }
    
    public void insertTiers(int position, int[] presets)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < presets.length; i++)
                tiers.add(position + i, new Tier(min, max, presets[i]));
        }
    }
    
    public void insertTiers(int position, int[] presets, int max)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < presets.length; i++)
                tiers.add(position + i, new Tier(min, max, presets[i]));
        }
    }
    
    public void insertTiers(int position, int[] presets, int min, int max)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"));
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", null, null, min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < presets.length; i++)
                tiers.add(position + i, new Tier(min, max, presets[i]));
        }
    }
    
    public void insertTiers(int position, int[] presets, int[] maxes)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"),
                  new Pair<Object, String>(maxes,   "maxes"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(null, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", null, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(presets.length, maxes.length);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < count; i++)
            {
                int thisMax    = i < maxes  .length ? maxes  [i] : max;
                int thisPreset = i < presets.length ? presets[i] : min;
                
                tiers.add(position + i, new Tier(min, thisMax, thisPreset));
            }
        }
    }
    
    public void insertTiers(int position, int[] presets, int[] mins, int[] maxes)
    {
        nullCheck(new Pair<Object, String>(presets, "presets"),
                  new Pair<Object, String>(mins,    "mins"),
                  new Pair<Object, String>(maxes,   "maxes"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(mins, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", mins, maxes, min, max);
        
        int count = ComparableMethods.getLargestOfPrimitives(presets.length, mins.length, maxes.length);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < count; i++)
            {
                int thisMin    = i < mins   .length ? mins   [i] : min;
                int thisMax    = i < maxes  .length ? maxes  [i] : max;
                int thisPreset = i < presets.length ? presets[i] : thisMin;
                
                tiers.add(position + i, new Tier(thisMin, thisMax, thisPreset));
            }
        }
    }
    
    public void insertTiers(int position, Collection<Integer> presets)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min, max;
        List<Integer> asList = presets instanceof List ? (List<Integer>)presets : null;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkIsBetweenBounds(presets, "presets", min, max);
                    
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            if(asList != null)
                for(int i = 0; i < asList.size(); i++)
                    tiers.add(position + i, new Tier(min, max, asList.get(i)));
            else
                for(int i : presets)
                    tiers.add(position, new Tier(min, max, i));
        }
    }
    
    public void insertTiers(int position, Collection<Integer> presets, int max)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min;
        List<Integer> asList = presets instanceof List ? (List<Integer>)presets : null;
        
        synchronized(minmaxBusy)
        { min = defaultMin; }
        
        checkMinMaxIsValid(min, max);
        checkIsBetweenBounds(presets, "presets", min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            if(asList != null)
                for(int i = 0; i < asList.size(); i++)
                    tiers.add(position + i, new Tier(min, max, asList.get(i)));
            else
                for(int i : presets)
                    tiers.add(position, new Tier(min, max, i));
        }
    }
    
    public void insertTiers(int position, List<Integer> presets, List<Integer> maxes)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"),
                      new Pair<Object, String>(maxes  , "maxes"  ));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(null, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", null, maxes, min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < presets.size(); i++)
                tiers.add(position + i, new Tier(min, max, presets.get(i)));
        }
    }
    
    public void insertTiers(int position, Collection<Integer> presets, int min, int max)
    {}
    
    public void insertTiers(int position, List<Integer> presets, List<Integer> mins, List<Integer> maxes)
    {
        deepNullCheck(new Pair<Object, String>(presets, "presets"));
        
        int min, max;
        
        synchronized(minmaxBusy)
        {
            min = defaultMin;
            max = defaultMax;
        }
        
        checkMinMaxIsValid(null, maxes, min, max);
        checkIsBetweenBounds(presets, "presets", null, maxes, min, max);
        
        synchronized(tiers)
        {
            if(position < 0 || position >= tiers.size())
                throw new IllegalArgumentException("Tier out of range. Expected between (incl) 0 and "
                                                   + (tiers.size() - 1) + ", was " + position + ".");
            
            for(int i = 0; i < presets.size(); i++)
                tiers.add(position + i, new Tier(min, max, presets.get(i)));
        }
    }
    
    public int removeTier()
    {
        synchronized(tiers)
        { return tiers.remove(tiers.size() - 1).getValue(); }
    }
    
    public int removeTier(int position)
    {
        synchronized(tiers)
        { return tiers.remove(position).getValue(); }
    }
    
    public int[] removeTiers(int numberOfTiersToRemove)
    {
        int[] removedValues = new int[numberOfTiersToRemove];
        
        synchronized(tiers)
        {
            for(int i = 0; i < numberOfTiersToRemove; i++)
                removedValues[i] = tiers.remove(tiers.size() - 1).getValue();
        }
        
        return removedValues;
    }
    
    public int[] removeTiers(int[] tiersToRemove)
    {
        nullCheck(new Pair<Object, String>(tiersToRemove, "tiersToRemove"));
        
        int[] toRemove = Arrays.copyOf(tiersToRemove, tiersToRemove.length);
        Arrays.sort(tiersToRemove);
        int[] removedValues = new int[tiersToRemove.length];
        
        synchronized(tiers)
        {
            for(int i = toRemove.length - 1; i >= 0; i--)
                removedValues[i] = tiers.remove(toRemove[i]).getValue();
        }
        
        return removedValues;
    }
    
    public int[] removeTiers(Collection<Integer> tiersToRemove)
    {
        deepNullCheck(new Pair<Object, String>(tiersToRemove, "tiersToRemove"));
        
        int[] removedValues = new int[tiersToRemove.size()];
        List<Integer> toRemove = new ArrayList<Integer>(tiersToRemove);
        Collections.sort(toRemove);
        
        synchronized(tiers)
        {
            for(int i = toRemove.size() - 1; i >= 0; i--)
                removedValues[i] = tiers.remove((int)toRemove.get(i)).getValue();
        }
        
        return removedValues;
    }
    
    private static void checkIsBetweenBounds(int toCheck, String toCheckName, int min, int max)
    {
        if(toCheck < min)
            throw new IllegalArgumentException(toCheckName + " was less than the minimum bound. Was " + toCheck
                                               + ", expected at or above " + min + ".");
        
        if(toCheck > max)
            throw new IllegalArgumentException(toCheckName + " was greater than the maximum bound. Was " + toCheck
                                               + ", expected at or below " + max + ".");
    }
    
    private static void checkIsBetweenBounds(Collection<Integer> toCheck, String toCheckName, int min, int max)
    {
        for(Integer i : toCheck)
        {
            if(i < min)
                throw new IllegalArgumentException("Member of " + toCheckName + " was less than the minimum bound. "
                                                   + "Was " + i + ", expected at or above " + min + ".");
            
            if(i > max)
                throw new IllegalArgumentException("Member of " + toCheckName + " was greater than the maximum bound. "
                                                   + "Was " + i + ", expected at or below " + max + ".");
        }
    }
    
    private static void checkIsBetweenBounds(List<Integer> toCheck, String toCheckName, List<Integer> mins, List<Integer> maxes, int min, int max)
    {
        for(int i = 0; i < toCheck.size(); i++)
        {
            int thisMin = i < mins .size() ? mins .get(i) : min;
            int thisMax = i < maxes.size() ? maxes.get(i) : max;
            
            if(toCheck.get(i) < thisMin)
                throw new IllegalArgumentException("Member of " + toCheckName + " [" + i + "] was less than its "
                                                   + "minimum bound. Expected at or above " + thisMin + ", was "
                                                   + toCheck.get(i) + ".");
            
            if(toCheck.get(i) > thisMax)
                throw new IllegalArgumentException("Member of " + toCheckName + " [" + i + "] was greater than its"
                                                   + "maximum bound. Expected at or below " + thisMax + ", was "
                                                   + toCheck.get(i) + ".");
        }
    }
    
    private static void checkIsBetweenBounds(int[] toCheck, String toCheckName, int[] mins, int[] maxes, int min, int max)
    {
        for(int i = 0; i < toCheck.length; i++)
        {
            int thisMin = i < mins .length ? mins [i] : min;
            int thisMax = i < maxes.length ? maxes[i] : max;
            
            if(toCheck[i] < thisMin)
                throw new IllegalArgumentException("Member of " + toCheckName + " [" + i + "] was less than its "
                                                   + "minimum bound. Expected at or above " + thisMin + ", was "
                                                   + toCheck[i] + ".");
            
            if(toCheck[i] < thisMax)
                throw new IllegalArgumentException("Member of " + toCheckName + " [" + i + "] was greater than its"
                                                   + "maximum bound. Expected at or below " + thisMax + ", was "
                                                   + toCheck[i] + ".");
        }
    }
    
    private static void checkMinMaxIsValid(int min, int max)
    {
        if(min < max)
            throw new IllegalArgumentException("The min value was less than the max value.");
    }
    
    private static void checkMinMaxIsValid(List<Integer> mins, List<Integer> maxes, int min, int max)
    {
        //int count = ComparableMethods.getLargestOfPrimitives(mins.size(), maxes.size());
        
        int c = mins  != null && maxes != null ? ComparableMethods.getLargestOfPrimitives(mins.size(), maxes.size()) :
                mins  != null                  ? mins .size() :
                maxes != null                  ? maxes.size() :
                0;
        
        if(c == 0)
            checkMinMaxIsValid(min, max);
        
        for(int i = 0; i < c; i++)
        {
            int thisMin = mins  != null && i < mins .size() ? mins .get(i) : min;
            int thisMax = maxes != null && i < maxes.size() ? maxes.get(i) : max;
            
            if(min < max)
                throw new IllegalArgumentException("One of the min values [" + i + "] (" + thisMin +") was greater "
                                                   + "than its corresponding max value. (" + thisMax + ")");
        }
    }
    
    private static void checkMinMaxIsValid(int[] mins, int[] maxes, int min, int max)
    {
        int c = mins  != null && maxes != null ? ComparableMethods.getLargestOfPrimitives(mins.length, maxes.length) :
                mins  != null                  ? mins .length :
                maxes != null                  ? maxes.length :
                0;
        
        if(c == 0)
            checkMinMaxIsValid(min, max);
        
        for(int i = 0; i < c; i++)
        {
            int thisMin = mins  != null && i < mins .length ? mins [i] : min;
            int thisMax = maxes != null && i < maxes.length ? maxes[i] : max;
            
            if(min < max)
                throw new IllegalArgumentException("One of the min values [" + i + "] (" + thisMin +") was greater "
                                                   + "than its corresponding max value. (" + thisMax + ")");
        }
    }
}