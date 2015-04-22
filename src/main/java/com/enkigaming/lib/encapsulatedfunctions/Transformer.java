package com.enkigaming.lib.encapsulatedfunctions;

/**
 * A method for generically obtaining a value from an object, wrapped in an object.
 * Work-around for Java not supporting Lambdas/Delegates, emulation thereof.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @param <T> The type of the object to get a value from.
 * @param <U> The type of the object being returned by the operation.
 */
public interface Transformer<T, U>
{
    /**
     * Gets the object within the passed parent object, using the specified functionality.
     * @param parent The object to get a value from.
     * @return The objects being returned from the parent object by the operation.
     */
    U get(T parent);
}