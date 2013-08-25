package datastructures.priorityQueue;

import datastructures.BST.StringNode;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/6/13
 * Time: 12:28 PM
 *
 * Test Priority Queue based on a BST
 *
 * String keys
 *
 * Larger String Key value == greater priority
 *
 *
 */
public class PriorityQueueMin_ArrayHeap_test
        extends PriorityQueueMin_base_test
{

  private Logger log =
          Logger.getLogger(PriorityQueueMin_ArrayHeap_test.class);

  protected PriorityQ<String> getPriorityQueue() {
    return new PriorityQueue_ArrayHeapMin_String(1000);
  }

  @Override
  protected void testSetNode(PriorityQ<String> q, int index, String value) {
    ((PriorityQueue_ArrayHeapMin_String)q).setNode(index, value);
  }
}
