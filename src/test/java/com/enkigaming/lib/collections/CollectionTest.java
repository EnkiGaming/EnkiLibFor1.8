package com.enkigaming.lib.collections;

import com.enkigaming.lib.extensions.ComparableMethods;
import com.enkigaming.lib.misc.TieredIncrementor;
import org.junit.Test;
import com.enkigaming.lib.testing.ThrowableAssertion;
import static org.junit.Assert.*;
import static com.enkigaming.lib.testing.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;

public abstract class CollectionTest
{
    public abstract Collection<String> getStringCollection();
    
    public abstract Collection<String> getStringCollection(String... presetValues);
    
    public abstract boolean canContainDuplicates();
    
    public abstract boolean canContainNull();
    
    public abstract boolean isMutable();
    
    //<editor-fold defaultstate="collapsed" desc="add">
    public void testAdd(String msg, String toAdd, String[] presetValues)
    {
        // Don't run tests with null values already in when the collection can't contain null
        if(ArrayUtils.contains(presetValues, null) && !canContainNull())
            return;
        
        // Don't run tests with duplicate values already in when the collection can't contain duplicate values.
        if(!canContainDuplicates())
            for(int i = 0; i < presetValues.length; i++)
                for(int j = 0; j < presetValues.length; j++)
                    if(i != j)
                        if((presetValues[i] == null && presetValues[j] == null)
                           || (presetValues[i] != null && presetValues[j] != null && presetValues[i].equals(presetValues[j])))
                            return;
        
        final Collection<String> testCollection = getStringCollection(presetValues);
        assertCollectionEquals(msg + ".presetvalues", testCollection, Arrays.asList(presetValues));
        
        if(!isMutable())
        {
            boolean result;
            
            try
            { result = testCollection.add(toAdd); }
            catch(UnsupportedOperationException e)
            { result = false; }
            
            assertCollectionEquals(msg + ".immutable.resultantcollection", testCollection, Arrays.asList(presetValues));
            assertFalse(msg + ".immutable.returnvalue", result);
            return;
        }
        
        if(toAdd == null && !canContainNull())
        {
            new ThrowableAssertion(msg + ".addnull.exception", IllegalArgumentException.class, NullPointerException.class)
            {
                @Override
                public void code() throws Throwable
                { testCollection.add(null); }
            };
            
            assertCollectionEquals(msg + ".addnull.resultantcollection", testCollection, Arrays.asList(presetValues));
            return;
        }
        
        boolean testBool = testCollection.add(toAdd);
        
        if(ArrayUtils.contains(presetValues, toAdd) && !canContainDuplicates())
        {
            assertCollectionEquals(msg + ".addduplicate.resultantcollection", testCollection, Arrays.asList(presetValues));
            assertFalse(msg + ".addduplicate.returnvalue", testBool);
            return;
        }
        
        assertCollectionEquals(msg + ".add.resultantcollection", testCollection, Arrays.asList(ArrayUtils.add(presetValues, toAdd)));
        assertTrue(".add.returnvalue", testBool);
    }
    
    public void testAdd_testcase(int valueBeingAddedCount, int uniqueValueCount, int duplicateValueCount, int nullCount)
    {
        List<String> vals = new ArrayList<String>();
        
        String[] colours = {"Red", "Yellow", "Green", "Purple", "Pink", "Aqua", "Fuschia", "Grey", "Black", "White",
                            "Marine", "Bubblegum", "Polkadot", "Peridot", "Sapphire", "Emerald", "Lime", "Royal Blue"};
        
        String[] animals = {"Dog", "Cat", "Mouse", "Horse", "Hedhog Cute", "Haggis", "Jackalope", "Pinemartine",
                            "Horned Martine", "Gorilla", "Monkey", "Bonobo", "Wolf", "Bear", "Goldilocks"};
        
        for(int i = 0; i < valueBeingAddedCount; i++)
            vals.add("Blue");
        
        for(int i = 0; i < uniqueValueCount; i++)
        {
            if(i >= colours.length) break;
            vals.add(colours[i]);
        }
        
        for(int i = 0; i < duplicateValueCount; i++)
        {
            if(i >= animals.length) break;
            vals.addAll(Arrays.asList(animals[i], animals[i], animals[i]));
        }
        
        for(int i = 0; i < nullCount; i++)
            vals.add(null);
        
        testAdd("Test case: " + valueBeingAddedCount + "-" + uniqueValueCount + "-" + duplicateValueCount + "-"
                + nullCount + "-val", "Blue", vals.toArray(new String[0]));
        
        vals.removeAll(Arrays.asList("Blue"));
        for(int i = 0; i < valueBeingAddedCount; i++)
            vals.add(0, null);
        
        testAdd("Test case: " + valueBeingAddedCount + "-" + uniqueValueCount + "-" + duplicateValueCount + "-"
                + nullCount + "-null", null, vals.toArray(new String[0]));
    }
    
