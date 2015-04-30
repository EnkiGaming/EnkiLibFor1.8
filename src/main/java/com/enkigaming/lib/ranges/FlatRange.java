package com.enkigaming.lib.ranges;

import java.util.Collection;
import java.util.List;

    /**
     * A single, contiguous, immutable series of numbers or other comparables, as "X to Y", representing all comparables
     * between the min and max values with no exclusions.
     * @param <T> The type this is to be a range of.
     */
public interface FlatRange<T extends Comparable<T>> extends Range<T>
{
    /**
     * Splits the range into a pair of ranges in a list, .get(0) covering all values below the passed split point and
     * .get(1) covering all values above the passed split point. The returned list will contain all resulting ranges,
     * and using a split point outwith the range should result in the list containing a single, unsplit range. All
     * ranges in the returned list will be flat ranges.
     * @param splitPoint The point at which the range should be split.
     * @return A list of ranges containing the two resulting ranges, or one if the splitpoint was outwith the range.
     * .get(0) is the lower range as .get(1) is the higher range.
     */
    @Override
    List<Range<T>> splitBy(T splitPoint);

    /**
     * Splits the range into a list of flat ranges (ordered from lowest to highest) covering the same values as this
     * range, split by the passed values as split-points. All returned ranges should be contiguous.
     * @param splitPoints The values by which to split this range.
     * @return A list of contiguous flat ranges, in order from lowest to highest, representing this range split
     * into subranges at the passed points.
     */
    @Override
    List<Range<T>> splitBy(T... splitPoints);

    /**
     * Splits the range into a list of flat ranges (ordered from lowest to highest) covering the same values as this
     * range, split by the passed values as split-points. All returned ranges should be contiguous.
     * @param splitPoints The values by which to split this range.
     * @return A list of contiguous flat ranges, in order from lowest to highest, representing this range split
     * into subranges at the passed points.
     */
    @Override
    List<Range<T>> splitBy(Collection<? extends T> splitPoints);
}