package com.enkigaming.lib.ranges;

import com.enkigaming.lib.encapsulatedfunctions.Transformer;
import com.enkigaming.lib.misc.SortedListHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExclusiveRange<T extends Comparable<T>> implements Range<T>
{
    private ExclusiveRange()
    { subranges = null; }
    
    public ExclusiveRange(T min, T max)
    { this(min, true, max, true); }
    
    public ExclusiveRange(T min, boolean includeMin, T max, boolean includeMax)
    { subranges = Arrays.<FlatRange<T>>asList(new ValueRange<T>(min, includeMin, max, includeMax)); }
    
    public ExclusiveRange(Range<? extends T> source)
    {
        subranges = new ArrayList<FlatRange<T>>();
        
        for(FlatRange<? extends T> i : source.toListOfFlatRanges())
            subranges.add(new ValueRange<T>(i));
    }
    
    public ExclusiveRange(Range<? extends T>... sources)
    {
        if(sources == null || sources.length == 0)
            throw new IllegalArgumentException("Cannot create an empty range.");
        
        Range<T> inner = new ExclusiveRange<T>(sources[0]);
        
        for(int i = 1; i < sources.length; i++)
            inner = inner.include(sources[i]);
        
        subranges = inner.toListOfFlatRanges();
    }
    
    public ExclusiveRange(Collection<? extends Range<? extends T>> sources)
    {
        if(sources == null || sources.isEmpty())
            throw new IllegalArgumentException("Cannot create an empty range.");
        
        Range<T> inner = null;
        boolean first = true;
        
        for(Range<? extends T> i : sources)
        {
            if(first)
            {
                inner = new ExclusiveRange<T>(i);
                first = false;
                continue;
            }
            
            inner = inner.include(i); // "dereferencing possible null pointer" shite.
        }
        
        subranges = inner.toListOfFlatRanges(); // It's impossible for this to be null as well.
    }
    
    public ExclusiveRange(T min, T max, Range<? extends T> toExclude)
    { this(min, true, max, true, toExclude); }
    
    public ExclusiveRange(T min, T max, Range<? extends T>... toExclude)
    { this(min, true, max, true, toExclude); }
    
    public ExclusiveRange(T min, T max, Collection<? extends Range<? extends T>> toExclude)
    { this(min, true, max, true, toExclude); }
    
    public ExclusiveRange(T min, boolean includeMin, T max, boolean includeMax, Range<? extends T> toExclude)
    { subranges = new ValueRange<T>(min, includeMin, max, includeMax).exclude(toExclude).toListOfFlatRanges(); }
    
    public ExclusiveRange(T min, boolean includeMin, T max, boolean includeMax, Range<? extends T>... toExclude)
    { subranges = new ValueRange<T>(min, includeMin, max, includeMax).excludeAll(toExclude).toListOfFlatRanges(); }
    
    public ExclusiveRange(T min, boolean includeMin, T max, boolean includeMax,
                          Collection<? extends Range<? extends T>> toExclude)
    { subranges = new ValueRange<T>(min, includeMin, max, includeMax).excludeAll(toExclude).toListOfFlatRanges(); }
    
    List<FlatRange<T>> subranges;

    @Override
    public T getMin()
    { return subranges.get(0).getMin(); }

    @Override
    public T getMax()
    { return subranges.get(subranges.size() - 1).getMax(); }

    @Override
    public boolean contains(T value)
    {
        if(value == null)
            return false;
        
        for(FlatRange<T> i : subranges)
            if(i.contains(value))
                return true;
        
        return false;
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
        if(other == null)
            return false;
        
        for(FlatRange<T> i : subranges)
            if(i.overlapsWith(other))
                return true;
        
        return false;
    }

    @Override
    public boolean encloses(Range<? extends T> other)
    {
        if(other == null)
            return false;
        
        List<FlatRange<? extends T>> othersNotEnclosed
            = new ArrayList<FlatRange<? extends T>>(other.toListOfFlatRanges());
        
        for(int i = othersNotEnclosed.size() - 1; i >= 0; i--)
            for(FlatRange<T> j : subranges)
                if(j.encloses(othersNotEnclosed.get(i)))
                {
                    othersNotEnclosed.remove(i);
                    break;
                }
        
        return othersNotEnclosed.isEmpty(); // If all subranges of other were fully covered by this object's subranges.
    }

    @Override
    public boolean isEnclosedBy(Range<? extends T> other)
    {
        if(other == null)
            return false;
        
        List<FlatRange<T>> notEnclosed = new ArrayList<FlatRange<T>>(subranges);
        
        for(int i = notEnclosed.size() - 1; i >= 0; i--)
            for(FlatRange<? extends T> j : other.toListOfFlatRanges())
                if(notEnclosed.get(i).isEnclosedBy(j))
                {
                    notEnclosed.remove(i);
                    break;
                }
        
        return notEnclosed.isEmpty(); // If all of this object's subranges were fully covered by other's subranges.
    }

    @Override
    public List<FlatRange<T>> toListOfFlatRanges()
    { return new ArrayList<FlatRange<T>>(subranges); }

    @Override
    public FlatRange<T> toFlatRange()
    {
        return new ValueRange<T>(subranges.get(0).getMin(),
                                 subranges.get(0).includesMin(),
                                 subranges.get(subranges.size() - 1).getMax(),
                                 subranges.get(subranges.size() - 1).includesMax());
    }

    @Override
    public Range<T> getOverlapWith(Range<? extends T> other)
    {
        if(other == null)
            return null;
        
        Collection<Range<? extends T>> overlaps = new ArrayList<Range<? extends T>>();
        
        for(FlatRange<T> i : subranges)
        {
            Range<T> iOverlap = i.getOverlapWith(other);
            
            if(iOverlap != null)
                overlaps.add(iOverlap);
        }
        
        Range<T> combined = new ExclusiveRange<T>(overlaps);
        return combined.isFlat() ? new ValueRange<T>(combined) : combined;
    }

    @Override
    public Range<T> getOverlapWith(Range<? extends T>... others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
        {
            result = result.getOverlapWith(i);
            
            if(result == null)
                return null;
        }
        
        return result;
    }

    @Override
    public Range<T> getOverlapWith(Collection<? extends Range<? extends T>> others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
        {
            result = result.getOverlapWith(i);
            
            if(result == null)
                return null;
        }
        
        return result;
    }

    @Override
    public Range<T> exclude(Range<? extends T> other)
    {
        if(other == null)
            return this;
        
        Collection<Range<T>> amendedSubranges = new ArrayList<Range<T>>();

        for(FlatRange<T> i : subranges)
        {
            Range<T> iResult = i.exclude(other);
            
            if(iResult != null)
                amendedSubranges.add(iResult);
        }
        
        Range<T> result = new ExclusiveRange<T>(amendedSubranges);
        return result.isFlat() ? new ValueRange<T>(result) : result;
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
            List<FlatRange<T>> mergedSubranges = new ArrayList<FlatRange<T>>(subranges);
            insert(mergedSubranges, new ValueRange<T>(flatOther));
            mergeOverlappingSubranges(mergedSubranges);
            
            Range<T> result = makeExclusiveRangeWith(mergedSubranges);
            return result.isFlat() ? new ValueRange<T>(result) : result;
        }
        
        Range<T> result = this;
        
        for(FlatRange<? extends T> i : other.toListOfFlatRanges())
            result = result.include(i);
        
        return result.isFlat() && !(result instanceof FlatRange) ? new ValueRange<T>(result) : result;
    }

    @Override
    public Range<T> includeAll(Range<? extends T>... others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
            result = result.include(i);
        
        return result.isFlat() && !(result instanceof FlatRange) ? new ValueRange<T>(result) : result;
    }

    @Override
    public Range<T> includeAll(Collection<? extends Range<? extends T>> others)
    {
        Range<T> result = this;
        
        for(Range<? extends T> i : others)
            result = result.include(i);
        
        return result.isFlat() && !(result instanceof FlatRange) ? new ValueRange<T>(result) : result;
    }

    @Override
    public Range<T> includeValue(T toInclude)
    {
        for(int i = 0; i < subranges.size(); i++)
        {
            FlatRange<T> subrange = subranges.get(i);
            
            if(subrange.contains(toInclude))
                return this;
            
            if(!subrange.includesMin() && subrange.getMin().compareTo(toInclude) == 0)
            {
                List<FlatRange<T>> subrangesCopy = new ArrayList<FlatRange<T>>(subranges);
                FlatRange<T> previousSubrange = i > 0 ? subranges.get(i - 1) : null;
                
                if(previousSubrange != null // If there is a previous subrange
                && previousSubrange.getMax().compareTo(subrange.getMin()) == 0) // If toInclude should merge two ranges
                {
                    // Replace previousSubrange and subrange with a new value range spanning them both.
                    subrangesCopy.remove(i);
                    subrangesCopy.remove(i - 1);
                    subrangesCopy.add(i - 1, new ValueRange<T>(previousSubrange.getMin(),
                                                               previousSubrange.includesMin(),
                                                               subrange.getMax(),
                                                               subrange.includesMax()));
                    return subrangesCopy.size() == 1 ? subrangesCopy.get(0) : makeExclusiveRangeWith(subrangesCopy);
                }
                
                // replace subrange with copy that includes its min value
                subrangesCopy.remove(i);
                subrangesCopy.add(i, new ValueRange<T>(subrange.getMin(), true, subrange.getMax(), subrange.includesMax()));
                return makeExclusiveRangeWith(subrangesCopy);
            }
            
            if(!subrange.includesMax() && subrange.getMax().compareTo(toInclude) == 0)
            {
                List<FlatRange<T>> subrangesCopy = new ArrayList<FlatRange<T>>(subranges);
                FlatRange<T> nextSubrange = i < subranges.size() - 1 ? subranges.get(subranges.size() - 1) : null;
                
                if(nextSubrange != null // If there is a next subrange
                && nextSubrange.getMin().compareTo(subrange.getMax()) == 0)
                {
                    // Replace subrange and nextSubrange with a new value range spanning them both.
                    subrangesCopy.remove(i);
                    subrangesCopy.remove(i);
                    subrangesCopy.add(i, new ValueRange<T>(subrange.getMin(),
                                                           subrange.includesMin(),
                                                           nextSubrange.getMax(),
                                                           nextSubrange.includesMax()));
                    return subrangesCopy.size() == 1 ? subrangesCopy.get(0) : makeExclusiveRangeWith(subrangesCopy);
                }
                
                // replace subrange with copy that includes its max value
                subrangesCopy.remove(i);
                subrangesCopy.add(i, new ValueRange<T>(subrange.getMin(), subrange.includesMin(), subrange.getMax(), true));
                return makeExclusiveRangeWith(subrangesCopy);
            }
        }
        
        // At this point, the passed value is not covered by any subranges, and is not part of the definition of any
        // subranges that don't include it. (e.g. as the min value of a range that doesn't include its min value)
        // Only thing to do now is insert the value is a single-value flat range.
        
        List<FlatRange<T>> subrangesCopy = new ArrayList<FlatRange<T>>(subranges);
        insert(subrangesCopy, new ValueRange<T>(toInclude));
        return makeExclusiveRangeWith(subrangesCopy);
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
    public List<Range<T>> splitBy(T splitPoint)
    {
        List<FlatRange<T>> bottomHalfSubranges = new ArrayList<FlatRange<T>>();
        List<FlatRange<T>> topHalfSubranges    = new ArrayList<FlatRange<T>>();
        boolean reachedSplitPoint = false;
        
        for(FlatRange<T> i : subranges)
        {
            if(reachedSplitPoint)
            {
                topHalfSubranges.add(i);
                continue;
            }
            
            if(i.contains(splitPoint))
            {
                List<Range<T>> splits = i.splitBy(splitPoint); // all members are instanceof FlatRange
                bottomHalfSubranges.add((FlatRange<T>)splits.get(0));
                topHalfSubranges.add((FlatRange<T>)splits.get(1));
                reachedSplitPoint = true;
                continue;
            }
            
            if(i.getMin().compareTo(splitPoint) >= 0) // If the loop has already passed the split point
            {
                topHalfSubranges.add(i);
                reachedSplitPoint = true;
                continue;
            }
            
            bottomHalfSubranges.add(i);
        }
        
        Range<T> bottom = bottomHalfSubranges.isEmpty()   ? null
                        : bottomHalfSubranges.size() == 1 ? bottomHalfSubranges.get(0)
                        :                                   makeExclusiveRangeWith(bottomHalfSubranges);
        
        Range<T> top = topHalfSubranges.isEmpty()   ? null
                     : topHalfSubranges.size() == 1 ? topHalfSubranges.get(0)
                     :                                makeExclusiveRangeWith(topHalfSubranges);
        
        List<Range<T>> result = new ArrayList<Range<T>>();
        
        if(bottom != null)
            result.add(bottom);
        
        if(top != null)
            result.add(top);
        
        if(result.isEmpty())
            throw new RuntimeException("Error in ExclusiveRange.splitBy - splitting an existing exclusive range has "
                                       + "somehow resulted in both an empty bottom-half and empty top-half. That is, "
                                       + "both splitting the range resulted in two empty (and thus, null) ranges.");
        
        return result;
    }

    @Override
    public List<Range<T>> splitBy(T... splitPoints)
    { return splitBy(Arrays.asList(splitPoints)); }

    @Override
    public List<Range<T>> splitBy(Collection<? extends T> splitPoints)
    {
        List<FlatRange<T>> subrangesCopy = new ArrayList<FlatRange<T>>(subranges);
        List<T> splitPointsList = new ArrayList<T>(splitPoints);
        Collections.sort(splitPointsList);
        List<Range<T>> results = new ArrayList<Range<T>>();
        List<FlatRange<T>> currentSubranges = new ArrayList<FlatRange<T>>();
        
        for(int i = 0, j = 0; i < splitPointsList.size(); i++)
        {
            T splitPoint = splitPointsList.get(i);
            
            while(j < subrangesCopy.size())
            {
                FlatRange<T> subrange = subrangesCopy.get(j);
                
                if(subrange.contains(splitPoint))
                {
                    List<Range<T>> splits = subrange.splitBy(splitPoint);
                    currentSubranges.add((FlatRange<T>)splits.get(0));
                    results.add(makeExclusiveRangeWith(currentSubranges));
                    currentSubranges = new ArrayList<FlatRange<T>>();
                    currentSubranges.add((FlatRange<T>)splits.get(1));
                }
                
                if(subrange.getMin().compareTo(splitPoint) <= 0) // Split point was between last and current
                {
                    results.add(makeExclusiveRangeWith(currentSubranges));
                    currentSubranges = new ArrayList<FlatRange<T>>();
                    currentSubranges.add(subrange);
                }
                
                // Split point has yet to be reached.
                currentSubranges.add(subrange);
                
                if(j == subrangesCopy.size() - 1) // If this is the last subrange
                    results.add(currentSubranges.size() == 1 ? currentSubranges.get(0) : makeExclusiveRangeWith(currentSubranges));
            }
        }
        
        return results;
    }

    @Override
    public boolean coversTheSameValuesAs(Range<? extends T> other)
    {
        List<? extends FlatRange<? extends T>> otherSubranges = other.toListOfFlatRanges();
        
        if(otherSubranges.size() != subranges.size())
            return false;
        
        for(int i = 0; i < subranges.size(); i++)
            if(!subranges.get(i).coversTheSameValuesAs(otherSubranges.get(i)))
                return false;
        
        return true;
    }

    @Override
    public boolean includesMin()
    { return subranges.get(0).includesMin(); }

    @Override
    public boolean includesMax()
    { return subranges.get(subranges.size() - 1).includesMax(); }

    @Override
    public boolean isFlat()
    { return subranges.size() == 1; }
    
    /**
     * Ensures that all flatranges in the passed (sorted) list are separate, that all the same values are covered, but
     * by single, contiguous, non-overlapping flat ranges.
     * @param <T> The type of the flatranges involved.
     * @param subranges The list of flat ranges in which to merge the overlapping ranges of.
     */
    private static <T extends Comparable<T>> void mergeOverlappingSubranges(List<FlatRange<T>> subranges)
    {
        for(int i = 0; i < subranges.size(); i++)
        {
            FlatRange<T> iSubrange = subranges.get(i);
            
            for(int j = i + 1; j < subranges.size();)
            {
                FlatRange<T> jSubrange = subranges.get(j);
                
                if(iSubrange.contains(jSubrange.getMax()) // subrange I contains subrange J
                || (iSubrange.getMax().compareTo(jSubrange.getMax()) == 0
                    && !iSubrange.includesMax()
                    && !jSubrange.includesMax()))
                { subranges.remove(j); }
                else if(jSubrange.contains(iSubrange.getMax()) // subrange I overlaps with subrange J
                     || iSubrange.getMax().compareTo(jSubrange.getMax()) == 0 && iSubrange.includesMax()) // subrange I connects to (but doesn't overlap with) subrange j
                {
                    FlatRange<T> combined = new ValueRange<T>(iSubrange.getMin(), iSubrange.includesMin(),
                                                              jSubrange.getMax(), jSubrange.includesMax());
                    subranges.remove(j);
                    subranges.remove(i);
                    subranges.add(i, combined);
                    iSubrange = combined;
                }
                else // subrange I doesn't doesn't make any contact with subrange J
                { break; }
            }
        }
    }
    
    /**
     * Ensures that the list toSort is correctly sorted. Sorted using the minimum bound of the ranges.
     * @param <T> The type of the ranges involved.
     * @param toSort The list to sort.
     */
    private static <T extends Comparable<T>> void sort(List<Range<T>> toSort)
    {
        Collections.sort(toSort, new Comparator<Range<T>>()
        {
            @Override
            public int compare(Range<T> first, Range<T> second)
            { return first.getMin().compareTo(second.getMin()); }
        });
    }
    
    /**
     * Inserts toInsert into the correct place in the sorted toInsertInto
     * @param <T> The type of the flatranges concerned.
     * @param toInsertInto The list having a value inserted.
     * @param toInsert The value to insert into toInsertInto
     */
    private static <T extends Comparable<T>> void insert(List<FlatRange<T>> toInsertInto, FlatRange<T> toInsert)
    {
        SortedListHandler<FlatRange<T>> inserter
            = new SortedListHandler<FlatRange<T>>(new Transformer<FlatRange<T>, Comparable>()
        {
            @Override
            public Comparable get(FlatRange<T> parent)
            { return parent.getMin(); }
        });
        
        inserter.insert(toInsertInto, toInsert);
    }
    
    /**
     * Creates an exclusive range from a sorted list of subranges, without the additional sorting/merging
     * that new ExclusiveRange(Collection<? extends FlatRange<? exnteds T>>) does.
     * @param <T> The type of the range
     * @param subranges The list of flat ranges that will be in the 
     * @return 
     */
    private static <T extends Comparable<T>> ExclusiveRange<T> makeExclusiveRangeWith(List<FlatRange<T>> subranges)
    {
        ExclusiveRange<T> result = new ExclusiveRange<T>();
        result.subranges = subranges;
        return result;
    }
}