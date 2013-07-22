package datastructures.heap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/7/13
 * Time: 11:02 AM
 *
 *
 *  Array based implementation of a Heap
 *
 *  Binary Tree Structure
 *
 *  Leaf Levels all filled completely from the left
 *    (any empty children are at the leaf level and left children
 *     are filled first so that any empty nodes are on the right)
 *
 *  Parent always has higher or equal value than any child
 *
 *    example:
 *                 X
 *             T        O
 *          B    S                  <<<  next child is  Left child of O
 *
 *  Array representation:  start at element 1
 *
 *    0 1 2 3 4 5 6
 *      X T O B S
 *
 *  note that the tree constraints guarantee that the array always
 *  fills up left to right starting at position 1,
 *  with NO GAPS.
 *  also the max value of the set is always at position 1.
 *
 *
 *  given an element at position i:
 *      it's parent is at position i/2  (for i > 1)
 *      it's children are at 2i and 2i+1
 *
 *
 *  based on Sedgewick ch 9
 *
 *  standard:  highest node at index==1,  index==0 always empty
 *
 *
 */
public abstract class ArrayHeap<T extends Comparable<T>> {

  static Logger log = LogManager.getLogger(ArrayHeap.class.getName());

  private int count;   // number of elements
  private int keySize = 4;   // for now

  /**
   * Insert new item into Heap
   *   at the next available array position '++count'
   *   this is at the bottom of the Heap which may or may not be right
   *   so..
   *
   * bubble up if reposition needed until it is not less than its parent
   *
   * @param e
   */
  public synchronized void insert(T e) {
    String m = "insert: ";
    p(m+" before insert of node '"+e.toString()+", Heap is "+toString());
    setNode(++count, e);
    nodeUp(count);

    p(m+" after  insert of node '"+e.toString()+", Heap is "+toString());
  }

  /**
   * max is the node at the top
   *
   * switch max with the end of the heap (last non-empty entry in array)
   *
   * we need to set what the new max will be after we've plucked the max
   *     we swap down the switched node that is now at the root
   *     this re-evaluation will put the highest of the root's children
   *     at the top as we swap the switched node down the tree
   *
   *
   * @return
   */
  public synchronized T removeMax() {
    String m = "removeMax: ";

    if (count <= 0) { return null; }

    int lastIndex = count;

    p(m+"exchange 1 and "+count+":  "+getNode(1)+" , "+getNode(count));
    exchange(1, count);

    p(m+"nodeDown 1 and "+(count-1)+":  "+getNode(1)+" , "+getNode(count-1));
    nodeDown(1, (count - 1));

    T retVal = getNode(count);
    p(m+"return getNode of "+count+":  "+retVal.toString());

    count--;
    p(m+"heap is now "+toString());

    return retVal;
  }

  public synchronized boolean empty() {
    return count == 0;
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
   *  higher node bubble up
   *
   *  if we are greater than any particular
   *
   *  input:  index - array index of node
   *
   */
  private synchronized void nodeUp(int index) {
    String m = "nodeUp";
    if (index > 1) {
      p(m+" index="+index+" less("+(index/2)+", "+index+") "+getNode((index/2))+
              ", "+getNode(index)+" is "+less((index/2), index));

    }

    while (index > 1  &&  less((index/2), index)) {

      p(m+"about to exchange node["+index+"]="+getNode(index).toString()+" with "+
      "node["+(index/2)+"]="+getNode((index/2)));

      exchange(index, (index/2));
      index = index / 2;

      p(m+" new index is now "+index);
      if (index > 1) {
        p(m+" index="+index+" less("+(index/2)+", "+index+") "+getNode(index/2)+
                " , "+getNode(index)+" is "+less((index/2), index));
      }
    }
  }

  private synchronized void nodeDown(int i1, int i2) {
    String m = "nodeDown: ";

    p(m+" i1="+i1+", i2="+i2);

    while ((2 * i1) <= i2) {
      int j = 2 * i1;

      // note:  for the check less(j, j+1) to work here
      //         if there is no node at j+1,
      //         then  'less' MUST return *false*
      //         else  you're putting yourself into NULL territory
      //
      if (j < i2  &&  less(j, j+1)) {
        j++;
      }
      if (! less(i1, j)) {
        break;
      }
      exchange(i1, j);
      i1 = j;
    }
  }

  /**
   *
   * @param i1
   * @param i2
   * @return
   *
   * must be called from a synchronized method
   *
   * condition:   i1 < i2  always
   *
   * if value at i1 is NULL then return false;  (value at i2 will be NULL)
   * if value at i2 is NULL then return false;
   *
   */
  private boolean less(int i1, int i2) {
    if ( i1 > i2 )  throw new RuntimeException("Error !  i1="+i1+", is greater than i2="+i2);
    if ( i1 > count)  return false;
    if ( i2 > count)  return false;
    T v1 = getNode(i1);
    T v2 = getNode(i2);
    if (v1.compareTo(v2) < 0) { return true; }
    return false;
  }

  private synchronized  void exchange(int i1, int i2) {
    T temp = getNode(i1);
    setNode(i1, getNode(i2));
    setNode(i2, temp);
  }

  abstract protected T getNode(int index);
  abstract protected void setNode(int index, T value);

  @Override
  public String toString() {
    return printHeap();
  }

  protected synchronized String printHeap() {
    StringBuilder sb = new StringBuilder("\nHeapArray:\n");

    if (count == 0)  {
      sb.append(" EMPTY ");
      return sb.toString();
    }

    // first print the array by element
    for (int i=1; i <= count; i++) {
      sb.append(getNode(i).toString()).append("  ");
    }
    sb.append("\n\n Heap Hierarchy:\n");

    int levels = 1;
    int temp = count;
    while ( temp > 1 )  {
      temp = temp/2;
      levels++;
    }

    // start at topLevel and go down
    int currLevelNumber = 0;
    //int perLevel = 1;
    int maxThisLevel = 1;
    int currThisLevel = 1;
    for (int i=1; i<=count; i++) {
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
    return sb.toString();
  }

  private String printPad(int totalLevels, int level) {
    int repeat = totalLevels - level + 1;
    String pad = "    " + "  ";     // assume keySize=4  add 2 spaces
    for (int i=0; i < repeat; i++) {
       pad = pad + pad;
    }
    return pad;
  }

  private void p(String s) {
    log.debug(s);
  }

}
