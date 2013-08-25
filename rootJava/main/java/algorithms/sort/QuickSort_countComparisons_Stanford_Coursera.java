package algorithms.sort;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/18/13
 * Time:The file contains all of the integers between 1 and 10,000
 * (inclusive, with no repeats) in unsorted order.
 * The integer in the ith row of the file gives you the ith entry of an input array.
 * <p/>
 * Lecture algorithm (modified to specify pivot):
 * <p/>
 * quicksort(A, length)
 * if n==1  return;
 * p := choose pivot(A, length)
 * partition around p     // p is now fixed
 * recursively sort L     // p excluded
 * recursively sort R     // p excluded
 * <p/>
 * <p/>
 * partition(A, l, r, p)
 * //- p := A[l];            // p = pivot choice don't do it this way
 * swap A[l] and A[p];
 * - pValue := A[l];
 * - i := l + 1;
 * - for j = (l + 1) to r
 * - if A[j] < pValue   // if A[j] > pValue, do nothing
 * - swap A[j] with A[i]
 * - i := i + 1;
 * <p/>
 * swap A[l] and A[i-1]    // place the pivot into its final place
 * <p/>
 * <p/>
 * <p/>
 * Your task is to compute the total number of comparisons used to
 * sort the given input file by QuickSort. As you know, the number of
 * comparisons depends on which elements are chosen as pivots,
 * so we'll ask you to explore three different pivoting rules.
 * <p/>
 * You should not count comparisons one-by-one. Rather, when there is
 * a recursive call on a subarray of length m, you should simply add m−1
 * to your running total of comparisons. (This is because the pivot element
 * is compared to each of the other m−1 elements in the subarray in
 * this recursive call.)
 * <p/>
 * WARNING: The Partition subroutine can be implemented in several
 * different ways, and different implementations can give you differing
 * numbers of comparisons. For this problem, you should implement the
 * Partition subroutine exactly as it is described in the video lectures
 * (otherwise you might get the wrong answer).
 * <p/>
 * DIRECTIONS FOR THIS PROBLEM:
 * <p/>
 * For the first part of the programming assignment, you should always
 * use the first element of the array as the pivot element.
 * <p/>
 * <p/>
 * <p/>
 * Question 2
 * GENERAL DIRECTIONS AND HOW TO GIVE US YOUR ANSWER:
 * <p/>
 * See the first question.
 * <p/>
 * DIRECTIONS FOR THIS PROBLEM:
 * <p/>
 * Compute the number of comparisons (as in Problem 1),
 * always using the final element of the given array as the pivot element.
 * Again, be sure to implement the Partition subroutine exactly as it is
 * described in the video lectures.
 * Recall from the lectures that, just before the main Partition subroutine,
 * you should exchange the pivot element (i.e., the last element) with the first element.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Question 3
 * GENERAL DIRECTIONS AND HOW TO GIVE US YOUR ANSWER:
 * <p/>
 * See the first question.
 * <p/>
 * DIRECTIONS FOR THIS PROBLEM:
 * <p/>
 * Compute the number of comparisons (as in Problem 1), using the "median-of-three"
 * pivot rule. [The primary motivation behind this rule is to do a little bit
 * of extra work to get much better performance on input arrays that are nearly
 * sorted or reverse sorted.] In more detail, you should choose the pivot as
 * follows.
 * <p/>
 * <p/>
 * Consider the first, middle, and final elements of the given array.
 * (If the array has odd length it should be clear what the "middle" element is;
 * for an array with even length 2k, use the kth element as the "middle" element.
 * So for the array 4 5 6 7, the "middle" element is the second one ---- 5 and not 6!)
 * Identify which of these three elements is the median (i.e., the one whose value
 * is in between the other two), and use this as your pivot. As discussed in the
 * first and second parts of this programming assignment, be sure to implement
 * Partition exactly as described in the video lectures (including exchanging
 * the pivot element with the first element just before the main Partition subroutine).
 * <p/>
 * EXAMPLE: For the input array 8 2 4 5 7 1
 * <p/>
 * you would consider the first (8), middle (4), and last (1) elements;
 * since 4 is the median of the set {1,4,8}, you would use 4 as your pivot element.
 * <p/>
 * SUBTLE POINT: A careful analysis would keep track of the comparisons made
 * in identifying the median of the three candidate elements.
 * <p/>
 * You should NOT do this.
 * <p/>
 * That is, as in the previous two problems,
 * you should simply add m−1 to your running total of comparisons every
 * time you recurse on a subarray with length m.
 */
public class QuickSort_countComparisons_Stanford_Coursera {

  int recurseCount = 0;
  int hardStop = -1;

  /**
   * Top Level entry method to perform quicksort and keep track of the
   * number of array element comparisons performed.
   *
   * @param a           input array
   * @param pivotMethod pivotSelection type 1, 2, 3  (see problem instructions above)
   * @return number of comparisons between array elements performed
   */
  public long quickSort(int[] a, int pivotMethod) {
    String m = "quickSort entry: ";
    recurseCount = 0;
    //p(m + " a.length-1=" + (a.length - 1));
    return quickSort(a, 0, a.length - 1, pivotMethod);
  }

