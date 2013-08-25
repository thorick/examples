package datastructures.graph.shortestPath;

import datastructures.heap.ArrayHeap;
import datastructures.priorityQueue.PriorityQ;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/7/13
 * Time: 2:36 PM
 */
public class PriorityQueue_ArrayHeapMin_DijkstraVertex
    extends ArrayHeap<DijkstraVertex>
      implements PriorityQ<DijkstraVertex>
{
  private DijkstraVertex[] heap;

  public PriorityQueue_ArrayHeapMin_DijkstraVertex(int size) {
    heap = new DijkstraVertex[size];
    isMaxHeap = false;   // minimum values on top !
  }

  /*
  @Override
  public DijkstraVertex removeTop() {
    DijkstraVertex s = super.removeTop();
    return s;
  }
  */

  @Override
  public int insert(DijkstraVertex dv) {
    int index = super.insert(dv);
    dv.setHeapArrayPosition(index);
    return index;
  }

  @Override
  public DijkstraVertex removeNode(int index) {
    DijkstraVertex dj = super.removeNode(index);
    dj.setHeapArrayPosition(0);
    return dj;
  }

  public DijkstraVertex getNode(int index) {
    return heap[index];
  }

  /**
   * set Node at position AND update the DijkstraVertex's record of
   * where it is in the Heap.
   *
   * This should never
   *
   * @param index
   * @param value
   */
  public void setNode(int index, DijkstraVertex value) {
    heap[index] = value;
    value.setHeapArrayPosition(index);
  }
}
