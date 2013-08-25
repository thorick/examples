package datastructures.heap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/7/13
 * Time: 11:02 AM
 * <p/>
 * <p/>
 * Array based implementation of a Heap
 * <p/>
 * Binary Tree Structure
 * <p/>
 * Leaf Levels all filled completely from the left
 * (any empty children are at the leaf level and left children
 * are filled first so that any empty nodes are on the right)
 * <p/>
 * Parent always has higher or equal value than any child
 * <p/>
 * example:
 * X
 * T        O
 * B    S                  <<<  next child is  Left child of O
 * <p/>
 * Array representation:  start at element 1
 * <p/>
 * 0 1 2 3 4 5 6
 * X T O B S
 * <p/>
 * note that the tree constraints guarantee that the array always
 * fills up left to right starting at position 1,
 * with NO GAPS.
 * also the max value of the set is always at position 1.
 * <p/>
 * <p/>
 * given an element at position i:
 * it's parent is at position i/2  (for i > 1)
 * it's children are at 2i and 2i+1
 * <p/>
 * <p/>
 * based on Sedgewick ch 9
 * <p/>
 * standard:  highest node at index==1,  index==0 always empty
 */
public abstract class ArrayHeap<T extends Comparable<T>> {

  static Logger log = LogManager.getLogger(ArrayHeap.class.getName());

  //  TRUE:   MAX VALUES ON TOP
  //  FALSE:  MIN VALUES ON TOP

  protected boolean isMaxHeap = true;
  private int count;   // index of the last element  (1 less than the number of elements)
  private int keySize = 4;   // for now

  /**
   * Insert new item into Heap
   * at the next available array position '++count'
   * this is at the bottom of the Heap which may or may not be right
   * so..
   * <p/>
   * bubble up if reposition needed until it is not less than its parent
   *
   * @param e
   */
  public synchronized int insert(T e) {
    if (isP())
      log.debug("insert-start  count="+count+", before insert of node '" + e.toString() + ", Heap is " + toString());
    setNode(++count, e);

    if (isP())
      log.debug("insert    after setNode at count="+count+", Heap is "+toString())
              ;
    int index = nodeUp(count);

    if (isP())
      log.debug(" after  insert of node '" + e.toString() + ", at index=" + index + ",  Heap is " + toString());
    return index;
  }

  /**
   * max is the node at the top
   * <p/>
   * switch max with the end of the heap (last non-empty entry in array)
   * <p/>
   * we need to set what the new max will be after we've plucked the max
   * we swap down the switched node that is now at the root
   * this re-evaluation will put the highest of the root's children
   * at the top as we swap the switched node down the tree
   *
   * @return
   */
  public synchronized T removeTop() {
    if (count <= 0) {
      return null;
    }

    int lastIndex = count;

    if (isP())
      log.debug("exchange 1 and " + count + ":  " + getNode(1) + " , " + getNode(count));
    exchange(1, count);

    if (isP())
      log.debug("nodeDown 1 and " + (count - 1) + ":  " + getNode(1) + " , " + getNode(count - 1));
    nodeDown(1, (count - 1));

    T retVal = getNode(count);

    if (isP())
      log.debug("return getNode of " + count + ":  " + retVal.toString());

    count--;
    if (isP())
      log.debug("heap is now " + toString());

    return retVal;
  }

  public synchronized T peekTop() {
    return getNode(1);
  }

  /**
   * remove the node specifically at position
   *
   * @param index
   * @return
   */
  public synchronized T removeNode(int index) {
    if (isP())
      log.debug("removeNode-start   ["+index+"]="+getNode(index));

    if (count <= 0) {
      return null;
    }

    // swap with end of array
    exchange(index, count);

    nodeDown(index, (count - 1));

    T retVal = getNode(count);
    count--;

    if (isP())
      log.debug("removeNode-end  removed is "+retVal+", Heap is now "+toString());


    return retVal;
  }

  /**
   * This method will examine the Node at the index and determine
   * if it needs to be pushed down or bubbled up, then take the action.
   *
   * @param index
   * @return
   */
  public synchronized int repositionNode(int index) {
    if (index < 1 || index > count) {  return 0; }

    if (isMaxHeap) {
      if (index == 1) {
        if (count <= 1) return 1;
        if (greater(2, 1)) {
          return nodeUp(2);
        }
        else if (count >= 3) {
          if (greater(3, 1)) {
            return nodeUp(3);
          }
        }
        else {
          return 1;
        }
      }
      else if (less((index/2), index)) {
        // parent is less than us, go up
        return nodeUp(index);
      }
      else {
        return nodeDown(index, count);
      }
    }
    else {

      log.debug(" check greater: i/2="+(index/2)+", i="+index+", count="+count);
      if (index == 1) {
        if (count <= 1) return 1;
        if (less(2, 1)) {
          return nodeUp(2);
        }
        else if (count >= 3) {
          if (less(3, 1)) {
            return nodeUp(3);
          }
        }
        else {
          return 1;
        }
      }
      else if (greater((index/2), index)) {
        return nodeUp(index);
      }
      else {
        return nodeDown(index, count);
      }
    }
    // shutup the compiler
    System.err.println("ERROR !  reached end of reposition node for index="+index);
    return count;
  }

  public synchronized boolean empty() {
    return count == 0;
  }

  public synchronized int size() {
    return count+1;
  }

  private synchronized int incrementCount() {
    count++;
    return count;
  }

  private synchronized int decrementCount() {
    count--;
    return count;
  }

