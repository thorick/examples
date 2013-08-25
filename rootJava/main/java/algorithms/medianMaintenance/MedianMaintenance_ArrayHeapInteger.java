package algorithms.medianMaintenance;

import datastructures.priorityQueue.PriorityQueue_ArrayHeapMin_Integer;
import datastructures.priorityQueue.PriorityQueue_ArrayHeap_Integer;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/11/13
 * Time: 1:38 PM
 *
 * When the number of elements is even,
 * then the median is taken to be the LESSER middle value.
 *
 * N=5   (5 + 1)/2 = 3   take from which ever Heap is the larger ?
 *   case:  1 2 3    4 5
 *
 *   case:  1 2    3 4 5
 *
 *
 * N=4   4/2  = 2
 *
 *   case:  1 2    3 4       always the LOW HEAP
 *
 *
 */
public class MedianMaintenance_ArrayHeapInteger {

  private Logger log =
          Logger.getLogger(MedianMaintenance_ArrayHeapInteger.class);


  PriorityQueue_ArrayHeapMin_Integer heapHigh;
  PriorityQueue_ArrayHeap_Integer    heapLow;

  int heapSize;

  int hCount = 0;    // kept outside of the heap for performance
  int lCount = 0;
  int currMedian = 0;

  public MedianMaintenance_ArrayHeapInteger(int heapSize) {
    this.heapSize = heapSize;
    heapHigh = new PriorityQueue_ArrayHeapMin_Integer(heapSize);
    heapLow = new PriorityQueue_ArrayHeap_Integer(heapSize);
  }

  public synchronized void newValue(int i) {
    Integer NextInt = new Integer(i);

    if (isP())
      log.debug("new i="+i+", currMedian="+currMedian);

    if (i > currMedian) {
      heapHigh.insert(NextInt);
      hCount++;
      if (isP())
        log.debug("inserted i="+i+" into High.  hCount="+hCount+"\n");

    } else {
      heapLow.insert(NextInt);
      lCount++;
      if (isP())
        log.debug("inserted i="+i+" into Low.   lCount="+hCount+"\n");

    }
    balanceHeaps();
    currMedian = getMedian();

  }

  public synchronized int getMedian() {
    if (isP())
      log.debug("getMedian:  hC="+hCount+", lC="+lCount);

    if (hCount > lCount) {
      if (isP())
        log.debug("getMedian: hC > lC "+hCount+" > "+lCount+"   returning high Top "+heapHigh.peekTop()+"\n");
      return heapHigh.peekTop();
    }
    else if (lCount > hCount) {
      if (isP())
              log.debug("getMedian: lC > hC "+lCount+" > "+hCount+"   returning low Top "+heapLow.peekTop()+"\n");
            return heapLow.peekTop();
    }
    if (isP())
           log.debug("getMedian: lC = hC "+lCount+" = "+hCount+"   returning low  Top "+heapLow.peekTop()+"\n");
    return heapLow.peekTop();
  }

  private synchronized void balanceHeaps() {
    int diff = hCount - lCount;
    if (isP())
      log.debug("balanceHeaps:  hC="+hCount+", lC="+lCount);

    if (diff > 1) {
      // transfer from Low to High
      heapLow.insert(heapHigh.removeTop());
      lCount++;
      hCount--;
      if (isP())
        log.debug("balanceHeaps:  moved from high to low. hC="+hCount+", lC="+lCount);

    } else if (diff < -1) {
      // transfer from High to Low
      heapHigh.insert(heapLow.removeTop());
      hCount++;
      lCount--;
      if (isP())
         log.debug("balanceHeaps:  moved from high to low. hC="+hCount+", lC="+lCount);
    }
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }

}