  /**
   * Recursive method to do quicksort.
   * Receives params delimiting the sections of the input array
   * that this invocation handles.
   *
   * @param a
   * @param left
   * @param right
   * @param pivotMethod
   * @return count of comparisons done in the partition method
   */
  private long quickSort(int[] a, int left, int right, int pivotMethod) {
    String m = "quickSort: ";

    long thisCount = 0;

    recurseCount++;
    if (hardStop > 0 && recurseCount >= hardStop) {
      p(m + " hardstop after " + recurseCount + " iterations");
      System.exit(0);
    }

    if (left == right) {
      //p(m + "l = r = " + left + " return count=0");
      return 0;
    }

    if (right == (left + 1)) {
      //p("\n\n" + m + "note:  array partition is of size=2.\n");
    }

    //
    // for the purposes of the homework assignment
    // we do NOT include the number of comparisons required
    // to select a pivotIndex.
    // If we were doing a strict performance analysis, we certainly would
    // include this number in the results !
    //
    int pivotIndex = choosePivot(a, left, right, pivotMethod);

    // comparisons are all done in the partition function
    // a count of the left and right blocks is the total
    // number of comparisons done during this recursion step
    int newPivotIndex = partition(a, left, right, pivotIndex);
    //p(m + " back from partition l=" + left + ", r=" + right + ", p=" + pivotIndex + " count=" + thisCount);

    thisCount = right - left;   // this is the number of comparisons done in partition above.
    long totalCount = thisCount;
    int rIndex = newPivotIndex - 1;
    long lCount = 0;
    if (rIndex >= left) {
      //lCount = rIndex - left;
      // thisCount += lCount;
      //p("\n"+m + " start QS LEFT l=" + left + ", r=" + rIndex+" lCount="+lCount+", thisCount="+thisCount+"  "+pInts(a, left, rIndex));

      lCount = quickSort(a, left, rIndex, pivotMethod);
      totalCount += lCount;
      //p(m+"\n after QS LEFT lCount="+lCount+" thisCount="+thisCount+", totalCount="+totalCount);
    } else {
      //p(m+" skip QS LEFT");
    }

    int lIndex = newPivotIndex + 1;
    long rCount = 0;
    if (lIndex <= right) {
      //rCount = right - lIndex;
      //   thisCount += rCount;
      //p("\n"+m + " start QS RIGHT l=" + lIndex + ", r=" + right+" rCount="+rCount+", thisCount="+thisCount+"  "+pInts(a, lIndex, right));

      rCount = quickSort(a, lIndex, right, pivotMethod);
      totalCount += rCount;
      //p(m+"\n after QS RIGHT rCount="+rCount+" thisCount="+thisCount+", totalCount="+totalCount);
    } else {
      //p(m+" skip QS RIGHT");
    }
    //p("\n"+m+" END of QS returning totalCount="+totalCount);
    return totalCount;
  }

  private int choosePivot(int[] a, int left, int right, int pivotMethod) {
    String m = "choosePivot: ";
    //p(m + " l=" + left + ", r=" + right + ", pivotMethod=" + pivotMethod);

    switch (pivotMethod) {
      // always the first element
      case 1:
        return left;

      // always the last element
      case 2:
        return right;

      // um..  NYI
      case 3:
        // even/odd is critical to get matching results with homework answer
        int len = right - left + 1;
        boolean odd = (len % 2 != 0);
        int middleIndexOffset = (len / 2) - 1; // by definition middle element of an array of length 2k is the kth element
        if (odd) {
          middleIndexOffset = ((len + 1) / 2) - 1;    // 0 1 2 3 4   index of '2' is 2 = (5+1)/2 - 1
        }

        int[] cArray = new int[3];

        int ind0 = left;
        int ind1 = left + middleIndexOffset;
        int ind2 = right;

        cArray[0] = a[ind0];
        cArray[1] = a[ind1];
        cArray[2] = a[ind2];

        // various auxilliary arrays solely for the purpose of easy debug
        // trace printing.
        int[] o = new int[3];
        o[0] = cArray[0];
        o[1] = cArray[1];
        o[2] = cArray[2];

        int[] ii = new int[3];
        ii[0] = ind0;
        ii[1] = ind1;
        ii[2] = ind2;

        //p(m+" method "+pivotMethod+": len="+len+", odd="+odd+", middleIndexOffset="+
        // middleIndexOffset+", first["+ind0+"]="+o[0]+", middle["+ind1+"]="+o[1]+", last["+ind2+"]="+o[2]);

        //
        // stupid O(n**2) insertion sort
        // but this is a small data set and
        // we should use a proven sorting method inside of
        // a sorting method that we are creating (so that we can trust the results)
        //
        int[] sortedC = new int[3];
        int smallestJ = 0;
        for (int i = 0; i < cArray.length; i++) {
          int smallest = 100001;
          for (int j = 0; j < cArray.length; j++) {
            if (cArray[j] > 0 && cArray[j] < smallest) {
              smallest = cArray[j];
              smallestJ = j;
            }
          }
          sortedC[i] = smallestJ;
          cArray[smallestJ] = -9999;   // remove it from input
        }

        //p(m+" sorted index order: "+pInts(sortedC, left, right)+", elements: ["+ii[sortedC[0]]+"]="+o[sortedC[0]]+
        //                ",["+ii[sortedC[1]]+"]="+o[sortedC[1]]+", ["+ii[sortedC[2]]+"]="+o[sortedC[2]]);

        int ind = sortedC[1];
        int retVal = 0;
        if (ind == 0) retVal = ind0;
        else if (ind == 1) retVal = ind1;
        else retVal = ind2;
        //p("\n"+m+pInts(a, left, right));
        //p(m+" returning median index="+sortedC[1]+", index="+retVal+" array value="+o[sortedC[1]]+"\n");
        return retVal;

      default:
        throw new RuntimeException("\n\nunhandled pivot method " + pivotMethod + " !\n\n");
    }
  }

