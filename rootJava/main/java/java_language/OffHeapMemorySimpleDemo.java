package unsafe;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 11/6/16
 * Time: 3:32 PM
 *
 * https://dzone.com/articles/understanding-sunmiscunsafe
 *
 * can use this to create a region in off heap memory for an array of
 * ints (4 Bytes each) and MORE than INTEGER.MAX_VALUE entries.
 *  max integer index is  2**31 - 1
 *    so default max positive ~ billion
 *
 *   2**10  ~ 1,000
 *   2**20  ~ 1,000,000
 *   2*30   ~ 1,000,000,000  billion
 *
 *    so we can allocate enough for a billion 10**9 indexes
 *    total memory  4 * 10**9
 *    4,000,000,000  bytes
 *                       B   M   T
 *    You need at least (2 147 483 647 + 1) * 4 byte = 8192 MB
 *       of native memory for running the code
 *
 *   Be aware that directly allocated memory is always native memory and
 *   therefore not garbage collected.
 *   You therefore have to free memory explicitly as demonstrated in
 *   the above example by a call to Unsafe#freeMemory(long).
 *   Otherwise you reserved some memory that can never be used for
 *   something else as long as the JVM instance is running what is a memory leak
 *   and a common problem in non-garbage collected languages.
 *
 *   Alternatively, you can also directly reallocate memory at a certain address
 *   by calling Unsafe#reallocateMemory(long, long) where the second argument describes
 *   the new amount of bytes to be reserved by the JVM at the given address.
 *
 *   Also, note that the directly allocated memory is not initialized with a
 *   certain value. In general, you will find garbage from old usages of this
 *   memory area such that you have to explicitly initialize your allocated memory
 *   if you require a default value. This is something that is normally done for
 *   you when you let the Java run time allocate the memory for you.
 *   In the above example, the entire area is overriden with zeros with help of
 *   the Unsafe#setMemory method.
 *
 */
public class OffHeapMemorySimpleDemo {

    private final static long INT_SIZE_IN_BYTES = 4;

    private final long startIndex;
    private Unsafe unsafe = getUnsafeInstance();

    public OffHeapMemorySimpleDemo(long size) {
        // allocate memory of size = size, we get back the startIndex into memory block
        startIndex = unsafe.allocateMemory(size * INT_SIZE_IN_BYTES);

        // starting at startIndex and going for the size of the entire region that we claimed
        //     zero out the memory that we have claimed
        unsafe.setMemory(startIndex, size * INT_SIZE_IN_BYTES, (byte) 0);
    }

    /**
     * note that the getter and setter for our memory mapped array use
     * index offsets into the native memory that we grabbed and got the
     * starting memory address for.
     *
     * there is no explicit java array in use here and that was the entire point of this
     * we are creating and using an implicit array based on using
     * index values into the memory that we grabbed for our non-heap usage.
     *
     * @param index
     */
    public int getValue(long index) {
        return unsafe.getInt(index(index));
    }

    public void setValue(long index, int value) {
        unsafe.putInt(index(index), value);
    }

    /**
     * compute the memory index in our implicit array by pointing to memory location of
     * element 'offset'
     *
     *    startIndex +  (array index value * INT_SIZE_IN_BYTES)
     * @param offset
     * @return
     */
    private long index(long offset) {
        return startIndex + offset * INT_SIZE_IN_BYTES;
    }

    public void destroy() {
        unsafe.freeMemory(startIndex);
    }


    private static void p(String s) {
        System.err.println(s);
    }

    private static Constructor<Unsafe> getUnsafeConstructor() {
        Constructor<Unsafe> unsafeConstructor = null;
        Unsafe unsafe = null;
        try {
            unsafeConstructor =
                    sun.misc.Unsafe.class.getDeclaredConstructor();

            // must do this top enable us to access this constructor since the constructor of Unsafe is private.
            unsafeConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            p(e.getMessage());
        }
        return unsafeConstructor;
    }


    private static Unsafe getUnsafeInstance() {
        Constructor<Unsafe> unsafeConstructor = getUnsafeConstructor();


        Unsafe unsafe = null;
        try {
            unsafe = unsafeConstructor.newInstance();
        } catch (Exception e) {
            p(e.getMessage());
        }
        return unsafe;
    }

    public static void main(String[] args) {

        long maximum = Integer.MAX_VALUE + 1L;
        OffHeapMemorySimpleDemo directIntArray = new OffHeapMemorySimpleDemo(maximum);
        directIntArray.setValue(0L, 10);
        directIntArray.setValue(maximum, 20);
        p("set values        [0] = 10,  ["+maximum+"] = 20");
        long longZero = directIntArray.getValue(0);
        long longMax = directIntArray.getValue(maximum);
        p("retrieved values  [0] = "+longZero+",  ["+maximum+"] = "+longMax);
        p("   now free up the off heap memory ");

        directIntArray.destroy();
        p("   DONE.");

    }
}
