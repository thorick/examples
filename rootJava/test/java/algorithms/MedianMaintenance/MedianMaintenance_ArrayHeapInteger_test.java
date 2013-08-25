package algorithms.MedianMaintenance;

import algorithms.medianMaintenance.MedianMaintenance_ArrayHeapInteger;
import datastructures.priorityQueue.PriorityQ;
import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/11/13
 * Time: 2:11 PM
 */
public class MedianMaintenance_ArrayHeapInteger_test {

  private Logger log =
          Logger.getLogger(MedianMaintenance_ArrayHeapInteger_test.class);

  @Test
  public void basicTest() {
    String m = "basicTest: ";
    MedianMaintenance_ArrayHeapInteger mm =
            new MedianMaintenance_ArrayHeapInteger(100);

    int[] stream = new int[] {1,5,7,2,20,40,30,80,95,2,100,90};
    int[] median = new int[] {1,1,5,2,5,5,7,7,20,7,20,20};
    int[] tracker = new int[stream.length];

    for (int i=0; i<stream.length; i++) {
      int currVal = stream[i];
      mm.newValue(currVal);
      int currMedian = mm.getMedian();

      tracker[i]=currVal;
      log.debug("["+i+"]="+currVal+", median="+currMedian+" of "+printSortedIntArray(tracker, (i+1))+"\n\n");
      //Assert.assertTrue("at entry="+i+", expected median="+median[i]+", but we found="+currMedian, (median[i] == currMedian));

    }
  }

  private String printSortedIntArray(int[] ia, int size) {
    StringBuilder sb = new StringBuilder();
    int[] copy = Arrays.copyOf(ia, size);
    Arrays.sort(copy);
    for (int i=0; i<copy.length ; i++)  {
      sb.append("["+i+"]="+copy[i]+",");
    }
    return sb.toString();
  }
}