  /**
   * partition method
   *
   * @param a
   * @param left
   * @param right
   * @return new pivot array index
   */
  public int partition(int[] a, int left, int right, int pivotIndex) {
    String m = "partition: ";
    //p(m + "start left=" + left + ", right=" + right + ", pivotIndex=" + pivotIndex);
    //p(m + "input=" + pInts(a, left, right));

    // swap pivot to head
    int pivotVal = a[pivotIndex];
    int temp = a[left];
    a[left] = a[pivotIndex];
    a[pivotIndex] = temp;
    //p(m + "after pivot swap input=" + pInts(a, left, right));

    int i = left + 1;
    //p(m + "start at i=" + i);

    for (int j = left + 1; j <= right; j++) {
      //p(m + "loop j=" + j + ", j:a[" + j + "]=" + a[j] + ", pivotVal=" + pivotVal);

      if (a[j] < pivotVal) {
        if (i != j) {
          temp = a[j];
          a[j] = a[i];
          a[i] = temp;
        } else {
          //p(m+"a[j] < pivotVal: but i == j, so no swap ");
        }
        i++;
        //p(m + "a[j] < pivotVal: after swap i=" + i + " input=" + pInts(a, left, right));

      }
    }

    // replace pivot to final position
    temp = a[left];
    a[left] = a[i - 1];
    a[i - 1] = temp;

    //p(m+" returning new pivot index ="+(i - 1)+", final array=" + pInts(a, left, right));
    return (i - 1);
  }


  public static void main(String[] args) {
    String inputFName = null;
    if (args.length > 0) {
      inputFName = args[0];
    }

    int method = 1;
    runHomeWork(inputFName, method);
      /*
       pivot method 1 found 162085 comparisons.  in 0 seconds.

       */

    method = 2;
    runHomeWork(inputFName, method);

    /*  '

    pivot method 2 found 164123 comparisons.  in 0 seconds.

     */

    method = 3;
    runHomeWork(inputFName, method);

    /*
    pivot method 3 found 138382 comparisons.  in 0 seconds.

     */

  }

  private static void runHomeWork(String fName, int inputMethod) {
    int[] data = readDataFile(fName);
    long startTime = System.currentTimeMillis();

    QuickSort_countComparisons_Stanford_Coursera sort =
            new QuickSort_countComparisons_Stanford_Coursera();

    long result = sort.quickSort(data, inputMethod);
    long endTime = System.currentTimeMillis();

    long totalTime = (endTime - startTime) / 1000;
    System.err.println("\n\npivot method " + inputMethod + " found " + result + " comparisons.  in " + totalTime + " seconds.\n\n");
  }

  private static int[] readDataFile(String inputFName) {
    // 100,000 is a little less than 2**17
    //           so 17 levels of recursion
    //
    int[] data = new int[110000];  // should be big enough
    int[] dataNew = null;
    FileReader fileR = null;
    String f = "Coursera_QuickSort.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\sort";
    String fileName = d + "\\" + f;
    if (inputFName != null && inputFName.length() > 0) {
      fileName = inputFName;
    }
    try {
      fileR = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.err.println(" cannot open data file " + fileName);
    }

    // get count so that we can build only the array we need
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      int i = 0;
      while ((line = br.readLine()) != null) {
        int number = Integer.parseInt(line);
        data[i] = number;
        i++;
        if (i % 1000 == 0) {
          System.err.println("read integer #" + i + " = " + number);
        }
      }
      br.close();

      dataNew = new int[i];
      System.arraycopy(data, 0, dataNew, 0, i);

    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return dataNew;
  }

  private String pInts(int[] in) {
    return pInts(in, 0, in.length - 1);
  }

  private String pInts(int[] in, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < in.length; i++) {
      if (i >= start && i <= end) {
        sb.append(Integer.toString(in[i])).append(", ");
      }
    }
    return sb.toString();
  }

  public static void p(String s) {
    System.err.println("QuickSort: " + s);
  }
}

