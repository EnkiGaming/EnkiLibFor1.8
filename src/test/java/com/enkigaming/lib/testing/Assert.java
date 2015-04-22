package com.enkigaming.lib.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.fail;

public class Assert
{
    public static <T> void assertCollectionEquals(Collection<T> howItBe, Collection<T> howItShouldBe)
    { assertCollectionEquals("", howItBe, howItShouldBe); }
    
    public static <T> void assertCollectionEquals(String message, Collection<T> howItBe, Collection<T> howItShouldBe)
    {
        if(howItBe == null)
            if(howItShouldBe == null)
                return;
            else
                fail(message + ": " + "Collection was null, should not have been.");
        
        if(howItShouldBe == null)
            fail(message + ": " + "Collection should have been null, was not.");
        
        if(howItBe.size() != howItShouldBe.size())
        {
            String sizeDescriptor = howItBe.size() > howItShouldBe.size() ? "bigger" : "smaller";
            
            fail(message + ": " + "Collection was " + sizeDescriptor + " than it should have been." + "\n\n"
               + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
               + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
        }
        
        Collection<T> howItBeCopy = new ArrayList<T>(howItBe);
        
        for(T i : howItShouldBe)
        {
            if(!howItBeCopy.remove(i))
                fail(message + ": " + "Collection didn't contain members it should have." + "\n\n"
                   + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
                   + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
        }
    }
    
    public static <T> void assertListEquals(List<T> howItBe, List<T> howItShouldBe)
    { assertListEquals("", howItBe, howItShouldBe); }
    
    public static <T> void assertListEquals(String message, List<T> howItBe, List<T> howItShouldBe)
    {
        if(howItBe == null)
            if(howItShouldBe == null)
                return;
            else
                fail(message + ": " + "List was null, should not have been.");
        
        if(howItShouldBe == null)
            fail(message + ": " + "List should have been null, was not.");
        
        if(howItBe.size() != howItShouldBe.size())
        {
            String sizeDescriptor = howItBe.size() > howItShouldBe.size() ? "bigger" : "smaller";
            
            fail(message + ": " + "Collection was " + sizeDescriptor + " than it should have been." + "\n\n"
                 + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
                 + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
        }
        
        for(int i = 0; i < howItBe.size(); i++)
        {
            T iShould = howItShouldBe.get(i), iIs = howItBe.get(i);
            
            if(iIs == null)
                if(iShould == null)
                    continue;
                else
                    fail(message + ": " + "Member [" + i + "] of the list was null, should not have been. "
                         + "Should have been \"" + iShould.toString() + "\""
                         + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
                         + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
            
            if(iShould == null)
                fail(message + ": " + "Member [" + i + "] of the list should have been null, it wasn't. "
                     + "It was \"" + iIs.toString() + "\""
                     + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
                     + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
            
            if(!iShould.equals(iIs))
                fail(message + ": " + "Member [" + i + "] of the list was not as expected. "
                     + "Should have been \"" + iShould.toString() + "\""
                     + "Contents of collection were: " + "\n" + howItBe.toString() + "\n\n"
                     + "Contents of collection should have been: " + "\n" + howItShouldBe.toString());
        }
    }
    
    public static <T> void assertCollectionEmpty(Collection<T> howItBe)
    { assertCollectionEmpty("", howItBe); }
    
    public static <T> void assertCollectionEmpty(String message, Collection<T> howItBe)
    {
        if(howItBe == null)
            fail(message + ": " + "Collection was null, should not have been.");
        
        if(!howItBe.isEmpty())
            fail(message + ": " + "Collection was not empty. Contained: " + "\n"
               + howItBe.toString());
    }
    
    public static void assertAllTrue(boolean... howItBe)
    { assertAllTrue("", howItBe); }
    
    public static void assertAllTrue(String message, boolean... howItBe)
    {
        if(howItBe == null)
            fail(message + ": " + "Array was null, should not have been.");
        
        for(int i = 0; i < howItBe.length; i++)
            if(!howItBe[i])
                fail(message + ": " + "Not all values were true; value [" + i + "] was false.");
    }
    
    public static void assertAllFalse(boolean... howItBe)
    { assertAllFalse("", howItBe); }
    
    public static void assertAllFalse(String message, boolean... howItBe)
    {
        if(howItBe == null)
            fail(message + ": " + "Array was null, should not have been.");
        
        for(int i = 0; i < howItBe.length; i++)
            if(howItBe[i])
                fail(message + ": " + "Not all values were false; value [" + i + "] was true.");
    }
    
    
}