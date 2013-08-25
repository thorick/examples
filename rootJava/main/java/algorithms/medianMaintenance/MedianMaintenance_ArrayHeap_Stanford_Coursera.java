package algorithms.medianMaintenance;

import datastructures.priorityQueue.PriorityQueue_ArrayHeapMin_Integer;
import datastructures.priorityQueue.PriorityQueue_ArrayHeap_Integer;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/7/13
 * Time: 8:41 PM
 * <p/>
 * The goal of this problem is to implement the "Median Maintenance" algorithm
 * (covered in the Week 5 lecture on heap applications).
 * <p/>
 * The text file contains a list of the integers from 1 to 10000 in unsorted order;
 * you should treat this as a stream of numbers, arriving one by one. Letting xi denote
 * the ith number of the file, the kth median mk is defined as the median of the numbers x1,…,xk.
 * <p/>
 * (So, if k is odd, then mk is ((k+1)/2)th smallest number among x1,…,xk;
 * <p/>
 * if k is even,
 * then mk is the (k/2)th smallest number among x1,…,xk.)
 * <p/>
 * In the box below you should type the sum of these 10000 medians, modulo 10000
 * <p/>
 * (i.e., only the last 4 digits). That is, you should compute (m1+m2+m3+⋯+m10000)mod10000.
 * <p/>
 * <p/>
 * OPTIONAL EXERCISE: Compare the performance achieved by heap-based and search-tree-based implementations of the algorithm.
 * <p/>
 * <p/>
 * recall:  use 2 heaps a MIN heap and a MAX heap
 * keep the  lowest  N/2 in MAX heap
 * keep the  highest N/2 in the MIN heap
 * <p/>
 * keep heap sizes balanced, when one heap larger than the other, transfer objects from one heap
 * to another.
 * <p/>
 * NO!
 *   if N = 5,  then  5 + 1 / 2 = 3
 *
 * N even smaller heap has median by definition above
 * N odd  the larger heap has the median
 *
 */

//
public class MedianMaintenance_ArrayHeap_Stanford_Coursera
        extends MedianMaintenance_Stanford_Coursera_Base {

  private Logger log =
          Logger.getLogger(MedianMaintenance_ArrayHeap_Stanford_Coursera.class);

  MedianMaintenance_ArrayHeapInteger mm;

  int[] inputStream;
  int   inputStreamLength;
  long medianSum = 0;   // running median SUM

  int currMedian = 0;

  public MedianMaintenance_ArrayHeap_Stanford_Coursera(FileResult fr) {
    inputStream = fr.array;
    inputStreamLength = fr.arraySize;
    mm = new MedianMaintenance_ArrayHeapInteger(HEAPSIZE);
  }

  public void processStream() {
    for (int i = 0; i < inputStreamLength; i++) {
      int nextInt = inputStream[i];
      mm.newValue(nextInt);
      currMedian = mm.getMedian();

      log.debug(i+" "+nextInt+"  medianSum ="+medianSum+"  currMedian="+currMedian);
      medianSum += currMedian;
      log.debug(i+" "+nextInt+"  addition  ="+medianSum+"\n");

    }
  }

  public long getAssignmentResult() {
    printAlways("sum of medians = " + medianSum);
    long retVal = medianSum % 10000;
    return retVal;
  }


  private boolean isP() {
    return log.isDebugEnabled();
  }

  protected static void printAlways(String s) {
    System.err.println(s);
  }


  public static void main(String[] args) {
    FileResult fr = readDataFile(null);

    MedianMaintenance_ArrayHeap_Stanford_Coursera runner =
            new MedianMaintenance_ArrayHeap_Stanford_Coursera(fr);

    runner.processStream();

    printAlways("result: " + runner.getAssignmentResult());

  }

  /*

  FIXED ARRAY ITERATION:

sum of medians = 46831213
result: 1213

   */

}
