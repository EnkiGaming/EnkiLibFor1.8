package com.enkigaming.lib.collections;

// For testing the test code

import java.util.ArrayList;
import java.util.Collection;
import scala.actors.threadpool.Arrays;


public class ArrayListTest extends CollectionTest
{

    @Override
    public Collection<String> getStringCollection()
    { return new ArrayList<String>(); }
    
    @Override
    public Collection<String> getStringCollection(String... presetValues)
    { return new ArrayList<String>(Arrays.asList(presetValues)); }

    @Override
    public boolean canContainDuplicates()
    { return true; }

    @Override
    public boolean canContainNull()
    { return true; }

    @Override
    public boolean isMutable()
    { return true; }
}