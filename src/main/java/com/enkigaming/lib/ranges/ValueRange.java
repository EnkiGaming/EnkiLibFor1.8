package com.enkigaming.lib.ranges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ValueRange<T extends Comparable<T>> implements FlatRange<T>
{
    public ValueRange(T min, T max)
    {
        this.min = min;
        this.max = max;
    }
    
    public ValueRange(Range<? extends T> source)
    {
        min = source.getMin();
        max = source.getMax();
        includesMin = source.includesMin();
        includesMax = source.includesMax();
    }
    
    public ValueRange(T min, boolean inclusiveOfMin, T max, boolean inclusiveOfMax)
    {
        if(min.compareTo(max) == 0)
        {
            if(!(inclusiveOfMin == inclusiveOfMax))
                throw new IllegalArgumentException("Where a range represents a single point, it cannot both include "
                                                   + "and exclude that value.");
            
            if(!inclusiveOfMin)
                throw new IllegalArgumentException("Range doesn't represent any values. A null reference should "
                                                   + "represent the lack of a value/range of values where a range is "
                                                   + "returned.");
        }
        
        this.min = min;
        this.max = max;
        includesMin = inclusiveOfMin;
        includesMax = inclusiveOfMax;
    }
    
    /**
     * Creates a single-value flat range.
     * @param value The value this range should cover.
     */
    public ValueRange(T value)
    { this(value, value); }
    
    protected final T min, max;
    
    // Whether or not this flat range is inclusive of its borders; if it includes the values that define its minimum
    // and maximum values.
    boolean includesMin = true, includesMax = true;

    @Override
    public List<Range<T>> splitBy(final T splitPoint)
    {
        if(splitPoint.compareTo(min) < 0 || splitPoint.compareTo(max) > 0)
            return Arrays.<Range<T>>asList(this);
        
        return Arrays.<Range<T>>asList(new ValueRange<T>(min, includesMin, splitPoint, true),
                                       new ValueRange<T>(splitPoint, true, max, includesMax));
    }

    @Override
    public List<Range<T>> splitBy(T... splitPoints)
    {
        List<Range<T>> results = new ArrayList<Range<T>>();
        T[] splits = Arrays.copyOf(splitPoints, splitPoints.length);
        Arrays.sort(splits, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            { return o1.compareTo(o2); }
        });
        
        T previousSplit = min;
        
        for(int i = 0; i < splits.length; i++)
            if(splits[i].compareTo(min) > 0 && splits[i].compareTo(max) < 0)
                results.add(previousSplit == min ? new ValueRange<T>(min, includesMin, previousSplit = splits[i], true)
                                                 : new ValueRange<T>(previousSplit, previousSplit = splits[i]));
        
        if(previousSplit.compareTo(max) <= 0)
            results.add(new ValueRange<T>(previousSplit, true, max, includesMax));
        
        return results;
    }

    @Override
    public List<Range<T>> splitBy(Collection<? extends T> splitPoints)
    {
        List<Range<T>> results = new ArrayList<Range<T>>();
        List<T> splits = new ArrayList<T>(splitPoints);
        Collections.sort(splits);
        
        T previousSplit = min;
        
        for(int i = 0; i < splits.size(); i++)
            if(splits.get(i).compareTo(min) > 0 && splits.get(i).compareTo(max) < 0)
                results.add(previousSplit == min ? new ValueRange<T>(min, includesMin, previousSplit = splits.get(i), true)
                                                 : new ValueRange<T>(previousSplit, previousSplit = splits.get(i)));
        
        if(previousSplit.compareTo(max) <= 0)
            results.add(new ValueRange<T>(previousSplit, true, max, includesMax));
        
        return results;
    }

    @Override
    public T getMin()
    { return min; }

    @Override
    public T getMax()
    { return max; }

    @Override
    public boolean contains(T value)
    {
        return includesMin ? min.compareTo(value) <= 0 : min.compareTo(value) < 0  // Check lower-bound
            && includesMax ? max.compareTo(value) >= 0 : max.compareTo(value) > 0; // Check upper-bound
    }

    @Override
    public boolean containsAll(T... values)
    {
        for(T i : values)
            if(!contains(i))
                return false;
        
        return true;
    }

    @Override
    public boolean containsAll(Collection<? extends T> values)
    {
        for(T i : values)
            if(!contains(i))
                return false;
        
        return true;
    }

    @Override
    public boolean containsAny(T... values)
    {
        for(T i : values)
            if(contains(i))
                return true;
        
        return false;
    }

    @Override
    public boolean containsAny(Collection<? extends T> values)
    {
        for(T i : values)
            if(contains(i))
                return true;
        
        return false;
    }

    @Override
    public boolean overlapsWith(Range<? extends T> other)
    {
        if(otherIsAboveOrBelowThis(other))
            return false;
        
        if(other instanceof FlatRange)
            return true;
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
            if(overlapsWith(i))
                return true;

        return false;
    }

    @Override
    public boolean encloses(Range<? extends T> other)
    {
        return ((includesMin || !other.includesMin()) ? other.getMin().compareTo(min) >= 0 : other.getMin().compareTo(min) > 0)  // If other's min is above or equal to this' min
            && ((includesMax || !other.includesMax()) ? other.getMax().compareTo(max) <= 0 : other.getMax().compareTo(max) < 0); // If other's max is above or equal to this' max
    }

    @Override
    public boolean isEnclosedBy(Range<? extends T> other)
    {
        if(other instanceof FlatRange)
            return ((other.includesMin() || !includesMin) ? min.compareTo(other.getMin()) >= 0 : min.compareTo(other.getMin()) > 0)  // If this' min is above or equal to other's min
                && ((other.includesMax() || !includesMax) ? max.compareTo(other.getMax()) <= 0 : max.compareTo(other.getMax()) < 0); // if this' max is below or equal to other's max
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
            if(isEnclosedBy(i))
                return true;
        
        return false;
    }

    @Override
    public List<FlatRange<T>> toListOfFlatRanges()
    { return Arrays.<FlatRange<T>>asList(this); }

    @Override
    public FlatRange<T> toFlatRange()
    { return this; }

    @Override
    public Range<T> getOverlapWith(Range<? extends T> other)
    {
        if(!overlapsWith(other))
            return null;
        
        List<FlatRange<T>> overlaps = new ArrayList<FlatRange<T>>();
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
        {
            if(i.getMax().compareTo(min) < 0 || i.getMin().compareTo(max) > 0)
                continue;
            
            int minCompareResult = i.getMin().compareTo(min);
            int maxCompareResult = i.getMax().compareTo(max);
            
            T overlapMin = minCompareResult < 0 ? min : i.getMin();
            T overlapMax = maxCompareResult > 0 ? max : i.getMax();
            
            boolean includeMin = minCompareResult == 0 ? includesMin || i.includesMin()
                               : minCompareResult  < 0 ? includesMin
                               :                         i.includesMin();
            
            boolean includeMax = maxCompareResult == 0 ? includesMax || i.includesMax()
                               : maxCompareResult  > 0 ? includesMax
                               :                         i.includesMax();
            
            overlaps.add(new ValueRange<T>(overlapMin, includeMin, overlapMax, includeMax));
        }
        
        if(overlaps.isEmpty())
            return null;
        
        if(overlaps.size() == 1)
            overlaps.get(0);
        
        return new ExclusiveRange<T>(overlaps);
    }

    @Override
    public Range<T> getOverlapWith(Range<? extends T>... others)
    {
        Range<T> result = new ValueRange<T>(this);

        for(Range<? extends T> i : others)
            result = result.getOverlapWith(i);

        return result;
    }

    @Override
    public Range<T> getOverlapWith(Collection<? extends Range<? extends T>> others)
    {
        Range<T> result = new ValueRange<T>(this);

        for(Range<? extends T> i : others)
            result = result.getOverlapWith(i);

        return result;
    }

    @Override
    public Range<T> exclude(Range<? extends T> other)
    {
        if(other == null || otherIsAboveOrBelowThis(other))
            return this;
        
        if(other instanceof FlatRange)
        {
            FlatRange<? extends T> flatOther = (FlatRange<? extends T>)other;
            
            int minCompareResult = flatOther.getMin().compareTo(min);
            int maxCompareResult = flatOther.getMax().compareTo(max);
            
            FlatRange<T> bottomHalf = null;
            FlatRange<T> topHalf = null;
            
            if(minCompareResult > 0 || (minCompareResult == 0 && !flatOther.includesMin() && includesMin))
                bottomHalf = new ValueRange<T>(min, includesMin, other.getMin(), !other.includesMin());
            
            if(maxCompareResult < 0 || (minCompareResult == 0 && !flatOther.includesMax() && includesMax))
                topHalf = new ValueRange<T>(other.getMax(), !other.includesMax(), max, includesMax);
            
            return bottomHalf != null && topHalf != null ? new ExclusiveRange<T>(bottomHalf, topHalf)
                 : bottomHalf == null && topHalf != null ? topHalf
                 : bottomHalf != null && topHalf == null ? bottomHalf
                 :                                         null;
        }
        
        Range<T> result = this;
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
        {
            result = result.exclude(i);
            
            if(result == null)
                return null;
        }
        
        return result;
    }

    @Override
    public Range<T> excludeAll(Range<? extends T>... others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
        {
            result = result.exclude(i);
            
            if(result == null)
                return null;
        }
        
        return result;
    }

    @Override
    public Range<T> excludeAll(Collection<? extends Range<? extends T>> others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
        {
            result = result.exclude(i);
            
            if(result == null)
                return null;
        }
        
        return result;
    }

    @Override
    public Range<T> include(Range<? extends T> other)
    {
        if(other == null)
            return this;
        
        if(other instanceof FlatRange)
        {
            FlatRange<? extends T> flatOther = (FlatRange<? extends T>)other;
            
            if(otherIsAboveOrBelowThis(flatOther))
                return new ExclusiveRange<T>(this, flatOther);
            
            int minCompareResult = flatOther.getMin().compareTo(min);
            int maxCompareResult = flatOther.getMax().compareTo(max);
            
            boolean otherMinIsLess = minCompareResult < 0 || (minCompareResult == 0 && flatOther.includesMin() && !includesMin);
            boolean otherMaxIsMore = maxCompareResult > 0 || (maxCompareResult == 0 && flatOther.includesMax() && !includesMax);
            
            T newMin = otherMinIsLess ? flatOther.getMin() : min;
            T newMax = otherMaxIsMore ? flatOther.getMax() : max;
            boolean includeMinInNew = otherMinIsLess ? flatOther.includesMin() : includesMin;
            boolean includeMaxInNew = otherMaxIsMore ? flatOther.includesMax() : includesMax;
            
            return new ValueRange<T>(newMin, includeMinInNew, newMax, includeMaxInNew);
        }
        
        Range<T> result = this;
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
            result = result.include(i);
        
        return result;
    }

    @Override
    public Range<T> includeAll(Range<? extends T>... others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
            result = result.include(i);
        
        return result;
    }

    @Override
    public Range<T> includeAll(Collection<? extends Range<? extends T>> others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
            result = result.include(i);
        
        return result;
    }

    @Override
    public Range<T> includeValue(T toInclude)
    {
        int toIncludeComparedToMin = toInclude.compareTo(min);
        int toIncludeComparedToMax = toInclude.compareTo(max);
        
        if((min.compareTo(max)     == 0 && toIncludeComparedToMin == 0)
        || (toIncludeComparedToMin  > 0 && toIncludeComparedToMax  < 0)
        || (toIncludeComparedToMin == 0 && includesMin)
        || (toIncludeComparedToMax == 0 && includesMax))
            return this; // If passed value is already in this range.
        
        if(toIncludeComparedToMin == 0)
            return new ValueRange<T>(min, true, max, includesMax);
        
        if(toIncludeComparedToMax == 0)
            return new ValueRange<T>(min, includesMin, max, true);
        
        return new ExclusiveRange<T>(this, new ValueRange<T>(toInclude, toInclude));
    }

    @Override
    public Range<T> includeAllValues(T... toInclude)
    {
        Range<T> result = this;
        
        for(T i : toInclude)
            result = result.includeValue(i);
        
        return result;
    }

    @Override
    public Range<T> includeAllValues(Collection<? extends T> toInclude)
    {
        Range<T> result = this;
        
        for(T i : toInclude)
            result = result.includeValue(i);
        
        return result;
    }

    @Override
    public boolean coversTheSameValuesAs(Range<? extends T> other)
    {
        return other.isFlat()
            && other.getMin().compareTo(min) == 0
            && other.getMax().compareTo(max) == 0
            && other.includesMin() == includesMin
            && other.includesMax() == includesMax;
    }

    @Override
    public boolean includesMin()
    { return includesMin; }

    @Override
    public boolean includesMax()
    { return includesMax; }
    
    /**
     * Checks if the other range's min or max values are outwith this range's min or max values with no overlap.
     * @param other The range to check against this one.
     * @return True if the other range's min is above (not above-or-equal-to) this range's max or vice versa.
     */
    private boolean otherIsAboveOrBelowThis(Range<? extends T> other)
    {
        return ((includesMax && other.includesMin()) ? other.getMin().compareTo(max) > 0 : other.getMin().compareTo(max) >= 0)  // If other's min is above this' max
            || ((includesMin && other.includesMax()) ? other.getMax().compareTo(min) < 0 : other.getMax().compareTo(min) <= 0); // If other's max is below this' min
    }

    @Override
    public boolean isFlat()
    { return true; }
}