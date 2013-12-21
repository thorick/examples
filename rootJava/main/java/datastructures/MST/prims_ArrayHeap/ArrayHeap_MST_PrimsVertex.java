package datastructures.MST.prims_ArrayHeap;

import datastructures.heap.ArrayHeap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/6/13
 * Time: 5:39 AM
 * <p/>
 * Special purpose extension to ArrayHeap
 * <p/>
 * Allow full access to tree levels in the Heap
 * This is needed to handle cases in which
 * the minimum of key value is NOT the only criteria
 * for element selection,
 * but we want the ability to find the lowest key value
 * given other secondary criteria
 * In the case of Prims we need not just the vertex with the lowest weight edge
 * we also need to have it pointing back to a vertex in the already selected graph
 */
public class ArrayHeap_MST_PrimsVertex extends ArrayHeap<PrimsVertex> {

  static Logger log = LogManager.getLogger(ArrayHeap_MST_PrimsVertex.class);

  private PrimsVertex[] heap;

  public ArrayHeap_MST_PrimsVertex(int size) {
    isMaxHeap = false;     // min on top
    heap = new PrimsVertex[size];
  }


  /**
   * Start at the top of the Heap Array.
   * Go per level until we find
   * the first level that contains an entry whose 'otherVertex' is in the selectedVertices Set
   * scan that entire level for the LOWEST valued Vertex whose 'otherVertex' is in the selectedVertices Set
   * This is the lowest qualified entry
   * Select that entry and remove from heap
   * update the selectedVertices Set
   * return it.
   *
   * @param selectedVertices
   * @return
   */
  /*
  public PrimsVertex removeTopForSelectedVertex(Set selectedVertices) {


    // we have to do a linear array scan anyway, so might as well just keep running counters

    // empty heap
    if (count < 1) return null;

    int currIndex = 1;
    int currLevel = 0;
    int currStart = 1;
    int maxLevelIndex = 0;
    PrimsVertex currPV = null;
    PrimsVertex lowPV = null;
    int lowIndex = 0;

    // search to the end
    log.debug("begin: vSearch.");
    while (currIndex <= count) {

      // find the first (lowest edge weight) vertex whose 'other' head vertex is in the selected Set
      currPV = heap[currIndex];

      log.debug("-- try "+currIndex+": "+currPV);

      if (selectedVertices.contains(currPV.otherVertex)) {

        log.debug("---- found otherVertex: "+currPV.otherVertex+" in Set");
        // finish out the level and see if we are the LOWEST edge pointing back in the current level
        int level = levelForIndex(currIndex);
        int lastIndexForLevel = endIndexForLevel(level);
        lowPV = currPV;
        int lowWeight = lowPV.weight;
        lowIndex = currIndex;
        currIndex++;
        while (currIndex <= lastIndexForLevel) {
          currPV = heap[currIndex];
          if (selectedVertices.contains(currPV.otherVertex)) {
            if (currPV.weight < lowWeight) {
              lowPV = currPV;
              lowWeight = lowPV.weight;
              lowIndex = currIndex;
            }
          }
          currIndex++;
        }
        break;  // hit !   linked back to selected vertex Set
      }
      currIndex++;
    }

    if (lowIndex <= 0) return null;

    // remove the Vertex from the Heap

    PrimsVertex theNode = removeNode(lowIndex);

    // sanity
    if (theNode.thisVertex != lowPV.thisVertex) throw new RuntimeException(" bad lowest vertex, thisVertex mismatch !");
    if (theNode.weight != lowPV.weight) throw new RuntimeException(" bad lowest vertex, weight mismatch !");
    if (!theNode.otherVertex.equals(lowPV.otherVertex))
      throw new RuntimeException(" bad lowest vertex, otherVertex mismatch !");

    // add the Vertex to the Selected Set
    selectedVertices.add(theNode.thisVertex);

    // return the Vertex
    return theNode;
  }
  */

  @Override
  public PrimsVertex removeTop() {
    PrimsVertex s = super.removeTop();
    return s;
  }

  @Override
  public PrimsVertex removeNode(int index) {
    PrimsVertex s = super.removeNode(index);
    return s;
  }

  public PrimsVertex getNode(int index) {
    return heap[index];
  }

  protected void setNode(int index, PrimsVertex value) {
    heap[index] = value;
  }

}