  /**
   * higher node bubble up
   * <p/>
   * if we are greater than any particular
   * <p/>
   * input:  index - array index of node
   */
  private synchronized int nodeUp(int index) {
    if (isP()) {
      if (index > 1) {
        log.debug("nodeUp-start ["+index+"]="+getNode(index)+", ["+(index/2)+"]="+getNode(index/2));
      }
    }

    while (index > 1 &&
            (isMaxHeap ?
                    (less((index / 2), index)) :
                    (greater((index / 2), index)))) {

      if (isP())
        log.debug("nodeUp    about to exchange node[" + index + "]=" + getNode(index).toString() + " with " +
                "node[" + (index / 2) + "]=" + getNode((index / 2)));

      exchange(index, (index / 2));
      index = index / 2;

      if (isP()) {
        log.debug("nodeUp    new index is now " + index+", heap is now "+toString());
      }
    }

    if (isP())
      log.debug("nodeUp-end  final index="+index+".  Heap is now "+toString());

    return index;
  }

  private synchronized int nodeDown(int i1, int i2) {
    if (isP())
      log.debug("nodeDown-start   i1=" + i1 + ", i2=" + i2);
    int j = i1;

    while ((2 * i1) <= i2) {
      j = 2 * i1;

      // note:  for the check less(j, j+1) to work here
      //         if there is no node at j+1,
      //         then  'less' MUST return *false*
      //         else  you're putting yourself into NULL territory
      //
      if (j < i2 &&
              (isMaxHeap ?
                      (less(j, j + 1)) :
                      greater(j, j + 1))) {
        j++;
      }
      if (!(isMaxHeap ?
              (less(i1, j)) :
              (greater(i1, j)))) {
        break;
      }
      exchange(i1, j);
      i1 = j;
    }

    if (isP())
      log.debug("after nodeDown  Heap is now "+toString());

    return j;
  }

  /**
   * @param i1
   * @param i2
   * @return must be called from a synchronized method
   *         <p/>
   *         condition:   i1 < i2  always
   *         <p/>
   *         if value at i1 is NULL then return false;  (value at i2 will be NULL)
   *         if value at i2 is NULL then return false;
   */
  private boolean less(int i1, int i2) {
    //if (i1 > i2) throw new RuntimeException("Error !  i1=" + i1 + ", is greater than i2=" + i2);
    if (i1 > count) return false;
    if (i2 > count) return false;
    T v1 = getNode(i1);
    T v2 = getNode(i2);
    if (v1.compareTo(v2) < 0) {
      return true;
    }
    return false;
  }

  /**
   * @param i1
   * @param i2
   * @return must be called from a synchronized method
   *         <p/>
   *         condition:   i1 > i2  always
   *         <p/>
   *         if value at i1 is NULL then return false;  (value at i2 will be NULL)
   *         if value at i2 is NULL then return false;
   */
  private boolean greater(int i1, int i2) {
    //if (i1 > i2) throw new RuntimeException("Error !  i1=" + i1 + ", is greater than i2=" + i2);
    if (i1 > count) return false;
    if (i2 > count) return false;
    T v1 = getNode(i1);
    T v2 = getNode(i2);
    if (v1.compareTo(v2) > 0) {
      return true;
    }
    return false;
  }

  private synchronized void exchange(int i1, int i2) {
    T temp = getNode(i1);
    setNode(i1, getNode(i2));
    setNode(i2, temp);

    if (isP())
      log.debug("exchange complete ["+i1+"]="+getNode(i1)+", ["+i2+"]="+getNode(i2)+", heap is now "+toString());
  }

  /**
   * return the node at index
   *   do not delete from array
   *
   * @param index
   * @return
   */
  abstract protected T getNode(int index);

  abstract protected void setNode(int index, T value);

  @Override
  public String toString() {
    return printHeap();
  }

  protected synchronized String printHeap() {
    StringBuilder sb = new StringBuilder("\nHeapArray isMaxHeap=" + isMaxHeap + ":\n");

    if (count == 0) {
      sb.append(" EMPTY ");
      return sb.toString();
    }

    // first print the array by element
    for (int i = 1; i <= count; i++) {
      sb.append("["+i+"]=");
      sb.append(getNode(i).toString()).append("\n");
    }
    sb.append("END.\n");

    /*
    sb.append("\n" +
            " Heap Hierarchy:\n");
    int levels = 1;
    int temp = count;
    while (temp > 1) {
      temp = temp / 2;
      levels++;
    }

    // start at topLevel and go down
    int currLevelNumber = 0;
    //int perLevel = 1;
    int maxThisLevel = 1;
    int currThisLevel = 1;
    for (int i = 1; i <= count; i++) {
      if (currThisLevel == 1) {
        sb.append(printPad(levels, ++currLevelNumber));
      }
      sb.append(getNode(i).toString()).append("  ");
      currThisLevel++;
      if (currThisLevel > maxThisLevel) {
        currLevelNumber++;
        maxThisLevel = maxThisLevel + maxThisLevel;
        currThisLevel = 1;    // reset to point to beginning
        sb.append("\n");
      }
    }
    sb.append("\n");
    */

    return sb.toString();
  }

  private String printPad(int totalLevels, int level) {
    int repeat = totalLevels - level + 1;
    String pad = "    " + "  ";     // assume keySize=4  add 2 spaces
    for (int i = 0; i < repeat; i++) {
      pad = pad + pad;
    }
    return pad;
  }

  private boolean isP() {
    return log.isDebugEnabled();

  }

}
