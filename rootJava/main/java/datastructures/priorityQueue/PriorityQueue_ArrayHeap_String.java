package datastructures.priorityQueue;

import datastructures.heap.ArrayHeap;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/7/13
 * Time: 2:36 PM
 */
public class PriorityQueue_ArrayHeap_String
    extends ArrayHeap<String>
      implements PriorityQ<String>
{
  private String[] heap;

  public PriorityQueue_ArrayHeap_String(int size) {
    heap = new String[size];
  }

  @Override
  public String removeTop() {
    String s = super.removeTop();
    return s == null ? "" : s;
  }

  @Override
  public String removeNode(int index) {
    String s = super.removeNode(index);
    return s == null ? "" : s;
  }

  public String getNode(int index) {
    return heap[index];
  }

  protected void setNode(int index, String value) {
    heap[index] = value;
  }
}
