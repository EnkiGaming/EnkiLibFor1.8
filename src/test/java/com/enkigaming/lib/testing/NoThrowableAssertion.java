package com.enkigaming.lib.testing;

import static org.junit.Assert.fail;

public abstract class NoThrowableAssertion
{
    public NoThrowableAssertion(String message, Class<? extends Throwable> throwableType)
    {
        try
        { code(); }
        catch(Throwable e)
        {
            if(throwableType.isInstance(e))
                fail(message + ": Specified throwable (" + throwableType.getSimpleName() + ") thrown.");
        }
    }

    public NoThrowableAssertion(Class<? extends Throwable> throwableType)
    { this("", throwableType); }
    
    // Asserts that one of any of the passed exceptions are thrown.
    public NoThrowableAssertion(String message, Class<? extends Throwable>... throwableTypes)
    {
        String throwablesListed = "";
        
        for(Class<Exception> i : (Class<Exception>[])throwableTypes)
            throwablesListed += i.getSimpleName() + ", ";
        
        throwablesListed = throwablesListed.substring(0, throwablesListed.length() - ", ".length());
        
        try
        { code(); }
        catch(Throwable e)
        {
            if(throwableIsSpecified(e, throwableTypes))
                fail(message + ": One of specified throwables (" + throwablesListed + ") thrown.");
        }
    }
    
    public NoThrowableAssertion(Class<? extends Throwable>... throwableTypes)
    { this("", throwableTypes); }
    
    private boolean throwableIsSpecified(Throwable e, Class<? extends Throwable>[] es)
    {
        for(Class<Throwable> i : (Class<Throwable>[])es)
            if(i.isInstance(e))
                return true;
        
        return false;
    }

    public abstract void code() throws Throwable;
}