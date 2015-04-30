package com.enkigaming.lib.ranges;

import java.util.Collection;
import java.util.List;

/**
 * An immutable series of numbers or other comparables, as "X to Y", possibly excluding subranges.
 * @param <T> The type this is to be a range of.
 */
public interface Range<T extends Comparable<T>> // Place in EnkiLib
{
    /**
     * Gets the lower bound of the range. Id est, the lowest value of the range.
     * @return The lower bound.
     */
    T getMin();

    /**
     * Gets the upper bound of the range. Id est, the highest value of the range.
     * @return The upper bound.
     */
    T getMax();

    /**
     * Whether or not the passed value is represented in this range.
     * @param value The value to check for.
     * @return True if it is represented. Otherwise, false.
     */
    boolean contains(T value);

    /**
     * Whether or not all of the passed values are represented in this range.
     * @param values The values to check for.
     * @return True if all passed values are represented. Otherwise, false.
     */
    boolean containsAll(T... values);

    /**
     * Whether or not all of the passed values are represented in this range.
     * @param values The values to check for.
     * @return True if all passed values are represented. Otherwise, false.
     */
    boolean containsAll(Collection<? extends T> values);

    /**
     * Whether or not any of the passed values are represented in this range.
     * @param values The values to check for.
     * @return True if any of the passed values are represented. Otherwise, false.
     */
    boolean containsAny(T... values);

    /**
     * Whether or not any of the passed values are represented in this range.
     * @param values The values to check for.
     * @return True if any of the passed values are represented. Otherwise, false.
     */
    boolean containsAny(Collection<? extends T> values);

    /**
     * Whether or not the passed range has any overlap with this one. Id est, any of the values represented by this
     * range are also represented by that range.
     * @param other The range to check for overlap with this one.
     * @return True if any values are represented by both ranges. Otherwise, false.
     */
    boolean overlapsWith(Range<? extends T> other);

    /**
     * Whether or not the passed range is entirely enclosed by this one. Id est, all of the values represented by
     * that range are also represented by this range.
     * @param other The range to check.
     * @return True if all values represented by that range are represented in this range. Otherwise, false.
     */
    boolean encloses(Range<? extends T> other);

    /**
     * Whether or not the this range is entirely enclosed by the other one. Id est, all of the values represented by
     * this range are also represented by that range.
     * @param other The range to check.
     * @return True if all values represented by this range are represented in that range. Otherwise, false.
     */
    boolean isEnclosedBy(Range<? extends T> other);

    /**
     * Gets a copy of this range as a list of FlatRanges rather than as a single range. Such that no ranges overlap
     * or share the same min/max values (i.e. they're collectively contiguous), and the list is in order starting
     * with the lowest ranges and ending with the highest.
     * @return 
     */
    List<FlatRange<T>> toListOfFlatRanges();

    /**
     * Gets a copy of this range as a single, fully contiguous range. That is, gets a copy of this range without
     * excluding any values between the lower and upper bounds.
     * @return 
     */
    FlatRange<T> toFlatRange();

    /**
     * Gets a range of all values that are overlapping between this range and the passed range.
     * @param other The other range to compare against this one to get overlapping between the two.
     * @return A range of all values overlapping between this range and that one. Null if there is no overlap.
     */
    Range<T> getOverlapWith(Range<? extends T> other);

    /**
     * Gets a range of all values that are overlapping between this range and all passed ranges.
     * @param others The other ranges to compare with this one to get overlapping ranges between all.
     * @return A range of all values that overlap with this and all passed ranges. Null if there is no overlap.
     */
    Range<T> getOverlapWith(Range<? extends T>... others);

    /**
     * Gets a range of all values that are overlapping between this range and all passed ranges.
     * @param others The other ranges to compare with this one to get overlapping ranges between all.
     * @return A range of all values that overlap with this and all passed ranges. Null if there is no overlap.
     */
    Range<T> getOverlapWith(Collection<? extends Range<? extends T>> others);

