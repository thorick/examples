package algorithms.sort.ctci_ch11;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 6/26/13
 * Time: 9:21 PM
 */
/*
    You are given 2 sorted arrays A and B, where A has a large enough buffer
    at the end to hold B.
    Write a method to merge B into A in sorted order.

 */
public class p11dot1_mergeSortedArrays {

  /*
    Since the input arrays are said to be sorted, we'll assume that any
    null entries in the array are at the end of each (if nulls would have been
    sorted to the beginning of the array then we'd modify our method to suit).

    The strategy is to first scan each array to get the full count of contiguous
    non-null entries.
    Since input A has enough room to hold B,
    we will start at the back of each array A and B comparing each 'last element' has
    the higher value.
    The higher value will be copied to the growing right end of A.
    we are done when B is exhausted.
    A now contains the merged sorted values of A and B.

   */
  public static void merge00(Comparable[] a, Comparable[] b) {
    int lastA;      // last position of A
    int lastB;      // last position of B
    int nextAddToA; // next place in A to move next largest value from lastA or lastB

    if (a == null) throw new RuntimeException("NULL array a !");
    if (b == null) return;    // it's a only

    lastA = findLast(a);
    lastB = findLast(b);
    if (lastB < 0)  return;  // there is nothing in B, so it's A

    p("input a = '"+printArray(a)+"'");
    p("input b = '"+printArray(b)+"'");

    nextAddToA = lastA + lastB + 1;

    while (lastB >= 0) {
      if (a[lastA].compareTo(b[lastB]) > 0) {
        a[nextAddToA] = a[lastA];
        lastA--;
      } else {
        a[nextAddToA] = b[lastB];
        lastB--;
      }
      nextAddToA--;
      p("partially merged a = '"+printArray(a));
    }
    p("merged array = '"+printArray(a)+"'");

  }

  //
  // find the index of the last non-null entry in array.
  //    -1 means that there are no non-null entries
  //
  private static int findLast(Object[] a) {
    if (a[0] == null) {
      return -1;
    } else if (a[a.length - 1] != null) return a.length - 1;
    else {
      for (int i = 0; i < a.length; i++) {
        if (a[i] != null) {
          return i;

        }
      }
    }
    throw new RuntimeException("could not determine last non-null entry for array");
  }

  private static String printArray(Object[] a) {
    if (a == null) return "NULL ARRAY";
    if (a[0] == null) return "EMPTY ARRAY";

    StringBuilder sb = new StringBuilder();
    for (int i=0; i < a.length ; i++) {
      sb.append(a[i].toString()).append(" ");
    }
    return sb.toString();
  }

  public class SortObject implements Comparable {
    private final int value;

    SortObject(int i) {
      this.value = i;
    }

    public int getValue() {
      return value;
    }

    public int compareTo(Object o) {
      if (! (o instanceof SortObject))  throw new RuntimeException("expected to compare input type 'SortObject"+
      "instead we got an instance of class '"+o.getClass().getName());

      if (getValue() < ((SortObject)o).getValue()) return -1;
      if (getValue() == ((SortObject)o).getValue()) return 0;
      return 1;
    }

    @Override
    public String toString() {
      return Integer.toString(value);
    }
  }

  private static void p(String s) {
    System.err.println("p11dot1_mergeSortedArrays: "+s);
  }
}
