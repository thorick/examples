package datastructures.MST.prims_ArrayHeap;

import datastructures.heap.ArrayHeap;
import datastructures.priorityQueue.PriorityQ;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/8/13
 * Time: 2:58 PM
 */
public class PriorityQueue_ArrayHeapMin_PrimsVertex
  extends ArrayHeap<PrimsVertex>
  implements PriorityQ<PrimsVertex>
{

  private PrimsVertex[] heap;

  public PriorityQueue_ArrayHeapMin_PrimsVertex(int size) {
    heap = new PrimsVertex[size];
    isMaxHeap = false;
  }


  @Override
  public int insert(PrimsVertex dv) {
    int index = super.insert(dv);
    dv.setHeapArrayPosition(index);
    return index;
  }


  @Override
  public PrimsVertex removeNode(int index) {
    PrimsVertex pm = super.removeNode(index);
    pm.setHeapArrayPosition(0);
    return pm;
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
  @Override
  public void setNode(int index, PrimsVertex value) {
    heap[index] = value;
    value.setHeapArrayPosition(index);
  }


  @Override
  public PrimsVertex getNode(int index) {
    return heap[index];
  }


}