    @Test
    public void testAdd()
    {
        /*
        Test case scheme: Cover every possible combination of the four ternary values (which can each be "no",
        "yes", and "yes, multiple"):
        preset values contains value
        preset values contains duplicate values
        preset valeus contains null
        preset values contains value being added.
        
        Can't say I'm not thorough.
        */
        
        int beingAdded = 0, uniqueValue = 0, duplicate = 0, nullcount = 0;
        
        TieredIncrementor inc = new TieredIncrementor(4, 0, 3);
        
        
        
        while(true)
        {
            testAdd_testcase(beingAdded, uniqueValue, duplicate, nullcount);
            
            if(nullcount == 0) nullcount = 1;
            else if(nullcount == 1) nullcount = 3;
            else if(nullcount == 3)
            {
                nullcount = 0;
                if(duplicate == 0) duplicate = 1;
                else if(duplicate == 1) duplicate = 3;
                else if(duplicate == 3)
                {
                    duplicate = 0;
                    if(uniqueValue == 0) uniqueValue = 1;
                    else if(uniqueValue == 1) uniqueValue = 3;
                    else if(uniqueValue == 3)
                    {
                        uniqueValue = 0;
                        if(beingAdded == 0) beingAdded = 1;
                        else if(beingAdded == 1) beingAdded = 3;
                        else if(beingAdded == 3) break;
                    }
                }
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="addAll">
    public void testAddAll(String msg, final Collection<String> toAdd, String[] presetValues)
    {
        // Don't run tests with null values already in when the collection can't contain null
        if(ArrayUtils.contains(presetValues, null) && !canContainNull())
            return;
        
        // Don't run tests with duplicate values already in when the collection can't contain duplicate values.
        if(!canContainDuplicates())
            for(int i = 0; i < presetValues.length; i++)
                for(int j = 0; j < presetValues.length; j++)
                    if(i != j)
                        if((presetValues[i] == null && presetValues[j] == null)
                           || (presetValues[i] != null && presetValues[j] != null && presetValues[i].equals(presetValues[j])))
                            return;
        
        final Collection<String> testCollection = getStringCollection(presetValues);
        assertCollectionEquals(msg + ".presetvalues", testCollection, Arrays.asList(presetValues));
        
        Collection<String> expectedResultantValues;
        
        if(canContainDuplicates())
            expectedResultantValues = new ArrayList<String>(Arrays.asList(presetValues));
        else
            expectedResultantValues = new LinkedHashSet<String>(Arrays.asList(presetValues));
            // Maintains order but doesn't allow duplicates.
        
        if(isMutable())
            if(canContainNull())
                expectedResultantValues.addAll(toAdd);
            else
                for(String i : toAdd)
                    if(i != null)
                        expectedResultantValues.add(i);
        
        if(!isMutable())
        {
            boolean result;
            
            try
            { result = testCollection.addAll(toAdd); }
            catch(UnsupportedOperationException e)
            { result = false; }
            
            assertCollectionEquals(msg + ".immutable.resultantcollection", testCollection, Arrays.asList(presetValues));
            assertFalse(msg + ".immutable.returnvalue", result);
            return;
        }
        
        if(toAdd.contains(null) && !canContainNull())
        {
            new ThrowableAssertion(msg + ".addnull.exception", IllegalArgumentException.class, NullPointerException.class)
            {
                @Override
                public void code() throws Throwable
                { testCollection.addAll(toAdd); }
            };
            
            assertCollectionEquals(msg + ".addnull.resultantcollection", testCollection, Arrays.asList(presetValues));
            return;
        }
        
        boolean testBool = testCollection.addAll(toAdd);
        
        assertCollectionEquals(msg + ".addall.resultantcollection", testCollection, expectedResultantValues);
        
        if(expectedResultantValues.size() == presetValues.length)
            assertFalse(".addall.returnvalue", testBool);
        else
            assertTrue(".addall.returnvalue", testBool);
        
        //assertCollectionEquals(msg + ".addall.resultantcollection", )
    }
    
    public void testAddAll_testcase(int addXUniqueValues,
                                    int addXSetsOfDuplicates,
                                    int addXNulls,
                                    int presetXUniqueValues,
                                    int presetXSetsOfDuplicates,
                                    int presetXNulls,
                                    int presetXUniqueValuesBeingAdded,
                                    int presetXSetsOfDuplicatesBeingAdded)
    {
        int presetValuesCount = 0;
        
        presetValuesCount = presetXUniqueValues + (presetXSetsOfDuplicates * 3) + presetXNulls;
        
        presetValuesCount = presetValuesCount + ComparableMethods.getSmallestOfPrimitives(addXUniqueValues, presetXUniqueValuesBeingAdded);
        presetValuesCount = presetValuesCount + (ComparableMethods.getSmallestOfPrimitives(addXSetsOfDuplicates, presetXSetsOfDuplicatesBeingAdded) * 3);
        
        String[] colours = {"Red", "Yellow", "Green", "Purple", "Pink", "Aqua", "Fuschia", "Grey", "Black", "White",
                            "Marine", "Bubblegum", "Polkadot", "Peridot", "Sapphire", "Emerald", "Lime", "Royal Blue"};
        
        String[] animals = {"Dog", "Cat", "Mouse", "Horse", "Hedhog Cute", "Haggis", "Jackalope", "Pinemartine",
                            "Horned Martine", "Gorilla", "Monkey", "Bonobo", "Wolf", "Bear", "Goldilocks"};
        
        String[] oldnations = {"Venice", "Morea", "Achaea", "Nicaea", "Thuringia", "Saxony", "Anglia", "Picardy",
                               "Frisia", "Zeta", "Bavaria", "Kievan Rus", "Crimea", "Trieste", "Tirol", "Naxos"};
        
        String[] languages = {"Java", "Scala", "C#", "C++", "Obj-C", "J++", "J#", "Swift", "Ruby", "Groovy", "F#", "C",
                              "B", "D", "Assembly", "Lua", "Python", "Delphi", "Shakespeare", "Brainfuck", "Lolcode"};
        
        List<String> valuesBeingAdded = new ArrayList<String>();
        
        String[] presetUniqueValuesBeingAdded = new String[ComparableMethods.getSmallestOfPrimitives(presetXUniqueValues, addXUniqueValues, oldnations.length)];
        String[] presetDuplicatesBeingAdded = new String[ComparableMethods.getSmallestOfPrimitives(presetXSetsOfDuplicatesBeingAdded, addXSetsOfDuplicates, languages.length) * 3];
        String[] presetUniqueValues = new String[ComparableMethods.getSmallestOfPrimitives(presetXUniqueValues, colours.length)];
        String[] presetDuplicates = new String[ComparableMethods.getSmallestOfPrimitives(presetXSetsOfDuplicates, animals.length) * 3];
        String[] presetNulls = new String[presetXNulls];
        
        Collection<String> toBeAdded = new ArrayList<String>();
        
        for(int i = 0; i < presetXUniqueValues && i < colours.length; i++)
            presetUniqueValues[i] = colours[i];
        
        for(int i = 0; i < presetXSetsOfDuplicates && i < animals.length; i++)
        {
            presetDuplicates[i*3] = animals[i];
            presetDuplicates[i*3+1] = animals[i];
            presetDuplicates[i*3+2] = animals[i];
        }
        
        for(int i = 0; i < presetXNulls; i++)
            presetNulls[i] = null;
        
        for(int i = 0; i < addXUniqueValues && i < oldnations.length; i++)
        {
            toBeAdded.add(oldnations[i]);
            
            if(i < presetXUniqueValuesBeingAdded)
                presetUniqueValuesBeingAdded[i] = oldnations[i];
        }
        
        for(int i = 0; i < addXSetsOfDuplicates && i < languages.length; i++)
        {
            toBeAdded.add(languages[i]);
            toBeAdded.add(languages[i]);
            toBeAdded.add(languages[i]);
            
            if(i < presetXSetsOfDuplicatesBeingAdded)
            {
                presetDuplicatesBeingAdded[i*3] = languages[i];
                presetDuplicatesBeingAdded[i*3+1] = languages[i];
                presetDuplicatesBeingAdded[i*3+2] = languages[i];
            }
        }
        
        for(int i = 0; i < addXNulls; i++)
            toBeAdded.add(null);
        
        String[] presetValues = ArrayUtils.addAll(presetUniqueValues, presetDuplicates);
        presetValues = ArrayUtils.addAll(presetValues, presetNulls);
        presetValues = ArrayUtils.addAll(presetValues, presetUniqueValuesBeingAdded);
        presetValues = ArrayUtils.addAll(presetValues, presetDuplicatesBeingAdded);
        
        testAddAll(addXUniqueValues + "-" + addXSetsOfDuplicates + "-" + addXNulls + "-" + presetXUniqueValues + "-"
                   + presetXSetsOfDuplicates + "-" + presetXNulls + "-" + presetXUniqueValuesBeingAdded + "-"
                   + presetXSetsOfDuplicatesBeingAdded,
                   toBeAdded, presetValues);
    }
    
    @Test
    public void testAddAll()
    {
        
    }
    //</editor-fold>
    
    @Test
    public void testClear()
    {}
    
    @Test
    public void testContains()
    {}
    
    @Test
    public void testEquals()
    {}
    
    @Test
    public void testIsEmpty()
    {}
    
    @Test
    public void testIterator()
    {}
    
    @Test
    public void testRemove()
    {}
    
    @Test
    public void testRetain()
    {}
    
    @Test
    public void testSize()
    {}
    
    @Test
    public void testToArray()
    {}
}