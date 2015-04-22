package com.enkigaming.lib.testing;

import static org.junit.Assert.fail;

public abstract class ThrowableAssertion
{
    public ThrowableAssertion(String message, Class<? extends Throwable> throwableType)
    {
        try
        { code(); }
        catch(Throwable e)
        {
            if(!throwableType.isInstance(e))
//                fail(message + ": Throwable other than specified throwable (" + throwableType.getSimpleName() + ") "
//                     + "thrown.");
                fail(message + ": Throwable other than specified one thrown. Expected " + throwableType.getSimpleName()
                     + ", caught " + e.getClass().getSimpleName());

            return;
        }

        fail(message + ": " + "Specified throwable (" + throwableType.getSimpleName() + ") not thrown.");
    }

    public ThrowableAssertion(Class<? extends Throwable> throwableType)
    { this("", throwableType); }
    
    // Asserts that one of any of the passed exceptions are thrown.
    public ThrowableAssertion(String message, Class<? extends Throwable>... throwableTypes)
    {
        String throwablesListed = "";
        
        for(Class<Exception> i : (Class<Exception>[])throwableTypes)
            throwablesListed += i.getSimpleName() + ", ";
        
        throwablesListed = throwablesListed.substring(0, throwablesListed.length() - ", ".length());
        
        try
        { code(); }
        catch(Throwable e)
        {
            if(!throwableIsSpecified(e, throwableTypes))
                fail(message + ": Throwable other than one of specified throwable (" + throwablesListed + ") thrown.");

            return;
        }

        fail(message + ": " + "No specified throwable (" + throwablesListed + ") thrown.");
    }
    
    public ThrowableAssertion(Class<? extends Throwable>... throwableTypes)
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