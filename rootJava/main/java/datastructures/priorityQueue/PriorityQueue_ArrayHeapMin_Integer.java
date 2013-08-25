package datastructures.priorityQueue;

import datastructures.heap.ArrayHeap;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/7/13
 * Time: 2:36 PM
 */
public class PriorityQueue_ArrayHeapMin_Integer
    extends ArrayHeap<Integer>
      implements PriorityQ<Integer>
{
  private Integer[] heap;

  public PriorityQueue_ArrayHeapMin_Integer(int size) {
    heap = new Integer[size];
    isMaxHeap = false;    // smallest values on top
  }

  @Override
  public Integer removeTop() {
    Integer s = super.removeTop();
    return s;
  }

  @Override
  public Integer removeNode(int index) {
    Integer s = super.removeNode(index);
    return s;
  }

  public Integer getNode(int index) {
    return heap[index];
  }

  protected void setNode(int index, Integer value) {
    heap[index] = value;
  }
}
