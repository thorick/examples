package datastructures.priorityQueue;

import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/6/13
 * Time: 12:28 PM
 * <p/>
 * Test Priority Queue based on lowest value at the top
 * <p/>
 * String keys
 * <p/>
 * Smaller String Key value == smaller priority
 */
public abstract class PriorityQueueMin_base_test {

  private Logger log =
          Logger.getLogger(PriorityQueueMin_base_test.class);


  protected abstract PriorityQ<String> getPriorityQueue();

  protected abstract void testSetNode(PriorityQ<String> q, int index, String value);

  //@Test
  public void test00_basic() {

    PriorityQ<String> q = getPriorityQueue();

    createQueue_00(q);

    int index = 3;
    String value = q.getNode(index);
    String expected = "20";
    String message = "at index=" + 3 + ", expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));


    String max = q.removeTop();
    message = "expected 10, got " + max;
    log.debug(message);
    Assert.assertTrue(message, max.equals("10"));

    max = q.removeTop();
    message = "expected 11, got " + max;
    log.debug(message);
    Assert.assertTrue(message, max.equals("11"));

    max = q.removeTop();
    message = "expected 15, got " + max;
    log.debug(message);
    Assert.assertTrue(message, max.equals("15"));

    max = q.removeTop();
    message = "expected 20, got " + max;
    log.debug(message);
    Assert.assertTrue(message, max.equals("20"));

    max = q.removeTop();
    message = "expected 40, got " + max;
    log.debug(message);
    Assert.assertTrue(message, max.equals("40"));

    max = q.removeTop();
    message = "expected <empty> got '" + max + "'";
    log.debug(message);
    Assert.assertTrue(message, max.length() == 0);

  }


  @Test
  public void test01_removeFromMiddle() {

    PriorityQ<String> q = getPriorityQueue();

    createQueue_00(q);

    int index = 3;
    String value = q.getNode(index);
    String expected = "20";
    String message = "at index=" + index + ", expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));


    index = 3;
    value = q.removeNode(index);
    expected = "20";
    message = "removed node from index=" + index + ", expected " + expected + ", got " + value;
    log.debug(message);

    index = 3;
    value = q.getNode(index);
    expected = "15";
    message = "at index=" + index + ", expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));


    // force node back into [3] and verify
    testSetNode(q, index, value);
    value = q.getNode(index);
    expected = "15";
    message = "at index=" + index + ", expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));



    expected = "10";
    value = q.removeTop();
    message = "expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));

    expected = "11";
    value = q.removeTop();
    message = "expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));

    expected = "15";
    value = q.removeTop();
    message = "expected " + expected + ", got " + value;
    log.debug(message);
    Assert.assertTrue(message, value.equals(expected));


  }


  /**
   * HeapArray isMaxHeap=false:   first element at position 1
   * <p/>
   * 10  11  20  40  15
   *
   * @param q
   */
  private void createQueue_00(PriorityQ<String> q) {
    q.insert("11");
    q.insert("40");
    q.insert("20");
    q.insert("15");
    q.insert("10");
  }

}
