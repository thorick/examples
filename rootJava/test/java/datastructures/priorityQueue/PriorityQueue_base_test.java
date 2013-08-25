package datastructures.priorityQueue;

import org.junit.Assert;
import org.junit.Test;
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
public abstract class PriorityQueue_base_test {

  private Logger log =
          Logger.getLogger(PriorityQueue_base_test.class);


  protected abstract PriorityQ<String> getPriorityQueue();


  @Test
  public void basicTest() {
    String m = "basicTest: ";
    PriorityQ<String> q = getPriorityQueue();

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

  protected void p(String s) {
    log.debug(s);
  }
}