    /**
     * Gets a copy of this range excluding the passed range.
     * @param other The range to exclude from this one.
     * @return A copy of this range excluding the values of the passed range. Null if the entire ranges is excluded.
     */
    Range<T> exclude(Range<? extends T> other);

    /**
     * Gets a copy of this range excluding all passed ranges.
     * @param others The ranges to exclude from this one.
     * @return A copy of this range excluding the values of all passed ranges.
     */
    Range<T> excludeAll(Range<? extends T>... others);

    /**
     * Gets a copy of this range excluding all passed ranges.
     * @param others The ranges to exclude from this one.
     * @return A copy of this range excluding the values of all passed ranges.
     */
    Range<T> excludeAll(Collection<? extends Range<? extends T>> others);

    /**
     * Gets a copy of this range including the passed range.
     * @param other The range to combine with this one.
     * @return A copy of this range with the values of the passed range included.
     */
    Range<T> include(Range<? extends T> other);

    /**
     * Gets a copy of this range including all passed ranges.
     * @param others The ranges to combine with this one.
     * @return A copy of this range with the values of all of the passed ranges included.
     */
    Range<T> includeAll(Range<? extends T>... others);

    /**
     * Gets a copy of this range including all passed ranges.
     * @param others The ranges to combine with this one.
     * @return A copy of this range with the values of all of the passed ranges included.
     */
    Range<T> includeAll(Collection<? extends Range<? extends T>> others);
    
    /**
     * Gets a copy of this range including the single passed value.
     * @param toInclude The value to include in this range.
     * @return A copy of this range with the passed value included.
     */
    Range<T> includeValue(T toInclude);
    
    /**
     * Gets a copy of this range include all of the individual passed values.
     * @param toInclude The values to include in the returned range.
     * @return A copy of this range with the individual passed values included.
     */
    Range<T> includeAllValues(T... toInclude);
    
    /**
     * Gets a copy of this range include all of the individual passed values.
     * @param toInclude The values to include in the returned range.
     * @return A copy of this range with the individual passed values included.
     */
    Range<T> includeAllValues(Collection<? extends T> toInclude);
    
    /**
     * Splits the range into a pair of ranges in a list, .get(0) covering all values below the passed split point and
     * .get(1) covering all values above the passed split point. The returned list will contain all resulting ranges,
     * and using a split point outwith the range should result in the list containing a single, unsplit range.
     * @param splitPoint The point at which the range should be split.
     * @return A list of ranges containing the two resulting ranges, or one if the splitpoint was outwith the range.
     * .get(0) is the lower range as .get(1) is the higher range.
     */
    List<Range<T>> splitBy(T splitPoint);
    
    /**
     * Splits the range into a list of ranges (ordered from lowest to highest) covering the same values as this
     * range, split by the passed values as split-points.
     * @param splitPoints The values by which to split this range.
     * @return A list of ranges, in order from lowest to highest, representing this range split into subranges at the
     * passed points.
     */
    List<Range<T>> splitBy(T... splitPoints);
    
    /**
     * Splits the range into a list of ranges (ordered from lowest to highest) covering the same values as this
     * range, split by the passed values as split-points.
     * @param splitPoints The values by which to split this range.
     * @return A list of ranges, in order from lowest to highest, representing this range split into subranges at the
     * passed points.
     */
    List<Range<T>> splitBy(Collection<? extends T> splitPoints);
    
    /**
     * Returns true if all values covered by other are also covered by this, but no more. And vice versa.
     * @param other The range this is being compared against.
     * @return True if other is not null and covers all of the same values as this. Otherwise, false.
     */
    boolean coversTheSameValuesAs(Range<? extends T> other);
    
    /**
     * Whether or not this range includes the value that marks its minimum bound.
     * @return True if this range includes the value that marks its minimum bound. Otherwise, false.
     */
    boolean includesMin();
    
    /**
     * Whether or not this range includes the value that marks its maximum bound.
     * @return True if this range includes the value that marks its maximum bound. Otherwise, false.
     */
    boolean includesMax();
    
    /**
     * Whether or not this range represents a contiguous range of values.
     * @return True if this range is contiguous. Otherwise, false.
     */
    boolean isFlat();
}