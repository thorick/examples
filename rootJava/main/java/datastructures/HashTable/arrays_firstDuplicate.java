package _codefights;

import java.util.HashSet;
import java.util.Set;

enum USE_TABLE {
  JDK("JDK"),
  LINKED_LIST_OVERFLOW("LINKED_LIST_OVERFLOW"),
  ADJACENT_ELEMENT_OVERFLOW("ADJACENT_ELEMENT_OVERFLOW");

  String name;

  USE_TABLE(String name) {
    this.name = name;
  }

  static Hash getHash(USE_TABLE useHash) {
    switch (useHash) {

      case LINKED_LIST_OVERFLOW:
        return new arrays_firstDuplicate.MyHash_linkedListCollisionOverflow();
      case ADJACENT_ELEMENT_OVERFLOW:
        return new arrays_firstDuplicate.MyHash_adjacentElementOverflow();
      case JDK:
      default:
        return new arrays_firstDuplicate.WrappedJDKhash();
    }
  }
}

interface Hash {
  boolean contains(Integer i);
  void add(Integer i);
}

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 5/6/2018
 * Time: 5:36 PM
 */
public class arrays_firstDuplicate {

  static int findFirstDuplicate(int[] inputArray, USE_TABLE useHash) {

    Set<Integer> intSet = new HashSet<Integer>();
    Hash hash = USE_TABLE.getHash(useHash);
    
    for (int i=0; i< inputArray.length; i++) {
       Integer val = Integer.valueOf(inputArray[i]);
       if (hash.contains(val))  return val.intValue();

       hash.add(val);
    }
    throw new RuntimeException("Sorry, the input array contains no duplicates.");
  }

  static class WrappedJDKhash implements Hash {
    static Set<Integer> hash = new HashSet<Integer>();

    public boolean contains(Integer val) {
       return hash.contains(val);
    }

    public void add (Integer val) {
      hash.add(val);
    }
  }
  
  //
  // Linked List overflow is self maintaining, but chasing linked lists of Java Wrapper Objects is CPU EXPENSIVE
  //
  static class MyHash_linkedListCollisionOverflow implements Hash {
    static int RADIX = 1000;
    static Integer[] hashArray = new Integer[RADIX];
    static Overflow[] overFlowArray = new Overflow[RADIX];

    public boolean contains(Integer val) {
      int pos = val % RADIX;
      if (hashArray[pos] == null) {
        return false;
      }
      if (hashArray[pos].equals(val)) {
        return true;
      }
      Overflow overflow = overFlowArray[pos];
      if (overflow == null) {
        return false;
      }
      if (overflow.value.equals(val)) return true;

      overflow = overflow.next;
      while (overflow != null) {
        if (overflow.value.equals(val)) {
          return true;
        }
        overflow = overflow.next;
      }
      return false;
    }


    public void add(Integer val) {
      if (contains(val)) return;
      
      int pos = val % RADIX;
      if (hashArray[pos] == null) {
        hashArray[pos] = val;
        return;
      }

      Overflow overflow = overFlowArray[pos];
      if (overflow == null) {
        overflow = new Overflow(val);
        overFlowArray[pos] = overflow;
        return;
      }
      Overflow prevOverflow = null;
      while (overflow != null) {
        if (overflow.value.equals(val)) {
          return;
        }
        prevOverflow = overflow;
        overflow = overflow.next;
      }
      Overflow thisOverflow = new Overflow(val);
      prevOverflow.next = thisOverflow;
    }
  }

  static class Overflow {
    Overflow next;
    final Integer value;
    Overflow(Integer val) {
         value = val;
    }
  }

  //
  // can be way faster than linked list overflow chasing but very sensitive to available space.
  // better for very fast access to a sparse set relative to the size of the initial space.
  // This impl may fail due to lack of space !
  // If you want it not to fail, then dynamically extend the size of the array when you bump the current end.
  // This also means a rehash..  you have to pay somewhere to have the lookup performance of this method if you push it
  // beyond it's initial capacity.
  //
  static class MyHash_adjacentElementOverflow implements Hash {
    int SIZE = 10000;
    int RADIX = SIZE/2;  // have to leave room for the last element to overflow to a neighbor
    boolean seenZero = false;   // the one hole in this scheme
    int[] hashArray = new int[SIZE];

    public boolean contains(Integer val) {
      int intVal = val.intValue();
      if (intVal == 0) {
        return seenZero;
      }
      int pos = val % RADIX;

      if (hashArray[pos] == intVal) {
        return true;
      }
      ++pos;
      while (pos < SIZE && hashArray[pos] != 0) {
        if (hashArray[pos] == intVal) {
          return true;
        }
        ++pos;
      }
      return false;
    }


    public void add(Integer val) {
      int intVal = val.intValue();
      if (intVal == 0) {
        seenZero = true;
        return;
      }
      if (contains(val)) return;
      int pos = val % RADIX;

      // the nearest null element and place our entry there
      while (hashArray[pos] != 0) {
         if (pos >= SIZE-1) {
           throw new RuntimeException("Sorry, can't add value "+val+" hashArray ran out of size for SIZE="+SIZE);
         }
         ++pos;
      }
      hashArray[pos] =intVal;
    }
  }


  public static void main (String[] args) {
    int[] testArray = new int[] {1001, 1005, 1007, 7, 1, 10, 5, 2, 6, 7, 11};

    //int firstDup = findFirstDuplicate(testArray, USE_TABLE.JDK);
    //int firstDup = findFirstDuplicate(testArray, USE_TABLE.LINKED_LIST_OVERFLOW);
    int firstDup = findFirstDuplicate(testArray, USE_TABLE.ADJACENT_ELEMENT_OVERFLOW);


    System.err.println("firstDup = "+firstDup);

  }
}
