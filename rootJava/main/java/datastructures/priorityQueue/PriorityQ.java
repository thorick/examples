package datastructures.priorityQueue;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 9:34 AM
 *
 * http://pages.cs.wisc.edu/~vernon/cs367/notes/11.PRIORITY-Q.html
 *
 *
 */
public interface PriorityQ <T extends Comparable<T>> {
  boolean empty();

  /**
   * Insert new Node
   * The int return value only has meaning in certain impls
   *   else it is always zero.
   *
   * for example in the Heap impl it is the index in the Heap Array
   * where the node was inserted into.
   *
   * @param n
   * @return
   */
  int insert(T n);

  T removeTop();

  /**
   * return the Top value without removing it from the Queue
   *
   * @return
   */
  T peekTop();

  /**
   * special case method (for now, only in the array impl)
   *
   * @return
   */
  int size();

  /**
   * special case method
   *
   * This method will examine the Node at the index and determine
   * if it needs to be pushed down or bubbled up, then take the action.
   *
   * @param index
   * @return
   */
  int repositionNode(int index);


  /**
   * special case method
   *
   * @param index
   * @return
   */
  T removeNode(int index);

  /**
   * special case method
   *
   * @param index
   * @return
   */
  T getNode(int index);
}
