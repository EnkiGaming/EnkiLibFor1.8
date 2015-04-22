package com.enkigaming.lib.exceptions;

/**
 * Exception thrown when null is passed as an argument where a null argument is illegal.
 * 
 * Included because the NullArgumentException normally thrown isn't available as Forge doesn't have the Apache commons
 * library added as a referenced library.
 * @author hanii
 */
public class NullArgumentException extends IllegalArgumentException
{
    public NullArgumentException()                            { super();           }
    public NullArgumentException(String msg)                  { super(msg);        }
    public NullArgumentException(Throwable cause)             { super(cause);      }
    public NullArgumentException(String msg, Throwable cause) { super(msg, cause); }
}