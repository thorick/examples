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
public class PriorityQueue_ArrayHeap_test
        extends PriorityQueue_base_test
{

  private Logger log =
          Logger.getLogger(PriorityQueue_ArrayHeap_test.class);

  protected PriorityQ<String> getPriorityQueue() {
    return new PriorityQueue_ArrayHeap_String(1000);
  }

  /*
  @Test
  public void basicTest() {
    String m = "basicTest: ";
    PriorityQ<String> q = new PriorityQueue_ArrayHeap_String();

    q.insert("11");
    q.insert("40");
    q.insert("20");
    q.insert("10");

    String max = q.removeTop();
    String message = "expected 40, got "+max;
    p(m+message);
    Assert.assertTrue(message, max.equals("40"));

    max = q.removeTop();
    message = "expected 20, got "+max;
    p(m+message);
    Assert.assertTrue(message, max.equals("20"));

    max = q.removeTop();
    message = "expected 11, got "+max;
    p(m+message);
    Assert.assertTrue(message, max.equals("11"));

    max = q.removeTop();
    message = "expected 10, got "+max;
    p(m+message);
    Assert.assertTrue(message, max.equals("10"));

    max = q.removeTop();
    message = "expected <empty> got '"+max+"'";
    p(m+message);
    Assert.assertTrue(message, max.length()==0);

  }

  private void p(String s) {
    log.debug(s);
  }
  */
}
