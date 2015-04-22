package com.enkigaming.lib.misc;

import com.enkigaming.lib.encapsulatedfunctions.Operation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MutableWrapper<T> extends Wrapper<T>
{
    public MutableWrapper()
    { super(); }
    
    public MutableWrapper(T value)
    { super(value); }
    
    public T set(T newValue)
    {
        synchronized(valueLock)
        {
            T temp = value;
            value = newValue;
            return temp;
        }
    }
    
    /**
     * Performs the passed operation using the wrapper's threadsafe/synchronisation lock.
     * @param operation 
     */
    public void performOperationOn(Operation operation)
    {
        synchronized(valueLock)
        { operation.perform(); }
    }
    
    public static <T> void setAll(T newValue, MutableWrapper<T>... wrappersToSet)
    {
        for(MutableWrapper<T> i : wrappersToSet)
            i.set(newValue);
    }
    
    /**
     * Methods that should be implemented as extension methods, if only Java supported them.
     */
    public static class ExtensionMethods
    {
        // There are a pile of add and subtract methods for every possible number, which are all otherwise identical,
        // because Java, despite having the wisdom to include a Number interface, doesn't have the wisdom to allow
        // any numerical operators to be used with said interface, rendering it almost completely useless. Discussion
        // with java-buffs in IRC results in them treating you like an idiot for expecting calls to operators on the
        // base interface of all numbers to route down to the specific object's implementation of it, despite numerical
        // operators being the *only* example where this isn't the case. There are times I hate Java so much. At least
        // C# doesn't pretend to have a useful numeric base interface. Although if it did, it could have fucking add
        // and subtract extension methods.
        
        //<editor-fold defaultstate="collapsed" desc="add(this MutableWrapper<Number>, Number)">
        public static byte add(MutableWrapper<Byte> toModify, byte amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                byte oldValue = toModify.value;
                // See the below rant in the short overload of this method.
                toModify.value = (byte)(toModify.value + amountToAdd);
                return oldValue;
            }
        }
        
        public static short add(MutableWrapper<Short> toModify, short amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                short oldValue = toModify.value;
                
                // See this? You can't just add two shorts together, no. That would be too simple. Adding two shorts
                // together automatically returns an *INT* as the result, meaning that the result of adding to shorts
                // normally, like you would expect, can't itself be referred to or converted to a short.
                // Why? For the glory of satan, of course!
                
                toModify.value = (short)(toModify.value + amountToAdd);
                return oldValue;
            }
        }
        
        public static int add(MutableWrapper<Integer> toModify, int amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                int oldValue = toModify.value;
                toModify.value += amountToAdd;
                return oldValue;
            }
        }
        
        public static long add(MutableWrapper<Long> toModify, long amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                long oldValue = toModify.value;
                toModify.value += amountToAdd;
                return oldValue;
            }
        }
        
        public static float add(MutableWrapper<Float> toModify, float amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                float oldValue = toModify.value;
                toModify.value += amountToAdd;
                return oldValue;
            }
        }
        
        public static double add(MutableWrapper<Double> toModify, double amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                double oldValue = toModify.value;
                toModify.value += amountToAdd;
                return oldValue;
            }
        }
        
        public static BigInteger add(MutableWrapper<BigInteger> toModify, BigInteger amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                BigInteger oldValue = toModify.value;
                toModify.value = toModify.value.add(amountToAdd);
                return oldValue;
            }
        }
        
        public static BigDecimal add(MutableWrapper<BigDecimal> toModify, BigDecimal amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                BigDecimal oldValue = toModify.value;
                toModify.value = toModify.value.add(amountToAdd);
                return oldValue;
            }
        }
        
        public static AtomicInteger add(MutableWrapper<AtomicInteger> toModify, AtomicInteger amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                AtomicInteger oldValue = toModify.value;
                toModify.value.getAndAdd(amountToAdd.get());
                return oldValue;
            }
        }
        
        public static AtomicLong add(MutableWrapper<AtomicLong> toModify, AtomicLong amountToAdd)
        {
            synchronized(toModify.valueLock)
            {
                AtomicLong oldValue = toModify.value;
                toModify.value.getAndAdd(amountToAdd.get());
                return oldValue;
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="subtract(this MutableWrapper<Number>, Number)">
        public static byte subtract(MutableWrapper<Byte> toModify, byte amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                byte oldValue = toModify.value;
                // See the below rant in the short overload of this method.
                toModify.value = (byte)(toModify.value - amountToSubtract);
                return oldValue;
            }
        }
        
        public static short subtract(MutableWrapper<Short> toModify, short amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                short oldValue = toModify.value;
                
                // See this? You can't just add two shorts together, no. That would be too simple. Adding two shorts
                // together automatically returns an *INT* as the result, meaning that the result of adding to shorts
                // normally, like you would expect, can't itself be referred to or converted to a short.
                // Why? For the glory of satan, of course!
                
                toModify.value = (short)(toModify.value - amountToSubtract);
                return oldValue;
            }
        }
        
        public static int subtract(MutableWrapper<Integer> toModify, int amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                int oldValue = toModify.value;
                toModify.value += amountToSubtract;
                return oldValue;
            }
        }
        
        public static long subtract(MutableWrapper<Long> toModify, long amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                long oldValue = toModify.value;
                toModify.value -= amountToSubtract;
                return oldValue;
            }
        }
        
        public static float subtract(MutableWrapper<Float> toModify, float amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                float oldValue = toModify.value;
                toModify.value -= amountToSubtract;
                return oldValue;
            }
        }
        
        public static double subtract(MutableWrapper<Double> toModify, double amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                double oldValue = toModify.value;
                toModify.value -= amountToSubtract;
                return oldValue;
            }
        }
        
        public static BigInteger subtract(MutableWrapper<BigInteger> toModify, BigInteger amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                BigInteger oldValue = toModify.value;
                toModify.value = toModify.value.subtract(amountToSubtract);
                return oldValue;
            }
        }
        
        public static BigDecimal subtract(MutableWrapper<BigDecimal> toModify, BigDecimal amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                BigDecimal oldValue = toModify.value;
                toModify.value = toModify.value.subtract(amountToSubtract);
                return oldValue;
            }
        }
        
        public static AtomicInteger subtract(MutableWrapper<AtomicInteger> toModify, AtomicInteger amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                AtomicInteger oldValue = toModify.value;
                toModify.value.getAndAdd(-amountToSubtract.get());
                return oldValue;
            }
        }
        
        public static AtomicLong subtract(MutableWrapper<AtomicLong> toModify, AtomicLong amountToSubtract)
        {
            synchronized(toModify.valueLock)
            {
                AtomicLong oldValue = toModify.value;
                toModify.value.getAndAdd(-amountToSubtract.get());
                return oldValue;
            }
        }
        //</editor-fold>
    }
}