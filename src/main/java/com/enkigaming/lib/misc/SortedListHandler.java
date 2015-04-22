package com.enkigaming.lib.misc;

import com.enkigaming.lib.encapsulatedfunctions.Transformer;
import java.util.ArrayList;
import java.util.List;

// To do: Update Javadoc.

/**
 * Provides methods for sorting, and searching through, a list according to one of its members.
 * @param <T> The generic datatype of the list this is used on.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public class SortedListHandler<T>
{
    public static class BinarySearchToAddResult
    {
        public BinarySearchToAddResult(boolean found, int position)
        { this(position, found); }

        public BinarySearchToAddResult(int position, boolean found)
        {
            matchFound = found;
            this.position = position;
        }

        final private boolean matchFound;
        final private int position;

        public boolean matchWasFound()
        { return matchFound; }

        public int getPosition()
        { return position; }
    }

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Constructs the handler.
     * @param keyGetter The encapsulated function for determining the key a list should be sorted by.
     */
    public SortedListHandler(Transformer<T, Comparable> keyGetter) { this.keyGetter = keyGetter; }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fields">
    Transformer<T, Comparable> keyGetter;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Methods">
    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    /**
     * Gets the key of the passed object, according to the lambda passed in at construction.
     * @param ContainingObject
     * @return
     */
    protected Comparable getKey(T ContainingObject)
    { return keyGetter.get(ContainingObject); }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Sorting">
    /**
     * Sorts a list according to the member that getKey returns using QuickSort recursively.
     * @param ToSort The list to search.
     * @return The sorted version of the list (The list passed in isn't actually modified)
     * @note Comparable.compareTo(Comparable) is used for sorting.
     */
    public List<T> quickSort(List<T> ToSort)
    {
        if(ToSort.size() > 0)
            return internalQuickSort(ToSort);
        else
            return ToSort;
    }

    List<T> internalQuickSort(List<T> ToSort)
    {
        if(ToSort.size() <= 1)
            return ToSort;
        else
        {
            List<T> LessList = new ArrayList<T>();
            int PivotPosition = (ToSort.size() / 2) - 1;
            T Pivot = ToSort.get(PivotPosition);
            List<T> GreaterList = new ArrayList<T>();

            for(int i = 0; i < ToSort.size(); i++)
            {
                if(i == PivotPosition)
                { /* Sod-all */ }
                else if(getKey(ToSort.get(i)).compareTo(getKey(Pivot)) <= 0)
                    LessList.add(ToSort.get(i));
                else
                    GreaterList.add(ToSort.get(i));
            }

            return quicksortCombine(quickSort(LessList), Pivot, quickSort(GreaterList));
        }
    }

    List<T> quicksortCombine(List<T> Less, T Pivot, List<T> Greater)
    {
        List<T> CombinedList = new ArrayList<T>();

        for(int i = 0; i < Less.size(); i++)
            CombinedList.add(Less.get(i));

        CombinedList.add(Pivot);

        for(int i = 0; i < Greater.size(); i++)
            CombinedList.add(Greater.get(i));

        return CombinedList;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Searching">
    /**
     * Searches a (presumably sorted) list for an object that contains the passed object as a member according to getKey(T).
     * @param ToSearch The list to search.
     * @param ToSearchFor What to search for.
     * @return The position in the list where a matching element was found, or -1 if one wasn't found.
     */
    public int binarySearch(List<T> ToSearch, Comparable ToSearchFor)
    {
        if(ToSearch.size() > 0)
            return binarySearch(ToSearch, ToSearchFor, 0, ToSearch.size() - 1);
        else
            return -1;
    }

    int binarySearch(List<T> ToSearch, Comparable ToSearchFor, int LowerBound, int UpperBound)
    {
        if(LowerBound >= UpperBound)
        {
            if(ToSearchFor.compareTo(getKey(ToSearch.get(LowerBound))) == 0)
                return LowerBound;
            else
                return -1;
        }
        else
        {
            int Mid = LowerBound + ((UpperBound - LowerBound) / 2);
            int temp = ToSearchFor.compareTo(getKey(ToSearch.get(Mid)));

            if(temp == 0)
                return Mid;
            else if(temp > 0)
                return binarySearch(ToSearch, ToSearchFor, Mid + 1, UpperBound);
            else
                return binarySearch(ToSearch, ToSearchFor, LowerBound, Mid - 1);
        }
    }

    /**
     * Searches a (presumably sorted) list for an object that contains the passed object as a member according to getKey(T).
     * @param ToSearch The list to search.
     * @param ToSearchFor What to search for.
     * @return A pair containing a boolean stating whether or not there was an element with a matching field.
     */
    public BinarySearchToAddResult binarySearchForPositionToAdd(List<T> ToSearch, Comparable ToSearchFor)
    {
        if(ToSearch.size() > 0)
            return binarySearchForPositionToAdd(ToSearch, ToSearchFor, 0, ToSearch.size() - 1);
        else
            return new BinarySearchToAddResult(0, false);
    }

    BinarySearchToAddResult binarySearchForPositionToAdd(List<T> ToSearch, Comparable ToSearchFor, int LowerBound, int UpperBound)
    {
        if(LowerBound >= UpperBound)
        {
            int Comparison = ToSearchFor.compareTo(getKey(ToSearch.get(LowerBound)));

            if(Comparison > 0)
                return new BinarySearchToAddResult(LowerBound + 1, false);
            else if(Comparison < 0)
                return new BinarySearchToAddResult(LowerBound, false);
            else
                return new BinarySearchToAddResult(LowerBound, true);
        }
        else
        {
            int Mid = LowerBound + ((UpperBound - LowerBound) / 2);
            int temp = ToSearchFor.compareTo(getKey(ToSearch.get(Mid)));

            if(temp > 0)
                return binarySearchForPositionToAdd(ToSearch, ToSearchFor, Mid + 1, UpperBound);
            else if(temp < 0)
                return binarySearchForPositionToAdd(ToSearch, ToSearchFor, LowerBound, Mid - 1);
            else
                return new BinarySearchToAddResult(Mid, true);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Insertion">
    /**
     * Inserts a given object into the given list at the correct position.
     * @param ListToAddTo The list to add the item to.
     * @param ItemToInsert The item to add to the list.
     * @return The position at which the element was added, or -1 if there was already a definition with the same name.
     */
    public int insert(List<T> ListToAddTo, T ItemToInsert)
    {
        BinarySearchToAddResult result = binarySearchForPositionToAdd(ListToAddTo, getKey(ItemToInsert));

        if(!result.matchWasFound())
        {
            ListToAddTo.add(result.getPosition(), ItemToInsert);
            return result.getPosition();
        }
        else
            return -1;
    }
    
    public int insertEvenIfAlreadyPresent(List<T> listToAddTo, T itemToInsert)
    {
        BinarySearchToAddResult result = binarySearchForPositionToAdd(listToAddTo, getKey(itemToInsert));
        
        listToAddTo.add(result.getPosition(), itemToInsert);
        return result.getPosition();
    }
    //</editor-fold>
    //</editor-fold>
}